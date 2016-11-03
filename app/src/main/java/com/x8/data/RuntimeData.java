package com.x8.data;

import com.x8.bean.StateBean;
import com.x8.socket.ISessionObj;

import static com.x8.bean.StateBean.ControlMode.QUERY_MODE;

public class RuntimeData {

    private static StateBean adjustBean = new StateBean();
    private static StateBean paramBean = new StateBean();
    private static StateBean queryBean = new StateBean();
    private static ISessionObj sessionObj;
    private static String workTime;

    private RuntimeData(){
        queryBean.setControlMode(QUERY_MODE);
        workTime = "00:00:00";
    }

    public static StateBean getAdjustBean(){
        return adjustBean;
    }

    public static StateBean getParamBean(){
        return paramBean;
    }

    public static StateBean getQueryBean(){
        return queryBean;
    }

    public static void setSessionObj(ISessionObj _sessionObj){
        sessionObj = _sessionObj;
    }

    public static ISessionObj getSessionObj(){
        return sessionObj;
    }

    public static void setWorkTime(int hour, int minute, int second){
        String strHour = (hour<10) ? ("0"+hour) : hour+"";
        String strMinute = (minute<10) ? ("0"+minute) : minute+"";
        String strSecond = (second<10) ? ("0"+second) : second+"";
        workTime = strHour + ":" + strMinute + ":" + strSecond;
    }

    public static String getWorkTime(){
        return workTime;
    }
}
