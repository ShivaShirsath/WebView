package ss.WebView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.webkit.DownloadListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import java.io.File;
import java.util.Date;

public class MainActivity extends AppCompatActivity{
    private Activity activity;
    private WebView webView;
	private DrawerLayout drawer_layout;
	private NavigationView left_nav, right_nav;
	private String git = "https://github.com/", user = "ShivaShirsath", tab = "?tab=", link = git + user;
	private String CM;  
	private ValueCallback<Uri> UM;  
	private ValueCallback<Uri[]> UMA;
	private SwipeRefreshLayout refresh;
	private long backPressedTime=0;
	private View fullScreen;

	@SuppressLint({"SetJavaScriptEnabled", "ObsoleteSdkInt"})

	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		
		fullScreen = getWindow().getDecorView();
		fullScreen.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener(){
			@Override
			public void onSystemUiVisibilityChange(int visibility){
				if(visibility==0)
					fullScreen.setSystemUiVisibility(getVisibility());
			}
		});

        webView = findViewById(R.id.webView);
		
        webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(WebView view, String url){
				webView.setVisibility(View.VISIBLE);
			}
		});

        webView.loadUrl(link);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);

        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);

        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false); 

        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);


		if(Build.VERSION.SDK_INT >= 21){  
			settings.setMixedContentMode(0);  
			webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);  
		}else if(Build.VERSION.SDK_INT >= 19){  
			webView.setLayerType(View.LAYER_TYPE_HARDWARE, null);  
		}else if(Build.VERSION.SDK_INT < 19){  
			webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);  
		}
		
		webView.setWebViewClient(new Callback());  
		webView.setWebChromeClient(new WebChromeClient() {  
				//For Android 3.0+  
				public void openFileChooser(ValueCallback<Uri> uploadMsg){
					fileChooser(uploadMsg);
				}  
				// For Android 3.0+, above method not supported in some android 3+ versions, in such case we use this  
				public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType){
					fileChooser(uploadMsg);
				}  
				//For Android 4.1+  
				public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture){
					fileChooser(uploadMsg);
				}  
				//For Android 5.0+  
				public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams){  
					if (UMA != null){  
						UMA.onReceiveValue(null);  
					}  
					UMA = filePathCallback;  
					Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
					if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null){  
						File photoFile = null;  
						try{  
							@SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());  
							String imageFileName = "img_" + timeStamp + "_";  
							File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);  
							photoFile = File.createTempFile(imageFileName, ".jpg", storageDir);  
							takePictureIntent.putExtra("PhotoPath", CM);  
						}catch (Exception e){  
							Toast.makeText(MainActivity.this, e.getMessage().toString(), Toast.LENGTH_LONG).show();
						}
						if (photoFile != null){  
							CM = "file:" + photoFile.getAbsolutePath();  
							takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));  
						}else{  
							takePictureIntent = null;  
						}  
					}   
					Intent[] intentArray;  
					if (takePictureIntent != null){  
						intentArray = new Intent[]{takePictureIntent};  
					}else{  
						intentArray = new Intent[0];  
					}  
					Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);  
					chooserIntent.putExtra(Intent.EXTRA_INTENT, fileChooser(null));  
					chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose an Action");  
					chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);  
					startActivityForResult(chooserIntent, 1);  
					return true;  
				}  
			});  
		webView.setDownloadListener(new DownloadListener() {       
			@Override
			public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength){
				setDownload("Downloading...", contentDisposition.replace("attachment; filename=", "").replace("-main", ""), url);
			}
		});
		refresh = (SwipeRefreshLayout)findViewById(R.id.swipeToRefresh);
		refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
			@Override
			public void onRefresh(){
				webView.loadUrl(webView.getUrl());
				refresh.setRefreshing(false);
			}
		});
		try{
			drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);

            left_nav = (NavigationView)findViewById(R.id.left_nav);
            left_nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
				@Override
                public boolean onNavigationItemSelected(MenuItem item){
					switch (item.getItemId()){
						case R.id.item_user     : link = git + 								item.getTitle();				break;
						case R.id.item_newRepo  : link = git + 				String.valueOf(	item.getTitle()).toLowerCase();	break;
						case R.id.item_repo     : link = git + user + tab + String.valueOf(	item.getTitle()).toLowerCase();	break;
						case R.id.item_project  : link = git + user + tab + String.valueOf(	item.getTitle()).toLowerCase();	break;
						case R.id.item_package  : link = git + user + tab + String.valueOf(	item.getTitle()).toLowerCase();	break;
						case R.id.item_settings : link = git + 				String.valueOf(	item.getTitle()).toLowerCase();	break;
					}
					webView.loadUrl(link);
					drawer_layout.closeDrawers();
					return true;
				}
			});
            right_nav = (NavigationView)findViewById(R.id.right_nav);
            right_nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener(){
				@Override
				public boolean onNavigationItemSelected(MenuItem item){
                    switch (item.getItemId()){
						case R.id.item_help         : link = "https://docs.github.com/"; break;
						case R.id.item_openInChrome : startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webView.getUrl())).setPackage("com.android.chrome"));
					}
					webView.loadUrl(link);
					drawer_layout.closeDrawers();
					return true;
				}
			});
			
		}catch (Exception e){
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
	@Override  
	protected void onActivityResult(int requestCode, int resultCode, Intent intent){  
		super.onActivityResult(requestCode, resultCode, intent);  
		if (Build.VERSION.SDK_INT >= 21){  
			Uri[] results = null;  
			//Check if response is positive  
			if(resultCode == Activity.RESULT_OK){  
				if(requestCode == 1){  
					if(null == UMA){  
						return;  
					}  
					if(intent == null){  
						//Capture Photo if no image available  
						if(CM != null){  
							results = new Uri[]{Uri.parse(CM)};  
						}  
					}else{  
						String dataString = intent.getDataString();  
						if(dataString != null){  
							results = new Uri[]{Uri.parse(dataString)};  
						}  
					}  
				}  
			}  
			UMA.onReceiveValue(results);  
			UMA = null;  
		}else{  
			if(requestCode == 1){  
				if (null == UM) return;  
				Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();  
				UM.onReceiveValue(result);  
				UM = null;  
			}  
		}  
	}        
	public Intent fileChooser(ValueCallback<Uri> uploadMsg){
		UM = uploadMsg;  
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
		intent.addCategory(Intent.CATEGORY_OPENABLE);  
		intent.setType("*/*");
		if(uploadMsg != null) MainActivity.this.startActivityForResult(Intent.createChooser(intent, "File Chooser"), 1);
		return intent;
	}
	public void setDownload(String msg,String fileName, String url){
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
		request.allowScanningByMediaScanner();
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //Notify client once download is completed!
		request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
		((DownloadManager) getSystemService(DOWNLOAD_SERVICE)).enqueue(request);
		Toast.makeText(getApplicationContext(), msg + " ( " + fileName + " )", Toast.LENGTH_SHORT).show();
	}
	public int getVisibility(){
		return View.SYSTEM_UI_FLAG_LAYOUT_STABLE; 
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		if(hasFocus)
			fullScreen.setSystemUiVisibility(getVisibility());
		super.onWindowFocusChanged(hasFocus);
	}
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
	}
	@Override
	protected void onSaveInstanceState(Bundle outState){
		super.onSaveInstanceState(outState);
		webView.saveState(outState);
	}
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState){
		super.onRestoreInstanceState(savedInstanceState);
		webView.restoreState(savedInstanceState);
	}
	@Override
	public void onBackPressed(){
		if(drawer_layout.isDrawerOpen(left_nav) || drawer_layout.isDrawerOpen(right_nav)){
			drawer_layout.closeDrawers();
		}else if(webView.canGoBack()){
			webView.goBack();
		}else if (backPressedTime + 2000 > System.currentTimeMillis()) {
			super.onBackPressed();
			finish();
			return;
		} 
		else {
			Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
		}
		backPressedTime = System.currentTimeMillis();
	}
	//Inside Class
	public class Callback extends WebViewClient{
		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl){   
			Toast.makeText(getApplicationContext(), "Failed loading app!", Toast.LENGTH_SHORT).show();   
		}
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if(url.contains("github")) {
				if(url.contains("raw")){
					setDownload(user+"@GitHub/"+url.substring(url.lastIndexOf("/")+1), user+"@GitHub/"+url.substring(url.lastIndexOf("/")+1), url);
				}else{
					view.loadUrl(url);
				}
			} else {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
			}
			return true;
		}
	}
}


