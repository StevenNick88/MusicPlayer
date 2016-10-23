package com.android.musicplayer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.android.musicplayer.bean.SearchResult;
import com.android.musicplayer.utils.DownloadUtils;

import java.io.File;


public class DownloadDialogFragment extends DialogFragment{

    private SearchResult searchResult;
    private MainActivity mainActivity;

    public static DownloadDialogFragment newInstance(SearchResult searchResult){
        DownloadDialogFragment downloadDialogFragment=new DownloadDialogFragment();
        downloadDialogFragment.searchResult=searchResult;
        return downloadDialogFragment;
    }

    private String[] items;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mainActivity= (MainActivity) getActivity();
        items=new String[]{context.getString(R.string.download),"取消"};
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder=new AlertDialog.Builder(mainActivity);
        builder.setCancelable(true);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    //执行下载
                    case 0:
                        downloadMusic();
                        break;

                    //取消
                    case 1:
                        dialog.dismiss();
                        break;
                }
            }
        });
        return builder.show();

    }

    //下载音乐
    private void downloadMusic() {
        Toast.makeText(mainActivity,"正在下载："+searchResult.getMusicName(),Toast.LENGTH_SHORT).show();
        DownloadUtils.getInstance().setListener(new DownloadUtils.OnDownloadListener() {
            @Override
            public void onDownload(String mp3Url) {//下载成功
                Toast.makeText(mainActivity,"下载成功",Toast.LENGTH_SHORT).show();
                //扫描新下载的歌曲
                Uri contentUri=Uri.fromFile(new File(mp3Url));
                Intent mediaScanIntent=new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,contentUri);
                getContext().sendBroadcast(mediaScanIntent);
            }

            @Override
            public void onFailed(String error) {//下载失败
                Toast.makeText(mainActivity,error,Toast.LENGTH_SHORT).show();
            }
        }).download(searchResult);
    }
}
