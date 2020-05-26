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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.ResourceBundle;

public class HistogramSpecification  implements Initializable {
    BufferedImage gray;
    @FXML
    ImageView original;
    @FXML
    ImageView processed;
    @FXML
    BarChart histogram;
    int [] gray_histogram;
    @FXML
    BarChart histogram_processed;
    @FXML
    LineChart<Number,Number> line;
    XYChart.Series<Number,Number> series;
    @FXML
    private NumberAxis xAxis ;
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
        yAxis.setUpperBound(1);
        yAxis.setTickUnit(0.05);
        series= new XYChart.Series<Number, Number>();//設定CDF
        series.getData().add(new XYChart.Data(255,1));//cdf 畫線
        series.getData().add(new XYChart.Data(0,0));//cdf 畫線
        series.setName("Linear Function");
    }
    public void set(BufferedImage image)
    {
        this.gray=image;
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
        BufferedImage new_processed = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        double []pdf=new double[256];//用來存取機率，先存pdf
        double []cdf=new double[256];//再算cdf放進去
        int total_pixels=256*256;
        for (int y=0;y<256;y++)
            for(int x=0;x<256;x++)
                pdf[gray.getRGB(x,y)&0x000000ff]+=1.0/total_pixels;//計算pdf

        for(int i=0;i<256;i++)
            for(int s=0;s<=i;s++)
                cdf[i]=cdf[i]+pdf[s];//計算cdf
        for (int y=0;y<256;y++) {
            for (int x = 0; x < 256; x++) {
                int new_gray_value = (int)(cdf[gray.getRGB(x, y)&0x000000ff] * 255);//新灰階值為cdf*255;
                new_processed.setRGB(x, y, 0xff000000 | new_gray_value << 16 | new_gray_value << 8 | new_gray_value);
            }
        }
        processed.setImage(SwingFXUtils.toFXImage(new_processed,null));


        series= new XYChart.Series<Number, Number>();//設定PiecewiseLinear Function
        line.getData().clear();
        series.setName("CDF");

        series.getData().add(new XYChart.Data(255,1));
        series.getData().add(new XYChart.Data(0,0));
        line.getData().add(series);
        setHistogram(new_processed,histogram_processed);
    }

    public  void setHistogram(BufferedImage image, BarChart histogram)
    {
        histogram.setAnimated(false);
        gray_histogram= new int[256];
        histogram.getData().clear();
        for(int i =0; i< 256;i++) {
            for (int s = 0; s < 256; s++) {
                gray_histogram[image.getRGB(i,s)&0x000000ff]++;//計算各灰階的個數
            }
        }
        XYChart.Series graySeries = new XYChart.Series<>();graySeries.setName("Gray");
        for (int i =0; i< 256;i++)
            graySeries.getData().add(new XYChart.Data(String.valueOf(i),gray_histogram[i]));
        histogram.getData().addAll(graySeries);
        //otsu_histogram.getXAxis().setOpacity(0);
        histogram.setVisible(true);
        addListener();
    }
    public void  specification()
    {
        int node_number=series.getData().size();
        series.getData().sort(new Comparator<XYChart.Data<Number, Number>>() {
            @Override
            public int compare(XYChart.Data<Number, Number> o1, XYChart.Data<Number, Number> o2) {
                if(o1.getXValue().floatValue()<o2.getXValue().floatValue())
                    return  -1;
                else  if(o1.getXValue().floatValue()>o2.getXValue().floatValue())
                    return 1;
                return 0;
            }
        });
        double[] x_points = new double[ node_number];//存放圖上 點X座標
        double[] y_points = new double[ node_number];//存放圖上 點Y座標
        for( int i=0;i<node_number;i++)
        {
            x_points[i]=series.getData().get(i).getXValue().doubleValue();//取得input下界
            y_points[i]=series.getData().get(i).getYValue().doubleValue();//取得input下界

            System.out.println( x_points[i]+" "+y_points[i]);
        }
        BufferedImage specified = new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        double cdf[] = new  double[256];//存放reference cdf中每個灰度的cdf
        for(int i=0;i<256;i++)//存放reference cdf中每個灰度的cdf
        {
            int stage_of_level =0;
            for( int s =0;s<node_number-1;s++)
                if(i>=x_points[s]&&i<=x_points[s+1])
                    stage_of_level=s;//取得各灰度在CDF第幾個分段
            double m = (y_points[stage_of_level+1]*1.0-y_points[stage_of_level])/(x_points[stage_of_level+1]*1.0-x_points[stage_of_level]);//求出目前點 屬於哪個stage區間的斜率
            double b= y_points[stage_of_level];//取得y=mx+b中的b
            cdf[i]=(i-x_points[stage_of_level])*m+b;
            //System.out.println("x:"+i + "m:"+m+"    b:"+b+"   "+cdf[i]);
        }

        int []gray_histogram= new int[256];
        for(int y=0;y<256;y++) {//存放原始影像個個灰度的個數
            for (int x = 0; x < 256; x++) {
                gray_histogram[gray.getRGB(x,y)&0x000000ff]++;//計算各灰階的個數
            }
        }
        double []original_cdf = new double[256];//原本的cdf
        for( int i=0;i<256;i++)
        {   if(i==0)
                original_cdf[i]=gray_histogram[i]*1.0/(256*256);
            else
                original_cdf[i]=original_cdf[i-1]+gray_histogram[i]*1.0/(256*256);
           // System.out.println(original_cdf[i]);
        }
        int []match = new int[256];
        int current_match=0;
        for( int i=0;i<256;i++)//跟據specified的cdf 以及 原圖cdf進行matching
        {
           for(int s= current_match;s<256;s++){
               if(original_cdf[s]<cdf[i]){
                   match[s]=i;
                   current_match=s;
                   System.out.println("original:"+original_cdf[s]+"    after:"+match[s]);
               }
               else
                   break;
           }

        }

        for(int y=0;y<256;y++){
            for(int x=0;x<256;x++) {
                int Pin = gray.getRGB(x,y)&0x000000ff;//取得原本值Pin
                int Pout = match[Pin];//取得其對應匹對的灰度
                specified.setRGB(x,y,0xff000000|Pout<<16|Pout<<8|Pout);
            }
        }
        processed.setImage(SwingFXUtils.toFXImage(specified,null));
        setHistogram(specified,histogram_processed);
    }
    @FXML
    Label node;
    public  void addListener() {
        line.setOnMouseClicked((MouseEvent event) -> {

            Point2D mouseSceneCoords = new Point2D(event.getSceneX(), event.getSceneY());

            double x = xAxis.sceneToLocal(mouseSceneCoords).getX();
            double y = yAxis.sceneToLocal(mouseSceneCoords).getY();
            NumberFormat formatter = new DecimalFormat("#0.00");
            node.setText("x:" + xAxis.getValueForDisplay(x).intValue() + " y:" + formatter.format(yAxis.getValueForDisplay(y)));//取得座標點
            XYChart.Series<Number, Number> new_series = new XYChart.Series<Number, Number>();
            series.getData().add(new XYChart.Data(xAxis.getValueForDisplay(x).intValue(), yAxis.getValueForDisplay(y)));
            new_series.getData().add(new XYChart.Data(xAxis.getValueForDisplay(x).intValue(), yAxis.getValueForDisplay(y)));
            for (XYChart.Data data : series.getData()) {
                new_series.getData().add(new XYChart.Data(data.getXValue(), data.getYValue()));//將舊有的點加入新的series
            }
            line.getData().clear();
            line.getData().add(new_series);
        });
    }
}
