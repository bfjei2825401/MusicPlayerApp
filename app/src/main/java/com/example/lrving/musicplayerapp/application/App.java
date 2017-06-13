package com.example.lrving.musicplayerapp.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.example.lrving.musicplayerapp.service.PlayService;
import com.example.lrving.musicplayerapp.utils.MusicUtils;

/**
 * Created by Lrving on 2017/6/7.
 */

public class App extends Application {
    public static Context appContext;
    public static int appScreenWidth;
    public static int appScreenheight;


    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        //获取屏幕宽度高度
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        appScreenWidth=dm.widthPixels;
        appScreenheight=dm.heightPixels;
        MusicUtils.initMusicList();
        startService(new Intent(this, PlayService.class));
    }
}
