package com.younle.younle624.myapplication.activity.manager.orderpager.bluetoothprinter;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.util.DisplayMetrics;

import com.younle.younle624.myapplication.R;
import com.younle.younle624.myapplication.activity.manager.orderpager.bluetoothprinter.print.PrintUtil;
import com.younle.younle624.myapplication.constant.Constant;
import com.younle.younle624.myapplication.domain.printsetting.SavedPrinter;
import com.younle.younle624.myapplication.utils.SaveUtils;

/**
 * Created by yefeng on 6/2/15.
 * github:yefengfreedom
 */
public class AppInfo {

    public static String dType;
    public static String dVersion;

    public static int appCode;
    public static String appVersion;
    public static String appName;

    public static int width;
    public static int height;
    public static float density;
    public static int densityDpi;

    public static String btAddress;
    public static String btAddress1;
    public static String btName;


    public static void init(Context mContext) {
        dType = Build.MODEL;
        dVersion = Build.VERSION.SDK_INT + "_" + Build.VERSION.RELEASE;
        PackageInfo pi = null;
        try {
            pi = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (null != pi) {
            appVersion = pi.versionName;
            appCode = pi.versionCode;
        } else {
            appVersion = "";
            appCode = 0;
        }
        appName = mContext.getString(R.string.app_name);
        initDisplay(mContext);
        SavedPrinter printDevices = (SavedPrinter) SaveUtils.getObject(mContext, Constant.BT_PRINTER);
        if(printDevices!=null) {
            String blueToothAdd = printDevices.getBlueToothAdd();
            btAddress = blueToothAdd;
        }
        btName = PrintUtil.getDefaultBluetoothDeviceName(mContext);
    }

    public static void initDisplay(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        width = metrics.widthPixels;
        height = metrics.heightPixels;
        density = metrics.density;
        densityDpi = metrics.densityDpi;
    }
}
