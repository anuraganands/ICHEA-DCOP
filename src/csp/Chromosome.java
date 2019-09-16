/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jdesktop.application.Application;



/**
 *
 * @author Anurag Sharma
 */
public class Chromosome implements Comparable, Cloneable, Serializable{
    public static final int BY_FITNESS = 1;
    public static final int BY_VIOLATIONS = 2;
    public static final int BY_SATISFACTIONS = 3;
    public static final int BY_RO = 4;
    public static final int BY_IMMUNITY = 5;
    public static final int BY_DISCOURAGE = 6;
    public static final int BY_AGE = 7;
        
    public static ArrayList<Integer> discourageVios;
    private ArrayList<Double> vals_;
    private ArrayList<Integer> position_;
    private Idx2D[] valVsConstIdx_;//constraint functions
    private ArrayList<Double> fitness_; //change back to private
    private ArrayList<Integer> violations_;
    private ArrayList<ArrayList> satisfactions_;
    private UserInput userInput_;
    private ExternalData externalData_;
    private int noProgressCounter;
    private int immunity;
    public ArrayList<Double> noGood; ///temporaly made it public for testing purpose.
    
//    public int fitnessRank;
    public Double tempRo;
    public static int tmpSortBy;
    public static int totalEvals=0;
    public boolean rmvIndicator;
    public int age;


    public ArrayList<Integer> Xdebug_getViolations() {
        return violations_;
    }
    
    
    
    /**
     * search near by...
     * @param influencer
     * @param degree_of_influence
     * @param isBlindFollower
     * @throws Exception 
     */
       
     
    public boolean isStagnant(final int noProgressLimit) {
        boolean isStagnant;
        if(noProgressCounter>noProgressLimit)
            isStagnant = true;
        else
            isStagnant = false;
        
        return isStagnant;
    }

    //private int rankingType;

    private Chromosome(){
        vals_ = new ArrayList<Double>();
        position_ = new ArrayList<Integer>();
        fitness_ = new ArrayList<Double>();
        violations_ = new ArrayList<Integer>();
        satisfactions_ = new ArrayList<ArrayList>();
        tempRo = -1.0; //Invalid negative value
        tmpSortBy = BY_VIOLATIONS; //default sort option
        noProgressCounter = 0;
        immunity = 0;
        //rankingType = BY_VIOLATIONS;
        noGood = new ArrayList<Double>();
        rmvIndicator = false;
        age = 0;
    }

