package com.android.musicplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/10/19.
 */
public abstract class BaseActivity extends FragmentActivity {

    protected PlayService playService;
    private boolean isBound = false;
    private static ArrayList<Activity> listActivity=new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ActionBar actionBar=this.getActionBar();
//        actionBar.setIcon(R.mipmap.app_logo2);
        listActivity.add(this);
    }

    /**
     * 全局退出功能
     */
    public static void exitApp(){
        for (int i=0;i<listActivity.size();i++){
            listActivity.get(i).finish();
        }
    }

    private ServiceConnection conn=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PlayService.PlayBinder playBinder= (PlayService.PlayBinder) service;
            playService=playBinder.getPlayService();
            playService.setMusicUpdateListener(musicUpdateListener);
            musicUpdateListener.onChange(playService.getCurrentPosition());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            playService=null;
            isBound=false;
        }
    };

    //模板设计模式，让子类去实现具体的方法
    private PlayService.MusicUpdateListener musicUpdateListener=new PlayService.MusicUpdateListener() {
        @Override
        public void onPublish(int progress) {
            publish(progress);
        }

        @Override
        public void onChange(int position) {
            change(position);
        }
    };

    public abstract void publish(int progress);
    public abstract void change(int position);

    public void bindPlayService(){
        if (!isBound){
            Intent intent=new Intent(this,PlayService.class);
            bindService(intent,conn, Context.BIND_AUTO_CREATE);
            isBound=true;
        }
    }

    public void unBindPlayService(){
        if (isBound){
            unbindService(conn);
            isBound=false;
        }
    }


}



