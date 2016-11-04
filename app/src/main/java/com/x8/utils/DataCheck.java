package com.x8.utils;

public class DataCheck {

    public static boolean checkByteArray(byte[] bytes){
        if(bytes.length != 16)
            return false;
        if((bytes[0]&0xFF) != 0xAA || bytes[1] != 0x03 || bytes[15] != 0x55)
            return false;
        if((bytes[3] > 0x06 && bytes[3] != 0x08) || bytes[3] < 0x00)
            return false;
        if(bytes[4] < 0x00 || bytes[5] < 0x00 || bytes[6] < 0x00 || bytes[7] < 0x00)
            return false;
        if(bytes[4] > 100 || bytes[5] > 100 || bytes[6] > 100 | bytes[7] > 100)
            return false;
        if(bytes[13] < 0 || bytes[13] > 59 || bytes[14] < 0 || bytes[14] > 59)
            return false;
        return true;
    }
}
