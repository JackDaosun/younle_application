package com.younle.younle624.myapplication.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.younle.younle624.myapplication.R;
import com.younle.younle624.myapplication.activity.createorder.AddServiceGoodActivity;
import com.younle.younle624.myapplication.constant.Constant;
import com.younle.younle624.myapplication.domain.GoodBean;
import com.younle.younle624.myapplication.utils.LogUtils;
import com.younle.younle624.myapplication.utils.Utils;
import com.younle.younle624.myapplication.view.FlowLayout;

import java.util.ArrayList;
import java.util.List;

import static com.younle.younle624.myapplication.R.id.member_price_logo;
import static com.younle.younle624.myapplication.constant.Constant.localOrderBean;

/**
 * Created by 我是奋斗 on 2016/6/30.
 * 微信/e-mail:tt090423@126.com
 * <p/>
 * 下单页面添加商品/服务界面右侧详细内容的adapter
 * 显示数据的操作
 */
public class GoodServiceAdapter<T> extends BaseAdapter {

    private static final String TAG = "GoodServiceAdapter<T>";
    private boolean ever_set_weigher;
    private TextView tv_gw_num;
    private ImageView iv_gw;
    private RelativeLayout rl_all_Interface_container;
    private Handler handler;
    private Activity context;
    private ViewHolder holder;
    private List<T> data;
    private List<GoodBean> entityGoodsList;
    private ViewGroup anim_mask_layout;//动画层
    private ImageView buyImg;
    /*private PathMeasure mPathMeasure;
    private float[] mCurrentPosition = new float[2];*/
    private PopupWindow multiPopupWindow;
    private com.younle.younle624.myapplication.view.FlowLayout fl_multi;
    private TextView tv_multi_price;
    private TextView tv_stock_text;
    private TextView tv_stock_num;
    //private TextView tv_tight_inventory_pop;
    private EditText et_goods_num;
    private ImageView btn_minus;
    private ImageView btn_add;
    private RelativeLayout rl_choose_multi_goods;
    //private RelativeLayout rl_add_reduce;
    private LinearLayout ll_pop_continer;
    private int pos_which_goods = 0;//商品的position
    private int pos_multi_goods = 0;//商品下多规格商品的position
    private boolean onclick = false;
    private List<GoodBean.SizeListBean> multiGoodsList;
    private String multiGoodsId = "";//多规格商品id
    private int last_pos = -1;//上一次点击位置
    private int last_mulit_pos = -1;//上一次多规格点击位置
    private int last_localdata_pos = -1;//上一次多规格点击位置
    private AddServiceGoodActivity.OnWeighGoodChangeListener weighListener;
    private int selectDgg;
    private View muiltPopView;
    private boolean showMemberPrice;
    private ImageView muilt_member_price_logo;

    public GoodServiceAdapter(Activity context, Handler handler, RelativeLayout rl_all_Interface_container, ImageView iv_gw, TextView tv_gw_num) {
        this.context = context;
        this.handler = handler;
        this.rl_all_Interface_container = rl_all_Interface_container;
        this.iv_gw = iv_gw;
        this.tv_gw_num = tv_gw_num;
    }
    public void setEverWeigher(boolean ever_set_weigher){
        this.ever_set_weigher=ever_set_weigher;
    }
    public void setData(List<T> data) {
        this.data = data;
        entityGoodsList = (List<GoodBean>) this.data;
        last_pos = -1;
        last_mulit_pos = -1;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = View.inflate(context, R.layout.user_detail_item_new, null);
            holder.tv_order_name = (TextView) convertView.findViewById(R.id.tv_order_name);//商品名称
            holder.ll_stock = (LinearLayout) convertView.findViewById(R.id.ll_stock);//库存
            holder.tv_tight_inventory = (TextView) convertView.findViewById(R.id.tv_tight_inventory);//库存
            holder.tv_order_stock = (TextView) convertView.findViewById(R.id.tv_order_stock);//库存
            holder.tv_devide_line = (TextView) convertView.findViewById(R.id.tv_devide_line);//库存
            holder.tv_order_unit = (TextView) convertView.findViewById(R.id.tv_order_unit);//库存
            holder.tv_order_price = (TextView) convertView.findViewById(R.id.tv_order_price);
            holder.btn_add = (ImageView) convertView.findViewById(R.id.btn_add);
            holder.btn_minus = (ImageView) convertView.findViewById(R.id.btn_minus);
            holder.tv_goods_num = (TextView) convertView.findViewById(R.id.tv_goods_num);
            holder.rl_mulity_explain = (LinearLayout) convertView.findViewById(R.id.ll_mulity_explain);
            holder.tv_middle_level = (TextView) convertView.findViewById(R.id.tv_middle_level);
            holder.member_price_logo= (ImageView) convertView.findViewById(member_price_logo);
            holder.tv_orign_price = (TextView) convertView.findViewById(R.id.tv_orign_price);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        GoodBean goodsListBean = entityGoodsList.get(position);
        holder.tv_order_name.setText(String.valueOf(goodsListBean.getGoodsName()));
        if(goodsListBean.getHasStock()==1) {
            holder.tv_middle_level.setVisibility(View.VISIBLE);
            holder.ll_stock.setVisibility(View.VISIBLE);
            holder.tv_order_stock.setText(Utils.formatPrice(Double.valueOf(goodsListBean.getStock())));
            if(goodsListBean.getStockWarning()==1){
                holder.tv_tight_inventory.setVisibility(View.VISIBLE);
            }else{
                holder.tv_tight_inventory.setVisibility(View.GONE);
            }
        }else{
            holder.tv_middle_level.setVisibility(View.GONE);
            holder.ll_stock.setVisibility(View.GONE);
        }
        if(goodsListBean.getGoodsUnit()!=null&&!"".equals(goodsListBean.getGoodsUnit())){
            holder.tv_devide_line.setVisibility(View.VISIBLE);
            holder.tv_order_unit.setVisibility(View.VISIBLE);
            holder.tv_order_unit.setText(goodsListBean.getGoodsUnit());
        }else{
            holder.tv_devide_line.setVisibility(View.GONE);
            holder.tv_order_unit.setVisibility(View.GONE);
        }
        if(goodsListBean.getGoodsList()!=null&&goodsListBean.getGoodsList().size()>0){//多规格
            double all_num = 0;
            double maxPrice;
            double minPrice;
            boolean compareVip;
            if(showMemberPrice&&goodsListBean.getGoodsList().get(0).getVipPrice()>=0) {
                holder.member_price_logo.setVisibility(View.GONE);//多规格外层不显示会员
                compareVip=true;
                 maxPrice = goodsListBean.getGoodsList().get(0).getVipPrice();
                 minPrice = goodsListBean.getGoodsList().get(0).getVipPrice();
            }else {
                holder.member_price_logo.setVisibility(View.GONE);
                compareVip=false;
                 maxPrice = goodsListBean.getGoodsList().get(0).getSizePrice();
                 minPrice = goodsListBean.getGoodsList().get(0).getSizePrice();
            }
            for(int i=0;i<goodsListBean.getGoodsList().size();i++){
                all_num += goodsListBean.getGoodsList().get(i).getSizeNum();
                double sizePrice;
                if(compareVip) {
                    sizePrice = goodsListBean.getGoodsList().get(i).getVipPrice();
                }else {
                     sizePrice = goodsListBean.getGoodsList().get(i).getSizePrice();
                }
                if(minPrice > sizePrice){
                    minPrice = sizePrice;
                }
                if(maxPrice < sizePrice){
                    maxPrice = sizePrice;
                }
            }
            holder.tv_goods_num.setText(Utils.formatPrice(all_num));
            holder.tv_goods_num.setOnClickListener(null);
            holder.rl_mulity_explain.setVisibility(View.VISIBLE);
            if(maxPrice > minPrice){
                String s = Utils.keepTwoDecimal(minPrice + "") + "~" + Utils.keepTwoDecimal(maxPrice + "");
                if(s.length()>9){
                    String s1 = Utils.keepTwoDecimal(minPrice + "") + "\n~" + Utils.keepTwoDecimal(maxPrice + "");
                    holder.tv_order_price.setText("￥"+s1);
                    holder.tv_middle_level.setVisibility(View.VISIBLE);
                }else{
                    holder.tv_middle_level.setVisibility(View.GONE);
                    holder.tv_order_price.setText(s);
                }
            }else{
                holder.tv_middle_level.setVisibility(View.GONE);
                holder.tv_order_price.setText(""+Utils.keepTwoDecimal(maxPrice+""));
            }
        }else{
            holder.rl_mulity_explain.setVisibility(View.GONE);
            holder.tv_goods_num.setText(Utils.formatPrice(goodsListBean.getGoodsNum()));
            holder.tv_goods_num.setOnClickListener(new EditTextOnClickListener(context,position,goodsListBean));

            double vipPrice = goodsListBean.getVipPrice();
            if(showMemberPrice &&vipPrice>=0) {
                //显示会员价
                holder.member_price_logo.setVisibility(View.VISIBLE);
                holder.tv_order_price.setText(String.valueOf(Utils.keepTwoDecimal(""+goodsListBean.getVipPrice())));
            }else {
                //不显示会员价
                holder.member_price_logo.setVisibility(View.GONE);
                holder.tv_order_price.setText(String.valueOf(Utils.keepTwoDecimal(""+goodsListBean.getGoodsPrice())));
            }

        }
        int isVisible = goodsListBean.getGoodsNum() <= 0?View.GONE:View.VISIBLE;
        holder.tv_goods_num.setVisibility(isVisible);
        holder.btn_minus.setVisibility(isVisible);
        setBtnState(goodsListBean,null,holder.btn_minus,holder.btn_add);
        holder.btn_add.setOnClickListener(new AddMinusOnClickListener(position));
        holder.btn_minus.setOnClickListener(new AddMinusOnClickListener(position));
        return convertView;
    }

