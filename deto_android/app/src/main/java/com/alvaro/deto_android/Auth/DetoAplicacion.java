package com.alvaro.deto_android.Auth;

import android.app.Application;

import com.alvaro.deto_android.RetrofitClient;

public class DetoAplicacion extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitClient.init(this);
    }
}