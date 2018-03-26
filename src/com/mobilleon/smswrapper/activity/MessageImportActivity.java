package com.mobilleon.smswrapper.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.mobilleon.smswrapper.adapters.MessageAdapter;
import com.mobilleon.smswrapper.asynctasks.MessageImportTask;
import com.mobilleon.smswrapper.helper.MailsComparator;
import com.mobilleon.smswrapper.model.MessageOperationRequest;
import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.model.SMSWrapperData;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService;
import com.mobilleon.smswrapper.service.SMSWrapperLifecycleService.SMSProLifecycleServiceBinder;
import com.mobilleon.smswrapper.utilities.IncomingMailServerConnection;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Atul Kaushik (atul.kaushik@gmail.com)
 *
 */
@SuppressLint("HandlerLeak")
public class MessageImportActivity extends SherlockListActivity implements OnSharedPreferenceChangeListener, OnQueryTextListener {

    private IncomingMailServerConnection mIncomingMailServerConnection;
    private ArrayList<MyMessage> mSelectedMessagesForImport;
    private ProgressDialog mDialog;
    private ListView mListView;
    private MessageAdapter mAdapter;
    private MyMessage myMessage;
    private int mImportRange;
    private MessageOperationRequest mImportRequest;
    private MailsComparator mComparator;
    private StringBuilder mMoreButtonLabel;
    private List<MyMessage> mMessagesFromServer = new ArrayList<MyMessage>();
    private String TAG = MessageImportActivity.class.getSimpleName();
    private boolean mBound;
    
    @SuppressWarnings("unused")
    private SMSWrapperLifecycleService mService; // make use of this service object to invoke methods defined in service

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.message_import_activity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(mIncomingMailServerConnection == null)
            getIncomingMailServerConnection();
        
        mImportRequest = new MessageOperationRequest();
        mImportRequest.mIncomingMailServerConnection = mIncomingMailServerConnection;
        mImportRequest.mImportRange = (mImportRange == 0) ? getImportRange() : mImportRange;
        
        setListFooter();
        
        Log.i(TAG,"mImportRequest.mImportRange = "+mImportRequest.mImportRange);
        Log.i(TAG,"mImportRequest.mImportRangeMultiplier = "+mImportRequest.mImportRangeMultiplier);
        
        new MessageImportTask(mImportHandler, getDialog("Loading..")).execute(mImportRequest);
        
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        menu.add(0, 1, 1, R.string.import_label)
        .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        
        //Create the search view
        SearchView searchView = new SearchView(getSupportActionBar().getThemedContext());
        
        mListView = getListView();
        mListView.setTextFilterEnabled(true);
        
        setupSearchView(searchView);
 
        menu.add(0, 2, 2, null)
            .setIcon(R.drawable.ic_search)
            .setActionView(searchView)
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

