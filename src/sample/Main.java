package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/assets/icon.png")));
        primaryStage.setTitle("Manga Downloader");
        primaryStage.setScene(new Scene(root, 736, 541));
        primaryStage.show();

//        primaryStage.setOnCloseRequest(event -> {
//            Platform.exit();
//        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
