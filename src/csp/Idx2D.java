/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.io.Serializable;

/**
 *
 * @author Anurag Sharma
 */
    
public class Idx2D implements Serializable, Cloneable{
    public int col;
    public int position;
    
    public Idx2D(){
        col = -1;
        position = -1;
    }    
    
    public boolean isEmpty(){
        return (col == -1 || position == -1);
    }
    
    @Override
    public Object clone() {
        try{
            return super.clone();
        }catch(CloneNotSupportedException cnse){
            cnse.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        if(col == -1 || position == -1)
            return " ";
        else
            return "[" + col + "," + position + ']';
    }
    
    
}