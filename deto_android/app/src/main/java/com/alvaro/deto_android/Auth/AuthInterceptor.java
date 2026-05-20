package com.alvaro.deto_android.Auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private Context context;
    private static final String TAG = "AuthInterceptor";

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        String url = originalRequest.url().toString();

        Log.d(TAG, "🌐 URL: " + url);

        // 🚫 NO añadir token en endpoints públicos
        if (url.contains("/auth/registro/") || url.contains("/auth/login/")) {
            Log.d(TAG, "🚫 Endpoint público, sin token");
            return chain.proceed(originalRequest);
        }

        // ✅ Añadir token en endpoints protegidos
        SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        if (token != null) {
            Log.d(TAG, "✅ Token añadido");
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .method(originalRequest.method(), originalRequest.body())
                    .build();
            return chain.proceed(newRequest);
        } else {
            Log.d(TAG, "⚠️ No hay token");
            return chain.proceed(originalRequest);
        }
    }
}