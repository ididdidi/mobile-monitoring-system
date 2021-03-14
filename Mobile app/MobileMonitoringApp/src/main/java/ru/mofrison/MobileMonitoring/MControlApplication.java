package ru.mofrison.MobileMonitoring;

import android.app.Application;
import android.util.Log;

import ru.mofrison.MobileMonitoring.mqtt.Connection;

public class MControlApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("APPLICATION", "onCreate MControlApplication");

        Connection.initInstance();
    }
}
