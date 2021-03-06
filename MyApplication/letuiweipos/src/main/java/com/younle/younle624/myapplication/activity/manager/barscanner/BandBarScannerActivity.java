package com.younle.younle624.myapplication.activity.manager.barscanner;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.younle.younle624.myapplication.R;
import com.younle.younle624.myapplication.constant.Constant;
import com.younle.younle624.myapplication.domain.printsetting.PrintDevices;
import com.younle.younle624.myapplication.utils.LogUtils;
import com.younle.younle624.myapplication.utils.SaveUtils;
import com.younle.younle624.myapplication.utils.Utils;
import com.younle.younle624.myapplication.utils.notice.NoticePopuUtils;
import com.younle.younle624.myapplication.utils.scanbar.HidConncetUtil;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

public class BandBarScannerActivity extends Activity {
    private static final String TAG = "BandBarScannerActivity";
    private TextView tv_add_new_device;
    private TextView tv_added;
    private RelativeLayout rl_device_info;
    private LinearLayout ll_cancel;
    private TextView tv_cancel;
    private TextView tv_title;
    private LinearLayout al_barscanner;
    private PrintDevices savedScannerDevice;
    private TextView barscanner_name;
    private BluetoothAdapter bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();
    private BluetoothDevice btDev;
    private BluetoothSocket mBluetoothSocket;
    private LinearLayout ll_howto_addbarscanner;
    private TextView tv_scanner_state;
    private PopupWindow scanner_connecting_pup;
    private TextView tv_delete_scanner;
    private Handler handler = new Handler(){
        public void handleMessage(Message msg){
            if(msg.what==1) {
                setFailure();
            }
        }
    };
    private String bleName="";

