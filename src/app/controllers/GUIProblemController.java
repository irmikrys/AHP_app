package app.controllers;

import Jama.Matrix;
import app.*;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * Created on 14.04.2017.
 */


public class GUIProblemController implements Initializable {

    private String title;
    private String path;
    private boolean saved = false;

    private Matrix currentMatrix;
    private double currentCI;
    private double currentSliderCI;
    private Criterion selectedCriterion;
    private GridPane matrixGrid = new GridPane();
    private boolean goodMatrixRange = true;

    private List<Criterion> criterionList = new ArrayList<>();
    private List<Alternative> alternativeList = new ArrayList<>();

    private List<String> criterionListString = new ArrayList<>();
    private List<String> alternativeListString = new ArrayList<>();

    private ListProperty<String> listPropertyAlt = new SimpleListProperty<>();
    private ListProperty<String> listPropertyCrit = new SimpleListProperty<>();

    private List<String> saatyList = new ArrayList<>();
    private List<String> balancedList = new ArrayList<>();
    private ListProperty<String> listPropertySaaty = new SimpleListProperty<>();
    private ListProperty<String> listPropertyBalanced = new SimpleListProperty<>();

    /************** ogólne ***************/
    @FXML public Button btnCountRes;
    @FXML public Button btnReset;

    @FXML public Tab tabAlts;
    @FXML public Tab tabCrits;
    @FXML public Tab tabDetails;

    @FXML public BorderPane bpMain;
    /*************************************/

    /************** menu *****************/
    @FXML public MenuItem miTheme;
    @FXML public MenuItem miNew;
    /*************************************/

    /************ alternatywy ************/
    @FXML public Button btnNewAlt;
    @FXML public Button btnChangeAlt;
    @FXML public Button btnResetAlts;
    @FXML public Button btnDelAlt;

    @FXML public Label lblListAlt;
    @FXML public Label lblErrAlt;
    @FXML public Label lblTipAlt;

    @FXML public ListView<String> listAlt;

    @FXML public TextField fldEnterName;

    @FXML public HBox hBoxNameAlt;
    /*************************************/

    /************** kryteria *************/
    @FXML public Button btnNewCrit;
    @FXML public Button btnResetCrits;
    @FXML public Button btnDelCrit;

    @FXML public Label lblCrit;
    @FXML public Label lblListCrit;
    @FXML public Label lblNameCrit;
    @FXML public Label lblParentCrit;
    @FXML public Label lblErrCrit;

    @FXML public TreeView<String> treeCrit;

    @FXML public TextField fldCritName;

    @FXML public ComboBox<String> cbParentCrit;
    /*************************************/

    /************** detale ***************/
    @FXML public Button btnSubmitMtx;
    @FXML public Button btnResetMtx;

    @FXML public Label lblMatrix;
    @FXML public Label lblSliderVal;
    @FXML public Label lblCurrCIval;
    @FXML public Label lblAlertCI;

    @FXML public BorderPane matrixBorderPane;

    @FXML public ChoiceBox<String> choiceAuxiliaryBox;
    @FXML public ListView<String> listAuxiliary;
    @FXML public ListView<String> listAltDet;

    @FXML public Slider sliderCI;

    @FXML public TreeView<String> treeCritDet;
    /*************************************/

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        resetAll();

        listAlt.itemsProperty().bind(listPropertyAlt);
        listAltDet.itemsProperty().bind(listPropertyAlt);
        listPropertyAlt.set(FXCollections.observableArrayList(alternativeListString));

        cbParentCrit.itemsProperty().bind(listPropertyCrit);
        listPropertyCrit.set(FXCollections.observableArrayList(criterionListString));

        setUnmodifiableLists();
        setChoiceAux();
        setTreeListener();
        setListAltListener();
        setTreeCritListener();
        setSliderListener();

