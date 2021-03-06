package com.younle.younle624.myapplication.activity.manager.orderpager.pos;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.younle.younle624.myapplication.R;
import com.younle.younle624.myapplication.activity.manager.orderpager.BaseActivity;
import com.younle.younle624.myapplication.activity.manager.orderpager.ShowChartActivity;
import com.younle.younle624.myapplication.adapter.OrderAdapter;
import com.younle.younle624.myapplication.application.MyApplication;
import com.younle.younle624.myapplication.constant.Constant;
import com.younle.younle624.myapplication.constant.UrlConstance;
import com.younle.younle624.myapplication.domain.PosOrderBean;
import com.younle.younle624.myapplication.domain.PosOrderKinds;
import com.younle.younle624.myapplication.domain.PrintTotalBean;
import com.younle.younle624.myapplication.domain.SavedFailOrder;
import com.younle.younle624.myapplication.domain.printsetting.SavedPrinter;
import com.younle.younle624.myapplication.domain.waimai.WmPintData;
import com.younle.younle624.myapplication.myservice.BluetoothService;
import com.younle.younle624.myapplication.myservice.FailOrderService;
import com.younle.younle624.myapplication.utils.LogUtils;
import com.younle.younle624.myapplication.utils.NetWorks;
import com.younle.younle624.myapplication.utils.SaveUtils;
import com.younle.younle624.myapplication.utils.Utils;
import com.younle.younle624.myapplication.utils.printmanager.BTPrintUtils;
import com.younle.younle624.myapplication.utils.printmanager.PrintDia;
import com.younle.younle624.myapplication.utils.printmanager.PrintUtils;
import com.younle.younle624.myapplication.view.SelfLinearLayout;
import com.younle.younle624.myapplication.view.XListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.DbManager;
import org.xutils.common.util.MD5;
import org.xutils.ex.DbException;
import org.xutils.x;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

import static com.younle.younle624.myapplication.activity.PintActivity.mService;

/**
 * pos机订单统计
 *
 */
public class PosOrderActivity extends BaseActivity implements SelfLinearLayout.ClickToReload, XListView.IXListViewListener {

    public OrderAdapter orderAdapter;
    public View to_chart;
    public boolean isShowDia = false;
    private PosOrderBean posOrderBean;
    private String TAG = "PosOrderActivity";
    private TextView tv_num_order;
    private TextView tv_num_money;
    private String goodId = "0";
    private String sizeId = "0";
    private int pageNum = 1;
    private String selectTime = "";
    private int code;
    private String TAG_DIR_CASH = "1";//直接收款的标识
    List<PosOrderBean.MsgBean.OrderListBean> posOrderBeanList = new ArrayList<>();
    MyApplication myAppinstance = MyApplication.getInstance();//数据库DB
    DbManager.DaoConfig daoConfig = myAppinstance.getDaoConfig();
    DbManager db = x.getDb(daoConfig);
    private String payType="-1";
    private String payTool="0";
    private List<PosOrderBean.MsgBean.OrderListBean> xListData;
    private boolean everUpdate=false;
    private int ordertype = 0;
    private boolean resetLeftRight = false;
    private boolean hasMemberSys  = false;
    private boolean isFirst  = true;
    private AlertDialog printingDia;

    private List<WmPintData> printData;
    private List<WmPintData> btprintData;

    private IWoyouService iWoyouService;
    //本地打印服务
    public ServiceConnection connService = new ServiceConnection() {
        //连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            iWoyouService = IWoyouService.Stub.asInterface(service);//拿到打印服务的对象
        }

