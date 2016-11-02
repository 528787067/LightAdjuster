package com.x8.socket;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

public class SocketConnector {
    private String ip;
    private int port;
    private long timeOut;
    private ProtocolCodecFactory protocolCodecFactory;
    private IoHandlerAdapter handerAdapter;
    private static IoSession ioSession;

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    public void setProtocolCodecFactory(ProtocolCodecFactory protocolCodecFactory) {
        this.protocolCodecFactory = protocolCodecFactory;
    }

    public void setHanderAdapter(IoHandlerAdapter ioHandlerAdapter){
        this.handerAdapter = ioHandlerAdapter;
    }

    public ISessionObj connect(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 创建一个连接对象
                IoConnector connector = new NioSocketConnector();
                // 设置连接时间
                connector.setConnectTimeoutMillis(timeOut);
                // 添加过滤器
                connector.getFilterChain().addLast("protocol", new ProtocolCodecFilter(protocolCodecFactory));
                // 添加业务处理类
                connector.setHandler(handerAdapter);
                try {
                    // 此处的操作是异步操作，连接后立即返回
                    ConnectFuture future = connector.connect(new InetSocketAddress(ip, port));
                    // 等待连接创建完成
                    future.awaitUninterruptibly();
                    // 获取 session
                    ioSession = future.getSession();
                    // 等待连接断开
                    ioSession.getCloseFuture().awaitUninterruptibly();
                    connector.dispose();
                } catch (Exception e){
                    try {
                        handerAdapter.exceptionCaught(ioSession, new Exception(SocketHandlerAdapter.SESSION_CONNECT_ERROR));
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }).start();
        return (ISessionObj)this.handerAdapter;
    }

}
