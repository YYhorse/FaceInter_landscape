package com.baiduface.www.Activity;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.baiduface.www.R;
import com.baiduface.www.Utils.SwitchUtil;
import com.baiduface.www.Utils.VoiceService.VoiceService;
import com.baiduface.www.Zxing.CameraManager;
import com.baiduface.www.Zxing.CaptureActivityHandler;
import com.baiduface.www.Zxing.InactivityTimer;
import com.baiduface.www.Zxing.ViewfinderView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.io.IOException;
import java.util.Vector;

/**
 * Created by yy on 2018/2/2.
 *
 *
 */
public class ScanPayDialog extends Activity implements SurfaceHolder.Callback {
    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private SurfaceView surfaceView;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private CameraManager cameraManager;
    private InactivityTimer inactivityTimer;
    private String characterSet;
    public static QRCallBack callBack = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scanpay);
        initUi();
    }

    private void initUi() {
        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinderview);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    public void ClickScanPayBackMethod(View view) { SwitchUtil.FinishActivity(ScanPayDialog.this);  }

    @Override
    protected void onResume() {
        super.onResume();
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        cameraManager = new CameraManager(getApplication());

        viewfinderView.setCameraManager(cameraManager);

        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        decodeFormats = null;
        characterSet = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        cameraManager.closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            cameraManager.openDriver(surfaceHolder);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void handleDecode(Result obj, Bitmap barcode) {
        inactivityTimer.onActivity();
        VoiceService.PlayVoice(0);
        showResult(obj, barcode);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SwitchUtil.FinishActivity(this);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public interface QRCallBack {   void callBack(String result);   }

    private void showResult(final Result rawResult, Bitmap barcode) {
        switch (0) {
            case 0:
                SwitchUtil.switchActivity(ScanPayDialog.this,UserRegisterActivity.class)
                        .addString("QRCODE",rawResult.getText().toString())
                        .switchToFinishWithValue(RESULT_FIRST_USER);
                break;
            case 1:
                if (callBack != null)
                    callBack.callBack(rawResult.getText());
                callBack = null;
                finish();
                break;
        }
    }
}
