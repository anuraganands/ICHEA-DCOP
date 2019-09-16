/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.activation.UnsupportedDataTypeException;
import org.jdesktop.application.Application;

/**
 *
 * @author Anurag Sharma
 */
public class MyMath {
    public static final int DIST_EUCLEADIAN = 1;    
    public static final int DIST_HAMMING = 2;
    public static final int DIST_DUPLICATE_MISMATCH = 3;
    private static ArrayList<String> permutes;
    
    /**
     * NOTE: It gives unpredictable results if Double value is very large
     * and cannot be converted to long.
     * @param n
     * @param decimalPlaces
     * @return 
     */
    public static double roundN(Double n, int decimalPlaces){
        double retVal  = Double.NaN;
        if(Double.isNaN(n) || Double.isInfinite(n)){
            return n;
        }
        
        try{
            long temp = Math.round(n*Math.pow(10.0,decimalPlaces));
            retVal = temp/Math.pow(10.0,decimalPlaces);
        }catch(Exception e){            
            System.err.println("Conversion error in roundN");
            Application.getInstance().exit();
        }
        return retVal;
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     * <code>Integer.MAX_VALUE - 1</code>.
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }
    
    
    public static ArrayList<String> getBinPermutes(final int size){
        permutes = new ArrayList<String>();
        
        printBin("", size);
        return permutes;
    }

    public static ArrayList<ArrayList<Double>> getXrandomBinPermutes(final int size,final int limit){
        ArrayList<ArrayList<Double>> combinations = new ArrayList<ArrayList<Double>>();
        
         //printBin("", size);
        double pmOne; //plus minus one
        //int []bits;
        final int combSize = (int)Math.min(limit, Math.pow(2, size));
        
        for (int i = 0; i < combSize; i++) {
            combinations.add(new ArrayList<Double>());
        }
        
        for (int i = 0; i < combSize; i++) {
            //bits = new int[size];
            for (int j = 0; j < size; j++) {
                pmOne = 1.0;
                if(Math.random() < 0.5){
                    pmOne = -1.0;
                } 
                combinations.get(i).add(pmOne);          
            }            
        }
        
        
        
         return combinations;
    }
    
    private static void printBin(String soFar, int iterations) {
        if(iterations == 0) {
            permutes.add(soFar);
        }
        else {
            printBin(soFar + "0", iterations - 1);
            printBin(soFar + "1", iterations - 1);
        }
    }
    
