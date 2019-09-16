/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import javax.naming.SizeLimitExceededException;
/**
 *
 * @author Anurag Sharma
 */
public class MyQueue<T> implements Iterable<T> {
    private int maxSize;
    private Queue<T> q;

    public Iterator<T> iterator() {
        return q.iterator();
    }
    
    
    
    public MyQueue(int size){
        this.maxSize = size;
        q = new LinkedList<T>();        
    }
    
    /**
     * applicable for fixed sized queue only
     * @param item
     * @throws SizeLimitExceededException 
     */
    public void forceEnqueueByDequeue(T item){// throws SizeLimitExceededException{
        if(capacity() == -1){
            throw new UnsupportedOperationException("Use T tryPush(T item) method");
        }
        if(this.capacity() == 0){
            return;
        }
        if(q.size()<this.capacity()){                        
            q.add(item); 
        } else {
            this.dequeue();
            q.add(item);//most likely it will not throw again
        }
    }
    
    public T dequeue(){
        return q.poll();
    }    
    
    public final int curSize(){
        return q.size();
    }
    
    public void clearAll(){
        while(dequeue()!=null){
            ;
        }
    }
    
    /**
     * The maximum elements that this queue can hold. If the returned value is
     * negative then size is <B>unlimited</B>.
     * @return 
     */
    public final int capacity(){
        return this.maxSize;
    }

    @Override
    public String toString() {
        return q.toString();
    }
    
}
