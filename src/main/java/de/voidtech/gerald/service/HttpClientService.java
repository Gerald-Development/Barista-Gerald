package main.java.de.voidtech.gerald.service;

import main.java.de.voidtech.gerald.exception.UnhandledGeraldException;
import main.java.de.voidtech.gerald.util.RequestInterceptor;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class HttpClientService {

    private OkHttpClient newClient() {
        return new OkHttpClient.Builder()
                .addInterceptor(new RequestInterceptor())
                .build();
    }

    public Response post(String url, String json) {
        try {
            RequestBody body = RequestBody.create(json.getBytes());
            OkHttpClient client = newClient();
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            return call.execute();
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public Response get(String url) {
        try {
            OkHttpClient client = newClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = client.newCall(request);
            return call.execute();
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public JSONObject getAndReturnJson(String url) {
        try {
            Response response = get(url);
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public String getAndReturnString(String url) {
        try {
            Response response = get(url);
            return response.body().string();
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public JSONArray getAndReturnJsonArray(String url) {
        try {
            Response response = get(url);
            return new JSONArray(response.body().string());
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public JSONObject postAndReturnJson(String url, String body) {
        try {
            Response response = post(url, body);
            return new JSONObject(response.body().string());
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public String postAndReturnString(String url, String body) {
        try {
            Response response = post(url, body);
            return response.body().string();
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }

    public String postEmptyBody(HttpUrl url) {
        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(null, new byte[]{}))
                    .build();

            try (Response response = newClient().newCall(request).execute()) {
                return response.body().string();
            }
        } catch (IOException e) {
            throw new UnhandledGeraldException(e);
        }
    }
}
