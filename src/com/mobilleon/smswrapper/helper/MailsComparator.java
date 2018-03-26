package com.mobilleon.smswrapper.helper;

import com.mobilleon.smswrapper.model.MyMessage;

import java.util.Comparator;


/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class MailsComparator implements Comparator<MyMessage>{
    
    @Override
    public int compare(MyMessage msg1, MyMessage msg2) {
            return (Long.valueOf(msg1.getDate().trim()) - Long.valueOf(msg2.getDate().trim())) > 0 ? -1 : 1;
    }
}
