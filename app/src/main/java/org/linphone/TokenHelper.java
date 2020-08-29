package org.linphone;

import static org.linphone.mediastream.MediastreamerAndroidContext.getContext;

import android.widget.Toast;
import java.util.HashMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class TokenHelper {
    public android.util.Log sysLog;
    private DatabaseHelper databaseHelper;

    public void storeLocation(Double latitude, Double longitude) {
        databaseHelper = new DatabaseHelper(getContext());
        HashMap<String, String> userData = databaseHelper.getUser();
        String domainId = userData.get("domain_id");
        String jwt = userData.get("jwt");
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl("https://" + domainId + ".mobikob.com/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                        .build();

        LoginInterface api = retrofit.create(LoginInterface.class);
        Call<String> call = api.setUserLocation(latitude, longitude, jwt);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                String jsonresponse = response.body().toString();
                                sysLog.d("MOBILOG", "store location json resp " + jsonresponse);
                            } else {
                                sysLog.d(
                                        "MOBILOG",
                                        "store location onResponse() : Returned empty response");
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        sysLog.d("MOBILOG", "store location on fail ");
                    }
                });
    }

    public void storeToken(final String token) {

        databaseHelper = new DatabaseHelper(getContext());
        HashMap<String, String> userData = databaseHelper.getUser();

        if (!(userData.containsKey("tenant_id") || userData.containsKey("i_user"))) {
            return;
        }
        String tenant = userData.get("tenant_id");
        String i_user = userData.get("i_user");
        sysLog.d("MOBILOG", "storing token " + token + " tenant " + tenant + " i_user " + i_user);
        String headertoken =
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhbmRyb2lkX3VzZXIiOnRydWUsImlhdCI6MTU3NTc1MDM4Mn0.Qg516tjWZvNpxgAOKlWD_Z0Ta5U9NteJF2WmnBU0IIQ";
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl("https://" + tenant + ".mobikob.com/")
                        .addConverterFactory(ScalarsConverterFactory.create())
                        .client(UnsafeOkHttpClient.getUnsafeOkHttpClient())
                        .build();

        LoginInterface api = retrofit.create(LoginInterface.class);

        Call<String> call = api.setUserToken(tenant, i_user, token, "Bearer " + headertoken);
        call.enqueue(
                new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        if (response.isSuccessful()) {
                            if (response.body() != null) {
                                String jsonresponse = response.body().toString();
                                sysLog.d("MOBILOG", "json resp " + jsonresponse);
                            } else {
                                sysLog.d("MOBILOG", "ACAA onResponse() : Returned empty response");

                                Toast.makeText(getContext(), "Nothing returned", Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        sysLog.d("MOBILOG", "on fail " + t.getCause().toString());
                        Toast.makeText(
                                        getContext(),
                                        "Fail: " + t.getCause().toString(),
                                        Toast.LENGTH_LONG)
                                .show();
                    }
                });
    }
}
