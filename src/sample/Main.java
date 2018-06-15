package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Start UI
 *
 * @author Quyen Truong
 * @version 1.4
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("view/main.fxml"));
        primaryStage.getIcons().add(new Image(Main.class.getResourceAsStream("/assets/icon.png")));
        primaryStage.setTitle("Manga Downloader");
        primaryStage.setScene(new Scene(root, 736, 558));
        primaryStage.setResizable(false);
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
