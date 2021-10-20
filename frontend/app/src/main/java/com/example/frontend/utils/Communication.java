package com.example.frontend.utils;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Field;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Communication {
    private static final String apiUrl = "http://192.144.220.229:80/api/";
    private static String token;
    public JSONObject object;

    public Communication(JSONObject object) {
        this.object = object;
    }

    /**
     * send post to apiUrl + suffix, e.g. "xx.com/api/" + "login"
     * @param suffix: type of request
     */
    public Response sendPost(String suffix, boolean needToken) throws IOException {
//        Log.e("post", apiUrl + suffix + object.toString());
        OkHttpClient client = new OkHttpClient();

        Request.Builder builder = new Request.Builder()
                .url(apiUrl + suffix)
                .post(RequestBody.create(MediaType.parse("application/json"), object.toString()));
        Request request;
        if(needToken) request = builder.addHeader("Authorization", token).build();
        else request = builder.build();
        return client.newCall(request).execute();  // response
    }

    public static void setToken(String token) {
        Communication.token = token;
    }
}
