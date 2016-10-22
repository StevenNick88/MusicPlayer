package com.android.musicplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.musicplayer.bean.Mp3Info;
import com.android.musicplayer.utils.MediaUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class PlayActivity extends BaseActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final String TAG = "PlayActivity";
    private static final int UPDATE_TIME = 0x1;//更新播放时间的标记
    private TextView textView1_title, textView1_start_time, textView1_end_time;
    private ImageView imageButton1_next, imageButton2_play_pause, imageButton3_previous,
            imageButton1_play_mode, imageView1_album, imageView1_favorite;
    private SeekBar seekBar1;
//    private ArrayList<Mp3Info> mp3Infos;
    private ViewPager viewPager;
    private PlayPagerAdapter playPagerAdapter;
    private List<View> views;
    private MusicPlayerApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        app = (MusicPlayerApplication) getApplication();
        views = getPagerViews();
        textView1_start_time = (TextView) this.findViewById(R.id.textView1_start_time);
        textView1_end_time = (TextView) this.findViewById(R.id.textView1_end_time);
        imageButton1_next = (ImageView) this.findViewById(R.id.imageButton1_next);
        imageButton2_play_pause = (ImageView) this.findViewById(R.id.imageButton2_play_pause);
        imageButton3_previous = (ImageView) this.findViewById(R.id.imageButton3_previous);
        imageButton1_play_mode = (ImageView) this.findViewById(R.id.imageButton1_play_mode);
        imageView1_favorite = (ImageView) this.findViewById(R.id.imageView1_favorite);

        seekBar1 = (SeekBar) this.findViewById(R.id.seekBar1);
        seekBar1.setOnSeekBarChangeListener(this);

        viewPager = (ViewPager) this.findViewById(R.id.viewpager);
        playPagerAdapter = new PlayPagerAdapter();
        viewPager.setAdapter(playPagerAdapter);

        imageButton2_play_pause.setOnClickListener(this);
        imageButton3_previous.setOnClickListener(this);
        imageButton1_next.setOnClickListener(this);
        imageButton1_play_mode.setOnClickListener(this);
        imageView1_favorite.setOnClickListener(this);

        progressHandler = new ProgressHandler(this);
    }

    private ArrayList<View> getPagerViews() {
        ArrayList<View> views = new ArrayList();
        View albumView = LinearLayout.inflate(PlayActivity.this, R.layout.album_image_layout, null);
        textView1_title = (TextView) albumView.findViewById(R.id.textView1_title);
        imageView1_album = (ImageView) albumView.findViewById(R.id.imageView1_album);
        View lrcView = LinearLayout.inflate(PlayActivity.this, R.layout.lrc_layout, null);
        views.add(albumView);
        views.add(lrcView);

        return views;
    }

    private class PlayPagerAdapter extends PagerAdapter {

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;

        }

        @Override
        public int getCount() {
            return views.size();
        }

        // 显示界面方法
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView(views.get(position));

        }

        // 销毁界面方法
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ((ViewPager) container).addView(views.get(position));
            return views.get(position);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "PlayActivity--bindPlayService");
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "PlayActivity--bindPlayService");
        unBindPlayService();
    }

    private static ProgressHandler progressHandler;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            this.playService.pause();
            this.playService.seekTo(progress);
            this.playService.start();
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    static class ProgressHandler extends Handler {

        private PlayActivity playActivity;

        public ProgressHandler(PlayActivity playActivity) {
            this.playActivity = playActivity;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_TIME:
                    playActivity.textView1_start_time.setText(MediaUtils.formatTime(msg.arg1));
                    break;

            }
        }
    }

    @Override
    public void publish(int progress) {
        Message msg = progressHandler.obtainMessage(UPDATE_TIME);
        msg.arg1 = progress;
        progressHandler.sendMessage(msg);
        seekBar1.setProgress(progress);
    }

    @Override
    public void change(int position) {
        Mp3Info mp3Info = this.playService.getMp3Infos().get(position);
        textView1_title.setText(mp3Info.getTitle());
        Bitmap albumBitmap = MediaUtils.getArtwork(this, mp3Info.getId(), mp3Info.getAlbumId(), true, false);
        imageView1_album.setImageBitmap(albumBitmap);
        textView1_end_time.setText(MediaUtils.formatTime(mp3Info.getDuration()));
        seekBar1.setProgress(0);
        seekBar1.setMax((int) mp3Info.getDuration());
        //初始化/更新播放状态
        if (playService.isPlaying()) {
            imageButton2_play_pause.setImageResource(R.mipmap.pause);
        } else {
            imageButton2_play_pause.setImageResource(R.mipmap.play);
        }
        //初始化/更新播放模式
        switch (playService.getPlay_mode()) {
            case PlayService.ORDER_PLAY:
                imageButton1_play_mode.setImageResource(R.mipmap.order);
                imageButton1_play_mode.setTag(PlayService.ORDER_PLAY);
                break;

            case PlayService.RANDOM_PLAY:
                imageButton1_play_mode.setImageResource(R.mipmap.random);
                imageButton1_play_mode.setTag(PlayService.RANDOM_PLAY);
                break;

            case PlayService.SINGLE_PLAY:
                imageButton1_play_mode.setImageResource(R.mipmap.single);
                imageButton1_play_mode.setTag(PlayService.SINGLE_PLAY);
                break;

        }
        //初始化/更新是否收藏
        try {
            Mp3Info likeMp3Info = app.dbUtils.findFirst(
                    Selector.from(Mp3Info.class).where("mp3InfoId", "=", mp3Info.getMp3InfoId()));
            if (likeMp3Info!=null){
                imageView1_favorite.setImageResource(R.mipmap.xin_hong);
            }else {
                imageView1_favorite.setImageResource(R.mipmap.xin_bai);
            }
        } catch (DbException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageButton2_play_pause:
                if (this.playService.isPlaying()) {
                    imageButton2_play_pause.setImageResource(R.mipmap.play);
                    this.playService.pause();
                } else {
                    //播放、暂停
                    if (this.playService.isPause()) {
                        imageButton2_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        this.playService.start();
                        //从头开始播 (这里在play（）方法中已经做了图片的改变，不需要了)
                    } else {
                        this.playService.play(playService.getCurrentPosition());
                    }
                }
                break;

            case R.id.imageButton1_next:
                this.playService.next();
                break;

            case R.id.imageButton3_previous:
                this.playService.prev();
                break;

            //播放模式
            case R.id.imageButton1_play_mode:
                int mode = (int) imageButton1_play_mode.getTag();
                switch (mode) {
                    case PlayService.ORDER_PLAY:
                        imageButton1_play_mode.setImageResource(R.mipmap.random);
                        imageButton1_play_mode.setTag(PlayService.RANDOM_PLAY);
                        playService.setPlay_mode(PlayService.RANDOM_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.random_play), Toast.LENGTH_SHORT).show();
                        break;

                    case PlayService.RANDOM_PLAY:
                        imageButton1_play_mode.setImageResource(R.mipmap.single);
                        imageButton1_play_mode.setTag(PlayService.SINGLE_PLAY);
                        playService.setPlay_mode(PlayService.SINGLE_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.single_play), Toast.LENGTH_SHORT).show();
                        break;

                    case PlayService.SINGLE_PLAY:
                        imageButton1_play_mode.setImageResource(R.mipmap.order);
                        imageButton1_play_mode.setTag(PlayService.ORDER_PLAY);
                        playService.setPlay_mode(PlayService.ORDER_PLAY);
                        Toast.makeText(PlayActivity.this, getString(R.string.order_play), Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }
                break;

            //收藏音乐
            case R.id.imageView1_favorite:
                Mp3Info mp3Info = this.playService.getMp3Infos().get(playService.getCurrentPosition());
                Log.v(TAG, "mp3Info:" + mp3Info);
                try {
                    Mp3Info likeMp3Info = app.dbUtils.findFirst(
                            Selector.from(Mp3Info.class).where("mp3InfoId", "=",getId(mp3Info)));
                    Log.v(TAG, "mp3Info:" + likeMp3Info);
                    //收藏表中沒有这首歌
                    if (likeMp3Info == null) {
                        mp3Info.setMp3InfoId(mp3Info.getId());
                        mp3Info.setIsLike(1);
                        app.dbUtils.save(mp3Info);
                        imageView1_favorite.setImageResource(R.mipmap.xin_hong);
                        Log.v(TAG, "mp3Info save");
                        //收藏表中有这首歌
                    } else {
                        int isLike=likeMp3Info.getIsLike();
                        if (isLike==1){
                            likeMp3Info.setIsLike(0);
                            imageView1_favorite.setImageResource(R.mipmap.xin_bai);
                        }else {
                            likeMp3Info.setIsLike(1);
                            imageView1_favorite.setImageResource(R.mipmap.xin_hong);
                        }
                        app.dbUtils.update(likeMp3Info,"isLike");
                        Log.v(TAG, "mp3Info update");
                    }
                } catch (DbException e) {
                    e.printStackTrace();
                }
                break;

            default:
                break;

        }
    }

    private long getId(Mp3Info mp3Info){
        //初始收藏状态
        long id=0;
        switch (playService.getChangePlayList()){
            case PlayService.MY_MUSIC_LIST:
                id=mp3Info.getId();
                break;
            case PlayService.LIKE_MUSIC_LIST:
                id=mp3Info.getMp3InfoId();
                break;
            default:
                break;

        }
        return id;

    }
}