        //断开服务
        @Override
        public void onServiceDisconnected(ComponentName name) {
            iWoyouService = null;
        }
    };
    private ICallback printCallBack=new ICallback.Stub() {
        @Override
        public void onRunResult(boolean isSuccess) throws RemoteException {
            LogUtils.Log("打印成功:");
            if(isSuccess) {
                if(printingDia!=null&&printingDia.isShowing()) {
                    printingDia.dismiss();
                }
            }
        }
        @Override
        public void onReturnString(String result) throws RemoteException {
            LogUtils.Log("onReturnString:"+result);
        }
        @Override
        public void onRaiseException(int code, String msg) throws RemoteException {
            LogUtils.Log("onRaiseException:"+msg);
        }
    };
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Constant.WAIT_BLUETOOTH_OPEN://等待蓝牙打开
                    BTPrintUtils.getInstance().connectBtPrinterTest(mService,PosOrderActivity.this,handler);
                    break;
                case Constant.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(PosOrderActivity.this, "已连接", Toast.LENGTH_SHORT).show();
                            startBtPrint();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(PosOrderActivity.this, "正在连接", Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_LISTEN:
                            break;
                        case BluetoothService.STATE_NONE:
                            Toast.makeText(PosOrderActivity.this, "无连接", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    break;
            }
        }
    };
    private boolean reigistService=false;
    private String remark = "";//搜索字段

    @Override
    public void getPrintData(final String startTime, final String endTime) {
        PrintUtils.getInstance().initService(this, connService);
        reigistService=true;
        showPrintingDia();
        NetWorks netWorks=new NetWorks(this);
        Map<String, String> params = netWorks.getPublicParams();
        params.put(Constant.PARAMS_ADV_ID,Constant.ADV_ID);
        params.put(Constant.PARAMS_NEME_STORE_ID,Constant.STORE_ID);
        params.put("start",startTime);
        params.put("end",endTime);
        params.put(Constant.PARAMS_NAME_VERSIONCODE,Constant.VERSION_CODE+"");
        netWorks.Request(UrlConstance.REQUEST_TOTAL_PRINT, params, 10000,0,new NetWorks.OnNetCallBack() {
            @Override
            public void onError(Exception e, int flag) {
                LogUtils.Log("获取打印汇总error() "+e.toString());
                Utils.showToast(PosOrderActivity.this,"未能取得数据，请重试");
            }

            @Override
            public void onResonse(String response, int flag) {
                LogUtils.Log("获取打印数据汇总返回："+response.toString());
                parsePrintDataJson(response,startTime,endTime);
            }
        });
    }
    /**
     * 打印中的dialog
     */
    private void showPrintingDia() {
        printingDia=PrintDia.getInstance().showPrintingDia(this, new PrintDia.PrintingCallBacK() {
            @Override
            public void close() {
                printingDia.dismiss();
            }
        });
    }
    /**
     * 解析打印汇总数据json
     * @param json
     */
    private void parsePrintDataJson(String json,String start,String end) {
        try {
            JSONObject jo=new JSONObject(json);
            int code=jo.getInt("code");
            String err = jo.getString("err");
            if(code==200) {
                Gson gson=new Gson();
                PrintTotalBean printTotalBean = gson.fromJson(jo.getString("data"), PrintTotalBean.class);

                printData = PrintUtils.getInstance().initTotalPrintData(printTotalBean, start, end,false);
                SavedPrinter device = (SavedPrinter) SaveUtils.getObject(this, Constant.BT_PRINTER);
                if(device!=null) {
                    btprintData = PrintUtils.getInstance().initTotalPrintData(printTotalBean, start, end,true);
                    startBtPrint();
                }
                startPrint();
            }else {
                printingDia.dismiss();
                Utils.showToast(this,err,1000);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void startPrint() {
        if (iWoyouService != null) {
            if(printData!=null&&printData.size()>0) {
                PrintUtils.getInstance().newPosPrint(iWoyouService, printData,printCallBack);
            }
        }
    }
    private void startBtPrint() {
        if(mService.getState()== BluetoothService.STATE_CONNECTED) {
            BTPrintUtils.getInstance().btFormatDataPrint(mService, btprintData, this);
        }else {
            BTPrintUtils.getInstance().connectBtPrinterTest(mService,this,handler);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mService= BluetoothService.getInstance();
        mService.setHandler(handler);
    }

    /**
     * 初始化选择时间
     */
    private void initSelectTime() {
        /*long lon = System.currentTimeMillis();
        selectTime = String.valueOf(lon/1000);*/
        selectTime = "";
    }

    @Override
    public void initTitle() {
        tv_title.setText("收银机订单统计");
        tv_mark_reback.setVisibility(View.VISIBLE);
        tv_mark_reback.setText("打印汇总单");
    }

    @Override
    public View initHeadView() {

        tv_select_left.setText("全部订单");
        left_icon.setVisibility(View.VISIBLE);
        rl_select_left.setEnabled(true);//设置可点击不可点击
        View headerView = View.inflate(this, R.layout.pos_order_header, null);
        to_chart = headerView.findViewById(R.id.rl_order_and_total);
        tv_num_order = (TextView) headerView.findViewById(R.id.tv_num_order);//售出商品：100
        tv_num_money = (TextView) headerView.findViewById(R.id.tv_num_money);//收款总额：2000.00元
        return headerView;
    }

    @Override
    public void initData() {
        reLoad();
        initPayWay();
        pageNum=1;
        if(!everUpdate) {
            updateAllFailLists();//同步所有失败订单
        }else {
            reLoad();
            getSelectedData(goodId);//更新过，直接加载数据
        }
    }

    @Override
    protected void onRestart() {
        if(xl_pos_order==null) {
            initView();
            initHeadView();
            initXlistView();
        }
        initData();
        super.onRestart();
    }

    /**
     * 初始化支付数据
     */
    private void initPayWay() {

        rightSelectData =new ArrayList<>();
        rightSelectData.add(new PosOrderKinds("全部收银通道", "-1"));
        rightSelectData.add(new PosOrderKinds("微信收款(记账)","8"));
        if(Constant.OPEN_WXPAY == 1){
            rightSelectData.add(new PosOrderKinds("微信收款","0"));
        }
        rightSelectData.add(new PosOrderKinds("支付宝收款(记账)","9"));
        if(Constant.OPEN_ALIPAY == 1){
            rightSelectData.add(new PosOrderKinds("支付宝收款","1"));
        }
        //刷卡支付判断
        if(Constant.UNION_PAY_STATUS == 1 || Constant.UNION_PAY_STATUS == 2){
            rightSelectData.add(new PosOrderKinds("刷卡收款","2"));
        }
        rightSelectData.add(new PosOrderKinds("刷卡收款(记账)","6"));
        rightSelectData.add(new PosOrderKinds("现金收款(记账)","3"));
        if(ordertype==0){
            rightSelectData.add(new PosOrderKinds("会员卡余额收款","4"));
        }
        setRightFilterData(rightSelectData, Constant.POS_ORDER_CHOOSE_GOODS);
        payToolsSelectData = new ArrayList<>();
        payToolsSelectData.add(new PosOrderKinds("全部收银方式","0"));
        payToolsSelectData.add(new PosOrderKinds("收银机","1"));
        payToolsSelectData.add(new PosOrderKinds("收款二维码","2"));
        payToolsSelectData.add(new PosOrderKinds("自助点单小程序","3"));
        payToolsSelectData.add(new PosOrderKinds("社区门店小程序","4"));

        setPayToolsFilterData(payToolsSelectData, Constant.POS_ORDER_CHOOSE_GOODS);
    }

    private void updateAllFailLists() {
        everUpdate=true;
        List<SavedFailOrder> listFailUpdate = null;
        try {
            listFailUpdate = db.findAll(SavedFailOrder.class);
        } catch (DbException e) {
            e.printStackTrace();
        }
        if(listFailUpdate != null && listFailUpdate.size()>0){
            LogUtils.e(TAG,"listFailUpdate.size()="+listFailUpdate.size());
            String listFails = new Gson().toJson(listFailUpdate);
            LogUtils.e(TAG,"同步上去的数据listFails====="+listFails);
            String currentTime = Utils.getCurrentTime();
            String token = Utils.getToken(currentTime,this);

            OkHttpUtils.post()
                    .url(UrlConstance.FAIL_ORDER_SUBMIT)
                    .addParams(Constant.PARAMS_NAME_POSTOKEN, token)
                    .addParams(Constant.PARAMS_NAME_TIMEAUTH, currentTime)
                    .addParams(Constant.PARAMS_NAME_USERACCOUNT, Constant.USER_ACCOUNT)
                    .addParams(Constant.PARAMS_NAME_IMEI, Constant.DEVICE_IMEI)
                    .addParams(Constant.PARAMS_NAME_DEVICENAME, Constant.DEVICE_NAME)
                    .addParams(Constant.PARAMS_NAME_MODEL, Constant.DEVICE_MODEL)
                    .addParams("userkey", MD5.md5(MD5.md5(Constant.PASSWORD)))//密码
                    .addParams("order", listFails)
                    .addParams("deviceid",Constant.DEVICE_IMEI)
                    .build()
                    .connTimeOut(5000)
                    .readTimeOut(5000)
                    .writeTimeOut(5000)
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {//延迟一定时间继续请求
                            LogUtils.e(TAG, "onError()=" + e);
                            isShowDia = true;
                            reLoad();
                            getSelectedData(goodId);
                        }

                        @Override
                        public void onResponse(String response) {//只要有返回就是成功
                            LogUtils.e(TAG, "onResponse():" + response);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                int isSuccess = jsonObject.getInt("code");
                                if (200 == isSuccess) {
                                    //cancleFailOrder();
                                    LogUtils.e(TAG, "删除所有数据");
                                    List<SavedFailOrder> failAllLists = db.findAll(SavedFailOrder.class);
                                    if (failAllLists != null) {
                                        if (failAllLists.size() > 0) {
                                            db.dropTable(SavedFailOrder.class);
                                        }
                                    }
                                } else {
                                    isShowDia = true;
                                }
                                //停止服务
                                if (3003 == isSuccess) {
                                    LogUtils.e(TAG, "3003stopService");
                                    Intent intent = new Intent(PosOrderActivity.this, FailOrderService.class);
                                    PosOrderActivity.this.stopService(intent);
                                    return;
                                }
                            } catch (Exception e) {
                                LogUtils.e(TAG, "Exception e" + e);
                                e.printStackTrace();
                            }
                            reLoad();
                            getSelectedData(goodId);
                        }
                    });
        }else{
            LogUtils.e(TAG,"没有失败订单需要同步");
            isShowDia = false;
            reLoad();
            getSelectedData(goodId);
        }
    }

    @Override
    public void chooseStatistics(int which) {
        super.chooseStatistics(which);
        switch (which){
            case 0:
                ordertype=0;
                tv_explain_statistics.setText(getString(R.string.income_statistics));
                if(rightSelectData!=null){
                    int size = rightSelectData.size();
                    if(!"会员卡余额收款".equals(rightSelectData.get(size-1).getName())){
                        rightSelectData.add(new PosOrderKinds("会员卡余额收款","4"));
                    }
                }
                break;
            case 1:
                ordertype=1;
                tv_explain_statistics.setText(getString(R.string.order_statistics));
                if(rightSelectData!=null){
                    int size = rightSelectData.size();
                    if("会员卡余额收款".equals(rightSelectData.get(size-1).getName())){
                        rightSelectData.remove(size-1);
                    }
                }
                break;
        }
        resetLeftRight = true;
        //切换的时候清除所有记录
        resetParams();
        //调用请求数据接口
        getSelectedData(goodId);//更新过，直接加载数据
    }

    private void resetParams() {
        goodId = "0";
        pageNum = 1;
        payType="-1";
        goodstype = "0";
    }

    /**
     * 装填数据显示
     */
    private void  showData() {
        //加载视图消失
        ll_loading.setVisibility(View.GONE);
        xl_pos_order.setPullRefreshEnable(true);
//        rl_select_all_tools.setVisibility(View.VISIBLE);
        //设置header数据
        if (xListData==null||xListData.size()<= 0){
            to_chart.setVisibility(View.GONE);
            ll_nodata.setVisibility(View.VISIBLE);
            if("自助点单小程序".equals(tv_all_tools.getText().toString())){
                tv_date_exception.setText(R.string.not_open_search_prompt);
            }else{
                tv_date_exception.setText(R.string.nodata);
            }
        }else{
            to_chart.setVisibility(View.VISIBLE);
            tv_num_order.setText("结算订单："+posOrderBean.getMsg().getSaled()+"个订单");
            tv_num_money.setText("总收入：￥" + String.valueOf(posOrderBean.getMsg().getIncome()));
        }

        //当不返回商品名称列表时，筛选隐藏
        List<PosOrderBean.MsgBean.GoodsNameListBean> goodsNameList = posOrderBean.getMsg().getGoodsNameList();

        if (goodsNameList != null&&goodsNameList.size() > 0){
            setLeftFilterData(goodsNameList, Constant.POS_ORDER_CHOOSE_GOODS);
        }
        orderAdapter = new OrderAdapter(this,remark);
        orderAdapter.setData(xListData,goodstype,goodId,sizeId);
        xl_pos_order.setAdapter(orderAdapter);
        xl_pos_order.setXListViewListener(this);
        //设置加载更多不可见
        int size = posOrderBean.getMsg().getOrderList().size();
        LogUtils.e(TAG, "posOrderBean.getMsg().getOrderList().size="+size);
        int count = 0;
        for (int i = 0; i<size;i++){
            List<PosOrderBean.MsgBean.OrderListBean> orderList = posOrderBean.getMsg().getOrderList();
            count = count+orderList.size();
        }
        if (count<10){
            xl_pos_order.setPullLoadEnable(false);
        }else{
            xl_pos_order.setPullLoadEnable(true);
        }
        //判断是否显示Dia
        if(isShowDia){
            isShowDia = false;
            new AlertDialog.Builder(this)
                    .setMessage("部分订单正在同步中，因此可能暂时无法显示，稍后即可显示。")
                    .setPositiveButton("确定", null)
                    .show();
        }

        if(posOrderBean.getMsg().getHasMemberSys()>0&&isFirst){
            all_order_two_header.setVisibility(View.VISIBLE);
            rb_statistics_by_income.setChecked(true);
            hasMemberSys = true;
            isFirst = false;
        }

        //是否重新设置左右品类显示
        if(resetLeftRight){
            resetLeftRight=false;
            //设置左右两个品类选择初始化显示：没有根据返回来动态设置
            tv_select_left.setText("全部订单");
            tv_right_select.setText("全部收银通道");
            tv_all_tools.setText("全部收银方式");
        }
    }

    /*private void setRemarkWay() {

        //判断是否显示后加的记账方式 8微信账 9支付宝记账 6刷卡记账 rightSelectData
        String remark="";
        List<Integer> markPayType = posOrderBean.getMsg().getMarkPayType();
        if(markPayType != null){
            for(int i=0;i<markPayType.size();i++){
                remark+=markPayType.get(i);
            }
            if(!remark.contains("6")){
                for(int i=0;i<rightSelectData.size();i++){
                    if("6".equals(rightSelectData.get(i).getId())){
                        rightSelectData.remove(i);
                        break;
                    }
                }
            }
            if(!remark.contains("8")){
                for(int i=0;i<rightSelectData.size();i++){
                    if("8".equals(rightSelectData.get(i).getId())){
                        rightSelectData.remove(i);
                        break;
                    }
                }
            }
            if(!remark.contains("9")){
                for(int i=0;i<rightSelectData.size();i++){
                    if("9".equals(rightSelectData.get(i).getId())){
                        rightSelectData.remove(i);
                        break;
                    }
                }
            }
        }
    }*/

    @Override
    public void setListener() {
        ll_loading.setClickToReload(this);

        to_chart.setOnClickListener(new MothDayOnClickListener());//(售出商品和收款总额)点击进入订单列表
        /*lv_pos_kinds.setOnItemClickListener(new KindsItemOnClickListener());
        lv_all_store.setOnItemClickListener(new StoreItemOnClickListener());*/
        et_order_search.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    tv_cancel.setVisibility(View.VISIBLE);
                    tv_cancel.setText("取消");
                } else {
                    tv_cancel.setVisibility(View.GONE);
                }
            }
        });
        et_order_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable != null && !"".equals(editable.toString())){
                    pageNum = 1;
                    remark = editable.toString();
                }else{
                    remark = "";
                }
                getSelectedData(goodId);
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                remark = "";
                if(et_order_search.hasFocus()) {
                    et_order_search.setText(null);
                    et_order_search.clearFocus();
                }
                getSelectedData(goodId);
            }
        });
        super.setListener();
    }

     /**
     * 选择完具体请求id的回调
     * @param id
     */
    @Override
    public void getChooseDataFromNet(String sizeId,String id,int isLeft,String goodsType,String payTool) {
        Utils.showWaittingDialog(this,"正在请求数据...");
        pageNum=1;
        goodstype = goodsType;
        if(isLeft==0){
            goodId = id;//此时传的是具体商品的goodsid
            this.sizeId = sizeId;
            getSelectedData(goodId);
        }else if(isLeft==1){
            payType = rightSelectData.get(Integer.valueOf(id)).getId();
            tv_right_select.setText(rightSelectData.get(Integer.valueOf(id)).getName());
            Toast.makeText(PosOrderActivity.this, rightSelectData.get(Integer.valueOf(id)).getName(), Toast.LENGTH_SHORT).show();
            changeCurrentState(2);
            //getSelectedData("0");//商品id为0时，为全部商品范围
            LogUtils.e(TAG,"getChooseDataFromNet() 右侧点击的数据请求");
            getSelectedData(goodId);
        }else{
            tv_all_tools.setText(payToolsSelectData.get(Integer.valueOf(id)).getName());
            LogUtils.e(TAG,"getChooseDataFromNet 点击最最最右侧 payTool="+payTool);
            this.payTool = payTool;
            changeCurrentState(3);
            getSelectedData(goodId);
        }
    }

    /**
     * 点击重新加载
     */
    @Override
    public void ClickToReload() {
        initData();
    }

    /**
     * 下拉刷新
     */
    @Override
    public void onRefresh() {
        LogUtils.e(TAG, "onRefresh()...");
        pageNum = 1;
        getSelectedData(goodId);
    }

    /**
     * 加载更多
     */
    @Override
    public void onLoadMore() {
        getMoreData(goodId, TAG_DIR_CASH);
    }

    /**
     * 请求数据，返回订单信息(初始化时候id为0，筛选时候id为商品id)
     *
     * @param id
     */
    private void getSelectedData(String id) {

        initSelectTime();
        String currentTime = Utils.getCurrentTime();
        LogUtils.e(TAG, "currentTime="+currentTime);
        String token = Utils.getToken(currentTime,this);

        LogUtils.e(TAG, "==========================");
        LogUtils.e(TAG, "token=" + token);
        LogUtils.e(TAG, "currentTime=" + currentTime);
        LogUtils.e(TAG, "useraccount=" + Constant.USER_ACCOUNT);
        LogUtils.e(TAG, "imei=" + Constant.DEVICE_IMEI);
        LogUtils.e(TAG, "devicename=" + Constant.DEVICE_NAME);
        LogUtils.e(TAG, "posmod=" + Constant.DEVICE_MODEL);
        LogUtils.e(TAG, "storeid=" + Constant.STORE_ID);
        LogUtils.e(TAG, "==========================");
        LogUtils.e(TAG, "goodsid=" + id);
        LogUtils.e(TAG, "goodstype=" + goodstype);
        LogUtils.e(TAG, "start=" + startTime);
        LogUtils.e(TAG, "end=" + endTime);
        LogUtils.e(TAG, "selectTime=" + selectTime);
        LogUtils.e(TAG, "page=" + String.valueOf(pageNum));
        LogUtils.e(TAG, "ordertype=" + String.valueOf(ordertype));
        LogUtils.e(TAG,"payType="+payType);
        LogUtils.e(TAG,"payTool="+payTool);
        LogUtils.e(TAG,"version=new");
        LogUtils.e(TAG,"versionCode="+Constant.VERSION_CODE);
        LogUtils.e(TAG,"remark="+remark);
        LogUtils.e(TAG, "==========================");
        OkHttpUtils
                .post()
                .url(UrlConstance.POS_LIST)
                .addParams(Constant.PARAMS_NAME_POSTOKEN, token)//token
                .addParams(Constant.PARAMS_NAME_TIMEAUTH, currentTime)
                .addParams(Constant.PARAMS_NAME_USERACCOUNT, Constant.USER_ACCOUNT)
                .addParams(Constant.PARAMS_NAME_IMEI, Constant.DEVICE_IMEI)
                .addParams(Constant.PARAMS_NAME_DEVICENAME, Constant.DEVICE_NAME)
                .addParams(Constant.PARAMS_NAME_MODEL, Constant.DEVICE_MODEL)
                .addParams("userkey", MD5.md5(MD5.md5(Constant.PASSWORD)))//密码
                .addParams(Constant.PARAMS_NEME_STORE_ID, Constant.STORE_ID)
                .addParams("goodsid", id)//商品id,按商品名称筛选
                .addParams("sizeid",sizeId)
                .addParams("goodstype", goodstype)
                .addParams("start",startTime+" "+startHour+":"+startMinute)
                .addParams("end",endTime+" "+endHour+":"+endMinute)
                .addParams("selectTime", selectTime)//第一页的查询时间戳（默认为当前系统时间）
                .addParams("page", String.valueOf(pageNum))//页码
                .addParams("payType", payType)
                .addParams("ordertype", String.valueOf(ordertype))
                .addParams("payTool", payTool)
                .addParams("version", "new")
                .addParams("versionCode",Constant.VERSION_CODE+"")
                .addParams("remark",remark)//搜索：用户输入的订单号或者备注信息
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        LogUtils.e(TAG, "返回数据失败：" + e.toString());
                        loadFailure("请稍后再试！");
                        Utils.dismissWaittingDialog();
                    }

                    @Override
                    public void onResponse(String response) {
                        LogUtils.e(TAG,"pos机收款列表 response="+response);
                        boolean toNextStep = Utils.checkSaveToken(PosOrderActivity.this, response);
                        Utils.dismissWaittingDialog();
                        if (toNextStep) {
                            parseJson(response);
                        }
                    }
                });
    }

    /**
     * 请求数据，返回订单信息(初始化时候id为0，筛选时候id为商品id)
     * @param id
     */
    private void getMoreData(String id, final String type) {

        String currentTime = Utils.getCurrentTime();
        String token = Utils.getToken(currentTime,this);

        boolean available = Utils.isNetworkAvailable(this);
        if(!available) {
            netError();
        }else {
            OkHttpUtils
                    .post()
                    .url(UrlConstance.POS_LIST)
                    .addParams(Constant.PARAMS_NAME_POSTOKEN, token)//token
                    .addParams(Constant.PARAMS_NAME_TIMEAUTH, currentTime)
                    .addParams(Constant.PARAMS_NAME_USERACCOUNT, Constant.USER_ACCOUNT)
                    .addParams(Constant.PARAMS_NAME_IMEI, Constant.DEVICE_IMEI)
                    .addParams(Constant.PARAMS_NAME_DEVICENAME, Constant.DEVICE_NAME)
                    .addParams(Constant.PARAMS_NAME_MODEL, Constant.DEVICE_MODEL)
                    .addParams("userkey", MD5.md5(MD5.md5(Constant.PASSWORD)))//密码
                    .addParams(Constant.PARAMS_NEME_STORE_ID, Constant.STORE_ID)
                    .addParams("goodsid", id)//商品id,按商品名称筛选
                    .addParams("goodstype", goodstype)
                    .addParams("start", startTime)//查询的开始时间（默认当月1号） startTime
                    .addParams("end", endTime)//查询的结束时间（默认当月当天） endTime
                    .addParams("selectTime", selectTime)//第一页的查询时间戳（默认为当前系统时间）
                    .addParams("page", String.valueOf(pageNum + 1))//页码
                    .addParams("payType", payType)
                    .addParams("ordertype", String.valueOf(ordertype))
                    .addParams("payTool", payTool)
                    .addParams("version", "new")
                    .addParams("versionCode",Constant.VERSION_CODE+"")
                    .addParams("remark",remark)//搜索：用户输入的订单号或者备注信息
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            LogUtils.e(TAG, "返回数据失败：" + e.toString());
                            loadFailure("请稍后再试！");
                            xl_pos_order.stopLoadMore();
                        }

                        @Override
                        public void onResponse(String response) {
                            boolean toNextStep = Utils.checkSaveToken(PosOrderActivity.this, response);
                            ll_loading.setVisibility(View.GONE);
                            LogUtils.e(TAG, "返回成功StringCallBack onResponse()=" + response);
                            if (toNextStep) {
                                parseMoreJson(response);
                            }
                        }
                    });
        }
    }

    /**
     * 加载更多的数据处理
     * @param json
     */
    private void parseMoreJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            code = jsonObject.getInt("code");
            if (code == 200) {
                Gson gson = new Gson();
                    LogUtils.e(TAG, "加载更多返回成功：");
                    PosOrderBean posOrderBeanMore = gson.fromJson(json, PosOrderBean.class);
                List<PosOrderBean.MsgBean.OrderListBean> orderList = posOrderBeanMore.getMsg().getOrderList();
                LogUtils.Log("orderList="+orderList.size());
                if(posOrderBeanMore.getMsg().getOrderList() != null && posOrderBeanMore.getMsg().getOrderList().size() > 0){
                    pageNum++;
                    xListData.addAll(posOrderBeanMore.getMsg().getOrderList());
                    LogUtils.e(TAG,"xListData.tojson="+(new Gson().toJson(xListData)));
                    xl_pos_order.stopLoadMore();
                    orderAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(PosOrderActivity.this, "没有更多数据", Toast.LENGTH_SHORT).show();
                    xl_pos_order.noMoreData();
                }
            } else {
                xl_pos_order.stopLoadMore();
                Toast.makeText(PosOrderActivity.this, "当前网络异常", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析json
     * @param json
     */
    private void parseJson(String json) {
        LogUtils.e(TAG,"parseJson");
        try {
            JSONObject jsonObject = new JSONObject(json);
            code = jsonObject.getInt("code");
            if (code == 200) {
                Gson gson = new Gson();
                posOrderBean = gson.fromJson(json, PosOrderBean.class);
                if(!Constant.IS_FIRST_SELECT_TIME){//只取第一次的值
                    Constant.IS_FIRST_SELECT_TIME = true;
                    selectTime = posOrderBean.getMsg().getSelectTime()+"";
                }
                xListData =posOrderBean.getMsg().getOrderList();
                xl_pos_order.stopRefresh();
                xl_pos_order.stopLoadMore();
                xl_pos_order.setVisibility(View.VISIBLE);
                ll_nodata.setVisibility(View.GONE);
                LogUtils.e(TAG, "showData()");
                showData();
            } else {
                ll_loading.setVisibility(View.GONE);
                ll_nodata.setVisibility(View.VISIBLE);
                xl_pos_order.setVisibility(View.GONE);
                tv_date_exception.setText(getString(R.string.nodata));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 图形列表展示的监听
     */
    private class MothDayOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.rl_order_and_total://本月总计
                    String time = tv_title_date.getText().toString();
                    String s_time=time.substring(0,time.indexOf("至"));
                    String e_time=time.substring(time.indexOf("至")+1,time.length());
                    Intent intent = new Intent(PosOrderActivity.this, ShowChartActivity.class);
                    intent.putExtra("from", Constant.POS_DATA);
                    intent.putExtra(Constant.START_DATE,s_time);
                    intent.putExtra(Constant.END_DATE,e_time);
                    intent.putExtra(Constant.GOOD_ID,goodId);
                    intent.putExtra("sizeid",sizeId);
                    intent.putExtra(Constant.GOOD_TYPE,goodstype);
                    intent.putExtra("right_selected",payType);
                    intent.putExtra("left_title",tv_select_left.getText().toString());
                    intent.putExtra("record_pos",three_layer_pos);
                    intent.putExtra("ordertype",ordertype);
                    intent.putExtra("payTool",payTool);
                    intent.putExtra("hasMemberSys",hasMemberSys);
                    startActivity(intent);
                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        if(reigistService) {
            unbindService(connService);
        }
        goodId = "0";
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
