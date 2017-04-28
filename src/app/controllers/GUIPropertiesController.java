package app.controllers;

import app.Main;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Separator;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Created on 15.04.2017.
 */
public class GUIPropertiesController implements Initializable {

    /*************** fields ****************/
    private ListProperty<String> listCssObserve = new SimpleListProperty<>();
    private List<String> listCss;

    @FXML public ChoiceBox propChoiceBox;
    @FXML public Button propBackMenu;
    /****************************************/

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setCbCss();
        propChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedCss = (String) propChoiceBox.getSelectionModel().getSelectedItem();
            if(selectedCss.contains("default")){
                selectedCss = selectedCss.substring(0, selectedCss.indexOf(" (default)"));
                System.out.println("Default selected: " + selectedCss);
            }
            String checkedCss = "";
            for(int i = 0; i < listCss.size(); i++){
                if(listCss.get(i).contains(selectedCss)){
                    checkedCss = listCss.get(i);
                    break;
                }
            }
            Scene scene = propBackMenu.getScene();
            scene.getStylesheets().clear();
            scene.getStylesheets().add(checkedCss);
            Main.currentCss = checkedCss;
        });
    }

    /****************************************/

    @FXML
    public void backToMainMenu(ActionEvent e) throws IOException {
        Parent root2 = FXMLLoader.load(Main.class.getResource("/fxml/Menu.fxml"));
        propBackMenu.getScene().setRoot(root2);
    }

    //////////////////////////////////////////

    private void setCbCss() {

        String css0 = Main.class.getResource("/css/Green.css").toExternalForm(); //default
        String css1 = Main.class.getResource("/css/BevelGrey.css").toExternalForm();
        String css7 = Main.class.getResource("/css/Royal.css").toExternalForm();

        String name0 = StringUtils.substringBetween(css0,"/css/",".css");
        String name1 = StringUtils.substringBetween(css1,"/css/",".css");
        String name7 = StringUtils.substringBetween(css7,"/css/",".css");

        listCss = new ArrayList<>(
                Arrays.asList(css0, css1, css7)
        );

        listCssObserve.set(FXCollections.observableArrayList(
                name0 + " (default)",  name1, name7)
        );

        propChoiceBox.setItems(FXCollections.observableArrayList(
                name0 + " (default)",  new Separator(), name1, name7)
        );

        String curCss = "";
        for(int i = 0; i < listCssObserve.size(); i++){
            if(i == 0) {
                if(Main.currentCss.contains(name0)){
                    curCss = name0 + " (default)";
                }
            }
            if(Main.currentCss.contains(listCssObserve.get(i))){
                curCss = listCssObserve.get(i);
                System.out.println(curCss);
                break;
            }
        }
        propChoiceBox.getSelectionModel().select(curCss);

    }

}
