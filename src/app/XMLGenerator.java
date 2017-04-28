package app;

import Jama.Matrix;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created on 08.04.2017.
 */
public class XMLGenerator {

    public static boolean notFullData = false;

    private  DocumentBuilderFactory dbFactory;
    private  DocumentBuilder dBuilder;
    private  Document doc;

    private  Element rootElement;

    private  List<Alternative> alternatives = new ArrayList<>();
    private  List<Criterion> criterions = new ArrayList<>();
    private  List<Element> critElems = new ArrayList<>();

    public XMLGenerator(String path, List<Alternative> alts, List<Criterion> crits){
        alternatives = alts;
        criterions = crits;
        if(alternatives.size() < 2 || criterions.size() < 1) notFullData = true;
        generateFromSources(path);
    }

    private  void generateFromSources(String path) {
        try {
            dbFactory = DocumentBuilderFactory.newInstance();
            dBuilder = dbFactory.newDocumentBuilder();
            doc = dBuilder.newDocument();

            // root element
            rootElement = doc.createElement("root");
            doc.appendChild(rootElement);

            handleAltSet();
            handleCritSet();

            //zapis do pliku xml
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(path));
            transformer.transform(source, result);

            //output na konsoli do przetestowania co jest w pliczku
            StreamResult consoleResult = new StreamResult(System.out);
            transformer.transform(source, consoleResult);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  void handleAltSet(){
        // altset element
        Element altSet = doc.createElement("altSet");
        for(int i = 0; i < alternatives.size(); i++){
            Element alt = doc.createElement("alternative");
            setAlternative(alt, alternatives.get(i).id, alternatives.get(i).name);
            altSet.appendChild(alt);
        }
        rootElement.appendChild(altSet);
    }

    private  void handleCritSet(){
        // critSet element + prior
        Element critSet = doc.createElement("critSet");
        Element prior = doc.createElement("criterion");
        setCriterion(prior, criterions.get(0).name, criterions.get(0).weights, criterions.get(0).matrix);
        critSet.appendChild(prior);

        for(int i = 1; i < criterions.size(); i++){

            //boolean exists = false;
            Element tmpParent = null;

            //if(!exists) {
            Element crit = doc.createElement("criterion");
            setCriterion(crit, criterions.get(i).name, criterions.get(i).weights, criterions.get(i).matrix);

            for(Element e: critElems){
                String tmpName = null;
                NamedNodeMap nnm = e.getAttributes();
                for (int mp = 0; mp < nnm.getLength(); mp++) {
                    if ((nnm.item(mp).getNodeName()).equals("name")) {
                        tmpName = nnm.item(mp).getNodeValue();
                    }
                }

                if(tmpName.equalsIgnoreCase(criterions.get(i).parent.name)){
                    tmpParent = e;
                    break;
                }
            }
            critElems.add(crit);
            if(tmpParent == null) {prior.appendChild(crit);}
            else {tmpParent.appendChild(crit);}

            //}

        }
        rootElement.appendChild(critSet);
    }

    private  void setAlternative(Element alt, Integer id, String name){
        alt.setAttribute("id", id.toString());
        alt.setTextContent(name);
    }

    private  void setCriterion(Element crit, String name, Matrix weights, Matrix matrix){
        crit.setAttribute("name", name);
        if(weights != null){
            double[][] arr = weights.getArrayCopy();
            String weightsStr = "";
            for(int i = 0; i < weights.getRowDimension(); i++) {
                for(int j = 0; j < weights.getColumnDimension(); j++) {
                    weightsStr += (arr[i][j] + ";");
                }
            }
            crit.setAttribute("weight", weightsStr);
        }
        else if(matrix != null){
            double[][] arr = matrix.getArrayCopy();
            String matrixStr = "";
            for(int i = 0; i < matrix.getRowDimension(); i++) {
                for(int j = 0; j < matrix.getColumnDimension(); j++) {
                    matrixStr += (arr[i][j]);
                    if(j == matrix.getColumnDimension() - 1 && i != matrix.getRowDimension() - 1){
                        matrixStr += ";\n\t\t\t\t\t\t\t";
                    } else {
                        matrixStr += ",";
                    }
                }
            }
            Element mat = doc.createElement("matrix");
            mat.setTextContent(matrixStr);
            crit.appendChild(mat);
        }
        else{
            notFullData = true;
            System.out.println("Both matrices are empty...-" + name);
        }
    }

    private  void genArtificialAlts(){
        Alternative alt1 = new Alternative(1, "Barcelona");
        Alternative alt2 = new Alternative(2, "Rome");
        Alternative alt3 = new Alternative(3, "Reykjavik");
        alternatives.add(alt1); alternatives.add(alt2); alternatives.add(alt3);
    }

    private  void genArtificialCrits(){
        Matrix wTemp = new Matrix(3,3,1);
        Matrix mTemp = new Matrix(3,3,0);
        Criterion prior = new Criterion("prior", wTemp, null, null);
        Criterion crit1 = new Criterion("climate", wTemp, null, prior);
        Criterion subcrit1 = new Criterion("temperature", null, mTemp, crit1);
        Criterion subcrit2 = new Criterion("wind", null, mTemp, crit1);
        Criterion subcrit3 = new Criterion("rainfalls", null, mTemp, crit1);
        Criterion crit2 = new Criterion("sightseeing", null, mTemp, prior);
        Criterion crit3 = new Criterion("environment", null, mTemp, prior);
        criterions.add(prior); criterions.add(crit1);
        criterions.add(subcrit1); criterions.add(subcrit2); criterions.add(subcrit3);
        criterions.add(crit2); criterions.add(crit3);
    }

}
