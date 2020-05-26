package sample;

import javafx.application.Platform;
import  javafx.scene.chart.XYChart;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import javafx.scene.control.Menu;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import sun.dc.pr.PRError;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import static java.awt.SystemColor.info;

public class loadingScreenController implements Initializable {

    @FXML private Pane screen;
    @FXML private ProgressBar progressBar;
    @FXML private TextField loadingPercent;
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        new SplashScreen().start();
    }
    class SplashScreen extends  Thread
    {   Parent root = null;
        Stage stage = null;
        Scene scene =null;
        public void run()
        {
            try{
                    root = FXMLLoader.load(getClass().getResource("sample1.fxml"));
                int i;
                Thread.sleep(200);
                for (i=0;i<=20;i++)
                {
                    progressBar.progressProperty().set(i/20.0);
                    //loadingPercent.textProperty().set(25*i+"%");
                    Thread.sleep(50);
                }

                Thread.sleep(200);
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {

                        scene = new Scene(root);
                        stage = new Stage();
                        stage.setScene(scene);
                        stage.setTitle("ImageProcessing");
                        stage.getIcons().add(new Image("/sample/icon.png"));


                        stage.show();

                        screen.getScene().getWindow().hide();
                    }
                });
            }catch ( Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
