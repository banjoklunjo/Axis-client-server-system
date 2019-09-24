package ui;

import java.awt.image.BufferedImage;
import java.io.IOException;

import client.Controller;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class PictureView {
    private Stage primaryStage;
    private Controller controller;
    private boolean isWindowClosed = false;

    @FXML
    ImageView imageView;

    public void updateImage(BufferedImage bufferedImage) {
        Image image = SwingFXUtils.toFXImage(bufferedImage, null);
        imageView.setFitHeight(image.getHeight());
        imageView.setFitWidth(image.getWidth());
        primaryStage.setHeight(image.getHeight());
        primaryStage.setWidth(image.getWidth());
        imageView.setImage(image);
    }

    public static PictureView displayPictureView() {
        PictureView pictureView = null;
        Stage primaryStage = new Stage();
        FXMLLoader loader = new FXMLLoader(PictureView.class.getResource("PictureView.fxml"));
        Parent root;
        try {
            root = loader.load();
            pictureView = loader.getController();
            pictureView.setStage(primaryStage);
            primaryStage.setTitle("Camera Picture");
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pictureView;
    }

    public void setStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setOnHiding(new EventHandler<WindowEvent>() {

            public void handle(WindowEvent event) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        isWindowClosed = true;
                        controller.displayPictureView();
                    }
                });
            }
        });
    }

    public boolean isWindowClosed() {
        return isWindowClosed;
    }

    public void setWindowsIsOpen() {
        isWindowClosed = true;
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

}