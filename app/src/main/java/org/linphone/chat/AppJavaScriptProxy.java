package org.linphone.chat;

import android.app.Activity;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class AppJavaScriptProxy {

    private Activity activity = null;

    public AppJavaScriptProxy(Activity activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void setCount(String message) {
        Log.d("MOBILOG", "set coutn clicked " + message);
        Toast toast =
                Toast.makeText(this.activity.getApplicationContext(), message, Toast.LENGTH_SHORT);

        toast.show();
    }
}
