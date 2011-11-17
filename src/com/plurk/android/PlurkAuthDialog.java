package com.plurk.android;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tam.plurkandroid.R;

public class PlurkAuthDialog extends Dialog {
    static final FrameLayout.LayoutParams FILL =
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                         ViewGroup.LayoutParams.FILL_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;
	
	private DialogListener mListener = null;
	private String mUrl = "";
	private FrameLayout mContent = null;
	private ImageView mCrossImage;
    private WebView mWebView;
	
    public PlurkAuthDialog(Context context, String url, DialogListener listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar);
        mUrl = url;
        mListener = listener;
        
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mContent = new FrameLayout(getContext());
        //mContent.setBackgroundColor(0x99FFFFFF);

        /* Create the 'x' image, but don't add to the mContent layout yet
         * at this point, we only need to know its drawable width and height 
         * to place the webview
         */
        createCrossImage();
        
        /* Now we know 'x' drawable width and height, 
         * layout the webivew and add it the mContent layout
         */
        int crossWidth = mCrossImage.getDrawable().getIntrinsicWidth();
        setUpWebView(crossWidth / 2);
        
        /* Finally add the 'x' image to the mContent layout and
         * add mContent to the Dialog view
         */
        mContent.addView(mCrossImage, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mCrossImage.setVisibility(View.VISIBLE);
        addContentView(mContent, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    
    private void createCrossImage() {
        mCrossImage = new ImageView(getContext());
        // Dismiss the dialog when user click on the 'x'
        mCrossImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancel();
                PlurkAuthDialog.this.dismiss();
            }
        });
        Drawable crossDrawable = getContext().getResources().getDrawable(R.drawable.close);
        mCrossImage.setImageDrawable(crossDrawable);
        /* 'x' should not be visible while webview is loading
         * make it visible only after webview has fully loaded
        */
        mCrossImage.setVisibility(View.INVISIBLE);
    }
    
    private void setUpWebView(int margin) {
        LinearLayout webViewContainer = new LinearLayout(getContext());
        LinearLayout webViewContainerInner = new LinearLayout(getContext());
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new WebViewClient() {
        	public void onPageStarted(WebView view, String url, Bitmap favicon){
        	}
        	public void onPageFinished(WebView view, String url){
        	}
        	public boolean shouldOverrideUrlLoading(WebView view, String url){
        		
        		Log.d("TAG", "url = " + url);
        		
        		if (url.contains(Plurk.CALLBACK_URL)){
        			Plurk.parseTokenAndSecret(url);
        			Bundle b = new Bundle();
        			b.putString("token", Plurk.This.getToken());
        			b.putString("token", Plurk.This.getSecret());
        			mListener.onComplete(b);
        			dismiss();
        			return true;
        		}
        		if (url.compareToIgnoreCase("http://www.plurk.com/m/t") == 0){
        			Log.d("TAG", "redirect to login");
        			url = Plurk.user_aithorize_url;
        	    	Log.d("TAG", "redirect to login url = " + url);
        			view.loadUrl(url);
        			
        			return false;
        		}
        		
        		view.loadUrl(url);
        		return false;
        	}
        });
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mWebView.setVisibility(View.VISIBLE);
        
        webViewContainer.setPadding(margin, margin, margin, margin);
        
        webViewContainer.addView(webViewContainerInner, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        webViewContainerInner.setPadding(10, 10, 10, 5);
        Drawable d = getContext().getResources().getDrawable(R.drawable.dialog_background);
        webViewContainerInner.setBackgroundDrawable(d);
        
        webViewContainerInner.addView(mWebView);
        
        mContent.addView(webViewContainer);
        //addContentView(mWebView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    }
    
    /**
     * Callback interface for dialog requests.
     *
     */
    public static interface DialogListener {

        /**
         * Called when a dialog completes.
         *
         * Executed by the thread that initiated the dialog.
         *
         * @param values
         *            Key-value string pairs extracted from the response.
         */
        public void onComplete(Bundle values);

        /**
         * Called when a dialog has an error.
         *
         * Executed by the thread that initiated the dialog.
         *
         */
        public void onError();

        /**
         * Called when a dialog is canceled by the user.
         *
         * Executed by the thread that initiated the dialog.
         *
         */
        public void onCancel();

    }
}