    private Chromosome(UserInput userInput){
        this();
        userInput_ = userInput;
        externalData_ = null;
        if(userInput_ == null){
            System.err.println("No user input provided.");
            Application.getInstance().exit();
        }

        valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            valVsConstIdx_[i] = new Idx2D(); //col = -1; position = -1;
        }
//        fitnessRank = Integer.MAX_VALUE;
    }
    public Chromosome(int sortValue, UserInput userInput){
        this(userInput);
        tmpSortBy = sortValue; //default sort option
    }

    private Chromosome(ExternalData externalData){
        this();
        externalData_ = externalData;
        userInput_ = externalData_.getUserInput();

        if(userInput_ == null || this.externalData_ == null){
            System.err.println("No user input provided or empty external data.");
            Application.getInstance().exit();
        }
        
        valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            valVsConstIdx_[i] = new Idx2D(); //col = -1; position = -1;
        }
    }
    
    public Chromosome(int sortValue, ExternalData externalData){
        this(externalData);
        tmpSortBy = sortValue; //default sort option
    }

    public ExternalData getExternalData() {
        return externalData_;
    }

    public void setExternalData(ExternalData externalData) {
        this.externalData_ = externalData;
    }

     /**
     * <b>Note:</b> this function is totally different from immunity used in
     * Chromosome. It refers to the immunity size of ExternalData file.
     * @return immunitySize or satisfactionSize of the best individual of the
     * last partial solution.
     */
    public boolean hasPartialSolImmunity(Chromosome bestChrom){
        
        return (vals_.size() > bestChrom.getValsCopy().size());
    }
    
    public int getParitalSolImmunity(){
        return vals_.size();
    }
    
    public ArrayList<Double> getNegVals(){
        ArrayList<Double> negVals = new ArrayList<Double>();  
        double val;
        for (int i = 0; i < vals_.size(); i++) {
            val = userInput_.minVals.get(i)+userInput_.maxVals.get(i) - vals_.get(i);
            if(val<userInput_.minVals.get(i))
                val = userInput_.minVals.get(i);
            if(val>userInput_.maxVals.get(i))
                val = userInput_.maxVals.get(i);
            
            negVals.add(val);
        }
        return negVals;
    }
    /**
     * Assumed - byfitness goes with violations...
     * Works for same era where {@link CspProcess#dynamicTime} is same
     * @param c
     * @return 
     */
    public boolean isMorePromisingThan(Chromosome c){
        if(tmpSortBy == BY_VIOLATIONS || tmpSortBy == BY_FITNESS){
            if(this.violations_.size() < c.violations_.size()){
                return true;
            }else if (this.violations_.size() == c.violations_.size()){
                if(this.getRank()<c.getRank())
                    return true;
                else
                    return false;                                     
            }else{ //>
                return false;                
            }             
        }else{
            throw new UnsupportedOperationException("Not yet implemented.");
        }
        
////       if(this.getRank()<c.getRank() && 
////               this.violations_.size()<=c.violations_.size() &&
////               this.satisfactions_.size()>=c.satisfactions_.size()){
////           return true;
////       }else
////           return false;
    }
    
    
    /**
     * The integer value denoting the rank of an individual in a population.
     * the lower the rank the better the value. It has range from [0 Total_Constraints-1]
     * Gives total violations or total satisfaction in terms of rank<br>
     * for violation <br>
     * rank = size of violation<br>
     * for satisfaction <br>
     * rank = total constraint - size of satisfaction <br>
     * Other types of rankings are NOT implemented
     * @return returns rank - values from good to worse. lower value indicate better chromosome.
     * @throws UnsupportedOperationException
     */
    public double getRank() throws UnsupportedOperationException{
        double rank;
        int sz = 0;
        
        if(tmpSortBy == BY_VIOLATIONS){ // || (tempSortBy == BY_FITNESS && externalData_ == null)){
            rank = violations_.size(); //the higher the violation the lower the rank (ascending order from 0...maxval)
        }else if(tmpSortBy == BY_SATISFACTIONS){
            for (ArrayList s : satisfactions_) {
                sz +=s.size();
            }            
            
            rank = userInput_.totalConstraints - sz;
        }else if(tmpSortBy == BY_FITNESS){// & externalData_ != null){
            if(fitness_.isEmpty())
                rank = Integer.MAX_VALUE;
            else
                rank =  getFitnessVal(0); // ????????????????? int val????
        }else{
            rank = -1;
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }
        
        
//        if(fitness_.isEmpty())
            return rank;
//        else
//            return getFitnessVal(0).intValue(); ////// WHY INT val?????????????????????????????????????????????//    
    }

    public Double getFitnessVal(int i) {
        Double d;
        try{
            d = new Double(fitness_.get(i));
        }catch(IndexOutOfBoundsException iobe){
            d = Double.NaN;
        }
        return d; 
    }

    
    public ArrayList<ArrayList> getSatisfaction(){
        return satisfactions_;
    }
        /**
     * Depending on the type of rank used in calculation it return the numeric
     * values of the rank components
     * <BR>
     * In case of violation - it return all violated constraint set
     * <BR>
     * In case of satisfaction - it return all satisfied constraint set
     * @return 
     */
    public ArrayList<Integer> getRankComponents(){
        ArrayList<Integer> rankComponents;

        if(userInput_.solutionBy == BY_VIOLATIONS || (userInput_.solutionBy == BY_FITNESS && externalData_ == null)){
            rankComponents = violations_; //the higher the violation the lower the rank (ascending order from 0...maxval)
        }else if(userInput_.solutionBy == BY_SATISFACTIONS){
            throw new UnsupportedOperationException("Not supported for satisfaction. use getSatisfaction() instead.");
        }else{
            rankComponents = null;
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }
        return new ArrayList<Integer>(rankComponents);
    }
    
    public void tempprintValVsConstIdx(){
        String print = "";
        String sp = " ";
        print += "\n"+sp+sp+"valsVsConstraintIdx:";
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            if(!valVsConstIdx_[i].isEmpty())
                print+= "(" + i + ")"+ valVsConstIdx_[i].toString()+"; ";            
        }
        System.out.println(print);
    }

    public void refreshValVsConstIdx() {
        Idx2D idx2D;
        int col;
        int pos;
        int i;
         
        valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];

        col = -1;
        i = 0;
        for (ArrayList<Integer> list : satisfactions_) {
            col++;
            pos = -1;
            for (Integer val : list) {
                pos++;
                idx2D = new Idx2D();
                idx2D.col = col;
                idx2D.position = pos;
                valVsConstIdx_[val] = idx2D;
                i++;
            }
        }
    }
    
    
    /**
     * Checks if the child is acceptable in inter-marriage crossover.
     * It is possible that child may not contain any trait of a parent. In this
     * case it is necessary to check for genuineness with this function.
     * If constraint violation is to be checked then <BR>
     * child must not violate any other constraint than its parent's already 
     * violated constraint. <BR>
     * If constraint satisfaction is to be checked then <BR>
     * child must satisfy same or more constraints.
     * @param child
     * @return 
     */
    public boolean isMyChild(Chromosome child){
        boolean result;
        
        if(tmpSortBy == BY_VIOLATIONS || tmpSortBy == BY_FITNESS){ //child must not violate any other constraint than its parent's already violated constraint.
            result = this.violations_.containsAll(child.violations_);
//            if(result){
//                for (int i = 0; i < position_.size(); i++) {
//                    if(position_.get(i) != child.position_.get(i)){
//                        result = false;
//                        break;
//                    }
//                }                    
//            }
        }else if(tmpSortBy == BY_SATISFACTIONS){ //child must satisfy same or more constraints.
            result = child.satisfactions_.containsAll(this.satisfactions_);
        }else{
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }   
        
        return result;
    }
    
    
    /**
     * The current (this) object is supposed to be a child
     * @param parent
     * @return 
     */
    public boolean myParent(Chromosome parent){
        boolean result;
        int vios[] = new int[userInput_.totalConstraints];
        ArrayList<Integer> parentSatisfactions = new ArrayList<Integer>();
        
        
        if(this.violations_.containsAll(parent.violations_) && 
                parent.violations_.containsAll(this.violations_)) ///same violations
            return true;
        
        for (int i = 0; i < parent.violations_.size(); i++) {
            vios[parent.violations_.get(i)] = 1;
        }
        
        for (int i = 0; i < vios.length; i++) {
            if(vios[i]==0){
                parentSatisfactions.add(i);
            }            
        }
        
        result = true;
        if(this.violations_.containsAll(parentSatisfactions)){
            result = false;
        }
        
        return result;
    } 
    
    public void refreshFitness() throws SolutionFoundException{
        refreshObjectiveFunction();
    }
    
    /**
     * It remove duplicate values - It is problem dependant.
     */
