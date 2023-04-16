package main.java.de.voidtech.gerald.util;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class RequestInterceptor implements Interceptor {

    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.2; WOW64; Trident/7.0; rv:11.0) like Gecko";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request userAgentRequest = chain.request()
                .newBuilder()
                .header("User-Agent", USER_AGENT)
                .build();
        return chain.proceed(userAgentRequest);
    }
}
