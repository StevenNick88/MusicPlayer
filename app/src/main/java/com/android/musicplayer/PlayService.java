package com.android.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import com.android.musicplayer.bean.Mp3Info;
import com.android.musicplayer.utils.MediaUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 音乐播放的服务组件
 * 实现功能：
 * 1、播放
 * 2、暂停
 * 3、上一首
 * 4、下一首
 * 5、获取当前的播放进度
 */
public class PlayService extends Service implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private MediaPlayer player;
    private int currentPosition;//当前正在播放的位置
    private ArrayList<Mp3Info> mp3Infos;
    private MusicUpdateListener musicUpdateListener;
    private boolean isPause = false;
    private Random random = new Random();

    //切换播放列表(主要是看目前是点击我的音乐列表中的歌曲，
    // 还是我喜欢列表中的歌曲，更新底部的播条和播放界面的数据)
    public static final int MY_MUSIC_LIST=1;// 我的音乐列表
    public static final int LIKE_MUSIC_LIST=2;// 我喜欢的列表
    public static final int PLAY_RECORD_MUSIC_LIST=3;// 最近播放的列表
    private int changePlayList=MY_MUSIC_LIST;

    //播放模式
    private int play_mode = ORDER_PLAY;
    public static final int ORDER_PLAY = 1;
    public static final int RANDOM_PLAY = 2;
    public static final int SINGLE_PLAY = 3;

    //声明线程池
    private ExecutorService es = Executors.newSingleThreadExecutor();

    public PlayService() {
    }

    public int getChangePlayList() {
        return changePlayList;
    }

    public void setChangePlayList(int changePlayList) {
        this.changePlayList = changePlayList;
    }

    public void setMp3Infos(ArrayList<Mp3Info> mp3Infos) {
        this.mp3Infos = mp3Infos;
    }

    public ArrayList<Mp3Info> getMp3Infos() {
        return mp3Infos;
    }

    public int getPlay_mode() {
        return play_mode;
    }

    public void setPlay_mode(int play_mode) {
        this.play_mode = play_mode;
    }

    public boolean isPause() {
        return isPause;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {
        switch (play_mode) {
            case ORDER_PLAY:
                next();
                break;

            case RANDOM_PLAY:
                play(random.nextInt(mp3Infos.size()));
                break;

            case SINGLE_PLAY:
                play(currentPosition);
                break;

            default:
                break;
        }

    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        player.reset();
        return false;
    }

    class PlayBinder extends Binder {
        public PlayService getPlayService() {
            return PlayService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        PlayBinder binder = new PlayBinder();
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MusicPlayerApplication app = (MusicPlayerApplication) getApplication();
        currentPosition = app.sp.getInt("currentPosition", 0);
        play_mode = app.sp.getInt("play_mode", PlayService.ORDER_PLAY);

        player = new MediaPlayer();
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
        mp3Infos = MediaUtils.getMp3Infos(this);
        es.execute(updateStateRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (es != null && !es.isShutdown()) {
            es.shutdown();
            es = null;
        }
    }

    Runnable updateStateRunnable = new Runnable() {
        @Override
        public void run() {
            while (true) {
                if (musicUpdateListener != null && player != null && player.isPlaying()) {
                    musicUpdateListener.onPublish(getCurrentProgress());
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //播放
    public void play(int position) {
        Mp3Info mp3Info = null;
        if (position < 0 || position >= mp3Infos.size()) {
            position = 0;
        }
        mp3Info = mp3Infos.get(position);
        try {
            player.reset();
            player.setDataSource(this, Uri.parse(mp3Info.getUrl()));
            player.prepare();
            player.start();
            currentPosition = position;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (musicUpdateListener != null) {
            musicUpdateListener.onChange(currentPosition);
        }
    }

    //暂停
    public void pause() {
        if (player.isPlaying()) {
            player.pause();
            isPause = true;
        }

    }

    //下一首
    public void next() {
        if (currentPosition + 1 >= mp3Infos.size()) {
            currentPosition = 0;
        } else {
            currentPosition++;
        }
        play(currentPosition);

    }

    //上一首
    public void prev() {
        if (currentPosition - 1 < 0) {
            currentPosition = mp3Infos.size() - 1;
        } else {
            currentPosition--;
        }
        play(currentPosition);

    }

    //开始播放
    public void start() {
        if (player != null && !player.isPlaying()) {
            player.start();
        }
    }

    //是否正在播放
    public boolean isPlaying() {
        if (player != null) {
            return player.isPlaying();
        }
        return false;
    }

    //获取当前播放进度
    public int getCurrentProgress() {
        if (player != null && player.isPlaying()) {
            return player.getCurrentPosition();
        }
        return 0;
    }

    //获取播放时长
    public int getDuration() {
        return player.getDuration();
    }

    //快进到sec
    public void seekTo(int sec) {
        player.seekTo(sec);
    }

    //在触发事件的地方定义接口
    public interface MusicUpdateListener {
        public void onPublish(int progress);

        public void onChange(int position);
    }

    public void setMusicUpdateListener(MusicUpdateListener musicUpdateListener) {
        this.musicUpdateListener = musicUpdateListener;
    }
}
