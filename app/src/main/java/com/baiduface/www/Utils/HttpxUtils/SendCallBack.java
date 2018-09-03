package com.baiduface.www.Utils.HttpxUtils;

import org.xutils.common.Callback;

/**
 * Created by yy on 2017/9/27.
 */
public interface SendCallBack {
    public void onSuccess(String result);
    public void onError(Throwable ex, boolean isOnCallback);
    public void onCancelled(Callback.CancelledException cex);
    public void onFinished();
}
