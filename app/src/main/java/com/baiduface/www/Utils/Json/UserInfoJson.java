package com.baiduface.www.Utils.Json;

/**
 * Created by yy on 2018/5/31.
 */
public class UserInfoJson {
    //{"state":1,"code":"200","data":{"uid":"1613","nickname":"\u65b0\u7528\u623713651810545","amount":"94350.31"}}
    private int state;
    private String code;
    private Data data;

    public int getState() {return this.state;}
    public String getCode() {return this.code;}
    public Data getData() {return this.data;}

    public class Data{
        private String uid,nickname,phone,amount;
        public String getUid() {return this.uid;}
        public String getNickname() {return this.nickname;}
        public String getAmount() {return this.amount;}
        public String getPhone() {return this.phone;}
    }
}
