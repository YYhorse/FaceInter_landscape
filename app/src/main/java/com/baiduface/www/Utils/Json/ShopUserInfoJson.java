package com.baiduface.www.Utils.Json;

/**
 * Created by yy on 2018/6/6.
 */
/*
 {
  "status_code": 0,
  "info": "string",
  "user": {
    "userId": "string",
    "nickname": "string",
    "sex": "string",
    "integration": 0,
    "country": "string",
    "province": "string",
    "avatarUrl": "string",
    "weixin": "string",
    "city": "string",
    "language": "string",
    "phone": "string",
    "chinaUid": "string"
  }
}
 */
public class ShopUserInfoJson {
    private int status_code;
    private String info;
    private User user;

    public int getStatus_code() {return this.status_code;}
    public String getInfo() {return this.info;}
    public User getUser() {return this.user;}

    public class User{
        private String userId,nickname,sex,country,province,avatarUrl,weixin,city,language,phone,chinaUid;
        private int integration;
        public String getUserId() {return this.userId;}
        public String getNickname() {return this.nickname;}
        public String getSex() {return this.sex;}
        public String getCountry() {return this.country;}
        public String getProvince() {return this.province;}
        public String getAvatarUrl() {return this.avatarUrl;}
        public String getWeixin() {return this.weixin;}
        public String getCity() {return this.city;}
        public String getLanguage() {return this.language;}
        public String getPhone() {return this.phone;}
        public String getChinaUid() {return this.chinaUid;}
        public int getIntegration() {return this.integration;}
    }
}
