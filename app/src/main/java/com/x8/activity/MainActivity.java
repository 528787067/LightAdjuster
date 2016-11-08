package com.x8.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.x8.bean.StateBean;
import com.x8.constant.SourceConstant;
import com.x8.data.RuntimeData;
import com.x8.socket.ISessionObj;
import com.x8.socket.SocketHandlerAdapter;
import com.x8.utils.DataTranslate;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final int TOAST_DELAY = 1000;
    private static final int REFRESH_TIMEOUT = 1000;
    private static final int NO_RESPONSE_COUNTS = 3;

    private Animation bgSwitchAnimation;

    private List<ImageView> switchBnImg;
    private List<TextView> switchBnTitle;
    private List<SeekBar> ledAdjustSeekBar;
    private List<TextView> ledAdjustText;
    private List<TextView> ledParamText;

    private View background;
    private TextView workModeText;
    private TextView workTimeText;
    private AlertDialog dialog;
    private AlertDialog exitAlert;

    private SocketBroadcastReceiver socketBroadcastReceiver;
    private Handler handler;
    private Runnable queryRunnable;
    private Toast toast;

    private ISessionObj sessionObj;
    volatile private boolean refreshFlag;
    volatile private boolean modeSwitchFlag;
    volatile private int queryCounts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        switchBnImg = new ArrayList<>();
        switchBnTitle = new ArrayList<>();
        ledAdjustSeekBar = new ArrayList<>();
        ledAdjustText = new ArrayList<>();
        ledParamText = new ArrayList<>();
        bgSwitchAnimation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.background_switch);
        socketBroadcastReceiver = new SocketBroadcastReceiver();
        sessionObj = RuntimeData.getSessionObj();
        refreshFlag = false;
        modeSwitchFlag = false;
        queryCounts = 0;

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == TOAST_DELAY && toast != null)
                    toast.cancel();
            }
        };
        queryRunnable = new Runnable() {
            @Override
            public void run() {
                if(++queryCounts > NO_RESPONSE_COUNTS && sessionObj.isConnected() && !dialog.isShowing()) {
                    dialogShowMessage(R.string.dialog_msg_not_respone);
                    return;
                }
                if(queryCounts > NO_RESPONSE_COUNTS/2) {
                    if (refreshFlag) {
                        refreshFlag = false;
                        toastMessage(R.string.toast_msg_data_refresh_err);
                    }
                    if (modeSwitchFlag) {
                        modeSwitchFlag = false;
                        toastMessage(R.string.toast_msg_mode_switch_err);
                    }
                }
                if(!sessionObj.isConnected())
                    return;
                sessionObj.write(RuntimeData.getQueryBean());
                handler.postDelayed(this, REFRESH_TIMEOUT);
            }
        };
        handler.post(queryRunnable);

        viewInit();
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
        MainActivity.this.registerReceiver(socketBroadcastReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MainActivity.this.unregisterReceiver(socketBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(sessionObj != null && sessionObj.isConnected())
            sessionObj.close();
    }

    private void viewInit() {
        background = this.findViewById(R.id.activity_main_bg);
        for(int i = 0; i < SourceConstant.SWITCH_BN_ID.length; i++){
            findViewById(SourceConstant.SWITCH_BN_ID[i]).setOnClickListener(this);
            switchBnImg.add((ImageView) ((ViewGroup)findViewById(SourceConstant.SWITCH_BN_ID[i])).getChildAt(0));
            switchBnTitle.add((TextView) ((ViewGroup)findViewById(SourceConstant.SWITCH_BN_ID[i])).getChildAt(1));
        }
        for(int i = 0; i < SourceConstant.ADJUST_SEEKBAR_ID.length; i++) {
            ledAdjustSeekBar.add((SeekBar) this.findViewById(SourceConstant.ADJUST_SEEKBAR_ID[i]));
            ledAdjustSeekBar.get(i).setOnSeekBarChangeListener(this);
        }
        for(int i = 0; i < SourceConstant.ADJUST_TEXT_ID.length; i++)
            ledAdjustText.add((TextView) this.findViewById(SourceConstant.ADJUST_TEXT_ID[i]));
        for(int i = 0; i < SourceConstant.PARAM_TEXT_ID.length; i++)
            ledParamText.add((TextView) this.findViewById(SourceConstant.PARAM_TEXT_ID[i]));

        this.findViewById(R.id.switch_bn_refresh).setOnClickListener(this);
        workModeText = (TextView) this.findViewById(R.id.work_mode_text);
        workTimeText = (TextView) this.findViewById(R.id.work_time_text);

        dialog = new AlertDialog.Builder(MainActivity.this).create();
        dialog.setTitle(R.string.dialog_title);
        dialog.setCancelable(false);
        dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_negative_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(sessionObj != null && sessionObj.isConnected())
                    sessionObj.close();
                RuntimeData.getAdjustBean().beanInit();
                RuntimeData.getParamBean().beanInit();
                viewInitWithStateBean(RuntimeData.getAdjustBean(), RuntimeData.getParamBean());
            }
        });
        exitAlert = new AlertDialog.Builder(MainActivity.this).create();
        exitAlert.setTitle(R.string.dialog_exit_title);
        exitAlert.setMessage(getString(R.string.dialog_exit_msg));
        exitAlert.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.dialog_positive_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        exitAlert.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.dialog_negative_button), (DialogInterface.OnClickListener)null);

        viewInitWithStateBean(RuntimeData.getAdjustBean(), RuntimeData.getParamBean());
    }

    private void viewInitWithStateBean(StateBean adjustBean, StateBean paramBean) {
        if(adjustBean != null) {
            int mode = DataTranslate.beanModeToNum(adjustBean);

            for(int i = 0; i < switchBnImg.size(); i++)
                switchBnImg.get(i).setImageResource(SourceConstant.SWITCH_ICON_NORMAL_IMG[i]);
            for(int i = 0; i < switchBnTitle.size(); i++)
                switchBnTitle.get(i).setTextColor(getResources().getColor(R.color.switch_title_normal_color));
            if(mode == -1){
                background.setBackgroundResource(R.mipmap.bg_not_connected);
            } else{
                background.setBackgroundResource(SourceConstant.BACKGROUD_IMG[mode]);
                if(mode < SourceConstant.SWITCH_ICON_SELECTED_IMG.length) {
                    switchBnImg.get(mode).setImageResource(SourceConstant.SWITCH_ICON_SELECTED_IMG[mode]);
                    switchBnTitle.get(mode).setTextColor(getResources().getColor(R.color.switch_title_selected_color));
                }
            }
            int[] ledValues = DataTranslate.ledNumToArr(adjustBean);
            for(int i = 0; i < ledAdjustSeekBar.size(); i++)
                ledAdjustSeekBar.get(i).setProgress(ledValues[i]);
            for(int i = 0; i < ledAdjustText.size(); i++)
                setValueOnTextView(ledAdjustText.get(i), SourceConstant.LED_VALUE_STR[i], ledValues[i]);
        }
        if(paramBean != null){
            int mode = DataTranslate.beanModeToNum(paramBean);
            workModeText.setText(getString(R.string.work_mode) + getString((mode == -1 ) ? R.string.mode_not_connected : SourceConstant.SWITCH_TITLE_STR[mode]));
            workTimeText.setText(getString(R.string.work_time) + RuntimeData.getWorkTime());
            int[] ledValues = DataTranslate.ledNumToArr(paramBean);
            for(int i = 0; i < SourceConstant.PARAM_TEXT_ID.length; i++)
                setValueOnTextView(ledParamText.get(i), SourceConstant.LED_VALUE_STR[i], ledValues[i]);
        }
    }

    private void setValueOnTextView(TextView textView, int stringId, int value){
        textView.setText(getText(stringId).toString() + ((value < 10) ? ("0" + value) : value) + "%");
    }

    private void toastMessage(int textId){
        if(toast != null)
            toast.cancel();
        handler.removeMessages(TOAST_DELAY);
        toast = Toast.makeText(MainActivity.this, textId, Toast.LENGTH_LONG);
        toast.show();
        handler.sendEmptyMessageDelayed(TOAST_DELAY, TOAST_DELAY);
    }

    private void dialogShowMessage(int textId){
        dialog.setMessage(getString(textId));
        if(!dialog.isShowing())
            dialog.show();
    }

    @Override
    public void onClick(View v) {
        if(sessionObj == null || !sessionObj.isConnected()){
            dialogShowMessage(R.string.dialog_msg_not_connect);
            return;
        }
        if(v.getId() == R.id.switch_bn_refresh){
            refreshFlag = true;
            sessionObj.write(RuntimeData.getQueryBean());
            toastMessage(R.string.toast_msg_data_refreshing);
            return;
        }
        for(int i = 0; i < SourceConstant.SWITCH_BN_ID.length; i++){
            if(v.getId() == SourceConstant.SWITCH_BN_ID[i]){
                int modeIndex = DataTranslate.beanModeToNum(RuntimeData.getAdjustBean());
                if(modeIndex == i) {
                    refreshFlag = true;
                    sessionObj.write(RuntimeData.getQueryBean());
                    toastMessage(R.string.toast_msg_data_refreshing);
                    return;
                }
                modeSwitchFlag = true;
                if(modeIndex >= 0 && modeIndex < SourceConstant.SWITCH_ICON_NORMAL_IMG.length) {
                    switchBnImg.get(modeIndex).setImageResource(SourceConstant.SWITCH_ICON_NORMAL_IMG[modeIndex]);
                    switchBnTitle.get(modeIndex).setTextColor(getResources().getColor(R.color.switch_title_normal_color));
                }
                switchBnImg.get(i).setImageResource(SourceConstant.SWITCH_ICON_SELECTED_IMG[i]);
                switchBnTitle.get(i).setTextColor(getResources().getColor(R.color.switch_title_selected_color));
                background.setBackgroundResource(SourceConstant.BACKGROUD_IMG[i]);
                background.startAnimation(bgSwitchAnimation);
                RuntimeData.getAdjustBean().setControlMode(DataTranslate.numToMode(i));
                sessionObj.write(RuntimeData.getAdjustBean());
                toastMessage(R.string.toast_msg_mode_switching);
                sessionObj.write(RuntimeData.getQueryBean());
                return;
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(RuntimeData.getAdjustBean().getControlMode() != StateBean.ControlMode.MANUAL_MODE)
            return;
        for(int i = 0; i < SourceConstant.ADJUST_SEEKBAR_ID.length; i++){
            if(seekBar.getId() == SourceConstant.ADJUST_SEEKBAR_ID[i]){
                setValueOnTextView(ledAdjustText.get(i), SourceConstant.LED_VALUE_STR[i], progress);
                if(i == 0)
                    RuntimeData.getAdjustBean().setLed1Value(progress);
                else if(i == 1)
                    RuntimeData.getAdjustBean().setLed2Value(progress);
                else if(i == 2)
                    RuntimeData.getAdjustBean().setLed3Value(progress);
                else //if(i == 3)
                    RuntimeData.getAdjustBean().setLed4Value(progress);
                if(sessionObj != null && sessionObj.isConnected())
                    sessionObj.write(RuntimeData.getAdjustBean());
                return;
            }
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if(sessionObj == null || !sessionObj.isConnected())
            return;
        if(RuntimeData.getAdjustBean().getControlMode() == StateBean.ControlMode.MANUAL_MODE)
            return;
        int modeIndex = DataTranslate.beanModeToNum(RuntimeData.getAdjustBean());
        switchBnImg.get(modeIndex).setImageResource(SourceConstant.SWITCH_ICON_NORMAL_IMG[modeIndex]);
        switchBnTitle.get(modeIndex).setTextColor(getResources().getColor(R.color.switch_title_normal_color));
        background.setBackgroundResource(R.mipmap.bg_manual);
        background.startAnimation(bgSwitchAnimation);
        RuntimeData.getAdjustBean().setControlMode(StateBean.ControlMode.MANUAL_MODE);
        toastMessage(R.string.switch_to_manual);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(sessionObj == null || !sessionObj.isConnected())
            dialogShowMessage(R.string.dialog_msg_not_connect);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK)
            exitAlert.show();
        return true;
    }

    private class SocketBroadcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case SocketHandlerAdapter.SESSION_INPUT_CLOSED:
                    dialogShowMessage(R.string.dialog_msg_not_connect);
                    break;
                case SocketHandlerAdapter.SESSION_MESSAGE_RECEIVED:
                    if(queryCounts != 0)
                        queryCounts = 0;
                    if(refreshFlag) {
                        RuntimeData.getAdjustBean().setControlMode(RuntimeData.getParamBean().getControlMode());
                        toastMessage(R.string.toast_msg_data_refresh_success);
                    }
                    if(modeSwitchFlag) {
                        modeSwitchFlag = false;
                        if (RuntimeData.getAdjustBean().getControlMode() != RuntimeData.getParamBean().getControlMode()) {
                            toastMessage(R.string.toast_msg_mode_switch_err);
                            MainActivity.this.viewInitWithStateBean(null, RuntimeData.getParamBean());
                            return;
                        }
                        toastMessage(R.string.toast_msg_mode_switch_success);
                    }
                    if(RuntimeData.getAdjustBean().getControlMode() == RuntimeData.getParamBean().getControlMode()) {
                        if(RuntimeData.getAdjustBean().getControlMode() == StateBean.ControlMode.MANUAL_MODE && !refreshFlag)
                            return;
                        RuntimeData.getAdjustBean().setLed1Value(RuntimeData.getParamBean().getLed1Value());
                        RuntimeData.getAdjustBean().setLed2Value(RuntimeData.getParamBean().getLed2Value());
                        RuntimeData.getAdjustBean().setLed3Value(RuntimeData.getParamBean().getLed3Value());
                        RuntimeData.getAdjustBean().setLed4Value(RuntimeData.getParamBean().getLed4Value());
                        if(refreshFlag){
                            refreshFlag = false;
                            MainActivity.this.viewInitWithStateBean(RuntimeData.getAdjustBean(), RuntimeData.getParamBean());
                            return;
                        }
                        int[] ledValues = DataTranslate.ledNumToArr(RuntimeData.getAdjustBean());
                        for(int i = 0; i < ledAdjustSeekBar.size(); i++)
                            ledAdjustSeekBar.get(i).setProgress(ledValues[i]);
                        for(int i = 0; i < ledAdjustText.size(); i++)
                            setValueOnTextView(ledAdjustText.get(i), SourceConstant.LED_VALUE_STR[i], ledValues[i]);
                    }
                    MainActivity.this.viewInitWithStateBean(null, RuntimeData.getParamBean());
                    break;
                case SocketHandlerAdapter.SESSION_EXCEPTION_CAUGHT:
                    dialogShowMessage(R.string.dialog_msg_connect_has_err);
                    break;
                default:
                    return;
            }
        }
    }
}
