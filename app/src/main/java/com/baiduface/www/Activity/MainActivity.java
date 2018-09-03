package com.baiduface.www.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.baiduface.www.Application.BaseApplication;
import com.baiduface.www.R;
import com.baiduface.www.Utils.HttpxUtils.HttpxUtils;
import com.baiduface.www.Utils.HttpxUtils.SendCallBack;
import com.baiduface.www.Utils.PopMessage.PopMessageUtil;
import com.baiduface.www.Utils.PopMessage.PopWindowMessage;
import com.baiduface.www.Utils.VoiceService.VoiceService;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import org.xutils.common.Callback;

public class MainActivity extends Activity {
    ImageView we9gologo_image,face_image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.HideTitleAndNavigation(this);
        setContentView(R.layout.activity_main);
        initUi();
    }

    private void initUi() {
        we9gologo_image = (ImageView) findViewById(R.id.we9gologo_image);
        face_image      = (ImageView) findViewById(R.id.face_image);
        Glide.with(MainActivity.this).load(R.drawable.face).asGif().dontAnimate().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(face_image);

        we9gologo_image.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                home.addCategory(Intent.CATEGORY_HOME);
                startActivity(home);
                return false;
            }
        });
    }

    /***********************************************************************************************
     * * 功能说明： 点击面部识别
     **********************************************************************************************/
    public void ClickFaceInterfaceMethod(View view){
        Intent intent = new Intent(MainActivity.this, DetectLoginActivity.class);
        startActivity(intent);
    }

    /***********************************************************************************************
     * * 功能说明： 无购物通道激活
     **********************************************************************************************/
    public void ClickNoFoodsChanelMethod(View view){
        PopMessageUtil.Loading(MainActivity.this, "Request\n请求中");
        String JsonString = "{ \"userId\":" + "\"ojwtN5QhnhOwNymTsLOYvstYoQe8\"" + "} ";
        VoiceService.PlayVoice(0);
        HttpxUtils.postHttp(new SendCallBack() {
            @Override
            public void onSuccess(String result) {
                PopMessageUtil.CloseLoading();
                Log.e("YY", "登陆接口返回：" + result);
                if (result.contains("\"status_code\":200") == true) {
                    VoiceService.SpeechContext("Please wait for the door open,请等待开门");
                } else
                    PopWindowMessage.PopWinMessage(MainActivity.this,"请求接口失败","无购物接口"+result,"error");
            }
            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                PopMessageUtil.CloseLoading();
                Log.e("YY", "服务器异常!" + ex.getMessage());
                PopWindowMessage.PopWinMessage(MainActivity.this, "请求服务器失败", "无购物接口" + ex.getMessage(), "error");
                ex.printStackTrace();
            }
            public void onCancelled(Callback.CancelledException cex) {}
            public void onFinished() {}
        }).setUrl("https://www.matrixsci.cn/supermarket/store/open?")
                .addJsonParameter(JsonString)
                .send();
    }
}
