
package com.mobilleon.smswrapper.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.internal.widget.IcsAdapterView;
import com.actionbarsherlock.internal.widget.IcsAdapterView.OnItemSelectedListener;
import com.mobilleon.smswrapper.adapters.AboutAppAdapter;
import com.mobilleon.smswrapper.asynctasks.PaymentTask;
import com.mobilleon.smswrapper.model.AboutAppAuthorItem;
import com.mobilleon.smswrapper.root.R;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("HandlerLeak")
public class AboutTheAppFragment extends SherlockFragment implements OnItemSelectedListener, OnClickListener{

    private String TAG = AboutTheAppFragment.class.getSimpleName();
    private AboutAppAdapter mAdapter;
    private List<AboutAppAuthorItem> mAboutItems;
    private ListView mAboutTheAppList;
    private LinearLayout mBrandContainer;
    
    private static final int request = 1; // the value used is up to you
    
    protected static final int INITIALIZE_SUCCESS = 0;
    protected static final int INITIALIZE_FAILURE = 1;

    ScrollView scroller;
    TextView labelPayment;
    EditText donationAmount;

    LinearLayout layoutPayment;

    CheckoutButton mCheckoutButton;
    
    // These are used to display the results of the transaction
    public static String resultTitle;
    public static String resultInfo;
    public static String resultExtra;
    
    
	public static Fragment newInstance(Context context) {
		AboutTheAppFragment appFragment = new AboutTheAppFragment();	
		
		return appFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		ViewGroup root = (ViewGroup) inflater.inflate(R.layout.about_the_app_fragment, null);
		mAboutTheAppList = (ListView) root.findViewById(R.id.app_list);
		mBrandContainer = (LinearLayout) root.findViewById(R.id.brand_container);
		getAboutTheAppListItem();
		setAboutTheAppList();
		setListListener();
		// Initialize the library. We'll do it in a separate thread because it requires communication with the server
        // which may take some time depending on the connection strength/speed.
        
        new PaymentTask(getSherlockActivity(), paypalHandler).execute();
		initUI(root);
		return root;
	}
	
