package com.android.musicplayer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.musicplayer.utils.Constant;
import com.lidroid.xutils.DbUtils;

/**
 * Created by Administrator on 2016/10/21.
 */
public class MusicPlayerApplication extends Application {

    public static SharedPreferences sp;
    public static DbUtils dbUtils;
    public static Context context;


    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constant.SP_NAME, Context.MODE_PRIVATE);
        dbUtils = DbUtils.create(getApplicationContext(), Constant.DB_NAME);//为音乐收藏创建的数据库
        context = getApplicationContext();
    }
}
