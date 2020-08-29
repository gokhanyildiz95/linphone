package org.linphone;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface LoginInterface {
    String BASEURL = "mobikob.com";

    @FormUrlEncoded
    @POST("/login_mobile_tenant/")
    Call<String> getUserLogin(
            @Field("email") String email,
            @Field("password") String password,
            @Field("qr_code") String qr_code);

    @FormUrlEncoded
    @POST("/login_mobile/")
    Call<String> getUserLoginOther(
            @Field("email") String email,
            @Field("password") String password,
            @Field("qr_code") String qr_code);

    @FormUrlEncoded
    @POST("/fb/storetoken")
    Call<String> setUserToken(
            @Field("tenant") String tenant,
            @Field("i_user") String i_user,
            @Field("token") String token,
            @Header("Authorization") String headertoken);

    @FormUrlEncoded
    @POST("/store_location/")
    Call<String> setUserLocation(
            @Field("latitude") Double latitude,
            @Field("longitude") Double longitude,
            @Header("JWT") String headertoken);
}
