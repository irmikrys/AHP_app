package app;

import Jama.Matrix;
import javafx.scene.control.Alert;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 12.03.2017.
 */
public class ParserDOM {

    public NodeList alternatives;
    public NodeList criterions;

    public List<Criterion> crits = new ArrayList<>();


    public void parseXML(String path){
        try {

            File inputFile = new File(path);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            giveRootElement(doc);
            giveAlternatives(doc);
            handleCriterions(doc);

            System.out.println("Data read..."); System.out.println();

            checkAndChange();
            displayCrits();

            System.out.println("Starting last counting...");
            evaluateValue("prior");
            printResult();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayCrits(){
        System.out.println("All criterions: ");
        for(Criterion c: crits){
            System.out.println("-" + c.name);
            if(c.hasCriterionChildren()){
                for(Criterion ch: crits){
                    if(ch.parent == c){
                        System.out.println("\t-" + ch.name);

                        if(ch.vectorCrit != null) ch.vectorCrit.print(ch.vectorCrit.getRowDimension(),4);
                        if(ch.vectorWeigh != null) ch.vectorWeigh.print(ch.vectorWeigh.getRowDimension(),4);
                        System.out.println("----");
                    }
                }
            }
            System.out.print("\t\t");
            if(c.vectorCrit != null) c.vectorCrit.print(c.vectorCrit.getRowDimension(),4);
            if(c.vectorWeigh != null) c.vectorWeigh.print(c.vectorWeigh.getRowDimension(),4);
        }
        System.out.println();
    }

    private void printResult(){
        System.out.println("---Result: ");
        String bestAlt = "";
        double bestVal = -1;
        for(int i = 0; i < alternatives.getLength(); i++){
            System.out.print(alternatives.item(i).getTextContent() + ": ");
            System.out.println(crits.get(0).vectorCrit.get(i,0));
            if (crits.get(0).vectorCrit.get(i,0) > bestVal) {
                bestVal = crits.get(0).vectorCrit.get(i,0);
                bestAlt = alternatives.item(i).getTextContent();
            }
        }
        System.out.println("-------------");
        System.out.println("Best alternative is: " + bestAlt + "!");
        System.out.println("-------------");

        String resulting = "";
        for(int i = 0; i < alternatives.getLength(); i++){
            resulting += alternatives.item(i).getTextContent() + ": ";
            resulting += crits.get(0).vectorCrit.get(i,0) + "\n";

        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Result");
        alert.getDialogPane().getStylesheets().add(Main.currentCss);
        alert.setHeaderText("The best is: " + bestAlt + "!");
        alert.setContentText(resulting);

        alert.showAndWait();
    }

    private void checkAndChange() {
        for(Criterion c: crits){
            if(c.children.size() > 0){ //jak c z crits ma jakies dieci
                for(Criterion child: c.children){
                    int ind = c.children.indexOf(child); //zeby wiedziec ktore dziecko zamienic
                    for(Criterion eqNameChild: crits){
                        if(eqNameChild.name.equals(child.name)){
                            if(eqNameChild.vectorCrit != null) {
                                System.out.println(eqNameChild.name + " " + child.name + ": ");
                                eqNameChild.vectorCrit.print(eqNameChild.vectorCrit.getRowDimension(), 4);
                            }
                            if(eqNameChild.vectorWeigh != null) {
                                System.out.println(eqNameChild.name + " " + child.name + ": ");
                                eqNameChild.vectorWeigh.print(eqNameChild.vectorWeigh.getRowDimension(), 4);
                            }
                            child = eqNameChild;
                            if(child.vectorCrit != null) {
                                System.out.println(eqNameChild.name + " " + child.name + ": ");
                                child.vectorCrit.print(child.vectorCrit.getRowDimension(), 4);
                            }
                            if(child.vectorWeigh != null) {
                                System.out.println(eqNameChild.name + " " + child.name + ": ");
                                child.vectorWeigh.print(child.vectorWeigh.getRowDimension(), 4);
                            }
                        }
                    }
                }
            }
        }
    }


    public void giveRootElement(Document doc){
        System.out.println("\n----------------------------");
        System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
        System.out.println("----------------------------");
    }

    public void giveAlternatives(Document doc) {
        alternatives = doc.getElementsByTagName("alternative");
        for(int i = 0; i < alternatives.getLength(); i++){
            System.out.println("\n------------ALT-------------");
            System.out.println(alternatives.item(i).getTextContent() + "\n " +
                    alternatives.item(i).getAttributes().getNamedItem("id"));
            System.out.println("----------------------------");
        }
    }


    public void handleCriterions(Document doc){
        criterions = doc.getElementsByTagName("criterion");
        for(int i = 0; i < criterions.getLength(); i++){

            Element currElem = (Element) criterions.item(i);
            System.out.println("\n-----------CRIT-------------");

            Criterion currCrit = new Criterion();

            currCrit.name = currElem.getAttribute("name");
            System.out.println("name: " + currCrit.name);
            Criterion c = isChild(currCrit, crits);
            if(c != null){
                currCrit = c;
                System.out.println("Parent found!");
            }

            checkForChildren(currElem, currCrit);
            checkForWeights(currElem, currCrit);
            checkForMatrix(currElem, currCrit);

            if(currCrit.hasParent()) {
                System.out.println("parent: " + currCrit.parent.name);
            }
            //System.out.println(currCrit.name + "!");
            crits.add(currCrit);

            System.out.println("----------------------------");

        }
    }


    public void checkForChildren(Element currElem, Criterion currCrit){
        if(currElem.hasChildNodes()) {
            NodeList currElemChildNodes = currElem.getChildNodes();
            for (int cur = 0; cur < currElemChildNodes.getLength(); cur++) {
                Node tempNode = currElemChildNodes.item(cur);
                if(tempNode.hasAttributes()) {
                    System.out.print("Child (crit): ");
                    NamedNodeMap currMap = tempNode.getAttributes();
                    String serName = "";
                    for (int map = 0; map < currMap.getLength(); map++) {
                        if ((currMap.item(map).getNodeName()).equals("name")) {
                            serName = currMap.item(map).getNodeValue();
                        }
                    }
                    System.out.println(serName);
                    currCrit.addChild(new Criterion(serName));
                }
            }
        } else {
            System.out.println(currElem.getAttribute("name") + " - has no children :(");
        }
    }

    public void checkForWeights(Element currElem, Criterion currCrit){
        if(currElem.hasAttributes()) {
            NamedNodeMap weighMap = currElem.getAttributes();
            for(int map = 0; map < weighMap.getLength(); map++){
                Node tempNode = weighMap.item(map);
                if(tempNode.getNodeName().equals("weight")){
                    String stringMatrix = tempNode.getNodeValue();
                    currCrit.weights = getJamaMatrix("weights", stringMatrix);
                    currCrit.vectorWeigh = currCrit.setWeighVector();
                }
            }
        } else {
            System.out.println("I have no attributes...");
        }
    }

    public void checkForMatrix(Element currElem, Criterion currCrit){
        if(currElem.getElementsByTagName("matrix").getLength() == 1){
            String stringMatrix = currElem.getElementsByTagName("matrix").item(0).getTextContent();
            currCrit.matrix = getJamaMatrix("crit", stringMatrix);
            currCrit.vectorCrit = currCrit.setCritVector();
        }
    }


    public Matrix getJamaMatrix(String type, String stringMatrix){
        String[] matrixElems = stringMatrix.split(",|;");
        double matrixSize = Math.sqrt(matrixElems.length);
        Matrix jamaMatrix = new Matrix((int)matrixSize, (int)matrixSize);

        if(matrixSize % 1 == 0){
            int row = 0;
            int col = 0;
            System.out.println(type + " matrix: ");
            for(int j = 0; j < matrixElems.length; j++){
                jamaMatrix.set(row, col, Double.valueOf(matrixElems[j]));
                if(col == 0){
                    System.out.print("|  ");
                }
                System.out.print(jamaMatrix.get(row,col) + "  ");
                col++;
                if(col == matrixSize){
                    System.out.println("|");
                    col = 0;
                    row++;
                }
            }
        }
        else{
            System.err.println("The matrix given is not square.");
            return null;
        }
        return jamaMatrix;
    }

    public Matrix evaluateValue(String s){

        Criterion c = new Criterion();
        boolean found = false;
        for(int i = 0; i < crits.size(); i++){
            if(crits.get(i).name.equals(s)){
                c = crits.get(i);
                found = true;
                break;
            }
        }
        if(!found){
            System.out.println("No such criterion! : " + s);
        }

        for (int i = 0; i < c.children.size(); i++){
            evaluateValue(c.children.get(i).name);
        }

        if(c.children.size() > 0){

            System.out.println("I have children, me: " + c.name);
            System.out.println(c.vectorCrit);

            Criterion tempChild = c.children.get(0);
            System.out.println(tempChild.name);

            c.vectorCrit = c.children.get(0).vectorCrit.times(c.vectorWeigh.get(0,0));
            c.vectorCrit.print(c.vectorCrit.getRowDimension(),5);
            for (int i = 1; i < c.children.size(); i++){
                Criterion temp = c.children.get(i);
                System.out.println(temp.name);
                Matrix toEvaluate = c.children.get(i).vectorCrit.times(c.vectorWeigh.get(i,0));
                c.vectorCrit = c.vectorCrit.plusEquals(toEvaluate);
                toEvaluate.print(toEvaluate.getRowDimension(),4);
                //c.vectorCrit.print(c.vectorCrit.getRowDimension(),6);
            }
            c.vectorCrit = c.vectorCrit.times(1/c.vectorCrit.norm1());
            System.out.println("--res " + c.name + ": ");
            c.vectorCrit.print(c.vectorCrit.getRowDimension(),4);
            return c.vectorCrit;
        }
        else {
            System.out.println("I have no children, me: " + c.name);
            return null;
        }
    }

    public Criterion isChild(Criterion curr, List<Criterion> list){
        for(int i = 0; i < list.size(); i++){
            Criterion c = list.get(i);
            for(int j = 0; j < c.children.size(); j++){
                Criterion childTemp = c.children.get(j);
                if(childTemp.name.equals(curr.name)){
                    curr = childTemp;
                    return curr;
                }
            }
        }
        return null;
    }


}
