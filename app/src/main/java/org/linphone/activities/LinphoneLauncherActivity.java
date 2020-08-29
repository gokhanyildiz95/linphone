/*
 * Copyright (c) 2010-2019 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.linphone.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.DatabaseHelper;
import org.linphone.LinphoneManager;
import org.linphone.LoginInterface;
import org.linphone.R;
import org.linphone.UnsafeOkHttpClient;
import org.linphone.assistant.MenuAssistantActivity;
import org.linphone.chat.ChatActivity;
import org.linphone.chat.ChatActivityView;
import org.linphone.contacts.ContactsActivity;
import org.linphone.dialer.DialerActivity;
import org.linphone.history.HistoryActivity;
import org.linphone.service.LinphoneService;
import org.linphone.service.ServiceWaitThread;
import org.linphone.service.ServiceWaitThreadListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/** Creates LinphoneService and wait until Core is ready to start main Activity */
public class LinphoneLauncherActivity extends Activity implements ServiceWaitThreadListener {

    // private PreferenceHelper preferenceHelper;
    private DatabaseHelper databaseHelper;
    public android.util.Log sysLog;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseHelper = new DatabaseHelper(this);
        sysLog.d("MOBILOG", "LAUNCH oncreate()");
        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (!getResources().getBoolean(R.bool.use_full_screen_image_splashscreen)) {
            setContentView(R.layout.launch_screen);
        } // Otherwise use drawable/launch_screen layer list up until first activity starts
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (LinphoneService.isReady()) {
            onServiceReady();
            sysLog.d("MOBILOG", "LAUNCH onStart() : device ready");
        } else {
            startService(
                    new Intent().setClass(LinphoneLauncherActivity.this, LinphoneService.class));
            new ServiceWaitThread(this).start();
        }
    }

    @Override
    protected void onDestroy() {
        databaseHelper.close();
        super.onDestroy();
    }

    @Override
    public void onServiceReady() {
        final Class<? extends Activity> classToStart;

        boolean useFirstLoginActivity =
                getResources().getBoolean(R.bool.display_account_assistant_at_first_start);
        // show login activity every time
        // && LinphonePreferences.instance().isFirstLaunch()
        boolean isLoggedIn = databaseHelper.getIsLogin();

        if (useFirstLoginActivity && !isLoggedIn) {
            classToStart = MenuAssistantActivity.class;
        } else {

            // Toast.makeText(this, "AUTO LOGIN", Toast.LENGTH_LONG).show();
            int i_user = databaseHelper.getUserId();
            if (i_user != 0) {
                databaseHelper.updateUserLoginStatus(i_user, 1);
            }

            if (getIntent().getExtras() != null) {
                String activity = getIntent().getExtras().getString("Activity", null);
                if (ChatActivity.NAME.equals(activity)) {
                    classToStart = ChatActivityView.class;
                } else if (HistoryActivity.NAME.equals(activity)) {
                    classToStart = HistoryActivity.class;
                } else if (ContactsActivity.NAME.equals(activity)) {
                    classToStart = ContactsActivity.class;
                } else {
                    classToStart = DialerActivity.class;
                }
            } else {
                Log.d("MOBILOG", "start  activity dialer");
                classToStart = DialerActivity.class;
            }
        }

        if (getResources().getBoolean(R.bool.check_for_update_when_app_starts)) {
            LinphoneManager.getInstance().checkForUpdate();
        }

        Intent intent = new Intent();
        intent.setClass(LinphoneLauncherActivity.this, classToStart);
        if (getIntent() != null && getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        intent.setAction(getIntent().getAction());
        intent.setType(getIntent().getType());
        intent.setData(getIntent().getData());
        startActivity(intent);
        resetUserLoginData();

        LinphoneManager.getInstance().changeStatusToOnline();

        // nitializeSessionWithWView();
    }

    public void resetUserLoginData() {

        String jwt = databaseHelper.getUserJWT();
        final String LOGINURL = "https://" + LoginInterface.BASEURL + "/login_mobile/";

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(LOGINURL)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                        .build();

        LoginInterface api = retrofit.create(LoginInterface.class);

        Call<String> call = api.getUserLoginOther("", "", jwt);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                String jsonresponse = response.body().toString();

                                sysLog.d(
                                        "MOBILOG",
                                        "LAUNCH onResponse() : succ data" + jsonresponse);
                                parseLoginData(jsonresponse);
                            } else {
                                sysLog.d("MOBILOG", "LAUNCH onResponse() : Error");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        sysLog.d("MOBILOG", "LAUNCH onFailure() : Error");
                    }
                });
    }

    private void parseLoginData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("result").equals("1")) {
                saveInfo(response);
            } else {
                sysLog.d("MOBILOG", "LAUNCH parseLoginData() : Error");
            }
        } catch (JSONException e) {
            sysLog.d("MOBILOG", "LAUNCH parseLoginData() : Catch");
        }
    }

    private void saveInfo(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            final int i_user = Integer.parseInt(jsonObject.getString("i_user"));
            final String tenant_id = jsonObject.getString("tenant_id");
            final String domain_id = jsonObject.getString("domain_id");
            final String email = jsonObject.getString("email");
            final String fullname = jsonObject.getString("fullname");
            final String username = jsonObject.getString("username");
            final String fs_server_domain = jsonObject.getString("fs_server_domain");
            final String avatar = jsonObject.getString("avatar");
            final String extension = jsonObject.getString("extension");
            final String extension_pass = jsonObject.getString("extension_pass");
            final String jwt = jsonObject.getString("jwt");

            databaseHelper.deleteUser(i_user);
            // databaseHelper.getUserCount();
            databaseHelper.setUser(
                    i_user,
                    tenant_id,
                    domain_id,
                    email,
                    fullname,
                    username,
                    fs_server_domain,
                    avatar,
                    extension,
                    extension_pass,
                    jwt,
                    1);

            sysLog.d("MOBILOG", "LAUNCH saveInfo() : Success");

        } catch (JSONException e) {
            e.printStackTrace();
            sysLog.d("MOBILOG", "LAUNCH saveInfo() : Error");
        }
    }
}