    private void setBtnState(GoodBean goodsListBean, GoodBean.SizeListBean sizeListBean,ImageView btn_minus, ImageView btn_add) {
        if("0".equals(goodsListBean.getIs_weigh())||!ever_set_weigher) {
            btn_minus.setImageResource(R.drawable.raduce_party_icon);
            btn_add.setImageResource(R.drawable.add_party_icon);
        }else {//称重商品
            if(sizeListBean!=null) {//多规格
                if(sizeListBean.getSizeNum()>0) {
                    btn_minus.setImageResource(R.drawable.weigh_del);
                    btn_add.setImageResource(R.drawable.add_party_icon);
                }else {
                    btn_add.setImageResource(R.drawable.weigh);
                }
            }else {
                btn_minus.setImageResource(R.drawable.weigh_del);
                if(goodsListBean.getGoodsNum()>0) {
                    btn_add.setImageResource(R.drawable.add_party_icon);
                }else {
                    btn_add.setImageResource(R.drawable.weigh);
                }
            }
        }
    }

    public void setWeighListener(AddServiceGoodActivity.OnWeighGoodChangeListener weighListener) {
        this.weighListener = weighListener;
    }

    public void setShowMemberPrice(boolean showMemberPrice) {
        this.showMemberPrice = showMemberPrice;
    }

    class EditTextOnClickListener implements View.OnClickListener{

        private int position;
        private double pop_goods_num;
        private Activity mActivity;
        private GoodBean goodsListBean;

        public EditTextOnClickListener(Activity mActivity,int position,GoodBean goodsListBean){
            this.position = position;
            this.mActivity = mActivity;
            this.goodsListBean = goodsListBean;
        }

