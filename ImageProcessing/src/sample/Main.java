package sample;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.scene.control.Menu;
import javafx.stage.StageStyle;

public class Main extends Application {

    public static  Boolean isLoaded = false;
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("loadingScreen.fxml"));
        primaryStage.setTitle("ImageProcessing");
        primaryStage.getIcons().add(new Image("/sample/icon.png"));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();


        //
       // System.out.println(fileChooser.getSelectedExtensionFilter().getDescription());
    }


    public static void main(String[] args) {
        launch(args);
    }
}