    /**
     * multiplication of a scalar var to each element of the vector v
     * @param var - scalar multiple
     * @param v - vector
     * @return scalar multiple of the vector
     */
    public static ArrayList<Double> constMultiplicationToVector(double var, final ArrayList<Double> v){
        ArrayList<Double> result;        
        result = new ArrayList<Double>();
                
        for (int i = 0; i < v.size(); i++) {
            result.add(var*v.get(i));
        }
        
        return result;
    }
    
    
    /**
     * Vector multiplication <br>
     * @param entryByEntry true or false <br>
     * if entryByEntry is true then - entry of v1 is multiplied by same index entry of v2
     * if entryByEntry is false - CURRENTLY NOT IMPLEMENTED
     * @param v1 first vector or scalar
     * @param v2 second vector or scalar
     * @return vector multiplication
     * @throws UnsupportedDataTypeException
     */
    public static ArrayList<Double> vectorMultiplication(boolean entryByEntry, final ArrayList<Double> v1, final ArrayList<Double> v2) throws UnsupportedDataTypeException{
        ArrayList<Double> result;
        int resultSize;  
        
        resultSize = v1.size();
        if (v2.size() > v1.size()){
            resultSize = v2.size();
        }
        
        result = new ArrayList<Double>(resultSize);
        
        if(entryByEntry){
                      
               
            if (v1.size() == 1){
                if (v2.size() == 1){
                    result.add(v1.get(0)*v2.get(0));
                }
                else{
                    for (int i = 0; i < v2.size(); i++) {
                        result.add(v1.get(0)*v2.get(i));
                    }
                }
            }
            else{
                if (v2.size() == 1){
                    for (int i = 0; i < v1.size(); i++) {
                        result.add(v1.get(i) * v2.get(0));
                    }
                }
                else{
                    if(v1.size() != v2.size()){
                        throw new UnsupportedOperationException("Dimensions of both point must be same.");
                    }
                    for (int i = 0; i < v1.size(); i++) {
                        result.add(v1.get(i)*v2.get(i));
                    }
                }
            }
            return result;
        }
        return null;
    }
    /**
     * Double norm is similar to Matlab's norm. It calculates euclidean distance
     * between 2 n dimensional points.
     * @param p1 - first n dimensional point
     * @param p2 - second n dimensional point
     * @return Euclidean distance between the given 2 points is returned.
     */
    public static double norm(final ArrayList<Double> p1, final ArrayList<Double> p2, final int distanceType){
        int size;
        Double sum;
        
        if(distanceType == DIST_EUCLEADIAN){
            if(p1.size() != p2.size()){
                throw new UnsupportedOperationException("Dimensions of both point must be same.");
            }

            size = p1.size();

            sum = 0.0;
            for (int i = 0; i < size; i++) {
                sum += Math.pow(p1.get(i) - p2.get(i),2);
            }
            sum = Math.sqrt(sum);
        }else if(distanceType == DIST_DUPLICATE_MISMATCH){
            int duplicates = 0;
            int min;
            ArrayList<Double> unique = new ArrayList<Double>(p1);
            unique.addAll(p2);
            
            min = p1.size();
            if(p2.size()<p1.size())
                min = p2.size();
            
            HashSet<Double> hashSet = new HashSet<Double>(unique);
            unique = new ArrayList<Double>(hashSet);
            sum = (double)(unique.size() - min);
//            for (Double d : p1) {
//                if(!p2.contains(d)){
//                    duplicates++;
//                }
//            } 
//            for (Double d : p2) {
//                if(!p1.contains(d)){
//                    duplicates++;
//                }
//            } 
//            sum = (double)duplicates;
        }else{
            sum = Double.NaN;
        }

        return sum;
    }


    /**
    * mean - calculates mean of the given ArrayList<Double>
    * @param v - ArrayList<Double> of whom mean is to be found
    * @return return mean/average
    */
    public static double mean(ArrayList v){
        if(v.isEmpty()){
            return Double.NaN;
        }        

        double sum = 0.0;
        for (int i = 0; i < v.size(); i++) {
            sum += Double.parseDouble(v.get(i).toString());
        }
        return sum/v.size();
    }

    /**
     * Double sum does the summation of an array from index minIdx to maxIdx
     * @param obj Array to be summed up
     * @param minIdx - minimum index of the array to be included in the summation
     * @param maxIdx - maximum index of the array to be included in the summation
     * @return summation from minIdx to maxIdx
     */
    public static Double sum(Double[] obj, int minIdx, int maxIdx){
        Double sum = 0.0;
        if (maxIdx > obj.length-1)
            throw new ArrayIndexOutOfBoundsException("maxIdx is greater than array length");
        if (minIdx < 0)
            throw new ArrayIndexOutOfBoundsException("minIdx is less than 0");

        for (int i = minIdx; i <= maxIdx; i++) {
            sum += obj[i];
        }

        return sum;
    }

    /**
     * Same as overloaded method sum(Double[] obj,...) at {@link csp.MyMath}
     * @see csp.MyMath
     * @param obj
     * @param minIdx
     * @param maxIdx
     * @return
     */
     public static Integer sum(Integer[] obj, int minIdx, int maxIdx){
        Integer sum = 0;
        if (maxIdx > obj.length-1)
            throw new ArrayIndexOutOfBoundsException("maxIdx is greater than array length");
        if (minIdx < 0)
            throw new ArrayIndexOutOfBoundsException("minIdx is less than 0");

        for (int i = minIdx; i <= maxIdx; i++) {
            sum += obj[i];
        }

        return sum;
    }   
     
