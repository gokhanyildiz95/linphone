package org.linphone;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {

    private final String LOGIN_STATUS = "login_status";
    private final String TENANT_ID = "tenant_id";
    private final String USERNAME = "username";
    private final String PASSWORD = "password";
    private final String FULLNAME = "fullname";
    private final String FSSERVERDOMAIN = "fs_server_domain";
    private final String EXTENSION = "extension";
    private final String EXTENSION_PASS = "extension_pass";
    private final String PROFILE_PIC = "profile_pic";
    private SharedPreferences app_prefs;
    private Context context;

    public PreferenceHelper(Context context) {
        app_prefs = context.getSharedPreferences("shared", Context.MODE_PRIVATE);
        this.context = context;
    }

    public String getFullname() {
        return app_prefs.getString(FULLNAME, "");
    }

    public void putFullname(String fullname) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(FULLNAME, fullname);
        edit.commit();
    }

    public String getProfilePic() {
        return app_prefs.getString(PROFILE_PIC, "");
    }

    public void putProfilePic(String pp) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(PROFILE_PIC, pp);
        edit.commit();
    }

    public String getFSServerDomain() {
        return app_prefs.getString(FSSERVERDOMAIN, "");
    }

    public void putFSServerDomain(String fsserver_domain) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(FSSERVERDOMAIN, fsserver_domain);
        edit.commit();
    }

    public String getExtension() {
        return app_prefs.getString(EXTENSION, "");
    }

    public void putExtension(String extension) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(EXTENSION, extension);
        edit.commit();
    }

    public String getExtensionPass() {
        return app_prefs.getString(EXTENSION_PASS, "");
    }

    public void putExtensionPass(String extension) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(EXTENSION_PASS, extension);
        edit.commit();
    }

    public void putIsLogin(boolean loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putBoolean(LOGIN_STATUS, loginorout);
        edit.commit();
    }

    public boolean getIsLogin() {
        return app_prefs.getBoolean(LOGIN_STATUS, false);
    }

    public void putTenant(String tenant_id) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(TENANT_ID, tenant_id);
        edit.commit();
    }

    public String getTenantId() {
        return app_prefs.getString(TENANT_ID, "");
    }

    public void putUsername(String name) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(USERNAME, name);
        edit.commit();
    }

    public String getUsername() {
        return app_prefs.getString(USERNAME, "");
    }

    public void putPassword(String loginorout) {
        SharedPreferences.Editor edit = app_prefs.edit();
        edit.putString(PASSWORD, loginorout);
        edit.commit();
    }

    public String getPassword() {
        return app_prefs.getString(PASSWORD, "");
    }
}
