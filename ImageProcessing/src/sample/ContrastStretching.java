package sample;

import javafx.embed.swing.SwingFXUtils;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;



import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

public class ContrastStretching implements Initializable {
    BufferedImage gray;
    @FXML
    ImageView original;
    @FXML
    ImageView processed;
    @FXML
    BarChart histogram;
    @FXML
    BarChart histogram_processed;
    @FXML
    LineChart<Number,Number> line;
    int [] gray_histogram;
    @FXML
    private NumberAxis xAxis ;
    XYChart.Series<Number,Number> series;
    @FXML
    private NumberAxis yAxis ;
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        xAxis.setAutoRanging(false);
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(255);
        xAxis.setTickUnit(3);

        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(255);
        yAxis.setTickUnit(3);
        series= new XYChart.Series<Number, Number>();//設定PiecewiseLinear Function
        series.getData().add(new XYChart.Data(255,255));//linearfunction 初始點
        series.getData().add(new XYChart.Data(0,0));//linearfunction 初始點
        series.setName("Linear Function");
    }
    public  void setOriginal(BufferedImage gray)
    {
        this.gray=gray;
        original.setImage(SwingFXUtils.toFXImage(this.gray,null));
        histogram.setAnimated(false);
        gray_histogram= new int[256];
        histogram.getData().clear();
        for(int i =0; i< 256;i++) {
            for (int s = 0; s < 256; s++) {
                gray_histogram[this.gray.getRGB(i,s)&0x000000ff]++;//計算各灰階的個數
            }
        }
        XYChart.Series graySeries = new XYChart.Series<>();graySeries.setName("Gray");
        for (int i =0; i< 256;i++)
            graySeries.getData().add(new XYChart.Data(String.valueOf(i),gray_histogram[i]));
        histogram.getData().addAll(graySeries);
        //otsu_histogram.getXAxis().setOpacity(0);
        histogram.setVisible(true);

    }
    public void DefaultContrastStretching()
    {
        int c=-1,d=0;
        int percent=0;//用放目前數到第幾個pixel
        for(int i=0;i<256;i++)
        {
            percent+=gray_histogram[i];
            if(percent>=0.95*256*256){//從大到小如果目前累積達到95%
                d=i;
                break;
            }
            else if(percent>=0.05*256*256&&c==-1)
            {
                c=i;}
        }
        BufferedImage Default = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<256;y++)
            for(int x=0;x<256;x++)
            {
                int Pout= new Long(Math.round(((gray.getRGB(x,y)&0x000000ff)-c)*(255.0/((d-c)*1.0)))).intValue();//跟局原本灰度設定計算新的灰度 (取整)
                if(Pout<0)
                    Pout=0;
                else  if(Pout >255)
                    Pout=255;
                Default.setRGB(x,y,0xff000000|Pout<<16|Pout<<8|Pout);
            }
         processed.setImage(SwingFXUtils.toFXImage(Default,null));

        histogram_processed.setAnimated(false);
        int []gray_histogram= new int[256];//這裡的histogram是contrast Stretching後的histogram
        histogram_processed.getData().clear();
        for(int i =0; i< 256;i++) {
            for (int s = 0; s < 256; s++) {
                gray_histogram[Default.getRGB(i,s)&0x000000ff]++;//計算各灰階的個數
            }
        }
        XYChart.Series graySeries = new XYChart.Series<>();graySeries.setName("Gray");
        for (int i =0; i< 256;i++){
            graySeries.getData().add(new XYChart.Data(String.valueOf(i),gray_histogram[i]));
        }
        histogram_processed.getData().addAll(graySeries);
        //otsu_histogram.getXAxis().setOpacity(0);
        histogram_processed.setVisible(true);

        series= new XYChart.Series<Number, Number>();//設定PiecewiseLinear Function
        line.getData().clear();
        series.setName("Linear Function");
       // line.setCreateSymbols(false);
        series.getData().add(new XYChart.Data(255,255));
        series.getData().add(new XYChart.Data(0,0));
        line.getData().add(series);
        addListener();
    }
    @FXML
    Label node;
    public  void addListener()
    {
        line.setOnMouseClicked((MouseEvent event) ->{

            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());

            double x = xAxis.sceneToLocal(mouseSceneCoords).getX();
            double y = yAxis.sceneToLocal(mouseSceneCoords).getY();
            node.setText("x:"+xAxis.getValueForDisplay(x).intValue()+" y:"+yAxis.getValueForDisplay(y).intValue());//取得座標點
            XYChart.Series<Number,Number> new_series=new XYChart.Series<Number, Number>();
            series.getData().add(new XYChart.Data(xAxis.getValueForDisplay(x).intValue(),yAxis.getValueForDisplay(y).intValue()));
            new_series.getData().add(new XYChart.Data(xAxis.getValueForDisplay(x).intValue(),yAxis.getValueForDisplay(y).intValue()));
            for(XYChart.Data data:series.getData()){
                new_series.getData().add(new XYChart.Data(data.getXValue(),data.getYValue()));//將舊有的點加入新的series
            }
            line.getData().clear();
            line.getData().add(new_series);
        });
    }

    public void Piecewise()
    {  int node_number=series.getData().size();
        series.getData().sort(new Comparator<XYChart.Data<Number, Number>>() {
            @Override
            public int compare(XYChart.Data<Number, Number> o1, XYChart.Data<Number, Number> o2) {
                if(o1.getXValue().intValue()<o2.getXValue().intValue())
                    return  -1;
                else  if(o1.getXValue().intValue()>o2.getXValue().intValue())
                    return 1;
                return 0;
            }
        });
       int[] a = new int[ node_number-1];//output lower
       int[] b = new int[ node_number-1];//output upper
       int[] c = new int[ node_number-1];//input lower
       int[] d = new int[ node_number-1];//input upper
        for( int i=0;i<node_number-1;i++)
        {
            a[i]=series.getData().get(i).getYValue().intValue();//取得input下界
            b[i]=series.getData().get(i+1).getYValue().intValue();//取得input下界
            c[i]=series.getData().get(i).getXValue().intValue();//取得input下界
            d[i]=series.getData().get(i+1).getXValue().intValue();//取得input上界
            System.out.println( c[i]+" "+d[i]+"      "+a[i]+" "+b[i]);
        }
        BufferedImage Default = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<256;y++)
            for(int x=0;x<256;x++)
            {
                int Pin = gray.getRGB(x,y)&0x000000ff;//取得原本值Pin
                int stage_of_level =0;
                for( int i =0;i<node_number-2;i++)
                    if(Pin>=c[i]&&Pin<c[i+1])
                        stage_of_level=i;//取得Pin在Linear Function第幾個分段
                int Pout = new Long(Math.round(((Pin -c[stage_of_level])*(b[stage_of_level]-a[stage_of_level])/(1.0*(d[stage_of_level]-c[stage_of_level])))+a[stage_of_level])).intValue();
                if(Pout<a[stage_of_level])//超出output範圍的值 要設定為邊界值
                    Pout=a[stage_of_level];
                else  if(Pout >b[stage_of_level])
                    Pout=b[stage_of_level];
                Default.setRGB(x,y,0xff000000|Pout<<16|Pout<<8|Pout);
            }
        processed.setImage(SwingFXUtils.toFXImage(Default,null));
        int []gray_histogram= new int[256];//這裡的histogram是contrast Stretching後的histogram
        histogram_processed.getData().clear();
        for(int i =0; i< 256;i++) {
            for (int s = 0; s < 256; s++) {
                gray_histogram[Default.getRGB(i,s)&0x000000ff]++;//計算各灰階的個數
            }
        }
        XYChart.Series graySeries = new XYChart.Series<>();graySeries.setName("Gray");
        for (int i =0; i< 256;i++){
            graySeries.getData().add(new XYChart.Data(String.valueOf(i),gray_histogram[i]));
        }
        histogram_processed.getData().addAll(graySeries);
    }
}
