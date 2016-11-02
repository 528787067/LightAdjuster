package com.x8.utils;

public class DataCheck {

    public static boolean checkByteArray(byte[] bytes){
        if(bytes.length != 16)
            return false;
        if((bytes[0]&0xFF) != 0xAA || bytes[1] != 0x03 || bytes[15] != 0x55)
            return false;
        if(bytes[3] > 0x08 && bytes[3] != 0x80)
            return false;
        return true;
    }
}
