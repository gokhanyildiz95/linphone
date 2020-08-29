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
package org.linphone.assistant;

import static org.linphone.mediastream.MediastreamerAndroidContext.getContext;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.DatabaseHelper;
import org.linphone.LinphoneManager;
import org.linphone.LoginInterface;
import org.linphone.R;
import org.linphone.UnsafeOkHttpClient;
import org.linphone.core.AccountCreator;
import org.linphone.core.Core;
import org.linphone.core.CoreListenerStub;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;
import org.linphone.dialer.DialerActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class QrCodeConfigurationAssistantActivity extends AssistantActivity {

    private TextureView mQrcodeView;
    private CoreListenerStub mListener;
    private static final int PERMISSION_REQUEST_CODE = 200;
    public android.util.Log sysLog;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.assistant_qr_code_remote_configuration);
        databaseHelper = new DatabaseHelper(this);

        if (!checkPermission(Manifest.permission.CAMERA)) {
            requestPermission();
        }
        mQrcodeView = findViewById(R.id.qr_code_capture_texture);

        mListener =
                new CoreListenerStub() {
                    @Override
                    public void onQrcodeFound(Core core, String result) {
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("URL", result);
                        setResult(Activity.RESULT_OK, resultIntent);

                        if (result != null) {
                            loginUserQrCode(result);
                            sysLog.d("MOBILOG", "QRCODE onQrcodeFound() URL : " + result);
                        } else {
                            finish();
                            Toast.makeText(
                                            getContext(),
                                            getResources().getString(R.string.qrcode_read_error),
                                            Toast.LENGTH_LONG)
                                    .show();
                        }
                    }
                };

        ImageView changeCamera = findViewById(R.id.qr_code_capture_change_camera);
        changeCamera.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinphoneManager.getCallManager().switchCamera();
                    }
                });
        Core core = LinphoneManager.getCore();
        if (core != null && core.getVideoDevicesList().length > 1) {
            changeCamera.setVisibility(View.VISIBLE);
        }

        setBackCamera();
    }

    // Permission Camera
    public boolean checkPermission(String permission) {
        int granted = getPackageManager().checkPermission(permission, getPackageName());
        Log.i(
                "[Permission] "
                        + permission
                        + " permission is "
                        + (granted == PackageManager.PERMISSION_GRANTED ? "granted" : "denied"));
        return granted == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {

        ActivityCompat.requestPermissions(
                this, new String[] {Manifest.permission.CAMERA}, PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Toast.makeText(getApplicationContext(), "Permission Granted",
                    // Toast.LENGTH_SHORT).show();
                    sysLog.d("MOBILOG", "QRCODE Camera Permission Granted");

                    // main logic
                } else {
                    // Toast.makeText(getApplicationContext(), "Permission Denied",
                    // Toast.LENGTH_SHORT).show();
                    sysLog.d("MOBILOG", "QRCODE Camera Permission Denied");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            showMessageOKCancel(
                                    "You need to allow access permissions",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermission();
                                            }
                                        }
                                    });
                        }
                    }
                }
                break;
        }
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    // Login User with QR Code
    private void loginUserQrCode(String qr_txt) {
        final String qr_code = qr_txt;
        final String LOGINURL = "https://" + LoginInterface.BASEURL + "/login_mobile/";

        // mConnect.setEnabled(false);
        sysLog.d("MOBILOG", "QRCODE loginUserQrCode() : login url " + LOGINURL);

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(LOGINURL)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                        .build();

        LoginInterface api = retrofit.create(LoginInterface.class);

        Call<String> call = api.getUserLoginOther("", "", qr_code);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        sysLog.d("MOBILOG", "QRCODE onResponse() : " + response.body().toString());
                        // Toast.makeText()
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                sysLog.d(
                                        "MOBILOG",
                                        "QRCODE onResponse() : " + response.body().toString());
                                String jsonresponse = response.body().toString();
                                parseLoginData(jsonresponse);

                            } else {
                                sysLog.d(
                                        "MOBILOG", "QRCODE onResponse() : Returned empty response");
                                finish();
                                Toast.makeText(
                                                getContext(),
                                                getResources()
                                                        .getString(R.string.qrcode_verify_error),
                                                Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        sysLog.d("MOBILOG", "QRCODE onFailure() : " + t.getCause());
                        finish();
                        Toast.makeText(
                                        getContext(),
                                        getResources().getString(R.string.qrcode_verify_error),
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }

    private void parseLoginData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            if (jsonObject.getString("result").equals("1")) {
                saveInfo(response);
            } else {
                finish();
                Toast.makeText(
                                getContext(),
                                getResources().getString(R.string.qrcode_verify_error),
                                Toast.LENGTH_LONG)
                        .show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            finish();
            Toast.makeText(
                            getContext(),
                            getResources().getString(R.string.qrcode_verify_error),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void saveInfo(String response) {

        try {
            removeAllAccounts(); // remove any old accounts
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

            if ((extension.length() > 0) && (!extension.isEmpty())) {

                databaseHelper.deleteUser(i_user);
                // databaseHelper.getUserCount();
                sysLog.d("MOBILOG", "QRCODE saveInfo() : Kullanıcı bilgileri silindi!");

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

                sysLog.d("MOBILOG", "QRCODE saveInfo() : Yeni kullanıcı bilgileri eklendi!");

                reloadDefaultAccountCreatorConfig();
                AccountCreator accountCreator = getAccountCreator();

                accountCreator.setUsername(extension);
                accountCreator.setPassword(extension_pass);
                accountCreator.setDomain(tenant_id + "." + fs_server_domain);
                accountCreator.setDisplayName(fullname);
                accountCreator.setTransport(TransportType.Tcp);
                createProxyConfigAndLeaveAssistant(true);

                Intent intent = new Intent();
                intent.setClass(QrCodeConfigurationAssistantActivity.this, DialerActivity.class);
                if (getIntent() != null && getIntent().getExtras() != null) {
                    intent.putExtras(getIntent().getExtras());
                }
                intent.setAction(getIntent().getAction());
                intent.setType(getIntent().getType());
                intent.setData(getIntent().getData());
                startActivity(intent);

                sysLog.d("MOBILOG ", "QRCODE saveInfo() : authenticated");

                Toast.makeText(
                                getContext(),
                                getResources().getString(R.string.qrcode_verify_success),
                                Toast.LENGTH_LONG)
                        .show();

                LinphoneManager.getInstance().changeStatusToOnline();
            } else {
                Toast.makeText(
                                getContext(),
                                getResources().getString(R.string.login_extension_error),
                                Toast.LENGTH_LONG)
                        .show();
                finish();
                sysLog.d("MOBILOG", "QRCODE saveInfo() : Extension yok!");
            }

        } catch (JSONException e) {
            e.printStackTrace();
            finish();
            Toast.makeText(
                            getContext(),
                            getResources().getString(R.string.qrcode_verify_error),
                            Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void removeAllAccounts() {
        Core core = LinphoneManager.getCore();
        // if core null pass
        if (core != null) {
            // get all accounts and remove them
            ProxyConfig[] proxyConfigs = core.getProxyConfigList();
            if (proxyConfigs.length > 0) {
                for (ProxyConfig config : proxyConfigs) {
                    core.removeProxyConfig(config);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        enableQrcodeReader(true);
    }

    @Override
    public void onPause() {
        enableQrcodeReader(false);
        super.onPause();
    }

    private void enableQrcodeReader(boolean enable) {
        Core core = LinphoneManager.getCore();
        if (core == null) return;

        core.setNativePreviewWindowId(enable ? mQrcodeView : null);
        core.enableQrcodeVideoPreview(enable);
        core.enableVideoPreview(enable);

        if (enable) {
            core.addListener(mListener);
        } else {
            core.removeListener(mListener);
        }
    }

    private void setBackCamera() {
        Core core = LinphoneManager.getCore();
        if (core == null) return;

        String firstDevice = null;
        for (String camera : core.getVideoDevicesList()) {
            if (firstDevice == null) {
                firstDevice = camera;
            }

            if (camera.contains("Back")) {
                Log.i("[QR Code] Found back facing camera: " + camera);
                core.setVideoDevice(camera);
                return;
            }
        }

        Log.i("[QR Code] Using first camera available: " + firstDevice);
        core.setVideoDevice(firstDevice);
    }
}
