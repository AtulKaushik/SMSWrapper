
package com.mobilleon.smswrapper.fragment;

import com.actionbarsherlock.app.SherlockFragment;
import com.mobilleon.smswrapper.adapters.AboutAuthorAdapter;
import com.mobilleon.smswrapper.model.AboutAppAuthorItem;
import com.mobilleon.smswrapper.root.R;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AboutTheAuthorFragment extends SherlockFragment {

    private AboutAuthorAdapter mAdapter;
    private List<AboutAppAuthorItem> mAboutItems;
    private ListView mAboutTheAuthList;
    
    
    public static Fragment newInstance(Context context) {
        AboutTheAuthorFragment authorFragment = new AboutTheAuthorFragment();    
        
        return authorFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.about_the_author_fragment, null);
        mAboutTheAuthList = (ListView) root.findViewById(R.id.auth_list);
        getAboutTheAuthorListItem();
        setAboutTheAppList();
        setListListener();
        return root;
    }
    
    private void setListListener() {
        mAboutTheAuthList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                if(position == 0){/*
                    Intent sendMailIntent = new Intent(Intent.ACTION_VIEW); 
                    sendMailIntent.putExtra(Intent.EXTRA_SUBJECT, "SMSPro Link");
                    sendMailIntent.putExtra(Intent.EXTRA_TEXT, "https://www.facebook.com/atul.kaushik.16");
                    sendMailIntent.putExtra(Intent.EXTRA_TITLE, "It's AWESOME");
                    sendMailIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendMailIntent, "Share Using"));
                */
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/atul.kaushik.16"));
                    startActivity(browserIntent);    
                }
                if(position == 1){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/atulkaushik7"));
                    startActivity(browserIntent); 
                }
                if(position == 2){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/105170487861992373475/posts"));
                    startActivity(browserIntent); 
                }
            }
        });
    }
    
    /** Setting adapter for list view */
    private void setAboutTheAppList() {

        if (!mAboutItems.isEmpty()) {
            mAdapter = new AboutAuthorAdapter(getSherlockActivity(), mAboutItems);
            mAboutTheAuthList.setAdapter(mAdapter);
        } else {
            mAboutTheAuthList.setAdapter(null);
        }
    }
    
    private void getAboutTheAuthorListItem(){
        mAboutItems = new ArrayList<AboutAppAuthorItem>();
        AboutAppAuthorItem mFbAppItem = new AboutAppAuthorItem();
        AboutAppAuthorItem mTwitterAppItem = new AboutAppAuthorItem();
        AboutAppAuthorItem mGPlusAppItem = new AboutAppAuthorItem();
        
        mFbAppItem.setmImageIcon(getResources().getDrawable(R.drawable.ic_author_facebook));
        mFbAppItem.setmItemHeader("Find me on Facebook");
    
    mAboutItems.add(mFbAppItem);
    
    mTwitterAppItem.setmImageIcon(getResources().getDrawable(R.drawable.ic_author_twitter));
    mTwitterAppItem.setmItemHeader("Follow me on Twitter");
    
    mAboutItems.add(mTwitterAppItem);
    
    mGPlusAppItem.setmImageIcon(getResources().getDrawable(R.drawable.ic_author_googleplus));
    mGPlusAppItem.setmItemHeader("Circle me on Google+");
      
    mAboutItems.add(mGPlusAppItem);
    }

}
