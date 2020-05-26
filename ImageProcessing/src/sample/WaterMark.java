package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ResourceBundle;

public class WaterMark implements Initializable {
    BufferedImage gray;
    BufferedImage new_gray;
    @FXML
    Label SNR;
    @FXML ImageView watermark;
    @FXML ImageView original;
    @FXML
    ImageView i1;
    @FXML
    ImageView i2;
    @FXML
    ImageView i3;
    @FXML
    ImageView i4;
    @FXML
    ImageView i5;
    @FXML
    ImageView i6;
    @FXML
    ImageView i7;
    @FXML
    ImageView i0;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
    public  void BitSlicing(BufferedImage gray)
    {
        BufferedImage []gray1 = new BufferedImage[8];
        for(int i=0;i<8;i++) {
            gray1[i] = new BufferedImage(gray.getWidth(), gray.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for(int s=0;s<gray.getHeight();s++)
                for(int j=0;j<gray.getWidth();j++)
                {
                    int Bit=(((gray.getRGB(s,j)&0x000000ff)>>i)&0x00000001)*255;
                    gray1[i].setRGB(s,j,0xff000000|Bit<<16|Bit<<8|Bit);
                }
        }
        i0.setImage(SwingFXUtils.toFXImage(gray1[0],null));
        i1.setImage(SwingFXUtils.toFXImage(gray1[1],null));
        i2.setImage(SwingFXUtils.toFXImage(gray1[2],null));
        i3.setImage(SwingFXUtils.toFXImage(gray1[3],null));
        i4.setImage(SwingFXUtils.toFXImage(gray1[4],null));
        i5.setImage(SwingFXUtils.toFXImage(gray1[5],null));
        i6.setImage(SwingFXUtils.toFXImage(gray1[6],null));
        i7.setImage(SwingFXUtils.toFXImage(gray1[7],null));
    }
    public  void setGray(BufferedImage gray)
    {
        this.gray= gray;
        original.setImage(SwingFXUtils.toFXImage(gray,null));
        BitSlicing(gray);

    }
    public  void setSNR()
    {
        double sigma=0;
        double square =0;

        for(int y=0;y<256;y++) {
            for (int x = 0; x <256; x++) {
                int original_value = gray.getRGB(x,y)&0x000000ff;//取得原圖灰階值
                int noise_value=new_gray.getRGB(x,y)&0x000000ff;//取得有雜訊的灰階值
                sigma+=(original_value-noise_value)*(original_value-noise_value);
                square+=original_value*original_value;
            }
        }
        Double SNR = 10*Math.log10(square/sigma);
        NumberFormat formatter = new DecimalFormat("#0.00");
        this.SNR.textProperty().set("SNR: "+formatter.format(SNR));
    }
    public void openFile() throws  Exception
    {
        FileChooser fileChooser;
        fileChooser = new FileChooser();
        fileChooser.setTitle("Open Watermark");
        File watermark = fileChooser.showOpenDialog(null);
        BufferedImage watermark_image = ImageIO.read(watermark);
        new_gray= new BufferedImage(256,256,BufferedImage.TYPE_INT_ARGB);
        for(int y=0;y<256;y++)
        {
              for(int x=0;x<256;x++)
              {
                  int watermark_value=watermark_image.getRGB(x,y)&0x000000ff;//取得water mark座標資訊
                  if(watermark_value >=128)//代表黑色
                   {
                          int new_gray_value=(((gray.getRGB(x,y)&0x000000ff)>>4)<<4)|0x0000000f;
                          new_gray.setRGB(x,y,0xff000000|new_gray_value<<16|new_gray_value<<8|new_gray_value);
                   }
                   else{
                         int new_gray_value=(((gray.getRGB(x,y)&0x000000ff)>>4)<<4)|0x00000000;
                         new_gray.setRGB(x,y,0xff000000|new_gray_value<<16|new_gray_value<<8|new_gray_value);
                  }
              }
        }
        original.setImage(SwingFXUtils.toFXImage(new_gray,null));
        this.watermark.setImage(SwingFXUtils.toFXImage(watermark_image,null));
        BitSlicing(new_gray);
        setSNR();
    }
}
