package com.example.lrving.musicplayerapp.utils;

import android.os.Environment;

import com.example.lrving.musicplayerapp.application.App;
import com.example.lrving.musicplayerapp.domain.Music;

import java.io.File;
import java.util.ArrayList;

/**
 * 访问外部存储设备sdcard读取本地有音乐
 * Created by Lrving on 2017/6/4.
 */

public class MusicUtils {
    // 存放歌曲列表
    public static ArrayList<Music> sMusicList = new ArrayList<Music>();

    public static void initMusicList() {
        // 获取歌曲列表
        sMusicList.clear();
        sMusicList.addAll(LocalMusicUtils.queryMusic(getBaseDir()));
    }

    /**
     * 获取内存卡根
     * @return
     */
    public static String getBaseDir() {
        String dir = null;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
            dir = Environment.getExternalStorageDirectory() + File.separator;
        } else {
            dir = App.appContext.getFilesDir() + File.separator;
        }

        return dir;
    }

    /**
     * 获取应用程序使用的本地目录
     * @return
     */
    public static String getAppLocalDir() {
        String dir = null;

        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
            dir = Environment.getExternalStorageDirectory() + File.separator
                    + "Music" + File.separator;
        } else {
            dir = App.appContext.getFilesDir() + File.separator + "Music" + File.separator;
        }

        return mkdir(dir);
    }

    /**
     * 获取音乐存放目录
     * @return
     */
    public static String getMusicDir() {
        String musicDir = getAppLocalDir();
        return mkdir(musicDir);
    }

    /**
     * 获取歌词存放目录
     *
     * @return
     */
    public static String getLrcDir() {
        String lrcDir = getAppLocalDir();
        return mkdir(lrcDir);
    }

    /**
     * 创建文件夹
     * @param dir
     * @return
     */
    public static String mkdir(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            for (int i = 0; i < 5; i++) {
                if(f.mkdirs()) return dir;
            }
            return null;
        }

        return dir;
    }
}