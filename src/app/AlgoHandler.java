package app;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Created on 30.03.2017.
 */
public class AlgoHandler {

    public static double maxCI;
    public static double currCI;

    public static double maxEigenValue(Matrix m){
        if(m != null){
            EigenvalueDecomposition eigDec = m.eig();
            double eigVal = -1;
            double[] eigVals = eigDec.getRealEigenvalues();
            for(int i = 0; i < eigVals.length; i++){
                if(eigVals[i] > eigVal){
                    eigVal = eigVals[i];
                }
            }
            return eigVal;
        }
        System.out.println("No plus real eigvals...");
        return -1;
    }

    public static Matrix vectorSumTo1(Matrix m, String s){
        double maxEigVal = AlgoHandler.maxEigenValue(m);
        System.out.println("Max eig value: " + maxEigVal);
        Matrix v = new Matrix(m.getRowDimension(), 1);

        EigenvalueDecomposition eigDec = m.eig();
        double max = AlgoHandler.maxEigenValue(m);
        Matrix tempMatrix = eigDec.getD();
        for (int i = 0; i < tempMatrix.getColumnDimension(); i++) {
            if (tempMatrix.get(i, i) == max) {
                Matrix valueMat = eigDec.getV().getMatrix(0, tempMatrix.getRowDimension() - 1, i, i);
                v = valueMat.times(1 / valueMat.norm1());
                if(v.get(0,0)<0)
                    v = v.times(-1);
            }
        }

        System.out.print("Vector from " + s + ": ");
        v.print(m.getRowDimension(),4);
        return v;
    }

    /*public static boolean checkMatrixRange(Matrix matrix){
        int size = matrix.getRowDimension();
        boolean good = true;
        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                if(j > i) {
                    System.out.println("Number: " + matrix.get(i, j));
                    if (matrix.get(i, j) < 0.12) {
                        System.out.println("Number: " + matrix.get(i, j) + " smaller than 0.12");
                        matrix.set(i,j,1.0);
                        good = false;
                    }
                    if (matrix.get(i, j) > 9.0) {
                        System.out.println("Number: " + matrix.get(i, j) + " bigger than 9.0");
                        matrix.set(i,j,1.0);
                        good = false;
                    }
                }
            }
        }
        if(!good){
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Warning - matrix elements range");
            alert.setHeaderText("Warning");
            alert.setContentText("There was at least one number out of range 0.12 to 9.0. All out-of-range numbers were" +
                    " changed to default. You will have to edit these elements again.");
            alert.getDialogPane().getStylesheets().add(Main.currentCss);
            alert.showAndWait();
        }
        return good;
    }*/

    public static void reciprocMatrix(Matrix matrix){
        int size = matrix.getRowDimension();
        for(int i = 0; i < size; i++){
            for(int j = 0; j <= i; j++){
                matrix.set(i,j,1/(matrix.get(j,i)));
            }
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        double val = Math.round(value * 100);
        double correctVal = val/100;

        return correctVal;
    }

    public static double countCurrCI(Matrix m, int size){
        double CI = 0;
        double CR = 0;
        double RIn = 0;

        switch(size) {
            case  3: RIn = 0.5247; break;
            case  4: RIn = 0.8816; break;
            case  5: RIn = 1.1086; break;
            case  6: RIn = 1.2479; break;
            case  7: RIn = 1.3417; break;
            case  8: RIn = 1.4057; break;
            case  9: RIn = 1.4499; break;
            case 10: RIn = 1.4854; break;
            default: RIn = generateRIn(size); break;
        }

        CI = (maxEigenValue(m) - size)/(size - 1);
        CR = CI / RIn;
        if(size == 0 || size == 1 || size == 2){
            CR = 0.0;
        }
        currCI = CR;

        return CR;
    }

    public static boolean checkIfConsistent(Matrix m, int size){
        AlgoHandler.currCI = countCurrCI(m, size);
        return AlgoHandler.currCI <= AlgoHandler.maxCI;
    }

    private static double generateRIn(int size) {

        Matrix tmp = new Matrix(size, size);
        double tmpCI = 0;
        int counter = 5000;
        double RI = 0;
        double low = 1/9;
        double high = 9;

        for(int k = 0; k < counter; k++) {
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    if (i == j) {
                        tmp.set(i, j, 1);
                    }
                }
            }
            for (int i = 0; i < size - 1; i++) {
                for (int j = i + 1; j < size; j++) {
                    double random = ThreadLocalRandom.current().nextDouble(low, high);
                    tmp.set(i, j, random);
                    tmp.set(j, i, 1 / random);
                }
            }
            tmpCI += (maxEigenValue(tmp) - size)/(size - 1);
        }

        RI = tmpCI/counter;

        /*
        //test wypelnienia
        double[][] matrix = tmp.getArray();
        for(int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                Double round = (double) Math.round(matrix[i][j] * 100) / 100;
                System.out.print(round + "   ");
                if (round.toString().length() == 3) {
                    System.out.print(" ");
                }
                if (j == size - 1) {
                    System.out.println();
                }
            }
        }*/

        return RI;
    }
}
