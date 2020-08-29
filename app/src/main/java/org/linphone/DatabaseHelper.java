package org.linphone;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "mobikobDB";
    private static final String TABLE_NAME = "userInfo";
    private static final int DB_VERSION = 1;
    private static String I_USER = "i_user";
    private static String TENANT_ID = "tenant_id";
    private static String DOMAIN_ID = "domain_id";
    private static String EMAIL = "email";
    private static String FULLNAME = "fullname";
    private static String USERNAME = "username";
    private static String FSSERVERDOMAIN = "fs_server_domain";
    private static String AVATAR = "avatar";
    private static String EXTENSION = "extension";
    private static String EXTENSION_PASS = "extension_pass";
    private static String JWT = "jwt";
    private static String LOGIN_STATUS = "login_status";

    private Context mContext;

    public android.util.Log sysLog;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        try {

            String CREATE_TABLE =
                    "CREATE TABLE "
                            + TABLE_NAME
                            + "("
                            + I_USER
                            + " INTEGER NOT NULL, "
                            + TENANT_ID
                            + " TEXT NOT NULL,"
                            + DOMAIN_ID
                            + " TEXT NOT NULL,"
                            + EMAIL
                            + " TEXT NOT NULL,"
                            + FULLNAME
                            + " TEXT NOT NULL,"
                            + USERNAME
                            + " TEXT NOT NULL,"
                            + FSSERVERDOMAIN
                            + " TEXT NOT NULL,"
                            + AVATAR
                            + " TEXT,"
                            + EXTENSION
                            + " TEXT NOT NULL,"
                            + EXTENSION_PASS
                            + " TEXT NOT NULL,"
                            + JWT
                            + " TEXT,"
                            + LOGIN_STATUS
                            + " INTEGER DEFAULT 0"
                            + ")";
            db.execSQL(CREATE_TABLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_NAME);
        onCreate(db);
    }

    public void deleteTable() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, null, null);
        db.close();
        sysLog.d("MOBILOG", "DBHELPER deleteTable() : " + TABLE_NAME);
    }

    public void deleteUser(int i_user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, I_USER + " = ?", new String[] {String.valueOf(i_user)});
        db.close();
        sysLog.d("MOBILOG", "DBHELPER deleteUser() : " + i_user);
    }

    public void deleteAllUser() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "DELETE FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.close();
        db.close();
        sysLog.d("MOBILOG", "DBHELPER deleteAllUser()");
    }

    public int getUserCount() {
        String countQuery = "SELECT  * FROM " + TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        sysLog.d("MOBILOG", "DBHELPER getUserCount() : " + count);
        return count;
    }

    public void setUser(
            int i_user,
            String tenant_id,
            String domain_id,
            String email,
            String fullname,
            String username,
            String fs_server_domain,
            String avatar,
            String extension,
            String extension_pass,
            String jwt,
            int login_status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(I_USER, i_user);
        values.put(TENANT_ID, tenant_id);
        values.put(DOMAIN_ID, domain_id);
        values.put(EMAIL, email);
        values.put(FULLNAME, fullname);
        values.put(USERNAME, username);
        values.put(FSSERVERDOMAIN, fs_server_domain);
        values.put(AVATAR, avatar);
        values.put(EXTENSION, extension);
        values.put(EXTENSION_PASS, extension_pass);
        values.put(JWT, jwt);
        values.put(LOGIN_STATUS, login_status);

        db.insert(TABLE_NAME, null, values);
        db.close();
        sysLog.d("MOBILOG", "DBHELPER setUser() : " + i_user);
    }

    public void updateUser(
            int i_user,
            String tenant_id,
            String domain_id,
            String email,
            String fullname,
            String username,
            String fs_server_domain,
            String avatar,
            String extension,
            String extension_pass,
            String jwt,
            int login_status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(I_USER, i_user);
        values.put(TENANT_ID, tenant_id);
        values.put(DOMAIN_ID, domain_id);
        values.put(EMAIL, email);
        values.put(FULLNAME, fullname);
        values.put(USERNAME, username);
        values.put(FSSERVERDOMAIN, fs_server_domain);
        values.put(AVATAR, avatar);
        values.put(EXTENSION, extension);
        values.put(EXTENSION_PASS, extension_pass);
        values.put(JWT, jwt);
        values.put(LOGIN_STATUS, login_status);

        // updating row
        db.update(TABLE_NAME, values, I_USER + " = ?", new String[] {String.valueOf(i_user)});
        db.close();
        sysLog.d("MOBILOG", "DBHELPER updateUser() : " + i_user);
    }

    public void updateUserLoginStatus(int i_user, int login_status) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LOGIN_STATUS, login_status);

        // updating row
        db.update(TABLE_NAME, values, I_USER + " = ?", new String[] {String.valueOf(i_user)});
        db.close();
        sysLog.d("MOBILOG", "DBHELPER updateUserLoginStatus() : " + i_user);
    }

    /*
     * HashMap<String, String> hashMap = databaseHelper.getUser();
     * String userName = hashMap.get("username");
     * ...
     */
    public HashMap<String, String> getUser() {

        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {

            user.put(I_USER, cursor.getString(cursor.getColumnIndex(I_USER)));
            user.put(TENANT_ID, cursor.getString(cursor.getColumnIndex(TENANT_ID)));
            user.put(DOMAIN_ID, cursor.getString(cursor.getColumnIndex(DOMAIN_ID)));
            user.put(EMAIL, cursor.getString(cursor.getColumnIndex(EMAIL)));
            user.put(FULLNAME, cursor.getString(cursor.getColumnIndex(FULLNAME)));
            user.put(USERNAME, cursor.getString(cursor.getColumnIndex(USERNAME)));
            user.put(FSSERVERDOMAIN, cursor.getString(cursor.getColumnIndex(FSSERVERDOMAIN)));
            user.put(AVATAR, cursor.getString(cursor.getColumnIndex(AVATAR)));
            user.put(EXTENSION, cursor.getString(cursor.getColumnIndex(EXTENSION)));
            user.put(EXTENSION_PASS, cursor.getString(cursor.getColumnIndex(EXTENSION_PASS)));
            user.put(JWT, cursor.getString(cursor.getColumnIndex(JWT)));
            user.put(LOGIN_STATUS, cursor.getString(cursor.getColumnIndex(LOGIN_STATUS)));
        }

        cursor.close();
        db.close();
        return user;
    }

    /*
     * int i_user = databaseHelper.getUserId();
     */
    public int getUserId() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        int i_user = 0;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            i_user = cursor.getInt(cursor.getColumnIndex(I_USER));
        }

        cursor.close();
        db.close();
        return i_user;
    }

    /*
     * String email = databaseHelper.getUserEmail();
     */
    public String getUserEmail() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String email = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            email = cursor.getString(cursor.getColumnIndex(EMAIL));
        }

        cursor.close();
        db.close();
        return email;
    }

    /*
     * String avatar = databaseHelper.getUserAvatar();
     */
    public String getUserAvatar() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String avatar = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            avatar = cursor.getString(cursor.getColumnIndex(AVATAR));
        }

        cursor.close();
        db.close();
        return avatar;
    }

    /*
     * String jwt = databaseHelper.getUserJWT();
     */
    public String getUserJWT() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String jwt = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            jwt = cursor.getString(cursor.getColumnIndex(JWT));
        }

        cursor.close();
        db.close();
        return jwt;
    }

    /*
     * String extension = databaseHelper.getExtension();
     */
    public String getExtension() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String extension = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            extension = cursor.getString(cursor.getColumnIndex(EXTENSION));
        }

        cursor.close();
        db.close();
        return extension;
    }

    /*
     * String fsServerDomain = databaseHelper.getFSSERVERDOMAIN();
     */
    public String getFSSERVERDOMAIN() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String domain = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            domain = cursor.getString(cursor.getColumnIndex(FSSERVERDOMAIN));
        }

        cursor.close();
        db.close();
        return domain;
    }

    /*
     * String fullName = databaseHelper.getFullname();
     */
    public String getFullname() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String fullname = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            fullname = cursor.getString(cursor.getColumnIndex(FULLNAME));
        }

        cursor.close();
        db.close();
        return fullname;
    }

    /*
     * String tenantId = databaseHelper.getTenantId();
     */
    public String getTenantId() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String tenantID = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            tenantID = cursor.getString(cursor.getColumnIndex(TENANT_ID));
        }

        cursor.close();
        db.close();
        return tenantID;
    }

    /*
     * String domainId = databaseHelper.getDomainId();
     */
    public String getDomainId() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String domainId = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            domainId = cursor.getString(cursor.getColumnIndex(DOMAIN_ID));
        }

        cursor.close();
        db.close();
        return domainId;
    }

    /*
     * String userName = databaseHelper.getUsername();
     */
    public String getUsername() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";
        String username = new String();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            username = cursor.getString(cursor.getColumnIndex(USERNAME));
        }

        cursor.close();
        db.close();
        return username;
    }

    /*
     * boolean isLoggedIn = databaseHelper.getIsLogin();
     */
    public boolean getIsLogin() {

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " LIMIT 1";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.getCount() > 0) {
            cursor.moveToFirst();
            int login_status = cursor.getInt(cursor.getColumnIndex(LOGIN_STATUS));

            if (login_status == 1) {
                cursor.close();
                db.close();
                return true;
            } else {
                cursor.close();
                db.close();
                return false;
            }
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }
}
