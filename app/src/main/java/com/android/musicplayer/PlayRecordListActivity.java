package com.android.musicplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.musicplayer.adapter.MusicListAdapter;
import com.android.musicplayer.bean.Mp3Info;
import com.android.musicplayer.utils.Constant;
import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.exception.DbException;

import java.util.ArrayList;
import java.util.List;

public class PlayRecordListActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private ListView listView_play_record;
    private TextView textView2_no_data;
    public MusicPlayerApplication app;
    private ArrayList<Mp3Info> mp3Infos;
    private MusicListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_record_list);
        app = (MusicPlayerApplication) getApplication();
        listView_play_record = (ListView) this.findViewById(R.id.listView_play_record);
        textView2_no_data = (TextView) this.findViewById(R.id.textView2_no_data);
        listView_play_record.setOnItemClickListener(this);
        initData();

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

    /**
     * 初始化最近播放记录
     */
    private void initData() {
        try {
            //查询最近播放记录
            List<Mp3Info> list = app.dbUtils.findAll(Selector.from(Mp3Info.class).where("playTime", "!=", 0)
                    .orderBy("playTime", true).limit(Constant.PLAY_RECORD_NUM));
            if (list == null || list.size() == 0) {
                textView2_no_data.setVisibility(View.VISIBLE);
                listView_play_record.setVisibility(View.GONE);
            } else {
                textView2_no_data.setVisibility(View.GONE);
                listView_play_record.setVisibility(View.VISIBLE);
                mp3Infos = (ArrayList<Mp3Info>) list;
                adapter = new MusicListAdapter(this, mp3Infos);
                listView_play_record.setAdapter(adapter);
            }
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

    /**
     * 用户在item上点击时更换播放列表
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (playService.getChangePlayList() != PlayService.PLAY_RECORD_MUSIC_LIST) {
            playService.setMp3Infos(mp3Infos);
            playService.setChangePlayList(PlayService.PLAY_RECORD_MUSIC_LIST);
        }
        playService.play(position);
    }
}
