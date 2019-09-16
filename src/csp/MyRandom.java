/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

/**
 *
 * @author Anurag Sharma
 */
public class MyRandom extends Random{
    public MyRandom(){
        super();        
    }
    
     /**
     * randVal() method randomly generates the sequence.
     *
     * @param minVal = minimum value of sequence INCLUSIVE
     * @param maxVal = maximum value of sequence INCLUSIVE
     * @return - Integer value between minVal to maxVal
     */
    public Double randVal(int minVal, int maxVal){
        //Random r = new Random();
        int rand;
        
        if(minVal > maxVal)
            throw new ArithmeticException();

        rand = minVal+this.nextInt(maxVal-minVal+1);              
        return (double)rand;
        
    
    }

    /**
     * randVal() method randomly generates the sequence.
     *
     * @param minVal = minimum value of sequence INCLUSIVE
     * @param maxVal = maximum value of sequence INCLUSIVE
     * @return - Double value between minVal to maxVal
     */
    public Double randVal(Double minVal, Double maxVal){
        //Random r = new Random();
        Double rand;

        if(minVal > maxVal)
            throw new ArithmeticException();

        rand = minVal + (maxVal - minVal)*this.nextDouble();
        return rand;
    }
    
    /**
     * randperm - Similar to randperm of Matlab
     * Generates random integer sequence from minVal inclusive to maxVal inclusive
     * @param minVal - minimum value (inclusive) of the sequence requested
     * @param maxVal - maximum value (inclusive) of the sequence requested
     * @return ArrayList<Integer> of random sequence from minVal to maxVal
     */
    public static ArrayList<Integer> randperm(int minVal, int maxVal){
        ArrayList<Integer> rand = new ArrayList<Integer>();

        if(minVal > maxVal)
            throw new ArithmeticException();

        for (int i = minVal; i <= maxVal; i++) {
            rand.add(i);
        }
        Collections.shuffle(rand);
        return rand;    
    }
}
