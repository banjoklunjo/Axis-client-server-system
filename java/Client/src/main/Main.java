package main;

import java.util.concurrent.Executors;

import client.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;

public class Main extends Application {
    private Controller controller;


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../ui/ConnectView.fxml"));
            Parent root = loader.load();
            primaryStage.setTitle("Client");
            primaryStage.setResizable(false);
            primaryStage.setScene(new Scene(root));
            primaryStage.show();
            System.out.println("MainMainHejHej");
            controller = new Controller(Executors.newCachedThreadPool(), loader.getController());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        controller.onQuit();
        System.out.println("Stage is closing");
    }

}