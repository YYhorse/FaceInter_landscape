package com.baiduface.www.Application;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.baidu.idl.face.platform.FaceConfig;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.LivenessTypeEnum;
import com.baiduface.www.Utils.Https.OnResultListener;
import com.baiduface.www.Utils.Model.AccessToken;
import com.baiduface.www.Utils.Model.FaceError;
import com.baiduface.www.Utils.VoiceService.VoiceService;

import org.xutils.x;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yy on 2018/5/2.
 */
public class BaseApplication extends Application {
    public static int CamerDirection = 1;       //1 前置摄像头   0后置摄像头

    public static BaseApplication instance;
    private Handler handler = new Handler(Looper.getMainLooper());
    public static final float VALUE_BRIGHTNESS = 40.0F;
    public static final float VALUE_BLURNESS = 0.7F;
    public static final float VALUE_OCCLUSION = 0.6F;
    public static final int VALUE_HEAD_PITCH = 15;
    public static final int VALUE_HEAD_YAW = 15;
    public static final int VALUE_HEAD_ROLL = 15;
    public static final int VALUE_CROP_FACE_SIZE = 400;
    public static final int VALUE_MIN_FACE_SIZE = 120;
    public static final float VALUE_NOT_FACE_THRESHOLD = 0.6F;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //===========语音加载==============//
        VoiceService.SystemAudioSet();                                                              //系统声音最大声
        VoiceService.SpeechInit();
        //===============网络请求==================//
        x.Ext.init(this);
        //===========人脸识别==============//
        FaceSDKManager.getInstance().initialize(this, Config.licenseID, Config.licenseFileName);
        FaceConfig config = FaceSDKManager.getInstance().getFaceConfig();
        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
        // 设置活体动作，通过设置list LivenessTypeEnum.Eye，LivenessTypeEnum.Mouth，LivenessTypeEnum.HeadUp，
        // LivenessTypeEnum.HeadDown，LivenessTypeEnum.HeadLeft, LivenessTypeEnum.HeadRight,
        // LivenessTypeEnum.HeadLeftOrRight
        List<LivenessTypeEnum> livenessList = new ArrayList<>();
        livenessList.add(LivenessTypeEnum.Mouth);
        livenessList.add(LivenessTypeEnum.Eye);
        livenessList.add(LivenessTypeEnum.HeadUp);
        livenessList.add(LivenessTypeEnum.HeadDown);
        livenessList.add(LivenessTypeEnum.HeadLeft);
        livenessList.add(LivenessTypeEnum.HeadRight);
        config.setLivenessTypeList(livenessList);
        // 设置 活体动作是否随机 boolean
        config.setLivenessRandom(true);
        config.setLivenessRandomCount(2);
        // 模糊度范围 (0-1) 推荐小于0.7
        config.setBlurnessValue(VALUE_BLURNESS);
        // 光照范围 (0-1) 推荐大于40
        config.setBrightnessValue(VALUE_BRIGHTNESS);
        // 裁剪人脸大小
        config.setCropFaceValue(VALUE_CROP_FACE_SIZE);
        // 人脸yaw,pitch,row 角度，范围（-45，45），推荐-15-15
        config.setHeadPitchValue(VALUE_HEAD_PITCH);
        config.setHeadRollValue(VALUE_HEAD_ROLL);
        config.setHeadYawValue(VALUE_HEAD_YAW);
        // 最小检测人脸（在图片人脸能够被检测到最小值）80-200， 越小越耗性能，推荐120-200
        config.setMinFaceSize(VALUE_MIN_FACE_SIZE);
        // 人脸置信度（0-1）推荐大于0.6
        config.setNotFaceValue(VALUE_NOT_FACE_THRESHOLD);
        // 人脸遮挡范围 （0-1） 推荐小于0.5
        config.setOcclusionValue(VALUE_OCCLUSION);
        // 是否进行质量检测
        config.setCheckFaceQuality(true);
        // 人脸检测使用线程数
        config.setFaceDecodeNumberOfThreads(2);
        // 是否开启提示音
        config.setSound(true);

        FaceSDKManager.getInstance().setFaceConfig(config);

        APIService.getInstance().init(this);
        APIService.getInstance().setGroupId(Config.groupID);
        // 用ak，sk获取token, 调用在线api，如：注册、识别等。为了ak、sk安全，建议放您的服务器，
        APIService.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                Log.i("wtf", "AccessToken->" + result.getAccessToken());
            }

            @Override
            public void onError(FaceError error) {
                Log.e("xx", "AccessTokenError:" + error);
                error.printStackTrace();
            }
        }, this, Config.apiKey, Config.secretKey);
    }
    public static BaseApplication getInstance(){
        return instance;
    }
    /**********************************************************************************************
     * * 函数名称: void HideTitleAndNavigation(Activity mActivity)
     * * 功能说明：隐藏标题和导航栏
     **********************************************************************************************/
    public static void HideTitleAndNavigation(Activity mActivity){
        //-------------标题隐藏------------------//
        mActivity.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //-------------禁止软键盘----------------//
//        mActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //-------------隐藏导航栏----------------//
        WindowManager.LayoutParams params =  mActivity.getWindow().getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE;
        mActivity.getWindow().setAttributes(params);
    }
}
