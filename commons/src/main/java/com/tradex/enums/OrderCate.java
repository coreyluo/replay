package com.tradex.enums;


public enum OrderCate implements IdAndName {
    MR("买入", 0),
    MC("卖出", 1),
    RZMR("融资买入", 2),
    RQMC("融券卖出", 3),
    MQHQ("买券还券", 4),
    MQHK("卖券还款", 5),
    XQHQ("现券还券", 6);

    private String name;
    private int id;

    OrderCate(String name, int id) {
        this.name = name;
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return id + name;
    }
}