    private void setFailure() {
        Toast.makeText(BandBarScannerActivity.this, "连接失败,请重试！", Toast.LENGTH_SHORT).show();
        bluetoothAdapter.cancelDiscovery();
        if(scanner_connecting_pup!=null&&scanner_connecting_pup.isShowing()) {
            scanner_connecting_pup.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_band_bar_scanner);
        if(!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        savedScannerDevice = (PrintDevices) SaveUtils.getObject(this, Constant.SAVED_BLUETOOTH_SCANNER);
        regisitReceiver();
        initView();
        setListner();
    }

    private void setListner() {
        tv_add_new_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addScanner();
                showScannerStateDia();
                handler.sendEmptyMessageDelayed(1,90000);
            }
        });
        ll_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        tv_delete_scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBindPup("确定要删除该设备吗？");
            }
        });
    }

    private void initView() {
        ll_howto_addbarscanner = (LinearLayout)findViewById(R.id.ll_howto_addbarscanner);
        tv_delete_scanner = (TextView)findViewById(R.id.tv_delete_scanner);
        tv_add_new_device = (TextView) findViewById(R.id.tv_add_new_device);
        tv_added = (TextView)findViewById(R.id.tv_added);
        rl_device_info = (RelativeLayout)findViewById(R.id.rl_device_info);
        ll_cancel = (LinearLayout)findViewById(R.id.ll_cancel);
        tv_cancel = (TextView)findViewById(R.id.tv_cancel);
        tv_cancel.setText("管理");
        tv_title = (TextView)findViewById(R.id.tv_title);
        tv_title.setText("扫码枪设置");
        al_barscanner = (LinearLayout)findViewById(R.id.al_barscanner);
        barscanner_name = (TextView)findViewById(R.id.barscanner_name);
        if(savedScannerDevice !=null) {
            ll_howto_addbarscanner.setVisibility(View.GONE);
            rl_device_info.setVisibility(View.VISIBLE);
            barscanner_name.setText(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(savedScannerDevice.getBlueToothAdd()).getName());
            tv_added.setVisibility(View.VISIBLE);
            tv_add_new_device.setVisibility(View.GONE);
        }else {
            ll_howto_addbarscanner.setVisibility(View.VISIBLE);
            rl_device_info.setVisibility(View.GONE);
            tv_added.setVisibility(View.GONE);
            tv_add_new_device.setVisibility(View.VISIBLE);
        }
    }
    public void showBindPup(String msg) {
       NoticePopuUtils.showBindPup(this, msg, R.id.al_barscanner, new NoticePopuUtils.OnClickCallBack() {
            @Override
            public void onClickYes() {
                unBindBluetooth();
                SaveUtils.saveObject(BandBarScannerActivity.this, Constant.SAVED_BLUETOOTH_SCANNER, null);
                rl_device_info.setVisibility(View.GONE);
                tv_added.setVisibility(View.GONE);
                tv_add_new_device.setVisibility(View.VISIBLE);
                ll_howto_addbarscanner.setVisibility(View.VISIBLE);
            }

            @Override
            public void onClickNo() {
                LogUtils.Log("取消删除");
            }
        });
    }

    private void addScanner() {
        bluetoothAdapter.startDiscovery();//开始搜索周边蓝牙设备
    }

    private void regisitReceiver() {
        IntentFilter mFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        registerReceiver(mReceiver, mFilter);
    }
    

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                isScannerDevice(device);
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                setProgressBarIndeterminateVisibility(false);
            }else if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice bondDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                LogUtils.e(TAG,"绑定状态变更了");
                if(bondDevice.getBondState()==BluetoothDevice.BOND_BONDING) {
                    LogUtils.Log("绑定中");
                }
                if(bondDevice.getBondState()==BluetoothDevice.BOND_BONDED) {
                    LogUtils.Log("已绑定");
                    bluetoothAdapter.cancelDiscovery();
                    connect(bondDevice);
                }
                if(bondDevice.getBondState()==BluetoothDevice.BOND_NONE){
                    if(handler!=null) {
                        handler.removeCallbacksAndMessages(null);
                    }
                    setFailure();
                }
            }else if(action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                int conectState = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);
                LogUtils.e(TAG, "connectState==" + conectState);
                final BluetoothDevice device=intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(conectState==0) {
                    LogUtils.e(TAG,"未连接");
                }else if(conectState==1) {
                    LogUtils.e(TAG,"连接中。。。");
                }else if(conectState==2) {
                    handler.removeCallbacksAndMessages(null);
                    LogUtils.e(TAG, "已连接");
                    //更新视图,并保存状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateView(device);
                        }
                    });
                }
            }
        }
    };

    private void updateView(BluetoothDevice device) {
        //1.popuwindow消失
        dismissDia();
        ll_howto_addbarscanner.setVisibility(View.GONE);
        tv_added.setVisibility(View.VISIBLE);
        tv_add_new_device.setVisibility(View.GONE);
        rl_device_info.setVisibility(View.VISIBLE);

        LogUtils.Log("待保存的设备名称：" + device.getName());
        barscanner_name.setText(device.getName());

        savedScannerDevice  = new PrintDevices("", device.getAddress(), "蓝牙扫码枪");
        SaveUtils.saveObject(this, Constant.SAVED_BLUETOOTH_SCANNER,savedScannerDevice);

    }

    private void dismissDia() {
        if(scanner_connecting_pup!=null&&scanner_connecting_pup.isShowing()) {
            scanner_connecting_pup.dismiss();
        }
    }

    private void isScannerDevice(BluetoothDevice btDev) {
        int majorDeviceClass = btDev.getBluetoothClass().getMajorDeviceClass();
        if(majorDeviceClass==BluetoothClass.Device.Major.PERIPHERAL) {
            bluetoothAdapter.cancelDiscovery();
            this.btDev = btDev;
            LogUtils.Log("btDev="+btDev.getName());
            startBond(btDev);
        }
    }

    private void showScannerStateDia() {
        View scanner_state_view = View.inflate(this, R.layout.bar_scanner_setting, null);
        ImageView iv_loading= (ImageView) scanner_state_view.findViewById(R.id.iv_loading);
        Utils.pbAnimation(this, iv_loading);

        scanner_connecting_pup = new PopupWindow(scanner_state_view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        params.alpha=0.4f;
        getWindow().setAttributes(params);
        scanner_connecting_pup.setBackgroundDrawable(new BitmapDrawable());
        scanner_connecting_pup.setOutsideTouchable(true);
        scanner_connecting_pup.setFocusable(true);
        scanner_connecting_pup.showAtLocation(findViewById(R.id.al_barscanner), Gravity.CENTER, 0, 0);
        scanner_connecting_pup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                params.alpha = 1;
                getWindow().setAttributes(params);
            }
        });
    }

    private void startBond(BluetoothDevice btDev) {
        if (btDev.getBondState() == BluetoothDevice.BOND_NONE) {
            try {
                Boolean returnValue = false;
                Method createBond = BluetoothDevice.class.getMethod("createBond");
                returnValue = (Boolean) createBond.invoke(btDev);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (btDev.getBondState() == BluetoothDevice.BOND_BONDED) {
            connect(btDev);
        }
    }
    public void connect(BluetoothDevice btDev){
        HidConncetUtil hidConncetUtil=new HidConncetUtil(this);
        hidConncetUtil.connect(btDev);
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if(mReceiver!=null) {
            unregisterReceiver(mReceiver);
        }
        super.onDestroy();
    }
    private void unBindBluetooth() {
        Set<BluetoothDevice> bondedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
        Iterator<BluetoothDevice> iterator = bondedDevices.iterator();
        while (iterator.hasNext()){
            BluetoothDevice bluetoothDevice = iterator.next();
            String blueToothAdd = savedScannerDevice.getBlueToothAdd();
            if(bluetoothDevice.getAddress().equals(blueToothAdd)) {
                Boolean returnValue = false;
                Method createBond = null;
                try {
                    createBond = BluetoothDevice.class.getMethod("removeBond");
                    returnValue= (Boolean) createBond.invoke(bluetoothDevice);
                    LogUtils.Log("returnValue:" + returnValue);
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.Log("解除绑定："+e.toString());
                }
            }
        }
    }
}
