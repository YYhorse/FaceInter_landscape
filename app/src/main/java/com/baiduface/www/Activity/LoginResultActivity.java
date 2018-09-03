/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baiduface.www.Activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baiduface.www.Application.BaseApplication;
import com.baiduface.www.Application.Config;
import com.baiduface.www.R;
import com.baiduface.www.Utils.HttpxUtils.HttpxUtils;
import com.baiduface.www.Utils.HttpxUtils.SendCallBack;
import com.baiduface.www.Utils.Images.ImageSaveUtil;
import com.baiduface.www.Utils.Json.ShopUserInfoJson;
import com.baiduface.www.Utils.PopMessage.PopMessageUtil;
import com.baiduface.www.Utils.PopMessage.PopWindowMessage;
import com.baiduface.www.Utils.VoiceService.VoiceService;
import com.google.gson.Gson;

import org.xutils.common.Callback;

import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * 此登录方式为加强安全级别 密码+ 人脸：先使用用户名密码登录拿到uid，
 * 再使用uid和人脸 调用https://aip.baidubce.com/rest/2.0/face/v2/verify接口
 * 实际应用时，为了防止破解app盗取ak，sk（为您在百度的标识，有了ak，sk就能使用您的账户），
 * 建议把ak，sk放在服务端，移动端把相关参数传给您出服务端去调用百度人脸注册和比对服务，
 * 然后再加上您的服务端返回的登录相关的返回参数给移动端进行相应的业务逻辑
 */

public class LoginResultActivity extends Activity {
    private static final int REQUEST_CODE = 100;
    private TextView resultTv;
    private TextView uidTv;
    private TextView scoreTv;
    private ImageView headIv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.HideTitleAndNavigation(this);
        setContentView(R.layout.activity_login_result);

