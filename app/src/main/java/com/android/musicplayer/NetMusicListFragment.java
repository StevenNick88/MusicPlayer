package com.android.musicplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.musicplayer.adapter.NetMusicListAdapter;
import com.android.musicplayer.bean.SearchResult;
import com.android.musicplayer.utils.AppUtils;
import com.android.musicplayer.utils.Constant;
import com.android.musicplayer.utils.SearchMusicUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;


public class NetMusicListFragment extends Fragment implements View.OnClickListener ,AdapterView.OnItemClickListener{

    private MainActivity mainActivity;
    private ListView listView_net_music;
    private LinearLayout load_layout,ll_search_btn_container,ll_search_container;
    private ImageButton ib_search_btn;
    private EditText et_search_content;
    private ArrayList<SearchResult> searchResults=new ArrayList<>();
    private NetMusicListAdapter netMusicListAdapter;
    private int page=1;//搜索音乐的页码

    public static NetMusicListFragment newInstance(){
        NetMusicListFragment net=new NetMusicListFragment();
        return  net;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity= (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_net_list,null);
        listView_net_music= (ListView) view.findViewById(R.id.listView_net_music);
        load_layout= (LinearLayout) view.findViewById(R.id.load_layout);
        ll_search_btn_container= (LinearLayout) view.findViewById(R.id.ll_search_btn_container);
        ll_search_container= (LinearLayout) view.findViewById(R.id.ll_search_container);
        ib_search_btn= (ImageButton) view.findViewById(R.id.ib_search_btn);
        et_search_content= (EditText) view.findViewById(R.id.et_search_content);

        listView_net_music.setOnItemClickListener(this);
        ll_search_btn_container.setOnClickListener(this);
        ib_search_btn.setOnClickListener(this);
        loadNetData();

        return view;

    }

    private void loadNetData() {
        load_layout.setVisibility(View.VISIBLE);//显示
        //执行异步加载网络音乐的任务
        new LoadNetDataTask().execute(Constant.BAIDU_URL+Constant.BAIDU_DAYHOT);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ll_search_btn_container:
                ll_search_btn_container.setVisibility(View.GONE);
                ll_search_container.setVisibility(View.VISIBLE);
                break;

            //搜索事件处理
            case R.id.ib_search_btn:
                searchMusic();
                break;
        }

    }

    /**
     * 搜索音乐
     */
    private void searchMusic() {
        //隐藏输入法
        AppUtils.hideInputMethod(et_search_content);
        ll_search_btn_container.setVisibility(View.VISIBLE);
        ll_search_container.setVisibility(View.GONE);
        String key=et_search_content.getText().toString();
        if (TextUtils.isEmpty(key)){
            Toast.makeText(mainActivity,"请输入歌名",Toast.LENGTH_SHORT).show();
            return;
        }
        load_layout.setVisibility(View.VISIBLE);
        SearchMusicUtils.getsInstance().setListener(new SearchMusicUtils.OnSearchResultListener() {
            @Override
            public void onSearchResult(ArrayList<SearchResult> results) {
                ArrayList<SearchResult> sr=netMusicListAdapter.getSearchResults();
                sr.clear();
                sr.addAll(results);
                netMusicListAdapter.notifyDataSetChanged();
                load_layout.setVisibility(View.GONE);
            }
        }).search(key,page);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position>=netMusicListAdapter.getSearchResults().size() || position<0){
            return;
        }
        showDownloadDialog(position);
    }

    //下载弹窗
    private void showDownloadDialog(final int position) {
        DownloadDialogFragment downloadDialogFragment=DownloadDialogFragment
                .newInstance(netMusicListAdapter.getSearchResults().get(position));
        downloadDialogFragment.show(getFragmentManager(),"download");

    }

    private class LoadNetDataTask extends AsyncTask<String,Integer,Integer>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            load_layout.setVisibility(View.VISIBLE);
            listView_net_music.setVisibility(View.GONE);
            searchResults.clear();
        }

        @Override
        protected Integer doInBackground(String... params) {
            String url=params[0];
            try {
                //使用Jsoup组件请求网络，并解析音乐数据
                Document doc= Jsoup.connect(url).userAgent(Constant.BAIDU_AGENT).timeout(6*1000).get();
                Elements songTitles=doc.select("span.song-title");
                Elements artists=doc.select("span.author_list");
                for (int i=0;i<songTitles.size();i++){
                    SearchResult searchResult=new SearchResult();
                    Elements urls=songTitles.get(i).getElementsByTag("a");
                    searchResult.setUrl(urls.get(0).attr("href"));
                    searchResult.setMusicName(urls.get(0).text());

                    Elements artistElements=artists.get(i).getElementsByTag("a");
                    searchResult.setArtist(artistElements.get(0).text());

                    searchResult.setAlbum("热歌榜");
                    searchResults.add(searchResult);
                }

            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            return 1;
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            if (result==1){
                netMusicListAdapter=new NetMusicListAdapter(mainActivity,searchResults);
                listView_net_music.setAdapter(netMusicListAdapter);
                listView_net_music.addFooterView(LayoutInflater.from(mainActivity).inflate(R.layout.footview_layout,null));
            }
            load_layout.setVisibility(View.GONE);
            listView_net_music.setVisibility(View.VISIBLE);
        }
    }
}
