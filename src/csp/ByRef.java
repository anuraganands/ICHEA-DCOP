/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

/**
 *
 * @author Anurag Sharma
 */
public class ByRef {
    
    public ByRef(Object val){
        this.val = val;
    }
    private Object val;
    
    public void setVal(Object val){
        this.val = val;
    }
    
    public Object getVal(){
        return this.val;
    }
    
}
