/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baiduface.www.Utils.Model;

/**
 * JSON解析
 * @param <T>
 */
public interface Parser<T> {
    T parse(String json) throws FaceError;
}
