/**
 * A class for getting
 * mail server authentication
 * & mail objects
 */
package com.mobilleon.smswrapper.utilities;

import android.util.Log;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * @author Atul Kaushik (atul.kaushik@gmail.com)
 *
 */
public class IncomingMailServerConnection extends Authenticator {

    private String mailhost = "imap.gmail.com";
    private String user;
    private String password;
    public Session session;
    private Folder smsfolder;
    private Folder defaultFolder; // generally it will be Inbox folder for most mail servers
    public String smsFolderName = "SMS Wrapper";
    public Store store;
    private Message[] msgs;
    //private String TAG = IncomingMailServerConnection.class.getSimpleName();

    public IncomingMailServerConnection(String user, String password) {

        this.user = user;
        this.password = password;
        Properties props = System.getProperties();
        try {
            props.setProperty("mail.store.protocol", "imaps");
            props.put("mail.imaps.auth", "true");
            props.put("mail.imaps.port", "993");
            props.put("mail.imaps.socketFactory.port", "993");
            props.put("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.imaps.socketFactory.fallback", "false");
            props.setProperty("mail.imaps.quitwait", "false");
            session = Session.getDefaultInstance(props, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * reading mail from
     * SMSPro folder
     * */
    public synchronized Message[] readMail() throws Exception {

        defaultFolder = getDefaultFolder();
        try {
            store.getFolder(smsFolderName);
        } catch (Exception e) {
            Log.e(IncomingMailServerConnection.class.getSimpleName(), e.getMessage());
        }
        smsfolder = createSubFolder(defaultFolder, smsFolderName);

        smsfolder.open(Folder.READ_ONLY);
        msgs = smsfolder.getMessages();
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        smsfolder.fetch(msgs, fp);

        return msgs;
    }
    
    /**
     * reading mail
     * from Inbox folder
     * */
    public synchronized Message[] readInboxMail(Integer range, Integer rangeMultiplier) throws Exception {

        store = session.getStore("imaps");
        store.connect(mailhost, user, password);
        //System.out.println(store);
        
        Folder inbox = null; 
        
        try {
            inbox = store.getFolder("Inbox");
            inbox.open(Folder.READ_ONLY);
        } catch (Exception e) {
            Log.e(IncomingMailServerConnection.class.getSimpleName(), e.getMessage());
        }
        
        int inboxCount = inbox.getMessageCount();
        Message messages[] = inbox.getMessages(inboxCount-(range*rangeMultiplier)+1, (inboxCount-(range*(rangeMultiplier-1))));
        //Message messages[] = inbox.getMessages(inboxCount-4,inboxCount );
        
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.ENVELOPE);
        inbox.fetch(messages, fp);
        
        return messages;
    }

    public Folder getDefaultFolder() throws MessagingException {

        store = session.getStore("imaps");
        store.connect(mailhost, user, password);
        Folder defaultFolder = store.getDefaultFolder();
        return defaultFolder;
    }

    /**  
     * Note that in gmail folder hierarchy is not maintained.  
     * */
    public Folder createSubFolder(Folder parent, String childFolderName) {

        Folder subFolder = null;
        try {
            subFolder = parent.getFolder(childFolderName);
            subFolder.create(Folder.HOLDS_MESSAGES);
        } catch (Exception e) {
            Log.e(IncomingMailServerConnection.class.getSimpleName(), e.getMessage());
        }
        return subFolder;
    }
}
