/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baiduface.www.Utils.Model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
public class RegParams implements RequestParams {

    private Map<String, String> params = new HashMap<>();
    private Map<String, File> fileMap = new HashMap<>();


    @Override
    public Map<String, File> getFileParams() {
        return fileMap;
    }

    @Override
    public Map<String, String> getStringParams() {
        return params;
    }

    private String uid;
    private String groupId;

    private String userInfo;


    public void setUid(String uid) {
        putParam("uid", uid);
    }

    public void setGroupId(String groupId) {

        putParam("group_id", groupId);
    }

    public void setBase64Img(String base64Img) {
        putParam("image", base64Img);
    }

    public void setUserInfo(String userInfo) {
        putParam("user_info", userInfo);
    }

    public void setToken(String token) {
        putParam("access_token", token);
    }

    public void setImageFile(File imageFile) {
        fileMap.put(imageFile.getName(), imageFile);
    }

    //----------中商超------------//
    public void setCategory(String category) { putParam("category", category);}
    public void setFacecode(String facecode) { putParam("face_code",facecode);}
    public void setFacestream(String facestream) { putParam("face_stream",facestream);}
    public void setFaceid(String faceid) { putParam("face_id",faceid);}
    public void setMerchant(String merchant) { putParam("merchant",merchant);}
    public void setTimestamp(String timestamp) { putParam("timestamp",timestamp);}
    public void setNonce(String nonce) { putParam("nonce",nonce);}
    public void setSignature(String signature){ putParam("signature",signature);}
    //----------本地上传-----------//
    public void setUserId(String uid) { putParam("userId", uid);  }
    public void setAvatarFile(File imageFile) {
        fileMap.put(imageFile.getName(), imageFile);
    }

    //-----------组件-------------//
    private void putParam(String key, String value) {
        if (value != null) {
            params.put(key, value);
        } else {
            params.remove(key);
        }
    }

    private void putParam(String key, boolean value) {
        if (value) {
            putParam(key, "true");
        } else {
            putParam(key, "false");
        }
    }
}
