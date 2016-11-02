package com.x8.socket;

import com.x8.bean.StateBean;

import org.apache.mina.core.session.IoSession;

public interface ISessionObj {
    public boolean isActive();
    public boolean isConnected();
    public boolean isClosing();
    public boolean write(StateBean bean);
    public void close();
}
