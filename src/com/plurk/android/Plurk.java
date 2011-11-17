/*
 * Package: Plurk Android OAuth
 * Author: Kenny Lee <kswlee@gmail.com>
 */
package com.plurk.android;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

import com.plurk.android.PlurkAuthDialog.DialogListener;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import oauth.signpost.http.HttpResponse;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.tam.plurkandroid.R;

public class Plurk {
	static public Plurk This = null;
	static public String REQUEST_URL = "http://www.plurk.com/OAuth/request_token";
	static public String ACCESS_URL = "http://www.plurk.com/OAuth/access_token";
	static public String AUTHORIZE_URL = "http://www.plurk.com/m/authorize";
	static public String CALLBACK_URL = "local://callback";
	static public String user_aithorize_url = "";
	static public String API_BASE = "http://www.plurk.com/APP/";
	static public String UPLOAD_PHOTO_API = "Timeline/uploadPicture";
	
	protected OAuthConsumer consumer = null;
	protected OAuthProvider provider = null;
	
	private Activity mActivity = null;
	private String mConsumerKey = "";
	private String mConsumerSecret = "";
	private static String mTokenVerifier = "";
	static private String mToken = "";
	static private String mSecret = "";
	
	private PlurkAuthDialog mAuthDialog = null;
	
	public interface OnRequestListener{
		abstract void onComplete(String response);
		abstract void onError();
	};
	
	public Plurk(Activity activity, String consumerKey, String consumerSecret){
		This = this;
		mActivity = activity;
		
		mConsumerKey = consumerKey;
		mConsumerSecret = consumerSecret;
		
    	this.consumer = new CommonsHttpOAuthConsumer(mConsumerKey, mConsumerSecret);
        this.provider = new CommonsHttpOAuthProvider(REQUEST_URL,
        											 ACCESS_URL,
        											 AUTHORIZE_URL);
	}
	
	public String getToken(){
		return mToken;
	}
	
	public String getSecret(){
		return mSecret;
	}
	
	public void request(String API, List<NameValuePair> list, OnRequestListener listener){
		new AsyncRequestTask(API, list, listener).execute("");
	}
	
	public String composeURL(String API, List<NameValuePair> list){
		if (null == list){
			return API;
		}
		
		String ret = API + ((list.size() > 0)? "?" : "");
		
		Iterator<NameValuePair> i = list.iterator();
		while(i.hasNext()){
			NameValuePair nvp = i.next();
			ret = String.format("%s%s=%s&", ret, nvp.getName(), nvp.getValue());
		}
		
		return ret;
	}
	
	public String request(String API, List<NameValuePair> list){
		Log.d("PROFILE", "begin request");
		consumer.setTokenWithSecret(mToken, mSecret);
		
        // create an HTTP request to a protected resource
        URL url;
        String content = "";
		try {
			HttpClient client = new DefaultHttpClient();
	        HttpGet request = new HttpGet(API_BASE + composeURL(API, list));
  
	        consumer.sign(request);
	        // send the request
	        content = client.execute(request, new BasicResponseHandler());
			Log.d("TAG",content);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OAuthMessageSignerException e) {
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			e.printStackTrace();
		} 
		Log.d("PROFILE", "end request");
		return content;
	}
	
	public String requestToken(){    	
        String url = "";
        try {
            url = provider.retrieveRequestToken(consumer, CALLBACK_URL);
        } catch (Exception e) {}
        
        return url;
	}
	
	public void setTokenAndSecret(String token, String verifier){
		mToken = token;
		mSecret = verifier;
	}
	
	public void reset(){
		setTokenAndSecret("", "");
	}
	
	public String getAccessToken(String verifier){
		String s = "";
		try {
			provider.retrieveAccessToken(consumer, verifier);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return consumer.getTokenSecret();
	}
	
	static public void parseTokenAndSecret(String url){
		try {
			List<NameValuePair> list = URLEncodedUtils.parse(new URI(url), "utf-8");
			for (int i = 0; i < list.size(); ++i){
				NameValuePair nvp = list.get(i);
				if (nvp.getName().compareTo("oauth_token") == 0){
					mToken = nvp.getValue();
				}else if (nvp.getName().compareTo("oauth_verifier") == 0){
					mTokenVerifier = nvp.getValue();
				}
			}
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		String tokenSecret = This.getAccessToken(mTokenVerifier);
		mToken = This.consumer.getToken();
		mSecret = This.consumer.getTokenSecret();
	}
	
	public void authroize(final OnAuthorizeListener listener){
		if (mToken.length() > 0){
			return;
		}
		user_aithorize_url = requestToken();
		
		mAuthDialog = new PlurkAuthDialog(mActivity, user_aithorize_url, new DialogListener(){
			@Override
			public void onComplete(Bundle values) {
				if (listener != null){
					listener.onAuthorized();
				}
			}

			@Override
			public void onError() {
				if (listener != null){
					listener.onError("Error authorize.");
				}
			}

			@Override
			public void onCancel() {
				if (listener != null){
					listener.onError("User cancelled.");
				}
			}});
		mAuthDialog.show();
		/*
		Intent intent = new Intent(activity, com.tam.ladyplurk.PlurkOAuthActivity.class);
		activity.startActivityForResult(intent, Configuration.OAUTH_REQUEST);*/
	}
	
	public interface OnAuthorizeListener{
		public abstract void onAuthorized();
		public abstract void onError(String error);
	};
	
	public void AsyncUploadPhoto(File image, OnRequestListener listener){
		new AsyncPhotoTask(image, listener).execute("");
	}
	
	private class AsyncPhotoTask extends AsyncTask<String, Integer, String> {
		File image = null;
		OnRequestListener listener = null;
		public AsyncPhotoTask(File image, OnRequestListener listener){
			this.image = image;
			this.listener = listener;
		}
		
		protected String doInBackground(String... urls) {
			return uploadPhoto(image);
		}

		protected void onPostExecute(String result) {
			if (listener != null){
				listener.onComplete(result);
			}
		}
	}
	
	public String uploadPhoto(File image){
		consumer.setTokenWithSecret(mToken, mSecret);

		String content = "";
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(API_BASE + UPLOAD_PHOTO_API);
			MultipartEntity entity = new MultipartEntity(); 
			entity.addPart("image", new FileBody(image));
			httppost.setEntity(entity);
			consumer.sign(httppost);
			// send the request
			content = httpclient.execute(httppost, new BasicResponseHandler());
			Log.d("TAG", "upload result = " + content);
		  
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.d("TAG", "upload result = " + content);
		} catch (IOException e) {
			e.printStackTrace();
			Log.d("TAG", "upload result = " + content);
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return content;
	}
	
	public void isTokenValid(final OnRequestListener listener){
		request("Profile/getOwnProfile", null, new OnRequestListener(){
			@Override
			public void onComplete(String response) {
				if (response == null || response.length() <= 0){
					reset();
					listener.onError();
				}else{
					listener.onComplete(response);
				}
			}

			@Override
			public void onError() {}
		});
	}
	
	private class AsyncRequestTask extends AsyncTask<String, Integer, String> {
		String API = "";
		List<NameValuePair> list = null;
		OnRequestListener listener = null;
		
		public AsyncRequestTask(String API, List<NameValuePair> list, OnRequestListener listener){
			this.API = API;
			this.list = list;
			this.listener = listener;
		}
		
		protected String doInBackground(String... urls) {
			String obj = request(API, list);
			Log.d("TAG", obj);
			return obj;
		}

		protected void onPostExecute(String result) {
			if (listener != null){
				listener.onComplete(result);
			}
		}
	}
}
