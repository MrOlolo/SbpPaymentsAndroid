package org.sbpexample.app;

import android.app.Application;

import payments.sbp.SbpUtils;

public class ExampleApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //initialization: here bank apps packages loading will be started
        SbpUtils.getInstance();
    }
}
