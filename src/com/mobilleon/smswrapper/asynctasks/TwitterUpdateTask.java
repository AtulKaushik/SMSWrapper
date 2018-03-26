package com.mobilleon.smswrapper.asynctasks;

import com.mobilleon.smswrapper.utilities.Constants;

import oauth.signpost.OAuth;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.http.AccessToken;
import android.content.SharedPreferences;
import android.os.AsyncTask;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class TwitterUpdateTask extends AsyncTask<SharedPreferences, Void, Void>{

    private SharedPreferences prefs;
    private String msg;
    
    public TwitterUpdateTask(String msg){
        super();
        this.msg = msg;
    }
    
    @Override
    protected Void doInBackground(SharedPreferences... params) {
        
        if(params == null || params[0] == null)return null;
        prefs = params[0];
        
        String token = prefs.getString(OAuth.OAUTH_TOKEN, "");
        String secret = prefs.getString(OAuth.OAUTH_TOKEN_SECRET, "");
        
        AccessToken a = new AccessToken(token,secret);
        Twitter twitter = new TwitterFactory().getInstance();
        twitter.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
        twitter.setOAuthAccessToken(a);
        
        try {
            twitter.updateStatus(msg);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        
        return null;
    }

}
