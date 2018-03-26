package com.mobilleon.smswrapper.utilities;

import com.mobilleon.smswrapper.utilities.NetworkUtility.CONNECTION;

/**
 * @author Atul Kaushik (kaushik.atul@gmail.com)
 *
 */
public class Constants {

	public static final String CONSUMER_KEY = "3mX4CbQTPKUIUOBnOOHbQ"; /*FILL IN YOUR CONSUMER KEY FROM TWITTER HERE*/
	public static final String CONSUMER_SECRET= "x0OP3at8SzhGmBmPNxchlzZVGxYNcKH4KHYV4UrKa8"; /*FILL IN YOUR CONSUMER SECRET FROM TWITTER HERE*/
	
    public static final String FACEBOOK_APPID = "510210235691119"; /*PUT YOUR FACEBOOK APP REGISTRATION ID HERE*/
    public static final String FACEBOOK_PERMISSION = "publish_stream";

	
	public static final String REQUEST_URL = "http://api.twitter.com/oauth/request_token";
	public static final String ACCESS_URL = "http://api.twitter.com/oauth/access_token";
	public static final String AUTHORIZE_URL = "http://api.twitter.com/oauth/authorize";
	
	public static final String	OAUTH_CALLBACK_SCHEME	= "x-oauthflow-twitter";
	public static final String	OAUTH_CALLBACK_HOST		= "callback";
	public static final String	OAUTH_CALLBACK_URL		= OAUTH_CALLBACK_SCHEME + "://" + OAUTH_CALLBACK_HOST;

	public static CONNECTION INTERNET_CONNECTION;
}

