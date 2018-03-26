    /**
     * Async task to
     * export sms/mail
     * from device to 
     * mail account
     * */


package com.mobilleon.smswrapper.asynctasks;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.mobilleon.smswrapper.model.MessageOperationRequest;
import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.model.SMSWrapperData;
import com.mobilleon.smswrapper.utilities.IncomingMailServerConnection;
import com.mobilleon.smswrapper.utilities.OutgoingMailServerConnection;

import java.util.List;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class MessageExportTask extends AsyncTask<MessageOperationRequest, Void, Boolean> {

    private List<MyMessage> msgsToExport;
    private Folder smsProFolder;
    private Store imapStore;
    private Session imapSession;
    private IncomingMailServerConnection mIncomingMailServerConnection;
    private OutgoingMailServerConnection mOutgoingMailServerConnection;
    private ProgressDialog mDialog;
    private int errorCode;
    private SharedPreferences mPreferences;
    private Handler mResponseHandler;
    private String TAG = MessageExportTask.class.getSimpleName();
    
    
    public MessageExportTask(Handler mResponseHandler, List<MyMessage> msgsToExport, ProgressDialog mDialog){
        super();
        this.msgsToExport = msgsToExport;
        this.mDialog = mDialog;
        this.mResponseHandler = mResponseHandler;
    }
    
    @Override
    protected void onPreExecute() {

        super.onPreExecute();
        if(mDialog != null)
            mDialog.show();
    }

    @Override
    protected Boolean doInBackground(MessageOperationRequest... params) {
        MessageOperationRequest exportRequest;
        
        if(params == null || params[0] == null) return null;
        exportRequest = params[0];
        
        mIncomingMailServerConnection = exportRequest.mIncomingMailServerConnection;
        mOutgoingMailServerConnection = exportRequest.mOutgoingMailServerConnection;
        mPreferences = exportRequest.mPreferences;
        
        try {
            if (!smsProFolderExists()) {
                smsProFolder = mIncomingMailServerConnection.createSubFolder(mIncomingMailServerConnection.getDefaultFolder(),
                        mIncomingMailServerConnection.smsFolderName);
            } else {
                smsProFolder = mIncomingMailServerConnection.getDefaultFolder().getFolder(mIncomingMailServerConnection.smsFolderName);
            }
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Invalid")) {

                errorCode = 2;
                e.printStackTrace();
                return null;
            } else {
                errorCode = 1;
                e.printStackTrace();
                return null;
            }
        }
        
        return sendMail(msgsToExport, smsProFolder, imapSession);
    }

    @Override
    protected void onPostExecute(Boolean result) {

        super.onPostExecute(result);

        Log.i(TAG, "result = "+result);
        if (mResponseHandler == null) return;
        
        SMSWrapperData<MyMessage> resultData = new SMSWrapperData<MyMessage>();
        if(result != null && result){
            resultData.requestSuccessful = true;
            resultData.responseErrorCode = 0;
        }else{
            resultData.requestSuccessful = false;
            resultData.responseErrorCode = errorCode;  //set this when result is null
        }
        
        android.os.Message m = android.os.Message.obtain();
        m.obj = resultData;
        mResponseHandler.sendMessage(m);

                if (smsProFolder != null && smsProFolder.isOpen()) {
                    try {
                        smsProFolder.close(true);
                    } catch (MessagingException e) {
                        e.printStackTrace();
                    }
            }
        
        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
    
    /**
     * checking if SMSPro folder exists or not
     * */
    private boolean smsProFolderExists() throws Exception {

        setImapConnection();

        return (imapStore.getFolder(mIncomingMailServerConnection.smsFolderName)).exists();
    }

    
    private void setImapConnection() {

        try {
            imapSession = mIncomingMailServerConnection.session;
            imapStore = imapSession.getStore("imaps");
        } catch (NoSuchProviderException e) {
            errorCode = 1;
            e.printStackTrace();
            return;
        }
        try {
            imapStore.connect("imap.gmail.com", mPreferences.getString("googleAddress", ""),mPreferences.getString("googlePassword", ""));
        } catch (MessagingException e) {
            errorCode = 2;
            e.printStackTrace();
            return;

        }
        return;
    }
    
    /**
     * sends an email
     * 
     * @param   context             context
     * @param   originatingAddress  originating address
     * @param   message             message contents
     */
    private boolean sendMail(List<MyMessage> messagesToExport, Folder destinationFolder, Session currentSession){

        Log.i(TAG, "messagesToExport size = "+messagesToExport.size());
        
        String[] toArr = {mPreferences.getString("googleAddress", "")};
        for(MyMessage message : messagesToExport){
            mOutgoingMailServerConnection.setTo(toArr);
            mOutgoingMailServerConnection.setSubject(message.getSender());
            mOutgoingMailServerConnection.setBody("\n" + message.getTextContent() + "\n");

            try {
                mOutgoingMailServerConnection.send(destinationFolder);
                
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }            
        }
        return true;
    }

    /**
     * if a mail
     * already exists
     * in folder
     * */

    @SuppressWarnings("unused")
    private boolean checkExistingMails(Folder target, String subject, String textBody) {  /* future feature :) */

        boolean mailExists = false;
        Message[] folderMsgs = null;
        try {
            if (!target.isOpen()) {
                target.open(Folder.READ_WRITE);

            }
            folderMsgs = target.getMessages();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        for (Message msg : folderMsgs) {
            String msgSubject = "";
            try {
                msgSubject = msg.getSubject();

            } catch (MessagingException e) {
                e.printStackTrace();
            }
            if (subject.equalsIgnoreCase(msgSubject)) {
                mailExists = true;
                break;
            }
        }

        return mailExists;
    }
}