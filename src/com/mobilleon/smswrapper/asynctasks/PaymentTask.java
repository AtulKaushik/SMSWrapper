/**
     * The PaymentTask takes care of all the basic Paypal Library initialization.
     *  
     */
    
package com.mobilleon.smswrapper.asynctasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;

import com.paypal.android.MEP.PayPal;

public class PaymentTask extends AsyncTask<Void, Void, Void>{
    
    private Handler mHandler;
    private Activity mActivity;
    
    // The PayPal server to be used - can also be ENV_NONE and ENV_LIVE
    private static final int server = PayPal.ENV_LIVE;
    
    // The ID of your application that you received from PayPal
    private static final String appID = "APP-66T93398XL262312U";
    
    public PaymentTask(Activity mActivity, Handler mHandler){
        super();
        this.mHandler = mHandler;
        this.mActivity = mActivity;
    }

    @Override
    protected Void doInBackground(Void... params) {


        PayPal pp = PayPal.getInstance();
        // If the library is already initialized, then we don't need to initialize it again.
        if(pp == null) {
            pp = PayPal.initWithAppID(mActivity, appID, server);
            
            pp.setLanguage("en_US"); // Sets the language for the library.
            
            pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER); 
            // Set to true if the transaction will require shipping.
            pp.setShippingEnabled(true);
            pp.setDynamicAmountCalculationEnabled(false);
        }
    
        return null;
    }
    
    @Override
    protected void onPostExecute(Void result) {
        mHandler.sendEmptyMessage(0);
        super.onPostExecute(result);
    }

}
