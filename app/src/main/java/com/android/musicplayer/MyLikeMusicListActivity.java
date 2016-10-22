package com.android.musicplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.android.musicplayer.adapter.MusicListAdapter;
import com.android.musicplayer.bean.Mp3Info;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class MyLikeMusicListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView;
    private MusicPlayerApplication app;
    private ArrayList<Mp3Info> likeMp3Infos;
    private MusicListAdapter adapter;
    //表示当前播放列表是否为收藏列表，因为该列表中的点击事件要复用MyMusicList中的
    private boolean isChangedLikeMusicList=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_like_list);
        app= (MusicPlayerApplication) getApplication();
        listView= (ListView) this.findViewById(R.id.listView);
        listView.setOnItemClickListener(this);
        initData();
    }

    private void initData() {
        try {
            List<Mp3Info> list=app.dbUtils.findAll(Selector.from(Mp3Info.class).where("isLike","=","1"));
            if (list==null|| list.size()==0){
                return;
            }
            likeMp3Infos= (ArrayList<Mp3Info>) list;
            adapter=new MusicListAdapter(this,likeMp3Infos);
            listView.setAdapter(adapter);
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void publish(int progress) {

    }

    @Override
    public void change(int position) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList()!=playService.LIKE_MUSIC_LIST){
            playService.setMp3Infos(likeMp3Infos);
            playService.setChangePlayList(PlayService.LIKE_MUSIC_LIST);
        }
        playService.play(position);
        //保存播放记录
        savePlayRecord();

    }

    /**
     * 保存播放记录
     */
    private void savePlayRecord() {
        //获取当前正在播放的音乐对象
        Mp3Info mp3Info=playService.getMp3Infos().get(playService.getCurrentPosition());
        try {
            Mp3Info playRecordMp3Info=app.dbUtils.findFirst(
                    Selector.from(Mp3Info.class).where("mp3InfoId","=",mp3Info.getMp3InfoId()));
            if (playRecordMp3Info==null){
                mp3Info.setPlayTime(System.currentTimeMillis());//设置当前播放时间
                app.dbUtils.save(mp3Info);
            }else {
                playRecordMp3Info.setPlayTime(System.currentTimeMillis());
                app.dbUtils.update(playRecordMp3Info,"playTime");
            }
        } catch (DbException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        bindPlayService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unBindPlayService();
    }
}
