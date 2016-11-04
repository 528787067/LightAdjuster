package com.x8.utils;

import com.x8.bean.StateBean;
import com.x8.bean.StateBean.ControlMode;

public class DataTranslate {
    public static byte[] beanToByteArr(StateBean state){
        byte[] bytes = new byte[16];
        bytes[0] = (byte) 0xAA;
        bytes[1] = (byte) 0x04;
        bytes[2] = (byte) 0x00;
        switch (state.getControlMode()){
            case SPRING_FALL_MODE:
                bytes[3] = (byte) 0x00;
                break;
            case SUMMER_MODE:
                bytes[3] = (byte) 0x01;
                break;
            case WINTER_MODE:
                bytes[3] = (byte) 0x02;
                break;
            case SUNNY_DAY_MODE:
                bytes[3] = (byte) 0x03;
                break;
            case CLOUDY_DAY_MODE:
                bytes[3] = (byte) 0x04;
                break;
            case LIGHTNING_MODE:
                bytes[3] = (byte) 0x05;
                break;
            case PARTLY_CLOUDY_MODE:
                bytes[3] = (byte) 0x06;
                break;
            case PROTECTRION_MODE:
                bytes[3] = (byte) 0x07;
                break;
            case MANUAL_MODE:
                bytes[3] = (byte) 0x08;
                break;
            case QUERY_MODE:
            default:
                bytes[3] = (byte) 0x80;
        }
        bytes[4] = (byte) state.getLed1Value();
        bytes[5] = (byte) state.getLed2Value();
        bytes[6] = (byte) state.getLed3Value();
        bytes[7] = (byte) state.getLed4Value();
        bytes[8] = (byte) 0x00;
        bytes[9] = (byte) 0x00;
        bytes[10] = (byte) 0x00;
        bytes[11] = (byte) 0x00;
        bytes[12] = (byte) 0x00;
        bytes[13] = (byte) 0x00;
        bytes[14] = (byte) 0x00;
        bytes[15] = (byte) 0x55;
        return bytes;
    }
    public static StateBean byteArrToBean(byte[] bytes){
        StateBean state = new StateBean();
        switch (bytes[3]&0xFF){
            case 0x00:
                state.setControlMode(ControlMode.SPRING_FALL_MODE);
                break;
            case 0x01:
                state.setControlMode(ControlMode.SUMMER_MODE);
                break;
            case 0x02:
                state.setControlMode(ControlMode.WINTER_MODE);
                break;
            case 0x03:
                state.setControlMode(ControlMode.SUNNY_DAY_MODE);
                break;
            case 0x04:
                state.setControlMode(ControlMode.CLOUDY_DAY_MODE);
                break;
            case 0x05:
                state.setControlMode(ControlMode.LIGHTNING_MODE);
                break;
            case 0x06:
                state.setControlMode(ControlMode.PARTLY_CLOUDY_MODE);
                break;
            case 0x07:
                state.setControlMode(ControlMode.PROTECTRION_MODE);
                break;
            case 0x08:
                state.setControlMode(ControlMode.MANUAL_MODE);
                break;
            case 0x80:
                state.setControlMode(ControlMode.QUERY_MODE);
                break;
            default:
                state.setControlMode(ControlMode.NOT_CONNECTED_MODE);
        }
        state.setLed1Value(bytes[4]&0xFF);
        state.setLed2Value(bytes[5]&0xFF);
        state.setLed3Value(bytes[6]&0xFF);
        state.setLed4Value(bytes[7]&0xFF);
        return state;
    }

    public static int beanModeToNum(StateBean state){
        switch (state.getControlMode()){
            case SPRING_FALL_MODE:
                return 0;
            case SUMMER_MODE:
                return 1;
            case WINTER_MODE:
                return 2;
            case SUNNY_DAY_MODE:
                return 3;
            case CLOUDY_DAY_MODE:
                return 4;
            case LIGHTNING_MODE:
                return 5;
            case PARTLY_CLOUDY_MODE:
                return 6;
            case MANUAL_MODE:
                return 7;
            default:
                return -1;
        }
    }

    public static ControlMode numToMode(int num){
        switch (num){
            case 0:
                return ControlMode.SPRING_FALL_MODE;
            case 1:
                return ControlMode.SUMMER_MODE;
            case 2:
                return ControlMode.WINTER_MODE;
            case 3:
                return ControlMode.SUNNY_DAY_MODE;
            case 4:
                return ControlMode.CLOUDY_DAY_MODE;
            case 5:
                return ControlMode.LIGHTNING_MODE;
            case 6:
                return ControlMode.PARTLY_CLOUDY_MODE;
            case 7:
                return ControlMode.MANUAL_MODE;
            default:
                return ControlMode.NOT_CONNECTED_MODE;
        }
    }

    public static int[] ledNumToArr(StateBean state){
        int[] arr = new int[4];
        arr[0] = state.getLed1Value();
        arr[1] = state.getLed2Value();
        arr[2] = state.getLed3Value();
        arr[3] = state.getLed4Value();
        if(arr[0] > 99)
            arr[0] = 99;
        if(arr[1] > 99)
            arr[1] = 99;
        if(arr[2] > 99)
            arr[2] = 99;
        if(arr[3] > 99)
            arr[3] = 99;
        return arr;
    }

}
