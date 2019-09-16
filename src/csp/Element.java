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
public class Element implements Comparable, Serializable {
    public double val;
    public int idx;
    public static final int ASCENDING = 0;
    public static final int DESCENDING = 1; 
    private static int order = ASCENDING; //Not a very good idea.... Element is a commonly used class bhai. make private..

    public Element(double val, int idx, int ... params){
        this.val = val;
        this.idx = idx; 
        order = params.length > 0 ? params[0] : this.ASCENDING;
    }
    public Element(int val, int idx, int ... params){
        this.val = val;
        this.idx = idx;
        order = params.length > 0 ? params[0] : this.ASCENDING;
    }
    public int compareTo(Object o) {
        if (!(o instanceof Element)) {
          throw new ClassCastException("Not a Person");
        }
        Element e = (Element) o;
        if(order == ASCENDING)
            return (int)(val-e.val);
        else
            return (int)(e.val - val);
    }

    @Override
    public String toString() {
        return "[" + val + "," + idx + "]";
    }
    
    

}
