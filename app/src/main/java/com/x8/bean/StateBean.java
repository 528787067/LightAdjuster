package com.x8.bean;

public class StateBean {

    private ControlMode controlMode;
    private int led1Value;
    private int led2Value;
    private int led3Value;
    private int led4Value;

    public StateBean(){
        beanInit();
    }

    public void beanInit(){
        this.controlMode = ControlMode.NOT_CONNECTED_MODE;
        this.led1Value = 0;
        this.led2Value = 0;
        this.led3Value = 0;
        this.led4Value = 0;
    }

    public ControlMode getControlMode() {
        return controlMode;
    }

    public void setControlMode(ControlMode controlMode) {
        this.controlMode = controlMode;
    }

    public int getLed1Value() {
        return led1Value;
    }

    public void setLed1Value(int led1Value) {
        this.led1Value = led1Value;
    }

    public int getLed2Value() {
        return led2Value;
    }

    public void setLed2Value(int led2Value) {
        this.led2Value = led2Value;
    }

    public int getLed3Value() {
        return led3Value;
    }

    public void setLed3Value(int led3Value) {
        this.led3Value = led3Value;
    }

    public int getLed4Value() {
        return led4Value;
    }

    public void setLed4Value(int led4Value) {
        this.led4Value = led4Value;
    }

    public enum ControlMode{
        SPRING_FALL_MODE,                // 春秋季模式
        SUMMER_MODE,                     // 夏季模式
        WINTER_MODE,                     // 冬季模式
        SUNNY_DAY_MODE,                  // 晴天模式
        CLOUDY_DAY_MODE,                 // 阴天模式
        LIGHTNING_MODE,                  // 闪电模式
        PARTLY_CLOUDY_MODE,             // 飘云模式
        PROTECTRION_MODE,               // 保护模式
        MANUAL_MODE,                    // 手动模式
        QUERY_MODE,                     // 查询模式
        NOT_CONNECTED_MODE             // 未连接
    }
}
