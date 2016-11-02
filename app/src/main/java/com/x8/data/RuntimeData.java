package com.x8.data;

import com.x8.bean.StateBean;
import com.x8.socket.ISessionObj;

import static com.x8.bean.StateBean.ControlMode.QUERY_MODE;

public class RuntimeData {

    private static StateBean adjustBean = new StateBean();
    private static StateBean paramBean = new StateBean();
    private static StateBean queryBean = new StateBean();
    private static ISessionObj sessionObj;

    private RuntimeData(){
        queryBean.setControlMode(QUERY_MODE);
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
}
