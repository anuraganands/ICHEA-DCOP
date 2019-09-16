 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import javax.activation.UnsupportedDataTypeException;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import matlabcontrol.MatlabConnectionException;
import org.jdesktop.application.Application;
//import org.jfree.data.category.DefaultCategoryDataset;
//import org.jfree.data.xy.XYDataset;
//import org.jfree.data.xy.XYSeries;
//import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author Anurag Sharma
 */
public class CspProcess{
    public boolean bMatlabDraw;
    private boolean drawStart;
    private ArrayList<Chromosome> chromosomes_;
    private Queue<Chromosome> suspended_;
    private static Chromosome bestSoFar;
    private static Chromosome bestSoFar_prevProgressive;
    private double prevBest_;
    private double curBest_;
    private int stillSameBestCount;
    private ArrayList<Chromosome> solutions_;
    private ArrayList<ArrayList<Double>> chromeValues;
    public static UserInput userInput_;
    private ExternalData externalData_;
    //private int population_;
    //private int generation_;
    private int poolSize_;
    private int tourSize_;
    private int knearest_;
    private String dataType_;
    private Double[] range_;
    static int MAX_MOVES = 5;//This is 5 always updated in chomosome.isMarriageCompatible
    private final int MUM = 20;
    private final int MU = 20;
    private double MUTATION_RATE = 0.1;
    //private final int ARCHIVE_MAX;
    private final double REPLACE_PERCENT = 0.10; //8% PERCENT of chromosomes replaced by new population
    private final double IMMUNITY_PERCENT = 0.10; 
    private final double PARTIAL_SOL_PERCENT = 0.10;
    private MyRandom r_;
    private ArrayList<ArrayList<Double>> sameBestChromeVals_; //stores top ranked SAME_BEST_VAL_PERCENT % of chromosomes
    private int hasAllSame_; //counter to check if the SAME_BEST_VAL_PERCENT % of chromosomes is same for SAME_BEST_GENERATIONS generations
    private final double SAME_BEST_VAL_PERCENT = 0.5; //top ranked percentage of total chromosome population
    private final int SAME_BEST_GENERATIONS = 12; //12;//measure to SAME_BEST_VAL_PERCENT % of top ranked chromosomes remain same for number of generations.
    private final int NO_PROGRESS_LIMIT = 6;//limit for no progress made in the NO_PROGRESS_LIMIT generations.
    static boolean bStagnant;
    static int stagnantCount;
    private int stagnantVisit;
    public static double bringCloserRatio = 0.5;
    public static double sendFurtherRatio = 0.2;
    private static boolean bOptimizationMode;
    public static double maxCSPval;
    public static ArrayList<ArrayList<ArrayList<Double>>> CSPsols;
    private final double FORCED_PERCENT = 0.75;
    public static boolean bInTransition;
    static int MaxComb; //10
    private int MaxHospital = 1; //2; //MaxComb/2;
    private static int dynamicTime;
    public static int dynamicTimeInc = 1; //* NOTE it can be changed in Gx function
    public static  int useConstraintNo = -1;
    public static int gensEachConstraints = 40;
    private double tabuDist;
    public static int negFeasibleRange;
    private int maxSolPop; //(int)(userInput_.population*0.25);
    private int maxNonSolPop;
   
    public double rhoCOP = 2.0;
    public double rhoCSP = 5.0;

    private static int gGen = 0; //generation
    private static int gGenLastBestOn = 0;
    private static final int gGenTrackStruggle = 10;
    private static MyQueue<Chromosome> prevBests;
    private static double bestProgressThreshold;
    private static boolean bTabuMode = false;
    
    private static final int SORT_HARDCONSVIOS_THEN_FITNESS = 1;
    private static final int SORT_HARDCONSVIOS_THEN_RHO = 2;
    private static final int SORT_FITNESS_THEN_NOVELTY = 3;
    private static final int SORT_FITNESS = 4;
    private static final int SORT_SATISFACTION = 5;
    
    /**
     * 
     * @param userInput
     * @throws MyException 
     */
    public CspProcess(UserInput userInput) throws MyException{
        //this();
        this.userInput_ = userInput;
        this.externalData_ = null;

        if(userInput_ == null){
            throw new MyException("No user input provided.", "Incorrect Data",JOptionPane.ERROR_MESSAGE);
        }

        initialize();
    } // Toavoid calling this constructor

    public double getPARTIAL_SOL_PERCENT() {
        return PARTIAL_SOL_PERCENT;
    }

    public static double getBestProgressThreshold() {
        if(prevBests.curSize()<prevBests.capacity()){
            return Double.MAX_VALUE;
        }else{
            double avg = 0.0;
            ArrayList<Chromosome> c = new ArrayList<Chromosome>();
            while(prevBests.curSize()>0){
                c.add(prevBests.dequeue());
            }
            for (int i = 0; i < c.size()-1; i++) {                
                avg += Math.abs(c.get(i).getRank()-c.get(i+1).getRank());
            }
            while(c.size()>0){
                prevBests.forceEnqueueByDequeue(c.remove(0));
            }
            avg = avg/prevBests.curSize();
            return avg;
        }
    }
    
    
    

    public CspProcess(ExternalData externalData)throws MyException{
        //this();
        this.externalData_ = externalData;
        this.userInput_ = this.externalData_.getUserInput();

        if(userInput_ == null || this.externalData_ == null){
            throw new MyException("No user input provided or empty external data.", "Incorrect Data",JOptionPane.ERROR_MESSAGE);
        }

        initialize();
    }

    private CspProcess(){
        ;
    }

    public static void upgradeBestSoFar(final Chromosome newBest) {
//        if(bestSoFar == null){
//            gGenLastBestOn = gGen;
//            CspProcess.bestSoFar = (Chromosome)newBest.clone();
//            prevBests.clearAll();
//            return;
//        }
//        
//        if(newBest.isMorePromisingThan(bestSoFar)){ //assume its always better... 
            double improvement = Math.abs(newBest.getRank()-bestSoFar.getRank());
//            if(gGen-gGenLastBestOn > gGenTrackStruggle){// && improvement > getBestProgressThreshold()/5){
                prevBests.forceEnqueueByDequeue((Chromosome)newBest.clone());
//            }
            
            if(improvement>getBestProgressThreshold()/5)
                gGenLastBestOn = gGen;
            
//            if(newBest.isMorePromisingThan(bestSoFar))
            if(bTabuMode){
                //Here fitness is more important because at this stage we are only after better fitness.
                if(newBest.isMorePromisingThan(bestSoFar) && newBest.getRank()<bestSoFar.getRank()){
                    CspProcess.bestSoFar = (Chromosome)newBest.clone();
                }
            }else{
                CspProcess.bestSoFar = (Chromosome)newBest.clone();
            }
//        }                
    }

    public static int getDynamicTime() {
        return dynamicTime;
    }

    public static void dynamicTimeIncrement(){
        dynamicTime+=dynamicTimeInc;
        if(dynamicTime > userInput_.maxDynamicTime){// userInput_.totalConstraints-userInput_.totalDecisionVars){
            dynamicTime = userInput_.maxDynamicTime; //userInput_.totalConstraints-userInput_.totalDecisionVars;        
        }
        
        setbOptimizationMode(false);
        gGenLastBestOn = gGen;
        
        if(!bTabuMode)
            prevBests.clearAll();
    }
    
    public static Chromosome getBestSoFar() {
        return bestSoFar;
    }

