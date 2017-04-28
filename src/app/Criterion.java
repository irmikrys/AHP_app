package app;

import Jama.Matrix;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 13.03.2017.
 */
public class Criterion {

    public String name = "";
    public Criterion parent;

    public List<Criterion> children = new ArrayList<>();

    public Matrix matrix;
    public Matrix weights;
    public Matrix vectorCrit;
    public Matrix vectorWeigh;

    public boolean modifiedMatrix;
    public boolean consistentMatrix;

    public Criterion(){

    }

    public Criterion(String n){
        this.name = n;
    }

    public Criterion(String n, Matrix weights, Matrix matrix, Criterion parent){
        this.name = n;
        this.weights = weights;
        this.matrix = matrix;
        this.parent = parent;
        this.modifiedMatrix = false;
    }

    public boolean hasCriterionChildren(){
        if(this.children.isEmpty()) {
            System.out.println("\t" + this.name + " - no criterion children...");
            return false;
        }
        return true;
    }

    public Criterion addChild(Criterion child){
        if(!children.contains(child)) {
            children.add(child);
            int ind = children.indexOf(child);
            Criterion c = children.get(ind);
            c.parent = this;
            //System.out.println("Child added: " + child.name + ", parent: " + child.parent.name);
            return child;
        }
        return null;
    }

    public Matrix setCritVector(){
        vectorCrit = AlgoHandler.vectorSumTo1(matrix, "criterions");
        return vectorCrit;
    }

    public Matrix setWeighVector(){
        vectorWeigh = AlgoHandler.vectorSumTo1(weights, "weights");
        return vectorWeigh;
    }

    public boolean hasParent() {

        return this.parent != null;
    }


}
