package com.younle.younle624.myapplication.pagers;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.younle.younle624.myapplication.activity.createorder.AddServiceGoodActivity;
import com.younle.younle624.myapplication.activity.createorder.NewOrderActivity;
import com.younle.younle624.myapplication.activity.orderguide.FunctionDetailActivit;
import com.younle.younle624.myapplication.adapter.UnPayAdapter;
import com.younle.younle624.myapplication.basepager.BasePager;
import com.younle.younle624.myapplication.constant.Constant;
import com.younle.younle624.myapplication.constant.UrlConstance;
import com.younle.younle624.myapplication.domain.QuerryGoodsRoomBean;
import com.younle.younle624.myapplication.domain.UnPayBean;
import com.younle.younle624.myapplication.utils.LogUtils;
import com.younle.younle624.myapplication.utils.SpUtils;
import com.younle.younle624.myapplication.utils.Utils;
import com.younle.younle624.myapplication.view.SelfLinearLayout;
import com.younle.younle624.myapplication.view.XListView;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.younle.younle624.myapplication.R;


import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.util.MD5;

import java.util.List;

import okhttp3.Call;


/**
 * Created by 我是奋斗 on 2016/5/9.
 * 微信/e-mail:tt090423@126.com
 */
public class OrderPager extends BasePager implements View.OnClickListener, SelfLinearLayout.ClickToReload, XListView.IXListViewListener {

    private String TAG = "OrderPager";
    private XListView lv_order_list;
    private TextView tv_title;
    private EditText et_order_search;
    private TextView tv_create_new_order;
    private LinearLayout ll_create_order,ll_unpayorder_num;
    private ImageView iv_create_order;
    private UnPayBean unPayBeans;
    private List<UnPayBean.MsgBean> unPayBeanLists;
    private UnPayAdapter unPayAdapter;
    private LinearLayout ll_search_fail;
    private TextView tv_search_fail;
    private TextView tv_search_fail_desc;
    public ImageView iv_jiazai_filure;
    public SelfLinearLayout ll_loading;
    public TextView tv_loading,unpay_order_total;
    public ProgressBar pb_loading;
    private boolean isGetMore;
    private boolean isRefresh;
    private boolean isSearch;
    private int pagerNum = 2;
    //private int showPos;
    private boolean canClick;
    private LinearLayout ll_cancel;
    private String inputStr = "";
    private View view;
    private TextView tv_showfunction;
    private LinearLayout charge_layout;
    private LinearLayout normal_layout;
    private String unpay_order_num;

    public OrderPager(Activity activity) {
        super(activity);
    }

    public static final OrderPager newInstance(Activity activity) {
        OrderPager fragment = new OrderPager(activity);
        return fragment;
    }
//卡券上线前打开
    @Override
    public View initView() {
        LogUtils.Log("orderpager initView()");
            view = View.inflate(mActivity, R.layout.order_pager_layout, null);
            lv_order_list = (XListView) view.findViewById(R.id.lv_order_list);

            unpay_order_total = (TextView) view.findViewById(R.id.unpay_order_total);
            ll_unpayorder_num = (LinearLayout) view.findViewById(R.id.ll_unpayorder_num);
            lv_order_list.setPullRefreshEnable(true);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            et_order_search = (EditText) view.findViewById(R.id.et_search);
            tv_create_new_order = (TextView) view.findViewById(R.id.tv_create_new_order);
            ll_create_order = (LinearLayout) view.findViewById(R.id.ll_create_order);
            iv_create_order = (ImageView) view.findViewById(R.id.iv_create_order);
            ll_search_fail = (LinearLayout) view.findViewById(R.id.ll_search_fail);
            tv_search_fail = (TextView) view.findViewById(R.id.tv_search_fail);
            tv_search_fail_desc = (TextView) view.findViewById(R.id.tv_search_fail_desc);
            ll_cancel = (LinearLayout) view.findViewById(R.id.ll_cancel);
            ll_cancel.setVisibility(View.GONE);
            tv_title.setText("下单");

            charge_layout = (LinearLayout) view.findViewById(R.id.charge_layout);
            normal_layout = (LinearLayout) view.findViewById(R.id.normal_layout);
            tv_showfunction = (TextView)view.findViewById(R.id.tv_showfunction);
            iv_jiazai_filure = (ImageView) view.findViewById(R.id.iv_jiazai_filure);
            tv_loading = (TextView) view.findViewById(R.id.tv_loading);
            pb_loading = (ProgressBar) view.findViewById(R.id.pb_loading);
            ll_loading = (SelfLinearLayout) view.findViewById(R.id.ll_loading);
            functionNormal();
            setListener();
        return view;
    }

