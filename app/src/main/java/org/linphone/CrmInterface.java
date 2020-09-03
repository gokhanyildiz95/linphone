package org.linphone;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface CrmInterface {
    String BASEURL = "mobikob.com";

    @FormUrlEncoded
    @POST("/crm/customer_list")
    Call<String> getCustomerList(
            @Field("current") String current,
            @Field("pageSize") String pageSize,
            @Field("start") String start,
            @Field("search") String search,
            @Header("Authorization") String headertoken);
}