        return super.onCreateOptionsMenu(menu);
    }
    
    private void setupSearchView(SearchView mSearchView) {
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setSubmitButtonEnabled(false);
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint("Search Sender");
    }
    
    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            mListView.clearTextFilter();
        } else {
            mListView.setFilterText(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(MessageImportActivity.this, SMSWrapperActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            case 1:
                importMessages();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        
        myMessage = new MyMessage();
        myMessage = (MyMessage) l.getItemAtPosition(position);
              
        Intent shareIntent =  new Intent(MessageImportActivity.this, ShareAndSaveActivity.class);
        
        Bundle shareBundle = new Bundle();
        shareBundle.putStringArray("smsValues", new String [] {myMessage.getSender(), myMessage.getDate().trim(), myMessage.getTextContent()});
        
        shareIntent.putExtra("SMS", shareBundle);
        
        startActivity(shareIntent);

    }
    
    /**
     * getting range for
     * messages being
     * imported
     * */
    
    private int getImportRange() {
        SharedPreferences preferences;
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(this);
            mImportRange = Integer.parseInt(preferences.getString("importRange", "10"));
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        Log.i("getImportRange() ", "mImportRange = "+mImportRange);
        return mImportRange;
    }

    private void importMessages() {
        if(mAdapter == null)
            return;
        
        mSelectedMessagesForImport = mAdapter.getSelectedMeassages();
        if(mSelectedMessagesForImport.isEmpty()){
            toastErrorMessage(3);
            //ImportHandler.sendEmptyMessage(3);
            return;
        }
        mDialog = getDialog("Importing..");
        mDialog.show();
        
        writeMessagesToInbox(mSelectedMessagesForImport);
        mSelectedMessagesForImport.clear();
        mDupliacteMessageFilterHandler.postDelayed(mDupliacteMessageFilterRunnable, 2000); // delaying in calling this method as sms writing consumes time
    }
    
  
/*    private void importMessages() {
        mSelectedMessagesForImport = mAdapter.getSelectedMeassages();
        if(mSelectedMessagesForImport.isEmpty()){
            ImportHandler.sendEmptyMessage(3);
            return;
        }
            
        int counter;
        for(MyMessage message : mSelectedMessagesForImport){
            counter = Collections.binarySearch(getSmsFromPhoneInbox(), message, new MailsComparator());
            
            if(counter > 0){
                mSelectedMessagesForImport.remove(message);
            }
        int counter = 0;
        
        MailsComparator compy = new MailsComparator();
        List<MyMessage> fred = getSmsFromPhoneInbox();
        Collections.sort(fred, compy);
        Collections.sort(mSelectedMessagesForImport, compy);
        //Iterator<MyMessage> itr = fred.iterator();
        Iterator<MyMessage> itr = mSelectedMessagesForImport.iterator();

        MyMessage message;
        //for(MyMessage message : mSelectedMessagesForImport){
        while( itr.hasNext() ){
            message = (MyMessage)itr.next();
            counter = Collections.binarySearch(fred, message, compy);

            if(counter > 0){
                    //mSelectedMessagesForImport.remove(message); // this could throw an exception is a for each loop
                    itr.remove();            
        }
       }
        if(mSelectedMessagesForImport.isEmpty()){
            ImportHandler.sendEmptyMessage(3);
            return;
        }
        writeMessagesToInbox(mSelectedMessagesForImport);
        mSelectedMessagesForImport.clear();
        //finish();
    }*/
    
    
    /** getting list of sms from device inbox */
    @SuppressWarnings("unused")
    private ArrayList<MyMessage> getSmsFromPhoneInbox() {  /** for future use :-) */

        ArrayList<MyMessage> inboxList = new ArrayList<MyMessage>();
        MyMessage myMessage;

        Uri smsUri = Uri.parse("content://sms/inbox");
        Cursor cursor = getContentResolver().query(smsUri, null, null, null, null);

        while (cursor.moveToNext()) {
            myMessage = new MyMessage();
            myMessage.setSender(cursor.getString(cursor.getColumnIndex("address")).trim());
            myMessage.setTextContent(cursor.getString(cursor.getColumnIndexOrThrow("body")).trim());
            myMessage.setDate(Long.toString(cursor.getLong(cursor.getColumnIndex("date"))).trim());

            inboxList.add(myMessage);

        }
        cursor.close();
        return inboxList;
    }

    /** Setting adapter for list view */
    private void setImportListAdapter(List<MyMessage> beanList) {

        if (beanList != null && !beanList.isEmpty()) {
            mAdapter = new MessageAdapter(this, beanList, true);
            setListAdapter(mAdapter);
        } else {
            loadNoMessageToImport();
        }
    }

    /**
     * updates the members according to the changed preference
     * 
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences,
     *      java.lang.String)
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        
        if(key.contentEquals("googleAddress") || key.contentEquals("googlePassword"))
        mIncomingMailServerConnection = new IncomingMailServerConnection(sharedPreferences.getString("googleAddress", ""), sharedPreferences.getString("googlePassword", ""));
        
        if(key.contentEquals("importRange"))
        mImportRange = Integer.parseInt(sharedPreferences.getString("importRange", "10"));
    }

    /**
     * reads configuration
     */
    private void getIncomingMailServerConnection() {
        SharedPreferences preferences = null;
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        } catch (Exception e) {
            Log.e(MessageImportActivity.class.getSimpleName(), e.getMessage());
        }
        /* initialize configured email address */
        mIncomingMailServerConnection = new IncomingMailServerConnection(preferences.getString("googleAddress", ""),preferences.getString("googlePassword", ""));
    }

    /** Writing Messages (SMS) to Device inbox */

    private void writeMessagesToInbox(List<MyMessage> messagesToWrite) {
        ContentValues values;
        for(MyMessage message : messagesToWrite){
            values = new ContentValues();
            
            values.put("address", message.getSender());
            values.put("body", message.getTextContent());
            values.put("date", message.getDate().trim()); // This sets the sms date to given date which is mail date here
            getContentResolver().insert(Uri.parse("content://sms/inbox"), values);    
        }    
    }

    /**
     * toasting exception
     * messages in case of
     * failed response from
     * Mail API/Server
     * @param errorCode :
     *                the codes for toasting custom messages
     * */

    private void toastErrorMessage (int errorCode) {

        switch (errorCode) {
        case 1:// No network available
            Toast toastNetwork = Toast.makeText(MessageImportActivity.this,
                    "Network is not available : Please check device settings", Toast.LENGTH_SHORT);
            toastNetwork.show();
            break;
            
        case 2:// Invalid credentials
            Toast toastAuthentication = Toast.makeText(MessageImportActivity.this,
                    "Authentication failed : Please check settings", Toast.LENGTH_SHORT);
            toastAuthentication.show();
            break;
              
        case 3:// No mail selected for import
            Toast toastNoSelection = Toast.makeText(MessageImportActivity.this,
                    "Import aborted : Please select a message to import", Toast.LENGTH_SHORT);
            toastNoSelection.show();
            break;
            
        case 4:// No mail selected for import
            Toast toastImapDisabled = Toast.makeText(MessageImportActivity.this,
                    "IMAP disabled : Please visit your Gmail settings page and enable your account for IMAP access.", Toast.LENGTH_LONG);
            toastImapDisabled.show();
            break;

        default:
            Toast toastUnidentified = Toast.makeText(MessageImportActivity.this,
                    "Unidentified error occurred : Please try later", Toast.LENGTH_LONG);
            toastUnidentified.show();
            break;
        }
    
    }
  
    /**
     * A handler to handle 
     * response from Mail API/Server 
     * */
    
    private Handler mImportHandler = new Handler(){
        
        @SuppressWarnings("unchecked")
        public void handleMessage(android.os.Message msg) {
            SMSWrapperData<List<MyMessage>> responseData = (SMSWrapperData<List<MyMessage>>) msg.obj;
            
            if(responseData.requestSuccessful){
                if(mComparator == null)
                mComparator = new MailsComparator();
                
                Collections.sort(responseData.response, mComparator);
                                
                    mMessagesFromServer.addAll(responseData.response);
                
                setImportListAdapter(mMessagesFromServer);
            }else{
                toastErrorMessage(responseData.responseErrorCode);
            }
        };
    };
    
    @Override
   protected void onResume() {
       // if(mMessagesFromServer == null)
         //   mMessagesFromServer = new ArrayList<MyMessage>();
        
       Log.i(TAG, "mMessagesFromServer size = "+mMessagesFromServer.size());
       super.onResume();
   };
    
    /**
     * method for getting progress dialogs
     * @param dialogMessage :
     *                     the message to be shown in dialog
     * */
    private ProgressDialog getDialog(String dialogMessage) {
        ProgressDialog progressDialog; 
        progressDialog = new ProgressDialog(MessageImportActivity.this);
        progressDialog.setMessage(dialogMessage);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(true);
        return progressDialog;
    }    
    
    /**
     * deletes dupliacte messages from
     * device inbox
     * @param  context
     *              : calling context
     * 
     * */
    public void deleteDuplicateSMS(Context context) {
        Cursor smsCursor = null;
        Cursor smsCursorCopy = null;
        try {
            Log.i("deleteSMS","Deleting SMS from inbox");
            Uri uriSms = Uri.parse("content://sms/inbox");
            smsCursor = context.getContentResolver().query(uriSms,
                new String[] { "_id", "thread_id", "address", "person", "date", "body" }, null, null, null);
            
            smsCursorCopy = context.getContentResolver().query(uriSms,
                    new String[] { "_id", "thread_id", "address", "person", "date", "body" }, null, null, null);
            
            if(smsCursorCopy != null && smsCursorCopy.move(2)){
                if (smsCursor.moveToFirst()) {
                    do {
                        long id_c = smsCursor.getLong(0);
                        //long threadId_c = smsCursor.getLong(1);
                        String address_c = smsCursor.getString(2);
                        String body_c = smsCursor.getString(5);

                        String address_cc = smsCursorCopy.getString(2);
                        String body_cc = smsCursorCopy.getString(5);
                        
                        if (body_cc.equals(body_c) && address_cc.equals(address_c)) {
                            context.getContentResolver().delete(Uri.parse("content://sms/" + id_c), null, null);
                        }
                    } while (smsCursor.moveToNext() && smsCursorCopy.moveToNext());
                }                
            }
        } catch (Exception e) {
            Log.e(MessageImportActivity.class.getSimpleName(),"deleteSMS() : Could not delete SMS from inbox: " + e.getMessage());
        }
        
        try {
            if(smsCursor != null)
                smsCursor.close();
            
            if(smsCursorCopy != null)
                smsCursorCopy.close();
            
        } catch (Exception e) {
            Log.e(MessageImportActivity.class.getSimpleName(),"deleteSMS() : failed to close the cursors: " + e.getMessage());
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    /**
     * A handler to delete duplicate messages
     * from device inbox : NOT THE BEST WAY TO DO IT
     * */
    private Handler mDupliacteMessageFilterHandler = new Handler();

    private Runnable mDupliacteMessageFilterRunnable = new Runnable() {

            @Override
            public void run() {
                    Log.i("mDupliacteMessageFilterHandler", "deleting duplicate messages");
                    deleteDuplicateSMS(MessageImportActivity.this);
                    mDupliacteMessageFilterHandler.removeCallbacks(mDupliacteMessageFilterRunnable);
                    finish();
                    if(mDialog.isShowing())
                        mDialog.dismiss();
            }
    };
    
    /**
     * loading no message to import
     * */
    private void loadNoMessageToImport() {
        TextView mEmptyListView;
        ListView mListView = getListView();
            mEmptyListView = new TextView(MessageImportActivity.this);
            mEmptyListView.setLayoutParams(new LayoutParams(
                            LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            mEmptyListView.setGravity(Gravity.CENTER);
            mEmptyListView.setText("NO MESSAGES AVAILABLE FOR IMPORT");
            mEmptyListView.setTextSize(24);
            mEmptyListView.setVisibility(View.GONE);
            ((ViewGroup) mListView.getParent()).addView(mEmptyListView);
            mListView.setEmptyView(mEmptyListView);
    }
    
    /**
     * for dynamically
     * loading more mails
     * from server
     * */
    
    private View setListFooter() {

        mMoreButtonLabel = new StringBuilder("Load Next ");
        mMoreButtonLabel.append(mImportRange);
        mMoreButtonLabel.append(" Messages");
        
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View footerView = layoutInflater.inflate(R.layout.import_list_footer, null);
        getListView().addFooterView(footerView);
        Button moreButton = (Button) footerView.findViewById(R.id.more_msgs_btn);
        moreButton.setVisibility(View.VISIBLE);
        ListFooterListener lfl = new ListFooterListener();
        //footerView.findViewById(R.id.more_msgs_btn).setOnClickListener(lfl);
        moreButton.setText(mMoreButtonLabel);
        moreButton.setOnClickListener(lfl);
        return footerView;
    }
    
    private class ListFooterListener implements OnClickListener{
        
        @Override
        public void onClick(View v) {
            //Toast.makeText(MessageImportActivity.this, "Yo main!!", Toast.LENGTH_SHORT).show();
            
            //Log.i(TAG, "mImportRequest = "+mImportRequest);
            mImportRequest.mImportRangeMultiplier = mImportRequest.mImportRangeMultiplier+1;
            mImportRequest.mImportRange = mImportRange;
            
            Log.i(TAG,"mImportRequest.mImportRange = "+mImportRequest.mImportRange);
            Log.i(TAG,"mImportRequest.mImportRangeMultiplier = "+mImportRequest.mImportRangeMultiplier);
            
            new MessageImportTask(mImportHandler, getDialog("Loading..")).execute(mImportRequest);
            
        }   
    }
    
    @Override
    protected void onStart() {
        if(!PreferenceManager.getDefaultSharedPreferences(this).getBoolean("remember", false)){
            Intent intent = new Intent(this, SMSWrapperLifecycleService.class);
            bindService(intent , mServiceConnection, Context.BIND_AUTO_CREATE);    
        }
        super.onStart();
    }
    
    @Override
    protected void onStop() {
        if(mBound)
            unbindService(mServiceConnection);  
        super.onStop();
    }
    
    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "onServiceDisconnected");
            mBound = false;
        }
        
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "onServiceConnected");
            SMSProLifecycleServiceBinder serviceBinder = (SMSProLifecycleServiceBinder) service;
            mService = serviceBinder.getService();
           mBound = true;
        }
    };
}