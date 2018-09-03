package com.baiduface.www.Utils.Json;

/**
 * Created by yy on 2018/6/1.
 */
public class ChinaRegisterInfoJson {
    //{"state":1,"code":"200","data":{"uid":"91483","msg":"Succeessful"}}
    //{"state":0,"code":301,"msg":"","data":{"uid":"","msg":"The face info is unvalid."}}
    private int state,code;
    private Data data;

    public int getState() {return this.state;}
    public int getCode() {return this.code;}
    public Data getData() {return this.data;}

    public class Data{
        private String uid,msg;
        public String getUid() {return this.uid;}
        public String getMsg() {return this.msg;}
    }
}
