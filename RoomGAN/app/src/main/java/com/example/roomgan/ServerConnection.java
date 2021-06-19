package com.example.roomgan;

import android.util.Base64;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ServerConnection {

    private final String serverURL = "https://172.26.105.25:35632/";

    public void get(final Listener listener, String endpoint, HashMap<String,String> params, String token){

        OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).readTimeout(180, TimeUnit.SECONDS).connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS)).build();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(serverURL+endpoint).newBuilder();
        if(params != null){
            for(Map.Entry<String, String> entry : params.entrySet()){
                urlBuilder.addQueryParameter(entry.getKey(), entry.getValue());
            }
        }

        Request request;

        if (token != null) {
            request = new Request.Builder().url(urlBuilder.build())
                    .header("Authorization", token).get().build();
        }else{
            request = new Request.Builder().url(urlBuilder.build()).get().build();
        }



        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                e.printStackTrace();
                Log.i("prueba", "Ha fallado la conexión");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    Log.i("prueba", data.toString());
                    listener.receiveMessage(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void post(final Listener listener, final JSONObject data, String endpoint, String token){
        OkHttpClient client = new OkHttpClient.Builder().hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }).readTimeout(180, TimeUnit.SECONDS).connectionSpecs(Collections.singletonList(ConnectionSpec.MODERN_TLS)).build();

        RequestBody body = RequestBody.create(data.toString(),MediaType.parse("application/json"));

        Request request;
        if (token != null) {
            request = new Request.Builder().url(serverURL+endpoint)
                    .header("Authorization", token).post(body).build();
        }else{
            request = new Request.Builder().url(serverURL+endpoint).post(body).build();
        }

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                call.cancel();
                e.printStackTrace();
                Log.i("prueba", "Ha fallado la conexión");
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                try {
                    JSONObject data = new JSONObject(response.body().string());
                    Log.i("prueba", data.toString());
                    listener.receiveMessage(data);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