    public static double getEucledianDist (ArrayList<Double> p, ArrayList<Double> q)
    {
        if(p.size() != q.size()){
            throw new IllegalArgumentException("Both array should be of same dimensions");
        }
        double sumSq= 0;
        for (int i = 0; i < p.size(); i++) {
            sumSq += Math.pow(q.get(i)-p.get(i),2);
        }
        return Math.sqrt(sumSq);
    }
  
     
    /**
      * vectorAddition add 2 vectors (one dimesional matrix) of same size
      * @param m1 first vector
      * @param m2 second vector
      * @return summation of 2 vectors
      */ 
    public static ArrayList<Double> vectorAddition(ArrayList<Double> m1, ArrayList<Double> m2){
        ArrayList<Double> result;

        if(m1.size() != m2.size()){
            throw new UnsupportedOperationException("Dimensions of both vectors must be same.");      
        }
        result = new ArrayList<Double>(m1.size());

        for (int i = 0; i < m1.size(); i++) {
            result.add(m1.get(i)+m2.get(i));            
        }
        return result;
    } 
    
    
    public static ArrayList<Double> vectorSubtraction(ArrayList<Double> m1, ArrayList<Double> m2){
        ArrayList<Double> result;

        if(m1.size() != m2.size()){
            throw new UnsupportedOperationException("Dimensions of both vectors must be same.");      
        }
        result = new ArrayList<Double>(m1.size());

        for (int i = 0; i < m1.size(); i++) {
            result.add(m1.get(i)-m2.get(i));            
        }
        return result;
    } 
    
    /**
     * Find <b>Index</b> of min value of any type of given array
     * @param in ArrayList must be instance of Comparable
     * @return return index of first found minimum element
     */
    public static int minIdx(ArrayList<? extends Comparable> in){        
        Comparable minVal;
        int minIdx = 0;
        
        if(in.isEmpty()){
            minVal = -1;
        }        
        
        minVal = in.get(minIdx);
        
        for (int i = 1; i < in.size(); i++) {
            if(minVal.compareTo(in.get(i))>0){
                minVal = in.get(i);
                minIdx = i;
            }            
        }  
        
        return minIdx;
    }
    
     /**
     * Linear Form: y = mx+c where c = 0, hence y = mx
     * @param list
     * @param reqPop
     * @param debugPrint
     * @return 
     */
    public static ArrayList<Integer> linearFnSelection(final int listSize, final int reqPop, final boolean debugPrint){
        ArrayList<Integer> tmpIdx = new ArrayList<Integer>();
        
        if(reqPop <= 0){
            ;// tmpIdx is empty
        }else if(reqPop == 1){
            tmpIdx.add(0); // only first index
        }else{ //>1        
            final double m = (listSize-1)/(reqPop-1);
        
            for (int i = 0; i < reqPop; i++) {
                tmpIdx.add((int)Math.floor(m*i));
            }
        }

        return tmpIdx;
    }
    
    
    /**
     * Exponential form: <I>&alpha;e<sup>&rho;x</sup></I>
     * <BR><B>USAGE:</B> used to pick the most 'promising' chromes in exponential manner.
     * Not necessarily the 'best' ones are picked. This method is used to get the diverse
     * population. It is used in {@link CspProcess.#categorizeChromesList(java.util.ArrayList, int, double, double, int, double, java.util.ArrayList) }.
     * @param list input list whose only promising chromes are to be selected
     * @param reqPop required size of the list.
     * @param rho mathematical usage in the exponential function in <I>&alpha;e<sup>&rho;x</sup></I>
     * @return exponentially selected chromes from the sorted list provided
     * @see #expFnSelection(java.util.ArrayList, int, double) 
     */
    public static ArrayList<Integer> negExpFnSelection(final int listSize, final int reqPop, final double rho, final boolean debugPrint){
        double x,y; // x-axis values for the function Ae^rho.x
        
        ArrayList<Integer> tmpIdx = new ArrayList<Integer>(); //y-axis values for the function Ae^rho.x  
        ArrayList chTmp = new ArrayList();
        final int lastIdxInitPop = listSize-1;
        double minVal = Double.MAX_VALUE;
        for (int i = reqPop-1; i>=0; i--) {
            x = i*1.0/(reqPop-1);
            y = Math.exp(-rho*x);
            if(i == reqPop-1){
                minVal = y;
            }
            tmpIdx.add((int)Math.floor((y-minVal)*lastIdxInitPop*1.0/(1-minVal)));
        }
        
        //re allocate tmpIdx because exponential eq. might have introduced duplicates. remove duplicates
        int curMaxIdx;
        if(!tmpIdx.isEmpty()){
            curMaxIdx = tmpIdx.get(0); //first one
            for (int j = 1; j < tmpIdx.size(); j++) {
                if(tmpIdx.get(j)<= curMaxIdx){
                    tmpIdx.set(j, curMaxIdx+1);
                } 
                curMaxIdx = tmpIdx.get(j);
            }
        }
                
//        for (int j = 0; j < tmpIdx.size(); j++) {
//            chTmp.add(list.get(tmpIdx.get(j)));                    
//        }  
//        return chTmp;       
        
        return tmpIdx;
    }
    