        listAuxiliary.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);
        listAltDet.addEventFilter(MouseEvent.MOUSE_PRESSED, Event::consume);

    }

    /******************* ogólne *******************/
    @FXML
    public void countResAction(ActionEvent e) {
        setPathAndTitle();
        boolean canCount = true;
        if(alternativeList.size() < 2 || criterionList.size() < 1) canCount = false;

        for(Criterion c: criterionList){
            if(!c.modifiedMatrix) {
                canCount = false;
                break;
            }
            if(!c.consistentMatrix) {
                canCount = false;
            }
        }

        if(!canCount) {
            String error = "An error occured. It might have been one of problems: ";
            error += "\n - There are less than 2 alternatives";
            error += "\n - There are not any criterions";
            error += "\n - Not all matrices have been submitted";
            error += "\n - Not all matrices are consistent \n";
            error += "\n Check above and count one more time.";
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().getStylesheets().add(Main.currentCss);
            alert.setTitle("Counting Result Error");
            alert.setHeaderText("Error");
            alert.setContentText(error);

            alert.showAndWait();
        }
        else {
            new XMLGenerator(path, alternativeList, criterionList);
            System.out.println();
            System.out.println("Parsowanie...");
            System.out.println();
            new ParserDOM().parseXML(path);
        }
    }

    @FXML
    public void resetAction(ActionEvent e) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Problem");
        alert.initStyle(StageStyle.UNDECORATED);
        alert.setContentText("Are you sure you want to reset the whole file?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK){
            // ... user chose OK
            resetAll();
        } else {
            // ... user chose CANCEL or closed the dialog
        }

    }

    //////////////////////////////////////////
    private void setPathAndTitle(){
        Stage stage = (Stage) btnCountRes.getScene().getWindow();
        title = stage.getTitle();
        path = "resources/xml/"+ title +".xml";
    }
    /**********************************************/

    /******************** menu ********************/

    @FXML
    public void newFile(ActionEvent e) throws IOException {
        //if(!saved){

        //}
        //else{
        Parent root2 = FXMLLoader.load(Main.class.getResource("/fxml/NewName.fxml"));
        Scene scene = new Scene(root2);
        scene.getStylesheets().add(Main.currentCss);
        Stage stage = (Stage) btnReset.getScene().getWindow();
        stage.setScene(scene);
        stage.setMinHeight(450);
        stage.setMinWidth(600);
        stage.setScene(scene);
        stage.show();

        //}
    }

    @FXML
    public void popupTheme(ActionEvent e) throws IOException {

        String css0 = Main.class.getResource("/css/Green.css").toExternalForm(); //default
        String css1 = Main.class.getResource("/css/BevelGrey.css").toExternalForm();
        String css7 = Main.class.getResource("/css/Royal.css").toExternalForm();

        List<String> listCss = new ArrayList<>(
                Arrays.asList(css0, css1, css7)
        );

        String name0 = StringUtils.substringBetween(css0,"/css/",".css");
        String name1 = StringUtils.substringBetween(css1,"/css/",".css");
        String name7 = StringUtils.substringBetween(css7,"/css/",".css");

        List<String> choices = new ArrayList<>();
        choices.add(name0); choices.add(name1); choices.add(name7);

        ChoiceDialog<String> dialog = new ChoiceDialog<>(StringUtils.substringBetween(Main.currentCss,"/css/",".css"), choices);
        dialog.setTitle("Change Theme");
        dialog.setHeaderText("");
        dialog.setContentText("Choose Theme:");
        dialog.getDialogPane().getStylesheets().add(Main.currentCss);

        GridPane grid = (GridPane) dialog.getDialogPane().getChildren().get(3);
        ComboBox<String> cb = (ComboBox) grid.getChildren().get(1);

        cb.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedCss = cb.getSelectionModel().getSelectedItem();
            for(int i = 0; i < listCss.size(); i++){
                if(listCss.get(i).contains(selectedCss)){
                    dialog.getDialogPane().getStylesheets().clear();
                    dialog.getDialogPane().getStylesheets().add(listCss.get(i));
                    break;
                }
            }
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            String curCss;
            for(int i = 0; i < listCss.size(); i++){
                if(listCss.get(i).contains(result.get())){
                    curCss = listCss.get(i);
                    Scene scene = btnCountRes.getScene();
                    scene.getStylesheets().clear();
                    scene.getStylesheets().add(curCss);
                    Main.currentCss = curCss;
                    break;
                }
            }

            System.out.println("Your choice: " + result.get());
        }

    }
    /**********************************************/

    /**************** alternatywy *****************/
    @FXML
    public void altNewAction(ActionEvent e){
        String newAltName = fldEnterName.getText();
        if(!newAltName.equals("")){
            if(checkNewAltName(newAltName)) {

                if(updateLeafsIfChangeAlt(Change.ChangeDetect.ALTERNATIVE_ADDED, newAltName)) {

                    alternativeListString.add(newAltName);
                    Alternative alternative = new Alternative(alternativeListString.size(), newAltName);
                    alternativeList.add(alternative);
                    listPropertyAlt.set(FXCollections.observableArrayList(alternativeListString));
                    fldEnterName.setText("");
                    lblErrAlt.setText("");
                    btnResetAlts.setDisable(false);

                    saved = false;
                }

            }
            else {
                System.out.println("Alternative already exists...");
            }
        }
        else {
            lblErrAlt.setText("You have to enter a name to add an alternative.");
        }
    }

    @FXML
    public void altChangeAction(ActionEvent e){
        lblErrAlt.setText("");
        String selected = listAlt.getSelectionModel().selectedItemProperty().getValue();
        int ind = alternativeListString.indexOf(selected);
        TextInputDialog dialog = new TextInputDialog(selected);
        dialog.getDialogPane().getStylesheets().add(Main.currentCss);
        dialog.setHeaderText("");
        dialog.setTitle("New name for selected alternative");
        dialog.setContentText("Enter new name:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if(!name.equals("")) {
                if(checkNewAltName(name)){
                    alternativeList.get(ind).name = name;
                    alternativeListString.remove(ind);
                    alternativeListString.add(ind, name);
                    listPropertyAlt.set(FXCollections.observableArrayList(alternativeListString));
                    btnChangeAlt.setDisable(true);
                    lblErrAlt.setText("");
                }
            }
            else {
                return;
            }
            System.out.println("New name: " + name);
            btnDelAlt.setDisable(true);
            lblTipAlt.setText("");
            btnChangeAlt.setDisable(true);

            saved = false;

        });
    }

    @FXML
    public void altDelAction(ActionEvent e) {
        String selected = listAlt.getSelectionModel().selectedItemProperty().getValue();
        if(alternativeListString.contains(selected)){
            if(updateLeafsIfChangeAlt(Change.ChangeDetect.ALTERNATIVE_DELETED, selected)) {
                int ind = alternativeListString.indexOf(selected);
                alternativeListString.remove(selected);
                alternativeList.remove(ind);
                updateAltList();
                listPropertyAlt.set(FXCollections.observableArrayList(alternativeListString));
                btnDelAlt.setDisable(true);
                if (alternativeListString.isEmpty()) {
                    btnResetAlts.setDisable(true);
                }
                saved = false;
            }
        }
        else{
            lblErrAlt.setText("There is no such alternative on the back alternative list");
        }
        btnChangeAlt.setDisable(true);
        lblTipAlt.setText("");
    }

    @FXML
    public void altResetAction(ActionEvent e){
        resetAlternatives();
        saved = false;
    }

    //////////////////////////////////////

    private boolean updateLeafsIfChangeAlt(Change.ChangeDetect change, String name){

        String communicate;
        String header;
        if(change == Change.ChangeDetect.ALTERNATIVE_ADDED){
            communicate = "The matrix of at least one criterion without children is already modified." +
                    " Are you sure you want to add another alternative? All matrices will be expanded, what might " +
                    "cause their inconsistency.";
            header = "Add Alternative";
        } else { //(change == Change.ChangeDetect.ALTERNATIVE_DELETED)
            communicate = "The matrix of at least one criterion is already modified." +
                    " Are you sure you want to delete this alternative? Matrices of leaf-criterions will change.";
            header = "Delete Alternative";
        }
        //jak macierze lisci juz edytowane to komunikat i zmienione wszystkie
        for (Criterion criterion: criterionList) {
            if(criterion.children.size() == 0){
                if(criterion.modifiedMatrix) {
                    //alert - jak OK to wsysztkie zeruje, jak nie ok to return
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText(header);
                    alert.getDialogPane().getStylesheets().add(Main.currentCss);
                    alert.setContentText(communicate);

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        //update wszystkich macierzy dla liści
                        for(Criterion c : criterionList) {
                            if(c.children.size() == 0){
                                if(!c.modifiedMatrix) {
                                    if(change == Change.ChangeDetect.ALTERNATIVE_ADDED) {
                                        c.matrix = new Matrix(alternativeList.size() + 1, alternativeList.size() + 1, 1.0);
                                        //jak wybrany lisc to uaktualnienie macierzy grid
                                        if(treeCritDet.getSelectionModel().getSelectedItems().size() > 0) {
                                            TreeItem<String> selectedItem = treeCritDet.getSelectionModel().getSelectedItem();
                                            Criterion crit = findCriterion(selectedItem.getValue());
                                            if (crit.children.size() == 0) {
                                                currentMatrix = c.matrix;
                                                crit.matrix = c.matrix;
                                                showCurrentMatrix(crit, alternativeList.size()+1);
                                            }
                                        }
                                    }
                                    else {
                                        c.matrix = new Matrix(alternativeList.size() - 1, alternativeList.size() - 1, 1.0);
                                        //jak wybrany lisc to uaktualnienie macierzy grid
                                        if(treeCritDet.getSelectionModel().getSelectedItems().size() > 0) {
                                            TreeItem<String> selectedItem = treeCritDet.getSelectionModel().getSelectedItem();
                                            Criterion crit = findCriterion(selectedItem.getValue());
                                            if (crit.children.size() == 0) {
                                                currentMatrix = c.matrix;
                                                crit.matrix = c.matrix;
                                                showCurrentMatrix(crit, alternativeList.size()-1);
                                            }
                                        }
                                    }
                                }
                                else{ //przepisanie bez danej kolumny i wiersza lub dodanie nowych elementow
                                    if(change == Change.ChangeDetect.ALTERNATIVE_ADDED){
                                        Matrix updated;
                                        int currSize = alternativeList.size()+1;
                                        //jak pierwsza to macierz 1x1
                                        if(currSize == 1) {
                                            updated = new Matrix(currSize, currSize, 1.0);
                                        }
                                        else{
                                            updated = new Matrix(currSize, currSize);
                                            for(int i = 0; i < currSize; i++){
                                                for(int j = 0 ; j < currSize; j++){
                                                    if(i == currSize - 1 || j == currSize - 1){
                                                        updated.set(i,j,1.0);
                                                    }
                                                    else {
                                                        updated.set(i, j, c.matrix.get(i, j));
                                                    }
                                                }
                                            }
                                        }
                                        c.matrix = updated;
                                        c.modifiedMatrix = true;
                                        //jak wybrany lisc to uaktualnienie macierzy grid
                                        if(treeCritDet.getSelectionModel().getSelectedItems().size() > 0) {
                                            TreeItem<String> selectedItem = treeCritDet.getSelectionModel().getSelectedItem();
                                            Criterion crit = findCriterion(selectedItem.getValue());
                                            if (crit.children.size() == 0) {
                                                currentMatrix = updated;
                                                crit.matrix = updated;
                                                crit.modifiedMatrix = true;
                                                showCurrentMatrix(crit, alternativeList.size()+1);
                                            }
                                        }
                                    }
                                    else{ //ALTERNATIVE_DELETED
                                        //sprawdzamy na ktorym miejscu byla alternatywa
                                        int ind = -1;
                                        for(int i = 0; i < alternativeList.size(); i++){
                                            if(alternativeList.get(i).name.equals(name)){
                                                ind = i;
                                                break;
                                            }
                                        }
                                        //usuwamy ten wiersz i kolumne które byly odpowiedzialne za ta alternatywe
                                        int newSize = alternativeList.size() - 1;
                                        Matrix updated = new Matrix(newSize, newSize,1);

                                        boolean iAfterInd = false;
                                        for(int i = 0; i < newSize; i++){
                                            boolean jAfterInd = false;
                                            if(i == ind) iAfterInd = true;
                                            for(int j = 0; j < newSize; j++){
                                                if(j == ind) jAfterInd = true;
                                                if(jAfterInd) {
                                                    if(iAfterInd){ updated.set(i,j,c.matrix.get(i+1,j+1));}
                                                    else {updated.set(i,j,c.matrix.get(i, j+1));}
                                                }
                                                else {
                                                    if(iAfterInd) {updated.set(i,j,c.matrix.get(i+1,j));}
                                                    else {updated.set(i,j,c.matrix.get(i,j));}
                                                }
                                            }
                                        }
                                        c.matrix = updated;
                                        c.modifiedMatrix = true;
                                        //jak wybrany lisc to uaktualnienie macierzy grid
                                        if(treeCritDet.getSelectionModel().getSelectedItems().size() > 0) {
                                            TreeItem<String> selectedItem = treeCritDet.getSelectionModel().getSelectedItem();
                                            Criterion crit = findCriterion(selectedItem.getValue());
                                            if (crit.children.size() == 0) {
                                                currentMatrix = updated;
                                                crit.matrix = updated;
                                                crit.modifiedMatrix = true;
                                                showCurrentMatrix(crit, alternativeList.size()-1);
                                            }
                                        }
                                    }
                                }
                                break;
                            }

                        }

                        return true;
                    } else {
                        // ... user chose CANCEL or closed the dialog
                        return false;
                    }
                }
                break;
            }

        }

        return true;

    }

    private void setListAltListener(){
        listAlt.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            lblErrAlt.setText("");
            lblTipAlt.setText("Change the name of alternative by clicking \"Change name\" button.");
            btnDelAlt.setDisable(false);
            btnChangeAlt.setDisable(false);
        });
    }

    private boolean checkNewAltName(String newAltName) {
        char[] chars = newAltName.toCharArray();
        if(!Character.isLetter(chars[0])) {
            lblErrAlt.setText("Name has to start with a letter.");
            return false;
        }
        for (char c : chars) {
            if (!Character.isLetterOrDigit(c)) {
                lblErrAlt.setText("Name can only contain letters and digits.");
                return false;
            }
        }
        for (String s: alternativeListString) {
            if(s.equalsIgnoreCase(newAltName)) {
                lblErrAlt.setText("This alternative is already added.");
                return false;
            }
        }
        return true; //nie ma na listach - jest okej
    }

    private void resetAlternatives() {
        alternativeList.clear();
        alternativeListString.clear();
        listPropertyAlt.set(FXCollections.observableArrayList(alternativeListString));
        fldEnterName.setText("");
        btnDelAlt.setDisable(true);
        lblErrAlt.setText("");
        lblTipAlt.setText("");
        btnResetAlts.setDisable(true);
        btnChangeAlt.setDisable(true);

        if(criterionList.size() == 1) btnReset.setDisable(true);
    }

    private void updateAltList() {
        for(int i = 0; i < alternativeListString.size(); i++){
            alternativeList.get(i).id = i + 1;
        }
    }

    /**********************************************/

    /****************** kryteria ******************/
    @FXML
    public void critNewAction(ActionEvent e){
        String newCritName = fldCritName.getText();
        String parentName = cbParentCrit.getSelectionModel().getSelectedItem();

        Criterion parent = findCriterion(parentName);
        System.out.println("Parent found: " + parent.name);

        if(!newCritName.equals("")){
            if(checkNewCritName(newCritName)) {

                //jak macierz rodzica juz edytowana to komunikat
                if (parent.modifiedMatrix) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Add New Child");
                    alert.getDialogPane().getStylesheets().add(Main.currentCss);
                    alert.setContentText("The matrix of this criterion parent is already modified. Are you sure you want to add "
                            + "this criterion to its children? If it is first child, current data will be lost.");

                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.get() == ButtonType.OK){
                        // ... user chose OK
                    } else {
                        // ... user chose CANCEL or closed the dialog
                        fldCritName.setText("");
                        lblErrCrit.setText("");
                        btnResetCrits.setDisable(false);
                        cbParentCrit.getSelectionModel().select("prior");
                        return;
                    }
                }

                int ind = criterionListString.indexOf(parent.name);
                int childNum = parent.children.size();
                criterionListString.add(ind + childNum + 1, newCritName);
                Criterion criterion = new Criterion(newCritName, null, null, parent);
                criterionList.add(ind + childNum + 1, criterion);
                parent.addChild(criterion);
                //uaktualnienie macierzy rodzica
                updateAfterCritAdd(parent);

                listPropertyCrit.set(FXCollections.observableArrayList(criterionListString));

                TreeItem<String> item1 = new TreeItem<> (newCritName);
                item1.setExpanded(true);
                TreeItem<String> treeItemParent = findTreeItemCrit(parentName);
                treeItemParent.getChildren().add(item1);

                TreeItem<String> item2 = new TreeItem<> (newCritName);
                item2.setExpanded(true);
                TreeItem<String> treeItemParentDet = findTreeItemDet(parentName);
                treeItemParentDet.getChildren().add(item2);

                fldCritName.setText("");
                lblErrCrit.setText("");
                btnResetCrits.setDisable(false);
                cbParentCrit.getSelectionModel().select("prior");

                //jak rodzic tego byl wybrany to uaktualnij macierz grid
                if (treeCritDet.getSelectionModel().getSelectedItems().size() > 0) {
                    if (treeCritDet.getSelectionModel().getSelectedItem().getValue().equals(parent.name)) {
                        showCurrentMatrix(parent, parent.children.size());
                    }
                }

                saved = false;

            }
            else {
                System.out.println("Something went wrong while checking criterions...");
            }
        }
        else {
            lblErrCrit.setText("You have to enter a name to add a criterion.");
        }
    }

    @FXML
    public void critChangeAction(ActionEvent e){
        saved = false;
    }

    @FXML
    public void critResetAction(ActionEvent e){
        resetCriterions();
        saved = false;
    }

    @FXML
    public void critDelAction(ActionEvent e) {
        String selected = treeCrit.getSelectionModel().getSelectedItem().getValue();
        if(selected.equals("prior")) {
            lblErrCrit.setText("You can't delete prior element.");
            e.consume();
        }
        else {
            Criterion crit = findCriterion(selected);

            //jak byla edytowana macierz tego kryterium juz to komunikat
            if (crit.modifiedMatrix) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Delete Criterion");
                alert.getDialogPane().getStylesheets().add(Main.currentCss);
                alert.setContentText("The matrix of this criterion is already modified. Are you sure you want to delete "
                        + "this criterion? Also its children data will be lost.");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    // ... user chose OK
                } else {
                    // ... user chose CANCEL or closed the dialog
                    return;
                }
            }

            //sprawdzenie czy rodzicowi zostają jakieś dzieci, jak tak to nowy wymiar macierzy, jak nie to przepisanie
            Criterion parent = findCriterion(crit.parent.name);
            int size;
            if (parent.children.size() > 1) {
                size = parent.children.size() - 1;
                updateAfterCritDel(parent, crit);
            } else { //ostatnie dziekco to bylo
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Delete Criterion");
                alert.getDialogPane().getStylesheets().add(Main.currentCss);
                alert.setContentText("It is the last child of its criterion parent." +
                        "Its data will be copied to its parent matrix. Are you OK with that?");

                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK){
                    // ... user chose OK
                } else {
                    // ... user chose CANCEL or closed the dialog
                    return;
                }

                size = alternativeList.size();
                parent.weights = null;
                parent.matrix = crit.matrix;
                parent.modifiedMatrix = true;
            }

            TreeItem<String> critItem = (TreeItem) treeCrit.getSelectionModel().getSelectedItem();
            critItem.getParent().getChildren().remove(critItem);

            TreeItem detItem = findTreeItemDet(critItem.getValue());
            detItem.getParent().getChildren().remove(detItem);

            deleteWithChildren(crit, criterionList, criterionListString);
            crit.parent.children.remove(crit);
            parent.children.remove(crit);

            listPropertyCrit.set(FXCollections.observableArrayList(criterionListString));
            btnDelCrit.setDisable(true);
            lblErrCrit.setText("");

            //jak w detalach wybrany rodzic to musi byc od razu uaktualnienie macierzy w gridzie
            if (treeCritDet.getSelectionModel().getSelectedItems().size() > 0) {
                if (treeCritDet.getSelectionModel().getSelectedItem().getValue().equals(parent.name)) {
                    showCurrentMatrix(parent, size);
                }
            }
            saved = false;
        }
    }

    ////////////////////////////////////
    private void deleteWithChildren(Criterion criterion, List<Criterion> list, List<String> names) {
        //jak ma dzieci to usun dzieci rekurencyjnie
        if(criterion.children.size() > 0) {
            for(Criterion c: criterion.children)
                deleteWithChildren(c, list, names);
        }
        //usun criterion
        list.remove(criterion);
        names.remove(criterion.name);
    }

    private void updateAfterCritDel(Criterion parent, Criterion child){
        //sprawdzamy na ktorym miejscu bylo kryterium
        int ind = -1;
        for(int i = 0; i < parent.children.size(); i++){
            if(parent.children.get(i).name.equals(child.name)){
                ind = i;
                break;
            }
        }
        //usuwamy ten wiersz i kolumne które byly odpowiedzialne za to dziecko
        int newSize = parent.children.size() - 1;
        Matrix updated = new Matrix(newSize, newSize,1);

        boolean iAfterInd = false;
        for(int i = 0; i < newSize; i++){
            boolean jAfterInd = false;
            if(i == ind){
                iAfterInd = true;
            }
            for(int j = 0; j < newSize; j++){
                if(j == ind) {
                    jAfterInd = true;
                }

                if(jAfterInd) {
                    if(iAfterInd) {
                        updated.set(i,j,parent.weights.get(i+1,j+1));
                    }
                    else{
                        updated.set(i,j,parent.weights.get(i, j+1));
                    }
                }
                else {
                    if(iAfterInd) {
                        updated.set(i,j,parent.weights.get(i+1,j));
                    }
                    else{
                        updated.set(i,j,parent.weights.get(i,j));
                    }
                }
            }
        }
        parent.weights = updated;
    }

    private void updateAfterCritAdd(Criterion parent){
        Matrix updated;
        int currSize = parent.children.size();
        //jak pierwsze dziecko to macierz 1x1
        if(currSize == 1) {
            parent.matrix = null;
            updated = new Matrix(currSize, currSize, 1.0);
        }
        else{
            updated = new Matrix(currSize, currSize);
            for(int i = 0; i < currSize; i++){
                for(int j = 0 ; j < currSize; j++){
                    if(i == currSize - 1 || j == currSize - 1){
                        updated.set(i,j,1.0);
                    }
                    else {
                        updated.set(i, j, parent.weights.get(i, j));
                    }
                }
            }
        }
        parent.weights = updated;
    }

    private void setTreeCritListener(){
        treeCrit.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            lblErrCrit.setText("");
            btnDelCrit.setDisable(false);
        });
    }

    private void resetCriterions() {
        btnNewCrit.setDefaultButton(true);

        criterionList.clear();
        criterionListString.clear();

        criterionList.add(new Criterion("prior",null,null,null));
        criterionListString.add("prior");
        listPropertyCrit.set(FXCollections.observableArrayList(criterionListString));
        cbParentCrit.getSelectionModel().select("prior");

        TreeItem<String> rootItem = new TreeItem<String> ("prior");
        rootItem.setExpanded(true);
        treeCrit.setRoot(rootItem);
        treeCrit.setShowRoot(true);

        fldCritName.setText("");
        btnDelCrit.setDisable(true);
        lblErrCrit.setText("");
        btnResetCrits.setDisable(true);

        if(alternativeList.size() == 0) btnReset.setDisable(true);
    }

    private boolean checkNewCritName(String name){
        char[] chars = name.toCharArray();
        if(!Character.isLetter(chars[0])) {
            lblErrAlt.setText("Name has to start with a letter.");
            return false;
        }
        for (char c : chars) {
            if (!Character.isLetterOrDigit(c)) {
                lblErrAlt.setText("Name can only contain letters and digits.");
                return false;
            }
        }
        for (String s: criterionListString) {
            if(s.equalsIgnoreCase(name)) {
                lblErrCrit.setText("This criterion is already added. Please pick another name.");
                return false;
            }
        }
        return true; //nie ma na listach - jest okej
    }

    private Criterion findCriterion(String name){
        Criterion c = null;
        for(int i = 0; i < criterionList.size(); i++){
            if(name.equals(criterionList.get(i).name)){
                c = criterionList.get(i);
                break;
            }
        }
        return c;
    }

    private TreeItem<String> findTreeItemCrit(String name){
        TreeItem<String> root = treeCrit.getRoot();
        expandTreeView(root);
        TreeItem<String> treeItem = new TreeItem<>();
        for(int i = 0; i < treeCrit.getExpandedItemCount(); i++){
            if(treeCrit.getTreeItem(i).getValue().equals(name)) {
                treeItem = treeCrit.getTreeItem(i);
                break;
            }
        }
        return treeItem;
    }

    private TreeItem<String> findTreeItemDet(String name){
        TreeItem<String> root = treeCritDet.getRoot();
        expandTreeView(root);
        System.out.println("Number of criterions details: " + treeCritDet.getExpandedItemCount() + "!");
        System.out.println();
        TreeItem<String> treeItem = new TreeItem<>();
        for(int i = 0; i < treeCritDet.getExpandedItemCount(); i++){
            if(treeCritDet.getTreeItem(i).getValue().equals(name)) {
                treeItem = treeCritDet.getTreeItem(i);
                break;
            }
        }
        return treeItem;
    }

    private void expandTreeView(TreeItem<String> item){
        if(item != null && !item.isLeaf()){
            item.setExpanded(true);
            for(TreeItem<String> child:item.getChildren()){
                expandTreeView(child);
            }
        }
    }

    /**********************************************/

    /******************* detale *******************/
    @FXML
    public void detSubmitAction(ActionEvent e){
        System.out.println("Submit button clicked...");
        if(selectedCriterion != null) {
            Criterion c = findCriterion(selectedCriterion.name);
            try {
                updateMatrixFromGrid(selectedCriterion);
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            System.out.println("SUBMIT _ CURRENT MATRIX :");
            currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getRowDimension());
            //boolean goodRange = checkMatrixRange(currentMatrix);
            //checkMatrixRange(currentMatrix);
            if(selectedCriterion.children.size() > 0){
                selectedCriterion.weights = currentMatrix;
                selectedCriterion.modifiedMatrix = true;
            }
            else {
                selectedCriterion.matrix = currentMatrix;
                selectedCriterion.modifiedMatrix = true;
            }
            try {

                //if (!goodRange) {
                showCurrentMatrix(selectedCriterion, currentMatrix.getRowDimension());

                //} else if(goodRange){
                //przepisz wszytskie wartosci z grida jak okej zakres
                updateMatrixFromGrid(selectedCriterion);
                System.out.println("Updated from grid:");
                currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getColumnDimension());

                //wpisz odwrotnosci do grida
                AlgoHandler.reciprocMatrix(currentMatrix);
                //System.out.println("Reciprocals:"); currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getColumnDimension());
                if(selectedCriterion.children.size() > 0){
                    selectedCriterion.weights = currentMatrix;
                    c.weights = currentMatrix;
                    //System.out.println("Weights:");currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getColumnDimension());
                }
                else{
                    selectedCriterion.matrix = currentMatrix;
                    c.matrix = currentMatrix;
                    //System.out.println("Matrix:"); currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getColumnDimension());
                }
                selectedCriterion.modifiedMatrix = true; c.modifiedMatrix = true;
                showCurrentMatrix(selectedCriterion, currentMatrix.getRowDimension());

                //check consistency(currentMatrix)
                if(AlgoHandler.checkIfConsistent(currentMatrix, currentMatrix.getRowDimension())){
                    lblAlertCI.setText("Matrix is consistent. You can go on.");
                    System.out.println("Consistent:");
                    currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getColumnDimension());
                    showCurrentMatrix(selectedCriterion, currentMatrix.getRowDimension());
                    selectedCriterion.consistentMatrix = true;
                    c.consistentMatrix = true;
                }
                else{
                    lblAlertCI.setText("Matrix INCONSISTENT! You won't be able to count result.");
                    System.out.println("Inconsistent:");
                    currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getColumnDimension());
                    showCurrentMatrix(selectedCriterion, currentMatrix.getRowDimension());
                    selectedCriterion.consistentMatrix = false;
                    c.consistentMatrix = false;
                }
                saved = false;

                //}

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        else{
            lblAlertCI.setText("Nothing to submit.");
        }
    }


    @FXML
    public void resetMatrix(ActionEvent e) {
        if(selectedCriterion != null) {
            try {
                int size = getSizeOfGrid(matrixGrid);
                for (int i = 0; i < size; i++) {
                    for (int j = 0; j < size; j++) {
                        setValueOfTextField(matrixGrid, i, j, 1.0);
                        currentMatrix.set(i, j, 1.0);
                    }
                }
                if (selectedCriterion.children.size() == 0) {
                    selectedCriterion.matrix = currentMatrix;
                } else {
                    selectedCriterion.weights = currentMatrix;
                }
                selectedCriterion.modifiedMatrix = false;

                showCurrentMatrix(selectedCriterion,currentMatrix.getRowDimension());

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else{
            lblAlertCI.setText("Nothing to reset.");
        }
    }

    ///////////////////////////////////

    private void resetDetails(){
        currentMatrix = null;
        //btnResetMtx.setDisable(true);
        sliderCI.setValue(0.10);
        AlgoHandler.maxCI = sliderCI.getValue();
        lblSliderVal.setText("0,10");
        lblCurrCIval.setText("0.0");
        AlgoHandler.currCI = 0.0;

        currentCI = 0.0;
        currentSliderCI = 0.10;

        lblMatrix.setText("Choose criterion:");
        lblAlertCI.setText("");

        TreeItem<String> rootItem = new TreeItem<String> ("prior");
        rootItem.setExpanded(true);
        treeCritDet.setRoot(rootItem);
        treeCritDet.setShowRoot(true);
    }

    private void setChoiceAux(){
        choiceAuxiliaryBox.setItems(FXCollections.observableArrayList("Balanced", "By Saaty"));
        choiceAuxiliaryBox.getSelectionModel().select("Balanced");
        listAuxiliary.itemsProperty().bind(listPropertyBalanced);
        choiceAuxiliaryBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selectedList = choiceAuxiliaryBox.getSelectionModel().getSelectedItem();
            if(selectedList.equals("By Saaty")){
                listAuxiliary.itemsProperty().bind(listPropertySaaty);
            }
            else{
                listAuxiliary.itemsProperty().bind(listPropertyBalanced);
            }
        });
    }

    private void setUnmodifiableLists(){

        saatyList.add("1 - Indifference");
        saatyList.add("2 - <->");
        saatyList.add("3 - Moderate preference");
        saatyList.add("4 - <->");
        saatyList.add("5 - Strong preference");
        saatyList.add("6 - <->");
        saatyList.add("7 - Very strong/Demonstrated preference");
        saatyList.add("8 - <->");
        saatyList.add("9 - Extreme preference");

        listPropertySaaty.set(FXCollections.observableArrayList(saatyList));


        balancedList.add("1 - Indifference");
        balancedList.add("1.22 - <->");
        balancedList.add("1.5 - Moderate preference");
        balancedList.add("1.86 - <->");
        balancedList.add("2.33 - Strong preference");
        balancedList.add("3 - <->");
        balancedList.add("4 - Very strong/Demonstrated preference");
        balancedList.add("5.67 - <->");
        balancedList.add("9 - Extreme preference");

        listPropertyBalanced.set(FXCollections.observableArrayList(balancedList));


    }

    private void setSliderListener(){
        sliderCI.valueProperty().addListener((ov, old_val, new_val) -> {
            lblSliderVal.setText(String.format("%.2f", (double) new_val));
            currentSliderCI = (double) new_val;
            AlgoHandler.maxCI = currentSliderCI;
            setLabelConsistent();
        });
    }

    private void setTreeListener(){
        treeCritDet.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String selected = newValue.getValue();
            int size = -1;
            for (Criterion crit: criterionList) {
                if(crit.name.equals(selected)){
                    selectedCriterion = crit;
                    System.out.println("Znalazłem kryterium...");
                    break;
                }
            }
            if(selectedCriterion.children.size() != 0) {
                lblMatrix.setText("Comparison between criterions:");
                System.out.println("Dzieci: " + selectedCriterion.children.size());
                size = selectedCriterion.children.size();
            }
            else {
                lblMatrix.setText("Comparison between alternatives:");
                System.out.println("Liść, alternatyw: " + alternativeList.size());
                size = alternativeList.size();
            }
            showCurrentMatrix(selectedCriterion, size);
            try {
                updateMatrixFromGrid(selectedCriterion);
            }
            catch(Exception ex){
                ex.printStackTrace();
            }
            System.out.println("Zakończono tworzenie macierzy o rozmiarze "+size+"x"+size+"!");
        });
    }

    private void showCurrentMatrix(Criterion searched, int size) {

        if(!searched.modifiedMatrix) {
            currentMatrix = new Matrix(size, size, 1);
            if(searched.children.size() > 0) {
                searched.weights = currentMatrix;
                lblMatrix.setText("Comparison between criterions:");
            }
            else {
                searched.matrix = currentMatrix;
                lblMatrix.setText("Comparison between alternatives:");
            }
        }
        else {
            if(searched.children.size() <= 0){
                currentMatrix = searched.matrix;
                lblMatrix.setText("Comparison between alternatives:");
            }
            else{
                currentMatrix = searched.weights;
                lblMatrix.setText("Comparison between criterions:");
            }
        }

        //StackPane gridHolder = new StackPane(matrixGrid);
        //mtxScrollPane.setContent(gridHolder);
        matrixGrid = new GridPane();

        matrixGrid.setMinHeight(30*size);
        matrixGrid.setMinWidth(30*size);
        matrixGrid.setMaxHeight(30*size);
        matrixGrid.setMaxWidth(50*size);

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {

                TextField tf = new TextField();
                Double value = currentMatrix.get(i,j);
                tf.setText(value.toString());
                tf.setAlignment(Pos.CENTER);

                tf.setEditable(true);
                if(j <= i) {
                    tf.setDisable(true);
                }
                tf.setPrefHeight(Region.USE_COMPUTED_SIZE);
                tf.setPrefWidth(Region.USE_COMPUTED_SIZE);

                GridPane.setRowIndex(tf, i);
                GridPane.setColumnIndex(tf, j);
                matrixGrid.getChildren().add(tf);
            }
        }

        /*
        gridHolder.minWidthProperty().bind(Bindings.createDoubleBinding(() ->
                scrollPane.getViewportBounds().getWidth(), scrollPane.viewportBoundsProperty()));

        gridHolder.minHeightProperty().bind(Bindings.createDoubleBinding(() ->
                scrollPane.getViewportBounds().getHeight(), scrollPane.viewportBoundsProperty()));

        scrollPane.setMinHeight(30*size);
        scrollPane.setMaxHeight(30*size);

        scrollPane.setMinWidth(30*size + 20);
        scrollPane.setPrefWidth(50*size + 20);
        scrollPane.setMaxWidth(50*size + 20);

        matrixBorderPane.setCenter(scrollPane);
        */

        matrixBorderPane.setCenter(matrixGrid);

        if(currentMatrix != null) {
            if (currentMatrix.getRowDimension() > 0) {
                currentCI = AlgoHandler.countCurrCI(currentMatrix, size);
                lblCurrCIval.setText(((Double) AlgoHandler.round(currentCI, 4)).toString());
            }
        }

        setLabelConsistent();
    }

    private void setLabelConsistent(){
        if(!goodMatrixRange){
            lblAlertCI.setText("Numbers have to be from range 0.12 to 9.0.");
            selectedCriterion.consistentMatrix = false;
        }
        else if(!selectedCriterion.modifiedMatrix){
            lblAlertCI.setText("");
        }
        else if(currentCI <= currentSliderCI){
            if(selectedCriterion.modifiedMatrix) {
                lblAlertCI.setText("Matrix is consistent. You can go on.");
                selectedCriterion.consistentMatrix = true;
            }
            else{
                lblAlertCI.setText("");
            }
        }
        else{
            lblAlertCI.setText("Matrix INCONSISTENT! You won't be able to count result.");
            selectedCriterion.consistentMatrix = false;
        }
    }

    private void updateMatrixFromGrid(Criterion criterion) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Integer rows = getSizeOfGrid(matrixGrid);
        if(currentMatrix.getColumnDimension() != rows){
            System.out.println();System.out.println("Inne wymiary matrixGrid i currentMatrix...");
            currentMatrix = new Matrix(rows,rows);
        }
        for(int i = 0; i < rows; i++) {
            for(int j = 0; j < rows; j++) {
                double val = getValueFromTextField(matrixGrid,i,j);
                currentMatrix.set(i,j,val);
            }
        }
        if(criterion.children.size() == 0) {
            criterion.matrix = currentMatrix;
        }
        else {
            criterion.weights = currentMatrix;
        }
        currentMatrix.print(currentMatrix.getRowDimension(), currentMatrix.getRowDimension());
        if(!goodMatrixRange){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning - matrix elements range");
            alert.setHeaderText("Warning");
            alert.setContentText("There was at least one field containing a non-double or number out of range 0.11 to 9.0." +
                    "All out-of-range fields were changed to default. You will be able to edit these elements again." +
                    " Matrix will be submitted anyway.");
            alert.getDialogPane().getStylesheets().add(Main.currentCss);
            alert.showAndWait();
        }
        goodMatrixRange = true;
        /*if(criterion.matrix != null) {
            if(criterion.matrix.getColumnDimension() > 0) {
                criterion.modifiedMatrix = true;
            }
        }
        if(criterion.weights != null){
            if(criterion.weights.getColumnDimension() > 0) {
                criterion.modifiedMatrix = true;
            }
        }*/
    }

    private Double getValueFromTextField(GridPane gridPane, int row, int col) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Double value;
        Integer size = getSizeOfGrid(gridPane);
        TextField tf = (TextField) gridPane.getChildren().get(row * size + col);
        String text = tf.getText();
        if(text.matches("^[0-9]+\\.?[0-9]*$")) {
            value = Double.parseDouble(tf.getText());
            if(value < 0.11 || value > 9.0){
                goodMatrixRange = false;
                tf.setText("1.0");
            }
        }
        else{
            goodMatrixRange = false;
            tf.setText("1.0");

        }
        value = Double.parseDouble(tf.getText());
        return value;
    }

    private void setValueOfTextField(GridPane gridPane, int row, int col, Double value) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Integer size = getSizeOfGrid(gridPane);
        TextField tf = (TextField) gridPane.getChildren().get(row * size + col);
        tf.setText(value.toString());
    }

    private int getSizeOfGrid(GridPane gridPane) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = gridPane.getClass().getDeclaredMethod("getNumberOfRows");
        method.setAccessible(true);
        return (int) method.invoke(gridPane);
    }

    /**********************************************/


    private void resetAll(){

        resetAlternatives();
        resetCriterions();
        resetDetails();

        //btnCountRes.setDisable(true);
        btnReset.setDisable(true);
        saved = true;

    }
}
