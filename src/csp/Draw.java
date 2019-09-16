/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import java.util.ArrayList;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;

/**
 *
 * @author Anurag Sharma
 */
public class Draw {
    MatlabProxyFactory factory;
    MatlabProxy proxy;

    public Draw() throws MatlabConnectionException{
        factory = new MatlabProxyFactory();
        proxy = factory.getProxy();
    }
    
    public void close(){
        proxy.disconnect();   
    }

    
    public void startDrawing(ArrayList<String> commands) throws MatlabInvocationException{
        for (int i = 0; i < commands.size(); i++) {
            proxy.eval(commands.get(i) + ";");            
        }                
    }
}