        findView();
        displayData();
    }

    private void findView() {
        resultTv = (TextView) findViewById(R.id.result_tv);
        uidTv = (TextView) findViewById(R.id.uid_tv);
        scoreTv = (TextView) findViewById(R.id.score_tv);
        headIv = (ImageView) findViewById(R.id.head_iv);
    }

    private void displayData() {
        Intent intent = getIntent();
        if (intent != null) {
            boolean loginSuccess = intent.getBooleanExtra("login_success", false);
            if (loginSuccess) {
                Log.e("YY", "UID=" + intent.getStringExtra("uid") + "UserInfo=" + intent.getStringExtra("user_info") + "Score=" + intent.getDoubleExtra("score", 0));
                int faceScore = BuildRandom();
                resultTv.setText("\n识别成功，您的颜值为" + faceScore + "\nyour face value is " + faceScore);
                if (faceScore >= 90 && faceScore <= 95)
                    VoiceService.SpeechContext("您的颜值爆表");
                else if (faceScore > 95 && faceScore <= 98)
                    VoiceService.SpeechContext("您美的让人惊叹");
                else
                    VoiceService.SpeechContext("您美的让人窒息");
                String uid = "";                //intent.getStringExtra("uid");
                String userInfo = intent.getStringExtra("user_info");
                if (TextUtils.isEmpty(userInfo)) {
                    uidTv.setText(uid);
                } else {
                    uidTv.setText(uid);
                    uidTv.setText(userInfo);
                }
//                HttpGetUserInfoMethod(intent.getStringExtra("uid"));
                OpenTheDoorMethod(intent.getStringExtra("uid"));
            } else {
                VoiceService.PlayVoice(1);
                resultTv.setText("识别失败");
                VoiceService.SpeechContext("识别失败!");
                String uid = intent.getStringExtra("uid");
                String errorMsg = intent.getStringExtra("error_msg");
                uidTv.setText(uid);
                scoreTv.setText(String.valueOf(errorMsg));
            }
            headIv.setVisibility(View.VISIBLE);
            Bitmap bmp = ImageSaveUtil.loadCameraBitmap(this, "head_tmp.jpg");
            if (bmp != null) {
                headIv.setImageBitmap(bmp);
            }
        }

    }

    /***********************************************************************************************
     * * 功能说明：生成90-100的评分
     **********************************************************************************************/
    private int BuildRandom() {
        int min = 80;
        int max = 100;
        Random random = new Random();
        int num = random.nextInt(max) % (max - min + 1) + min;
        return num;
    }

    /***********************************************************************************************
     * * 功能说明：开门请求
     **********************************************************************************************/
    private void OpenTheDoorMethod(String uId) {
        PopMessageUtil.Loading(LoginResultActivity.this, "开门请求中");
        String JsonString = "{ \"userId\":" + "\"" + uId + "\"" + "} ";
        HttpxUtils.postHttp(new SendCallBack() {
            @Override
            public void onSuccess(String result) {
                PopMessageUtil.Log("登陆接口返回：" + result);
                PopMessageUtil.CloseLoading();
                if (result.contains("\"status_code\":200") == true) {
                    VoiceService.PlayVoice(0);
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            finish();
                        }
                    }, 4000);
                } else {
                    VoiceService.PlayVoice(1);
                    PopWindowMessage.PopWinMessage(LoginResultActivity.this, "接口错误", "开门请求接口错误：" + result, "error");
                    finish();
                }
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                VoiceService.PlayVoice(1);
                PopMessageUtil.Log("服务器异常!" + ex.getMessage());
                PopMessageUtil.CloseLoading();
                PopWindowMessage.PopWinMessage(LoginResultActivity.this, "服务器错误", "开门请求异常：" + ex.getMessage(), "error");
                finish();
                ex.printStackTrace();
            }
            public void onCancelled(Callback.CancelledException cex) {}
            public void onFinished() {}
        }).setUrl(Config.ShopOpenDoorUrl)
                .addJsonParameter(JsonString)
                .send();
    }

    /**************************************************************************************
     * * 功能说明：根据userId 换取用户基本信息
     *************************************************************************************/
    private void HttpGetUserInfoMethod(final String userId) {
        PopMessageUtil.Loading(LoginResultActivity.this, "获取用户信息");
        String JsonString = "{ \"userId\":" + "\"" + userId + "\"" + "} ";
        HttpxUtils.postHttp(new SendCallBack() {
            @Override
            public void onSuccess(String result) {
                PopMessageUtil.CloseLoading();
                PopMessageUtil.Log("用户信息接口返回：" + result);
                Gson gson  = new Gson();
                ShopUserInfoJson shopRegisterInfoJson = gson.fromJson(result, ShopUserInfoJson.class);
                if (shopRegisterInfoJson.getStatus_code() == 200) {

                    if(shopRegisterInfoJson.getUser().getChinaUid().compareTo("")==0){
                        //未绑定中商超会员卡
                        VoiceService.PlayVoice(1);
                        TipBindVipCard(userId,shopRegisterInfoJson.getUser().getNickname());
                    }
                    else {
                        //已绑定会员卡     开门操作
                        VoiceService.PlayVoice(0);
                        OpenTheDoorMethod(userId);
                    }
                }
                else{
                    VoiceService.PlayVoice(1);
                    PopMessageUtil.CloseLoading();
                    PopWindowMessage.PopWinMessage(LoginResultActivity.this, "接口错误", "用户识别返回：" + result, "error");
                }

            }
            public void onError(Throwable ex, boolean isOnCallback) {
                VoiceService.PlayVoice(1);
                PopMessageUtil.Log(ex.getMessage());
                PopWindowMessage.PopWinMessage(LoginResultActivity.this, "服务器错误", "用户识别返回：" + ex.getMessage(), "error");
                PopMessageUtil.CloseLoading();
                ex.printStackTrace();
            }
            public void onCancelled(Callback.CancelledException cex) {}
            public void onFinished() {}
        }).setUrl(Config.ShopGetUserUrl)
                .addJsonParameter(JsonString)
                .send();
    }

    private void TipBindVipCard(final String userId,String name) {
        VoiceService.SpeechContext("亲爱的"+name+"顾客，您是否绑定中商超会员卡?");
        final SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(LoginResultActivity.this,3);
        sweetAlertDialog
                .setTitleText("温馨提示(Reminder)")
                .setContentText("亲爱的"+name+"顾客，您是否绑定中商超会员卡?")
                .setConfirmText("绑定")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sw) {
                        sweetAlertDialog.cancel();
                        PopMessageUtil.showToastShort("功能暂时未开放!");
                        OpenTheDoorMethod(userId);
                    }
                })
                .setCancelText("不绑定")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.cancel();
                        OpenTheDoorMethod(userId);
                    }
                })
                .changeAlertType(3);
        sweetAlertDialog.setCanceledOnTouchOutside(false);
        sweetAlertDialog.show();
    }
}
