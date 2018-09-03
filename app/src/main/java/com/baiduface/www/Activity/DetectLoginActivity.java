/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baiduface.www.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.aip.face.CameraImageSource;
import com.baidu.aip.face.DetectRegionProcessor;
import com.baidu.aip.face.FaceDetectManager;
import com.baidu.aip.face.FaceFilter;
import com.baidu.aip.face.ImageFrame;
import com.baidu.aip.face.PreviewView;
import com.baidu.aip.face.camera.ICameraControl;
import com.baidu.aip.face.camera.PermissionCallback;
import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.facesdk.FaceInfo;
import com.baiduface.www.Application.APIService;
import com.baiduface.www.Application.BaseApplication;
import com.baiduface.www.R;
import com.baiduface.www.Utils.Https.OnResultListener;
import com.baiduface.www.Utils.Images.ImageSaveUtil;
import com.baiduface.www.Utils.Images.ImageUtil;
import com.baiduface.www.Utils.Model.FaceError;
import com.baiduface.www.Utils.Model.FaceModel;
import com.baiduface.www.Utils.PopMessage.PopMessageUtil;
import com.baiduface.www.Utils.SwitchUtil;
import com.baiduface.www.Utils.VoiceService.VoiceService;
import com.baiduface.www.Utils.Widget.BrightnessTools;
import com.baiduface.www.Utils.Widget.FaceRoundView;
import com.baiduface.www.Utils.Widget.WaveHelper;
import com.baiduface.www.Utils.Widget.WaveView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * 实时检测调用identify进行人脸识别，MainActivity未给出改示例的入口，开发者可以在MainActivity调用
 * Intent intent = new Intent(MainActivity.this, DetectLoginActivity.class);
 * startActivity(intent);
 *
 * 人脸识别
 */
public class DetectLoginActivity extends Activity {
    private final static int MSG_INITVIEW = 1001;
    private final static int MSG_DETECTTIME = 1002;
    private final static int MSG_INITWAVE = 1003;
    private TextView nameTextView;
    private PreviewView previewView;
    private View mInitView;
    //  private TextureView textureView;
    private FaceRoundView rectView;
    private boolean mGoodDetect = false;
    private static final double ANGLE = 15;
    private ImageView closeIv;
    private boolean mDetectStoped = false;
    private ImageView mSuccessView;
    private Handler mHandler;
    private String mCurTips;
    private boolean mDetectTime = true;
    private boolean mUploading = false;
    private long mLastTipsTime = 0;
    private int mDetectCount = 0;
    private int mCurFaceId = -1;

