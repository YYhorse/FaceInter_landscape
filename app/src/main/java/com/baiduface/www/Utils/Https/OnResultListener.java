/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baiduface.www.Utils.Https;


import com.baiduface.www.Utils.Model.FaceError;

public interface OnResultListener<T> {
    void onResult(T result);

    void onError(FaceError error);
}