//    private void repairVal(){
//        Set<Double> s = new LinkedHashSet<Double>(this.vals_);
//        this.vals_ = new ArrayList<Double>(s);      
//    }
    
    /**
     * This is tricky one if we deal with both numeric and ordinal data<BR>
     * - for numeric data vals are features/dimensions where the total dimension 
     * remain constant. as in my circle example. val size will remain 2.<BR>
     * - for ordinal data vals are just constraint representation. it can keep
     * on growing for any size (in some problems up to max satisfaction)
     * @param vals - 
     * <BR> - for numeric data - val is dimension/feature value.
     * <BR> - for ordinal data - val is constraint representation.
     */
    public void appendVal(final double vals) throws SolutionFoundException{
        this.vals_.add(new Double(vals));
        int lastIdx = this.vals_.size() - 1;
         
        if(this.userInput_.dataType.contains("Double")){
            if(this.vals_.size() == this.userInput_.totalDecisionVars)
                resetObjectiveFunction();
            
        }else{
            if(vals_.size()>=1){
//                for (int i = 0; i < lastIdx; i++) {
//                    if(violationChk(i,lastIdx)>0){//violated
//                        this.vals_.remove(lastIdx);
//                        break;
//                    }
//                }
//                refreshRank();
                
                appendObjectiveFunction(); 
            }
        }
    }

    /**
     * Updates the vals with the new vals_ from argument
     * @param vals_ to be assigned to chromosome
     */
    public void setVals(ArrayList<Double> vals_) throws SolutionFoundException{
        this.vals_ = vals_;
        this.satisfactions_ = new ArrayList<ArrayList>();
        this.violations_ = new ArrayList<Integer>();
        this.fitness_ = new ArrayList<Double>();
        this.valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
        
        for (int i = 0; i < valVsConstIdx_.length; i++) {
            valVsConstIdx_[i] = new Idx2D();
        }

        if (this.vals_ == null){
            this.vals_ = new ArrayList<Double>();
            return;
        }
        if(vals_.size()>=1){
            resetObjectiveFunction();
        }
    }
    
    public void setVal(final int idx, final double val) throws SolutionFoundException, Exception{ 
        if(userInput_.dataType.contains("Integer")){
            remove(idx);            
            appendVal(val);            
        }else{
            this.vals_.set(idx, val); //can be removed anywhere
            resetObjectiveFunction();
        }
    }

    public void replaceVal(int idx, Double val) throws SolutionFoundException, Exception{ 
        if(userInput_.dataType.contains("Integer")){
            remove(idx);            
            appendVal(val);
            
        }else{
            this.vals_.set(idx, val); //can be removed anywhere
            resetObjectiveFunction();
        }
    }
    
    public void remove(final int idx) throws Exception{
        if(externalData_ != null)
            externalData_.ObjectiveFnRemove(vals_, fitness_, satisfactions_, valVsConstIdx_, idx);  
        else
            throw new UnsupportedOperationException("Not supported!\n");
    }

    /**
     * Because of the nature of satisfaction structure (2 dimensional), simple
     * crossover is not applicable. Because we need a diverse solution and 
     * diverse solution in this case relies on diverse satisfaction array rather
     * than val array which is just constraint satisfaction holder without
     * any patter. We must keep creating diverse pattern.
     * @param percent 
     */
    public void restructure(double percent, boolean fromLeft) throws SolutionFoundException{
        if(tmpSortBy == BY_SATISFACTIONS){
        
            ArrayList<Integer> ind = new ArrayList<Integer>();
            int temp = -1;
            for (ArrayList<Integer> grp : satisfactions_) {
                temp++;
                if(grp.size()>0){
                    ind.add(temp);
                }
            }

            this.noGood.clear();//are you sure????
            if(fromLeft){
                for (int i = 0; i < Math.floor(ind.size()*percent); i++) {
                    satisfactions_.set(ind.get(i), new ArrayList<Integer>());
                }
            }else{
                for (int i = ind.size()-1; i >= Math.ceil(ind.size()*percent); i--) {
                    satisfactions_.set(ind.get(i), new ArrayList<Integer>());
                }
            }

    //        for (int grp : MyRandom.randperm(0,satisfactions_.size()-1).subList(0, (int)Math.ceil(satisfactions_.size()*percent))) {
    //            satisfactions_.set(grp, new ArrayList<Integer>());            
    //        }

            vals_.clear();                                
            valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
            for (int i = 0; i < valVsConstIdx_.length; i++) {
                valVsConstIdx_[i] = new Idx2D(); //col = -1; position = -1;
            }

            Idx2D idx2D;
            int col = -1;
            int position;

            for (ArrayList<Integer> grp : satisfactions_) {
                col++;
                position = -1;
                for (Integer i : grp) {
                    position++;
                    vals_.add(i*1.0);
                    idx2D = new Idx2D();
                    idx2D.col = col;
                    idx2D.position = position;
                    valVsConstIdx_[i] = idx2D; 
                }
            }
        }else if(tmpSortBy == BY_FITNESS || tmpSortBy == BY_VIOLATIONS){
            valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
            for (int i = 0; i < valVsConstIdx_.length; i++) {
                valVsConstIdx_[i] = new Idx2D(); //col = -1; position = -1;
            }
        }
        
        refreshFitness();
        
    }
    
    /**
     * returns the value specified by the index
     * @param index the index of the decision value
     * @return the value of specified index of the decision value array
     */
    public Double getVals(int index){
        return new Double(this.vals_.get(index));
    }

    /**
     * returns the value array;
     * @return the value array
     */
    public ArrayList<Double> getValsCopy() {                
        return new ArrayList<Double>(this.vals_); //to protect vals_ ??? i don't think so...
    }

    
    public boolean isFeasible(){        
        if(this.isSolution()){
            return true;
        }
        if(satisfactions_.isEmpty() && violations_.isEmpty()){//is solution (checked above) or no solution
            return false;
        }
        
        if(violations_.size() > 0 && violations_.size() < userInput_.totalConstraints/2){
            return true;
        }
        
        if(satisfactions_.size() > 0 && satisfactions_.size() < userInput_.totalConstraints){
            return true;
        }
       
        return false;
    }
    
    /**
     * Checks to see if there is any violation
     * if rank is 0, it has no violation.
     * @return true or false for violation
     */
    public boolean isSolution(){
        boolean bsol = false;        
                
        
        if(getRank() == 0)//if byFitness then useless...
            bsol = true;

        //external data
        if(vals_.size() == userInput_.totalConstraints && externalData_ != null)
            bsol = true;
               
        if(violations_.size() == 0 && externalData_ == null)
            bsol = true;
        
        if(bsol){
            if(CspProcess.bInTransition){
                //System.out.println("\n... current transition sol reached...\n...\n...\n...\n");
                bsol = false;
            }
        }
        
        return bsol;            
    }

    /**
     * Checks to see if the input chromosome has the same violation as the
     * current object.
     * @param from the chomosome with whom comparision is to be made.
     * @return true or false for same violation
     */
    public boolean hasSameRankComponent(Chromosome from){
        boolean bsame =false;        
        if(tmpSortBy == BY_VIOLATIONS || externalData_ == null){
            if(this.violations_.size() == this.userInput_.totalConstraints)
                bsame = false;
            else            
                bsame = this.violations_.containsAll(from.violations_) || from.violations_.containsAll(this.violations_); //the higher the violation the lower the rank (ascending order from 0...maxval)
        }else if(tmpSortBy == BY_SATISFACTIONS){
            if(this.satisfactions_.isEmpty() || from.satisfactions_.isEmpty())
                bsame = false;
            else
                bsame = this.satisfactions_.containsAll(from.satisfactions_) || from.satisfactions_.containsAll(this.satisfactions_);
//        }else if(tempSortBy == BY_FITNESS){
//            //fitness based on satisfaction or violation?
//            if(!this.satisfactions_.isEmpty()){ //using satisfaction
//                if(this.satisfactions_.isEmpty() || from.satisfactions_.isEmpty())
//                    bsame = false;
//                else
//                    bsame = this.satisfactions_.containsAll(from.satisfactions_) || from.satisfactions_.containsAll(this.satisfactions_);
//            }else if(!this.violations_.isEmpty()){//using violation
//                if(this.isSolution())//this.violations_.size() == this.userInput_.totalConstraints)
//                    bsame = true;
//                else            
//                    bsame = this.violations_.containsAll(from.violations_) || from.violations_.containsAll(this.violations_); //the higher the violation the lower the rank (ascending order from 0...maxval)
//            }else if (this.isSolution() || from.isSolution()){
//                bsame = true;
//                //throw new UnsupportedOperationException("Not able to determine what ranking type is used by fitness.");
//                //assumption is violation is empty hence isSolution
//            }
        }else{
            throw new UnsupportedOperationException("Incorrect Ranking Type Defined.");
        }
        return bsame;
    }

    public ArrayList<Integer> getPositionRelConst() {
        return position_;
    }
  
    public void tryForcedCSPsolUpdate(){
        if(externalData_ != null){
            externalData_.tryForcedCSPsolUpdate(vals_, fitness_, satisfactions_, noGood, valVsConstIdx_,false); 
        }
        else
            ;    
    }
    
    public boolean forceFindSolution() throws SolutionFoundException{
        
        if(externalData_ != null){
            if(externalData_.getForcedCSPsol(satisfactions_, false)){
                refreshFitness();
                //refreshValVsConstIdx();
                return true;
            }else
                return false;
        
        }else
            return false;
    }  
    
   
    private void refreshObjectiveFunction() throws SolutionFoundException{
    //DO NOT DELETE THESE LINES
    //<<
        //totalEvals++;
        double prevRank;
        double curRank;
        prevRank = this.getRank();
    //>>

    //TODO... Call your objective function here....
    //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        //objFunction_NQueenII();
    try{
        if(externalData_ != null)
            externalData_.ObjectiveFnRefresh(satisfactions_, fitness_, vals_, valVsConstIdx_); 
        else
            CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_); 
        
        if(CspProcess.getBestSoFar() != null){
//////            if(violations_.size() <= CspProcess.bestSoFar.getRankComponents().size() && fitness_.get(0) < CspProcess.bestSoFar.getFitnessVal(0)){
//////                CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
//////            }
            if(this.isMorePromisingThan(CspProcess.getBestSoFar())){
//                CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
                CspProcess.upgradeBestSoFar(this); //clone will happen in metod
            }
        }
    }catch (SolutionFoundException sfe){
        CspProcess.upgradeBestSoFar(this);
        throw sfe;
    }
    //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

    //DO NOT DELETE THESE LINES
    //<<
        curRank = this.getRank();        
        if(curRank>=prevRank){ //worse or same => stagnant
            noProgressCounter++;
        }else{
            noProgressCounter = 0;
        }
    //>>    
    }
    

    /**
     * NOT TESTED... TEST IT FIRST BEFORE USING IT
     */
    private void resetObjectiveFunction() throws SolutionFoundException{
        //DO NOT DELETE THESE LINES
        //<<
            totalEvals++;
            double prevRank;
            double curRank;
            prevRank = this.getRank();
        //>>

        //TODO... Call your objective function here....
        //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            //objFunction_NQueenII();
        try{
            if(externalData_ != null)
                externalData_.ObjectiveFnReset(vals_, fitness_, satisfactions_, noGood, valVsConstIdx_); 
            else
                CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_);
            
            if(CspProcess.getBestSoFar() != null){
                if(this.isMorePromisingThan(CspProcess.getBestSoFar())){
//                    CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
                    CspProcess.upgradeBestSoFar(this); //cloned
                }
//////                if(violations_.size() <= CspProcess.bestSoFar.getRankComponents().size() && fitness_.get(0) < CspProcess.bestSoFar.getFitnessVal(0)){
//////                    CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
//////                }
            }
        }catch (SolutionFoundException sfe){
            CspProcess.upgradeBestSoFar(this);
            throw sfe;
        }
        //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                
        //DO NOT DELETE THESE LINES
        //<<
            curRank = this.getRank();        
            if(curRank>=prevRank){ //worse or same => stagnant
                noProgressCounter++;
            }else{
                noProgressCounter = 0;
            }
        //>>
    }
    
    /**
     * NOT TESTED.... TEST IT PROPERLY BEFORE USE
     */
    private void appendObjectiveFunction() throws SolutionFoundException{
        totalEvals++;
        //DO NOT DELETE THESE LINES
        //<<
            double prevRank;
            double curRank;
            prevRank = this.getRank();
        //>>      
            
        //TODO... Call your objective function here....
        //Start<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
        try{
            if(externalData_ != null){
                externalData_.ObjectiveFnAppend(vals_, fitness_, satisfactions_, noGood, valVsConstIdx_); 
            }
            else
                CCSPfns.objFn(1, vals_, fitness_, violations_, userInput_);
            
            if(CspProcess.getBestSoFar() != null){
//////                if(violations_.size() <= CspProcess.bestSoFar.getRankComponents().size() && fitness_.get(0) < CspProcess.bestSoFar.getFitnessVal(0)){
//////                    CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
//////                }
                if(this.isMorePromisingThan(CspProcess.getBestSoFar())){
//                    CspProcess.bestSoFar.setVals((ArrayList<Double>)vals_.clone(), maxCSPval);
                    CspProcess.upgradeBestSoFar(this);
                }
            }
        }catch (SolutionFoundException sfe){
            CspProcess.upgradeBestSoFar(this);
            throw sfe;
        }
        //End>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
       
        //DO NOT DELETE THESE LINES
        //<<
            curRank = this.getRank();        
            if(curRank>=prevRank){ //worse or same => stagnant
                noProgressCounter++;
            }else{
                noProgressCounter = 0;
            }
        //>>
    }

    /**
     * checks if marriage is compatible in a sense that two chromosomes should
     * have at least two opposite positions which can help in finding the point
     * of intersection. for example {-1,-1, 0 1} and {-1, 1, 0 , -1} has opposite
     * positions at 2nd and 4th place.
     * 
     * @param c another couple
     * @return 
     */
    public boolean  isMarriageCompatible(Chromosome c){        
        boolean result = false;
        
        CspProcess.bringCloserRatio = 0.75;
        
        if(this.hasSameRankComponent(c)){ // && this.getRankComponents().size() < userInput_.totalConstraints-userInput_.totalDecisionVars){
//            if(CspProcess.bOptimizationMode){
//                result = false;
//            }else{
//            if(CspProcess.stagnantCount%2==0){
//                result = false;
//                CspProcess.MAX_MOVES = 5;
//            }else{
//                CspProcess.bringCloserRatio = 0.25;
//                result = true;            
//                CspProcess.MAX_MOVES = 5;
//            }
            
            if(Math.random() < 0.25){
                result = false;
                CspProcess.MAX_MOVES = 5;
            }else{
                CspProcess.bringCloserRatio = 0.1;
                result = true;            
                CspProcess.MAX_MOVES = 5;
            }
        }else{ 
            CspProcess.bringCloserRatio = 0.75;
            CspProcess.MAX_MOVES = 5;
            result = true;                       
        }
        
        return  result;
    }

