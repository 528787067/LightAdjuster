package com.x8.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.x8.data.RuntimeData;
import com.x8.socket.ByteArrayCodecFactory;
import com.x8.socket.ISessionObj;
import com.x8.socket.SocketConnector;
import com.x8.socket.SocketHandlerAdapter;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int CONNECT_TIME_OUT = 2000;
    private static final String DATA = "data";
    private static final String IP_ADDRESS = "ipAddress";
    private static final String PORT = "port";

    private SocketConnector connector;
    private ISessionObj sessionObj;
    private SocketBroadcastReceiver socketBroadcastReceiver;

    private SharedPreferences sp;
    SharedPreferences.Editor editor;

    private EditText ipAddress;
    private EditText port;
    private AlertDialog dialog;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = getSharedPreferences(DATA, MODE_PRIVATE);
        editor = sp.edit();

        connector = new SocketConnector();
        connector.setHanderAdapter(new SocketHandlerAdapter(getApplicationContext()));
        connector.setProtocolCodecFactory(new ByteArrayCodecFactory());
        connector.setTimeOut(CONNECT_TIME_OUT);
        socketBroadcastReceiver = new SocketBroadcastReceiver();

        ipAddress = (EditText) this.findViewById(R.id.edit_text_ip);
        port = (EditText) this.findViewById(R.id.edit_text_port);

        ipAddress.setText(sp.getString(IP_ADDRESS, getString(R.string.connect_ip_text)));
        port.setText(sp.getString(PORT, getString(R.string.connect_port_text)));

        dialog = new AlertDialog.Builder(LoginActivity.this).create();
        dialog.setTitle(R.string.dialog_title);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_positive_button), (Message)null);
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setCancelable(false);

        this.findViewById(R.id.button_connect).setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SocketHandlerAdapter.SESSION_CREATED);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_OPENED);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_CLOSED);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_IDLE);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_INPUT_CLOSED);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_EXCEPTION_CAUGHT);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_MESSAGE_RECEIVED);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_MESSAGE_SENT);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_MESSAGE_RECEIVED_ERR);
        intentFilter.addAction(SocketHandlerAdapter.SESSION_CONNECT_ERROR);
        LoginActivity.this.registerReceiver(socketBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LoginActivity.this.unregisterReceiver(socketBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);
    }

    @Override
    public void onClick(View v) {
        if(ipAddress.getText().toString().trim().length() == 0 || port.getText().toString().trim().length() == 0) {
            Toast.makeText(LoginActivity.this, getString(R.string.toast_msg_edit_text_null), Toast.LENGTH_SHORT).show();
            return;
        }
        connector.setIp(ipAddress.getText().toString());
        connector.setPort(Integer.parseInt(port.getText().toString().trim()));
        progressDialog.setMessage(getString(R.string.dialog_msg_connecting));
        progressDialog.show();
        sessionObj = connector.connect();
    }

    private class SocketBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case SocketHandlerAdapter.SESSION_MESSAGE_RECEIVED:
                    RuntimeData.getAdjustBean().setControlMode(RuntimeData.getParamBean().getControlMode());
                    RuntimeData.getAdjustBean().setLed1Value(RuntimeData.getParamBean().getLed1Value());
                    RuntimeData.getAdjustBean().setLed2Value(RuntimeData.getParamBean().getLed2Value());
                    RuntimeData.getAdjustBean().setLed3Value(RuntimeData.getParamBean().getLed3Value());
                    RuntimeData.getAdjustBean().setLed4Value(RuntimeData.getParamBean().getLed4Value());
                    progressDialog.dismiss();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    break;
                case SocketHandlerAdapter.SESSION_MESSAGE_RECEIVED_ERR:
                    if(sessionObj != null && sessionObj.isConnected())
                        sessionObj.close();
                    dialog.setMessage(getString(R.string.dialog_msg_data_err));
                    progressDialog.dismiss();
                    dialog.show();
                    break;
                case SocketHandlerAdapter.SESSION_OPENED:
                    Toast.makeText(LoginActivity.this, getString(R.string.toast_msg_connect_success), Toast.LENGTH_SHORT).show();
                    RuntimeData.setSessionObj(sessionObj);

                    if(!sp.getString(IP_ADDRESS, getString(R.string.connect_ip_text)).equals(ipAddress.getText().toString().trim())
                    || !sp.getString(PORT, getString(R.string.connect_port_text)).equals(port.getText().toString().trim())) {
                        editor.putString(IP_ADDRESS, ipAddress.getText().toString().trim());
                        editor.putString(PORT, port.getText().toString().trim());
                        editor.commit();
                    }

                    sessionObj.write(RuntimeData.getQueryBean());
                    progressDialog.setMessage(getString(R.string.toast_msg_data_refreshing));
                    break;
                case SocketHandlerAdapter.SESSION_CONNECT_ERROR:
                    dialog.setMessage(getString(R.string.dialog_msg_connect_err));
                    progressDialog.dismiss();
                    dialog.show();
                    break;
                default:
                    return;
            }
        }
    }
}
