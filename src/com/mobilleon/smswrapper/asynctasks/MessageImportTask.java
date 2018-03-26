    /**
     * Async task to
     * import sms/mail
     * from mail account
     * */

package com.mobilleon.smswrapper.asynctasks;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.mobilleon.smswrapper.model.MessageOperationRequest;
import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.model.SMSWrapperData;
import com.mobilleon.smswrapper.utilities.Utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;


/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class MessageImportTask extends AsyncTask<MessageOperationRequest, Void, List<MyMessage>>{

    private Message[] mMessagesFromServer;
    private int mMessageCountFromServer = 0;
    private ArrayList<MyMessage> mListOfMessagesFromServer;
    private ProgressDialog mDialog;
    private Handler mHandler;
    private int errorCode;
    private String TAG = MessageImportTask.class.getSimpleName();
    
    public MessageImportTask(Handler mHandler, ProgressDialog mDialog){  
    super();
        this.mHandler = mHandler;
        this.mDialog = mDialog;
    }
    
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(mDialog != null)
        mDialog.show();
    }

    @Override
    protected List<MyMessage> doInBackground(MessageOperationRequest... params) {

        MessageOperationRequest request;
        if(params == null || params[0] == null) return null; 
        request = params[0];
        
        try {
            Log.i(TAG,"request.mImportRange = "+request.mImportRange);
            Log.i(TAG,"request.mImportRangeMultiplier = "+request.mImportRangeMultiplier);
            mMessagesFromServer = request.mIncomingMailServerConnection.readInboxMail(request.mImportRange, request.mImportRangeMultiplier);
            try {
                mMessagesFromServer = request.mIncomingMailServerConnection.readInboxMail(request.mImportRange, request.mImportRangeMultiplier);
            } catch (Exception e) {
                e.printStackTrace();
                errorCode = 4;
                return null;
            }
            mMessageCountFromServer = mMessagesFromServer.length;
            Log.i(TAG,"mMessageCountFromServer = "+mMessageCountFromServer);
            
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("Invalid")) {
                Log.e(MessageImportTask.class.getSimpleName(), e.getMessage());
                errorCode = 2;
                return null;
            } else {
                e.printStackTrace();
                errorCode = 1;
                return null;
            }
        }
        if (mMessageCountFromServer > 0) {

            mListOfMessagesFromServer = new ArrayList<MyMessage>();
            int multipartCount = 0;
            Multipart multipart;
            Part part;
            String textMessage = "";
            Object mMessageObject = null;
            int mMessageCount = 0;
            Utilities myUtilityClassObject = new Utilities();
            for (mMessageCount = 0; mMessageCount < mMessagesFromServer.length; mMessageCount++) {

                try {
                    mMessageObject = (mMessagesFromServer[mMessageCount]).getContent();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                } catch (MessagingException e) {
                    Log.e(TAG, e.getMessage());
                }
                if (mMessageObject instanceof Multipart) {

                    multipart = (Multipart) mMessageObject;

                    try {
                        multipartCount = multipart.getCount();
                    } catch (MessagingException e) {
                        Log.e(TAG, e.getMessage());
                    }

                    for (int i = 0; i < multipartCount; i++) {

                        try {
                            part = multipart.getBodyPart(i);
                            textMessage = myUtilityClassObject.getText(part);
                        } catch (MessagingException e) {
                            Log.e(TAG, e.getMessage());
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }

                        if (textMessage != null && textMessage.trim().length() > 0) {
                            break;
                        }
                    }
                }
                /* call method here to save a mail(message body, mail date, mail sender) in mail adapter. */
                mListOfMessagesFromServer.add(new MyMessage( myUtilityClassObject.getMailSender(mMessagesFromServer[mMessageCount]),
                        Long.toString(myUtilityClassObject.getMailDate(mMessagesFromServer[mMessageCount]).getTime()), textMessage.trim()));
            }
        }
        return mListOfMessagesFromServer;
    }

    @Override
    protected void onPostExecute(List<MyMessage> result) {
        super.onPostExecute(result);
        
        if (mHandler == null) return;
        
        SMSWrapperData<List<MyMessage>> resultData = new SMSWrapperData<List<MyMessage>>();
        if(result == null){
            resultData.requestSuccessful = false;
            resultData.responseErrorCode = errorCode;  //set this when result is null
        }else{
            resultData.requestSuccessful = true;
            resultData.response = result;      
        }
        
        android.os.Message m = android.os.Message.obtain();
        m.obj = resultData;
        mHandler.sendMessage(m);

        if (mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }
}

