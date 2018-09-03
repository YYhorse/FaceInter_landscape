package com.baiduface.www.Utils.PopMessage;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.baiduface.www.Application.BaseApplication;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by yy on 2017/9/25.
 */
public class PopMessageUtil {
    /**********************************************************************************************
     * * 功能说明：进度条加载
     **********************************************************************************************/
    public static SweetAlertDialog pDialog;

    public static void CloseLoading() {
        if (pDialog != null) {
            if (pDialog.isShowing())
                pDialog.cancel();
            pDialog = null;
        }
    }

    public static void Loading(Activity activity, String EnglishTitle) {
        if (pDialog == null) {
            pDialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
        }
        pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        pDialog.setCancelable(false);
        pDialog.setTitleText(EnglishTitle);
        pDialog.show();
        pDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    pDialog.cancel();
                }
                return false;
            }
        });
    }

    /**********************************************************************************************
     * * 功能说明：Toast
     **********************************************************************************************/
    private static Toast mToast;

    public static void showToastLong(String EnglishMessage) {
        showToast(EnglishMessage, Toast.LENGTH_LONG);
    }

    public static void showToastShort(String EnglishMessage) {
            showToast(EnglishMessage, Toast.LENGTH_SHORT);
    }

    private static void showToast(String text, int time) {
        if (mToast == null)
            mToast = Toast.makeText(BaseApplication.getInstance(), text, time);
        else {
            mToast.setText(text);
            mToast.setDuration(Toast.LENGTH_SHORT);
        }
        mToast.show();
    }

    /**********************************************************************************************
     * * 功能说明：Log.e
     **********************************************************************************************/
    public static void Log(String context) {
        Log.e("YY", context);
    }
}
