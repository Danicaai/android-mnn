package cn.sample.mnn.detect;

import android.app.Application;
import android.graphics.Typeface;

/**
 * Created by GDD on 2019/4/16.
 */


public class App extends Application {

    private static final String TAG = "WebSocketLib";
    private static App instance;
    private Typeface typeface;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;


    }

    public static App getInstance() {
        return instance;
    }


    public static App getInstace() { return instance;}


}
