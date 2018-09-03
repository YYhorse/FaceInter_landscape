/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.baiduface.www.Application;


public class Config {
    // 为了apiKey,secretKey为您调用百度人脸在线接口的，如注册，识别等。
    // 为了的安全，建议放在您的服务端，端把人脸传给服务器，在服务端端进行人脸注册、识别放在示例里面是为了您快速看到效果
    public static String apiKey = "tEpi0HmnNf3Ba7Rw4DVEGyHt";
    public static String secretKey = "PWQZNmIgxUquIiwuWH73DEvvGIk4Guyb";
    public static String licenseID = "MealAddDemo-face-android";
    public static String licenseFileName = "idl-license.face-android";

    /**
     * groupId，标识一组用户（由数字、字母、下划线组成），长度限制128B，可以自行定义，只要注册和识别都是同一个组。
     * 详情见 http://ai.baidu.com/docs#/Face-API/top
     * 人脸识别 接口 https://aip.baidubce.com/rest/2.0/face/v2/identify
     * 人脸注册 接口 https://aip.baidubce.com/rest/2.0/face/v2/faceset/user/add
     */
    public static String ShopUrl = "https://www.matrixsci.cn/supermarket";
    public static String ShopregisterUrl = ShopUrl + "/user/add?";              //根据RFCODE查询商品
    public static String ShopAvaterUrl   = ShopUrl + "/user/upload?";           //上传头像
    public static String ShopGetUserUrl  = ShopUrl + "/user/get?";              //根据userId获取用户信息
    public static String ShopOpenDoorUrl = ShopUrl + "/store/open?";            //开门请求

    public static String groupID = "supermarket";                //supermarket     supermarket_test
    //----------中商超URL-----------//
    public static String ChinaUrl = "http://47.88.159.243:9099/myphp/web/thirdapicall.php";
//    public static String ChinaUrl = "http://www.zgspcjsc.com/thirdapicall.php";
}
