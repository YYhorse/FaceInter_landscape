package com.baiduface.www.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.baiduface.www.Application.APIService;
import com.baiduface.www.Application.BaseApplication;
import com.baiduface.www.Application.Config;
import com.baiduface.www.R;
import com.baiduface.www.Utils.Datas.Base64;
import com.baiduface.www.Utils.Datas.Unicode;
import com.baiduface.www.Utils.Https.OnResultListener;
import com.baiduface.www.Utils.HttpxUtils.HttpxUtils;
import com.baiduface.www.Utils.HttpxUtils.SendCallBack;
import com.baiduface.www.Utils.Images.ImageSaveUtil;
import com.baiduface.www.Utils.Json.ChinaRegisterInfoJson;
import com.baiduface.www.Utils.Json.ShopUserInfoJson;
import com.baiduface.www.Utils.Json.UserInfoJson;
import com.baiduface.www.Utils.Model.FaceError;
import com.baiduface.www.Utils.Model.RegResult;
import com.baiduface.www.Utils.PopMessage.PopMessageUtil;
import com.baiduface.www.Utils.PopMessage.PopWindowMessage;
import com.baiduface.www.Utils.SwitchUtil;
import com.baiduface.www.Utils.VoiceService.VoiceService;
import com.baiduface.www.Utils.sha256encrypt.SHA256Encrypt;
import com.google.gson.Gson;

import org.xutils.common.Callback;

import java.io.File;

/**
 * Created by yy on 2018/5/29.
 * 主要功能   人脸注册
 */