    private FaceDetectManager faceDetectManager;
    private DetectRegionProcessor cropProcessor = new DetectRegionProcessor();
    private WaveHelper mWaveHelper;
    private WaveView mWaveview;
    private int mBorderColor = Color.parseColor("#28FFFFFF");
    private int mBorderWidth = 0;
    private int mScreenW;
    private int mScreenH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseApplication.HideTitleAndNavigation(this);
        setContentView(R.layout.activity_login_detected);
        faceDetectManager = new FaceDetectManager(this);
        initScreen();
        initView();
        mHandler = new InnerHandler(this);
        mHandler.sendEmptyMessageDelayed(MSG_INITVIEW, 500);
    }

    private void initScreen() {
        WindowManager manager = getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        mScreenW = outMetrics.widthPixels;
        mScreenH = outMetrics.heightPixels;
    }

    private void initView() {
        mInitView = findViewById(R.id.camera_layout);
        previewView = (PreviewView) findViewById(R.id.preview_view);

        rectView = (FaceRoundView) findViewById(R.id.rect_view);
        final CameraImageSource cameraImageSource = new CameraImageSource(this);
        cameraImageSource.setPreviewView(previewView);

        faceDetectManager.setImageSource(cameraImageSource);
        faceDetectManager.setOnFaceDetectListener(new FaceDetectManager.OnFaceDetectListener() {
            @Override
            public void onDetectFace(final int retCode, FaceInfo[] infos, ImageFrame frame) {
                if (mUploading)
                    return;
                String str = "";
                if (retCode == 0) {
                    if (infos != null && infos[0] != null) {
                        FaceInfo info = infos[0];
                        boolean distance = false;
                        if (info != null && frame != null) {
                            if (info.mWidth >= (0.9 * frame.getWidth())) {
                                distance = false;
                                str = getResources().getString(R.string.detect_zoom_out);
                            } else if (info.mWidth <= 0.4 * frame.getWidth()) {
                                distance = false;
                                str = getResources().getString(R.string.detect_zoom_in);
                            } else {
                                distance = true;
                            }
                        }
                        boolean headUpDown;
                        if (info != null) {
                            if (info.headPose[0] >= ANGLE) {
                                headUpDown = false;
                                str = getResources().getString(R.string.detect_head_up);
                            } else if (info.headPose[0] <= -ANGLE) {
                                headUpDown = false;
                                str = getResources().getString(R.string.detect_head_down);
                            } else {
                                headUpDown = true;
                            }

                            boolean headLeftRight;
                            if (info.headPose[1] >= ANGLE) {
                                headLeftRight = false;
                                str = getResources().getString(R.string.detect_head_left);
                            } else if (info.headPose[1] <= -ANGLE) {
                                headLeftRight = false;
                                str = getResources().getString(R.string.detect_head_right);
                            } else {
                                headLeftRight = true;
                            }

                            if (distance && headUpDown && headLeftRight) {
                                mGoodDetect = true;
                            } else {
                                mGoodDetect = false;
                            }

                        }
                    }
                } else if (retCode == 1) {
                    str = getResources().getString(R.string.detect_head_up);
                } else if (retCode == 2) {
                    str = getResources().getString(R.string.detect_head_down);
                } else if (retCode == 3) {
                    str = getResources().getString(R.string.detect_head_left);
                } else if (retCode == 4) {
                    str = getResources().getString(R.string.detect_head_right);
                } else if (retCode == 5) {
                    str = getResources().getString(R.string.detect_low_light);
                } else if (retCode == 6) {
                    str = getResources().getString(R.string.detect_face_in);
                } else if (retCode == 7) {
                    str = getResources().getString(R.string.detect_face_in);
                } else if (retCode == 10) {
                    str = getResources().getString(R.string.detect_keep);
                } else if (retCode == 11) {
                    str = getResources().getString(R.string.detect_occ_right_eye);
                } else if (retCode == 12) {
                    str = getResources().getString(R.string.detect_occ_left_eye);
                } else if (retCode == 13) {
                    str = getResources().getString(R.string.detect_occ_nose);
                } else if (retCode == 14) {
                    str = getResources().getString(R.string.detect_occ_mouth);
                } else if (retCode == 15) {
                    str = getResources().getString(R.string.detect_right_contour);
                } else if (retCode == 16) {
                    str = getResources().getString(R.string.detect_left_contour);
                } else if (retCode == 17) {
                    str = getResources().getString(R.string.detect_chin_contour);
                }

                boolean faceChanged = true;
                if (infos != null && infos[0] != null) {
                    Log.d("DetectLogin", "face id is:" + infos[0].face_id);
                    if (infos[0].face_id == mCurFaceId) {
                        faceChanged = false;
                    } else {
                        faceChanged = true;
                    }
                    mCurFaceId = infos[0].face_id;
                }

                if (faceChanged) {
                    showProgressBar(false);
                    onRefreshSuccessView(false);
                }

                final int resultCode = retCode;
                if (!(mGoodDetect && retCode == 0)) {
                    if (faceChanged) {
                        showProgressBar(false);
                        onRefreshSuccessView(false);
                    }
                }

                if (retCode == 6 || retCode == 7 || retCode < 0) {
                    rectView.processDrawState(true);
                } else {
                    rectView.processDrawState(false);
                }

                mCurTips = str;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if ((System.currentTimeMillis() - mLastTipsTime) > 3000) {
                            nameTextView.setText(mCurTips);
                            VoiceService.SpeechContext(mCurTips);
                            mLastTipsTime = System.currentTimeMillis();
                        }
                        if (mGoodDetect && resultCode == 0) {
                            nameTextView.setText("");
                            onRefreshSuccessView(true);
                            showProgressBar(true);
                        }
                    }
                });

                if (infos == null) {
                    mGoodDetect = false;
                }


            }
        });
        faceDetectManager.setOnTrackListener(new FaceFilter.OnTrackListener() {
            @Override
            public void onTrack(FaceFilter.TrackedModel trackedModel) {
                if (trackedModel.meetCriteria() && mGoodDetect) {
                    upload(trackedModel);
                    mGoodDetect = false;
                }
            }
        });

        cameraImageSource.getCameraControl().setPermissionCallback(new PermissionCallback() {
            @Override
            public boolean onRequestPermission() {
                ActivityCompat.requestPermissions(DetectLoginActivity.this,
                        new String[]{Manifest.permission.CAMERA}, 100);
                return true;
            }
        });

        rectView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                start();
                rectView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        ICameraControl control = cameraImageSource.getCameraControl();
        control.setPreviewView(previewView);
        // 设置检测裁剪处理器
        faceDetectManager.addPreProcessor(cropProcessor);

        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);

        if (isPortrait) {
            previewView.setScaleType(PreviewView.ScaleType.FIT_WIDTH);
        } else {
            previewView.setScaleType(PreviewView.ScaleType.FIT_HEIGHT);
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        cameraImageSource.getCameraControl().setDisplayOrientation(rotation);

        previewView.getTextureView().setScaleX(-1);
        nameTextView = (TextView) findViewById(R.id.name_text_view);
        closeIv = (ImageView) findViewById(R.id.closeIv);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mSuccessView = (ImageView) findViewById(R.id.success_image);

        mSuccessView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mSuccessView.getTag() == null) {
                    Rect rect = rectView.getFaceRoundRect();
                    RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) mSuccessView.getLayoutParams();
                    int w = 40;
                    rlp.setMargins(rect.centerX() - (w / 2),rect.top - (w / 2), 0, 0);
                    mSuccessView.setLayoutParams(rlp);
                    mSuccessView.setTag("setlayout");
                }
                mSuccessView.setVisibility(View.GONE);

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
                    mSuccessView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                else
                    mSuccessView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
        // mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        init();
    }

    private void initWaveview(Rect rect) {
        RelativeLayout rootView = (RelativeLayout) findViewById(R.id.root_view);

        RelativeLayout.LayoutParams waveParams = new RelativeLayout.LayoutParams(
                rect.width(), rect.height());

        waveParams.setMargins(rect.left, rect.top, rect.left, rect.top);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        waveParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        mWaveview = new WaveView(this);
        rootView.addView(mWaveview, waveParams);

        // mWaveview = (WaveView) findViewById(R.id.wave);
        mWaveHelper = new WaveHelper(mWaveview);

        mWaveview.setShapeType(WaveView.ShapeType.CIRCLE);
        mWaveview.setWaveColor(
                Color.parseColor("#28FFFFFF"),
                Color.parseColor("#3cFFFFFF"));

        mBorderColor = Color.parseColor("#28f16d7a");
        mWaveview.setBorder(mBorderWidth, mBorderColor);
    }

    private void visibleView() {
        mInitView.setVisibility(View.INVISIBLE);
    }

    private void initBrightness() {
        int brightness = BrightnessTools.getScreenBrightness(DetectLoginActivity.this);
        if (brightness < 200) {
            BrightnessTools.setBrightness(this, 200);
        }
    }


    private void init() {
        FaceSDKManager.getInstance().getFaceConfig().setCheckFaceQuality(true);
        // 该角度为上下，左右，偏头的角度的阀值，大于将无法检测出人脸，
        FaceSDKManager.getInstance().getFaceConfig().setHeadYawValue(45);
        FaceSDKManager.getInstance().getFaceConfig().setHeadRollValue(45);
        FaceSDKManager.getInstance().getFaceConfig().setHeadPitchValue(45);
        FaceSDKManager.getInstance().getFaceConfig().setVerifyLive(false);

        initBrightness();
    }

    private void start() {
        Rect dRect = rectView.getFaceRoundRect();
        //   RectF newDetectedRect = new RectF(detectedRect);
        int preGap = getResources().getDimensionPixelOffset(R.dimen.preview_margin);
        int w = getResources().getDimensionPixelOffset(R.dimen.detect_out);

        int orientation = getResources().getConfiguration().orientation;
        boolean isPortrait = (orientation == Configuration.ORIENTATION_PORTRAIT);
        if (isPortrait) {
            // 检测区域矩形宽度
            int rWidth = mScreenW - 2 * preGap;
            // 圆框宽度
            int dRectW = dRect.width();
            // 检测矩形和圆框偏移
            int h = (rWidth - dRectW) / 2;
            int rLeft = w;
            int rRight = rWidth - w;
            int rTop = dRect.top - h - preGap + w;
            int rBottom = rTop + rWidth - w;

            RectF newDetectedRect = new RectF(rLeft, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        } else {
            int rLeft = mScreenW / 2 - mScreenH / 2 + w;
            int rRight = mScreenW / 2 + mScreenH / 2 + w;
            int rTop = 0;
            int rBottom = mScreenH;

            RectF newDetectedRect = new RectF(rLeft, rTop, rRight, rBottom);
            cropProcessor.setDetectedRect(newDetectedRect);
        }

        faceDetectManager.start();
        initWaveview(dRect);
    }

    @Override
    protected void onStop() {
        super.onStop();
        faceDetectManager.stop();
        mDetectStoped = true;
        onRefreshSuccessView(false);
        if (mWaveview != null) {
            mWaveview.setVisibility(View.GONE);
            mWaveHelper.cancel();
        }
    }
    /**
     * 参考https://ai.baidu.com/docs#/Face-API/top 人脸识别接口
     * 无需知道uid，如果同一个人多次注册，可能返回任意一个帐号的uid
     * 建议上传人脸到自己的服务器，在服务器端调用https://aip.baidubce.com/rest/2.0/face/v2/identify，比对分数阀值（如：80分），
     * 认为登录通过
     * group_id	是	string	用户组id（由数字、字母、下划线组成），长度限制128B，如果需要查询多个用户组id，用逗号分隔
     * image	是	string	图像base64编码，每次仅支持单张图片，图片编码后大小不超过10M
     * ext_fields	否	string	特殊返回信息，多个用逗号分隔，取值固定: 目前支持 faceliveness(活体检测)，活体检测参考分数0.834963
     * 返回登录认证的参数给客户端
     *
     * @param model
     */
    private boolean SwitchRegister = false;
    private void upload(FaceFilter.TrackedModel model) {
        if (mUploading) {
            Log.d("liujinhui", "is uploading");
            return;
        }
        mUploading = true;
        if (model.getEvent() != FaceFilter.Event.OnLeave) {
            mDetectCount++;
            try {
                final Bitmap face = model.cropFace();
                final File file = File.createTempFile(UUID.randomUUID().toString() + "", ".jpg");
                ImageUtil.resize(face, file, 200, 200);
                ImageSaveUtil.saveCameraBitmap(DetectLoginActivity.this, face, "head_tmp.jpg");
                if(SwitchRegister == false) {
                    APIService.getInstance().identify(new OnResultListener<FaceModel>() {
                        @Override
                        public void onResult(FaceModel result) {
                            deleteFace(file);
                            mUploading = false;
                            if (result == null) {
                                if (mDetectCount >= 3) {
                                    SwitchRegister = true;
                                    VoiceService.PlayVoice(1);
                                    PopMessageUtil.showToastShort("人脸校验不通过,请先注册");
                                    SwitchUtil.switchActivity(DetectLoginActivity.this, UserRegisterActivity.class).switchToAndFinish();
                                }
                                return;
                            } else {
                                if (result.getScore() > 80 && result.getFaceliveness() > 0.834963) {
                                    SwitchRegister = true;
                                    Log.d("DetectLoginActivity", "onResult ok");
                                    PopMessageUtil.Log("UID=" + result.getUid() + "|" + result.getUserInfo());
                                    mDetectTime = false;
                                    Intent intent = new Intent(DetectLoginActivity.this, LoginResultActivity.class);
                                    intent.putExtra("login_success", true);
                                    intent.putExtra("user_info", result.getUserInfo());
                                    intent.putExtra("uid", result.getUid());
                                    intent.putExtra("score", result.getScore());
                                    startActivity(intent);
                                    finish();
                                    return;
                                } else {
                                    PopMessageUtil.Log("活体过低或者匹配分低");
                                    if (mDetectCount >= 3) {
                                        mDetectTime = false;
                                        SwitchRegister = true;
                                        VoiceService.SpeechContext("人脸未注册,请先注册");
                                        PopMessageUtil.showToastShort("人脸校验不通过,请先注册");
                                        SwitchUtil.switchActivity(DetectLoginActivity.this, UserRegisterActivity.class).switchToAndFinish();
                                        return;
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(FaceError error) {
                            error.printStackTrace();
                            deleteFace(file);

                            mUploading = false;
                            if (error.getErrorCode() == 216611) {
                                mDetectTime = false;
                                Intent intent = new Intent();
                                intent.putExtra("login_success", false);
                                intent.putExtra("error_code", error.getErrorCode());
                                intent.putExtra("error_msg", error.getErrorMessage());
                                setResult(Activity.RESULT_OK, intent);
                                finish();
                                return;
                            }

                            if (mDetectCount >= 3) {
                                mDetectTime = false;
                                SwitchRegister = true;
                                if (error.getErrorCode() == 10000) {
                                    VoiceService.SpeechContext("网络超时，请检查网络");
                                    Toast.makeText(DetectLoginActivity.this, "人脸校验不通过,请检查网络后重试", Toast.LENGTH_SHORT).show();
                                } else {
                                    VoiceService.SpeechContext("身份无法识别");
                                    Toast.makeText(DetectLoginActivity.this, "人脸校验不通过", Toast.LENGTH_SHORT).show();
                                }
                                finish();
                            }
                            return;
                        }
                    }, file);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        } else {
            onRefreshSuccessView(false);
            showProgressBar(false);
            mUploading = false;
        }
    }

    private void showProgressBar(final boolean show) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (show) {
                    if (mWaveview != null) {
                        mWaveview.setVisibility(View.VISIBLE);
                        mWaveHelper.start();
                    }
                } else {
                    if (mWaveview != null) {
                        mWaveview.setVisibility(View.GONE);
                        mWaveHelper.cancel();
                    }
                }

            }
        });
    }

    private void deleteFace(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mWaveview != null) {
            mWaveHelper.cancel();
            mWaveview.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDetectTime = true;
        if (mDetectStoped) {
            faceDetectManager.start();
            mDetectStoped = false;
        }

    }

    private void onRefreshSuccessView(final boolean isShow) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSuccessView.setVisibility(isShow ? View.VISIBLE : View.INVISIBLE);
            }
        });
    }

    private static class InnerHandler extends Handler {
        private WeakReference<DetectLoginActivity> mWeakReference;

        public InnerHandler(DetectLoginActivity activity) {
            super();
            this.mWeakReference = new WeakReference<DetectLoginActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            if (mWeakReference == null || mWeakReference.get() == null) {
                return;
            }
            DetectLoginActivity activity = mWeakReference.get();
            if (activity == null) {
                return;
            }
            if (msg == null) {
                return;

            }
            switch (msg.what) {
                case MSG_INITVIEW:
                    activity.visibleView();
                    break;
                case MSG_DETECTTIME:
                    activity.mDetectTime = true;
                    break;
                default:
                    break;
            }
        }
    }
}
