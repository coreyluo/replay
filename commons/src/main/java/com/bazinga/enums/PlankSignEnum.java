package com.bazinga.enums;

/**
 * @author yunshan
 * @date 2019/2/16
 */
public enum PlankSignEnum {

    SUB_NEW(0,"次新");

    private Integer code;

    private String desc;

    PlankSignEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

}
