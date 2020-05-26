package sample;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ResourceBundle;

public class BitSlicing implements Initializable {
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
    public BufferedImage gray;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void setGray(BufferedImage gray) {

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
}
