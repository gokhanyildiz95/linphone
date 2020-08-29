package org.linphone.chat;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebStorage;
import java.util.HashMap;
import org.linphone.DatabaseHelper;
import org.linphone.R;
import org.linphone.activities.MainActivity;

public class ChatActivityView extends MainActivity {
    public static final String NAME = "Chat";
    private DatabaseHelper databaseHelper;
    private ChatRoomsFragmentWView chatRoomsFragmentWView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        databaseHelper = new DatabaseHelper(this);
        HashMap<String, String> userData = databaseHelper.getUser();
        String jwt = userData.get("jwt");
        String domain_id = userData.get("domain_id");
        Log.d("MOBILOG", "wtf domain ? " + domain_id);
        getIntent().putExtra("Activity", NAME);
        getIntent().putExtra("jwt", jwt);
        getIntent().putExtra("domain_id", domain_id);

        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragmentContainer);
        if (currentFragment == null) {
            showChatRooms();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mChatSelected.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getIntent().setAction("");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WebStorage.getInstance().deleteAllData();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK
                && chatRoomsFragmentWView != null
                && chatRoomsFragmentWView.canGoBack())) {
            chatRoomsFragmentWView.goBack();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showChatRooms() {
        Bundle extras = new Bundle();
        extras.putString("jwt", getIntent().getStringExtra("jwt"));
        extras.putString("domain_id", getIntent().getStringExtra("domain_id"));
        chatRoomsFragmentWView = new ChatRoomsFragmentWView();
        chatRoomsFragmentWView.setArguments(extras);
        changeFragment(chatRoomsFragmentWView, "Chat rooms", false);
    }
}
