
package com.mobilleon.smswrapper.utilities;

import android.util.Log;

import com.sun.mail.smtp.SMTPMessage;
import java.util.Date;
import java.util.Properties;
import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * @author AtulKaushik (atul.kaushik@gmail.com)
 *
 */
public class OutgoingMailServerConnection extends Authenticator {

    private String user;
    private String pass;
    private String[] to;
    private String port;
    private String sport;
    private String host;
    private String subject;
    private String body;
    private boolean auth;
    private boolean debuggable;
    private Multipart multipart;
    private Message messageToBeSend;
    Message[] msgContainer;
    Session smtpSession;
    private String TAG = OutgoingMailServerConnection.class.getSimpleName();

    public OutgoingMailServerConnection() {

        host = "smtp.gmail.com"; // default smtp server 
        port = "465"; // default smtp port 
        sport = "465"; // default socketfactory port 

        user = ""; // username 
        pass = ""; // password 
        subject = ""; // email subject 
        body = ""; // email body 

        debuggable = false; // debug mode on or off - default off 
        auth = true; // smtp authentication - default on 

        multipart = new MimeMultipart();

        // There is something wrong with MailCap, javamail can not find a handler for the multipart/mixed part, so this bit needs to be added. 
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public OutgoingMailServerConnection(String user, String pass) throws Exception {

        this();

        this.user = user;
        this.pass = pass;
    }

    public boolean send(Folder destination) throws Exception {

        //Log.i(TAG, "excecuting send(..)");
        Properties props = setProperties();

        if (!user.equals("") && !pass.equals("") && to.length > 0 && !subject.equals("") && !body.equals("")) {
            smtpSession = Session.getInstance(props, this);

            MimeMessage msg = new MimeMessage(smtpSession);

            msg.setFrom(new InternetAddress(user));

            InternetAddress[] addressTo = new InternetAddress[to.length];
            for (int i = 0; i < to.length; i++) {
                addressTo[i] = new InternetAddress(to[i]);
            }
            msg.setRecipients(MimeMessage.RecipientType.TO, addressTo);

            msg.setSubject(subject);
            msg.setSentDate(new Date());

            // setup message body 
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            multipart.addBodyPart(messageBodyPart);

            // Put parts in message 
            msg.setContent(multipart);

            // SMTPMessage smtpMessageObject = new SMTPMessage(msg);
            messageToBeSend = new SMTPMessage(msg);

            msgContainer = new SMTPMessage[1];
            msgContainer[0] = messageToBeSend;

            // send email 
            destination.appendMessages(msgContainer);

            Log.i(TAG, "Message Send = true");
            return true;
        } else {
            Log.e(TAG, "Message Send = false");
            return false;
        }
    }

    /* This method is not used as of now */
    public void addAttachment(String filename) throws Exception {

        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName(filename);

        multipart.addBodyPart(messageBodyPart);
    }

    @Override
    public PasswordAuthentication getPasswordAuthentication() {

        return new PasswordAuthentication(user, pass);
    }

    private Properties setProperties() {

        Properties props = new Properties();

        props.put("mail.smtp.host", host);

        if (debuggable) {
            props.put("mail.debug", "true");
        }

        if (auth) {
            props.put("mail.smtp.auth", "true");
        }

        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.port", sport);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");

        return props;
    }

    /** the getters and setters */
    public String getBody() {

        return body;
    }

    public void setBody(String body) {

        this.body = body;
    }

    public String[] getTo() {

        return to;
    }

    public void setTo(String[] to) {

        this.to = to;
    }

    public String getSubject() {

        return subject;
    }

    public void setSubject(String subject) {

        this.subject = subject;
    }

}
