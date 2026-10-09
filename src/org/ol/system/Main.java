/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package org.ol.system;

import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        stage.setTitle("GrandStay | Gestión hotelera");
        stage.setMinWidth(980);
        stage.setMinHeight(640);
        show("LoginView.fxml", 1100, 720);
        stage.show();
    }

    public static void show(String view, double width, double height) throws IOException {
        Parent root = FXMLLoader.load(Main.class.getResource("/org/ol/view/" + view));
        primaryStage.setScene(new Scene(root, width, height));
        primaryStage.centerOnScreen();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
