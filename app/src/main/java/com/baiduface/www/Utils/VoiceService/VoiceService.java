package com.baiduface.www.Utils.VoiceService;

import android.app.Service;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.baiduface.www.Application.BaseApplication;
import com.baiduface.www.R;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

import java.util.HashMap;

/**
 * 静态全局控制类
 * Created by yy on 2017/8/24.
 * 人工语音服务  +  音效控制
 * 任何地方使用方法
 */
public class VoiceService {
    //******************音效***************************//
    public static AudioManager am;
    public static SoundPool soundPool;
    public static HashMap musicId;                                                                         //定义一个HashMap用于存放音频流的ID
    public static SpeechSynthesizer mTts;                                                                  //人工语音

    private static Handler UiHandler = new Handler(Looper.getMainLooper());

    /***********************************************************************************************
     * * 函数名称: void SystemAudioSet()
     * * 功能说明：音效加载
     * * 最大音量15
     **********************************************************************************************/
    public static void SystemAudioSet() {
        SetAudioVoice();
        soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 5);
        musicId = new HashMap();
        musicId.put(0, soundPool.load(BaseApplication.getInstance(), R.raw.success, 1));
        musicId.put(1, soundPool.load(BaseApplication.getInstance(), R.raw.fail, 1));
    }

    /***********************************************************************************************
     * * 函数名称: void SetAudioVoice()
     * * 功能说明：设置音频声音大小
     * * 最大音量15
     **********************************************************************************************/
    public static void SetAudioVoice() {
        am = (AudioManager) BaseApplication.getInstance().getSystemService(Service.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
    }


    /***********************************************************************************************
     * * 函数名称:  void PlayVoice(final int id)
     * * 功能说明： 播放特效声音
     **********************************************************************************************/
    public static void PlayVoice(final int id) {
        UiHandler.post(new Runnable() {
            @Override
            public void run() {
                soundPool.play((Integer) musicId.get(id), 1, 1, 0, 0, 1);
            }
        });
    }

    /***********************************************************************************************
     * * 函数名称: void SpeechInit()
     * * 功能说明：语音初始化
     **********************************************************************************************/
    public static void SpeechInit() {
        SpeechUtility.createUtility(BaseApplication.getInstance(), SpeechConstant.APPID + "=589acbd7");         //将“12345678”替换成您申请的APPID，申请地址：http://open.voicecloud.cn
        mTts = SpeechSynthesizer.createSynthesizer(BaseApplication.getInstance(), null);
        mTts.setParameter(SpeechConstant.VOICE_NAME, "xiaoqi");//vixl   vixy
        mTts.setParameter(SpeechConstant.SPEED, "60");                                              //设置语速
        mTts.setParameter(SpeechConstant.VOLUME, "100");                                            //设置音量，范围0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);                   //设置云端
        //设置合成音频保存位置（可自定义保存位置），保存在“./sdcard/iflytek.pcm”
        //如果不需要保存合成音频，注释该行代码
        // mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, "./sdcard/iflytek.pcm");
    }

    /***********************************************************************************************
     * * 函数名称: SpeechContext(final String context)
     * * 功能说明：自定义语音播报
     **********************************************************************************************/
    public static void SpeechContext(final String ChinaContext) {
            UiHandler.post(new Runnable() {
                @Override
                public void run() {
                        mTts.startSpeaking(ChinaContext, mTtsListener);
                }
            });
    }

    /**********************************************************************************************
     * * 函数名称: SynthesizerListener mTtsListener
     * * 功能说明：合成回调接听
     **********************************************************************************************/
    public static SynthesizerListener mTtsListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            //开始播放
        }

        @Override
        public void onSpeakPaused() {
            //暂停播放
        }

        @Override
        public void onSpeakResumed() {
            //继续播放
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
            // 合成进度
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度

        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                //播放完成
            } else if (error != null) {
//                Toast.makeText(mContext, error.getPlainDescription(true), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            //	if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            //		String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
            //		Log.d(TAG, "session id =" + sid);
            //	}
        }
    };
}