    public static void setbOptimizationMode(boolean bOptMode) {
        CspProcess.bOptimizationMode = bOptMode;
        if (bOptMode){
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        }else{
            Chromosome.tmpSortBy = userInput_.solutionBy;
        }
    }

    
    
    
    /**
     * private Constructor used only for default initialization
     */
    private void initialize() throws MyException{
        //abToHoJaFlag = false;
        bMatlabDraw = false;
        drawStart = false;
        chromosomes_ = new ArrayList<Chromosome>();
        suspended_ = new LinkedList<Chromosome>();
        solutions_ = new ArrayList<Chromosome>();
        this.tourSize_ = 2; //default value assumed
        this.knearest_ = (int)(0.05*userInput_.population); //default value assumed
        this.r_ = new MyRandom();
        
        this.poolSize_ = userInput_.population/2; //default values assumed
        //ARCHIVE_MAX = userInput_.population/2;
        this.dataType_ = this.userInput_.dataType;

        this.range_ = new Double[userInput_.totalDecisionVars];
        for (int i = 0; i < userInput_.totalDecisionVars; i++) {
            this.range_[i] = 0.5; //double assumed.
        }

        if (this.userInput_.population < 5 || this.userInput_.generation < 1){
            throw new MyException("poulation size should be > 5 and generation should be > 1", "Input Data Error!",JOptionPane.ERROR_MESSAGE);
        }
        hasAllSame_ = 0;
        sameBestChromeVals_ = null;
        bStagnant = false;
        stagnantCount = 0;
        prevBest_ = Double.POSITIVE_INFINITY;
        curBest_ = Double.POSITIVE_INFINITY;
        stillSameBestCount = 0;
        stagnantVisit = 0;
        setbOptimizationMode(false);
        maxCSPval = Double.MAX_VALUE;
        dynamicTime = 0;//0
        negFeasibleRange = 0;
        tabuDist = -1.0;
        
        CSPsols = new ArrayList<ArrayList<ArrayList<Double>>>();
        CSPsols.add(new ArrayList<ArrayList<Double>>());
        for (int i = 0; i < userInput_.totalConstraints; i++) {
            CSPsols.get(0).add(new ArrayList<Double>());
        }
        bInTransition = false;       

        maxSolPop = (int)(userInput_.population*0.75); 
        maxNonSolPop = userInput_.population-maxSolPop;

        prevBests = new MyQueue<Chromosome>(4);       
        MaxComb = Math.max(5,userInput_.totalConstraints); 
    }
    
    
    /**
     * Starts the whole process
     */
    public void start(JProgressBar pb, boolean saveChromes, ByRef nextPrefSuggestion, Draw draw) throws MyException{
        ArrayList<Chromosome> parents;
        ArrayList<Chromosome> offspring;
        ArrayList<Chromosome> temp;
        ArrayList<Chromosome> CSPsolsDB = new ArrayList<Chromosome>();
        ArrayList<Chromosome> tempMut = new ArrayList<Chromosome>();
        Chromosome tempChrom;
        double startTime = 0.0;
        double endTime = 0.0;
        int totalSaved;
 
        PrintWriter runHistory = null; 
        
        //problem sol
        //<<<


        //>>
        
        
        try{
            
        File directory = new File ( "Test Results" ) ;
        

            File [ ] filesInDir = directory.listFiles(
                new FileFilter() {
                    public boolean accept(File pathname) {
                        return (pathname.getName().startsWith("G"+userInput_.gxFn+"-") 
                                && pathname.getName().endsWith("_history.csv"));
                    }
                }
            );
        
            
        
            int fileNo = 1;
            
            if(filesInDir.length>0){
                fileNo = filesInDir.length+1;
            }
        
            runHistory = new PrintWriter(directory.getAbsolutePath() + "\\G"+userInput_.gxFn+ "-"+fileNo+"_history.csv");
            
            //columns
            runHistory.println("Gen"+","+"Best Fitness"+","+"Best Violations" + "," 
                            + "Constraints"+","+"Processing Time");

            initializeChromosomes(this.chromosomes_, userInput_.population, gGen);
            bestSoFar = this.chromosomes_.get(0);
                                    
            startTime = System.nanoTime();
            startTime = startTime/Math.pow(10, 9);

            for (gGen = 1; gGen <= userInput_.generation; gGen++) {
                //if(g < userInput_.generation-1){ 
                    CSPsolsDB = new ArrayList<Chromosome>(); 
                    if(bestSoFar.isSolution()){
                        int cloneCount = 0;
                        long muRate;
                        
                        if(externalData_ != null){
                            for (int i = 0; i < 1; i++) {
                                for (Chromosome ch : chromosomes_) {
                                    if(ch.isSolution()){ 
                                        tempChrom = (Chromosome)ch.clone();
                                        mutationSwap(tempChrom, 1,10); //0.1*userInput_.totalConstraints);//Math.ceil(2*Math.exp(-0.01*g)));
                                        CSPsolsDB.add(tempChrom);
                                    }
                                }
                                for (Chromosome ch : chromosomes_) {
                                    if(ch.isSolution()){ 
                                        tempChrom = (Chromosome)ch.clone();
                                        mutationGroupSwap(tempChrom);
                                        CSPsolsDB.add(tempChrom);
                                    }
                                }
                            }    
                        }
                    }
                    
                    parents = noveltyTournamentSelection(); //select best parents only.
                    offspring = interRaceCrossover(parents); //crossover selected parents only  
                                      
                    mutation(offspring);//mutating crossovered offspring only                                                                                        
                    parents.clear();//no longer needed               
                           
                    
                    
// I don't think this is useful .... you may uncomment it and test
                    
//                    if(bOptimizationMode){                        
//                        tempMut = new ArrayList<Chromosome>();
//                        for (int i = 0; i < userInput_.population*0.25; i++) {
//                            tempMut.add((Chromosome)chromosomes_.get(i).clone());
//                        }
//                        
//                        double tmp = this.MUTATION_RATE;
//                        MUTATION_RATE = 1.0;
//                        mutation(tempMut);
//                        MUTATION_RATE = tmp;
//                        chromosomes_.addAll(tempMut);
//                    }
                     
                            
                    chromosomes_.addAll(offspring);//include all offspring into the chromosome set.                        
                    chromosomes_.addAll(CSPsolsDB);

                    
                    temp = new ArrayList<Chromosome>();
                    initializeChromosomes(temp, userInput_.population-chromosomes_.size(), gGen);
                    chromosomes_.addAll(temp);
                    
                    sortAndReplace(gGen);//sort according to least violation first then on ro value (novelty)                        
                    
                    System.out.println("Gen: "+gGen); 
                    System.out.println(bestSoFar);
   
                    endTime = System.nanoTime();
                    endTime = endTime/Math.pow(10, 9);
                    endTime = MyMath.roundN(endTime - startTime,2);
                    
                    
                    System.out.println("Gen: " + gGen + ", Dynamic Time: " + getDynamicTime()+"/"
                            + userInput_.maxDynamicTime
                            + ", time: "+endTime+" Sec");
                    

                    
                    runHistory.println(gGen+","+bestSoFar.getFitnessVal(0)+","+bestSoFar.getRankComponents().size() + "," 
                            + dynamicTime+","+endTime);
                    
                    
                    
                    System.out.println("top ones..." + MaxComb);
                    for (int i = 0; i < 5; i++) {
                        //System.out.print(new DecimalFormat  ("#.##").format(chromosomes_.get(i).getFitnessVal(0)) +", ");
                        System.out.print(chromosomes_.get(i).getFitnessVal(0)+", ");// +"["+ chromosomes_.get(i).vals_.get(0) + "], ");
                        
                    }  

                
                if(bMatlabDraw){
////                    if(startDrawing == null)
////                        startDrawing = new Draw(); 
                    draw.startDrawing(matlabPlotBuildGeneration());
                }
                
                if(pb != null)
                    pb.setValue(pb.getMinimum()+(pb.getMaximum()-pb.getMinimum())*gGen/(userInput_.generation));
            }            
        }catch(SolutionFoundException SFE){
            System.out.println("\nSolution found at generation " + (gGen));
            System.out.println("Reason: " + SFE.getMessage());
            runHistory.println("\n\nSolution found at generation " + (gGen));
            runHistory.println("Reason: " + SFE.getMessage());
            try {
                if(bMatlabDraw){
////                    if(startDrawing == null)
////                        startDrawing = new Draw();
                    draw.startDrawing(matlabPlotBuildGeneration());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
//            g = userInput_.generation;
            pb.setValue(pb.getMinimum()+(pb.getMaximum()-pb.getMinimum())*userInput_.generation/(userInput_.generation));            
        }catch(MyException me){
            me.showMessageBox();
        }catch(UnsupportedDataTypeException udte){
            udte.printStackTrace();
        }catch (MatlabConnectionException mce) {
            mce.printStackTrace();           
        }
        catch(Exception e){
            e.printStackTrace();
            throw new MyException("Exception raised in Start Process", "Check Start Process()",JOptionPane.ERROR_MESSAGE);           
        }finally{
            endTime = System.nanoTime();
            endTime = endTime/Math.pow(10, 9);
            System.out.println("Process time(Sec): " + MyMath.roundN(endTime - startTime,2));
            System.out.println("total chromosomes: "+chromosomes_.size());
            System.out.println("Total Evaluations: "+ Chromosome.totalEvals);
            
            
            runHistory.println("Process time(Sec): " + MyMath.roundN(endTime - startTime,2));
            runHistory.println("total chromosomes: "+chromosomes_.size());
            runHistory.println("Total Evaluations: "+ Chromosome.totalEvals);
            
            
            System.err.flush();
            System.out.flush();
            //Thread.currentThread().sleep(100);//sleep for 1000 ms

            setSolution();
            if(this.solutions_.isEmpty()){                            
                System.out.println("No Solution Found :( ****************");
                System.out.println("best chromosomes\n" + bestSoFar);
                runHistory.println("No Solution Found :( ****************"); 
                runHistory.println("\nbest chromosomes\n" + bestSoFar);                
            }else{
                System.out.println("Solution Found");
                System.out.println("best chromosomes\n" + bestSoFar);
                runHistory.println("\n\nSolution Found"); 
                runHistory.println("\nbest chromosomes\n" + bestSoFar);
            }  
            
            runHistory.close();
            
            if(externalData_ != null){ //external data is used.  
                nextPrefSuggestion.setVal(String.valueOf(externalData_.getNextPrefLimit()));
                String fileName = "partial_solutions_pref_"+externalData_.getCurPref()+".ichea";
                try {
                    FileOutputStream fos;
                    fos = new FileOutputStream(fileName);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);

                    Set<Chromosome> s = new LinkedHashSet<Chromosome>(chromosomes_);
                    chromosomes_ = new ArrayList<Chromosome>(s); 
                    totalSaved = Math.min(chromosomes_.size(),(int)(PARTIAL_SOL_PERCENT*userInput_.population));
                    
                    chromosomes_ = new ArrayList<Chromosome>(chromosomes_.subList(0, totalSaved));
                    chromosomes_.add(0, bestSoFar);
                    for (Chromosome c : chromosomes_) {
                        try{
                            c.refreshFitness();
                        }catch (SolutionFoundException sfe){
                            ;
                        }
                    }
                    
                    oos.writeObject(chromosomes_);//chromosomes_.subList(0, (int)(PARTIAL_SOL_PERCENT*userInput_.population)));
                    oos.flush();
                    oos.close();
                    System.out.println("["+ totalSaved + "] chromosomes of data successfully Saved to File ["+fileName+"].");
                } catch (FileNotFoundException fnfe) {
                    System.err.println("Serialize Error! File cannot be created.");
                } catch (IOException ioe){
                    ioe.printStackTrace();
                    System.err.println("Serialize Error! Cannot write to the file ["+fileName+"].");
                }                
            }
        }    
    }


    private boolean allDynamicConstraintsIncluded(){
        //why minus totalDecisionVars?
        //See input data: totalConstraints = totalConstraints + totalDecisionVars
        return (CspProcess.dynamicTime >= userInput_.maxDynamicTime);
    }

    public ArrayList<Chromosome> getSolution(){
        return this.solutions_;
    }
    
    public ArrayList<Chromosome> getChromosomes(){
        return this.chromosomes_;
    }
    
    /**
     * It is the measure of novely. The larger the ro value the more the novelty
     * in the search space.
     * @param chrome
     * @return
     * @throws MyException
     * @throws UnsupportedDataTypeException 
     */
    double getRoValue(Chromosome chrome) throws MyException, UnsupportedDataTypeException{
        double ro;
        if(this.dataType_.contains("Integer")){
            ro = getIntegerRoValue(chrome);
        }
        else if(this.dataType_.contains("Double")){
            ro = getDoubleRoValue(chrome);
        }else{
            throw new UnsupportedDataTypeException("Only supports Integer and Double data type");
        }
        return ro;
    }
    
    /**
     * method determines ro value for nominal data types. the higher the better.
     * @param chrome
     * @return
     * @throws MyException 
     */
    double getIntegerRoValue(Chromosome chrome) throws MyException{
        double ro;
        ArrayList<Integer> validChromosomesIdx = new ArrayList<Integer>();
        Double []dist;
        int tempKnearest;
        
        if (chromosomes_.isEmpty()){
            throw new MyException("No chromosme population", "Variable Initialization Error",JOptionPane.ERROR_MESSAGE);
        }
 
        for (int i = 0; i < chromosomes_.size(); i++) {
            if (chromosomes_.get(i).getRank() != this.userInput_.totalConstraints)
                validChromosomesIdx.add(i);            
        }

        dist = new Double[validChromosomesIdx.size()];
        for (int i = 0; i < validChromosomesIdx.size(); i++) {
            dist[i] = MyMath.norm(chrome.getValsCopy(), chromosomes_.get(validChromosomesIdx.get(i)).getValsCopy(), MyMath.DIST_DUPLICATE_MISMATCH);
//            NOTE: I am using "SQUARE of distance" instead of just distance
//            because I will be using variance for ro_min.
            dist[i] = Math.pow(dist[i], 2);

        }
        Arrays.sort(dist);        
        // x1 itself is included in this set which should have the value 0.
        if(dist.length<=knearest_){ //////@Danger code............................
            tempKnearest = dist.length-1;
        }else{
            tempKnearest = knearest_;
        }
        ro = (1.0/tempKnearest) * MyMath.sum(dist, 0, tempKnearest);//Note: should not be knearest-1 as x1 itself is also included

        ro = MyMath.roundN(ro, 0); //to reduce so much variations...
        return ro;                     
    }
    
    /**
     * ro value determines the rank of novelty. The higher value the better.
     * @param chrome
     * @return
     * @throws MyException 
     */
    double getDoubleRoValue(Chromosome chrome) throws MyException{
        double ro;
        int maxViolation = this.userInput_.totalConstraints;
        ArrayList<Integer> validChromosomesIdx = new ArrayList<Integer>();
        Double []dist;
        int tempKnearest;

        
        if (chromosomes_.isEmpty()){
            throw new MyException("No chromosme population", "Variable Initialization Error",JOptionPane.ERROR_MESSAGE);
        }
 
        for (int i = 0; i < chromosomes_.size(); i++) {
            if (chromosomes_.get(i).getRank() != maxViolation)
                validChromosomesIdx.add(i);            
        }

        dist = new Double[validChromosomesIdx.size()];
        for (int i = 0; i < validChromosomesIdx.size(); i++) {
            dist[i] = MyMath.norm(chrome.getValsCopy(), chromosomes_.get(validChromosomesIdx.get(i)).getValsCopy(), MyMath.DIST_EUCLEADIAN);
//            NOTE: I am using "SQUARE of distance" instead of just distance
//            because I will be using variance for ro_min.
            dist[i] = Math.pow(dist[i], 2);

        }
        Arrays.sort(dist);        
        // x1 itself is included in this set which should have the value 0.
        if(dist.length<=knearest_){ //////@Danger code............................
            tempKnearest = dist.length-1;
        }else{
            tempKnearest = knearest_;
        }
        ro = Math.pow(1.0/tempKnearest, 2) * MyMath.sum(dist, 0, tempKnearest);//Note: should not be knearest-1 as x1 itself is also included

        ro = MyMath.roundN(ro, 0);
        return ro;
    }


    private ArrayList<String> matlabPlotBuildGeneration() throws Exception{
        ArrayList<String> MatlabCommands = new ArrayList<String>();
        int drawXdata;
        double drawYdata;  
        String title = "G";
        
        if(userInput_.gxFn == 240)
            title = title+"24\\_0";
        else if(userInput_.gxFn == 241)
            title = title+"24\\_1";
        else if(userInput_.gxFn == 243)
            title = title+"24\\_3";
        else if(userInput_.gxFn == 244)
            title = title+"24\\_4";
        else
            title = title+userInput_.gxFn;
        
                
        if(!drawStart){
            MatlabCommands.add("figure;");
            MatlabCommands.add("hold on;");
            MatlabCommands.add("title('"+title+"');"); 
            MatlabCommands.add("xlabel('Generations');");
            MatlabCommands.add("ylabel('Fitness');");  
            
            drawStart = true;
        }
        

        
        MatlabCommands.add("xlim([1 " + userInput_.generation+"]);");
        drawXdata = gGen; //x axis;
        drawYdata = -MyMath.roundN(getBestSoFar().getFitnessVal(0),4);//y axis
        MatlabCommands.add("x=" + drawXdata + ";"); 
        MatlabCommands.add("y=" + drawYdata + ";");
        
        MatlabCommands.add("plot(x,y,'.k');");
        
        if(gGen%gensEachConstraints==0){
            MatlabCommands.add("maxVal = ylim;");
            MatlabCommands.add("maxVal = ceil(maxVal(2));");
            for (int i = 1; i <= getDynamicTime(); i++) {
                MatlabCommands.add("vertX = ones(maxVal+1,1)*"+i*gensEachConstraints+";");  
                MatlabCommands.add("vertY = [0:maxVal];");
                MatlabCommands.add("plot(vertX,vertY,'b-');");
            }
        }
        
        MatlabCommands.add("set(gca,'XTick',0:"+gensEachConstraints+":"+userInput_.generation+");");

        MatlabCommands.add("drawnow;");
        

        return MatlabCommands;
    }
    
    private ArrayList<String> matlabPlotBuildConstraints(){
        ArrayList<String> commands = new ArrayList<String>();
        
                commands.add("hold on;");
        commands.add("x = [-100:0.1:100];");
        commands.add("br = 10.0;");
        commands.add("sr = 9.9;");
        commands.add("y1p = sqrt(br^2 - x.^2);");
        commands.add("y1m = -sqrt(br^2 - x.^2);");
        commands.add("y2p = sqrt(sr^2 - x.^2);");
        commands.add("y2m = -sqrt(sr^2 - x.^2);");
        
        commands.add("y3p = sqrt(br^2 - (x+2*br - 0.1).^2);");
        commands.add("y3m = -sqrt(br^2 - (x+2*br - 0.1).^2);");
        commands.add("y4p = sqrt(sr^2 - (x+2*br - 0.1).^2);");
        commands.add("y4m = -sqrt(sr^2 - (x+2*br - 0.1).^2);");


        commands.add("fig1 = gcf;"); //get current figure or create figure fig1
        commands.add("axes1 = axes('Parent',fig1);"); //Create axes
        
        commands.add("ylim(axes1,[-100 100]);");
        commands.add("box(axes1,'on');");
        commands.add("hold(axes1,'all');");
        commands.add("plot(x,y1p);");
        commands.add("plot(x,y1m,'Parent',axes1);");
        commands.add("plot(x,y2p,'Parent',axes1);");
        commands.add("plot(x,y2m,'Parent',axes1);");
        commands.add("plot(x,y3p,'Parent',axes1);");
        commands.add("plot(x,y3m,'Parent',axes1);");
        commands.add("plot(x,y4p,'Parent',axes1);");
        commands.add("plot(x,y4m,'Parent',axes1);");
        
        return commands;
    }

    private void initializeChromosomes(ArrayList<Chromosome> chromosome, final int SIZE, final int gen) throws Exception {
        boolean bInitialStage;
        if(SIZE<=0){
            chromosome = null;
            return;
        }
        
        if (externalData_ != null){
            if(gen<10){
                bInitialStage = true;
            }else{
                bInitialStage = false;
            }
                
            chromosome.addAll(externalData_.initializeExternalChrmosomes(SIZE));
        }else{
            initializeChromosomesRandomly(chromosome,SIZE);
        }
    }

    /**
     * Initializes Chromosomes with random values
     */
    private void initializeChromosomesRandomly(ArrayList<Chromosome> chromosome, final int SIZE) throws Exception{
        for (int i = 0; i < SIZE; i++) {            
            chromosome.add(initializeChromosomeRandomly());
        }        
    }
    
    
    private Chromosome initializeChromosomeRandomly() throws Exception{
        Object rand = null;
        Chromosome tempChromosome;
               
        tempChromosome = new Chromosome(this.userInput_.solutionBy, this.userInput_);
        for (int j = 0; j < userInput_.totalDecisionVars; j++) {
            if (userInput_.dataType.contains("Integer")){
                rand = r_.randVal(userInput_.minVals.get(j).intValue(), userInput_.maxVals.get(j).intValue());
            }else if (userInput_.dataType.contains("Double")){
                if(Math.random()<0.5){
                    rand = r_.randVal((Double)userInput_.minVals.get(j), (Double)userInput_.maxVals.get(j));
                }else{
                    if(Math.random() < 0.5){
                        rand = userInput_.minVals.get(j);
                    }else{
                        rand = userInput_.maxVals.get(j);
                    }
                }
            }
            else{
                System.err.println("Incorrect use of data types");
                System.exit(1);
            }
            tempChromosome.appendVal((Double)rand);
        }
        return tempChromosome;       
    }
    

    /**
     * noveltyTournamentSelection() - Tournament selection based on novelty
     * of the chromosome in the population.
     * @return Returns ArrayList<Chromosome> of parent selected population 
     */
    private ArrayList<Chromosome> noveltyTournamentSelection() throws MyException, UnsupportedDataTypeException{
        ArrayList<Chromosome> candidates = new ArrayList<Chromosome>();
        ArrayList<Chromosome> parents = new ArrayList<Chromosome>(); // shoud have this.pool sizse
        ArrayList<Integer> temp;
        double csize0, csize1;
        double ro0, ro1;
        int candidate0dominates;
        
        if(this.tourSize_ != 2){
            throw new MyException("Tour Should be 2", "Inappropriate Tour Size",JOptionPane.ERROR_MESSAGE);
        }
                 
        for (int p = 0; p < this.poolSize_; p++) {
            //select tourSize_ chromosomes k.e 2 chromosomes randomly from the population
            temp = MyRandom.randperm(0, chromosomes_.size()-1);
            candidates.clear();
            for (int t = 0; t < this.tourSize_; t++) {                
                candidates.add(chromosomes_.get(temp.get(t)));
            }
            temp = null;

            try{                   
                if(candidates.get(0).getRankComponents().size() < candidates.get(1).getRankComponents().size()){
                    parents.add(candidates.get(0));
                }else if (candidates.get(0).getRankComponents().size() > candidates.get(1).getRankComponents().size()){
                    parents.add(candidates.get(1));
                }else{
                
                    csize0 = MyMath.roundN(candidates.get(0).getRank(),3);
                    csize1 = MyMath.roundN(candidates.get(1).getRank(),3);

                    if (csize0 < csize1){ // the lower the better
                        parents.add(candidates.get(0));                
                    }
                    else if (csize1 < csize0){
                        parents.add(candidates.get(1));                
                    }
                    else{  
                        //  
                         //<< you can move it bottom .....
                        ro0 = getRoValue(candidates.get(0)); //do not need to use getRo function, check sortnreplace function if it has already been set in tempRo property.
                        ro1 = getRoValue(candidates.get(1));

                        if (ro0 > ro1 || candidates.get(0).getValsCopy().size() == 1) // the larger the better
                            parents.add(candidates.get(0));
                        else if (ro1 > ro0 || candidates.get(1).getValsCopy().size() == 1)
                            parents.add(candidates.get(1));
                        else{
                        //>>..........................                   
                            
                        //    
                        candidate0dominates = 0;

                        if(candidates.get(0).isStagnant(this.NO_PROGRESS_LIMIT)){
                            parents.add(candidates.get(1));
                        }else if(candidates.get(1).isStagnant(this.NO_PROGRESS_LIMIT)){
                            parents.add(candidates.get(0));
                        }else{
                            temp = MyRandom.randperm(0, 1);
                            parents.add(candidates.get(temp.get(0)));
                        }
                    }                                    
                }
                }
                }catch(Exception e){
                e.printStackTrace();
            }
//            //>>
        }                
        
        return parents;
    }
    
    /**
     * IMPROPER METHOD... NEEDS CORRECTION.... Checks if the solution for CSP has been achieved
     * @return Returns ArrayList<Chromosome> of solution chromosomes.
     */
    private void setSolution(){
        //ArrayList<ArrayList<Double>> duplicates = new ArrayList<ArrayList<Double>>();
        chromeValues = new ArrayList<ArrayList<Double>>();
        int beforeSize, afterSize;
                
        for (Chromosome chromosome : this.chromosomes_) {
            if(chromosome.isSolution()){ // no violations
                this.solutions_.add(chromosome);//no need to clone... it is called at the end
            }
        }  
        if(bestSoFar.isSolution()){
            this.solutions_.add(bestSoFar);
        }
    }
    
    public String printChromeValues(){
        String str;
        
        str = Integer.toString(chromeValues.size()) + "\n";
        str += Integer.toString(this.userInput_.totalConstraints) + "\n";
        for (int i = 0; i < chromeValues.size(); i++) {
            for (int j = 0; j < chromeValues.get(i).size(); j++) {
                str += chromeValues.get(i).get(j).toString() + " ";                
            }
            str += "\n";            
        }
        return str;
    }
    

     /**
     * inter race crossover - offers crossover between 2 different constraint regions only
     * the offspring will have better or same constraint violation than their parents.
     * This process requires 2 parents that produce 2 offspring
     * @param parents list of parents
     * @return returns offspring
     * @throws MyException
     * @throws UnsupportedDataTypeException
     */
    private ArrayList<Chromosome> interRaceCrossover(final ArrayList<Chromosome> parents) throws MyException, UnsupportedDataTypeException,  SolutionFoundException, Exception{
        ArrayList<Chromosome> candidates = new ArrayList<Chromosome>(this.tourSize_);
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        ArrayList<Integer> tempIntAL; 

        //ArrayList<Double> directions;
        //ArrayList<Double> approachDist = new ArrayList<Double>(1);
        
        //double maxDist;
        //double ratio;
        int count;
        
        if(this.tourSize_ != 2 && parents.size() >1){
            throw new MyException("Tour Should be 2", "Inappropriate Tour Size",JOptionPane.ERROR_MESSAGE);
        }
        
        if(parents.isEmpty()){
            System.out.println("No parents??");
        }
        
        for (int i = 0; i < userInput_.population/2; i++) {
            if(Math.random() < 0.9){
                //Randomly pick two parents.
                tempIntAL = MyRandom.randperm(0, parents.size()-1);
                candidates.clear();
                for (int t = 0; t < this.tourSize_; t++) {                
                    candidates.add((Chromosome)parents.get(tempIntAL.get(t)).clone());
                }
                
                try{
                    //Note here we can make integer and double combined problem
                    //set as well.
                    if(dataType_.contains("Integer")){
                        count = this.tourSize_;
                        //while both parents belong to same Constraint region
                        //Drawback - this will case very few crossover + very few
                        //final solutions. That might affect the optimization
                        //problem where we need many candidate solutions.

                            while(candidates.get(0).hasSameRankComponent(candidates.get(1))){// &&
                                    //candidates.get(0).getRank() == candidates.get(1).getRank()){
                                candidates.remove(1);
                                candidates.add(parents.get(tempIntAL.get(count)));
                                count++;
                                if(count >= parents.size()){
                                    //candidates.add(parents.get(tempIntAL.get(immuneCount-1)));
                                    break;
                                    //throw new MyException("No unique parents exist", "Parents in crossover",JOptionPane.WARNING_MESSAGE);
                                }
                            }
//                        }
                        
                        offspring.addAll(interRaceCrossoverInteger(candidates));//only 1 move
                        
                        
                    }else if(dataType_.contains("Double")){
                        //further filter for boundary intersections... 
                        count = this.tourSize_;

                        while(!candidates.get(0).isMarriageCompatible(candidates.get(1))){
                            candidates.remove(1);
                            candidates.add((Chromosome)parents.get(tempIntAL.get(count)).clone());
                            count++;
                            if(count >= parents.size()){                            
                                break;                            
                            }
                        }
                        
//                        if(userInput_.totalDecisionVars < 10 && !bOptimizationMode)
                            offspring.addAll(interRaceCrossoverDoubleStagnant(this.MAX_MOVES, candidates));
//                        else
//                            offspring.addAll(interRaceCrossoverDouble(this.MAX_MOVES, candidates));
                      
                    }else{
                        throw new UnsupportedDataTypeException("Only supports Integer and Double");
                    }         

                }catch (UnsupportedDataTypeException udte) {
                    throw new UnsupportedDataTypeException("Check your data type");
                }
//                catch (MyException me){
//                    me.printMessage();
//                }
            }
        }
        
        return offspring;
    }

    /**
     * interRaceCrossoverInteger - is used only with nominal data types. for integer data
     * use interRaceCrossoverDouble. it virtually moves 2 parents.
     * @param move
     * @param candidates - parents from which offspring are sought.
     * @return returns offspring from given candidate parents
     */
    private ArrayList<Chromosome> interRaceCrossoverInteger(final ArrayList<Chromosome> candidates) throws Exception{
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        ArrayList<Integer> idx = new ArrayList<Integer>();
        Chromosome tempChrome;
        int move;
        boolean isSol;
        
        if (candidates.size() != 2){
            throw new UnsupportedOperationException("Require only 2 parents");
        }
        
        isSol = false;
        for (Chromosome can : candidates) {
            if(can.isSolution()){
                isSol = true;
                break;
            }
        }
        
        //Check common values in both candidate parents.
        int [] commonVals = new int[userInput_.totalConstraints]; //all initialized to 0
        int constVal = 1;
        
        
        for (int j = 0; j < candidates.size(); j++) {
            for (double v : candidates.get(j).getValsCopy()) {
                commonVals[(int)v] += constVal;
            }
            constVal = constVal*10;
        }

        int prevLength, newLength;
        //Technique 1 - Append chromosomes- multi-offpring (0-n) afrom 2 parents. - 
        //<< Build up structure for satisfaction list
//        if(Math.random() < 0.5){//5 && !isSol){ //!bStagnant){ //obviously Math.random is always [0 1)
//        if(!bOptimizationMode){
        if(!isSol){
            constVal = 1;
            for (int j = 0; j < candidates.size(); j++) {
                tempChrome = (Chromosome)candidates.get((j+1)%tourSize_).clone();
                prevLength = tempChrome.noGood.size();
                
                for (int i = 0; i < commonVals.length; i++) {
                    if(commonVals[i]==constVal){
                        tempChrome.appendVal(i);//NOTE ofsp want getSatisfaction value but in this case both are same
                    }                    
                }

                newLength = tempChrome.noGood.size();

                offspring.add((Chromosome)tempChrome);
                constVal = constVal*10;
            }

        }
        
        return offspring;
    }
   
    /**
     * highest valid index of CSPsols.
     * It is possible that the index = highest+1 is in buildup process and 
     * not available for usage.
     * 
     * @return highest valid index. -1 means no valid index available
     */
    private int maxIdxAvailCSPsols(){        
        int highestIndx = CSPsols.size()-1; //can be 1 initially.        
        
        for (ArrayList<Double> valGrp: CSPsols.get(highestIndx)) {
            if(valGrp.isEmpty()){
                highestIndx--; //it can become -1
                break;
            }
        }
        
        //last index can get cleared in CCSPfns so we take the second last
        
        if(highestIndx>0){
            highestIndx--;
        }
        
        return highestIndx;
    }
        
    
    /**
     * interRaceCrossoverDouble - can be used for interger or double data types.
     * double crossover reqires 2 parents and generate 2 offspring
     * process: the original genes of parents are moved closer to each other until
     * the better or same ofsp.e. (less or equal violations) is reached. the number
     * of moves is determined by  move parameter
     * @param move - number of maximum moves until the better/same solution is reached
     * @param candidates Two parents
     * @return Two offspring
     * @throws UnsupportedDataTypeException
     */    
    private ArrayList<Chromosome> interRaceCrossoverDoubleStagnant(final int MOVE, final ArrayList<Chromosome> candidates) 
            throws SolutionFoundException, UnsupportedDataTypeException, Exception{
        ArrayList<Double> delta;
        ArrayList<Double> newDelta;
        ArrayList<Chromosome> offspring = new ArrayList<Chromosome>();
        Chromosome childChrome = null;
        ArrayList<Chromosome> tempMut;

        if (candidates.size() != 2){
            throw new UnsupportedOperationException("Require only 2 parents");
        }
      
        Chromosome p1 = null, p2=null, pTowardsBest = new Chromosome(this.userInput_.solutionBy, this.userInput_); //parent 1 and parent 2;
        int pickedCons = -1;
        int randPickIdx = 0; //0 is always available... worst case can be empty..
        final int highestValidCSPidx = maxIdxAvailCSPsols();
        int k;
        int trials = 0;
        boolean bMoved = false;  
        ArrayList<Double> initDelta;
        //neighborhood = (ArrayList<Double>)Collections.unmodifiableList(initDelta);
        ArrayList<Double> neighborDelta;
        ArrayList<Chromosome> hospital = new ArrayList<Chromosome>();
        boolean bCondition;
        ArrayList<Double> prevVals, newVals;
        
        ArrayList<String> permutes;   
        ArrayList<ArrayList<Double>> combinations;
        //ArrayList<Double> dims = new ArrayList<Double>(); //dimensions..
        double dtemp;
        boolean isInvalid = false;
        double coe = 0.5;

        for (int j = 0; j < this.tourSize_; j++) {

            p1 = candidates.get(j);
            
            if(!bOptimizationMode){
                if(highestValidCSPidx >= 0 && Math.random()<FORCED_PERCENT && !p1.isSolution() && p1.getRankComponents().size()>0){                        
                    pickedCons = MyRandom.randperm(0, p1.getRankComponents().size()-1).get(0);
                    pickedCons = p1.getRankComponents().get(pickedCons);

                    p2 = new Chromosome(this.userInput_.solutionBy,  userInput_);
                    randPickIdx = MyRandom.randperm((int)Math.floor(0.5*highestValidCSPidx), highestValidCSPidx).get(0);
                    p2.setVals(CSPsols.get(randPickIdx).get(pickedCons));   

                }else{
                    if(highestValidCSPidx == -1 && !p1.isSolution() && p1.getRankComponents().size()>0){ //I use then when it is difficult to find CSP
                        for (int i = 0; i < p1.getRankComponents().size(); i++) {
                            pickedCons = p1.getRankComponents().get(i);


                            if(CSPsols.get(0).get(pickedCons).isEmpty()){
                                p2 = null;
                                continue;
                            }else{
                                p2 = new Chromosome(this.userInput_.solutionBy,  userInput_);
                                p2.setVals(CSPsols.get(0).get(pickedCons));
                                break;
                            }
                        }
                    }                               

                    if(p2 == null)
                        p2 = candidates.get((j+1)%tourSize_);  
                }            
            }else{
                if(p2 == null)
                    p2 = candidates.get((j+1)%tourSize_);
            }

            
            p1.isMarriageCompatible(p2);
            
            initDelta = new ArrayList<Double>(MyMath.vectorSubtraction(p2.getValsCopy(), p1.getValsCopy()));
            hospital.clear();
            combinations = MyMath.getXrandomBinPermutes(userInput_.totalDecisionVars, MaxComb);
            
            
            isInvalid = false;
            delta = initDelta;
            
            for(ArrayList<Double> dims: combinations){
                isInvalid = false;
                                
                k = 0;
                prevVals = null;
                newVals=null;

                for (k = 1; k <= MOVE; k++){ //) MOVE; k++) {
                    bMoved = false;
                    //find which direction to MOVE?
                    childChrome = new Chromosome(this.userInput_.solutionBy, this.userInput_);
                    newDelta = MyMath.constMultiplicationToVector(Math.pow(bringCloserRatio,k), delta); 


                    newVals = MyMath.vectorAddition(p1.getValsCopy(), newDelta);
                    
                    double fi = 0.0;
                    if(userInput_.totalDecisionVars > 21){
                        fi = 0.9;
                    }else if(userInput_.totalDecisionVars > 7){
                        fi = Math.log(0.5 + 0.1*userInput_.totalDecisionVars);
                    }else{
                        fi = 0.0;
                    }
                    fi = MyMath.roundN(fi, 1);

                    childChrome.setVals(newVals);

                    bCondition = childChrome.getRank()<=p1.getRank() || bStagnant;
                    
                    if(bCondition){
                        hospital.add(childChrome);
                        if(childChrome.isSolution()){
                            //bestSoFar = childChrome;//.clone()                                    
                            //throw new SolutionFoundException("Sol found during crossover...");
                        }
                        bMoved = true;
                        break;
                    }
                    
                    if(!childChrome.myParent(p1)){ //there is a gap/black hole so no point searching further.
                        bMoved = true;
                        break;
                    }
                    
                    
                    prevVals = newVals;
                }

                //chk boundary...
                
                neighborDelta = MyMath.vectorMultiplication(true, dims, initDelta);                
                p2 = new Chromosome(this.userInput_.solutionBy, this.userInput_);
                p2.setVals(MyMath.vectorAddition(p1.getValsCopy(), neighborDelta));  
                delta = neighborDelta; //MyMath.vectorSubtraction(p2.getValsCopy(), p1.getValsCopy());  
                
                p1.isMarriageCompatible(p2);
            }
            
            if(!hospital.isEmpty()){
                //Collections.sort(hospital); 
                hospital = sortTwice(Chromosome.BY_VIOLATIONS, Chromosome.BY_FITNESS,hospital, Math.min(MaxHospital, hospital.size()));            
                for (int i = 0; i < Math.min(MaxHospital, hospital.size()); i++) {
                    offspring.add(hospital.get(i)); //(Chromosome)hospital.get(0).clone());
                }             
                hospital.clear();
            }            
        }    
        return offspring;
    }
 
    /**
     * Mutate the given set of offspring.
     * @param offspring mutation applied only to offspring
     */
    private void mutation(ArrayList<Chromosome> offspring) throws UnsupportedDataTypeException, SolutionFoundException{
        if(offspring.isEmpty()){
            return;
        }
        if(!userInput_.doMutation){
            return;
        }

         //update the offspring
        if(this.dataType_.contains("Integer")){
            mutationInteger(offspring);
        }
        else if(this.dataType_.contains("Double")){
            mutationDouble(offspring);
        }else{
            throw new UnsupportedDataTypeException("Only supports Integer and Double data type");
        }
    }

     /**
     * mutationDouble only mutate Doubles. It uses Polynomial Mutation as described in NSGA - II <br>
     * <B>Note</B> that offspring ArrayList is updated here.
     * @param offspring offspring generated after crossover.
     */
    private void mutationDouble(ArrayList<Chromosome> offspring) throws SolutionFoundException{
        int size = offspring.size();
        ArrayList<Integer> randInts;
        Chromosome temp;
        double val;
        double rand;
        double add;
        int muteBits = (int)Math.ceil(0.1*userInput_.totalDecisionVars); //10%
        
        for (int i = 0; i < size; i++) {
            
            try{
                if(Math.random()< MUTATION_RATE){//1.0/userInput_.totalDecisionVars){
                    randInts = MyRandom.randperm(0, size-1);
                    temp = offspring.get(randInts.get(0));                   

                    for (int j : MyRandom.randperm(0, userInput_.totalDecisionVars-1).subList(0, muteBits)){                   
//                    for (int j = 0; j < userInput_.totalDecisionVars; j++) {
                        val = temp.getVals(j);
                        rand = Math.random();
                        if(rand<0.5)
                            add = Math.pow(2.0*rand,1.0/(MUM+1)) -1;
                        else
                            add = 1- Math.pow(2.0*(1-rand),1.0/(MUM+1));

                        val = val+add;

                        if(val>userInput_.maxVals.get(j))
                            val = userInput_.maxVals.get(j);
                        else if(val<userInput_.minVals.get(j))
                            val = userInput_.minVals.get(j);

                        temp.replaceVal(j, val); 
                    }                
                }  
            }catch(SolutionFoundException sfe){
                throw sfe;
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    
    private void mutationSwap(Chromosome ch, double maxSwap, final double maxIteration) throws Exception{
        ArrayList<Integer> randVal; // = MyRandom.randperm(0, ch.getValsCopy().size()-1);
        ArrayList<Double> vals = new ArrayList<Double>();
        double val0, val1;
        int lowIdx, hiIdx;
        //double maxSwap = 1; //0.05*ch.getValsCopy().size();
//        final double maxIteration = 4;
        double bfFitness, afFitness;
        
        if(ch.getValsCopy().size()<2){
            return;
        }
        
        bfFitness = ch.getFitnessVal(0);
        
        for (int j = 0; j < maxIteration; j++) {
            for (int i = 0; i < maxSwap; i++) {
                randVal = MyRandom.randperm(0, ch.getValsCopy().size()-1);

                lowIdx = randVal.get(0);
                hiIdx = randVal.get(1);

                if(lowIdx > hiIdx){
                    lowIdx = randVal.get(1);
                    hiIdx = randVal.get(0);
                }

                val0 = ch.getVals(lowIdx);
                val1 = ch.getVals(hiIdx);

                vals.add(val0);
                vals.add(val1);

                try {
                    ch.remove(lowIdx);
                    ch.remove(hiIdx-1);
                    //ch.appendVal(val1);
                    //ch.appendVal(val0);
                } catch (Exception e) {
                    e.printStackTrace();    
                }   
            }

            for (Double v : vals) {
                ch.appendVal(v);
            }
        
            afFitness = ch.getFitnessVal(0);
            
            if(afFitness!=bfFitness){
                if(afFitness<bfFitness){
                    afFitness = afFitness;
                }
                break;
            }
        }
    }
    
    private void mutationSwapNew(Chromosome ch, double maxSwap){
        ArrayList<Integer> randVal; // = MyRandom.randperm(0, ch.getValsCopy().size()-1);
        ArrayList<Double> vals = new ArrayList<Double>();
        double val0;
        int lowIdx;
        //double maxSwap = 1; //0.05*ch.getValsCopy().size();
//        final double maxIteration = 4;
        
        if(ch.getValsCopy().size()<2){
            return;
        }
        
        
        for (int i = 0; i < maxSwap; i++) {
            randVal = MyRandom.randperm(0, ch.getValsCopy().size()-1);

            lowIdx = randVal.get(0);               

            val0 = ch.getVals(lowIdx);
            vals.add(val0);

            try {
                ch.remove(lowIdx);
            } catch (Exception e) {
                e.printStackTrace();    
            }   
        }

            
        ch.tryForcedCSPsolUpdate();
       
    }
    
    private void mutationGroupSwap(Chromosome ch) throws Exception{
        ArrayList<Integer> randVal = MyRandom.randperm(0, ch.getSatisfaction().size()-1);
        int Idx0, Idx1;
        int loc;
        ArrayList<ArrayList<Integer>> temp = new ArrayList<ArrayList<Integer>>();
        final int sz = ch.getSatisfaction().size();
        
        if(Math.random()<0.1){
            loc = MyRandom.randperm(0, sz -1).get(0);
            
            for (int i = 0; i < sz; i++) {
                temp.add(ch.getSatisfaction().get(i));
            }
            
            for (int i = 0; i < sz; i++) {
                ch.getSatisfaction().set(i,temp.get((i+loc)%sz));
            }
           
        }else{
            Idx0 = randVal.get(0);
            Idx1 = randVal.get(1);

            ArrayList<Double> list0 = ch.getSatisfaction().get(Idx0);
            ArrayList<Double> list1 = ch.getSatisfaction().get(Idx1);

            ch.getSatisfaction().set(Idx0, list1);
            ch.getSatisfaction().set(Idx1, list0);
        }

        //ch.refreshValVsConstIdx();       
        ch.refreshFitness();        
    }
    
    /**
     * METHOD IS NOT TESTED. TEST IT FIRST BEFORE USE.
     * mutationInteger only mutate integers. It uses swap elements technique. 
     * so that it disrupts order more to get new allel values<br>
     * <B>Note:</B> that offspring ArrayList is updated here but rank will remain
     * same because swapping satisfaction value will produce same result. It may
     * only give different results in crossover.
     * @param offspring offspring generated after crossover.
     */
    private void mutationInteger(ArrayList<Chromosome> offspring){
        ArrayList<Integer> randDim;
        ArrayList<Integer> randVal;
        Double temp = 0.0;
        int muteBits;
        
        //System.out.println("testing... " + offspring);

        if(userInput_.domainVals == null || userInput_.domainVals.isEmpty()){ //mutation not supported
            return;
        }

        if(externalData_ == null){ //currently works only for external data
            return;
        }
        
        //Technique 1: swapping values
        //<<
//        for (int ofsp = 0; ofsp < offspring.size(); ofsp++) {            
//            if(Math.random()<1.0/offspring.get(ofsp).getValsCopy().size()){
//                //Only deal with valid values...
//                randDim = MyRandom.randperm(0, offspring.get(ofsp).getRankComponents().size()-1);                
//                
//                if(randDim.size()<2){ //swapping not possible
//                    continue;
//                }else{
//                    holdVals = (ArrayList<Double>)offspring.get(ofsp).getValsCopy().clone();
//                    holdVals.set(randDim.get(0), offspring.get(ofsp).getValsCopy(randDim.get(1)));
//                    holdVals.set(randDim.get(1), offspring.get(ofsp).getValsCopy(randDim.get(0)));                    
//                    offspring.get(ofsp).setVals(holdVals);                    
//                }
//            }
//        }
        //>>
        
        
        ArrayList<Double> vals;
        //ArrayList<Double> noGoods; 
        int expectedVal;
        
        
        
        //Technique 2: mutate a given value from available domain value;
        //<<
//        for (int ofsp = 0; ofsp < offspring.size(); ofsp++) {  
        for (Chromosome offsp : offspring) {                    
            if(Math.random()<MUTATION_RATE){ //1.0/userInput_.totalDecisionVars){ //>1.0/offspring.get(ofsp).getValsCopy().size() || bStagnant){
                               
                vals = offsp.getValsCopy();
                Collections.sort(vals);
                expectedVal = 0;

                vals.clear();
                if(offsp.noGood.isEmpty()){
                    continue; //nothing to replace with
                }
                
                if(offsp.getValsCopy().size()<2){ //swapping not possible
                    continue;
                }else{
                    if(userInput_.domainVals == null){
                        continue;
                    }else if(userInput_.domainVals.isEmpty()){
                        continue;
                    }
                    
                    muteBits = 1;
                    if(bStagnant){
                        muteBits = Math.max(1,(int)(offsp.getValsCopy().size()*0.2));
                    }
                    //randVal = MyRandom.randperm(0,offsp.noGood.size()-1);
                    
                    for (int j = 0; j < muteBits && j<offsp.noGood.size(); j++) {
                        
                        randVal = MyRandom.randperm(0,offsp.noGood.size()-1);
                        
                        if(bStagnant){ //Important... must refresh in every iteration....
                            muteBits = Math.max(1,(int)(offsp.getValsCopy().size()*0.2));
                        }

                        //Only deal with valid values...
                        randDim = MyRandom.randperm(0, offsp.getValsCopy().size()-1);
                        
                        if(randDim.get(0) >= offsp.getValsCopy().size()){
                            System.err.println("NOT possible. Please check...");
                            System.out.println(vals);
                        }
                        try{
//                            randVal = MyRandom.randperm(0,userInput_.domainVals.get(randDim.get(0)).size()-1);
//                            randVal = MyRandom.randperm(0,noGoods.size()-1);
                            if(!externalData_.isHighlyConstrained(offsp.getVals(randDim.get(0)).intValue())) //in optimization mode noGood is empty so automatically this won't be executed.
                                offsp.replaceVal(randDim.get(0),offsp.noGood.get(randVal.get(0)));

//                            int prevVal =  vals.get(randDim.get(0)).intValue();
//                            for (int k = 0; k < userInput_.domainVals.get(prevVal).size(); k++) {
//                                temp = userInput_.domainVals.get(prevVal).get(randVal.get(k));
//                                if(vals.get(randDim.get(0)) != temp){                                
//                                    offspring.get(ofsp).replaceVal(randDim.get(0), temp);//Be warned! may create duplicate values..
//                                    break;
//                                }   
//                            }
                        
                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        
                    }
                             
                }
            }
        }
        //>>
    }

    private void sortAndReplace(int gen) throws Exception, SolutionFoundException{
        if (userInput_.dataType.contains("Integer")){
//            noViolationSortAndReplace(gen); //duplicateSatisfactionSortAndReplace();
            ;
        }else if (userInput_.dataType.contains("Double")){
            noViolationSortAndReplaceDouble(gen);
        }
        else{    
            throw new Exception("Incorrect use of data types");
        }
    }
 
    
    private void noViolationSortAndReplaceDouble (int gen) throws Exception{
        ArrayList<Chromosome> sols = new ArrayList<Chromosome>();
        ArrayList<Chromosome> nonSols = new ArrayList<Chromosome>();

        final int funcionalConstraints = userInput_.maxDynamicTime+1; //userInput_.totalConstraints - userInput_.totalDecisionVars+1;
        ArrayList<ArrayList<Chromosome>> grouping = new ArrayList<ArrayList<Chromosome>>();
        int front2[] = new int[2];

        if(bOptimizationMode)
            curBest_ = MyMath.roundN(bestSoFar.getFitnessVal(0),3);
        else
            curBest_ = bestSoFar.getRankComponents().size();
                    
        if(prevBest_ == curBest_){
            stillSameBestCount++;
        }else{
            stillSameBestCount = 0;
        }          
        
        if(stagnantVisit >= 10 || stillSameBestCount == 0){
            stillSameBestCount = 0;
            bStagnant = false;
            stagnantVisit = 0;
        }        
        
        
        Chromosome ch; 
        for (int i = 0; i < chromosomes_.size(); i++) {
            ch = chromosomes_.get(i);
            ch.age++;
            
            if(Chromosome.tmpSortBy != userInput_.solutionBy){
                System.out.println("Error....");
            }
            
            if(ch.isSolution()){
                sols.add(ch);
                chromosomes_.remove(i);
                i--;
            }else{
                nonSols.add(ch);
                chromosomes_.remove(i);
                i--;
            }
        }

        chromosomes_.clear();
               
//        final int maxSolPop = (int)(userInput_.population*0.25);
//        final int maxNonSolPop = userInput_.population-maxSolPop;
//        
        int solPop = Math.min(maxSolPop,sols.size());
        int nonSolPop = Math.min(maxNonSolPop,nonSols.size()); //userInput_.population - solPop;
        
        if(solPop < maxSolPop){
            nonSolPop = userInput_.population - solPop;
        }else if(nonSolPop <maxNonSolPop){
            solPop = userInput_.population - nonSolPop;
        }else if (solPop + nonSolPop != userInput_.population){         
            if(sols.size() > solPop){
                solPop++;
            }else if(nonSols.size() > nonSolPop){
                nonSolPop++;
            }else{
                System.err.println("population size error mate on noViolationSortAndReplace.");
                Application.getInstance().exit();   
            }   
        }
                    
        if(!sols.isEmpty()){
            setbOptimizationMode(true);           
            this.maxCSPval = sols.get(sols.size()-1).getFitnessVal(1);                                        
            //throw new SolutionFoundException("found reee...");
        }

        if(sols.size()>1){
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
            final double maxVal = Collections.max(sols).getFitnessVal(0);
            final double minVal = Collections.min(sols).getFitnessVal(0);
            final int range = (int)Math.max(maxVal-minVal, 1);
            final int slots = (int)Math.min(range,10);

            sols = categorizeChromesList(slots, sols, solPop, SORT_FITNESS_THEN_NOVELTY, rhoCOP, null, false);
        }
        try{ 
            if(!nonSols.isEmpty()){                        
                nonSols = categorizeChromesList(funcionalConstraints,nonSols, nonSolPop, SORT_HARDCONSVIOS_THEN_RHO, rhoCOP, null, false);
                                       
                if(!bOptimizationMode){
                    if(nonSols.get(0).getFitnessVal(0)<bestSoFar.getFitnessVal(0))
                        CspProcess.upgradeBestSoFar(nonSols.get(0)); //It is not necessary that get(0) it the best accroding to fitness val
                                                                    // but it donsen't matter in case of CSP...
                }

                if(!bOptimizationMode && stillSameBestCount >= SAME_BEST_GENERATIONS){// && externalData_.getCurPref()>= externalData_.maxPref())){
                    bStagnant = true;                 
                    stagnantVisit++;           
                    
                    if(stagnantVisit == 1){
                        System.out.println("*** temp -- removed...");
                        ArrayList<Double> nearestNeighbor = null;
                        tabuDist = -1.0;
                                            
                        if(grouping.size()>=2){
                            if(grouping.get(front2[0]).size()>0 && grouping.get(front2[1]).size()>0){ //obviously... huun..
                                tabuDist = Math.abs(MyMath.norm(grouping.get(front2[0]).get(0).getValsCopy(), 
                                        grouping.get(front2[1]).get(0).getValsCopy(),MyMath.DIST_EUCLEADIAN));
                            }
                        }    
                        
                        
                        if(tabuDist>0){
                            System.out.println("Tabu contraint added");
                            addDynamicConstraint(grouping.get(front2[0]).get(0).getValsCopy(), tabuDist);                        
                        }
                        for (Chromosome c : nonSols) {
                            c.refreshFitness();
                        }
                    }
                }
            }
        } catch(Exception e){
            throw e;
        }    
        
  

        chromosomes_.clear(); //just to be safe;
        chromosomes_.addAll(sols);
        chromosomes_.addAll(nonSols);
            
        if(chromosomes_.size() != userInput_.population){
            System.err.println("population size error on noViolationSortAndReplace.");
            Application.getInstance().exit();
        }

        Chromosome.tmpSortBy = userInput_.solutionBy;
         
        prevBest_ = curBest_;
        if(bStagnant){
            randomDeath(gen, 1, true); //spacre top one            
        }
        else            
            randomDeath(gen, (int)(userInput_.population*0.03),false);//,true     
        
        
        if((gen%gensEachConstraints == 0 && bestSoFar.isSolution())){//bestSoFar.getRankComponents().size() == 0) /*&& dynamicTime<9*/ ){
//            System.out.println("****best before change: "+bestSoFar);
            if(getDynamicTime() < userInput_.maxDynamicTime){//userInput_.totalConstraints-userInput_.totalDecisionVars){ // related with allDynamicConstraintsSolved();
                dynamicTimeIncrement();                          
            
                for (Chromosome c : chromosomes_) {
                    c.refreshFitness();
                }

                suspended_.clear();
                setbOptimizationMode(false);
                stillSameBestCount = 0;
                bStagnant = false;
                stagnantVisit = 0;

                Collections.sort(chromosomes_);
                CspProcess.upgradeBestSoFar(chromosomes_.get(0));  
            }
        }
        
        
        System.out.println("debug last best: " + (gGen - gGenLastBestOn));
        System.out.println("prevbest: " + prevBests.curSize());
        
        if(bOptimizationMode && allDynamicConstraintsIncluded() && gGen - gGenLastBestOn>gGenTrackStruggle
                && prevBests.curSize() > 0 && bestSoFar.isSolution()){ /////////////////////////////////////??????????????????????????????????????
             System.out.println("Opt Tabu contraint added");
             tabuDist = Math.abs(MyMath.norm(bestSoFar.getValsCopy(), 
                                        prevBests.dequeue().getValsCopy(),MyMath.DIST_EUCLEADIAN));
             
//             final int knownOptSolDP = 4;
//            for (int i = 0; i < sols.size(); i++) {
//              if(MyMath.roundN(chromosomes_.get(i).getFitnessVal(0), knownOptSolDP) == MyMath.roundN(bestSoFar.getFitnessVal(0),knownOptSolDP)){
//                  chromosomes_.remove(i);
//                  i--;
//                  chromosomes_.add(initializeChromosomeRandomly());
//              }else{
//                  break;
//              }  
//            }  
                         
             addDynamicConstraint(bestSoFar.getValsCopy(), tabuDist);                         
        }                
        
        
        if(bOptimizationMode && allDynamicConstraintsIncluded()){
            bTabuMode = true; //once true then remain true forever ever..ever..
        }
    }
    
    
        
    /**
     * @param list - (Pass by value). input list will be destroyed. Get the returned list
     * @param listPop
     * @param minVal
     * @param maxVal
     * @param categorizeBy
     * @param a 1 is preferred can use lesser values as well
     * @param ro current test results shows no difference in picking any value
     * @param grpIdx (Pass by Ref) - indices in final list indicating starting indices of groups/slots
     */
     private ArrayList<Chromosome> categorizeChromesList(final int slots, ArrayList<Chromosome> list, final int listPop, 
        final int categorizeBy, final double ro, ArrayList<Integer> grpIdx, final boolean debugPrint){
         
        //System.out.println("<<catgorizing...>>: " + list.size());
        
        int slotSize;
        int FirstSlotSize;
        int empty;
        int incompleteSlots;
        int slotAddition;
        int vios;
        ArrayList<ArrayList<Chromosome>> grouping = new ArrayList<ArrayList<Chromosome>>();
        
        int front2[] = new int[2];

        if(list.isEmpty()){
            return list;
        }     
        
        if(categorizeBy == SORT_HARDCONSVIOS_THEN_FITNESS){
            list = sortTwice(Chromosome.BY_VIOLATIONS, Chromosome.BY_FITNESS, list, list.size()); //Math.min(list.size(),listPop*2)); 
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        }else if(categorizeBy == SORT_HARDCONSVIOS_THEN_RHO){
            list = sortTwice(Chromosome.BY_VIOLATIONS, Chromosome.BY_FITNESS, list, list.size()); //Math.min(list.size(),listPop*2)); 
            Chromosome.tmpSortBy = Chromosome.BY_RO;
        }else if(categorizeBy == SORT_SATISFACTION){
            Chromosome.tmpSortBy = Chromosome.BY_SATISFACTIONS;
            Collections.sort(list);
//            list = new ArrayList<Chromosome>(list.subList(0, Math.min(list.size(),listPop*2)));
        }else if(categorizeBy == SORT_FITNESS){ //depends on the current Chromosome.tmpSortBy specified by the caller             
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
            Collections.sort(list);
//            list = new ArrayList<Chromosome>(list.subList(0, Math.min(list.size(),listPop*2)));
        }else if (categorizeBy == SORT_FITNESS_THEN_NOVELTY){
            list = sortTwice(Chromosome.BY_FITNESS, Chromosome.BY_RO, list, list.size()); //Math.min(list.size(),listPop*2)); 
//            list = sortFitnessThenNovelty(list, Math.min(list.size(),listPop*2));
            Chromosome.tmpSortBy = Chromosome.BY_FITNESS;
        }
               
        slotSize = listPop/(slots); //last one for infeasibles as well
        FirstSlotSize = slotSize + (listPop - slotSize*(slots));
        empty = 0;
        incompleteSlots = 0;

        for (int i = 0; i < slots; i++) {//funcionalConstraints
            grouping.add(new ArrayList<Chromosome>());
        }      
        
        vios = -1;                
//                    for (Integer i : MyRandom.randperm(0, nonSols.size()-1)) {
        for(int i = 0; i<list.size(); i++){
            vios = Math.min(slots-1,list.get(i).getRankComponents().size()); //1 to total constraints
            grouping.get(vios).add(list.get(i));
        }
        
        Chromosome.tmpSortBy = userInput_.solutionBy;
        for (ArrayList<Chromosome> grp : grouping) {
            Collections.sort(grp);
        }

        int fr = 0;                                        
        for (int i = 0; i < grouping.size(); i++) {
            ArrayList<Chromosome> g = grouping.get(i); 

            if(g.size()>0 && fr < 2){
                front2[fr++] = i;
            }                        
        }

        empty = 0;
        incompleteSlots = 0;
        for (int i = 0; i < grouping.size(); i++) {
            if(grouping.get(i).size()<slotSize){
                empty += slotSize-grouping.get(i).size();
                incompleteSlots++;
            }
        }

        slotAddition = empty/(slots-incompleteSlots); //empty slot space has to be distributed to filled/partially filled slots 
        slotSize += slotAddition;
        FirstSlotSize += slotAddition;
        FirstSlotSize += empty - (slots-incompleteSlots)*slotAddition;

        ArrayList<Chromosome> additionals = new ArrayList<Chromosome>();
//        ArrayList<ArrayList<Chromosome>> additionals = new ArrayList<ArrayList<Chromosome>>();
//         for (int i = 0; i < slots; i++) {
//             additionals.add(new ArrayList<Chromosome>());
//         }

        ArrayList<Integer> tmpIdx;
        ArrayList<Chromosome> chTmp;
        int count;
             
        boolean bFirstSlotAdded = false;
        for (int i = 0; i < grouping.size(); i++) {
            if(!bFirstSlotAdded && grouping.get(i).size() >= FirstSlotSize){  
                
                //<<.... 
//                tmpIdx = MyMath.linearFnSelection(grouping.get(i), FirstSlotSize, debugPrint);
                tmpIdx = MyMath.negExpFnSelection(grouping.get(i).size(), FirstSlotSize, ro, debugPrint);
                chTmp = new ArrayList<Chromosome>();

                for (int j = 0; j < tmpIdx.size(); j++) {
                    chTmp.add(grouping.get(i).get(tmpIdx.get(j)));                    
                }                                
                
                if(grouping.get(i).size() > FirstSlotSize ){
                    count = 0;
                    for (int j = 0; j < grouping.get(i).size(); j++) {
                        if(count<tmpIdx.size()){
                            if(j==tmpIdx.get(count).intValue()){
                                count++;
                                continue;
                            }
                        }
//                        additionals.get(i).add(grouping.get(i).get(j));   
                        additionals.add(grouping.get(i).get(j));
                    }    
                }
                grouping.set(i, chTmp);
                //>>...                

                bFirstSlotAdded = true;
                continue;
            }
            
            //<<
                tmpIdx = new ArrayList<Integer>();
                chTmp = new ArrayList<Chromosome>();
                int sz = Math.min(slotSize,grouping.get(i).size());                                
                
//                tmpIdx = MyMath.linearFnSelection(grouping.get(i), sz, debugPrint);
                tmpIdx = MyMath.negExpFnSelection(grouping.get(i).size(), sz, ro, debugPrint);
                
                for (int j = 0; j < tmpIdx.size(); j++) {
                    chTmp.add(grouping.get(i).get(tmpIdx.get(j)));                    
                }
                                
                if(grouping.get(i).size() > slotSize ){
                    count = 0;
                    for (int j = 0; j < grouping.get(i).size(); j++) {
                        if(count<tmpIdx.size()){
                            if(j==tmpIdx.get(count).intValue()){
                                count++;
                                continue;
                            }
                        }
//                        additionals.get(i).add(grouping.get(i).get(j));    
                        additionals.add(grouping.get(i).get(j));
                    }    
                }
                grouping.set(i, chTmp);
            //>>
            
        }

        list.clear(); 
        if(grpIdx == null){
            grpIdx = new ArrayList<Integer>(); //NOTE pass-by-ref distroyed here. Code for grpIdx below are now USELESS                    
        }
        
        grpIdx.add(0);//first index obviously.
        for (int i = 0; i < grouping.size(); i++) {
            ArrayList<Chromosome> g = grouping.get(i); 
            list.addAll(g);
            grpIdx.add(list.size());//next index
        }
        grpIdx.remove(grpIdx.size()-1);//last one is invalid it is size+1. there is no next after size().
        
        list.addAll(additionals.subList(0, listPop-list.size())); 

        Chromosome.tmpSortBy = userInput_.solutionBy;  
        
        return list;
    }
    
   
   
    
    private void addDynamicConstraint(ArrayList<Double> center, double radius){ 
        negFeasibleRange = 0;
        //dynamicConstraintNo = 0;
        userInput_.totalConstraints++;
        //MAX_FUNCTIONAL_CONSTRAINTS = userInput_.totalConstraints - userInput_.totalDecisionVars;
        
        
        CCSPfns.addTabuConstraint(center, radius); 
        
        try {
            for (Chromosome c : chromosomes_) {
                c.restructure(1, true);
            }
            if(!bTabuMode)
                bestSoFar.restructure(1, true);
            prevBests.clearAll();
            
        } catch (SolutionFoundException ex) {
            ex.printStackTrace();
        }
    }
    

    
    /**
     * NOTE: This function <B>DOES NOT</B> sort the chromosomes inside a ranked group.
     * If it has <I>n</I> ranks/groups, it only tries to give best ranked chromosomes,
     * then the leftovers are <B>ONLY</B> sorted according to fitness.
     * You must sort each ranked groups separately afterwards.
     * @param in
     * @param size
     * @return 
     */
    private ArrayList<Chromosome> sortTwice(int firstSortType, int SecondSortType, final ArrayList<Chromosome> in, final int size){        
        Chromosome.tmpSortBy = firstSortType; //Chromosome.BY_VIOLATIONS;
        Collections.sort(in);
        
        ArrayList<Chromosome> out = null;
        final int maxAcceptedRank = in.get(size-1).getRankComponents().size(); //violations
         
        int safePointer = -1;
        Chromosome chrome;
        ArrayList<Chromosome> temp = new ArrayList<Chromosome>();
        
        try{
            if(in.size()<=1 || in.size() <= size){
                out = in;
                throw new ExecutionException(null);
            }

            for (Chromosome chromosome : in) {
                if(chromosome.getRankComponents().size() == maxAcceptedRank){
                    chrome = (Chromosome)chromosome.clone();
//                    chrome.tempSortBy = userInput_.solutionBy ;//by fitness
                    temp.add(chrome); //exract chromosomes with max accepted violations.
                }else if(chromosome.getRankComponents().size() < maxAcceptedRank){
                    safePointer++;
                }else{
                    break;
                }
            }


            if(size <= safePointer+1){
                out = new ArrayList<Chromosome>(in.subList(0, size));//not get only required sorted ones.  
            }else{
                out = new ArrayList<Chromosome>(in.subList(0, safePointer+1));//not get only required sorted ones.                        
                Chromosome.tmpSortBy = SecondSortType; //Chromosome.BY_RO; //userInput_.solutionBy;
                if(!temp.isEmpty()){
                    temp = temp;
                }
                Collections.sort(temp);
                out.addAll(temp.subList(0, size-safePointer-1));                
                out = new ArrayList<Chromosome>(out.subList(0, size));
            }
        
        }catch(ExecutionException ee){
            out = out;
        }
        catch(Exception e){
            e.printStackTrace();
            out = out;
        }

        Chromosome.tmpSortBy = userInput_.solutionBy;
        return out;
    }
   
    /**
     * yoni..
     * @param gen
     * @param spareSize
     * @param bImportSuspended
     * @throws SolutionFoundException 
     */
    private void randomDeath(int gen, int spareSize, boolean bImportSuspended) throws SolutionFoundException{
        int d;
        ArrayList<Chromosome> newRandPop = new ArrayList<Chromosome>();
        d = (int)Math.round(this.REPLACE_PERCENT*userInput_.population);
        
        try {
            initializeChromosomes(newRandPop, d, gen);             
        } catch (Exception e) {
            e.printStackTrace();
            Application.getInstance().exit();
        }

        int totChromesRemoved = 0;
        Chromosome ch;

        for(int i = chromosomes_.size()-1; i> spareSize; i--){ 
            if(suspended_.size()<userInput_.population && chromosomes_.get(i).isSolution() && allDynamicConstraintsIncluded())
                suspended_.add(chromosomes_.get(i)); //getting reference? it is ok as it will be deleted below.
            
            if(suspended_.size()>2*d){
                ch = suspended_.remove();;
                chromosomes_.set(i,ch); 
                totChromesRemoved++;
            }
            if(totChromesRemoved >= d){
                break;
            }
        }        
    }  
} //End of class definition