//    public void useImmunity() {
//        this.immunity--;
//        if(immunity < 0){
//            immunity = 0;
//        }
//    }
//
//    public int getImmunity() {
//        return immunity;
//    }
    
    
    @Override
    public String toString() {
        String sp = "  ";
        String print = "\n"+sp+"{";
        String lineStart = "\n"+sp+sp;
        print += lineStart+"vals("+ vals_.size()+ "):, " + vals_.toString();
        print += lineStart+"Fitness (bweighted-"+userInput_.bWeighted+"):," + fitness_.toString();
        if(violations_.isEmpty())
            print += lineStart + "Violations:, (No Violation)";
        else
            print += lineStart +"Violations:," + violations_.toString();
//        if(!satisfactions_.isEmpty())
//            print += lineStart+"Satisfaction:" + satisfactions_.toString();
        
        //print += lineStart+"valsVsConstraintIdx:";
//        for (int i = 0; i < valVsConstIdx_.length; i++) {
//            if(!valVsConstIdx_[i].isEmpty())
//                print+= "(" + i + ")"+ valVsConstIdx_[i].toString()+"; ";            
//        }
        
//        print += lineStart+"NoGoods:" + noGood.toString();
        print += lineStart+"Rank:," + getRank()+"/[0:"+(userInput_.totalConstraints-1)+"]";
//        print += lineStart+"pStart:" + externalData_.getpStart();
        print += "\n"+sp+"}\n";
        return print;
    }

    @Override
    public int compareTo(Object obj){
        if (!(obj instanceof Chromosome)) {
            throw new ClassCastException("Not a Chromosome");
        }
        Chromosome c = (Chromosome) obj;
        
//        if(this.tempSortBy != c.tempSortBy){
//            throw new UnsupportedOperationException("Found different sort types.");
//        }
        
        int totalConstainThis=0;
        int totalConstainC=0;
        int retVal;
        
        if(tmpSortBy == Chromosome.BY_DISCOURAGE){
            for (Integer pv : discourageVios) {
                if(this.violations_.contains(pv)){
                    totalConstainThis++;
                }
                if(c.violations_.contains(pv)){
                    totalConstainC++;
                }
            }
            if(totalConstainThis - totalConstainC>0){ //totalConstainThis  is worse
                return 1;
            }else if(totalConstainThis - totalConstainC<0){
                return -1;
            }else{
                tmpSortBy = BY_VIOLATIONS;
                if(this.getRank() - c.getRank()>0){ //this is worse than c
                    retVal =  1;
                }else if(this.getRank() - c.getRank()<0){
                    retVal =  -1;
                }else{
                    retVal =  0;
                }
                tmpSortBy = Chromosome.BY_DISCOURAGE;
                return retVal;
            }
        }
        
        if(tmpSortBy == Chromosome.BY_VIOLATIONS 
                || tmpSortBy == Chromosome.BY_SATISFACTIONS
                || tmpSortBy == Chromosome.BY_FITNESS) //good to bad - less to more
            //return this.getRank() - c.getRank();
        {
            if(this.getRank() - c.getRank()>0){
                return 1;
            }else if(this.getRank() - c.getRank()<0){
                return -1;
            }else{
                return 0;
            }
        }
        else if(tmpSortBy == BY_AGE){
            if(this.age > c.age){
                return 1;
            }else if(this.age < c.age){
                return -1;
            }else{
                return 0;
            }
        }
        else if(tmpSortBy == Chromosome.BY_IMMUNITY){
            return this.immunity - c.immunity;
        }
        else if(tmpSortBy == Chromosome.BY_RO){ //good to bad - more to less
            if(c.tempRo - this.tempRo>0){
                return 1;
            }else if(c.tempRo - this.tempRo<0){
                return -1;
            }else{
                return 0;
            }
        }
        else //can also be used for "preferenced fitness_ values" - not implemented yet.
            throw new UnsupportedOperationException("Not supported yet.");
        
    }

    @Override
    public Object clone() {
        try {
            Chromosome chromosome = (Chromosome) super.clone();
            // nope... do not clone thses....
            //chromosome.externalData_ = (ExternalData) this.externalData_.clone();
            //chromosome.userInput_ = (UserInput) this.userInput_.clone();

//            chromosome.age = 0;
            
            chromosome.vals_ = (ArrayList<Double>)vals_.clone();
            //Collections.copy(chromosome.vals_, vals_);
            
            chromosome.valVsConstIdx_ = new Idx2D[userInput_.totalConstraints];
            for (int i = 0; i < valVsConstIdx_.length; i++) {
                chromosome.valVsConstIdx_[i] = (Idx2D)valVsConstIdx_[i].clone();
            }
            
            chromosome.noGood = (ArrayList<Double>)noGood.clone();

            chromosome.fitness_ = (ArrayList<Double>)fitness_.clone();
            //Collections.copy(chromosome.fitness_, fitness_);


            chromosome.violations_ = (ArrayList<Integer>)violations_.clone();
            //Collections.copy(chromosome.violations_, violations_);

            //chromosome.satisfactions_ = (ArrayList<ArrayList>)satisfactions_.clone();
            
            chromosome.satisfactions_ = new ArrayList<ArrayList>();
            
            for (int i = 0; i < satisfactions_.size(); i++) {
                chromosome.satisfactions_.add((ArrayList)satisfactions_.get(i).clone()); 
            }

           // Collections.copy(chromosome.satisfactions_, satisfactions_);

            return chromosome;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Chromosome)) {
            throw new ClassCastException("Not a Chromosome");
        }
        Chromosome c = (Chromosome) obj;
        boolean iseq;
        
        if(this.satisfactions_.isEmpty()){ //using violations...
            iseq = false;
            if(this.violations_.equals(c.violations_)){
                iseq = true;
            }         
        }else{
            iseq = false;
            if(this.satisfactions_.equals(c.satisfactions_)){
                iseq = true;
            }            
        }
       
        return iseq;        
    }  
}
