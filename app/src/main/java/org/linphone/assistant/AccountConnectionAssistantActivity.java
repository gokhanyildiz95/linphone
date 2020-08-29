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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;
import org.linphone.DatabaseHelper;
import org.linphone.LinphoneManager;
import org.linphone.LoginInterface;
import org.linphone.R;
import org.linphone.TokenHelper;
import org.linphone.UnsafeOkHttpClient;
import org.linphone.core.AccountCreator;
import org.linphone.core.AccountCreatorListenerStub;
import org.linphone.core.Core;
import org.linphone.core.DialPlan;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;
import org.linphone.settings.LinphonePreferences;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AccountConnectionAssistantActivity extends AssistantActivity {
    private RelativeLayout mPhoneNumberConnection, mUsernameConnection;
    private Switch mUsernameConnectionSwitch;
    private EditText mPrefix, mPhoneNumber, mUsername, mPassword, mTenant;
    private TextView mCountryPicker, mError, mConnect, mQrConnect, mDemoLink;
    public android.util.Log sysLog;
    private AccountCreatorListenerStub mListener;
    private DatabaseHelper databaseHelper;
    private ProxyConfig mProxyConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // preferenceHelper = new PreferenceHelper(this);
        databaseHelper = new DatabaseHelper(this);
        setContentView(R.layout.assistant_account_connection);

        mPhoneNumberConnection = findViewById(R.id.phone_number_form);

        mUsernameConnection = findViewById(R.id.username_form);

        mUsernameConnectionSwitch = findViewById(R.id.username_login);
        mUsernameConnectionSwitch.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        mPhoneNumberConnection.setVisibility(isChecked ? View.GONE : View.VISIBLE);
                        mUsernameConnection.setVisibility(isChecked ? View.VISIBLE : View.GONE);
                    }
                });

        mConnect = findViewById(R.id.assistant_login);
        mQrConnect = findViewById(R.id.assistant_qrlogin);
        mDemoLink = findViewById(R.id.create_demo_link);

        mDemoLink.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sysLog.d("MOBILOG", "create demo link clicked");
                        String demoUrl = "https://mobikob.com/sth/create_tenant/";
                        Uri uri = Uri.parse(demoUrl);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        startActivity(intent);
                    }
                });

        /*
        mConnect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AccountCreator accountCreator = getAccountCreator();
                        accountCreator.reset();
                        mConnect.setEnabled(false);

                        if (mUsernameConnectionSwitch.isChecked()) {
                            accountCreator.setUsername(mUsername.getText().toString());
                            accountCreator.setPassword(mPassword.getText().toString());

                            createProxyConfigAndLeaveAssistant();
                        } else {
                            accountCreator.setPhoneNumber(
                                    mPhoneNumber.getText().toString(),
                                    mPrefix.getText().toString());
                            accountCreator.setUsername(accountCreator.getPhoneNumber());

                            AccountCreator.Status status = accountCreator.recoverAccount();
                            if (status != AccountCreator.Status.RequestOk) {
                                Log.e(
                                        "[Account Connection Assistant] recoverAccount returned "
                                                + status);
                                mConnect.setEnabled(true);
                                showGenericErrorDialog(status);
                            }
                        }
                    }
                });

         */
        mQrConnect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sysLog.d("MOBILOG", "qrcode login button clicked");
                        Intent intent =
                                new Intent(
                                        AccountConnectionAssistantActivity.this,
                                        QrCodeConfigurationAssistantActivity.class);
                        startActivity(intent);
                    }
                });

        mConnect.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sysLog.d("MOBILOG", "login button clicked");
                        mConnect.setEnabled(false);
                        loginUser();
                    }
                });
        mConnect.setEnabled(false);

        if (getResources().getBoolean(R.bool.use_phone_number_validation)) {
            if (getResources().getBoolean(R.bool.isTablet)) {
                mUsernameConnectionSwitch.setChecked(true);
            } else {
                mUsernameConnection.setVisibility(View.GONE);
            }
        } else {
            mPhoneNumberConnection.setVisibility(View.GONE);
            findViewById(R.id.username_switch_layout).setVisibility(View.GONE);
        }

        mCountryPicker = findViewById(R.id.select_country);
        mCountryPicker.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showCountryPickerDialog();
                    }
                });

        mError = findViewById(R.id.phone_number_error);

        mPrefix = findViewById(R.id.dial_code);
        mPrefix.setText("+");
        mPrefix.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String prefix = s.toString();
                        if (prefix.startsWith("+")) {
                            prefix = prefix.substring(1);
                        }
                        DialPlan dp = getDialPlanFromPrefix(prefix);
                        if (dp != null) {
                            mCountryPicker.setText(dp.getCountry());
                        }

                        updateConnectButtonAndDisplayError();
                    }
                });

        mPhoneNumber = findViewById(R.id.phone_number);
        mPhoneNumber.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        updateConnectButtonAndDisplayError();
                    }
                });

        ImageView phoneNumberInfos = findViewById(R.id.info_phone_number);
        phoneNumberInfos.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPhoneNumberDialog();
                    }
                });

        mUsername = findViewById(R.id.assistant_username);
        mUsername.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        mConnect.setEnabled(
                                (s.length() > 0)
                                        && (mPassword.getText().length() > 0)
                                        && (mTenant.getText().length() > 0));
                    }
                });

        mPassword = findViewById(R.id.assistant_password);
        mPassword.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        mConnect.setEnabled(
                                (s.length() > 0)
                                        && (mUsername.getText().length() > 0)
                                        && (mTenant.getText().length() > 0));
                    }
                });

        mTenant = findViewById(R.id.assistant_tenant);
        mTenant.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        mConnect.setEnabled(
                                (s.length() > 0)
                                        && (mUsername.getText().length() > 0)
                                        && (mPassword.getText().length() > 0));
                    }
                });

        mListener =
                new AccountCreatorListenerStub() {
                    @Override
                    public void onRecoverAccount(
                            AccountCreator creator, AccountCreator.Status status, String resp) {
                        Log.i(
                                "[Account Connection Assistant] onRecoverAccount status is "
                                        + status);
                        if (status.equals(AccountCreator.Status.RequestOk)) {
                            Intent intent =
                                    new Intent(
                                            AccountConnectionAssistantActivity.this,
                                            PhoneAccountValidationAssistantActivity.class);
                            intent.putExtra("isLoginVerification", true);
                            startActivity(intent);
                        } else {
                            mConnect.setEnabled(true);
                            showGenericErrorDialog(status);
                        }
                    }
                };
    }

    @Override
    protected void onResume() {
        super.onResume();

        Core core = LinphoneManager.getCore();
        if (core != null) {
            reloadLinphoneAccountCreatorConfig();
        }

        getAccountCreator().addListener(mListener);

        DialPlan dp = getDialPlanForCurrentCountry();
        displayDialPlan(dp);

        String phoneNumber = getDevicePhoneNumber();
        if (phoneNumber != null) {
            mPhoneNumber.setText(phoneNumber);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getAccountCreator().removeListener(mListener);
    }

    @Override
    public void onCountryClicked(DialPlan dialPlan) {
        super.onCountryClicked(dialPlan);
        displayDialPlan(dialPlan);
    }

    private void updateConnectButtonAndDisplayError() {
        if (mPrefix.getText().toString().isEmpty() || mPhoneNumber.getText().toString().isEmpty())
            return;

        int status = arePhoneNumberAndPrefixOk(mPrefix, mPhoneNumber);
        if (status == AccountCreator.PhoneNumberStatus.Ok.toInt()) {
            mConnect.setEnabled(true);
            mError.setText("");
            mError.setVisibility(View.INVISIBLE);
        } else {
            mConnect.setEnabled(false);
            mError.setText(getErrorFromPhoneNumberStatus(status));
            mError.setVisibility(View.VISIBLE);
        }
    }

    private void displayDialPlan(DialPlan dp) {
        if (dp != null) {
            mPrefix.setText("+" + dp.getCountryCallingCode());
            mCountryPicker.setText(dp.getCountry());
        }
    }

    private void loginUser() {
        final String username = mUsername.getText().toString();
        final String password = mPassword.getText().toString();
        final String tenant_id = mTenant.getText().toString();
        final String LOGINURL =
                "https://" + tenant_id + "." + LoginInterface.BASEURL + "/login_mobile_tenant/";

        // mConnect.setEnabled(false);
        sysLog.d("MOBILOG", "ACAA loginUser() : login url " + LOGINURL);

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(LOGINURL)
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                        .build();

        LoginInterface api = retrofit.create(LoginInterface.class);

        Call<String> call = api.getUserLogin(username, password, null);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        sysLog.d("MOBILOG", "ACAA onResponse() : " + response.body().toString());
                        mConnect.setEnabled(true);
                        // Toast.makeText()
                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                sysLog.d(
                                        "MOBILOG",
                                        "ACAA onResponse() : " + response.body().toString());

                                String jsonresponse = response.body().toString();
                                parseLoginData(jsonresponse);

                            } else {
                                sysLog.d("MOBILOG", "ACAA onResponse() : Returned empty response");

                                Toast.makeText(getContext(), "Nothing returned", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        sysLog.d("MOBILOG", "ACAA onFailure() : " + t.getCause());
                        mConnect.setEnabled(true);
                        Toast.makeText(
                                        getContext(),
                                        "Fail: " + t.getCause().toString(),
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
                String token = LinphonePreferences.instance().getPushNotificationRegistrationID();
                if (token != null && !token.isEmpty()) {
                    TokenHelper tkHelper = new TokenHelper();
                    tkHelper.storeToken(token);
                }

            } else {
                mConnect.setEnabled(true);
                Toast.makeText(
                                getContext(),
                                "Kullanıcı bilgileri doğrulanamadı!",
                                Toast.LENGTH_LONG)
                        .show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
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
                sysLog.d("MOBILOG", "ACAA saveInfo() : Kullanıcı bilgileri silindi!");

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

                sysLog.d("MOBILOG", " ACAA saveInfo() : Yeni kullanıcı bilgileri eklendi!");

                reloadDefaultAccountCreatorConfig();
                AccountCreator accountCreator = getAccountCreator();

                mConnect.setEnabled(false);

                accountCreator.setUsername(extension);
                accountCreator.setPassword(extension_pass);
                accountCreator.setDomain(tenant_id + "." + fs_server_domain);
                accountCreator.setDisplayName(fullname);
                accountCreator.setTransport(TransportType.Tcp);

                createProxyConfigAndLeaveAssistant(true);

                sysLog.d("MOBILOG", "ACAA saveInfo() : authenticated");

            } else {
                Toast.makeText(
                                getContext(),
                                getResources().getString(R.string.login_extension_error),
                                Toast.LENGTH_LONG)
                        .show();
                finish();
                sysLog.d("MOBILOG", "ACAA saveInfo() : Extension yok!");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        /*preferenceHelper.putIsLogin(true);
        try {
            JSONObject jsonObject = new JSONObject(response);
            final String username = jsonObject.getString("username");
            final String fullname = jsonObject.getString("fullname");
            final String extension = jsonObject.getString("extension");
            final String extension_pass = jsonObject.getString("extension_pass");
            final String fs_server_domain = jsonObject.getString("fs_server_domain");
            final String avatar = jsonObject.getString("avatar");
            preferenceHelper.putPassword(password);
            preferenceHelper.putTenant(tenant_id);
            preferenceHelper.putFullname(fullname);
            preferenceHelper.putUsername(username);
            preferenceHelper.putExtension(extension);
            preferenceHelper.putExtensionPass(extension_pass);
            preferenceHelper.putFSServerDomain(fs_server_domain);
            preferenceHelper.putProfilePic(avatar);
            reloadDefaultAccountCreatorConfig();
            AccountCreator accountCreator = getAccountCreator();

            mConnect.setEnabled(false);

            accountCreator.setUsername(extension);
            accountCreator.setPassword(extension_pass);
            accountCreator.setDomain(tenant_id + "." + fs_server_domain);
            accountCreator.setDisplayName(fullname);
            accountCreator.setTransport(TransportType.Tcp);
            createProxyConfigAndLeaveAssistant(true);

        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }
}
