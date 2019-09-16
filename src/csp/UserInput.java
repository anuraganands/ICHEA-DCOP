/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.JOptionPane;

/**
 *
 * @author Anurag Sharma
 */
public class UserInput implements Cloneable,Serializable {

    public int totalObjectives;
    public int totalDecisionVars;
    public ArrayList<Double> minVals;
    public ArrayList<Double> maxVals;
    public int totalConstraints;
    String dataType;
    //public boolean fileData; 
    public int population;
    public int generation;
    public boolean doMutation;
    public boolean saveChromes;
    public ArrayList<ArrayList<Double>> domainVals;//applicable for ordinal data only. Domain values for each dimension
    public int solutionBy;
    public boolean bWeighted;
    public boolean bHasConstraintPreferences;
    public int gxFn=-1;
    public int maxDynamicTime;
    //public boolean dataIsDiscrete;
   
    private UserInput(){
        ;
    }
    /**
     * The only constructor
     */
    public UserInput(Class t, boolean saveChromes) throws InstantiationException, IllegalAccessException{
        this.totalObjectives = -1;
        this.totalDecisionVars = -1;
        this.minVals = new ArrayList();
        this.maxVals = new ArrayList();
        this.totalConstraints = -1;
        this.population = 0;
        this.generation = 0;
        solutionBy = -1;
        //this.externalData_ = null;
        dataType = t.getName();
        //fileData = false;
        doMutation = true;
        domainVals = null; // only needed in special cases.
        this.saveChromes = saveChromes;
        
        bWeighted = false;
        bHasConstraintPreferences = false;
    }

    @Override public String toString() {
        String msg;
        msg = "totalObjectives: " + String.valueOf(totalObjectives);
        msg = msg + "\nTotal Decision Vars: " + String.valueOf(totalDecisionVars);
        msg = msg + "\nTotal Constraints: " + String.valueOf(totalConstraints);
        msg = msg + "\nMin Vals: " + minVals.toString();
        msg = msg + "\nMax Vals: " + maxVals.toString();
        msg = msg + "\nData Type: " + dataType;
        //msg = msg + "\nFile Data: " + fileData;

        return msg;
    }

    public void validateData() throws MyException{
        if(totalDecisionVars != minVals.size() || totalDecisionVars != maxVals.size()){
            throw new MyException("Incorrect Total Decision Vars value", "Incorrect Data Combinations",JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * lone defined for UserInput is ONLY SHALLOW CLONE.
     * @return Object.clone();
     * @throws CloneNotSupportedException
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }


}
