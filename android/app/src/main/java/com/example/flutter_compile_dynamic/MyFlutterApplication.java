package com.example.flutter_compile_dynamic;

import android.app.Activity;
import android.app.Application;

import androidx.annotation.CallSuper;

public class MyFlutterApplication extends Application {
    private Activity mCurrentActivity = null;

    public MyFlutterApplication() {
    }

    @CallSuper
    public void onCreate() {
        super.onCreate();
        MyFlutterMain.startInitialization(this);
    }

    public Activity getCurrentActivity() {
        return this.mCurrentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        this.mCurrentActivity = mCurrentActivity;
    }
}
