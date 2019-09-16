///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package user_code;
//
//import csp.Chromosome;
//import csp.ExternalData;
//import csp.MyException;
//import csp.MyMath;
//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.Scanner;
//import java.util.Set;
//import java.util.StringTokenizer;
//
///**
// *
// * @author Anurag Sharma
// */
//public class MapColoring extends ExternalData{
//    private ArrayList<ArrayList<Integer>> states_;
//    
//    public MapColoring(Scanner dataFile, int populaion, int generation, boolean saveChromes, int solutionBy, Class t) throws InstantiationException, IllegalAccessException, MyException {
//        super(dataFile, populaion, generation, saveChromes, solutionBy, t);
//        this.states_ = new ArrayList<ArrayList<Integer>>();
//        this.readData();
//    }
//
//    @Override
//    protected void readData() throws MyException {
//        final int TOTAL_STATES;
//        final int TOTAL_COLORS;
//        ArrayList<Integer> arrayList;
//        
//        while (dataFile_.hasNext("#")){                
//            System.out.println("got #####");
//            dataFile_.nextLine(); //ignore comments  
//        }
//
//        TOTAL_STATES = dataFile_.nextInt();
//        dataFile_.nextLine(); //ignore comments
//           
//        TOTAL_COLORS = dataFile_.nextInt();
//        dataFile_.nextLine(); //ignore
//        
//        for (int i = 0; i < TOTAL_STATES; i++) {
//            arrayList = new ArrayList<Integer>();
//            states_.add(arrayList);  
//        }
//        
//        StringTokenizer str;
//        Integer neighborState;
//        int nextState = 0;
//        int sz;
//        String tempStr;
//        
//        while(dataFile_.hasNext()){    
//            tempStr = dataFile_.nextLine();
//            str = new StringTokenizer(tempStr," ");
//            sz = str.countTokens();
//            for (int i = 0; i < sz; i++){
//                
//                tempStr = str.nextElement().toString();
//                
//                if(tempStr.toLowerCase().compareTo("null") == 0){
//                    ;
//                }else{
//                    neighborState = Integer.valueOf(tempStr);
//                    states_.get(neighborState).add(nextState);
//                }
//                
//                
//            } 
//            if(sz>0)
//                nextState++;                
//        }
//
//        userInput_.fileData = true;
//        userInput_.totalConstraints = TOTAL_STATES;
//        userInput_.totalDecisionVars = 1;
//        userInput_.totalObjectives = 1;
//        for (int i = 0; i < userInput_.totalDecisionVars; i++) {
//            userInput_.minVals.add(0.0);
//            userInput_.maxVals.add(TOTAL_STATES-1.0);            
//        }        
//        userInput_.validateData();
//        userInput_.population = TOTAL_STATES;
//
//        userInput_.domainVals = new  ArrayList<ArrayList<Double>>();
//        for (int i = 0; i < userInput_.totalConstraints; i++) {
//            userInput_.domainVals.add(new ArrayList<Double>());
//            for (int j = 0; j < TOTAL_COLORS; j++) {
//                userInput_.domainVals.get(i).add(i + j/10.0);
//            }
//        }
//
//        userInput_.doMutation = true;
//    }
//
////    @Override
////    public ArrayList<Double> negateVal(ArrayList<Double> vals) {
////        throw new UnsupportedOperationException("Not supported yet.");
////    }
//
//    
//    
//    
//    @Override
//    protected int isViolated(Object obj1, Object obj2, Object... additionalInfo) {
//        int violated; // 1 = > true; 0 => false
//
//        if (obj1 instanceof Double && obj2 instanceof Double){
//            ;
//        }else{
//            throw new ClassCastException("Expecting Double");
//        }                
//        
//        Double stateColor1 = (Double)obj1;
//        Double stateColor2 = (Double)obj2;
//        
//        if(stateColor1.intValue() == stateColor2.intValue()){
//            violated = 1; //note..............
//            return violated;
//        }
//
//        if(states_.get(stateColor1.intValue()).isEmpty() || states_.get(stateColor2.intValue()).isEmpty()){
//            violated = 0;
//            return violated;
//        }
//        
//        if(MyMath.roundN(stateColor1 - stateColor1.intValue(),2) == 
//                MyMath.roundN(stateColor2 - stateColor2.intValue(),2)){            
//            if(states_.get(stateColor1.intValue()).contains(stateColor2.intValue()))
//                violated = 1;
//            else
//                violated = 0;
//        }else{
//            violated = 0;    
//        }
//        
//        return violated;
//    }
//
//    @Override
//    protected double degreeOfViolation(Object obj1, Object obj2) {
//        throw new UnsupportedOperationException("Not supported yet.");
//    }
//
//    @Override
//    protected ArrayList<Chromosome> initializeExternalChrmosomes(final int population) {
//        if(userInput_ == null)
//            throw new UnsupportedOperationException("User input not initialized");
//
//        Chromosome chrome;
//                
//        for (Integer i = 0; i < userInput_.totalConstraints; i++) {            
//            for (int j = 0; j < userInput_.domainVals.get(i).size(); j++) {
//                chrome = new Chromosome(userInput_.solutionBy, this);
//                //chrome.appendVal(i.doubleValue() + j/10.0);
//                chrome.appendVal(userInput_.domainVals.get(i).get(j));
//                chromosome_.add(chrome);
//            }            
//        }
//
//        userInput_.population = chromosome_.size();
//        return chromosome_;
//    }
//
//    @Override
//    protected int getConstraintID(Double val) {
//        return val.intValue();
//    }
//
//
//
////    @Override
////    protected Set<Double> domainValues(int dimension) {
////        Set<Double> s = new LinkedHashSet<Double>();
////        for (int j = 0; j < userInput_.totalDecisionVars; j++) {
////            s.add(j/10.0);
////        }
////        return s;
////    }
//}
