package activity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import bean.GetSOReturnInfo;
import bean.SellReturnLine;
import cn.bosyun.anyou.R;
import constant.Constant;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import utils.CommonUtils;
import utils.HttpUtils;
import utils.SPUtils;

/**
 * Created by Jinpan on 2017/1/18 0018.
 */
public class SellReturnActivity1 extends BaseActivity {

    private Button bt_sm;
    private Button bt_search;
    private TextView sellreturn_tv1;
    private TextView sellreturn_tv2;
    private TextView sellreturn_tv3;
    private ListView sellreturn_lv;
    private ImageView ac_iv_add;
    private TextView sellreturn_tv4;
    private TextView sellreturn_tv5;
    private EditText sellreturn_bz;
    private Button sellreturn_add;
    private GetSOReturnInfo getSOReturnInfo;
    private List<GetSOReturnInfo.SupplierListbean> supplierList;
    private MyAdapter myAdapter;
    private List<SellReturnLine> lines;
    private List<GetSOReturnInfo.ItemListbean> itemList;
    private List<GetSOReturnInfo.ItemListbean.WhsListbean> whsList;
    private List<GetSOReturnInfo.ItemListbean> itemList2;
    private List<GetSOReturnInfo.ItemListbean.WhsListbean> whsList2;
    private ProgressDialog progressDialog;
    private List<GetSOReturnInfo.ItemListbean.SOReturnBatchesbean> soReturnBatches;
    private String objectAuth_16;
    private List<GetSOReturnInfo.ItemListbean> list;
    private BMAdapter bmAdapter;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg != null) {
                progressDialog.dismiss();
                CommonUtils.showToast(getApplicationContext(), "数据加载完毕");
            }
            myAdapter.notifyDataSetChanged();
        }
    };


    @Override
    public int getContentLayoutRes() {
        return R.layout.activity_sellreturn1;
    }

    @Override
    public void initView(View v) {
        progressDialog = ProgressDialog.show(SellReturnActivity1.this, "", "数据加载中，请稍后……");
        progressDialog.setCancelable(true);
        bt_sm = ((Button) findViewById(R.id.bt_sm));
        bt_search = ((Button) findViewById(R.id.bt_search));
        sellreturn_tv1 = ((TextView) findViewById(R.id.sellreturn_tv1));
        sellreturn_tv2 = ((TextView) findViewById(R.id.sellreturn_tv2));
        sellreturn_tv3 = ((TextView) findViewById(R.id.sellreturn_tv3));
        sellreturn_lv = ((ListView) findViewById(R.id.sellreturn_lv));

        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.item_foot_buyreturn1, null);
        sellreturn_lv.addFooterView(view);
        sellreturn_lv.setFooterDividersEnabled(false);
        ac_iv_add = ((ImageView) view.findViewById(R.id.ac_iv_add));
        sellreturn_tv4 = ((TextView) view.findViewById(R.id.buyreturn_tv4));
        sellreturn_tv5 = ((TextView) view.findViewById(R.id.buyreturn_tv5));
        sellreturn_bz = ((EditText) view.findViewById(R.id.buyreturn_bz));
        sellreturn_add = ((Button) view.findViewById(R.id.buyreturn_add));

        objectAuth_16 = SPUtils.getString(getApplicationContext(), "ObjectAuth_16");
        if (objectAuth_16.equals("R")) {
            view.setVisibility(View.GONE);
            sellreturn_tv1.setBackgroundResource(R.drawable.bg_nochecked);
            sellreturn_tv1.setEnabled(false);
            sellreturn_tv2.setBackgroundResource(R.drawable.bg_nochecked);
            sellreturn_tv2.setEnabled(false);
        }

        initTime();
        initData();
        bt_sm.setOnClickListener(onClick);
        bt_search.setOnClickListener(onClick);
        sellreturn_tv1.setOnClickListener(onClick);
        sellreturn_tv2.setOnClickListener(onClick);
        ac_iv_add.setOnClickListener(onClick);
        sellreturn_add.setOnClickListener(onClick);

        lines = new ArrayList<>();
        myAdapter = new MyAdapter();
        sellreturn_lv.setAdapter(myAdapter);
    }

    //默认显示当天时间
    private void initTime() {
        String format = CommonUtils.getCurrentTime();
        sellreturn_tv2.setText(format);
    }

    //请求数据
    private void initData() {
        //reJson={"UserName":"testAccount", "Password":"1234", "DBName":"ANSA410"}
        SharedPreferences sp = this.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sp.getString("username", null);
        String password = sp.getString("password", null);
        String dbName = SPUtils.getString(getApplicationContext(), "DBName");
        String json = "{\"UserName\":\"" + username + "\", \"Password\":\"" + password + "\", \"DBName\":\"" + dbName + "\"}";
        Callback callBack = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String result = response.body().string();
                getSOReturnInfo = new Gson().fromJson(result, GetSOReturnInfo.class);
                Message message = Message.obtain();
                message.obj = getSOReturnInfo;
                handler.sendMessage(message);

            }
        };
        HttpUtils.getResult(Constant.HOST_GetSOReturnInfo, json, callBack);
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                //点击扫描按钮的监听
                case R.id.bt_sm:
                    //扫描二维码
                    SellReturnActivity1.this.startActivityForResult(new Intent(SellReturnActivity1.this.getBaseContext(), CaptureActivity.class), 7);
                    break;

                //点击查找按钮的监听
                case R.id.bt_search:
                    View view = getLayoutInflater().inflate(R.layout.dialog, (ViewGroup) findViewById(R.id.dialog));
                    final EditText editText = (EditText) view.findViewById(R.id.et);
                    editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            SearchPoReturnDetail(editText);
                            return true;
                        }
                    });
                    new AlertDialog.Builder(SellReturnActivity1.this).setTitle("请输入要查询的单号").setView(view)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton("搜索", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SearchPoReturnDetail(editText);

                                }
                            })
                            .show();
                    break;

                //点击供应商编码的监听
                case R.id.sellreturn_tv1:
                    PopupWindow popupWindow = null;
                    ListView lv = new ListView(SellReturnActivity1.this);
                    if (getSOReturnInfo != null) {
                        supplierList = getSOReturnInfo.getSupplierList();
                    }
                    lv.setAdapter(new CardCodeAdapter());
                    lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            sellreturn_tv1.setText(supplierList.get(position).getCardCode());
                            sellreturn_tv3.setText(supplierList.get(position).getCardName());
                        }
                    });

                    if (popupWindow == null) {
                        popupWindow = new PopupWindow(lv, 700, 400);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow.setOutsideTouchable(true);

                    }
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    } else {
                        popupWindow.showAsDropDown(sellreturn_tv1, 0, 0);
                        popupWindow.getContentView().setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return false;
                            }
                        });
                    }
                    break;

                //点击过账日期的监听
                case R.id.sellreturn_tv2:
                    final Calendar c = Calendar.getInstance();
                    DatePickerDialog dialog = new DatePickerDialog(SellReturnActivity1.this,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    c.set(year, monthOfYear, dayOfMonth);
                                    sellreturn_tv2.setText(DateFormat.format("yyy.MM.dd", c));
                                }
                            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                    dialog.show();
                    break;

                //添加一行图标的点击监听
                case R.id.ac_iv_add:
                    lines.add(new SellReturnLine());
                    myAdapter.notifyDataSetChanged();
                    break;

                //添加按钮的监听
                case R.id.buyreturn_add:
                    new AlertDialog.Builder(SellReturnActivity1.this).setTitle("提示")//设置对话框标题
                            .setMessage("是否添加销售退货的直接制单?")//设置显示的内容
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {//添加返回按钮
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    dialog.dismiss();
                                }
                            }).setPositiveButton("确定", new DialogInterface.OnClickListener() {//添加确定按钮
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //等待动画
                            progressDialog = ProgressDialog.show(SellReturnActivity1.this, "", "加载中，请稍后……");

                            //连接服务器获取数据
                            SharedPreferences sp = getApplicationContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                            final String username = sp.getString("username", null);
                            final String password = sp.getString("password", null);
                            final String dbname = SPUtils.getString(getApplicationContext(), "DBName");
                            String CardCode = sellreturn_tv1.getText().toString();
                            String CardName = sellreturn_tv3.getText().toString();
                            String Comments = sellreturn_bz.getText().toString();
                            String DocDate = sellreturn_tv2.getText().toString();

                            //{"UserName":"testAccount","Password":"1234","DBName":"ANSA410","CardCode":"S00015","CardName":"温州新吉高包装有限公司","DocDate":"2016.12.30","Lines":[]}
                            String s2 = "";
                            for (SellReturnLine line : lines) {
                                String s1 = new Gson().toJson(line);
                                s2 += (s1 + ",");
                            }
                            String substring = null;
                            if (s2.length() != 0) {
                                substring = s2.substring(0, s2.length() - 1);
                            }
                            String s = "[" + substring + "]";
                            final String json = "{\"UserName\":\"" + username + "\", \"Password\":\"" + password + "\", \"DBName\":\"" + dbname +
                                    "\", \"CardCode\":\"" + CardCode + "\", \"CardName\":\"" + CardName + "\",\"DocDate\":\"" + DocDate + "\",\"Comments\":\"" + Comments + "\",\"Lines\":" + s + "}";
                            Callback callBack = new Callback() {
                                @Override
                                public void onFailure(Call call, IOException e) {
                                    progressDialog.dismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            new AlertDialog.Builder(SellReturnActivity1.this).setTitle("添加失败:").setMessage("没有连接到服务器").setNegativeButton("取消", null).show();
                                        }
                                    });
                                }

                                @Override
                                public void onResponse(Call call, Response response) throws IOException {

                                    String result = response.body().string();
                                    String status = HttpUtils.getCode(result, "status");
                                    final String Description = HttpUtils.getCode(result, "Description");
                                    if ("0".equals(status)) {
                                        progressDialog.dismiss();
                                        CommonUtils.showToast(getApplicationContext(), "添加成功");
                                        finish();
                                    } else if ("-1".equals(status)) {
                                        CommonUtils.write("销售退货直接制单:", json);
                                        progressDialog.dismiss();
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                new AlertDialog.Builder(SellReturnActivity1.this).setTitle("添加失败:").setMessage(Description).setNegativeButton("取消", null).show();
                                            }
                                        });
                                    }
                                }
                            };
                            HttpUtils.getResult(Constant.HOST_AddSOReturn, json, callBack);

                        }
                    }).show();

                    break;

            }
        }
    };

    //根据单号搜索未清的采购收货单
    private void SearchPoReturnDetail(EditText editText) {
        //等待动画
        progressDialog = ProgressDialog.show(SellReturnActivity1.this, "", "加载中，请稍后……");
        //连接服务器获取数据
        SharedPreferences sp = getApplicationContext().getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String username = sp.getString("username", null);
        String password = sp.getString("password", null);
        String dbname = SPUtils.getString(getApplicationContext(), "DBName");
        String Keyword = editText.getText().toString().trim();
        //{"UserName":"testAccount", "Password":"1234", "DBName":"ANSA410","Keyword":"408"}
        String json = "{\"UserName\":\"" + username + "\", \"Password\":\"" + password + "\", \"DBName\":\"" + dbname + "\",\"Keyword\":\"" + Keyword + "\"}";
        Callback callBack = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                progressDialog.dismiss();
                CommonUtils.showToast(getApplicationContext(), "没有连接到服务器");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                String status = HttpUtils.getCode(result, "status");
                String Description = HttpUtils.getCode(result, "Description");
                if ("-1".equals(status)) {
                    progressDialog.dismiss();
                    CommonUtils.showToast(getApplicationContext(), "搜索失败," + Description);
                } else {
                    progressDialog.dismiss();
                    CommonUtils.showToast(getApplicationContext(), "搜索成功");
                    Intent intent = new Intent();
                    intent.putExtra("searchSOReturnDetail", result);
                    intent.setClass(SellReturnActivity1.this, SellReturnActivity2.class);
                    startActivity(intent);

                }
            }
        };
        HttpUtils.getResult(Constant.HOST_SearchSOReturnDetail, json, callBack);
    }

    //客户编码的适配器
    private class CardCodeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return supplierList == null ? 0 : supplierList.size();
        }

        @Override
        public Object getItem(int position) {
            return supplierList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(SellReturnActivity1.this, R.layout.ac_item2_2, null);
                viewHolder.ac_item2_3 = (TextView) convertView.findViewById(R.id.ac_item2_3);
                viewHolder.ac_item2_4 = (TextView) convertView.findViewById(R.id.ac_item2_4);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = ((ViewHolder) convertView.getTag());
            }
            viewHolder.ac_item2_3.setText(supplierList.get(position).getCardCode());
            viewHolder.ac_item2_4.setText(supplierList.get(position).getCardName());
            return convertView;
        }

        private class ViewHolder {
            TextView ac_item2_3;
            TextView ac_item2_4;
        }
    }

    //外部listview的适配器
    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return lines == null ? 0 : lines.size();
        }

        @Override
        public Object getItem(int position) {
            return lines.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            double sum1 = 0.0;
            double sum2 = 0.0;
            for (int i = 0; i <= position; i++) {
                sum1 += lines.get(i).getBagAmount();
                sum2 += lines.get(i).getAmount();
            }
            sellreturn_tv4.setText(String.valueOf(sum1));
            sellreturn_tv5.setText(String.valueOf(sum2));
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = View.inflate(SellReturnActivity1.this, R.layout.ac_item5_4, null);
                holder.ac_item5_iv = (ImageView) convertView.findViewById(R.id.ac_item5_iv);
                holder.ac_item5_1 = ((TextView) convertView.findViewById(R.id.ac_item5_1));
                holder.ac_item5_2 = ((TextView) convertView.findViewById(R.id.ac_item5_2));
                holder.ac_item5_3 = ((EditText) convertView.findViewById(R.id.ac_item5_3));
                holder.ac_item5_4 = ((TextView) convertView.findViewById(R.id.ac_item5_4));
                holder.ac_item5_5 = ((TextView) convertView.findViewById(R.id.ac_item5_5));
                holder.ac_item5_6 = ((TextView) convertView.findViewById(R.id.ac_item5_6));
                convertView.setTag(holder);
            } else {
                holder = ((ViewHolder) convertView.getTag());
            }
            holder.ac_item5_iv.setTag(position);
            holder.ac_item5_1.setTag(position);
            holder.ac_item5_2.setTag(position);
            holder.ac_item5_3.setTag(position);
            holder.ac_item5_4.setTag(position);
            holder.ac_item5_5.setTag(position);
            holder.ac_item5_6.setTag(position);

            holder.ac_item5_1.setText(lines.get(position).getItemCode());
            holder.ac_item5_2.setText(lines.get(position).getItemName());
            holder.ac_item5_3.setText(String.valueOf(lines.get(position).getBagAmount()));
            holder.ac_item5_4.setText(CommonUtils.addComma(String.valueOf(lines.get(position).getAmount())));
            holder.ac_item5_5.setText(lines.get(position).getWhsCode());
            holder.ac_item5_6.setText("可选 ->");

            holder.ac_item5_iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    lines.remove(position);
                    notifyDataSetChanged();
                }
            });

            holder.ac_item5_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int tag = (int) holder.ac_item5_1.getTag();
                    ListView lv5 = new ListView(SellReturnActivity1.this);
                    LayoutInflater layoutInflater = LayoutInflater.from(getApplicationContext());
                    View view = layoutInflater.inflate(R.layout.item_search, null);
                    lv5.addHeaderView(view);
                    if (getSOReturnInfo != null) {
                        itemList = getSOReturnInfo.getItemList();
                        list = itemList;
                    }
                    final EditText item_search = (EditText) view.findViewById(R.id.item_search);
                    item_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                        @Override
                        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                            String trim = item_search.getText().toString().trim();
                            list = new ArrayList<>();
                            for (int i = 0; i < itemList.size(); i++) {
                                if ((itemList.get(i).getItemCode() + itemList.get(i).getItemName()).contains(trim)) {
                                    GetSOReturnInfo.ItemListbean itemListbean = itemList.get(i);
                                    list.add(itemListbean);
                                }
                            }
                            bmAdapter.notifyDataSetChanged();
                            return false;
                        }
                    });
                    bmAdapter = new BMAdapter();
                    lv5.setAdapter(bmAdapter);
                    lv5.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        private int num;

                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            holder.ac_item5_1.setText(list.get(position - 1).getItemCode());
                            holder.ac_item5_2.setText(list.get(position - 1).getItemName());
                            lines.get(tag).setItemCode(holder.ac_item5_1.getText().toString());
                            lines.get(tag).setItemName(holder.ac_item5_2.getText().toString());
                            for (int i = 0; i < itemList.size(); i++) {
                                if (itemList.get(i).getItemCode().equals(holder.ac_item5_1.getText().toString())) {
                                    num = i;
                                }
                            }

                            whsList = itemList.get(num).getWhsList();
                            soReturnBatches = itemList.get(num).getSOReturnBatches();
                            itemList2 = itemList;
                            itemList2.get(tag).setWhsList(whsList);
                            itemList2.get(tag).setSOReturnBatches(soReturnBatches);
                            final double factor1 = itemList.get(num).getFactor1();
                            holder.ac_item5_3.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    String trim = holder.ac_item5_3.getText().toString().trim();
                                    int tag = (int) holder.ac_item5_3.getTag();
                                    if (trim == null || trim.length() == 0) {
                                        trim = "0.0";
                                    }
                                    double v = Double.parseDouble(trim);
                                    lines.get(tag).setBagAmount(v);
                                    ;
                                    double v1 = CommonUtils.mul(v, factor1);
                                    holder.ac_item5_4.setText(CommonUtils.addComma(String.valueOf(v1)));
                                    lines.get(tag).setAmount(v1);
                                    lines.get(tag).setFactor2(v1);
                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                }
                            });

                        }
                    });
                    PopupWindow popupWindow = null;
                    if (popupWindow == null) {
                        popupWindow = new PopupWindow(lv5, 800, 300);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow.setOutsideTouchable(true);

                    }
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    } else {
                        popupWindow.showAsDropDown(holder.ac_item5_1, 0, 0);
                        popupWindow.getContentView().setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return false;
                            }
                        });
                    }
                }
            });


            holder.ac_item5_5.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final int tag = (int) holder.ac_item5_5.getTag();
                    if (itemList2 != null) {
                        whsList2 = itemList2.get(tag).getWhsList();
                        soReturnBatches = itemList2.get(tag).getSOReturnBatches();
                    }
                    ListView lv2 = new ListView(SellReturnActivity1.this);
                    lv2.setAdapter(new CKAdapter());
                    lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            List list = new ArrayList();
                            holder.ac_item5_5.setText(SellReturnActivity1.this.whsList2.get(position).getWhsCode());
                            lines.get(tag).setWhsCode(holder.ac_item5_5.getText().toString());
                            for (GetSOReturnInfo.ItemListbean.SOReturnBatchesbean soReturnBatch : soReturnBatches) {
                                if (soReturnBatch.getWhsCode().equals(holder.ac_item5_5.getText().toString())) {
                                    soReturnBatch.setSysNumber(null);
                                    soReturnBatch.setWhsCode(null);
                                    list.add(soReturnBatch);
                                }
                            }
                            lines.get(tag).setBatches(list);
                        }
                    });
                    PopupWindow popupWindow = null;
                    if (popupWindow == null) {
                        popupWindow = new PopupWindow(lv2, 500, 300);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow.setOutsideTouchable(true);

                    }
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    } else {
                        popupWindow.showAsDropDown(holder.ac_item5_5, 0, 0);
                        popupWindow.getContentView().setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return false;
                            }
                        });
                    }
                }
            });

            holder.ac_item5_6.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ListView lv3 = new ListView(SellReturnActivity1.this);
                    final int tag = (int) holder.ac_item5_6.getTag();
                    soReturnBatches = lines.get(tag).getBatches();

                    lv3.setAdapter(new PCAdapter(tag));

                    PopupWindow popupWindow = null;
                    if (popupWindow == null) {
                        popupWindow = new PopupWindow(lv3, 450, 300);
                        popupWindow.setFocusable(true);
                        popupWindow.setBackgroundDrawable(new BitmapDrawable());
                        popupWindow.setOutsideTouchable(true);

                    }
                    if (popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    } else {
                        popupWindow.showAsDropDown(holder.ac_item5_6, 0, 0);
                        popupWindow.getContentView().setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return false;
                            }
                        });
                    }
                }
            });

            return convertView;
        }

        class ViewHolder {
            ImageView ac_item5_iv;
            TextView ac_item5_1;
            TextView ac_item5_2;
            EditText ac_item5_3;
            TextView ac_item5_4;
            TextView ac_item5_5;
            TextView ac_item5_6;
        }
    }

    //编码listview适配器
    private class BMAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(SellReturnActivity1.this, R.layout.ac_item2_2, null);
                viewHolder.ac_item2_3 = (TextView) convertView.findViewById(R.id.ac_item2_3);
                viewHolder.ac_item2_4 = (TextView) convertView.findViewById(R.id.ac_item2_4);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = ((ViewHolder) convertView.getTag());
            }
            viewHolder.ac_item2_3.setTag(position);
            viewHolder.ac_item2_4.setTag(position);

            int tag = (int) viewHolder.ac_item2_3.getTag();
            viewHolder.ac_item2_3.setText(list.get(tag).getItemCode());
            viewHolder.ac_item2_4.setText(list.get(tag).getItemName());
            return convertView;
        }

        class ViewHolder {
            TextView ac_item2_3;
            TextView ac_item2_4;
        }
    }

    //内部仓库listview的适配器
    private class CKAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return whsList2 == null ? 0 : whsList2.size();
        }

        @Override
        public Object getItem(int position) {
            return whsList2.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(SellReturnActivity1.this, R.layout.ac_item2_2, null);
                viewHolder.ac_item2_3 = (TextView) convertView.findViewById(R.id.ac_item2_3);
                viewHolder.ac_item2_4 = (TextView) convertView.findViewById(R.id.ac_item2_4);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = ((ViewHolder) convertView.getTag());
            }

            viewHolder.ac_item2_3.setText(whsList2.get(position).getWhsName());
            viewHolder.ac_item2_4.setText(whsList2.get(position).getWhsCode());
            return convertView;
        }

        class ViewHolder {

            TextView ac_item2_3;
            TextView ac_item2_4;
        }
    }

    //内部批次listview的适配器
    private class PCAdapter extends BaseAdapter {
        private final int tag;

        public PCAdapter(int tag) {
            this.tag = tag;
        }

        @Override
        public int getCount() {
            return soReturnBatches == null ? 0 : soReturnBatches.size();
        }

        @Override
        public Object getItem(int position) {
            return soReturnBatches.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = View.inflate(SellReturnActivity1.this, R.layout.ac_item2_4, null);
                viewHolder.ac_item2_3 = (EditText) convertView.findViewById(R.id.ac_item2_3);
                viewHolder.ac_item2_4 = (EditText) convertView.findViewById(R.id.ac_item2_4);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = ((ViewHolder) convertView.getTag());
            }
            viewHolder.ac_item2_3.setTag(position);
            viewHolder.ac_item2_4.setTag(position);

            viewHolder.ac_item2_3.setText(lines.get(tag).getBatches().get(position).getBatchNumber());
            viewHolder.ac_item2_4.setText(String.valueOf(lines.get(tag).getBatches().get(position).getAmount()));
            viewHolder.ac_item2_3.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String trim = viewHolder.ac_item2_3.getText().toString().trim();
                    int pcTag = (int) viewHolder.ac_item2_3.getTag();
                    if (trim == null || trim.length() == 0) {
                        trim = "0";
                    }
                    lines.get(tag).getBatches().get(pcTag).setBatchNumber(trim);
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            viewHolder.ac_item2_4.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String trim = viewHolder.ac_item2_4.getText().toString().trim();
                    int pcTag = (int) viewHolder.ac_item2_4.getTag();
                    if (trim == null || trim.length() == 0) {
                        trim = "0";
                    }
                    lines.get(tag).getBatches().get(pcTag).setAmount(Double.parseDouble(trim));
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            return convertView;
        }

        class ViewHolder {

            EditText ac_item2_3;
            EditText ac_item2_4;
        }

    }

    //扫描二维码的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            String result = data.getExtras().getString("result");
            if (requestCode == 7) {
                EditText editText = new EditText(SellReturnActivity1.this);
                editText.setText(result);
                SearchPoReturnDetail(editText);
                CommonUtils.showToast(getApplicationContext(), "没有权限");
                Button bt_sm = (Button) findViewById(R.id.bt_sm);
                InputMethodManager imm = (InputMethodManager) bt_sm.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.SHOW_FORCED);
            }
        }
    }

}
