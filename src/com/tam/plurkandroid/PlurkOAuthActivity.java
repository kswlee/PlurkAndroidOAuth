package com.tam.plurkandroid;

import com.plurk.android.Plurk;
import com.plurk.android.Plurk.OnAuthorizeListener;
import com.plurk.android.Plurk.OnRequestListener;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class PlurkOAuthActivity extends Activity {
	private Plurk mPlurk = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        /*
         * Need to apply the consumer key and secret from http://www.plurk.com/PlurkApp/register
         */
        String appKey = "";
        String appSecret = "";
        
        mPlurk = new Plurk(this, appKey, appSecret);
        mPlurk.authroize(new OnAuthorizeListener(){
			@Override
			public void onAuthorized() {
				getUserInfo();
			}

			@Override
			public void onError(String error) {	
			}
        	
        });
    }
    
    private void getUserInfo(){
    	mPlurk.request("Profile/getOwnProfile", null, new OnRequestListener(){

			@Override
			public void onComplete(String response) {
				Log.d("Plurk", response);
			}

			@Override
			public void onError() {
				Log.d("Plurk", "onError");
			}
    		
    	});
    }
}