        @Override
        public void onClick(View v) {
            final LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View popView = inflater.inflate(R.layout.pop_edit_goods_num, null);
            ImageView btn_minus = (ImageView) popView.findViewById(R.id.btn_minus);
            final EditText et_goods_num = (EditText) popView.findViewById(R.id.et_goods_num);
            pop_goods_num = goodsListBean.getGoodsNum();
            et_goods_num.setText(String.valueOf(pop_goods_num));
            ImageView btn_add = (ImageView) popView.findViewById(R.id.btn_add);
            TextView tv_return = (TextView) popView.findViewById(R.id.tv_return);
            TextView tv_determine = (TextView) popView.findViewById(R.id.tv_determine);
            popView.setFocusableInTouchMode(true);
            final PopupWindow popupWindow = new PopupWindow(popView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            setAlpha(0.6f);
            popupWindow.setBackgroundDrawable(new BitmapDrawable());
            popupWindow.setOutsideTouchable(false);
            popupWindow.setFocusable(true);
            popupWindow.showAtLocation(mActivity.findViewById(R.id.add_service_good), Gravity.CENTER, 0, 0);
            popupWindow.update();
            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    setAlpha(1);
                }
            });
            btn_minus.setOnClickListener(new View.OnClickListener() {//减少商品
                @Override
                public void onClick(View v) {
                    String str_goods_num = et_goods_num.getText().toString();
                    if(!"".equals(str_goods_num)&&!".".equals(str_goods_num)&&Double.valueOf(str_goods_num)>=0){
                        if(pop_goods_num != Double.valueOf(str_goods_num)){
                            pop_goods_num = Double.valueOf(str_goods_num);
                            double goodsNum = goodsListBean.getGoodsNum();
                            double dif = pop_goods_num - goodsNum;
                            goodsListBean.setGoodsNum(pop_goods_num);
                            if(showMemberPrice&&goodsListBean.getVipPrice()>=0) {
                                UpdatePriceNum(dif,goodsListBean.getVipPrice(),1);
                            }else {
                                UpdatePriceNum(dif,goodsListBean.getGoodsPrice(),1);
                            }
                        }
                        if(pop_goods_num>0){
                            if(pop_goods_num>0&&pop_goods_num<1){
                                pop_goods_num = 0;
                                et_goods_num.setText("0");
                            }else{
                                pop_goods_num--;
                                et_goods_num.setText(String.valueOf(pop_goods_num));
                            }
                            minus(position,0,-1);
                        }else{
                            Toast.makeText(mActivity,"商品数量已经减少到0！",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(mActivity,"商品数量输入有误！",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            btn_add.setOnClickListener(new View.OnClickListener() {//增加商品
                @Override
                public void onClick(View v) {
                    String str_goods_num = et_goods_num.getText().toString();
                    if(!"".equals(str_goods_num)&&!".".equals(str_goods_num)&&Double.valueOf(str_goods_num)>=0){
                        if(pop_goods_num != Double.valueOf(str_goods_num)){
                            pop_goods_num = Double.valueOf(str_goods_num);
                            double goodsNum = goodsListBean.getGoodsNum();
                            double dif = pop_goods_num - goodsNum;
                            goodsListBean.setGoodsNum(pop_goods_num);
                            if(showMemberPrice&&goodsListBean.getVipPrice()>=0) {
                                UpdatePriceNum(dif,Double.valueOf(goodsListBean.getVipPrice()),1);
                            }else {
                                UpdatePriceNum(dif,goodsListBean.getGoodsPrice(),1);
                            }
                        }
                        pop_goods_num++;
                        et_goods_num.setText(String.valueOf(pop_goods_num));
                        add(position, entityGoodsList.get(position).getGoodsId(), localOrderBean.getGoodList(),0);
                    }else{
                        Toast.makeText(mActivity,"商品数量输入有误！",Toast.LENGTH_SHORT).show();
                    }
                }
            });
            tv_return.setOnClickListener(new View.OnClickListener() {//返回
                @Override
                public void onClick(View v) {
                    if(popupWindow!=null){
                        popupWindow.dismiss();
                    }
                }
            });
            tv_determine.setOnClickListener(new View.OnClickListener() {//确定
                @Override
                public void onClick(View v) {
                    //存储商品到本地订单；更新显示数据
                    String str_goods_num = et_goods_num.getText().toString();
                    if("".equals(str_goods_num)||".".equals(str_goods_num)||Double.valueOf(str_goods_num)<0){
                        Toast.makeText(mActivity,"商品数量输入有误！",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //设置同步显示
                    double goodsNum = goodsListBean.getGoodsNum();
                    double dif = Double.valueOf(str_goods_num) - goodsNum;
                    goodsListBean.setGoodsNum(Double.valueOf(str_goods_num));

                    if(showMemberPrice&&goodsListBean.getVipPrice()>=0) {
                        UpdatePriceNum(dif,Double.valueOf(goodsListBean.getVipPrice()),1);
                    }else {
                        UpdatePriceNum(dif,goodsListBean.getGoodsPrice(),1);
                    }
                    if(popupWindow!=null){
                        popupWindow.dismiss();
                    }
                }
            });
        }

        private void setAlpha(float alpha) {
            WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
            params.alpha = alpha;
            mActivity.getWindow().setAttributes(params);
        }
    }

    /**
     * 添加或减少的监听，外层监听
     */
    class AddMinusOnClickListener implements View.OnClickListener {

        private int position;
        public AddMinusOnClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            ((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            switch (v.getId()) {
                case R.id.btn_add:
                    LogUtils.e(TAG,"点击之前的记录位置信息：last_pos="+last_pos+",last_mulit_pos="+last_mulit_pos);
                    //获取sizeList长度是否大于1
                    multiGoodsList = entityGoodsList.get(position).getGoodsList();
                    multiGoodsId = entityGoodsList.get(position).getGoodsId();
                    if(multiGoodsList!=null&&multiGoodsList.size()>0){
                        //弹出多规格弹框
                        pos_which_goods = position;
                        pos_multi_goods = 0;
                        last_mulit_pos = -1;
                        LogUtils.e(TAG,"btn_add pos_which_goods="+pos_which_goods);
                        showMultiSpecfication(entityGoodsList.get(position),position);
                    }else{
                        String is_weigh = entityGoodsList.get(position).getIs_weigh();
                        if("0".equals(is_weigh)||!ever_set_weigher) {//不需要称重
                          pos_which_goods = position;
                          add(position, entityGoodsList.get(position).getGoodsId(), localOrderBean.getGoodList(),0);
                          startAnimation(v);
                        }else {//需要称重
                            if(weighListener !=null) {
                                weighListener.weigh(entityGoodsList.get(position),-1);
                            }
                        }
                    }
                    break;
                case R.id.btn_minus:
                    multiGoodsList = entityGoodsList.get(position).getGoodsList();
                    multiGoodsId = entityGoodsList.get(position).getGoodsId();
                    if(multiGoodsList!=null&&multiGoodsList.size()>0){
                        //弹出多规格弹框
                        pos_which_goods = position;
                        pos_multi_goods = 0;
                        last_mulit_pos = -1;
                        LogUtils.e(TAG,"minus pos_which_goods="+pos_which_goods);
                        showMultiSpecfication(entityGoodsList.get(position),position);
                    }else{
                        minus(position,0,-1);
                    }
                    break;
            }
        }
    }

    /**
     * 移除
     * @param position
     */
    private void minus(int position,int tag,int wcPosition) {
        double changeNum=0;
        switch (tag){
            case 0://没有多规格
                for (int i = 0; i < localOrderBean.getGoodList().size(); i++) {
                    if (entityGoodsList.get(position).getGoodsId().equals(localOrderBean.getGoodList().get(i).getGoodsId())) {
                        double num = localOrderBean.getGoodList().get(i).getGoodsNum();
                        boolean not_weigh = "0".equals(localOrderBean.getGoodList().get(i).getIs_weigh());
                        changeNum=(not_weigh||!ever_set_weigher)?1:localOrderBean.getGoodList().get(i).getGoodsNum();
                        if (num > 1&&(not_weigh||!ever_set_weigher)) {//非称重
                            localOrderBean.getGoodList().get(i).setGoodsNum(num - 1);
                            entityGoodsList.get(position).setGoodsNum(num - 1);
                        } else {
                            //隐藏
                            localOrderBean.getGoodList().remove(i);
                            entityGoodsList.get(position).setGoodsNum(0);
                        }
                        if(showMemberPrice&&entityGoodsList.get(position).getVipPrice()>=0) {
                            UpdatePriceNum(-changeNum,-entityGoodsList.get(position).getVipPrice()*changeNum,0);
                        }else {
                            UpdatePriceNum(-changeNum,-entityGoodsList.get(position).getGoodsPrice()*changeNum,0);
                        }
                        return;
                    }
                }
                break;
            case 1://有多规格 //确定是多规格商品
                LogUtils.e(TAG,"多规格点击减少");
                OUT:
                for(int i = 0; i< localOrderBean.getGoodList().size(); i++){
                    if(localOrderBean.getGoodList().get(i).getGoodsList()!=null&& localOrderBean.getGoodList().get(i).getGoodsList().size()>0){
                        for(int j = 0; j< localOrderBean.getGoodList().get(i).getGoodsList().size(); j++){
                            if(multiGoodsId.equals(localOrderBean.getGoodList().get(i).getGoodsList().get(j).getSizeId())){
                                double totalGoodsNum = localOrderBean.getGoodList().get(i).getGoodsNum();
                                double multiSizeNum = localOrderBean.getGoodList().get(i).getGoodsList().get(j).getSizeNum();
                                GoodBean goodBean = entityGoodsList.get(pos_which_goods);
                                GoodBean.SizeListBean sizeListBean = goodBean.getGoodsList().get(pos_multi_goods);
                                double sizeNum = sizeListBean.getSizeNum();
                                boolean not_weigh = "0".equals(goodBean.getIs_weigh());
                                changeNum=(not_weigh||!ever_set_weigher)?1:sizeListBean.getSizeNum();
                                if(totalGoodsNum>1){
                                    if(not_weigh||!ever_set_weigher) {
                                        goodBean.setGoodsNum(totalGoodsNum - 1);
                                        localOrderBean.getGoodList().get(i).setGoodsNum(totalGoodsNum - 1);
                                    }else{
                                        goodBean.setGoodsNum(totalGoodsNum- sizeNum);
                                        localOrderBean.getGoodList().get(i).setGoodsNum(totalGoodsNum- sizeNum);
                                    }
                                }else {
                                    goodBean.setGoodsNum(0);
                                    localOrderBean.getGoodList().get(i).setGoodsNum(0);
                                }

                                if(multiSizeNum > 1&&(not_weigh||!ever_set_weigher)){
                                    sizeListBean.setSizeNum(multiSizeNum - 1);
                                    localOrderBean.getGoodList().get(i).getGoodsList().get(j).setSizeNum(multiSizeNum - 1);
                                    et_goods_num.setText("" + localOrderBean.getGoodList().get(i).getGoodsList().get(j).getSizeNum());
                                }else{
                                    sizeListBean.setSizeNum(0);
                                    localOrderBean.getGoodList().get(i).getGoodsList().get(j).setSizeNum(0);
                                    et_goods_num.setText("0");
                                    et_goods_num.setVisibility(View.GONE);
                                    btn_minus.setVisibility(View.GONE);
                                    if(!not_weigh) {
                                        btn_add.setImageResource(R.drawable.weigh);
                                    }
                                }
                                last_mulit_pos = pos_which_goods;
                                if(showMemberPrice&&sizeListBean.getVipPrice()>=0) {
                                    UpdatePriceNum(-changeNum,-sizeListBean.getVipPrice()*changeNum,0);
                                }else {
                                    UpdatePriceNum(-changeNum,-sizeListBean.getSizePrice()*changeNum,0);
                                }

                                break OUT;
                            }
                        }
                    }
                }
                break;
        }
    }

    /**
     * 添加实物或者服务
     * @param position
     */
    private void add(final int position, final String goodsId, final List<GoodBean> goodList, final int tag) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startAdd(position, goodsId, goodList, tag);
            }
        }).run();
    }

    private void startAdd(int position, String goodsId, List<GoodBean> goodList, int tag) {
        LogUtils.Log("position=="+position+" goodsId="+goodsId);
        switch (tag){
            case 0://没有多规格
                if (goodList != null) {
                    for (int i = 0; i < goodList.size(); i++) {//添加过
                        if (goodsId.equals(goodList.get(i).getGoodsId())) {
                            last_localdata_pos = i;
                            double num = goodList.get(i).getGoodsNum();
                            goodList.get(i).setGoodsNum(num + 1);
                            entityGoodsList.get(position).setGoodsNum(num + 1);
                            if(showMemberPrice&&entityGoodsList.get(position).getVipPrice()>=0) {
                                UpdatePriceNum(1,Double.valueOf(entityGoodsList.get(position).getVipPrice()),0);
                            }else {
                                UpdatePriceNum(1,entityGoodsList.get(position).getGoodsPrice(),0);
                            }
                            return;
                        }
                    }
                    //新增
                    entityGoodsList.get(position).setGoodsNum(1);
                    goodList.add(entityGoodsList.get(position));

                    if(showMemberPrice&&entityGoodsList.get(position).getVipPrice()>=0) {
                        UpdatePriceNum(1,entityGoodsList.get(position).getVipPrice(),0);
                    }else {
                        UpdatePriceNum(1,entityGoodsList.get(position).getGoodsPrice(),0);
                    }

                } else {
                    last_localdata_pos = 0;
                    entityGoodsList.get(position).setGoodsNum(1);
                    List<GoodBean> list = new ArrayList<>();
                    list.add(entityGoodsList.get(position));
                    localOrderBean.setGoodList(list);

                    if(showMemberPrice&&entityGoodsList.get(position).getVipPrice()>=0) {
                        UpdatePriceNum(1,entityGoodsList.get(position).getVipPrice(),0);
                    }else {
                        UpdatePriceNum(1,entityGoodsList.get(position).getGoodsPrice(),0);
                    }
                }
                break;
            case 1://有多规格 position为第几个位置的多规格商品 goodsId此时为sizeId
                LogUtils.e(TAG,"多规格点击add");
                if (goodList != null) {//本次点击的为多规格商品增加，且之前有添加过其他商品
                    boolean isMulti = true;
                    OUT:
                    for(int i = 0; i< localOrderBean.getGoodList().size(); i++){//遍历比较之前添加的商品：如果本次点击在之前已添加过：就直接数量增加1，如果没有添加过：新添加一个分商品
                        if(localOrderBean.getGoodList().get(i).getGoodsList()!=null&& localOrderBean.getGoodList().get(i).getGoodsList().size()>0){
                            for(int j = 0; j< localOrderBean.getGoodList().get(i).getGoodsList().size(); j++){
                                if(multiGoodsId.equals(localOrderBean.getGoodList().get(i).getGoodsList().get(j).getSizeId())){
                                    double totalGoodsNum = localOrderBean.getGoodList().get(i).getGoodsNum();
                                    double multiSizeNum = localOrderBean.getGoodList().get(i).getGoodsList().get(j).getSizeNum();
                                    for(int k=0;k<entityGoodsList.size();k++){
                                        if(entityGoodsList.get(k).getGoodsList()!=null&&entityGoodsList.get(k).getGoodsList().size()>0){
                                            for(int n=0;n<entityGoodsList.get(k).getGoodsList().size();n++){
                                                if(multiGoodsId.equals(entityGoodsList.get(k).getGoodsList().get(n).getSizeId())){
                                                    entityGoodsList.get(k).setGoodsNum(totalGoodsNum + 1);
                                                    entityGoodsList.get(k).getGoodsList().get(n).setSizeNum(multiSizeNum + 1);
                                                    localOrderBean.getGoodList().get(i).setGoodsNum(totalGoodsNum + 1);
                                                    localOrderBean.getGoodList().get(i).getGoodsList().get(j).setSizeNum(multiSizeNum + 1);
                                                    et_goods_num.setText("" + localOrderBean.getGoodList().get(i).getGoodsList().get(j).getSizeNum());//设置显示为添加过的商品数量
                                                    et_goods_num.setVisibility(View.VISIBLE);
                                                    btn_minus.setVisibility(View.VISIBLE);
                                                    last_mulit_pos = pos_multi_goods;
                                                    if(showMemberPrice&&entityGoodsList.get(k).getGoodsList().get(n).getVipPrice()>=0) {
                                                        UpdatePriceNum(1,entityGoodsList.get(k).getGoodsList().get(n).getVipPrice(),0);
                                                    }else {
                                                        UpdatePriceNum(1,entityGoodsList.get(k).getGoodsList().get(n).getSizePrice(),0);
                                                    }
                                                    isMulti = false;
                                                    break OUT;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    //之前有商品，对于没有添加过的商品：
                    if(isMulti){
                        multiGoodsList.get(pos_multi_goods).setSizeNum(1);
                        et_goods_num.setText("1.0");
                        et_goods_num.setVisibility(View.VISIBLE);
                        btn_minus.setVisibility(View.VISIBLE);
                        List<GoodBean> list = new ArrayList<>();
                        entityGoodsList.get(pos_which_goods).setGoodsNum(1);
                        entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).setSizeNum(1);
                        list.add(entityGoodsList.get(pos_which_goods));
                        localOrderBean.getGoodList().addAll(list);
                        last_mulit_pos = pos_multi_goods;
                        if(showMemberPrice&&multiGoodsList.get(pos_multi_goods).getVipPrice()>=0) {
                            UpdatePriceNum(1,multiGoodsList.get(pos_multi_goods).getVipPrice(),0);
                        }else {
                            UpdatePriceNum(1,multiGoodsList.get(pos_multi_goods).getSizePrice(),0);
                        }
                    }
                } else {//添加进第一个多规格商品
                    multiGoodsList.get(pos_multi_goods).setSizeNum(1);
                    et_goods_num.setText("1.0");
                    et_goods_num.setVisibility(View.VISIBLE);
                    btn_minus.setVisibility(View.VISIBLE);
                    entityGoodsList.get(pos_which_goods).setGoodsNum(1);
                    List<GoodBean> list = new ArrayList<>();
                    list.add(entityGoodsList.get(pos_which_goods));
                    localOrderBean.setGoodList(list);
                    last_mulit_pos = pos_multi_goods;
                    if(showMemberPrice&&multiGoodsList.get(pos_multi_goods).getVipPrice()>=0) {
                        UpdatePriceNum(1,multiGoodsList.get(pos_multi_goods).getVipPrice(),0);
                    }else {
                        UpdatePriceNum(1,multiGoodsList.get(pos_multi_goods).getSizePrice(),0);
                    }
                }
                break;
        }
    }

    /**
     * 添加至购物车的动画
     * @param v
     */
    public void startAnimation(View v) {
        int[] start_location = new int[2];
        v.getLocationInWindow(start_location);
        buyImg = new ImageView(context);
        buyImg.setImageResource(R.drawable.gouwuche_circle);
        setAnim(buyImg, start_location);
    }

    /**
     * 设置动画
     *
     * @param v
     * @param start_location
     */
    private void setAnim(final View v, int[] start_location) {

        if(rl_choose_multi_goods != null){
            rl_choose_multi_goods.addView(v);
        }else{
            anim_mask_layout = null;
            anim_mask_layout = createAnimLayout();
            anim_mask_layout.addView(v);//把动画小球添加到动画层
        }

        final View view = addViewToAnimLayout(v, start_location);
        int[] end_location = new int[2];// 这是用来存储动画结束位置的X、Y坐标
        iv_gw.getLocationInWindow(end_location);// shopCart是那个购物车

        // 计算位移
        int endX = 0 - start_location[0] + 40;// 动画位移的X坐标
        int endY = end_location[1] - start_location[1];// 动画位移的y坐标
        TranslateAnimation translateAnimationX = new TranslateAnimation(0, endX, 0, 0);
        translateAnimationX.setInterpolator(new LinearInterpolator());
        translateAnimationX.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(false);

        TranslateAnimation translateAnimationY = new TranslateAnimation(0, 0, 0, endY);
        translateAnimationY.setInterpolator(new AccelerateInterpolator());
        translateAnimationY.setRepeatCount(0);// 动画重复执行的次数
        translateAnimationX.setFillAfter(false);

        AnimationSet set = new AnimationSet(false);
        set.setFillAfter(false);
        set.addAnimation(translateAnimationY);
        set.addAnimation(translateAnimationX);
        set.setDuration(600);
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        view.startAnimation(set);
        set.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                v.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
                tv_gw_num.setVisibility(View.VISIBLE);
                view.setLayerType(View.LAYER_TYPE_NONE, null);
            }
        });
    }

    private ViewGroup createAnimLayout() {
        ViewGroup rootView = (ViewGroup) context.getWindow().getDecorView();
        RelativeLayout animLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        animLayout.setLayoutParams(lp);
        animLayout.setId(Integer.MAX_VALUE);
        animLayout.setBackgroundResource(android.R.color.transparent);
        rootView.addView(animLayout);
        return animLayout;
    }

    /**
     * 添加动画到动画层
     * @param view
     * @param location
     * @return
     */
    private View addViewToAnimLayout(final View view, int[] location) {
        int x = location[0];
        int y = location[1];
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.leftMargin = x;
        lp.topMargin = y;
        view.setLayoutParams(lp);
        return view;
    }

    /**
     * 显示多规格弹框
     */
    private void showMultiSpecfication(GoodBean goodBean, int wcPosition) {
        ((InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(context.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        muiltPopView = inflater.inflate(R.layout.choose_multi_specification, null);
        muilt_member_price_logo = (ImageView) muiltPopView.findViewById(R.id.muilt_member_price_logo);
        fl_multi = (FlowLayout) muiltPopView.findViewById(R.id.fl_multi);

        TextView tv_good_name = (TextView) muiltPopView.findViewById(R.id.tv_good_name);
        ImageView iv_click_to_close = (ImageView) muiltPopView.findViewById(R.id.iv_click_to_close);
        tv_multi_price = (TextView) muiltPopView.findViewById(R.id.tv_multi_price);
        tv_stock_text = (TextView) muiltPopView.findViewById(R.id.tv_stock_text);
        tv_stock_num = (TextView) muiltPopView.findViewById(R.id.tv_stock_num);
        et_goods_num = (EditText) muiltPopView.findViewById(R.id.et_goods_num);
        btn_minus = (ImageView) muiltPopView.findViewById(R.id.btn_minus);
        btn_add = (ImageView) muiltPopView.findViewById(R.id.btn_add);

        rl_choose_multi_goods = (RelativeLayout) muiltPopView.findViewById(R.id.rl_choose_multi_goods);
        ll_pop_continer = (LinearLayout) muiltPopView.findViewById(R.id.ll_pop_continer);

        multiPopupWindow = new PopupWindow(muiltPopView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        setAlpha(0.4f);
        multiPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        multiPopupWindow.setOutsideTouchable(false);
        multiPopupWindow.setFocusable(true);
        multiPopupWindow.showAtLocation(context.findViewById(R.id.add_service_good), Gravity.CENTER, 0, 0);
        multiPopupWindow.update();
        multiPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                rl_choose_multi_goods = null;
                setAlpha(1);
            }
        });

        //多规格显示
        int size = multiGoodsList.size();
        for (int i = 0; i < 6; i++) {
            TextView childAt = (TextView) fl_multi.getChildAt(i);
            if (i < size) {
                if (i == 0) {
                    //初始化显示
                    childAt.setSelected(true);
                    pos_multi_goods = 0;
                    multiGoodsId = multiGoodsList.get(0).getSizeId();
                    et_goods_num.setText("" + multiGoodsList.get(0).getSizeNum());
                    tv_good_name.setText(goodBean.getGoodsName());
                    if (showMemberPrice &&multiGoodsList.get(0).getVipPrice()>=0) {
                        muilt_member_price_logo.setVisibility(View.VISIBLE);
                        tv_multi_price.setText("￥" + multiGoodsList.get(0).getVipPrice());
                    }else {
                        muilt_member_price_logo.setVisibility(View.GONE);
                        tv_multi_price.setText("￥" + multiGoodsList.get(0).getSizePrice());
                    }

                    if (multiGoodsList.get(0).getHasStock() == 1) {
                        tv_stock_text.setVisibility(View.VISIBLE);
                        tv_stock_num.setVisibility(View.VISIBLE);
                        tv_stock_num.setText(multiGoodsList.get(0).getStock());
                    } else {
                        tv_stock_text.setVisibility(View.GONE);
                        tv_stock_num.setVisibility(View.GONE);
                    }
                    if (multiGoodsList.get(0).getStockWarning() == 1) {
                        tv_stock_text.setText("库存紧张：");
                        tv_stock_text.setTextColor(Color.parseColor("#ffffff"));
                        tv_stock_num.setTextColor(Color.parseColor("#ff0000"));
                        tv_stock_text.setBackgroundResource(R.drawable.discount_info_shape);
                    } else {
                        tv_stock_text.setText("库存：");
                        tv_stock_text.setTextColor(Color.parseColor("#999999"));
                        tv_stock_num.setTextColor(Color.parseColor("#999999"));
                        tv_stock_text.setBackgroundResource(R.drawable.discount_info_shape_white);
                    }
                    if (multiGoodsList.get(0).getSizeNum() > 0) {
                        btn_minus.setVisibility(View.VISIBLE);
                        et_goods_num.setVisibility(View.VISIBLE);
                    } else {
                        btn_minus.setVisibility(View.GONE);
                        et_goods_num.setVisibility(View.GONE);
                    }
                }
                childAt.setVisibility(View.VISIBLE);
                childAt.setText(multiGoodsList.get(i).getSizeName());
                MyMultiListener myMultiListener = new MyMultiListener(size, i);
                myMultiListener.setGoodBean(goodBean);
                childAt.setOnClickListener(myMultiListener);
            } else {
                childAt.setVisibility(View.GONE);
            }
        }

        MyMultiListener myMultiListener = new MyMultiListener(size);
        myMultiListener.setWcPosition(wcPosition);
        myMultiListener.setGoodBean(goodBean);
        iv_click_to_close.setOnClickListener(myMultiListener);
        setBtnState(goodBean,null,btn_minus,btn_add);
        btn_minus.setOnClickListener(myMultiListener);
        btn_add.setOnClickListener(myMultiListener);

        et_goods_num.addTextChangedListener(new MyMultiEditextListener());
        rl_choose_multi_goods.setOnClickListener(myMultiListener);
        ll_pop_continer.setOnClickListener(myMultiListener);
    }


    /**
     * 多规格监听
     */
    class MyMultiListener implements View.OnClickListener {

        private GoodBean goodBean;
        private int size;
        private int position;//多规格数组中的位置
        private int wcPosition;

        public MyMultiListener(int size,int position) {
            this.size=size;
            this.position = position;
        }
        public MyMultiListener(int size) {
            this.size=size;
        }
        public void setGoodBean(GoodBean goodBean) {
            this.goodBean = goodBean;
        }
        public void setWcPosition(int wcPosition) {
            this.wcPosition = wcPosition;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ll_pop_continer:
                    break;
                case R.id.rl_choose_multi_goods:
                    LogUtils.e(TAG,"onClick() R.id.rl_choose_multi_goods");
                    if(multiPopupWindow!=null){
                        multiPopupWindow.dismiss();
                    }
                    break;
                case R.id.iv_click_to_close:
                    if(multiPopupWindow!=null){
                        multiPopupWindow.dismiss();
                    }
                    break;
                case R.id.btn_minus:
                    onclick = true;
                    minus(position,1,wcPosition);
                    onclick = false;
                    break;
                case R.id.btn_add:
                    onclick = true;
                    if ("0".equals(goodBean.getIs_weigh())||!ever_set_weigher) {
                        startAnimation(btn_add);
                        add(position,multiGoodsList.get(position).getSizeId(), localOrderBean.getGoodList(),1);
                    }else {
                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                        if(muiltPopView!=null) {
                            imm.hideSoftInputFromWindow(muiltPopView.getWindowToken(), 0); //强制隐藏键盘
                        }
                        if(weighListener!=null) {
                            if(multiPopupWindow!=null&&multiPopupWindow.isShowing()) {
                                multiPopupWindow.dismiss();
                            }
                            weighListener.weigh(goodBean,selectDgg);
                        }
                    }
                    onclick = false;
                    break;
                case R.id.tv_multi_pos_one:
                    if(onclick){
                        Utils.showToast(context,"正在计算价格，请稍后...");
                        return;
                    }
                    selectDgg=0;
                    switchChooseState(0,size,goodBean);
                    break;
                case R.id.tv_multi_pos_two:
                    if(onclick){
                        Utils.showToast(context,"正在计算价格，请稍后...");
                        return;
                    }
                    selectDgg=1;
                    switchChooseState(1,size,goodBean);
                    break;
                case R.id.tv_multi_pos_three:
                    if(onclick){
                        Utils.showToast(context,"正在计算价格，请稍后...");
                        return;
                    }
                    selectDgg=2;
                    switchChooseState(2,size,goodBean);
                    break;
                case R.id.tv_multi_pos_four:
                    if(onclick){
                        Utils.showToast(context,"正在计算价格，请稍后...");
                        return;
                    }
                    selectDgg=3;
                    switchChooseState(3,size,goodBean);
                    break;
                case R.id.tv_multi_pos_five:
                    if(onclick){
                        Utils.showToast(context,"正在计算价格，请稍后...");
                        return;
                    }
                    selectDgg=4;
                    switchChooseState(4,size,goodBean);
                    break;
                case R.id.tv_multi_pos_six:
                    if(onclick){
                        Utils.showToast(context,"正在计算价格，请稍后...");
                        return;
                    }
                    selectDgg=5;
                    switchChooseState(5,size,goodBean);
                    break;
            }
        }
    }

    /**
     *
     * @param pos 被选中的多规格的位置
     * @param size 多规格上的size
     * @param goodBean
     */
    private void switchChooseState(int pos,int size,GoodBean goodBean) {
        for(int i=0;i<6;i++){
            TextView childAt = (TextView) fl_multi.getChildAt(i);
            if(i<size){
                if(i==pos){
                    childAt.setSelected(true);
                    //显示各规格库存商品的价格
                    if(showMemberPrice&&multiGoodsList.get(i).getVipPrice()>=0) {
                        muilt_member_price_logo.setVisibility(View.VISIBLE);
                        tv_multi_price.setText("￥"+multiGoodsList.get(i).getVipPrice());
                    }else {
                        muilt_member_price_logo.setVisibility(View.GONE);
                        tv_multi_price.setText("￥"+multiGoodsList.get(i).getSizePrice());
                    }

                    if(multiGoodsList.get(i).getHasStock()==1){
                        tv_stock_text.setVisibility(View.VISIBLE);
                        tv_stock_num.setVisibility(View.VISIBLE);
                        tv_stock_num.setText(""+multiGoodsList.get(i).getStock());
                    }else{
                        tv_stock_num.setVisibility(View.GONE);
                        tv_stock_text.setVisibility(View.GONE);
                    }
                    if(multiGoodsList.get(i).getStockWarning()==1){
                        //tv_tight_inventory_pop.setVisibility(View.VISIBLE);
                        tv_stock_text.setText("库存紧张：");
                        tv_stock_text.setTextColor(Color.parseColor("#ffffff"));
                        tv_stock_num.setTextColor(Color.parseColor("#ff0000"));
                        tv_stock_text.setBackgroundResource(R.drawable.discount_info_shape);
                        //rl_add_reduce.setVisibility(View.VISIBLE);
                    }else{
                        //tv_tight_inventory_pop.setVisibility(View.GONE);
                        tv_stock_text.setText("库存：");
                        tv_stock_text.setTextColor(Color.parseColor("#999999"));
                        tv_stock_num.setTextColor(Color.parseColor("#999999"));
                        tv_stock_text.setBackgroundResource(R.drawable.discount_info_shape_white);
                        //rl_add_reduce.setVisibility(View.GONE);
                    }
                    pos_multi_goods = i;
                    multiGoodsId = multiGoodsList.get(i).getSizeId();
                    et_goods_num.setText(""+multiGoodsList.get(i).getSizeNum());
                    if(multiGoodsList.get(i).getSizeNum()>0){
                        btn_minus.setVisibility(View.VISIBLE);
                        et_goods_num.setVisibility(View.VISIBLE);
                    }else{
                        btn_minus.setVisibility(View.GONE);
                        et_goods_num.setVisibility(View.GONE);
                    }
                    LogUtils.Log("选的位置"+pos);
                    setBtnState(goodBean,goodBean.getGoodsList().get(selectDgg),btn_minus,btn_add);
                }else{
                    childAt.setSelected(false);
                }
            }else{
                childAt.setVisibility(View.GONE);
            }
        }
    }

    class MyMultiEditextListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //输入点默认加0
            if (s.toString().trim().substring(0).equals(".")) {
                s = "0" + s;
                et_goods_num.setText(s);
                et_goods_num.setSelection(2);
            }
            //限制不能输入0006，000.3这样格式的数据
            if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                if (!s.toString().substring(1, 2).equals(".")) {
                    et_goods_num.setText(s.subSequence(0, 1));
                    et_goods_num.setSelection(1);
                    return;
                }
            }
            //限定只能输入一个小数点
            if (et_goods_num.getText().toString().indexOf(".") >= 0) {
                if (et_goods_num.getText().toString().indexOf(".", et_goods_num.getText().toString().indexOf(".") + 1) > 0) {
                    et_goods_num.setText(et_goods_num.getText().toString().substring(0, et_goods_num.getText().toString().length() - 1));
                    et_goods_num.setSelection(et_goods_num.getText().toString().length());
                }
            }
            //限制小数点后两位
            if (s.toString().contains(".")) {
                if (s.length() - 1 - s.toString().indexOf(".") > 2) {
                    s = s.toString().subSequence(0,
                            s.toString().indexOf(".") + 3);
                    et_goods_num.setText(s);
                    et_goods_num.setSelection(s.length());
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            //1.不能在点击加减的时候监听行为:每次点击数字录入到文本框以后afterTextChanged调用
            //2.不能多次重复修改商品数量
            if(!onclick){
                if(!"".equals(et_goods_num.getText().toString())){
                    if(Double.valueOf(et_goods_num.getText().toString())>=0){
                        int first_pos = -1;
                        int isChoosed = -1;
                        if(localOrderBean.getGoodList()==null){
                            entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).setSizeNum(Double.valueOf(et_goods_num.getText().toString()));
                            entityGoodsList.get(pos_which_goods).setGoodsNum(Double.valueOf(et_goods_num.getText().toString()));
                            List goodLists = new ArrayList();
                            goodLists.add(entityGoodsList.get(pos_which_goods));
                            localOrderBean.setGoodList(goodLists);
                            isChoosed = -3;
                            last_mulit_pos = pos_multi_goods;
                            LogUtils.e(TAG,"afterTextChanged1");
                            if(showMemberPrice&&entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice()>=0) {
                                UpdatePriceNum(Double.valueOf(et_goods_num.getText().toString()),
                                        entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice(),1);
                            }else {
                                UpdatePriceNum(Double.valueOf(et_goods_num.getText().toString()),
                                        entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizePrice(),1);
                            }
                        }else{
                            //判断当前位置分商品有没有选过
                            OUT:
                            for(int i=0;i<localOrderBean.getGoodList().size();i++){
                                //LogUtils.e(TAG,"总商品："+i+",GoodsId:"+localOrderBean.getGoodList().get(i).getGoodsId());
                                for(int j=0;j<localOrderBean.getGoodList().get(i).getGoodsList().size();j++){
                                    //LogUtils.e(TAG,"分商品："+j+",SizeId:"+localOrderBean.getGoodList().get(i).getGoodsList().get(j).getSizeId());
                                    if(entityGoodsList.get(pos_which_goods).getGoodsId().equals(localOrderBean.getGoodList().get(i).getGoodsId())){
                                        if(entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizeId().equals(localOrderBean.getGoodList().get(i).getGoodsList().get(j))){
                                            //LogUtils.e(TAG,"已选sizeId="+entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizeId());
                                            double sizeNum = entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizeNum();
                                            double dif = Double.valueOf(et_goods_num.getText().toString()) - sizeNum;
                                            entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).setSizeNum(Double.valueOf(et_goods_num.getText().toString()));
                                            localOrderBean.getGoodList().get(i).getGoodsList().get(j).setSizeNum(Double.valueOf(et_goods_num.getText().toString()));
                                            double goodsNum = 0;
                                            for(int k=0;k<localOrderBean.getGoodList().get(i).getGoodsList().size();k++){
                                                goodsNum += localOrderBean.getGoodList().get(i).getGoodsList().get(k).getSizeNum();
                                            }
                                            localOrderBean.getGoodList().get(i).setGoodsNum(goodsNum);
                                            entityGoodsList.get(pos_which_goods).setGoodsNum(goodsNum);
                                            isChoosed = -2;
                                            last_mulit_pos = pos_multi_goods;
                                            LogUtils.e(TAG,"afterTextChanged2");
                                            if(showMemberPrice&&entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice()>=0) {
                                                UpdatePriceNum(dif,entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice(),1);
                                            }else {
                                                UpdatePriceNum(dif,entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizePrice(),1);
                                            }
                                            break OUT;
                                        }else{
                                            //LogUtils.e(TAG,"未选sizeId="+entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizeId());
                                            //选过此总商品内的多规格，但没选过本次点击商品
                                            isChoosed = j;
                                            first_pos = i;
                                            //LogUtils.e(TAG,"111 isChoosed="+isChoosed+",i="+first_pos);
                                            break OUT;
                                        }
                                    }
                                }
                            }
                        }
                        //LogUtils.e(TAG,"222 isChoosed="+isChoosed);
                        if(isChoosed == -1){//没选过此多规格商品
                            //LogUtils.e(TAG,"没选过此多规格商品 isChoosed == -1");
                            List goodLists = new ArrayList();
                            double sizeNum = entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizeNum();
                            double dif = Double.valueOf(et_goods_num.getText().toString()) - sizeNum;
                            entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).setSizeNum(Double.valueOf(et_goods_num.getText().toString()));
                            entityGoodsList.get(pos_which_goods).setGoodsNum(Double.valueOf(et_goods_num.getText().toString()));
                            goodLists.add(entityGoodsList.get(pos_which_goods));
                            //LogUtils.e(TAG,"没选过此多规格商品 添加："+new Gson().toJson(goodLists));
                            localOrderBean.getGoodList().addAll(goodLists);
                            last_mulit_pos = pos_multi_goods;
                            LogUtils.e(TAG,"afterTextChanged3");
                            if(showMemberPrice&&entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice()>=0) {
                                UpdatePriceNum(dif,entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice(),1);
                            }else {
                                UpdatePriceNum(dif,entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizePrice(),1);
                            }
                            //LogUtils.e(TAG,"没选过此多规格商品 添加："+new Gson().toJson(localOrderBean));
                        }else if(isChoosed >= 0){//选过此总商品内的多规格，但没选过本次点击商品
                            //LogUtils.e(TAG,"选过此总商品内的多规格，但没选过本次点击商品 isChoosed =="+isChoosed);
                            double sizeNum = entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizeNum();
                            double dif = Double.valueOf(et_goods_num.getText().toString()) - sizeNum;
                            entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).setSizeNum(Double.valueOf(et_goods_num.getText().toString()));
                            localOrderBean.getGoodList().get(first_pos).getGoodsList().get(pos_multi_goods).setSizeNum(Double.valueOf(et_goods_num.getText().toString()));
                            //设置总数
                            double goodsNum = 0;
                            for(int i=0;i<entityGoodsList.get(pos_which_goods).getGoodsList().size();i++){
                                goodsNum += entityGoodsList.get(pos_which_goods).getGoodsList().get(i).getSizeNum();
                            }
                            entityGoodsList.get(pos_which_goods).setGoodsNum(goodsNum);
                            localOrderBean.getGoodList().get(first_pos).setGoodsNum(goodsNum);
                            last_mulit_pos = pos_multi_goods;
                            LogUtils.e(TAG,"afterTextChanged4");
                            if(showMemberPrice&&entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice()>=0) {
                                UpdatePriceNum(dif,entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getVipPrice(),1);
                            }else {
                                UpdatePriceNum(dif,entityGoodsList.get(pos_which_goods).getGoodsList().get(pos_multi_goods).getSizePrice(),1);
                            }
                        }
                        notifyDataSetChanged();
                    }
                }
            }
        }
    }

    //更新价格和商品数量:isEditOrClick=1代表输入的数量；0代表点击添加
    private void UpdatePriceNum(final double num, final double price, final int isEditOrClick) {
        last_pos = pos_which_goods;
        LogUtils.e(TAG,"UpdatePriceNum() num="+num+",price="+price);
        Message mes = Message.obtain();
        mes.what = Constant.TOTAL_ACCOUNT;
        Bundle bundle = new Bundle();
        bundle.putInt("e_c",isEditOrClick);
        bundle.putDouble("g_p",price);
        bundle.putDouble("g_n",num);
        mes.setData(bundle);
        handler.sendMessage(mes);
        LogUtils.e(TAG,"点击以后的记录位置信息：last_pos="+last_pos+",last_mulit_pos="+last_mulit_pos);
    }

    private void setAlpha(float alpha) {
        WindowManager.LayoutParams params = context.getWindow().getAttributes();
        params.alpha = alpha;
        context.getWindow().setAttributes(params);
    }

    class ViewHolder {
        TextView tv_order_price;
        LinearLayout ll_stock;
        TextView tv_order_stock;
        TextView tv_tight_inventory;
        TextView tv_devide_line;
        TextView tv_order_unit;
        TextView tv_order_name;
        ImageView btn_add;
        ImageView btn_minus;
        TextView tv_goods_num;
        TextView tv_middle_level;
        LinearLayout rl_mulity_explain;
        ImageView member_price_logo;
        TextView tv_orign_price;
    }
}
