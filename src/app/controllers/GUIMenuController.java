package app.controllers;

import app.Main;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;


public class GUIMenuController implements Initializable{

    /************** menu *****************/
    @FXML public Button btnNew;
    @FXML public Button btnProp;
    @FXML public Button btnExit;

    @FXML public Button newBackMenu;
    @FXML public Button btnCreateProblem;
    @FXML public TextField fldProblemName;
    @FXML public Label lblNewProblem;
    @FXML public Label lblMenuStart;
    /*************************************/


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    /******************* menu *******************/
    @FXML
    public void gotoProp(ActionEvent e) throws IOException {
        changeScene(btnProp, "/fxml/Properties.fxml");
    }

    @FXML
    public void createProblem(ActionEvent e) throws IOException {
        changeScene(btnNew, "/fxml/NewName.fxml");
    }

    @FXML
    public void backToMainMenu(ActionEvent e) throws IOException {
        if(e.getSource() == newBackMenu) {
            changeScene(newBackMenu, "/fxml/Menu.fxml");
        }
    }

    @FXML
    public void newProblem(ActionEvent e) throws IOException {
        if(fldProblemName.getText().equals("")){
            lblNewProblem.setText("You must enter a name of your problem to continue.");
        }
        else {
            changeScene(btnCreateProblem, "/fxml/AHP_Problem_Resolving.fxml");
        }
    }

    @FXML
    public void exit(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getDialogPane().getStylesheets().add(Main.currentCss);
        alert.setHeaderText("");
        alert.setTitle("Exit");
        alert.setContentText("Are you sure you want to exit?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            Stage stage = (Stage) btnExit.getScene().getWindow();
            stage.close();
        } else {
            e.consume();
        }
    }

    /**********************************************/

    public void changeScene(Button btn, String fxmlFile) throws IOException {

        Parent root2 = FXMLLoader.load(Main.class.getResource(fxmlFile));
        if(btn == btnCreateProblem) {
            Stage stage = (Stage) btn.getScene().getWindow();
            stage.setMinWidth(800);
            stage.setMinHeight(650);
            stage.centerOnScreen();
            stage.setTitle(fldProblemName.getText());
        }
        btn.getScene().setRoot(root2);
    }

}



