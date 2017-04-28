package app; /**
 * Created on 24.03.2017.
 */

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

public class Main extends Application {

    public static String currentCss = Main.class.getResource("/css/Green.css").toExternalForm();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent pre = FXMLLoader.load(Main.class.getResource("/fxml/Menu.fxml"));
        Scene scene = new Scene(pre);
        scene.getStylesheets().add(Main.currentCss);
        primaryStage.setScene(scene);
        primaryStage.setMinHeight(450);
        primaryStage.setMinWidth(600);
        primaryStage.setOnCloseRequest(we -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getDialogPane().getStylesheets().add(Main.currentCss);
            alert.setHeaderText("");
            alert.setTitle("Exit");
            alert.setContentText("Are you sure you want to exit?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                primaryStage.close();
            } else {
                we.consume();
            }
        });
        primaryStage.show();

    }
}