    /**
     * Exponential form: <I>&alpha;e<sup>&rho;x</sup></I> <B>BUT</B> the value of &lt;&rho;&gt; is meaningless 
     * as this is canceled when calculating <code>pickedIdx</code>. <code>pickedIdx = </code>
     * <I>&alpha;e<sup>&rho;const/&rho;</sup> = &alpha;e<sup>const</sup></I>. <I>const</I> is a 
     * constant value calculated for calculation for all the <code>pickedIdx</code>.
     * <BR><B>USAGE:</B> used to pick the most 'promising' chromes in exponential manner.
     * Not necessarily the 'best' ones are picked. This method is used to get the diverse
     * population. It is used in {@link CspProcess.#categorizeChromesList(java.util.ArrayList, int, double, double, int, double, java.util.ArrayList) }.
     * @param list input list whose only promising chromes are to be selected
     * @param reqPop required size of the list.
     * @param alpha mathematical usage in the exponential function in <I>&alpha;e<sup>&rho;x</sup></I>
     * @return exponentially selected chromes from the sorted list provided
     * @see #negExpFnSelection(java.util.ArrayList, int, double) 
     */
    public static ArrayList<Integer> expFnSelection(final int listSize, final int reqPop, final double alpha){
        double maxExp = Math.log((listSize-1)/alpha);//corresponds to the value of grouping.get(i).size()
        maxExp = maxExp/(reqPop-1); //value of each division

        ArrayList<Integer> tmpIdx = new ArrayList<Integer>();
        ArrayList chTmp = new ArrayList();
        int pickedIdx, curMaxIdx;

        for (int j = 0; j < reqPop; j++) {
            pickedIdx = (int)Math.floor(alpha*Math.exp(j*maxExp));
            tmpIdx.add(pickedIdx);
        }
        
        //re allocate tmpIdx because exponential eq. might have introduced duplicates. remove duplicates
        if(!tmpIdx.isEmpty()){
            curMaxIdx = tmpIdx.get(0); //first one
            for (int j = 1; j < tmpIdx.size(); j++) {
                if(tmpIdx.get(j)<= curMaxIdx){
                    tmpIdx.set(j, curMaxIdx+1);
                } 
                curMaxIdx = tmpIdx.get(j);
            }
        }
                
//        for (int j = 0; j < tmpIdx.size(); j++) {
//            chTmp.add(list.get(tmpIdx.get(j)));                    
//        }          
//        return chTmp;
        return tmpIdx;
    } 
}