    private boolean functionNormal() {
        if (Constant.OPENED_PERMISSIONS.contains("2")) {
            charge_layout.setVisibility(View.GONE);
            normal_layout.setVisibility(View.VISIBLE);
            return true;
        }else {
            charge_layout.setVisibility(View.VISIBLE);
            normal_layout.setVisibility(View.GONE);
            return false;
        }
    }

    private void setListener() {
            ll_loading.setClickToReload(this);
            et_order_search.addTextChangedListener(new SearchInputWatcher());
            et_order_search.setOnFocusChangeListener(new FocusChangeListener());
            ll_create_order.setOnClickListener(this);
            lv_order_list.setXListViewListener(this);
            tv_showfunction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.startActivity(new Intent(mActivity,FunctionDetailActivit.class));
                }
            });
    }

    @Override
    public void ClickToReload() {
        initData();
    }

    @Override
    public void onRefresh() {
        isRefresh = true;
        reLoad();
        getDataFromNet();
    }

    @Override
    public void onLoadMore() {
        isGetMore = true;
        getMoreData();
    }

    private void getMoreData() {
        String currentTime = Utils.getCurrentTime();
        String token = Utils.getToken(currentTime, mActivity);
        OkHttpUtils.post()
                .url(UrlConstance.UNPAY_ORDER_LISTS)
                .addParams(Constant.PARAMS_NAME_POSTOKEN, token)
                .addParams(Constant.PARAMS_NAME_TIMEAUTH, currentTime)
                .addParams(Constant.PARAMS_NAME_USERACCOUNT, Constant.USER_ACCOUNT)
                .addParams(Constant.PARAMS_NAME_IMEI, Constant.DEVICE_IMEI)
                .addParams(Constant.PARAMS_NAME_DEVICENAME, Constant.DEVICE_NAME)
                .addParams(Constant.PARAMS_NAME_MODEL, Constant.DEVICE_MODEL)
                .addParams(Constant.PARAMS_NEME_PASSDORD_MD5, MD5.md5(MD5.md5(Constant.PASSWORD)))
                .addParams("storeid", Constant.STORE_ID)
                .addParams("pagenum", "" + pagerNum)
                .addParams("pagesize", "10")
                .addParams("param", inputStr)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        //暂未请求到数据稍后再试
                        LogUtils.e(TAG, "加载第二页：onError e=" + e);
                        netError();
                        lv_order_list.stopLoadMore();
                    }

                    @Override
                    public void onResponse(String response) {
                        LogUtils.e(TAG, "加载第二页：未支付订单返回：response=" + response);
                        boolean b = Utils.checkSaveToken(mActivity, response);
                        if (b) {
                            parseJson(response);
                        }
                    }
                });
    }

    class FocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            LogUtils.Log("hasFocus==" + hasFocus);
            if (hasFocus) {
                tv_create_new_order.setVisibility(View.VISIBLE);
                tv_create_new_order.setText("      取消     ");
                iv_create_order.setVisibility(View.GONE);
            } else {
                //et_order_search.setText("");
                tv_create_new_order.setText(" 创建订单");
                iv_create_order.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void initData() {
        LogUtils.e(TAG, "initData()......");
        isSearch = false;
        if(lv_order_list==null) {
            initView();
        }
      /*  if (Constant.OPENED_PERMISSIONS.contains("2")) {
            getDataFromNet();
        }*/
        ll_loading.setVisibility(View.VISIBLE);
        getDataFromNet();

    }
    private void getDataFromNet() {
        LogUtils.e(TAG, "initData() 联网请求数据");
        String currentTime = Utils.getCurrentTime();
        String token = Utils.getToken(currentTime, mActivity);

        OkHttpUtils.post()
                .url(UrlConstance.UNPAY_ORDER_LISTS)
                .addParams(Constant.PARAMS_NAME_POSTOKEN, token)
                .addParams(Constant.PARAMS_NAME_TIMEAUTH, currentTime)
                .addParams(Constant.PARAMS_NAME_USERACCOUNT, Constant.USER_ACCOUNT)
                .addParams(Constant.PARAMS_NAME_IMEI, Constant.DEVICE_IMEI)
                .addParams(Constant.PARAMS_NAME_DEVICENAME, Constant.DEVICE_NAME)
                .addParams(Constant.PARAMS_NAME_MODEL, Constant.DEVICE_MODEL)
                .addParams(Constant.PARAMS_NEME_PASSDORD_MD5, MD5.md5(MD5.md5(Constant.PASSWORD)))
                .addParams("storeid", Constant.STORE_ID)
                .addParams("pagenum", "1")
                .addParams("pagesize", "10")
                .addParams("param", inputStr)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        LogUtils.e(TAG, "onError e=" + e);
                        lv_order_list.stopRefresh();
                        netError();
                    }

                    @Override
                    public void onResponse(String response) {
                        LogUtils.Log("未支付订单列表返回：response=" + response);
                        boolean b = Utils.checkSaveToken(mActivity, response);
                        ll_loading.setVisibility(View.GONE);
                        if (b) {
                            if(functionNormal()) {
                                LogUtils.Log("1");
                                parseJson(response);
                            }/*else {
                                initData();
                            }*/
                        }
                    }
                });
    }

    private void parseJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            int code = jsonObject.getInt("code");
            Gson gson = new Gson();
            if (code == 200) {//返回成功
                LogUtils.Log("2");
                ll_unpayorder_num.setVisibility(View.VISIBLE);
                unpay_order_num = jsonObject.getString("num");
                if (isGetMore) {
                    isGetMore = false;
                    LogUtils.e(TAG,"test22 isGetMore");
                    UnPayBean unPayBean = gson.fromJson(json, UnPayBean.class);
                    List<UnPayBean.MsgBean> moreOrderLists = unPayBean.getMsg();
                    lv_order_list.stopLoadMore();
                    if (moreOrderLists != null && moreOrderLists.size() > 0) {
                        pagerNum++;
                        unPayBeanLists.addAll(moreOrderLists);
                        unPayAdapter.notifyDataSetChanged();
                    }
                } else {
                    LogUtils.e(TAG, "test22 notisGetMore  isRefresh=="+isRefresh);
                    if (isRefresh) {
                        isRefresh = false;
                        lv_order_list.stopRefresh();
                        unPayBeans = gson.fromJson(json, UnPayBean.class);
                        unPayBeanLists = unPayBeans.getMsg();
                    } else {
                        unPayBeans = gson.fromJson(json, UnPayBean.class);
                        unPayBeanLists = unPayBeans.getMsg();
                    }
                }
                netErrorDismiss();
                showData();
            } else if (code == 10113) {//没有数据
                ll_unpayorder_num.setVisibility(View.GONE);
                LogUtils.e(TAG,"test22 没有数据");
                if (isGetMore) {
                    LogUtils.e(TAG,"test22 isGetMore没有数据");
                    isGetMore = false;
                    //lv_order_list.noMoreData();
                    lv_order_list.stopLoadMore();
                }
                if (isRefresh) {
                    LogUtils.e(TAG,"test22 isRefresh没有数据");
                    isRefresh = false;
                    lv_order_list.stopRefresh();
                }
                if (isSearch) {
                    LogUtils.e(TAG,"test22 isSearch没有数据");
                    if(lv_order_list.getVisibility()==View.VISIBLE) {
                        lv_order_list.setVisibility(View.GONE);
                    }
                    if(ll_search_fail.getVisibility()==View.GONE) {
                        ll_search_fail.setVisibility(View.VISIBLE);
                        tv_search_fail.setVisibility(View.VISIBLE);
                        tv_search_fail_desc.setVisibility(View.GONE);
                    }

                }
                if(!isSearch&&!isRefresh&&!isGetMore){
                    noNotPaidOrder();
                }
            } else if(code == 10112){//上拉加载更多的时候没有数据
                LogUtils.e(TAG, "test22 10112上拉加载更多的时候没有数据");
                Utils.showToast(mActivity, "没有更多数据了");
                isGetMore = false;
                //lv_order_list.noMoreData();
                lv_order_list.stopLoadMore();

                //unPayAdapter.notifyDataSetChanged();
            }else{
                ll_unpayorder_num.setVisibility(View.GONE);
                noData();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void showData() {
        unpay_order_total.setText(unpay_order_num);
        ll_search_fail.setVisibility(View.GONE);
        tv_search_fail.setVisibility(View.GONE);
        tv_search_fail_desc.setVisibility(View.GONE);
        lv_order_list.setVisibility(View.VISIBLE);
        if(unPayAdapter==null) {
          unPayAdapter = new UnPayAdapter(mActivity);
        }
        if (isGetMore) {
            isGetMore = false;
            unPayAdapter.notifyDataSetChanged();
        } else {
            unPayAdapter.setData(unPayBeanLists);
            lv_order_list.setAdapter(unPayAdapter);
        }

        int count = 0;
        for (int i = 0; i <unPayBeanLists.size(); i++) {
            count += unPayBeanLists.get(i).getOrderlist().size();
        }

        if (count < 6) {
            lv_order_list.setPullLoadEnable(false);
        } else {
            lv_order_list.setPullLoadEnable(true);
        }
    }

    public void reLoad() {
        tv_loading.setText("拼命加载中...");
        pb_loading.setVisibility(View.VISIBLE);
        iv_jiazai_filure.setVisibility(View.GONE);
    }

    public void noData() {
        tv_loading.setText("没有数据，点击屏幕重新加载！");
        pb_loading.setVisibility(View.GONE);
        iv_jiazai_filure.setVisibility(View.GONE);
    }

    /**
     *  偶发崩溃
     */
    public void noNotPaidOrder() {
        LogUtils.e(TAG, "noNotPaidOrder()");
        if(ll_search_fail!=null){
            ll_search_fail.setVisibility(View.VISIBLE);
        }
        if(tv_search_fail!=null){
            tv_search_fail.setVisibility(View.VISIBLE);
            tv_search_fail.setText("尚无未结账订单");
        }
        if(tv_search_fail_desc!=null){
            tv_search_fail_desc.setVisibility(View.VISIBLE);
            tv_search_fail_desc.setText("已结账订单请至 “管理-订单管理-收银机订单统计” 中查看");
        }
        if(lv_order_list!=null){
            lv_order_list.setVisibility(View.GONE);
        }
        if(ll_loading!=null){
            ll_loading.setVisibility(View.GONE);
        }
    }

    public void netError() {
        LogUtils.e(TAG,"netError()");
        iv_jiazai_filure.setVisibility(View.VISIBLE);
        iv_jiazai_filure.setImageResource(R.drawable.net_error);
        pb_loading.setVisibility(View.GONE);
        tv_loading.setText("请检查网络后，点击屏幕重新加载！");
    }

    public void netErrorDismiss() {
        LogUtils.Log("orderpager 加载页面消失");
        ll_loading.setVisibility(View.GONE);
    }

    class SearchInputWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          /*  lv_order_list.setVisibility(View.VISIBLE);
            tv_search_fail.setVisibility(View.GONE);*/
            LogUtils.e(TAG, "beforeTextChanged");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            LogUtils.e(TAG, "onTextChanged");
        }

        @Override
        public void afterTextChanged(Editable s) {
            isSearch = true;
            LogUtils.e(TAG, "afterTextChanged() 联网请求");
            String currentTime = Utils.getCurrentTime();
            String token = Utils.getToken(currentTime, mActivity);
            inputStr = s.toString().toLowerCase();
            OkHttpUtils.post()
                    .url(UrlConstance.UNPAY_ORDER_LISTS)
                    .addParams(Constant.PARAMS_NAME_POSTOKEN, token)
                    .addParams(Constant.PARAMS_NAME_TIMEAUTH, currentTime)
                    .addParams(Constant.PARAMS_NAME_USERACCOUNT, Constant.USER_ACCOUNT)
                    .addParams(Constant.PARAMS_NAME_IMEI, Constant.DEVICE_IMEI)
                    .addParams(Constant.PARAMS_NAME_DEVICENAME, Constant.DEVICE_NAME)
                    .addParams(Constant.PARAMS_NAME_MODEL, Constant.DEVICE_MODEL)
                    .addParams(Constant.PARAMS_NEME_PASSDORD_MD5, MD5.md5(MD5.md5(Constant.PASSWORD)))
                    .addParams("storeid", Constant.STORE_ID)
                    .addParams("param", inputStr)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                        }

                        @Override
                        public void onResponse(String response) {
                            LogUtils.e(TAG, "搜索：response=" + response);

                            boolean b = Utils.checkSaveToken(mActivity, response);
                            if (b) {
                                parseJson(response);
                            }
                        }
                    });
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        /*    case btn_upgrade_now:
                Intent intent = new Intent(mActivity, UpgradeAccountActivity.class);
                ChargeItem payItem = new ChargeItem("60", "160", "280", "480", "mdp");
                intent.putExtra("pay_item", payItem);
                mActivity.startActivity(intent);
                break;*/
            case R.id.ll_create_order:
                LogUtils.e(TAG, "R.id.ll_create_order");
                if (et_order_search.hasFocus()) {
                    et_order_search.clearFocus();
                    et_order_search.setText("");
                    tv_create_new_order.setText(" 创建订单");
                    ll_create_order.requestFocus();
                    InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(),0);
                } else {
                    if (!canClick) {//增加点击限制
                        canClick = true;
                        isSearch = false;
                        clearChooseData();
                        if(Utils.isNetworkAvailable(mActivity)){
                            Utils.showWaittingDialog(mActivity,"正在创建订单...");
                            queryRoomsGoods();
                        }else{
                            canClick = false;
                            Utils.showToast(mActivity, "网络异常请，检查网络后再试");
                        }
                    }
                }
                break;
        }
    }

    /**
     * 清除所选信息
     */
    private void clearChooseData() {
        Constant.order_price = 0.00;
        Constant.order_goods_num = 0.00;
        Constant.localOrderBean.setGoodList(null);
    }

    /**
     * 查询是否
     */
    private void queryRoomsGoods() {

        LogUtils.e(TAG, "OkHttpUtils.post()：查询房间商品");
        LogUtils.e(TAG, "Constant.STORE_ID=" + Constant.STORE_ID);
        String currentTime = Utils.getCurrentTime();
        String token = Utils.getToken(currentTime, mActivity);
        OkHttpUtils.post()
                .url(UrlConstance.QUERRY_GOODS_ROOM)
                .addParams(Constant.PARAMS_NAME_POSTOKEN, token)
                .addParams(Constant.PARAMS_NAME_TIMEAUTH, currentTime)
                .addParams(Constant.PARAMS_NAME_USERACCOUNT, Constant.USER_ACCOUNT)
                .addParams(Constant.PARAMS_NAME_IMEI, Constant.DEVICE_IMEI)
                .addParams(Constant.PARAMS_NAME_DEVICENAME, Constant.DEVICE_NAME)
                .addParams(Constant.PARAMS_NAME_MODEL, Constant.DEVICE_MODEL)
                .addParams(Constant.PARAMS_NEME_PASSDORD_MD5, MD5.md5(MD5.md5(Constant.PASSWORD)))//密码
                .addParams("storeid", Constant.STORE_ID)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        canClick = false;
                        Utils.showToast(mActivity, "获取数据失败，请重试或检查网络！");
                        Utils.dismissWaittingDialog();
                    }

                    @Override
                    public void onResponse(String response) {
                        LogUtils.e(TAG, "点击创建订单 time2=" + Utils.getCurrentTimeMill());
                        LogUtils.e(TAG, "查询房间信息response:" + response);
                        boolean b = Utils.checkSaveToken(mActivity, response);
                        Utils.dismissWaittingDialog();
                        LogUtils.e(TAG, "b===" + b);
                        if (b) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                int code = jsonObject.getInt("code");
                                LogUtils.e(TAG, "code==" + code);
                                if (code == 200) {
                                    QuerryGoodsRoomBean querryGoodsRoomBean = new Gson().fromJson(response, QuerryGoodsRoomBean.class);
                                    //改：0有房间有商品 1有房间无商品 2无房间有商品
                                    LogUtils.e(TAG, "querryGoodsRoomBean.getMsg().getFlag()=" + querryGoodsRoomBean.getMsg().getFlag());
                                    setChoosedRoom(querryGoodsRoomBean.getMsg().getDay_fee(), querryGoodsRoomBean.getMsg().getHour_fee(), querryGoodsRoomBean.getMsg().getNo_fee());
                                    if (querryGoodsRoomBean.getMsg().getFlag() == 0) {
                                        startNextActivity(0);
                                    } else if (querryGoodsRoomBean.getMsg().getFlag() == 1) {
                                        startNextActivity(1);
                                    } else if (querryGoodsRoomBean.getMsg().getFlag() == 2) {
                                        startNextActivity(2);
                                    } else {
                                        showWaittingPopWindow();
                                    }
                                } else {
                                    Utils.showToast(mActivity, "未获取房间数据,请重试！");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                LogUtils.e(TAG, "JSONException e=" + e);
                            }
                        }
                    }
                });
    }

    /**
     * 设置默认选中房间
     * @param day_fee
     * @param hour_fee
     * @param no_fee
     */
    private void setChoosedRoom(int day_fee, int hour_fee, int no_fee) {
        if(SpUtils.getInstance(mActivity).get("charge_model",-1)>=0){
            LogUtils.e(TAG,"setChoosedRoom="+SpUtils.getInstance(mActivity).get("charge_model",-1));
            return;
        }
        if(no_fee==1){
            SpUtils.getInstance(mActivity).save("charge_model", 0);
            LogUtils.e(TAG,"setChoosedRoom no_fee==1");
            return;
        }
        if(hour_fee==1){
            SpUtils.getInstance(mActivity).save("charge_model", 1);
            LogUtils.e(TAG, "setChoosedRoom hour_fee==1");
            return;
        }
        if(day_fee==1){
            SpUtils.getInstance(mActivity).save("charge_model", 2);
            LogUtils.e(TAG, "setChoosedRoom day_fee==1");
            return;
        }
    }

    /**
     * 进入下一个Activity
     */
    private void startNextActivity(int flag) {//改：0有房间有商品 1有房间无商品 2无房间有商品
        Constant.NOT_CHOOSE_ROOM = true;
        Intent intent;
        LogUtils.e(TAG, "点击创建订单 time3="+Utils.getCurrentTimeMill());
        if (flag == 0) {//0有房间有商品 1有房间无商品 2无房间有商品
            //进入房间选择页面
            LogUtils.e(TAG, "有房间：进入房间选择页面");
            intent = new Intent(mActivity, NewOrderActivity.class);
            intent.putExtra("title_name", "选择房间");
            intent.putExtra(Constant.FROME_WHERE, Constant.NEW_ORDER_TO_DETAIL);
            mActivity.startActivity(intent);
        } else if (flag == 1) {
            //进入房间选择页面
            LogUtils.e(TAG, "有房间：进入房间选择页面");
            intent = new Intent(mActivity, NewOrderActivity.class);
            intent.putExtra("title_name", "选择房间");
            intent.putExtra(Constant.FROME_WHERE, Constant.JUST_HAS_ROOM_NO_GOODS);
            mActivity.startActivity(intent);
        } else if (flag == 2) {
            //跳过房间直接进入选择商品页面
            String trade_num = Utils.getTradeNum();
            LogUtils.e(TAG, "没有房间有商品：跳过房间直接进入选择商品页面");
            intent = new Intent(mActivity, AddServiceGoodActivity.class);
            intent.putExtra("title_name", "选择商品");
            intent.putExtra("trade_num", trade_num);
            intent.putExtra("from_where", Constant.ADD_FROM_NORMAl_NUM);
            mActivity.startActivity(intent);
        }
        LogUtils.e(TAG, "点击创建订单 time4="+Utils.getCurrentTimeMill());
        canClick = false;
    }

    /**
     * 显示提示的PopWindow
     */
    private void showWaittingPopWindow() {
        canClick = false;
        final LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.pop_prompt_set_goods_layout, null);
        Button btn_confirm = (Button) view.findViewById(R.id.btn_confirm);
        final PopupWindow promptPop = new PopupWindow(view, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        promptPop.setOutsideTouchable(false);
        promptPop.setFocusable(true);
        WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
        params.alpha = 0.7f;
        mActivity.getWindow().setAttributes(params);
        promptPop.showAtLocation(mActivity.findViewById(R.id.ly_order), Gravity.CENTER, 0, 0);//设置为取景框中间
        btn_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
                params.alpha = 1f;
                mActivity.getWindow().setAttributes(params);
                promptPop.dismiss();
            }
        });
    }
}
