/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package csp;

import javax.swing.JOptionPane;

/**
     * @author - Anurag Sharma
     * @param message - text message to be appeared in the msg box
     * @param msgboxVals - msgboxVals[0] - for Title msgboxVals[1] for
     * msgbox type. It is an enum integer and should be picked from
     * JOptionPane.?enum values eg. JOptionPane.WARNING_MESSAGE.
     *
     */
public class MyException extends Exception{
    private String message_;
    private String title_;
    private int msgBoxType_;
    private boolean useMsgBox_;

    /**
     *
     * @param message - text message to be appeared in the msg box
     * @param msgboxVals - msgboxVals[0] - for Title msgboxVals[1] for
     * msgbox type. It is an enum integer and should be picked from
     * JOptionPane.?enum values eg. JOptionPane.WARNING_MESSAGE.
     *
     */
    public MyException(String message, Object... msgboxVals) {
        super();
        if(message == null){
            this.message_ = "No message Specified!";
            useMsgBox_ = false;
        }
        else if(message != null && msgboxVals.length == 0)            {
            this.message_ = message;
            this.useMsgBox_ = false;
        }
        else if (message != null && msgboxVals.length == 2){
            this.message_ = message;
            this.useMsgBox_ = true;
            this.title_ = (String)msgboxVals[0];
            this.msgBoxType_ = (Integer)msgboxVals[1];             
        }    
    }

    @Override
    public String getMessage() {
        return this.message_;
    }
    
    public void showMessageBox(){
        if(useMsgBox_)
            JOptionPane.showMessageDialog(null, this.message_, this.title_, this.msgBoxType_);
        else
            System.err.println(this.message_);
    }
    
    public void printMessage(){
        System.out.flush(); // It is must otherwise there is dealy in printing System.err
        System.err.flush();
        System.err.println(this.message_);
        System.err.flush();
        System.out.flush();
    }

    @Override
    public String toString() {
        return this.message_;
    }
}