public class UserRegisterActivity extends Activity {
    private static final int REQUEST_CODE_DETECT_FACE = 1000;
    private ImageView avatarIv;
    private String facePath;
    private Bitmap mHeadBmp;
    private String Uid;                 //中商超会员ID
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.HideTitleAndNavigation(this);
        setContentView(R.layout.activity_register);
        initUi();
        ShowHeadPic();
        VoiceService.SpeechContext("人脸未注册,请先注册");
    }

    private void initUi() {
        avatarIv = (ImageView) findViewById(R.id.avatar_iv);
    }

    public void ClickRegisterBackMethod(View view){
        SwitchUtil.FinishActivity(UserRegisterActivity.this);
    }
    private void ShowHeadPic(){
        facePath = ImageSaveUtil.loadCameraBitmapPath(this, "head_tmp.jpg");
        if (mHeadBmp != null) {
            mHeadBmp.recycle();
        }
        mHeadBmp = ImageSaveUtil.loadBitmapFromPath(this, facePath);
        if (mHeadBmp != null) {
            avatarIv.setImageBitmap(mHeadBmp);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_DETECT_FACE && resultCode == Activity.RESULT_OK) {
            facePath = ImageSaveUtil.loadCameraBitmapPath(this, "head_tmp.jpg");
            if (mHeadBmp != null) {
                mHeadBmp.recycle();
            }
            mHeadBmp = ImageSaveUtil.loadBitmapFromPath(this, facePath);
            if (mHeadBmp != null) {
                avatarIv.setImageBitmap(mHeadBmp);
            }
        }
        else if(resultCode == RESULT_FIRST_USER){
            String qrcode = data.getStringExtra("QRCODE");
            PopMessageUtil.Log("获取二维码="+qrcode);
            HttpRegisterByQrcode(qrcode);
        }
    }

    /***********************************************************************************************
     * * 功能说明： 获取中商超会员信息
     **********************************************************************************************/
    public void ClickScanChinaQrcodeMethod(View view){
        SwitchUtil.switchActivity(UserRegisterActivity.this, ScanPayDialog.class).switchToForResult(1);
    }

    /***********************************************************************************************
     * * 功能说明：根据用户会员码进行注册
     **********************************************************************************************/
    private void HttpRegisterByQrcode(String qrcode){
        PopMessageUtil.Loading(UserRegisterActivity.this, "会员注册");
        String mkey   = "0afbc3787d0b4ee2b9d56f569550967d";       //key
        long timeStamp = System.currentTimeMillis();               //时间戳    1420042083228
        String face_stream = "";
        try {
            face_stream = Base64.encodeBase64File(facePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String temp_signature = "face_code="+qrcode+"&face_stream="+face_stream+"&merchant=WE9GO"+"&nonce=we9go"+
                "&timestamp="+timeStamp+"&mkey="+mkey;
        PopMessageUtil.Log(temp_signature);
        String secret = SHA256Encrypt.getSHA256StrJava(temp_signature);
        PopMessageUtil.Log(secret);


        HttpxUtils.postHttp(new SendCallBack() {
            @Override
            public void onSuccess(String result) {
                PopMessageUtil.Log("会员验证接口返回：" + result);
                Gson gson = new Gson();
                ChinaRegisterInfoJson registerInfoJson = gson.fromJson(result,ChinaRegisterInfoJson.class);
                if(registerInfoJson.getCode()==200){
                    //中商超会员卡绑定成功
                    VoiceService.PlayVoice(0);
                    Uid = registerInfoJson.getData().getUid();
                    HttpGetChinaUserInfoMethod(Uid);
                }
                else{
                    //{"state":0,"code":301,"msg":"","data":{"uid":"","msg":"The face info is unvalid."}}
                    //已经注册过
                    ShowErrorTipMethod("接口错误", "中商超会员注册接口" + result);
                }
            }

            public void onError(Throwable ex, boolean isOnCallback) {
                ShowErrorTipMethod("服务器错误","中商超会员注册接口"+ex.getMessage());
                ex.printStackTrace();
            }
            public void onCancelled(Callback.CancelledException cex) {}
            public void onFinished() {}
        }).setUrl(Config.ChinaUrl)
                .addBodyParameter("category", "face")
                .addBodyParameter("face_code", qrcode)
                .addBodyParameter("face_stream", face_stream)
                .addBodyParameter("merchant", "WE9GO")
                .addBodyParameter("timestamp", String.valueOf(timeStamp))
                .addBodyParameter("nonce", "we9go")
                .addBodyParameter("signature", secret)
                .send();
    }

    /**************************************************************************************
     * * 功能说明：根据userId 换取用户基本信息
     *************************************************************************************/
    private void HttpGetChinaUserInfoMethod(final String chinaId) {
        PopMessageUtil.Loading(UserRegisterActivity.this, "获取会员信息");
        String category = "userinfo";
        String merchant = "WE9GO";                                 //商户号
        long timeStamp = System.currentTimeMillis();               //时间戳    1420042083228
        String nonce = "WE9GOWE9GO";                               //随机字符串
        String mkey   = "0afbc3787d0b4ee2b9d56f569550967d";       //key

        String temp_signature = "merchant="+merchant+"&nonce="+nonce+
                "&timestamp="+timeStamp+"&uid="+chinaId+"&mkey="+mkey;
        PopMessageUtil.Log(temp_signature);
        String secret = SHA256Encrypt.getSHA256StrJava(temp_signature);
        PopMessageUtil.Log(secret);


        HttpxUtils.postHttp(new SendCallBack() {
            @Override
            public void onSuccess(String result) {
                PopMessageUtil.Log("会员验证接口返回：" + result);
                Gson gson = new Gson();
                UserInfoJson userInfoJson = gson.fromJson(result, UserInfoJson.class);
                if(userInfoJson.getCode().compareTo("200")==0){
                    //识别到用户
                    VoiceService.PlayVoice(0);
                    String uid = userInfoJson.getData().getUid();
                    String nickname = Unicode.decode(userInfoJson.getData().getNickname());
                    String phone = userInfoJson.getData().getPhone();
                    PopMessageUtil.Log(uid + "|" + nickname + "|" + phone+"|"+userInfoJson.getData().getAmount());
                    HttpRegisterAccountMethod(nickname,phone,chinaId);
                }
                else
                    ShowErrorTipMethod("接口错误","获取中商超会员信息"+result);
            }
            public void onError(Throwable ex, boolean isOnCallback) {
                ShowErrorTipMethod("服务器错误", "获取中商超会员信息" + ex.getMessage());
                ex.printStackTrace();
            }
            public void onCancelled(Callback.CancelledException cex) {}
            public void onFinished() {}
        }).setUrl(Config.ChinaUrl)
                .addBodyParameter("category", category)
                .addBodyParameter("uid", chinaId)
                .addBodyParameter("merchant", merchant)
                .addBodyParameter("timestamp", String.valueOf(timeStamp))
                .addBodyParameter("nonce", nonce)
                .addBodyParameter("signature", secret)
                .send();
    }

    //--------------------------------普通用户---------------------------------//
    /***********************************************************************************************
     * * 功能说明： 添加普通用户信息
     **********************************************************************************************/
    public void ClickAddGeneralUserMethod(View view){
        VoiceService.SpeechContext("请输入您的姓名和电话");
        final AlertDialog myDialog = new AlertDialog.Builder(this, R.style.dialog).create();
        Window w = myDialog.getWindow();
        w.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        lp.y = 100;
        myDialog.show();
        myDialog.setCanceledOnTouchOutside(false);
        myDialog.getWindow().setContentView(R.layout.dialog_generaluser);
        myDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        final EditText userName_etxt = (EditText) myDialog.getWindow().findViewById(R.id.dialog_userregister_name_etxt);
        final EditText userTel_etxt  = (EditText) myDialog.getWindow().findViewById(R.id.dialog_userregister_tel_etxt);

        //弹出对话框后直接弹出键盘
        userName_etxt.setFocusableInTouchMode(true);
        userName_etxt.requestFocus();
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputManager = (InputMethodManager) userName_etxt.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(userName_etxt, 0);
            }
        }, 100);
        Button Ok_btn = (Button) myDialog.getWindow().findViewById(R.id.dialog_ok_btn);
        Button Cancel_btn = (Button) myDialog.getWindow().findViewById(R.id.dialog_cancel_btn);

        Ok_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userName_etxt.getText().length()!=0&&userTel_etxt.getText().length()!=0){
                    myDialog.dismiss();
                    HttpRegisterAccountMethod(userName_etxt.getText().toString(),userTel_etxt.getText().toString(),"");
                }
                else{
                    PopMessageUtil.showToastShort("填写信息不对(Filling out information is wrong)");
                }
            }
        });

        Cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
    }

    /***********************************************************************************************
     * * 功能说明： 网络请求用户注册
     **********************************************************************************************/
    private void HttpRegisterAccountMethod(String name, String phone,String chinaUid) {
        PopMessageUtil.Loading(UserRegisterActivity.this, "注册中\nRequest");
        String JsonString = "{ \"nickName\":" + "\""+name+
                "\",\"phone\":" + "\""+phone+
                "\",\"chinaUid\":"+"\""+chinaUid+"\"" + "}";
        PopMessageUtil.Log(JsonString);
        HttpxUtils.postHttp(new SendCallBack() {
            @Override
            public void onSuccess(String result) {
                PopMessageUtil.Log("普通用户注册返回：" + result);
                Gson gson = new Gson();
                ShopUserInfoJson shopRegisterInfoJson = gson.fromJson(result, ShopUserInfoJson.class);
                if (shopRegisterInfoJson.getStatus_code() == 200) {
                    VoiceService.PlayVoice(0);
                    UploadPicFileYun(shopRegisterInfoJson.getUser().getUserId());
                } else
                    ShowErrorTipMethod("接口错误", "普通用户注册接口" + result);
            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                ShowErrorTipMethod("服务器错误", "普通用户注册接口" + ex.getMessage());
                ex.printStackTrace();
            }

            public void onCancelled(Callback.CancelledException cex) {
            }

            public void onFinished() {
            }
        }).setUrl(Config.ShopregisterUrl)
                .addJsonParameter(JsonString)
                .send();
    }

    /**************************************************************************************
     * * 功能说明：根据userId 上传用户头像到后台
     *************************************************************************************/
    private void UploadPicFileYun(final String uid){
        File file = new File(facePath);
        if (!file.exists()) {
            Toast.makeText(UserRegisterActivity.this, "文件不存在", Toast.LENGTH_LONG).show();
            return;
        }
        PopMessageUtil.Loading(UserRegisterActivity.this, "人脸入库中");
        HttpxUtils.postHttp(new SendCallBack() {
            @Override
            public void onSuccess(String result) {
                PopMessageUtil.Log("上传图像本地库接口返回：" + result);
                //{"status_code":200,"info":"ok"}
                if(result.contains("\"status_code\":200")==true){
                    VoiceService.PlayVoice(0);
                    UploadBaiduYun(uid);
                }
                else
                    ShowErrorTipMethod("接口错误","人脸入本地库接口"+result);
            }

            public void onError(Throwable ex, boolean isOnCallback) {
                ShowErrorTipMethod("服务器错误", "人脸入本地库接口" + ex.getMessage());
                ex.printStackTrace();
            }
            public void onCancelled(Callback.CancelledException cex) {}
            public void onFinished() {}
        }).setUrl(Config.ShopAvaterUrl)
                .addBodyParameter("userId", uid)
                .setMultipart()
                .addFileParameter("avatar", file)
                .send();
    }

    /***********************************************************************************************
     * * 功能说明：根据用户uid  上传头像到百度云
     **********************************************************************************************/
    private void UploadBaiduYun(String uid){
        final File file = new File(facePath);
        if (!file.exists()) {
            Toast.makeText(UserRegisterActivity.this, "文件不存在", Toast.LENGTH_LONG).show();
            return;
        }
        // TODO 人脸注册说明 https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add
        // 模拟注册，先提交信息注册获取uid，再使用人脸+uid到百度人脸库注册，
        // TODO 实际使用中，建议注册放到您的服务端进行（这样可以有效防止ak，sk泄露） 把注册信息包括人脸一次性提交到您的服务端，
        // TODO 注册获得uid，然后uid+人脸调用百度人脸注册接口，进行注册。

        // 每个开发者账号只能创建一个人脸库；
        // 每个人脸库下，用户组（group）数量没有限制；
        // 每个用户组（group）下，可添加最多300000张人脸，如每个uid注册一张人脸，则最多300000个用户uid；
        // 每个用户（uid）所能注册的最大人脸数量没有限制；
        // 说明：人脸注册完毕后，生效时间最长为35s，之后便可以进行识别或认证操作。
        // 说明：注册的人脸，建议为用户正面人脸。
        // 说明：uid在库中已经存在时，对此uid重复注册时，新注册的图片默认会追加到该uid下，如果手动选择action_type:replace，
        // 则会用新图替换库中该uid下所有图片。
        // uid          是	string	用户id（由数字、字母、下划线组成），长度限制128B
        // user_info    是	string	用户资料，长度限制256B
        // group_id	    是	string	用户组id，标识一组用户（由数字、字母、下划线组成），长度限制128B。
        // 如果需要将一个uid注册到多个group下，group_id,需要用多个逗号分隔，每个group_id长度限制为48个英文字符
        // image	    是	string	图像base64编码，每次仅支持单张图片，图片编码后大小不超过10M
        // action_type	否	string	参数包含append、replace。如果为“replace”，则每次注册时进行替换replace（新增或更新）操作，
        // 默认为append操作
//        String uid = Md5.MD5(username, "utf-8");
        PopMessageUtil.Loading(UserRegisterActivity.this,"人脸入云中");
        APIService.getInstance().reg(new OnResultListener<RegResult>() {
            @Override
            public void onResult(RegResult result) {
                //{"log_id":3341785924053109}
                VoiceService.PlayVoice(0);
                PopMessageUtil.CloseLoading();
                PopMessageUtil.Log(result.toString());
                PopMessageUtil.showToastShort("注册成功!(Success!)");
                VoiceService.SpeechContext("恭喜您注册成功,请刷脸开门!");
                SwitchUtil.switchActivity(UserRegisterActivity.this, MainActivity.class).switchToAndFinish();
            }

            @Override
            public void onError(FaceError error) {
                ShowErrorTipMethod("服务器错误", "人脸云库接口" + error.getMessage());
                VoiceService.SpeechContext("注册失败,请重新操作!");
            }
        }, file, uid, uid);
    }

    private void ShowErrorTipMethod(String title,String info){
        PopMessageUtil.CloseLoading();
        VoiceService.PlayVoice(1);
        PopWindowMessage.PopWinMessage(UserRegisterActivity.this, title,info, "error");
    }
}
