package com.android.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.musicplayer.adapter.MusicListAdapter;
import com.android.musicplayer.bean.Mp3Info;
import com.android.musicplayer.utils.MediaUtils;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;


public class MyMusicListFragment extends Fragment implements AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG="MyMusicListFragment";
    private ListView listView;
    private ImageView imageView_album, imageView2_play_pause, imageView3_next;
    private TextView textView_songName, textView2_singer;

    private ArrayList<Mp3Info> mp3Infos;
    private MusicListAdapter myMusicListAdapter;
    private MainActivity mainActivity;
    private int position=0;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;

    }

    public static MyMusicListFragment newInstance() {
        MyMusicListFragment my = new MyMusicListFragment();
        return my;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_list, container, false);
        listView = (ListView) view.findViewById(R.id.listView);
        imageView_album = (ImageView) view.findViewById(R.id.imageView_album);
        imageView2_play_pause = (ImageView) view.findViewById(R.id.imageView2_play_pause);
        imageView3_next = (ImageView) view.findViewById(R.id.imageView3_next);
        textView_songName = (TextView) view.findViewById(R.id.textView_songName);
        textView2_singer = (TextView) view.findViewById(R.id.textView2_singer);
        listView.setOnItemClickListener(this);
        imageView2_play_pause.setOnClickListener(this);
        imageView3_next.setOnClickListener(this);
        imageView_album.setOnClickListener(this);
        loadData();
        return view;
    }

    /**
     * 加載本地音乐列表
     */
    public void loadData() {
        mp3Infos =MediaUtils.getMp3Infos(mainActivity);
        myMusicListAdapter = new MusicListAdapter(mainActivity, mp3Infos);
        listView.setAdapter(myMusicListAdapter);
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG,"MyMusicListFragment--bindPlayService");
        mainActivity.bindPlayService();
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG,"MyMusicListFragment--unBindPlayService");
        mainActivity.unBindPlayService();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mainActivity.playService.getChangePlayList()!=PlayService.MY_MUSIC_LIST){
            mainActivity.playService.setMp3Infos(mp3Infos);
            mainActivity.playService.setChangePlayList(PlayService.MY_MUSIC_LIST);
        }
        mainActivity.playService.play(position);
        //保存播放记录
        savePlayRecord();
    }


    /**
     * 保存播放记录
     */
    private void savePlayRecord() {
        //获取当前正在播放的音乐对象
        Mp3Info mp3Info=mainActivity.playService.getMp3Infos().get(mainActivity.playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info=mainActivity.app.dbUtils.findFirst(
                    Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getId()));
            if (playRecordMp3Info==null){
                mp3Info.setMp3InfoId(mp3Info.getId());
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前播放时间
                mainActivity.app.dbUtils.save(mp3Info);
            }else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                mainActivity.app.dbUtils.update(playRecordMp3Info,"playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    public void changUIStatusOnPlay(int position) {
        if (position >= 0 && position < mainActivity.playService.getMp3Infos().size()) {
            Mp3Info mp3Info = mainActivity.playService.getMp3Infos().get(position);
            textView_songName.setText(mp3Info.getTitle());
            textView2_singer.setText(mp3Info.getArtist());
            if (mainActivity.playService.isPlaying()){
                imageView2_play_pause.setImageResource(R.mipmap.pause);
            }else {
                imageView2_play_pause.setImageResource(R.mipmap.play);
            }

            Bitmap albumBitmap = MediaUtils.getArtwork(mainActivity, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
            imageView_album.setImageBitmap(albumBitmap);

            this.position=position;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageView2_play_pause:
                if (mainActivity.playService.isPlaying()) {
                    imageView2_play_pause.setImageResource(R.mipmap.player_btn_play_normal);
                    mainActivity.playService.pause();
                } else {
                    //播放、暂停
                    if (mainActivity.playService.isPause()) {
                        imageView2_play_pause.setImageResource(R.mipmap.player_btn_pause_normal);
                        mainActivity.playService.start();
                        //从头开始播 (这里在play（）方法中已经做了图片的改变，不需要了)
                    } else {
                        mainActivity.playService.play(mainActivity.playService.getCurrentPosition());
                    }
                }
                break;

            case R.id.imageView3_next:
                mainActivity.playService.next();
                break;

            case R.id.imageView_album:
                Intent intent = new Intent(mainActivity, PlayActivity.class);
                startActivity(intent);
                break;

            default:
                break;

        }

    }
}
