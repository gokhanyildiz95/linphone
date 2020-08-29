package org.linphone.task;

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import androidx.annotation.RequiresApi;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.linphone.R;

public class TaskFragment extends Fragment {
    private RelativeLayout mWaitLayout;
    private ViewTreeObserver.OnGlobalLayoutListener mKeyboardListener;
    private ValueCallback<Uri[]> mFilePathCallback;
    private WebView webView;
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    private String mCameraPhotoPath;
    private String domain_id = "";

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.task, container, false);
        if (getArguments() != null) {
            String jwt = getArguments().getString("jwt");
            domain_id = getArguments().getString("domain_id");
            Log.d("MOBILOG", "jwt " + jwt);
            mWaitLayout = view.findViewById(R.id.waitScreenTask);

            webView = view.findViewById(R.id.task_webview);

            try {

                String postData = "qr_code=" + URLEncoder.encode(jwt, "UTF-8");
                // todo get these settings from one place for all webviews
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setDomStorageEnabled(true);
                webSettings.setAppCacheEnabled(true);
                webSettings.setDatabaseEnabled(true);
                webSettings.setAllowFileAccess(true);
                webSettings.setAllowContentAccess(true);
                webSettings.setLoadsImagesAutomatically(true);
                webSettings.setUseWideViewPort(true);

                // Force links and redirects to open in the WebView instead of in a browser
                webView.setWebViewClient(new TaskFragment.MyWebViewClient());
                webView.setWebChromeClient(
                        new WebChromeClient() {
                            public boolean onShowFileChooser(
                                    WebView webView,
                                    ValueCallback<Uri[]> filePathCallback,
                                    WebChromeClient.FileChooserParams fileChooserParams) {
                                if (mFilePathCallback != null) {
                                    mFilePathCallback.onReceiveValue(null);
                                }
                                mFilePathCallback = filePathCallback;

                                Intent takePictureIntent =
                                        new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                if (takePictureIntent.resolveActivity(
                                                getActivity().getPackageManager())
                                        != null) {
                                    // Create the File where the photo should go
                                    File photoFile = null;
                                    try {
                                        photoFile = createImageFile();
                                        takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                                    } catch (IOException ex) {
                                        // Error occurred while creating the File
                                        Log.e("MOBILOG", "Unable to create Image File", ex);
                                    }

                                    // Continue only if the File was successfully created
                                    if (photoFile != null) {
                                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                                        takePictureIntent.putExtra(
                                                MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                    } else {
                                        takePictureIntent = null;
                                    }
                                }

                                Intent contentSelectionIntent =
                                        new Intent(Intent.ACTION_GET_CONTENT);
                                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                                contentSelectionIntent.setType("image/*");

                                Intent[] intentArray;
                                if (takePictureIntent != null) {
                                    intentArray = new Intent[] {takePictureIntent};
                                } else {
                                    intentArray = new Intent[0];
                                }

                                Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
                                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

                                startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

                                return true;
                            }
                        });

                webView.loadUrl("https://" + domain_id + ".mobikob.com/task_management/android");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        addVirtualKeyboardVisiblityListener();
        // Force hide keyboard
        getActivity()
                .getWindow()
                .setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
                                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        InputMethodManager inputMethodManager =
                (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
        if (getActivity().getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(
                    getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        removeVirtualKeyboardVisiblityListener();
    }

    private void addVirtualKeyboardVisiblityListener() {
        mKeyboardListener =
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        Rect visibleArea = new Rect();
                        getActivity()
                                .getWindow()
                                .getDecorView()
                                .getWindowVisibleDisplayFrame(visibleArea);

                        int screenHeight =
                                getActivity().getWindow().getDecorView().getRootView().getHeight();
                        int heightDiff = screenHeight - (visibleArea.bottom - visibleArea.top);
                        if (heightDiff > screenHeight * 0.15) {
                            showKeyboardVisibleMode();
                        } else {
                            hideKeyboardVisibleMode();
                        }
                    }
                };
        getActivity()
                .getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .addOnGlobalLayoutListener(mKeyboardListener);
    }

    private void removeVirtualKeyboardVisiblityListener() {
        getActivity()
                .getWindow()
                .getDecorView()
                .getViewTreeObserver()
                .removeOnGlobalLayoutListener(mKeyboardListener);
        hideKeyboardVisibleMode();
    }

    private void showKeyboardVisibleMode() {
        ((TaskActivityView) getActivity()).hideTabBar();
    }

    private void hideKeyboardVisibleMode() {
        ((TaskActivityView) getActivity()).showTabBar();
    }

    private class MyWebViewClient extends WebViewClient {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public boolean shouldOverrideUrlLoading(WebView webView, String url) {
            if (url.contains("messageslist") || url.contains("login_mobile_tenant")) return false;
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            mWaitLayout.setVisibility(View.GONE);
            Log.d("MOBILOG", "PAGE LOADED");
            if (url.contains("login_mobile_tenant")) { // deprecated login handled on first activity
                Log.d("MOBILOG", "login mobile url " + url + " tenant is " + domain_id);
                webView.loadUrl("https://" + domain_id + ".mobikob.com/solowebapp/messageslist");
            }
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("MOBILOG", "PAGE STARTED");
            mWaitLayout.setVisibility(View.VISIBLE);

            super.onPageStarted(view, url, favicon);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }

        Uri[] results = null;

        // Check that the response is a good one
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                // If there is not data, then we may have taken a photo
                if (mCameraPhotoPath != null) {
                    results = new Uri[] {Uri.parse(mCameraPhotoPath)};
                }
            } else {
                String dataString = data.getDataString();
                if (dataString != null) {
                    results = new Uri[] {Uri.parse(dataString)};
                }
            }
        }

        mFilePathCallback.onReceiveValue(results);
        mFilePathCallback = null;
        return;
    }

    /**
     * More info this method can be found at
     * http://developer.android.com/training/camera/photobasics.html
     *
     * @return
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File imageFile =
                File.createTempFile(
                        imageFileName, /* prefix */
                        ".jpg", /* suffix */
                        storageDir /* directory */);
        return imageFile;
    }
}
