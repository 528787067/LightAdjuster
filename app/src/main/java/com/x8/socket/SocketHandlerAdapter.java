package com.x8.socket;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.x8.bean.StateBean;
import com.x8.data.RuntimeData;
import com.x8.utils.DataCheck;
import com.x8.utils.DataTranslate;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

public class SocketHandlerAdapter extends IoHandlerAdapter implements ISessionObj {

    public static final String SESSION_CREATED = "session_created";
    public static final String SESSION_OPENED = "session_opened";
    public static final String SESSION_CLOSED = "session_closed";
    public static final String SESSION_IDLE = "session_idle";
    public static final String SESSION_INPUT_CLOSED = "session_input_closed";
    public static final String SESSION_EXCEPTION_CAUGHT = "session_exception_caught";
    public static final String SESSION_MESSAGE_RECEIVED = "session_message_received";
    public static final String SESSION_MESSAGE_SENT = "session_message_send";
    public static final String SESSION_MESSAGE_RECEIVED_ERR = "session_message_received_err";
    public static final String SESSION_CONNECT_ERROR = "session_connect_error";

    private Context context;
    private IoSession session;

    public SocketHandlerAdapter(Context context){
        this.context = context;
    }

    @Override
    public boolean isActive() {
        return session.isActive();
    }

    @Override
    public boolean isConnected(){
        return this.session.isConnected();
    }

    @Override
    public boolean isClosing() {
        return session.isClosing();
    }

    @Override
    public boolean write(StateBean bean){
        if(this.session == null || !this.session.isConnected())
            return false;
        this.session.write(DataTranslate.beanToByteArr(bean));
        return true;
    }

    @Override
    public void close(){
        if(this.session != null && !this.session.isClosing())
            this.session.closeNow();
    }

    private void eventReceived(IoSession session, String action){
        this.session = session;
        context.sendBroadcast(new Intent().setAction(action));
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        eventReceived(session, SESSION_CREATED);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        eventReceived(session, SESSION_OPENED);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        if(session.isConnected())
            return;
        RuntimeData.getAdjustBean().beanInit();
        RuntimeData.getParamBean().beanInit();
        RuntimeData.setWorkTime(0, 0, 0);
        eventReceived(session, SESSION_CLOSED);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        eventReceived(session, SESSION_IDLE);
    }

    @Override
    public void inputClosed(IoSession session) throws Exception {
        if(session.isClosing() || !session.isConnected())
            return;
        if(session.isConnected())
            session.closeNow();
        RuntimeData.getAdjustBean().beanInit();
        RuntimeData.getParamBean().beanInit();
        eventReceived(session, SESSION_INPUT_CLOSED);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        if(cause.getMessage().equals(SESSION_CONNECT_ERROR))
            eventReceived(session, SESSION_CONNECT_ERROR);
        else
            eventReceived(session, SESSION_EXCEPTION_CAUGHT);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        eventReceived(session, SESSION_MESSAGE_SENT);
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        byte[] bytes = (byte[])message;
        if(!DataCheck.checkByteArray(bytes)){
            eventReceived(session, SESSION_MESSAGE_RECEIVED_ERR);
            return;
        }
        StateBean bean = DataTranslate.byteArrToBean(bytes);
        RuntimeData.getParamBean().setControlMode(bean.getControlMode());
        RuntimeData.getParamBean().setLed1Value(bean.getLed1Value());
        RuntimeData.getParamBean().setLed2Value(bean.getLed2Value());
        RuntimeData.getParamBean().setLed3Value(bean.getLed3Value());
        RuntimeData.getParamBean().setLed4Value(bean.getLed4Value());
        RuntimeData.setWorkTime(bytes[12]&0xFF, bytes[13]&0xFF, bytes[14]&0xFF);
        eventReceived(session, SESSION_MESSAGE_RECEIVED);
    }
}
