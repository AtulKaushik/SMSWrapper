/**
 * 
 */
package com.mobilleon.smswrapper.utilities;

import java.io.IOException;
import java.util.Date;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import android.util.Log;
import com.sun.mail.imap.IMAPMessage;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class Utilities {

    private String senderString = "";
    private String sender = "";
    
    @SuppressWarnings("unused")
    private boolean textIsHtml = false; /* future feature :) */

    public Utilities() {

        super();
    }

    /** Method for getting sender of each mail */

    public String getMailSender(Message mail) {

        try {
            senderString = ((IMAPMessage) mail).getSender().toString();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        if (senderString.length() > 0) {
            if (senderString.contains("<")) {
                sender = senderString.substring(0, (senderString.indexOf("<")));
            } else {
                sender = senderString.substring(0, (senderString.indexOf("@")));
            }

        }
        return sender;
    }

    /** Method for retrieving date of each mail */

    public Date getMailDate(Message mail) {

        Date date = null;
        try {
            date = ((IMAPMessage) mail).getReceivedDate();
        } catch (MessagingException e) {
            Log.e(Utilities.class.getSimpleName(), e.getMessage());
        }
        return date;
    }

    /**
     * Return the primary text content of the message.
     */
    public String getText(Part p) throws MessagingException, IOException {

        if (p.isMimeType("text/*")) {
            String s = (String) p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            /* prefer HTML text over plain text */
            Multipart mp = (Multipart) p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }
}