    // This handler will allow us to properly update the UI. You cannot touch Views from a non-UI thread.
    Handler hRefresh = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case INITIALIZE_SUCCESS:
                    setupButtons();
                    break;
                case INITIALIZE_FAILURE:
                    showFailure();
                    break;
            }
        }
    };

	
	private void setListListener() {
	    mAboutTheAppList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if(position == 0){
                    //Toast.makeText(getActivity(), "Coming Soon..", Toast.LENGTH_SHORT).show();
                    Intent sendMailIntent = new Intent(Intent.ACTION_SEND); 
                    sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, "SMS Wrapper Link");
                    sendMailIntent.putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.mobilleon.smswrapper.root");
                    sendMailIntent.putExtra(Intent.EXTRA_TITLE, "It's AWESOME");
                    sendMailIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendMailIntent, "Share Using"));
                }
                if(position == 1){
                    //Toast.makeText(getActivity(), "Coming Soon..", Toast.LENGTH_SHORT).show();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.mobilleon.smswrapper.root"));
                    startActivity(browserIntent); 
                }
                if(position == 2){
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "SMS Wrapper Feedback");
                    //intent.putExtra(Intent.EXTRA_TEXT, "message");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"kaushik.atul@gmail.com"});
                    intent.setType("message/rfc822");
                    Intent mailer = Intent.createChooser(intent, null);
                    startActivity(mailer);
                } 
            }
        });
    }
	
    /** Setting adapter for list view */
    private void setAboutTheAppList() {

        if (!mAboutItems.isEmpty()) {
            mAdapter = new AboutAppAdapter(getSherlockActivity(), mAboutItems);
            mAboutTheAppList.setAdapter(mAdapter);
        } else {
            mAboutTheAppList.setAdapter(null);
        }
    }
    
    private void getAboutTheAppListItem(){
        mAboutItems = new ArrayList<AboutAppAuthorItem>();
        AboutAppAuthorItem mShareAppItem = new AboutAppAuthorItem();
        AboutAppAuthorItem mRateAppItem = new AboutAppAuthorItem();
        AboutAppAuthorItem mFeedbackAppItem = new AboutAppAuthorItem();
        
        mShareAppItem.setmImageIcon(getResources().getDrawable(R.drawable.ic_share_app));
        mShareAppItem.setmItemHeader("Share this app!");
        mShareAppItem.setmItemDescription("Tell your friends how great it is");
    
    mAboutItems.add(mShareAppItem);
    
    mRateAppItem.setmImageIcon(getResources().getDrawable(R.drawable.ic_rating_good));
    mRateAppItem.setmItemHeader("Rate this app on market!");
    mRateAppItem.setmItemDescription("Is it worth 5 stars");
    
    mAboutItems.add(mRateAppItem);
    
    mFeedbackAppItem.setmImageIcon(getResources().getDrawable(R.drawable.ic_feedback));
    mFeedbackAppItem.setmItemHeader("Send me your feedback!");
    mFeedbackAppItem.setmItemDescription("To make this app better");
    
    mAboutItems.add(mFeedbackAppItem);
    }

    @Override
    public void onItemSelected(IcsAdapterView<?> parent, View view, int position, long id) {
        Toast.makeText(getSherlockActivity(), "let's see if it works", Toast.LENGTH_SHORT).show();
        
    }

    @Override
    public void onNothingSelected(IcsAdapterView<?> parent) {
        // TODO Auto-generated method stub
        
    }
    
    private void initUI(View root) {
        labelPayment = (TextView) root.findViewById(R.id.paypal_support_initiative); 
        layoutPayment = (LinearLayout) root.findViewById(R.id.paypal_button);
         
        donationAmount = (EditText) root.findViewById(R.id.donation_amount);
        labelPayment.setText("Support the initiative");
        //labelPayment.setVisibility(View.GONE);
        
    }
    
    private Handler paypalHandler = new Handler(){
        
        public void handleMessage(android.os.Message msg) {// The library is initialized so let's create our CheckoutButton and update the UI.
            if (PayPal.getInstance().isLibraryInitialized()) {
                hRefresh.sendEmptyMessage(INITIALIZE_SUCCESS);
            }
            else {
                hRefresh.sendEmptyMessage(INITIALIZE_FAILURE);
            }};
    
    }; 
     
    
    /**
     * Create our CheckoutButton and update the UI.
     */
    public void setupButtons() {
        PayPal pp = PayPal.getInstance();
        // Get the CheckoutButton. There are five different sizes. The text on the button can either be of type TEXT_PAY or TEXT_DONATE.
        mCheckoutButton = pp.getCheckoutButton(getSherlockActivity(), PayPal.BUTTON_194x37, CheckoutButton.TEXT_DONATE);
        // You'll need to have an OnClickListener for the CheckoutButton. For this application, MPL_Example implements OnClickListener and we
        // have the onClick() method below.
        mCheckoutButton.setOnClickListener(this);
        // The CheckoutButton is an android LinearLayout so we can add it to our display like any other View.
        layoutPayment.addView(mCheckoutButton);
                
        // Show our labels and the preapproval EditText.
        labelPayment.setVisibility(View.VISIBLE);
        //appVersion.setVisibility(View.VISIBLE);
        
    }
    
    /**
     * Show a failure message because initialization failed.
     */
    public void showFailure() {
        Toast.makeText(getSherlockActivity(), "error occured while initializing paypal payment. please try later", Toast.LENGTH_SHORT).show();
    }
    
    
    public void onClick(View v) {
        
        /**
         * For each call to checkout() and preapprove(), we pass in a ResultDelegate. If you want your application
         * to be notified as soon as a payment is completed, then you need to create a delegate for your application.
         * The delegate will need to implement PayPalResultDelegate and Serializable. See our ResultDelegate for
         * more details.
         */     
        
        if(v == mCheckoutButton) {
            
            Log.v(TAG, "amt = "+donationAmount.getText().toString().trim());
            Log.v(TAG, "hint = "+donationAmount.getHint().toString());
            
            if(donationAmount.getText().toString().length() > 0 || donationAmount.getHint().equals("1.00 (default amount in USD)")){
                
                if(donationAmount.getText().toString().length() == 0 && donationAmount.getHint().equals("1.00 (default amount in USD)")){
                    donationAmount.setText("1");
                }

                PayPalPayment newPayment = new PayPalPayment();
                newPayment.setSubtotal(new BigDecimal(Integer.parseInt(donationAmount.getText().toString())));
                newPayment.setCurrencyType("USD");
                newPayment.setRecipient("kaushik.atul@gmail.com");
                newPayment.setPaymentType(PayPal.PAYMENT_TYPE_NONE);
                newPayment.setPaymentSubtype(PayPal.PAYMENT_SUBTYPE_DONATIONS);
                newPayment.setMerchantName("Support the initiative");
                // Sets the memo. This memo will be part of the notification sent by PayPal to the necessary parties.
                newPayment.setMemo("Thanks for the contribution");
                Intent paypalIntent = PayPal.getInstance().checkout(newPayment, getSherlockActivity());
                startActivityForResult(paypalIntent, request);
            }else{
                Toast.makeText(getSherlockActivity(), "Please enter the amount you want to donate", Toast.LENGTH_LONG).show();
                donationAmount.setHint("1.00 (default amount in USD)");
            }
            
            mCheckoutButton.updateButton();
        }/* else if(v == exitApp) {
            // The exit button was pressed, so close the application.
            finish();
        }*/
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode != request)
            return;
        
        /**
         * If you choose not to implement the PayPalResultDelegate, then you will receive the transaction results here.
         * Below is a section of code that is commented out. This is an example of how to get result information for
         * the transaction. The resultCode will tell you how the transaction ended and other information can be pulled
         * from the Intent using getStringExtra.
         */
        switch(resultCode) {
        case Activity.RESULT_OK:
            Toast.makeText(getSherlockActivity(), "You have successfully completed this trasaction", Toast.LENGTH_SHORT).show();
            
            break;
        case Activity.RESULT_CANCELED:
            Toast.makeText(getSherlockActivity(), "You have cancelled this trasaction", Toast.LENGTH_SHORT).show();
            
            break;
        case PayPalActivity.RESULT_FAILURE:
            Toast.makeText(getSherlockActivity(), "This trasaction has failed", Toast.LENGTH_SHORT).show();
            
        }
        
        mCheckoutButton.updateButton();
    }
}
