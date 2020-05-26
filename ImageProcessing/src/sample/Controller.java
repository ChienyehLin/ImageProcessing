package sample;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import javafx.event.ActionEvent;
import javafx.stage.StageStyle;
import org.omg.IOP.Encoding;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;

import static sun.swing.MenuItemLayoutHelper.max;

public class Controller implements Initializable {
    public  static File pcxfile;
    BufferedImage Image;
    byte [] bytes;
    myImage format;
    BufferedImage palette;
    ArrayList<Color> colors;
    @FXML ScrollPane scroll;
    @FXML TableView<MyDataType> table;
    @FXML TableColumn<MyDataType,String> property;
    @FXML TableColumn <MyDataType,Integer> value;
    @FXML private ImageView paletteView;
    @FXML ImageView image;
    @FXML ImageView Rimage;
    @FXML ImageView Gimage;
    @FXML ImageView Bimage;
    @FXML ImageView gray_negative;
    @FXML ImageView gray_gray;
    @FXML ImageView gray_Thresholding;
    @FXML BarChart histogram;
    @FXML CategoryAxis x;
    @FXML NumberAxis y;
    @FXML TabPane tab;
    @FXML Tab gray_tab;
    @FXML Tab opacity_tab;
    @FXML MenuItem Gray_Image;
    @FXML MenuItem Opacity;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
            tab.setVisible(false);
            table.setVisible(false);
            scroll.setVisible(false);
            gray_tab.setDisable(true);
            opacity_tab.setDisable(true);
            Image_operation.setDisable(true);
    }

    @FXML
    private void OpenFile(ActionEvent event) throws  Exception
    {
        FileChooser fileChooser;
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        pcxfile = fileChooser.showOpenDialog(null);
        if (pcxfile == null || !(pcxfile.getName().matches("..*\\.pcx"))) {
            System.out.println("Open pcx file error");
            return;
        }

        bytes = Files.readAllBytes(pcxfile.toPath());
        format  = new myImage(bytes);
        System.out.println(pcxfile.getName() +"   "+bytes.length);
        setTable(format);//讀取檔案後設定table資訊
        setPalette();
        setImage();
        setRGB_negativeImage();
        setHistogram();
        //System.out.println((bytes[bytes.length-768]));
        Gray_Image.setDisable(false);//讓 menu啟用
        Opacity.setDisable(false);//讓 menu啟用
        height=format.Ymax-format.Ymin+1;
        width=format.Xmax-format.Xmin+1;
        setStatusBar(image);//將讀入檔案第一個image加入 statusBar
        setStatusBar(Rimage);//將讀入檔案第一個image加入 statusBar
        setStatusBar(Gimage);//將讀入檔案第一個image加入 statusBar
        setStatusBar(Bimage);//將讀入檔案第一個image加入 statusBar
        setStatusBar(negative);//將讀入檔案第一個image加入 statusBar
    }
    @FXML Label image_coordinate;
    @FXML Label R_status;
    @FXML Label G_status;
    @FXML Label B_status;
    @FXML ImageView image_color_status;
    public  void setStatusBar( ImageView img){
            img.setOnMouseMoved(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    int x=(int)event.getX();
                    int y=(int)event.getY();
                    image_coordinate.setText("X: "+x+"   Y: "+y);
                    BufferedImage temp_buff_img= SwingFXUtils.fromFXImage(img.getImage(),null);//將取得的imageview轉為buff
                    try{
                    int rgb =temp_buff_img.getRGB(x,y);//取得滑鼠的rgb
                        R_status.setTextFill(javafx.scene.paint.Color.web("#ff0000"));
                        G_status.setTextFill(javafx.scene.paint.Color.web("#00ff00"));
                        B_status.setTextFill(javafx.scene.paint.Color.web("#0000ff"));
                        R_status.textProperty().setValue("R: "+((rgb&0x00ff0000)>>16));
                        G_status.textProperty().setValue("G: "+((rgb&0x0000ff00)>>8));
                        B_status.textProperty().setValue("B: "+((rgb&0x000000ff)));
                        BufferedImage color = new BufferedImage(26,26,BufferedImage.TYPE_INT_ARGB);
                        Graphics2D    graphics = color.createGraphics(); //畫筆
                        graphics.setPaint(new Color(rgb));//設定顏色
                        graphics.fillRect(0,0,26,26);//劃出滑鼠所在點顏色
                        image_color_status.setImage(SwingFXUtils.toFXImage(color,null));//將滑鼠所在點顏色 buffimg放入 imageview
                    }catch (Exception e)//因為imageview會回傳大於臨界值
                    {
                        System.out.println("x:"+x+"y:"+y);
                    }



                }
            });

    }
    @FXML
    TextField threshold_txt;
    public void setGray()
    {
        gray_negative.setFitHeight(format.Xmax-format.Xmin+1);
        gray_negative.setFitWidth (format.Ymax-format.Ymin+1);
        gray_gray.setFitHeight(format.Xmax-format.Xmin+1);
        gray_gray.setFitWidth (format.Ymax-format.Ymin+1);
        gray_Thresholding.setFitHeight(format.Xmax-format.Xmin+1);
        gray_Thresholding.setFitWidth (format.Ymax-format.Ymin+1);
        gray = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);//在imageview中顯示灰階
        threshold = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);//在imageview中顯示二值化
        BufferedImage gray_negative = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);

        for(int i =0; i< format.Xmax-format.Xmin+1;i++) {
            for (int s = 0; s < format.Ymax - format.Ymin + 1; s++) {
                int gray_value =new Double((format.image.getRGB(i,s)&0x000000ff)*0.299+((format.image.getRGB(i,s)>>8)&0x000000ff)*0.587+((format.image.getRGB(i,s)>>16)&0x000000ff)*0.114).intValue();
                gray_negative.setRGB(i,s,0xff000000|(255-gray_value)<<16|(255-gray_value)<<8|(255-gray_value));
                gray.setRGB(i,s,0xff000000|gray_value<<16|gray_value<<8|gray_value);
                threshold.setRGB(i,s,0xff000000|gray_value<<16|gray_value<<8|gray_value);
            }
        }
        this.gray_negative.setImage(SwingFXUtils.toFXImage(gray_negative, null));
        gray_gray.setImage(SwingFXUtils.toFXImage(gray, null));
        gray_Thresholding.setImage(SwingFXUtils.toFXImage(threshold, null));
        gray_tab.setDisable(false);
        gray_slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        Thresholding();
                        threshold_txt.setText("Threshold: "+ new Double(gray_slider.getValue()).intValue());
            }
        });
        draw_gray_histogram();
    }
    BufferedImage threshold;
    BufferedImage gray;
    @FXML Slider gray_slider;
    public void openBMP()throws  Exception{

        FileChooser fileChooser;
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open BMP");
        File bmp = fileChooser.showOpenDialog(null);
        BufferedImage image = ImageIO.read(bmp);
        this.image.setImage(SwingFXUtils.toFXImage(image,null));
        tab.setVisible(true);


        BufferedImage  gray_filtered = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(image.getWidth()+2,image.getHeight()+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<image.getHeight()+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < image.getWidth()+1; x++) {
                temp.setRGB(x,y,image.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<image.getWidth()+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,image.getRGB(0,0));
                temp.setRGB(x,image.getHeight()+1,image.getRGB(0,image.getHeight()-1));
            }
            else if(x==image.getWidth()+1){//設定右邊邊角
                temp.setRGB(x,0,image.getRGB(image.getWidth()-1,0));
                temp.setRGB(x,image.getHeight()+1,image.getRGB(image.getWidth()-1,image.getHeight()-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,image.getRGB(x-1,0));
                temp.setRGB(x,image.getHeight()+1,image.getRGB(x-1,image.getHeight()-1));

            }
        }
        for(int y=1;y<image.getHeight()-1;y++){//填補左右兩邊
            temp.setRGB(0,y,image.getRGB(0,y-1));
            temp.setRGB(image.getWidth()+1,y,image.getRGB(image.getWidth()-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<image.getHeight()+1;y++) {
            for (int x = 1; x < image.getWidth()+1; x++) {
                neighbor[0]=temp.getRGB(x-1,y-1)&0x000000ff;
                neighbor[1]=temp.getRGB(x,y-1)&0x000000ff;
                neighbor[2]=temp.getRGB(x+1,y-1)&0x000000ff;
                neighbor[3]=temp.getRGB(x-1,y)&0x000000ff;
                neighbor[4]=temp.getRGB(x,y)&0x000000ff;
                neighbor[5]=temp.getRGB(x+1,y)&0x000000ff;
                neighbor[6]=temp.getRGB(x-1,y+1)&0x000000ff;
                neighbor[7]=temp.getRGB(x,y+1)&0x000000ff;
                neighbor[8]=temp.getRGB(x+1,y+1)&0x000000ff;
                double average=0;
                average+=Math.abs((neighbor[6]+2*neighbor[7]+neighbor[8])-(neighbor[0]+2*neighbor[1]+neighbor[2]));
                average+=Math.abs((neighbor[2]+2*neighbor[5]+neighbor[8])-(neighbor[0]+2*neighbor[3]+neighbor[6]));
                int newRGB=(int)Math.round(average);
                if(newRGB>128)
                    newRGB=255;
                else if(  newRGB <=128)
                    newRGB=0;
                //  System.out.println(newRGB);
                gray_filtered.setRGB(x-1,y-1,0xff000000|newRGB<<16|newRGB<<8|newRGB);

            }
        }
       Rimage.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
      //  filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        ImageIO.write(gray_filtered, "png", new File("MyImage.bmp"));




    }
    public void Thresholding()
    {
        for(int i =0; i< format.Xmax-format.Xmin+1;i++) {
            for (int s = 0; s < format.Ymax - format.Ymin + 1; s++) {
                int gray_value = gray.getRGB(i,s)&0x000000ff;
                if(gray_value>=gray_slider.getValue())
                    threshold.setRGB(i,s,0xffffffff);
                else
                    threshold.setRGB(i,s,0xff000000);
            }
        }
        gray_Thresholding.setImage(SwingFXUtils.toFXImage(threshold, null));
        draw_gray_histogram();
    }
    public void setBitSlicing() throws  Exception//把每個bit拆開該bit是0則設0為1則設255
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/BitSlicing.fxml"));

        Parent root = (Parent)fxmlLoader.load();
        BitSlicing bitSlicing=fxmlLoader.getController();
        bitSlicing.setGray(gray);
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Bit Slicing");
        stage.getIcons().add(new Image("/sample/icon.png"));
        stage.show();
    }

    @FXML ImageView operation_original;
    @FXML ImageView operation_processed;
    @FXML Tab Image_operation;
    @FXML Slider magnify;
    @FXML Slider reduce;
    @FXML Slider magnify2;//線性差值
    @FXML Slider reduce2;
    @FXML TextField magnify2_text;
    @FXML TextField reduce2_text;
    @FXML TextField magnify_text;
    @FXML TextField reduce_text;
    int width;
    int height;
    int[]otsu_threshold;//用來記錄多階otsu的值以便畫圖
    @FXML BarChart otsu_histogram;
    public void draw_gray_histogram()//根據otsu/threshold結果設定histogram
    {
        otsu_histogram.setAnimated(false);
        int []gray_histogram= new int[256];
        otsu_histogram.getData().clear();
        for(int i =0; i< format.Xmax-format.Xmin+1;i++) {
            for (int s = 0; s < format.Ymax - format.Ymin + 1; s++) {
               gray_histogram[threshold.getRGB(i,s)&0x000000ff]++;//計算各灰階的個數
            }
        }
        XYChart.Series graySeries = new XYChart.Series<>();graySeries.setName("Gray");
        for (int i =0; i< 256;i++)
            graySeries.getData().add(new XYChart.Data(String.valueOf(i),gray_histogram[i]));
        otsu_histogram.getData().addAll(graySeries);
        //otsu_histogram.getXAxis().setOpacity(0);
        otsu_histogram.setVisible(true);
    }
    public void setOtsu()//先設定一次otsu
    {
        otsu_threshold_list.clear();
        otsu_times=0;
        otsu_level.setText("Otsu Level:"+ new Double(Math.pow(2,otsu_times)).intValue());
        double max_g=0;
        int otsu=0;
        double g_of_otsuThreshold= 0;//用來暫存不同threshold時其g，g最大時為otsu臨界值https://blog.csdn.net/piaoxuezhong/article/details/78302893
        for(int i=0;i<=255;i++)
        {
            double w0=0;//小於threshold機率
            double w1=0;//大於threshold機率
            int N0=0;//小於threshold個數
            int N1=0;//大於threshold個數
            double u0=0;//小於threshold的平均灰階值
            double u1=0;//大於threshold的平均灰階值
            for(int y=0;y<height;y++)
            {
                for(int x=0;x<width;x++)
                {
                    int gray_value = gray.getRGB(x,y)&0x000000ff;
                    if(gray_value>=i){//目前灰階值大於第s灰階
                        N1++;
                        u1+=gray_value;}//先計算灰階值加總
                    else{
                        N0++;
                        u0+=gray_value;}//先計算灰階值加總
                }
            }
            u0=u0/N0;
            u1=u1/N1;
            w1=1.0*N1/(format.Ymax-format.Ymin+1)*(format.Xmax-format.Xmin+1);
            w0=1.0*N0/(format.Ymax-format.Ymin+1)*(format.Xmax-format.Xmin+1);
            g_of_otsuThreshold=w0*w1*(u0-u1)*(u0-u1);
            if(g_of_otsuThreshold>max_g){
                max_g=g_of_otsuThreshold;
                otsu=i;
            }
        }
        for(int y=0;y<height;y++)
        {
            for(int x=0;x<width;x++) {
                int gray_value = gray.getRGB(x, y) & 0x000000ff;
                if (gray_value >= otsu)
                    threshold.setRGB(x,y,0xffffffff);
                else
                    threshold.setRGB(x,y,0xff000000);
            }
        }
        gray_Thresholding.setImage(SwingFXUtils.toFXImage(threshold, null));//將Threshold做完得gray image值放入image view
        gray_slider.setValue(otsu);
        otsu_threshold = new  int[2];
        otsu_threshold[0]=0;
        otsu_threshold[1]=otsu;
        otsu_threshold[1]=256;
        draw_gray_histogram();//劃出grayImage的histogram但未畫出threshold
        XYChart.Series thresholdSeries = new XYChart.Series<>();thresholdSeries.setName("Threshold");

        thresholdSeries.getData().add(new XYChart.Data(String.valueOf(otsu),50000));
        otsu_histogram.getData().addAll(thresholdSeries);
    }
    @FXML Slider directRotation;
    @FXML TextField directRotation_text;
    @FXML Slider inverseRotation;
    public void directRotate() {
        directRotation.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double theta=directRotation.getValue();
                double[][][] coordinate = new double[width][height][2];
                for (int y = 0; y < width; y++) {
                    for (int x = 0; x < height; x++) {
                        coordinate[x][y][0] = ( x-127.5) * Math.cos(theta) - Math.sin(theta) * (127.5- y);//先把Index轉換成正常坐標系再計算旋轉
                        coordinate[x][y][1] = ( x-127.5) * Math.sin(theta) + Math.cos(theta) * (127.5 - y);
                    }
                }
                double min_x=0;
                double max_x=0;
                double min_y=0;
                double max_y=0;
                for (int y = 0; y < width; y++) {//找出最大x,y以建立新imageView
                    for (int x = 0; x < height; x++) {
                        {
                            if(coordinate[x][y][0]>max_x)
                                max_x=coordinate[x][y][0];
                            if(coordinate[x][y][1]>max_y)
                                max_y=coordinate[x][y][1];
                            if(coordinate[x][y][0]<min_x)
                                min_x=coordinate[x][y][0];
                            if(coordinate[x][y][1]<min_y)
                                min_y=coordinate[x][y][1];
                        }
                    }
                }
                int new_width = new Long(Math.round(max_x-min_x)).intValue()+1;
                int new_height = new Long(Math.round(max_y-min_y)).intValue()+1;
                directRotation_text.setText("width: "+new_width +"height: "+new_height);
                BufferedImage rotate = new BufferedImage(new_width,new_height,BufferedImage.TYPE_INT_ARGB);
               // System.out.println(coordinate[128][128][0]+" " +coordinate[128][128][1]);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int rgb=format.image.getRGB(x,y);
                       //

                        try {
                            //rotate.setRGB(new Long(Math.round(coordinate[x][y][0]+ ((new_width*1.0)/ 2))).intValue() , new Long(Math.round(-coordinate[x][y][1] +(new_height *1.0)/ 2)).intValue(), rgb);
                            rotate.setRGB(new Long(Math.round(coordinate[x][y][0]+ max_x)).intValue() , new Long(Math.round(-coordinate[x][y][1] +max_y)).intValue(), rgb);
                        }catch (Exception e)
                        {
                            System.out.println("width:"+new_width+"height"+new_height);
                            System.out.println(coordinate[x][y][0]+" " +(coordinate[x][y][1]));
                            System.out.println(new Double(Math.round(coordinate[x][y][0]+ ((new_width*1.0)/ 2)) ).intValue()+" "+ new Long(Math.round(-coordinate[x][y][1] +new_height *1.0)/ 2).intValue());
                        }
                    }

                }
                operation_processed.setFitWidth(new_width);
                operation_processed.setFitHeight(new_height);
                operation_processed.setImage(SwingFXUtils.toFXImage(rotate,null));
            }
        });

    }
    @FXML TextField inverseRotation_text;
    public  void inverseRotation()
    {
        inverseRotation.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                //利用四個頂點找最長width,height
                //建立新大小矩陣計算對應反轉對應到原本的點
                double theta=inverseRotation.getValue();
                double[][]coordinate = new double[4][2];
                coordinate [0][0]= -127.5*Math.cos(theta) - Math.sin(theta) * (127.5);//左上角旋轉後x
                coordinate [0][1]= -127.5 * Math.sin(theta) + Math.cos(theta) * (127.5);//左上角旋轉後y
                coordinate [1][0]= 127.5*Math.cos(theta) - Math.sin(theta) * (127.5);//右上角旋轉後x
                coordinate [1][1]=127.5 * Math.sin(theta) + Math.cos(theta) * (127.5);//右上角旋轉後y
                coordinate [2][0]= -127.5*Math.cos(theta) - Math.sin(theta) * (-127.5);//左下角旋轉後x
                coordinate [2][1]=-127.5 * Math.sin(theta) + Math.cos(theta) * (-127.5);//左下角旋轉後y
                coordinate [3][0]= 127.5*Math.cos(theta) - Math.sin(theta) * (-127.5);//右下角旋轉後x
                coordinate [3][1]=127.5 * Math.sin(theta) + Math.cos(theta) * (-127.5);//右下角旋轉後y
                double min_x=0;
                double max_x=0;
                double min_y=0;
                double max_y=0;
                for (int y = 0; y < 4; y++) {//找出最大x,y以建立新imageView
                    if(coordinate[y][0]>max_x)
                        max_x=coordinate[y][0];
                    if(coordinate[y][1]>max_y)
                        max_y=coordinate[y][1];
                    if(coordinate[y][0]<min_x)
                        min_x=coordinate[y][0];
                    if(coordinate[y][1]<min_y)
                        min_y=coordinate[y][1];
                }
                int new_width = new Long(Math.round(max_x-min_x)).intValue()+1;
                int new_height = new Long(Math.round(max_y-min_y)).intValue()+1;
                inverseRotation_text.setText("width:"+new_width +" height:"+new_height);
                BufferedImage rotate = new BufferedImage(new_width,new_height,BufferedImage.TYPE_INT_ARGB);
                // System.out.println(coordinate[128][128][0]+" " +coordinate[128][128][1]);
               // double [][][]new_coordinate =new double[new_width][new_height][2];//新座標放對應到原本座標的點
                operation_processed.setFitWidth(new_width);
                operation_processed.setFitHeight(new_height);
                for (int y = 0; y < new_height; y++) {
                    for (int x = 0; x < new_width; x++) {
                            double original_x = (x - new_width / 2.0) * Math.cos(-theta) - Math.sin(-theta) * (new_height / 2.0 - y);//取得轉換回去原本的x
                            double original_y = (x - new_width / 2.0) * Math.sin(-theta) + Math.cos(-theta) * (new_height / 2.0 - y);//取得轉換回去原本的y
                            int original_x_index = (int) Math.round(original_x + 127.5);
                            int original_y_index = (int) Math.round(127.5 - original_y);
                        try {
                            if(original_x_index>=0&&original_x_index<256&&original_y_index>=0&&original_y_index<256){
                                int rgb = format.image.getRGB(original_x_index, original_y_index);
                                rotate.setRGB(x, y, rgb);
                          }
                        }catch (Exception e)
                        {
                            e.printStackTrace();
                            System.out.println("width "+new_height+" height"+new_width);
                            System.out.println(original_x_index+" "+original_y_index);
                        }
                    }
                }
               // System.out.println(rotate.getWidth()+" "+rotate.getHeight());
                operation_processed.setImage(SwingFXUtils.toFXImage(rotate,null));
            }
        });
    }

    int otsu_times=0;//用過了幾次otsu
    @FXML Button otsu_level;
    @FXML
    TextArea otsu_threshold_list;
    public  void setOtsu_level(){
        otsu_threshold_list.clear();
        otsu_level.setText("Otsu Level:"+(int)Math.pow(2,otsu_times));
        ArrayList <Integer>new_otsu_threshold= new ArrayList();
        otsu_times+=1;
        new_otsu_threshold.add(256);
        for(int i=0;i<otsu_threshold.length-1;i++)//iterate previous thresholds
        {
            new_otsu_threshold.add(otsu_threshold[i]);//把舊的threshold (ex: 0, 110, 256)放入新的
            double max_g=0;//用來比較最大g
            int otsu=0;//暫存otsu值
            double g_of_otsuThreshold= 0;//用來暫存不同threshold時其g，g最大時為otsu臨界值https://blog.csdn.net/piaoxuezhong/article/details/78302893
            for(int s=otsu_threshold[i];s<otsu_threshold[i+1];s++)
            {
                double w0=0;//小於threshold機率
                double w1=0;//大於threshold機率
                int N0=0;//小於threshold個數
                int N1=0;//大於threshold個數
                double u0=0;//小於threshold的平均灰階值
                double u1=0;//大於threshold的平均灰階值
                for(int y=0;y<height;y++)
                {
                    for(int x=0;x<width;x++)
                    {
                        int gray_value = gray.getRGB(x,y)&0x000000ff;
                        if(gray_value>=s&&gray_value<otsu_threshold[i+1]){//目前灰階值大於第s灰階 且小於下個threshold
                            N1++;
                            u1+=gray_value;}//先計算灰階值加總
                        else if(gray_value<s&&gray_value>=otsu_threshold[i]){//目前灰階值小於灰階值s 且大於上個threshold
                            N0++;
                            u0+=gray_value;}//先計算灰階值加總
                    }
                }
                u0=u0/N0;
                u1=u1/N1;
                w1=1.0*N1/width*height;
                w0=1.0*N0/width*height;
                g_of_otsuThreshold=w0*w1*(u0-u1)*(u0-u1);
                if(g_of_otsuThreshold>max_g){
                    max_g=g_of_otsuThreshold;
                    otsu=s;
                }
            }
            new_otsu_threshold.add(otsu);//把計算完的threshold放入arraylist
            for(int y=0;y<height;y++)
            {
                for(int x=0;x<width;x++) {
                    int gray_value = gray.getRGB(x, y) & 0x000000ff;
                    if (gray_value >= otsu&&gray_value<otsu_threshold[i+1])//若大於當前otsu，顏色設為下一個threshold
                        threshold.setRGB(x,y,0xff000000|(otsu_threshold[i+1]-1)<<16|(otsu_threshold[i+1]-1)<<8|(otsu_threshold[i+1]-1));
                    else if(gray_value<otsu&&gray_value>=otsu_threshold[i])
                        threshold.setRGB(x,y,0xff000000|otsu_threshold[i]<<16|otsu_threshold[i]<<8|otsu_threshold[i]);
                }
            }
        }
        gray_Thresholding.setImage(SwingFXUtils.toFXImage(threshold, null));//將Threshold做完得gray image值放入image view
        otsu_threshold = new int [new_otsu_threshold.size()];
        for(int index =0;index<new_otsu_threshold.size();index++)
            otsu_threshold[index]=new_otsu_threshold.get(index);
        java.util.Arrays.sort(otsu_threshold);
        for(int index =1;index<new_otsu_threshold.size()-1;index++)
            otsu_threshold_list.appendText(new Integer(otsu_threshold[index]).toString()+"\n");
        draw_gray_histogram();//劃出grayImage的histogram但未畫出threshold


        XYChart.Series thresholdSeries = new XYChart.Series<>();thresholdSeries.setName("Threshold");
        for(int i=1;i<otsu_threshold.length-1;i++)//劃出threshold
            thresholdSeries.getData().add(new XYChart.Data(String.valueOf(otsu_threshold[i]),50000));
        otsu_histogram.getData().addAll(thresholdSeries);
    }
    public  void setHistogramEqualization()//直方圖均衡化
    {
        double []pdf=new double[256];//用來存取機率，先存pdf
        double []cdf=new double[256];//再算cdf放進去
        int total_pixels=width*height;
        for (int y=0;y<height;y++)
            for(int x=0;x<width;x++)
                pdf[gray.getRGB(x,y)&0x000000ff]+=1.0/total_pixels;//計算pdf

        for(int i=0;i<256;i++)
            for(int s=0;s<=i;s++)
                cdf[i]=cdf[i]+pdf[s];//計算cdf
        for (int y=0;y<height;y++) {
            for (int x = 0; x < width; x++) {
                int new_gray_value = (int)(cdf[gray.getRGB(x, y)&0x000000ff] * 255);//新灰階值為cdf*255;
                threshold.setRGB(x, y, 0xff000000 | new_gray_value << 16 | new_gray_value << 8 | new_gray_value);
            }
        }
        gray_Thresholding.setImage(SwingFXUtils.toFXImage(threshold,null));
        draw_gray_histogram();



    }
    public  void waterMark() throws Exception//設定浮水印
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/WaterMark.fxml"));

        Parent root = (Parent)fxmlLoader.load();
        WaterMark waterMark=fxmlLoader.getController();
        waterMark.setGray(gray);
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("WaterMark");
        stage.getIcons().add(new Image("/sample/icon.png"));
        stage.show();
    }
    public  void histogramSpecification() throws  Exception
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/histogramSpecification.fxml"));

        Parent root = (Parent)fxmlLoader.load();
        HistogramSpecification histogramSpecification =fxmlLoader.getController();
        histogramSpecification.set(gray);
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("WaterMark");
        stage.getIcons().add(new Image("/sample/icon.png"));
        stage.show();
    }
    public  void contrastStreching() throws  Exception
    {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/sample/contrastStretching.fxml"));

        Parent root = (Parent)fxmlLoader.load();
        ContrastStretching contrastStretching=fxmlLoader.getController();
        contrastStretching.setOriginal(gray);
        Stage stage = new Stage();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Contrast Stretching");
        stage.getIcons().add(new Image("/sample/icon.png"));
        stage.show();
    }

    public void magnify()//使用複製填補方法放大
    {
        magnify.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double ratio = magnify.getValue();
                magnify_text.setText("Magnify Ratio: "+magnify.getValue());
                int height=new Long(Math.round(ratio*(format.Xmax-format.Xmin+1))).intValue();
                int width= new Long(Math.round(ratio*(format.Ymax-format.Ymin+1))).intValue();
                BufferedImage processed= new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);//處理過的影像待生成
                operation_processed.setFitWidth(width);
                operation_processed.setFitHeight(height);
                for (int i=0;i<height;i++)
                    for(int j=0; j<width;j++)
                        processed.setRGB(i,j,format.image.getRGB(new Double(i/ratio).intValue(),new Double(j/ratio).intValue()));

                operation_processed.setImage(SwingFXUtils.toFXImage(processed,null));
            }
        });

    }
    public void magnify2()//線性插值法
    {
        magnify2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double ratio = magnify2.getValue();
                magnify2_text.setText("Magnify Ratio: "+magnify2.getValue());
                int height=new Long(Math.round(ratio*(format.Xmax-format.Xmin+1))).intValue();
                int width= new Long(Math.round(ratio*(format.Ymax-format.Ymin+1))).intValue();
                BufferedImage processed= new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);//處理過的影像待生成
                operation_processed.setFitWidth(width);
                operation_processed.setFitHeight(height);
                for (int i=0;i<height;i++)
                    for(int j=0; j<width;j++) {
                        Double x = new Double(i / ratio);//單純計算後的x
                        Double y = new Double(j / ratio);//單純計算後的y
                        int previousPixelX = x.intValue();
                        int previousPixelY = y.intValue();
                        if(previousPixelX ==255||previousPixelY==255)
                        {
                            processed.setRGB(i, j, format.image.getRGB(previousPixelX,previousPixelY));
                        }
                        else{
                            double x1y1_r = ((format.image.getRGB(previousPixelX, previousPixelY) & 0x00ff0000) >> 16) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY) & 0x00ff0000) >> 16) * (x - previousPixelX);
                            double x1y1_g = ((format.image.getRGB(previousPixelX, previousPixelY) & 0x0000ff00) >> 8) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY) & 0x0000ff00) >> 8) * (x - previousPixelX);
                            double x1y1_b = ((format.image.getRGB(previousPixelX, previousPixelY) & 0x000000ff)) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY) & 0x000000ff)) * (x - previousPixelX);
                            double x2y2_r = ((format.image.getRGB(previousPixelX, previousPixelY + 1) & 0x00ff0000) >> 16) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY + 1) & 0x00ff0000) >> 16) * (x - previousPixelX);
                            double x2y2_g = ((format.image.getRGB(previousPixelX, previousPixelY + 1) & 0x0000ff00) >> 8) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY + 1) & 0x0000ff00) >> 8) * (x - previousPixelX);
                            double x2y2_b = ((format.image.getRGB(previousPixelX, previousPixelY + 1) & 0x000000ff)) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY + 1) & 0x000000ff)) * (x - previousPixelX);

                        int xy_r = new Double((1 - y + previousPixelY) * x1y1_r + x2y2_r * (y - previousPixelY)).intValue();
                        int xy_g = new Double((1 - y + previousPixelY) * x1y1_g + x2y2_g * (y - previousPixelY)).intValue();
                        int xy_b = new Double((1 - y + previousPixelY) * x1y1_b + x2y2_b * (y - previousPixelY)).intValue();
                        processed.setRGB(i, j, 0xff000000 | xy_r << 16 | xy_g << 8 | xy_b);
                        }
                    }
                operation_processed.setImage(SwingFXUtils.toFXImage(processed,null));
            }
        });
    }
    public  void reduce()//直接捨去方法
    {
        reduce.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double ratio = reduce.getValue();
                reduce_text.setText("Reduce Ratio: "+reduce.getValue());
                int height=new Long(Math.round(ratio*(format.Xmax-format.Xmin+1))).intValue();
                int width= new Long(Math.round(ratio*(format.Ymax-format.Ymin+1))).intValue();
                BufferedImage processed= new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);//處理過的影像待生成
                operation_processed.setFitWidth(width);
                operation_processed.setFitHeight(height);
                for (int i=0;i<height;i++)
                    for(int j=0; j<width;j++)
                        processed.setRGB(i,j,format.image.getRGB(new Double(i/ratio).intValue(),new Double(j/ratio).intValue()));

                operation_processed.setImage(SwingFXUtils.toFXImage(processed,null));
            }
        });
    }
    public  void reduce2()//平均法
    {
        reduce2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                double ratio = reduce2.getValue();
                reduce2_text.setText("Magnify Ratio: "+reduce2.getValue());
                int height=new Long(Math.round(ratio*(format.Xmax-format.Xmin+1))).intValue();
                int width= new Long(Math.round(ratio*(format.Ymax-format.Ymin+1))).intValue();
                BufferedImage processed= new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);//處理過的影像待生成
                operation_processed.setFitWidth(width);
                operation_processed.setFitHeight(height);
                for (int i=0;i<height;i++)
                    for(int j=0; j<width;j++) {
                        Double x = new Double(i / ratio);//單純計算後的x
                        Double y = new Double(j / ratio);//單純計算後的y
                        int previousPixelX = x.intValue();
                        int previousPixelY = y.intValue();
                        if(previousPixelX ==255||previousPixelY==255)
                        {

                            processed.setRGB(i, j, format.image.getRGB(previousPixelX,previousPixelY));
                        }
                        else{
                            double x1y1_r = ((format.image.getRGB(previousPixelX, previousPixelY) & 0x00ff0000) >> 16) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY) & 0x00ff0000) >> 16) * (x - previousPixelX);
                            double x1y1_g = ((format.image.getRGB(previousPixelX, previousPixelY) & 0x0000ff00) >> 8) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY) & 0x0000ff00) >> 8) * (x - previousPixelX);
                            double x1y1_b = ((format.image.getRGB(previousPixelX, previousPixelY) & 0x000000ff)) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY) & 0x000000ff)) * (x - previousPixelX);
                            double x2y2_r = ((format.image.getRGB(previousPixelX, previousPixelY + 1) & 0x00ff0000) >> 16) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY + 1) & 0x00ff0000) >> 16) * (x - previousPixelX);
                            double x2y2_g = ((format.image.getRGB(previousPixelX, previousPixelY + 1) & 0x0000ff00) >> 8) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY + 1) & 0x0000ff00) >> 8) * (x - previousPixelX);
                            double x2y2_b = ((format.image.getRGB(previousPixelX, previousPixelY + 1) & 0x000000ff)) * (1 - x + previousPixelX) + ((format.image.getRGB(previousPixelX + 1, previousPixelY + 1) & 0x000000ff)) * (x - previousPixelX);

                            int xy_r = new Double((1 - y + previousPixelY) * x1y1_r + x2y2_r * (y - previousPixelY)).intValue();
                            int xy_g = new Double((1 - y + previousPixelY) * x1y1_g + x2y2_g * (y - previousPixelY)).intValue();
                            int xy_b = new Double((1 - y + previousPixelY) * x1y1_b + x2y2_b * (y - previousPixelY)).intValue();
                            processed.setRGB(i, j, 0xff000000 | xy_r << 16 | xy_g << 8 | xy_b);
                        }
                    }
                operation_processed.setImage(SwingFXUtils.toFXImage(processed,null));
            }
        });
    }
    public  void setImageOperation(){//啟用影像處理的tab
        Image_operation.setDisable(false);
        operation_original.setFitHeight(height);
        operation_original.setFitWidth(width);
        operation_original.setImage(SwingFXUtils.toFXImage(format.image,null));
        operation_processed.setFitHeight(format.Ymax-format.Ymin+1);
        operation_processed.setFitWidth(format.Xmax-format.Xmin+1);
        operation_processed.setImage(SwingFXUtils.toFXImage(format.image,null));
        magnify();//啟用放大Listener
        magnify2();
        reduce();
        reduce2();
        directRotate();
        inverseRotation();
    }
    public  void mainDiagonalReflection(){//主對角線鏡射action
        operation_processed.setFitHeight(format.Ymax-format.Ymin+1);
        operation_processed.setFitWidth(format.Xmax-format.Xmin+1);
        BufferedImage temp = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        for (int i=0;i<format.Ymax-format.Ymin+1;i++)
            for(int j =0; j<format.Xmax-format.Xmin+1;j++)
            {
                temp.setRGB(i,j,format.image.getRGB(j,i));
            }
        operation_processed.setImage(SwingFXUtils.toFXImage(temp,null));
    }
    public  void counterDiagonalReflection(){
        operation_processed.setFitHeight(format.Ymax-format.Ymin+1);
        operation_processed.setFitWidth(format.Xmax-format.Xmin+1);
        BufferedImage temp = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        for (int i=0;i<format.Ymax-format.Ymin+1;i++)
            for(int j =0; j<format.Xmax-format.Xmin+1;j++)
            {
                    temp.setRGB(i,j,format.image.getRGB(255-j,255-i));
            }
            operation_processed.setImage(SwingFXUtils.toFXImage(temp,null));
    }
    public  void horizontalReflection(){
        operation_processed.setFitHeight(height);
        operation_processed.setFitWidth(width);
        BufferedImage temp = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        for (int i=0;i<height;i++)
            for(int j =0; j<width;j++)
            {
                temp.setRGB(i,j,format.image.getRGB(i,255-j));
            }
        operation_processed.setImage(SwingFXUtils.toFXImage(temp,null));
    }
    public  void verticalDiagonalReflection(){
        operation_processed.setFitHeight(format.Ymax-format.Ymin+1);
        operation_processed.setFitWidth(format.Xmax-format.Xmin+1);
        BufferedImage temp = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        for (int i=0;i<format.Ymax-format.Ymin+1;i++)
            for(int j =0; j<format.Xmax-format.Xmin+1;j++)
            {
                temp.setRGB(i,j,format.image.getRGB(255-i,j));
            }
        operation_processed.setImage(SwingFXUtils.toFXImage(temp,null));
    }
    @FXML ImageView opacity_original;
    public  void setOpacity()
    {
        opacity_tab.setDisable(false);
        opacity_original.setImage(SwingFXUtils.toFXImage(format.image,null));
    }
    @FXML Slider opacity_slider;
    @FXML Slider opacity_slider1;
    @FXML ImageView opacity_combine;
    public  void opacity_file() throws  Exception
    {

        FileChooser fileChooser;
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open opacity File");
        File opacityFile = fileChooser.showOpenDialog(null);
        if (opacityFile == null || !(opacityFile.getName().matches("..*\\.pcx"))) {
            System.out.println("Open opacity File error");
            return;
        }
        byte []opacity_bytes = Files.readAllBytes(opacityFile.toPath());
        myImage opacity = new myImage(opacity_bytes);
        setOpacity_new(opacity_bytes,opacity);
        opacity_slider.setDisable(false);
        BufferedImage opacity_image = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        opacity_slider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for(int i =0; i< format.Xmax-format.Xmin+1;i++) {
                    for (int s = 0; s < format.Ymax - format.Ymin + 1; s++) {
                        double transparency = opacity_slider.getValue()/100;
                        int opacity_r=new Double(transparency*((format.image.getRGB(i,s)&0x00ff0000)>>16)+(1-transparency)*((opacity.image.getRGB(i,s)&0x00ff0000)>>16)).intValue();
                        int opacity_g=new Double(transparency*((format.image.getRGB(i,s)&0x0000ff00)>>8)+(1-transparency)*((opacity.image.getRGB(i,s)&0x0000ff00)>>8)).intValue();
                        int opacity_b=new Double(transparency*((format.image.getRGB(i,s)&0x000000ff))+(1-transparency)*((opacity.image.getRGB(i,s)&0x000000ff))).intValue();
                        int opacity_rgb= 0xff000000|opacity_r<<16|opacity_g<<8|opacity_b;
                        opacity_image.setRGB(i,s,opacity_rgb);
                    }
                }
                opacity_combine.setImage(SwingFXUtils.toFXImage(opacity_image,null));
            }
        });
        opacity_slider1.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                for(int i =0; i< format.Xmax-format.Xmin+1;i++) {
                    for (int s = 0; s < format.Ymax - format.Ymin + 1; s++) {
                        double transparency = opacity_slider1.getValue()/100;
                        if(i>=128){
                            int opacity_r=new Double(transparency*((format.image.getRGB(i,s)&0x00ff0000)>>16)+(1-transparency)*((opacity.image.getRGB(i,s)&0x00ff0000)>>16)).intValue();
                            int opacity_g=new Double(transparency*((format.image.getRGB(i,s)&0x0000ff00)>>8)+(1-transparency)*((opacity.image.getRGB(i,s)&0x0000ff00)>>8)).intValue();
                            int opacity_b=new Double(transparency*((format.image.getRGB(i,s)&0x000000ff))+(1-transparency)*((opacity.image.getRGB(i,s)&0x000000ff))).intValue();
                            int opacity_rgb= 0xff000000|opacity_r<<16|opacity_g<<8|opacity_b;
                            opacity_image.setRGB(i,s,opacity_rgb);
                        }else{
                        int opacity_r=((format.image.getRGB(i,s)&0x00ff0000)>>16);
                        int opacity_g=((format.image.getRGB(i,s)&0x0000ff00)>>8);
                        int opacity_b=(format.image.getRGB(i,s)&0x000000ff);
                        int opacity_rgb= 0xff000000|opacity_r<<16|opacity_g<<8|opacity_b;
                        opacity_image.setRGB(i,s,opacity_rgb);}
                    }
                }
                opacity_combine.setImage(SwingFXUtils.toFXImage(opacity_image,null));
            }
        });
    }
    @FXML ImageView opacity_new;
    public  void setOpacity_new(byte [] opacity_newBytes,myImage opacity)
    {

        opacity.palette = new int[256][3];//讀取opacity調色盤
        ArrayList<Color> opacity_colors = new ArrayList<>();
        for (int i =0;i<256;i+=1)//設定調色盤byte array
        {
            opacity.palette[i][0]=opacity_newBytes[opacity_newBytes.length-768+i*3]&0xff;
            opacity.palette[i][1]=opacity_newBytes[opacity_newBytes.length-768+i*3+1]&0xff;
            opacity.palette[i][2]=opacity_newBytes[opacity_newBytes.length-768+i*3+2]&0xff;
            // System.out.println(format.palette[i][0]+" "+format.palette[i][1]+" "+format.palette[i][2]);
            //Graphics2D    graphics = palette.createGraphics(); //畫筆
            opacity_colors.add(new Color(opacity.palette[i][0], opacity.palette[i][1], opacity.palette[i][2]));//把rgb塞入 color arrayList
        }

        opacity.image = new BufferedImage(opacity.Xmax-opacity.Xmin+1,opacity.Ymax-opacity.Ymin+1,BufferedImage.TYPE_INT_ARGB);//建立buffered image instance
        int count=0;
        for(int s=128;s<opacity_newBytes.length-769;)//RLE一定會以每256結尾
        {
            byte temp =opacity_newBytes[s];
            if(((temp>>6)&3)==3)//判斷讀到的前兩個bit是否是11
            {
                int length=opacity_newBytes[s]&0x3f;
                for (int i=0;i< length;i++)
                {
                    opacity.image.setRGB((count+i)%256,(count+i)/256,opacity_colors.get(opacity_newBytes[s+1]&(0xFF)).getRGB());
                }
                //System.out.println(bytes[s]&0x3f);
                count += length;
                s+=2;
                continue;
            }
            opacity.image.setRGB((count)%256,(count)/256,opacity_colors.get(opacity_newBytes[s]&(0xFF)).getRGB());
            s+=1;
            count+=1;
        }

        opacity_new.setFitHeight(format.Xmax-format.Xmin+1);
        opacity_new.setFitWidth (format.Ymax-format.Ymin+1);
        opacity_new.setImage(SwingFXUtils.toFXImage( opacity.image, null));
    }
    @FXML ImageView negative;
    public void setRGB_negativeImage()
    {
        Rimage.setFitHeight(format.Xmax-format.Xmin+1);
        Rimage.setFitWidth (format.Ymax-format.Ymin+1);
        Gimage.setFitHeight(format.Xmax-format.Xmin+1);
        Gimage.setFitWidth (format.Ymax-format.Ymin+1);
        Bimage.setFitHeight(format.Xmax-format.Xmin+1);
        Bimage.setFitWidth (format.Ymax-format.Ymin+1);
        negative.setFitHeight(format.Xmax-format.Xmin+1);
        negative.setFitWidth (format.Ymax-format.Ymin+1);
        BufferedImage RImage = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        BufferedImage GImage = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        BufferedImage BImage = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        BufferedImage NegativeImage = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        for(int i =0; i< format.Xmax-format.Xmin+1;i++)
        {
            for(int s=0;s<format.Ymax-format.Ymin+1;s++)
            {
                RImage.setRGB(i,s,(format.image.getRGB(i,s)&0xFFFF0000));//r
                GImage.setRGB(i,s,(format.image.getRGB(i,s)&0xFF00FF00));//g
                BImage.setRGB(i,s,(format.image.getRGB(i,s)&0xFF0000FF));//b
                int negativeR= 255-((format.image.getRGB(i,s)&0x00FF0000)>>16);
                int negativeG= 255-((format.image.getRGB(i,s)&0x0000FF00)>>8);
                int negativeB= 255-(format.image.getRGB(i,s)&0x000000FF);
                NegativeImage.setRGB(i,s,0xff000000|negativeR<<16|negativeG<<8|negativeB);
            }
        }
        Rimage.setImage(SwingFXUtils.toFXImage(RImage, null));
        Gimage.setImage(SwingFXUtils.toFXImage(GImage, null));
        Bimage.setImage(SwingFXUtils.toFXImage(BImage, null));
        negative.setImage(SwingFXUtils.toFXImage(NegativeImage, null));
    }
    public  void setHistogram()
    {
        histogram.getData().clear();
        for(int i =0; i< format.Xmax-format.Xmin+1;i++) {
            for (int s = 0; s < format.Ymax - format.Ymin + 1; s++) {
                format.RGB[format.image.getRGB(i,s)&0x000000ff][2]++;//計算B的個數
                format.RGB[(format.image.getRGB(i,s)>>8)&0x000000ff][1]++;//計算G的個數
                format.RGB[(format.image.getRGB(i,s)>>16)&0x000000ff][0]++;//計算R的個數
            }
        }
        XYChart.Series Rseries = new XYChart.Series<>();Rseries.setName("R");
        XYChart.Series Gseries = new XYChart.Series<>();Gseries.setName("G");
        XYChart.Series Bseries = new XYChart.Series<>();Bseries.setName("B");
        for (int i =0; i< 256;i++) {
            Rseries.getData().add(new XYChart.Data(String.valueOf(i),format.RGB[i][0]));
            Gseries.getData().add(new XYChart.Data(String.valueOf(i),format.RGB[i][1]));
            Bseries.getData().add(new XYChart.Data(String.valueOf(i),format.RGB[i][2]));
        }
      //x.setVisible(false);
       // x.setAutoRanging(true);
        histogram.getData().addAll(Rseries,Gseries,Bseries);
        histogram.setAnimated(false);
        histogram.setVisible(true);
    }
    public  void setImage()//解PCX檔案並將其印在
    {
        int count=0;
        for(int s=128;s<bytes.length-769;)//RLE一定會以每256結尾
        {
            byte temp =bytes[s];
            if(((temp>>6)&3)==3)//判斷讀到的前兩個bit是否是11
            {
                int length=bytes[s]&0x3f;

                for (int i=0;i< length;i++)
                {
                    format.image.setRGB((count+i)%256,(count+i)/256,colors.get(bytes[s+1]&(0xFF)).getRGB());
                }
                //System.out.println(bytes[s]&0x3f);
                count += length;
                s+=2;
                continue;
            }
            format.image.setRGB((count)%256,(count)/256,colors.get(bytes[s]&(0xFF)).getRGB());
            s+=1;
            count+=1;
        }
        image.setFitHeight(format.Xmax-format.Xmin+1);
        image.setFitWidth (format.Ymax-format.Ymin+1);
        image.setImage(SwingFXUtils.toFXImage(format.image, null));
        tab.setVisible(true);
    }
    public void setPalette()
    {
        format.RGB  = new int[256][3];
        for(int i =0; i< 256;i++) {
            for (int s = 0; s < 3; s++) {
                format.RGB[i][s]=0;
            }
        }
        format.palette = new int[256][3];
        format.image = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);
        palette = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        colors = new ArrayList<>();
        for (int i =0;i<256;i+=1)//設定調色盤byte array
        {
            format.palette[i][0]=bytes[bytes.length-768+i*3]&0xff;
            format.palette[i][1]=bytes[bytes.length-768+i*3+1]&0xff;
            format.palette[i][2]=bytes[bytes.length-768+i*3+2]&0xff;
            // System.out.println(format.palette[i][0]+" "+format.palette[i][1]+" "+format.palette[i][2]);
            Graphics2D    graphics = palette.createGraphics(); //畫筆
            colors.add(new Color(format.palette[i][0], format.palette[i][1], format.palette[i][2]));
            graphics.setPaint(colors.get(i));//設定顏色
            graphics.fillRect((i%16)*16,(i/16)*16,16,16);//劃出調色盤
        }

        paletteView.setImage(SwingFXUtils.toFXImage(palette, null));
        scroll.setVisible(true);
    }
    public void setTable(myImage format){
        table.getItems().clear();
        table.setVisible(true);
        property.setCellValueFactory(new PropertyValueFactory<MyDataType,String>("property"));
        value.setCellValueFactory(new PropertyValueFactory<MyDataType,Integer>("value"));
        table.getItems().add(new MyDataType("Manufacturer",format.manufacturer));
        table.getItems().add(new MyDataType("Version",format.PCX_version_number));
        table.getItems().add(new MyDataType("Encoding",format.Encoding));
        table.getItems().add(new MyDataType("BitsPerPixel",format.BitsPerPixel));
        table.getItems().add(new MyDataType("Xmin",format.Xmin));
        table.getItems().add(new MyDataType("Xmax",format.Xmax));
        table.getItems().add(new MyDataType("Ymin",format.Ymin));
        table.getItems().add(new MyDataType("Ymax",format.Ymax));
        table.getItems().add(new MyDataType("Hdpi",format.Hdpi));
        table.getItems().add(new MyDataType("Vdpi",format.Vdpi));
        table.getItems().add(new MyDataType("Reserved",format.Reserved));
        table.getItems().add(new MyDataType("NPlanes",format.NPlanes));
        table.getItems().add(new MyDataType("BytesPerPlane",format.BytesPerLine));
        table.getItems().add(new MyDataType("PaletteInfo",format.PaletteInfo));
        table.getItems().add(new MyDataType("HScreenSize",format.HscreenSize));
        table.getItems().add(new MyDataType("VScreenSize",format.VscreenSize));;
    }

    @FXML ImageView filter_original;
    @FXML ImageView filter_processed;
    BufferedImage filter;
    public void setFilterTab()
    {
        gray = new BufferedImage(format.Xmax-format.Xmin+1,format.Ymax-format.Ymin+1,BufferedImage.TYPE_INT_ARGB);//在imageview中顯示灰階
        for(int i =0; i< width;i++) {
            for (int s = 0; s < height; s++) {
                int gray_value =new Double((format.image.getRGB(i,s)&0x000000ff)*0.299+((format.image.getRGB(i,s)>>8)&0x000000ff)*0.587+((format.image.getRGB(i,s)>>16)&0x000000ff)*0.114).intValue();
                gray.setRGB(i,s,0xff000000|gray_value<<16|gray_value<<8|gray_value);
            }
        }
        filter_original.setImage(SwingFXUtils.toFXImage(gray,null));
        filter = SwingFXUtils.fromFXImage(filter_original.getImage(),null);
        filter_processed.setImage(SwingFXUtils.toFXImage(filter,null));
        setSNR();
    }
    public  void setMakeNoise()//給gray image撒入雜訊
    {  Random  random = new Random();
        for(int i=0;i<2000;i++)//產生10000noise
        {

            int random_noise =random.nextInt(255);//產生一個灰階訊號 任意數值0-255
            //System.out.println(random_noise);
            filter.setRGB(random.nextInt(255),random.nextInt(255),0xff000000|random_noise<<16|random_noise<<8|random_noise);
            filter_processed.setImage(SwingFXUtils.toFXImage(filter,null));
        }
        setSNR();
    }
    @FXML Label SNR;
    public void setSNR()
    {
        double sigma=0;
        double square =0;
        BufferedImage  gray_original = SwingFXUtils.fromFXImage(filter_original.getImage(),null);
        for(int y=0;y<height;y++) {
            for (int x = 0; x < width; x++) {
                    int original_value = gray_original.getRGB(x,y)&0x000000ff;//取得原圖灰階值
                    int noise_value=filter.getRGB(x,y)&0x000000ff;//取得有雜訊的灰階值
                    sigma+=(original_value-noise_value)*(original_value-noise_value);
                    square+=original_value*original_value;
            }
        }
        NumberFormat formatter = new DecimalFormat("#0.00");
        Double SNR = 10*Math.log10(square/sigma);
        this.SNR.textProperty().set("SNR: "+formatter.format(SNR.floatValue()));
    }
    @FXML Button filter_lowPass_button;
    public  void lowPassFilter()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width+1; x++) {
                neighbor[0]=temp.getRGB(x-1,y-1)&0x000000ff;
                neighbor[1]=temp.getRGB(x,y-1)&0x000000ff;
                neighbor[2]=temp.getRGB(x+1,y-1)&0x000000ff;
                neighbor[3]=temp.getRGB(x-1,y)&0x000000ff;
                neighbor[4]=temp.getRGB(x,y)&0x000000ff;
                neighbor[5]=temp.getRGB(x+1,y)&0x000000ff;
                neighbor[6]=temp.getRGB(x-1,y+1)&0x000000ff;
                neighbor[7]=temp.getRGB(x,y+1)&0x000000ff;
                neighbor[8]=temp.getRGB(x+1,y+1)&0x000000ff;
                double average=0;
                for(int i=0;i<9;i++)
                    average+= neighbor[i]*1.0/9.0;
                gray_filtered.setRGB(x-1,y-1,0xff000000|(new Long(Math.round(average)).intValue())<<16|(new Long(Math.round(average)).intValue())<<8|(new Long(Math.round(average)).intValue()));
            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();
    }
    @FXML Button filter_medianFilter_button;
    public  void medianFilter()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width + 1; x++) {
                neighbor[0] = temp.getRGB(x - 1, y - 1) & 0x000000ff;
                neighbor[1] = temp.getRGB(x, y - 1) & 0x000000ff;
                neighbor[2] = temp.getRGB(x + 1, y - 1) & 0x000000ff;
                neighbor[3] = temp.getRGB(x - 1, y) & 0x000000ff;
                neighbor[4] = temp.getRGB(x, y) & 0x000000ff;
                neighbor[5] = temp.getRGB(x + 1, y) & 0x000000ff;
                neighbor[6] = temp.getRGB(x - 1, y + 1) & 0x000000ff;
                neighbor[7] = temp.getRGB(x, y + 1) & 0x000000ff;
                neighbor[8] = temp.getRGB(x + 1, y + 1) & 0x000000ff;
                /*進行排序取median*/
                for(int i=0;i<9;i++){
                    for(int j=0;j<8;j++)
                    {
                        if(neighbor[j]>neighbor[j+1]){
                            int tmp =neighbor[j+1];
                            neighbor[j+1]=neighbor[j];
                            neighbor[j]=tmp;
                        }
                    }
                }
                /*排序完成 設定median值*/
                gray_filtered.setRGB(x-1,y-1,0xff000000|neighbor[4]<<16|neighbor[4]<<8|neighbor[4]);
            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();

    }
    public  void pseudoMedian()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width + 1; x++) {
                if(x-2<0)
                    neighbor[0] = temp.getRGB(x -1, y) & 0x000000ff;
                else
                neighbor[0] = temp.getRGB(x -2, y) & 0x000000ff;
                neighbor[1] = temp.getRGB(x-1, y ) & 0x000000ff;
                neighbor[2] = temp.getRGB(x , y ) & 0x000000ff;
                neighbor[3] = temp.getRGB(x +1, y) & 0x000000ff;
                if(x+2>height+1)
                    neighbor[4] = temp.getRGB(x+1, y) & 0x000000ff;
                else
                    neighbor[4] = temp.getRGB(x+2, y) & 0x000000ff;
                if(y-2<0)
                neighbor[5] = temp.getRGB(x , y-1) & 0x000000ff;
                else
                    neighbor[5] = temp.getRGB(x , y-2) & 0x000000ff;
                neighbor[6] = temp.getRGB(x , y -1) & 0x000000ff;
                neighbor[7] = temp.getRGB(x, y +1 ) & 0x000000ff;
                if(y+2>height+1)
                    neighbor[8] = temp.getRGB(x , y + 1) & 0x000000ff;
                else
                    neighbor[8] = temp.getRGB(x , y + 2) & 0x000000ff;
                /*進行排序取 median*/
                double pseudoMedian= 0.5*Math.min(minmaxof5(neighbor[0],neighbor[1],neighbor[2],neighbor[3],neighbor[4]),minmaxof5(neighbor[5],neighbor[6],neighbor[7],neighbor[8],neighbor[2]));
                pseudoMedian+=0.5*Math.max(maxminof5(neighbor[0],neighbor[1],neighbor[2],neighbor[3],neighbor[4]),maxminof5(neighbor[5],neighbor[6],neighbor[7],neighbor[8],neighbor[2]));
                /*排序完成 設定median值*/
                int rgb=(int)Math.round(pseudoMedian);
                gray_filtered.setRGB(x-1,y-1,0xff000000|rgb<<16|rgb<<8|rgb);
            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();

    }
    public int minmaxof5(int a, int b,int c,int d,int e)
    {
        int []temp=new int[10];
        temp[0]=maxof3(a,b,c);
        temp[1]=maxof3(a,b,d);
        temp[2]=maxof3(a,b,e);
        temp[3]=maxof3(a,c,d);
        temp[4]=maxof3(a,c,e);
        temp[5]=maxof3(a,d,e);
        temp[6]=maxof3(b,c,d);
        temp[7]=maxof3(b,c,e);
        temp[8]=maxof3(b,d,e);
        temp[9]=maxof3(c,d,e);
        int min=999;
        for(int i=0;i<10;i++){
            if(temp[i]<min)
                min=temp[i];
        }
        return min;
    }
    public int maxof3(int a, int b,int c)
    {
        if(a>b){
            if(a>c)
                return a;
            else return c;
        }
        else
            if(b>c)
                return b;
            else
                return c;
    }
    public int minof3(int a, int b,int c)
    {
        if(a<b){
            if(a<c)
                return a;
            else return c;
        }
        else
        if(b<c)
            return b;
        else
            return c;
    }
    public int maxminof5(int a, int b,int c,int d,int e){
        int []temp=new int[10];
        temp[0]=minof3(a,b,c);
        temp[1]=minof3(a,b,d);
        temp[2]=minof3(a,b,e);
        temp[3]=minof3(a,c,d);
        temp[4]=minof3(a,c,e);
        temp[5]=minof3(a,d,e);
        temp[6]=minof3(b,c,d);
        temp[7]=minof3(b,c,e);
        temp[8]=minof3(b,d,e);
        temp[9]=minof3(c,d,e);
        int max=0;
        for(int i=0;i<10;i++){
            if(temp[i]>max)
                max=temp[i];
        }
        return max;
    }
    public  void HighpassFilter()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width+1; x++) {
                neighbor[0]=temp.getRGB(x-1,y-1)&0x000000ff;
                neighbor[1]=temp.getRGB(x,y-1)&0x000000ff;
                neighbor[2]=temp.getRGB(x+1,y-1)&0x000000ff;
                neighbor[3]=temp.getRGB(x-1,y)&0x000000ff;
                neighbor[4]=temp.getRGB(x,y)&0x000000ff;
                neighbor[5]=temp.getRGB(x+1,y)&0x000000ff;
                neighbor[6]=temp.getRGB(x-1,y+1)&0x000000ff;
                neighbor[7]=temp.getRGB(x,y+1)&0x000000ff;
                neighbor[8]=temp.getRGB(x+1,y+1)&0x000000ff;
                double average=0;
                for(int i=0;i<9;i++){
                    if(i!=4)
                    average+= neighbor[i]*(-1.0/9.0);}
                average+= neighbor[4]*8.0/9.0;

                int newRGB=(int)Math.round(average);
                if(newRGB>255)
                    newRGB=255;
                else if(  newRGB <0)
                    newRGB=0;
                //  System.out.println(newRGB);
                gray_filtered.setRGB(x-1,y-1,0xff000000|newRGB<<16|newRGB<<8|newRGB);

            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();
    }
    public void EdgeCrispening()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width+1; x++) {
                neighbor[0]=temp.getRGB(x-1,y-1)&0x000000ff;
                neighbor[1]=temp.getRGB(x,y-1)&0x000000ff;
                neighbor[2]=temp.getRGB(x+1,y-1)&0x000000ff;
                neighbor[3]=temp.getRGB(x-1,y)&0x000000ff;
                neighbor[4]=temp.getRGB(x,y)&0x000000ff;
                neighbor[5]=temp.getRGB(x+1,y)&0x000000ff;
                neighbor[6]=temp.getRGB(x-1,y+1)&0x000000ff;
                neighbor[7]=temp.getRGB(x,y+1)&0x000000ff;
                neighbor[8]=temp.getRGB(x+1,y+1)&0x000000ff;
                double average=0;
                for(int i=0;i<9;i++)
                    if (i == 1||i==3||i==5||i==7)
                        average += neighbor[i] * (-1);
                average+= neighbor[4]*5;

                int newRGB=(int)Math.round(average);
                if(newRGB>255)
                    newRGB=255;
                else if(  newRGB <0)
                    newRGB=0;
                //  System.out.println(newRGB);
                gray_filtered.setRGB(x-1,y-1,0xff000000|newRGB<<16|newRGB<<8|newRGB);
            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();
    }
    public void HighboostFilter()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width+1; x++) {
                neighbor[0]=temp.getRGB(x-1,y-1)&0x000000ff;
                neighbor[1]=temp.getRGB(x,y-1)&0x000000ff;
                neighbor[2]=temp.getRGB(x+1,y-1)&0x000000ff;
                neighbor[3]=temp.getRGB(x-1,y)&0x000000ff;
                neighbor[4]=temp.getRGB(x,y)&0x000000ff;
                neighbor[5]=temp.getRGB(x+1,y)&0x000000ff;
                neighbor[6]=temp.getRGB(x-1,y+1)&0x000000ff;
                neighbor[7]=temp.getRGB(x,y+1)&0x000000ff;
                neighbor[8]=temp.getRGB(x+1,y+1)&0x000000ff;
                double average=0;
                for(int i=0;i<9;i++){
                    if(i!=4)
                        average+= neighbor[i]*((-1.0)/9.0);}
                average+= neighbor[4]*((1.2-1)+8.0/9.0);

                int newRGB=(int)Math.round(average);
                if(newRGB>255)
                    newRGB=255;
                else if(  newRGB <0)
                    newRGB=0;
                //  System.out.println(newRGB);
                gray_filtered.setRGB(x-1,y-1,0xff000000|newRGB<<16|newRGB<<8|newRGB);

            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();
    }
    public void Sobel()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width+1; x++) {
                neighbor[0]=temp.getRGB(x-1,y-1)&0x000000ff;
                neighbor[1]=temp.getRGB(x,y-1)&0x000000ff;
                neighbor[2]=temp.getRGB(x+1,y-1)&0x000000ff;
                neighbor[3]=temp.getRGB(x-1,y)&0x000000ff;
                neighbor[4]=temp.getRGB(x,y)&0x000000ff;
                neighbor[5]=temp.getRGB(x+1,y)&0x000000ff;
                neighbor[6]=temp.getRGB(x-1,y+1)&0x000000ff;
                neighbor[7]=temp.getRGB(x,y+1)&0x000000ff;
                neighbor[8]=temp.getRGB(x+1,y+1)&0x000000ff;
                double average=0;
                average+=Math.abs((neighbor[6]+2*neighbor[7]+neighbor[8])-(neighbor[0]+2*neighbor[1]+neighbor[2]));
                average+=Math.abs((neighbor[2]+2*neighbor[5]+neighbor[8])-(neighbor[0]+2*neighbor[3]+neighbor[6]));
                int newRGB=(int)Math.round(average);
                if(newRGB>255)
                    newRGB=255;
                else if(  newRGB <0)
                    newRGB=0;
                //  System.out.println(newRGB);
                gray_filtered.setRGB(x-1,y-1,0xff000000|newRGB<<16|newRGB<<8|newRGB);

            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();
    }
    public  void outlier()
    {
        BufferedImage  gray_filtered = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
        BufferedImage temp = new BufferedImage(width+2,height+2,BufferedImage.TYPE_INT_ARGB);//擴充邊際+1以用於
        for(int y=1;y<height+1;y++) {//將原圖放在邊界擴大後的圖中間
            for (int x = 1; x < width+1; x++) {
                temp.setRGB(x,y,filter.getRGB(x-1,y-1));
            }
        }
        for(int x=0;x<width+2;x++) {//填補上下兩邊
            if(x==0){//設定左邊邊角
                temp.setRGB(x,0,filter.getRGB(0,0));
                temp.setRGB(x,height+1,filter.getRGB(0,height-1));
            }
            else if(x==width+1){//設定右邊邊角
                temp.setRGB(x,0,filter.getRGB(width-1,0));
                temp.setRGB(x,height+1,filter.getRGB(width-1,height-1));
            }
            else {//設定邊角外上下兩邊
                temp.setRGB(x,0,filter.getRGB(x-1,0));
                temp.setRGB(x,height+1,filter.getRGB(x-1,height-1));

            }
        }
        for(int y=1;y<height-1;y++){//填補左右兩邊
            temp.setRGB(0,y,filter.getRGB(0,y-1));
            temp.setRGB(width+1,y,filter.getRGB(width-1,y-1));
        }
        /*已設定放大image,進行filter*/
        int []neighbor = new  int[9];
        for(int y=1;y<height+1;y++) {
            for (int x = 1; x < width + 1; x++) {
                //先取得鄰居
                neighbor[0] = temp.getRGB(x - 1, y - 1) & 0x000000ff;
                neighbor[1] = temp.getRGB(x, y - 1) & 0x000000ff;
                neighbor[2] = temp.getRGB(x + 1, y - 1) & 0x000000ff;
                neighbor[3] = temp.getRGB(x - 1, y) & 0x000000ff;
                neighbor[4] = temp.getRGB(x, y) & 0x000000ff;
                neighbor[5] = temp.getRGB(x + 1, y) & 0x000000ff;
                neighbor[6] = temp.getRGB(x - 1, y + 1) & 0x000000ff;
                neighbor[7] = temp.getRGB(x, y + 1) & 0x000000ff;
                neighbor[8] = temp.getRGB(x + 1, y + 1) & 0x000000ff;
                double neighbors_average =0;
                for(int i=0;i<9;i++){
                    if(i!=4)
                    neighbors_average+= neighbor[i]*1.0/8.0;}
               // System.out.println("average:"+neighbors_average+"      center:"+neighbor[4]);
                if(Math.abs(neighbor[4]-neighbors_average)>=35)
                {
                    int newRGB=(int)Math.round(neighbors_average);
                  //  System.out.println(newRGB);
                    gray_filtered.setRGB(x-1,y-1,0xff000000|newRGB<<16|newRGB<<8|newRGB);
                }
                else
                    gray_filtered.setRGB(x-1,y-1,0xff000000|neighbor[4]<<16|neighbor[4]<<8|neighbor[4]);
            }
        }
        filter_processed.setImage(SwingFXUtils.toFXImage(gray_filtered,null));
        filter = SwingFXUtils.fromFXImage(filter_processed.getImage(),null);//將filter後結果 轉回給filter 以便進行SNR計算以及下次filter
        setSNR();
    }
}
