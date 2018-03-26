package com.mobilleon.smswrapper.fragment;

import com.mobilleon.smswrapper.activity.SMSWrapperActivity;
import com.mobilleon.smswrapper.activity.SMSWrapperSettingsActivity;
import com.mobilleon.smswrapper.activity.SpanningActivity;
import com.mobilleon.smswrapper.root.R;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class AccountManagerDialog extends DialogFragment{
    
    private Account mAccount;
    private AccountManager mAccountManager;
    private String TAG = AccountManagerDialog.class.getSimpleName();
    
    private TextView mUser;
    private EditText mUsername;
    private EditText mPassword;
    private CheckBox mCheckBox;
    
    private String usernameString;
    private String passwordString;
    
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        mAccount = getExistingUser();
       

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_account_settings, null);
        initUI(view);
        
        mUser.setText("Hello "+mAccount.name.split("@")[0]);
        mUsername.setText(mAccount.name);
        builder.setView(view)
       .setPositiveButton("  CONFIRM  ", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               if(validateCredentials()){
                   setCredentials();
                   SpanningActivity mActivity = (SpanningActivity) getActivity(); 
                   Intent intent = new Intent(mActivity, SMSWrapperActivity.class);
                   startActivity(intent);
                   mActivity.finish();   
               }else{
                   new AccountManagerDialog().show(getFragmentManager(), TAG);  
               }
           }
       })
       .setNegativeButton("NEW ACCOUNT", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int id) {
               SpanningActivity mActivity = (SpanningActivity) getActivity(); 
               Intent intent = new Intent(mActivity, SMSWrapperSettingsActivity.class);
               startActivity(intent);
               mActivity.finish();
           }
       });    
    
        return builder.create();
       
    }
    
    private void initUI(View view) {
        mUser = ((TextView) view.findViewById(R.id.user));
        mUsername = ((EditText) view.findViewById(R.id.username));
        mPassword = ((EditText) view.findViewById(R.id.password));
        mCheckBox = (CheckBox) view.findViewById(R.id.acc_mgr_pref);
    }
    
    @Override
    public void dismissAllowingStateLoss() {
        // TODO Auto-generated method stub
       // super.dismissAllowingStateLoss();
    }

    private Account getExistingUser() {
        mAccountManager = AccountManager.get(getActivity()); 
        Account[] accounts = mAccountManager.getAccountsByType("com.google");
        if(accounts.length>0){
            
            return accounts[0];    
        }    
            return null;
    }
    
    /**
     * writes configuration
     */
    private void setCredentials() {
        SharedPreferences.Editor editor;
        try {
            editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            editor.putString("googleAddress", usernameString);
            editor.putString("googlePassword", passwordString);
            editor.putBoolean("remember", mCheckBox.isChecked());
            editor.putBoolean("rememberSession", !mCheckBox.isChecked());
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     
    private boolean validateCredentials() {

         usernameString = mUsername.getText().toString();
         passwordString = mPassword.getText().toString();
        boolean flag = false;
        if (usernameString.length() > 5 && usernameString.contains("@") && usernameString.contains(".")) {
            if (passwordString.length() > 3) {
                flag = true;
            }
        }
        return flag;
    }
}
