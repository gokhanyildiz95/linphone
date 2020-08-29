package org.linphone.chat;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import org.linphone.R;

public class ChatMessagesFragmentWView extends Fragment {
    private Context mContext;
    private LayoutInflater mInflater;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain the fragment across configuration changes
        setRetainInstance(true);
        String url = "";
        if (getArguments() != null) {
            if (getArguments().getString("TheUrl") != null) {
                url = getArguments().getString("TheUrl");
            }
        }

        mContext = getActivity().getApplicationContext();
        mInflater = inflater;
        View view = inflater.inflate(R.layout.chatwview, container, false);
        WebView webView = (WebView) view.findViewById(R.id.chat_webview);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Force links and redirects to open in the WebView instead of in a browser

        webView.loadUrl(url);

        return view;
    }
}
