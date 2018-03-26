package com.mobilleon.smswrapper.activity;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.widget.SearchView;
import com.actionbarsherlock.widget.SearchView.OnQueryTextListener;
import com.mobilleon.smswrapper.adapters.MessageAdapter;
import com.mobilleon.smswrapper.asynctasks.MessageExportTask;
import com.mobilleon.smswrapper.model.MessageOperationRequest;
import com.mobilleon.smswrapper.model.MyMessage;
import com.mobilleon.smswrapper.model.SMSWrapperData;
import com.mobilleon.smswrapper.root.R;
import com.mobilleon.smswrapper.utilities.Constants;
import com.mobilleon.smswrapper.utilities.IncomingMailServerConnection;
import com.mobilleon.smswrapper.utilities.NetworkUtility.CONNECTION;
import com.mobilleon.smswrapper.utilities.OutgoingMailServerConnection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * @author AtulKaushik (atul.kaushik@gmail.com)
 *
 */
public class MessageExportActivity extends SherlockListActivity implements OnSharedPreferenceChangeListener, OnQueryTextListener {

    private MyMessage myMessage;
    private ListView mListView;
    private MessageAdapter mAdapter;
    private SharedPreferences mPreferences;
    private OutgoingMailServerConnection mOutgoingMailServerConnection;
    private IncomingMailServerConnection mIncomingMailServerConnection;
    //private String TAG = MessageExportActivity.class.getSimpleName();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /* initialize configured email address */
        if(mOutgoingMailServerConnection == null)
            getOutgoingMailServerConnection();
        
        if(mIncomingMailServerConnection == null)
        getIncomingMailServerConnection();
            
        setContentView(R.layout.message_export_activity);

    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        menu.add(0, 1, 1, R.string.export_label)
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
                Intent intent = new Intent(MessageExportActivity.this, SMSWrapperActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;

            case 1:
                if(Constants.INTERNET_CONNECTION == CONNECTION.PROCEED){
                    if(mPreferences == null)
                        getPreferences();
                    
                    MessageOperationRequest exportRequest = new MessageOperationRequest();
                    exportRequest.mIncomingMailServerConnection = mIncomingMailServerConnection;
                    exportRequest.mOutgoingMailServerConnection = mOutgoingMailServerConnection;
                    exportRequest.mPreferences = mPreferences;
                    
                    List<MyMessage> msgsToExport;
                    msgsToExport = mAdapter.getSelectedMeassages();
                    if(msgsToExport.isEmpty()){
                        toastExportMessage(3);
                    }else{
                        new MessageExportTask(mExportHandler, msgsToExport, getDialog("Exporting..")).execute(exportRequest);    
                    } 
                }else{
                    Toast.makeText(this, "NO INTERNET CONNECTION : PLEASE CHECK YOUR CONNECTION", Toast.LENGTH_LONG).show();
                }
                break;
                
        }   
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        super.onListItemClick(l, v, position, id);
        
        myMessage = new MyMessage();
        myMessage = (MyMessage) l.getItemAtPosition(position);
              
        Intent shareIntent =  new Intent(MessageExportActivity.this, ShareAndSaveActivity.class);
        
        Bundle shareBundle = new Bundle();
        shareBundle.putStringArray("smsValues", new String [] {myMessage.getSender(), myMessage.getDate().trim(), myMessage.getTextContent()});
        
        shareIntent.putExtra("SMS", shareBundle);
        
