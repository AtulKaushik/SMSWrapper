/**
 * message model
 */
package com.mobilleon.smswrapper.model;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class MyMessage {

    private String sender = "";
    private String textContent = "";
    private String date = "";

    public MyMessage(){
        super();
    }
    
    public MyMessage(String sender,String date){
        super();
        this.sender = sender;
        this.date = date;
    }
    
    public MyMessage(String sender,String date,String textMeassage){
        super();
        this.sender = sender;
        this.date = date;
        this.textContent = textMeassage;
    }
    
/* Getters and Setters */

    public String getSender() {

        return sender.trim();
    }

    public void setSender(String sender) {

        this.sender = sender.trim();
    }

    public String getTextContent() {

        return textContent.trim();
    }

    public void setTextContent(String textContent) {

        this.textContent = textContent.trim();
    }

    public String getDate() {

        return date.trim();
    }

    public void setDate(String date) {

        this.date = date.trim();
    } 
}
