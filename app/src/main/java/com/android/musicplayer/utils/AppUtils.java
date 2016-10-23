package com.android.musicplayer.utils;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.android.musicplayer.MusicPlayerApplication;

/**
 * Created by Administrator on 2016/10/23.
 */
public class AppUtils {

    //隐藏输入法
    public static void hideInputMethod(View view){
        InputMethodManager imm= (InputMethodManager) MusicPlayerApplication.context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive()){
            imm.hideSoftInputFromWindow(view.getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
        }

    }
}