        startActivity(shareIntent);

    }
    
    /** Setting adapter for list view */
    private void setExportListAdapter(ArrayList<MyMessage> messagesList) {

        if (messagesList!= null && !messagesList.isEmpty()) {
            mAdapter = new MessageAdapter(this, messagesList, true);
            setListAdapter(mAdapter);
        } else {
            loadNoMessageToExport();
        }
    }
    

    /**
     * rereads the configuration and updates the start screen
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {

        super.onResume();
        //getCredentials();

        setExportListAdapter(getSmsFromPhoneInbox());
    }
    
    
    /** getting list of messages from device inbox */
    private ArrayList<MyMessage> getSmsFromPhoneInbox() {

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

    /**
     * updates the members according to the changed preference
     * 
     * @see android.content.SharedPreferences.OnSharedPreferenceChangeListener#onSharedPreferenceChanged(android.content.SharedPreferences,
     *      java.lang.String)
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        try {
            mOutgoingMailServerConnection = new OutgoingMailServerConnection(sharedPreferences.getString("googleAddress", ""), sharedPreferences.getString("googlePassword", ""));
            mIncomingMailServerConnection = new IncomingMailServerConnection(sharedPreferences.getString("googleAddress", ""), sharedPreferences.getString("googlePassword", ""));
        } catch (Exception e) {
            Log.e(MessageExportActivity.class.getSimpleName(), e.getMessage());
        }
    }
    
    private void getPreferences() {
        
        try {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        } catch (Exception e) {
            Log.e(MessageImportActivity.class.getSimpleName(), e.getMessage());
        }
    }

    /**
     * reads configuration
     */
    
    private void getOutgoingMailServerConnection() {
        
       if(mPreferences == null)
           getPreferences();
       
        /* initialize configured email address */
        try {
            mOutgoingMailServerConnection = new OutgoingMailServerConnection(mPreferences.getString("googleAddress", ""),mPreferences.getString("googlePassword", ""));
        } catch (Exception e) {
            Log.e(MessageExportActivity.class.getSimpleName(), e.getMessage());
        }
    }
    
    /**
     * A handler to handle 
     * response from Mail API/Server 
     * */
    
    @SuppressWarnings("unchecked")
    private Handler mExportHandler = new Handler(){ 
        
        public void handleMessage(android.os.Message msg) {
                SMSWrapperData<MyMessage> responseData = (SMSWrapperData<MyMessage>) msg.obj;
                toastExportMessage(responseData.responseErrorCode);    
            
        };
    };

    /**
     * setting a handler
     * to handle exception
     * messages
     * */
    
    private void toastExportMessage(int errorCode) {

        switch(errorCode){
            
        case 0:
                Toast toastSuccess = Toast.makeText(MessageExportActivity.this,
                    "Messages exported successfully", Toast.LENGTH_SHORT);
                toastSuccess.show();

                break;            
        case 1:
            Toast toastNetwork = Toast.makeText(MessageExportActivity.this,
                    "Network is not available : Please check device settings", Toast.LENGTH_SHORT);
            toastNetwork.show();

            break;
        case 2:
            Toast toastAuthentication = Toast.makeText(MessageExportActivity.this,
                    "Authentication failed : Please check settings", Toast.LENGTH_SHORT);
            toastAuthentication.show();

            break;
        case 3:
            Toast toastDuplicateInboxSms = Toast.makeText(MessageExportActivity.this,
                    "Export aborted : Please selecet a message to export", Toast.LENGTH_SHORT);
            toastDuplicateInboxSms.show();

            break;
        default:
            Toast toastUnidentified = Toast.makeText(MessageExportActivity.this,
                    "Unidentified error occurred : Please try later", Toast.LENGTH_LONG);
            toastUnidentified.show();

            break;
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
     * method for getting progress dialogs
     * @param dialogMessage :
     *                     the message to be shown in dialog
     * */
    private ProgressDialog getDialog(String dialogMessage) {
        ProgressDialog progressDialog; 
        progressDialog = new ProgressDialog(MessageExportActivity.this);
        progressDialog.setMessage(dialogMessage);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(true);
        return progressDialog;
    }    
    
    /**
     * loading no message to export
     * */
    private void loadNoMessageToExport() {
        TextView mEmptyListView;
        ListView mListView = getListView();
            mEmptyListView = new TextView(MessageExportActivity.this);
            mEmptyListView.setLayoutParams(new LayoutParams(
                            LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            mEmptyListView.setGravity(Gravity.CENTER);
            mEmptyListView.setText("NO MESSAGES AVAILABLE FOR EXPORT");
            mEmptyListView.setTextSize(24);
            mEmptyListView.setVisibility(View.GONE);
            ((ViewGroup) mListView.getParent()).addView(mEmptyListView);
            mListView.setEmptyView(mEmptyListView);
    }
    
    /**
     * reads configuration
     */
    private IncomingMailServerConnection getIncomingMailServerConnection() {
        SharedPreferences preferences = null;
        try {
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        } catch (Exception e) {
            Log.e(MessageImportActivity.class.getSimpleName(), e.getMessage());
        }
        /* initialize configured email address */
        mIncomingMailServerConnection = new IncomingMailServerConnection(preferences.getString("googleAddress", ""),preferences.getString("googlePassword", ""));
        return mIncomingMailServerConnection;
    }

}