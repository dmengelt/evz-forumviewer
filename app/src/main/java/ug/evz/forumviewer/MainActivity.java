package ug.evz.forumviewer;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.IOException;

public class MainActivity extends Activity {

    private static final String EVZ_MAIN_URL = "http://www.evz.ch";
    private static final String EVZ_FORUM_URL = EVZ_MAIN_URL + "/fans/forum";
    private static final String CSS_FILE_SUFFIX = ".css";
    private static final String PATCHED_FORUM_CSS_FILE = "forum.css";

    private WebView mWebView;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        // remote webview debugging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags &= ApplicationInfo.FLAG_DEBUGGABLE)) {
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }

        setContentView(R.layout.activity_main);

        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.getSettings().setJavaScriptEnabled(true);
        setupWebViewClient();
        setupWebChromeClient();
        setupProgressBar();

        mWebView.loadUrl(EVZ_FORUM_URL);
    }

    private void setupWebChromeClient() {
        mWebView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                mProgressBar.setProgress(progress * 100);

                if (progress >= 100) {
                    mProgressBar.setVisibility(View.INVISIBLE);
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupWebViewClient() {
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                if (url.contains(EVZ_MAIN_URL) && url.contains(CSS_FILE_SUFFIX)) {
                    Log.i(MainActivity.class.getName(), "Loading css file: " + url);
                    try {
                        return new WebResourceResponse("text/css", "UTF-8", getAssets().open(PATCHED_FORUM_CSS_FILE));
                    } catch (IOException ignored) {
                        Log.e(MainActivity.class.getName(), "Unable to replace css file!", ignored);
                    }
                }

                return super.shouldInterceptRequest(view, url);
            }
        });
    }

    private void setupProgressBar() {
        mProgressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 24));

        final FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(mProgressBar);

        ViewTreeObserver observer = mProgressBar.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View contentView = decorView.findViewById(android.R.id.content);
                mProgressBar.setY(contentView.getY() - 10);

                ViewTreeObserver observer = mProgressBar.getViewTreeObserver();
                observer.removeGlobalOnLayoutListener(this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                mWebView.reload();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (mWebView.canGoBack()) {
                        mWebView.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }

        }
        return super.onKeyDown(keyCode, event);
    }

}
