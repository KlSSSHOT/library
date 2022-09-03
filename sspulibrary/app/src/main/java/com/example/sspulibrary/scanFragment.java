package com.example.sspulibrary;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.widget.CompoundButton.OnCheckedChangeListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sspulibrary.R;
import com.uhf.api.cls.Reader.TAGINFO;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import cn.pda.serialport.Tools;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;


public class scanFragment extends Fragment implements OnCheckedChangeListener,OnClickListener{
    @Nullable

    private Button btnStart;//inventory button扫描开始按钮
    private boolean isMulti = false;// multi mode flag多标签模式勾选,默认为false不改变
    private CheckBox checkMulti;//multi model check box 勾选框



    private View view;// this fragment UI
    private boolean isTid = false;// multi mode flag
    private boolean isPlay = true;// multi mode flag
    private int allCount = 0;// inventory count

    private Set<String> epcSet = null; //store different EPC
    private List<EpcDataModel> listEpc = null;//EPC list
    private Map<String, Integer> mapEpc = null; //store EPC position //显示的标签
    private EPCadapter adapter;//epc list adapter //adapter 调用展示列表方法
    private ListView lvEpc;// epc list view 列表展示?
//    private TextView tvTagCount;//tag count text view  展示标签扫描数次不需要

    private TextView tvTagSum;//tag sum text view

    private TextView tvTitle;//tag sum text view
    //


    private Button savebutton;//测试按钮
    private Runnable runnable_MainActivity = new Runnable() {

        @Override
        public void run() {


            List<TAGINFO> list1;
            if (isMulti) {
//                Log.e(TGA, "runnable-isMulti-true");
                list1 = MainActivity.mUhfrManager.tagInventoryRealTime();
            } else {
                if (isTid) {
                    list1 = MainActivity.mUhfrManager.tagEpcTidInventoryByTimer((short) 50);
                }else {
                    list1 = MainActivity.mUhfrManager.tagInventoryByTimer((short) 50);
                }
            }
            String data;
            handler1.sendEmptyMessage(1980);
            if (list1 != null && list1.size() > 0) {//
//                Log.e(TGA, list1.size() + "");
                if(isPlay) {
                    Util.play(1, 0);
                }
                for (TAGINFO tfs : list1) {
                    byte[] epcdata = tfs.EpcId;
                    if (isTid){
                        data = Tools.Bytes2HexString(tfs.EmbededData, tfs.EmbededDatalen);
                    }else {
                        data = Tools.Bytes2HexString(epcdata, epcdata.length);
                    }
                    int rssi = tfs.RSSI;
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle b = new Bundle();
                    b.putString("data", data);
                    b.putString("rssi", rssi + "");
                    msg.setData(b);
                    handler1.sendMessage(msg);
                }
            }
            handler1.postDelayed(runnable_MainActivity, 0);
        }
    };
    private Handler handler1 = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    String epc = msg.getData().getString("data");
                    String rssi = msg.getData().getString("rssi");
                    if (epc == null || epc.length() == 0) {
                        return;
                    }
                    int position;
                    allCount++;
                    if (epcSet == null) {//first
                        epcSet = new HashSet<String>();
                        listEpc = new ArrayList<EpcDataModel>();
                        mapEpc = new HashMap<String, Integer>();
                        epcSet.add(epc);
                        mapEpc.put(epc, 0);
                        EpcDataModel epcTag = new EpcDataModel();
                        epcTag.setepc(epc);
                        epcTag.setrssi(rssi);
                        epcTag.setCount(1);
                        listEpc.add(epcTag);
                        adapter = new EPCadapter(getActivity(), listEpc);
                        Log.e("aa","listEpc.size():"+listEpc.size());
                        lvEpc.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        MainActivity.mSetEpcs = epcSet;
                    } else {
                        if (epcSet.contains(epc)) {//set already exit
                            position = mapEpc.get(epc);
                            EpcDataModel epcOld = listEpc.get(position);
                            epcOld.setrssi(rssi);
                            epcOld.setCount(epcOld.getCount() + 1);
                            listEpc.set(position, epcOld);
                        } else {
                            epcSet.add(epc);
                            mapEpc.put(epc, listEpc.size());
                            EpcDataModel epcTag = new EpcDataModel();
                            epcTag.setepc(epc);
                            epcTag.setrssi(rssi);
                            epcTag.setCount(1);
                            listEpc.add(epcTag);
                            MainActivity.mSetEpcs = epcSet;
                        }
//                        tvTagCount.setText("" + allCount);
//                        tvTagSum.setText("" + listEpc.size());//不需要 tag总数
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case 1980:
//                    String countString = tvRunCount.getText().toString();
//                    if (countString.equals("") || countString == null) {
//                        tvRunCount.setText(String.valueOf(1));
//                    } else {
//                        int previousCount = Integer.valueOf(countString);
//                        int nowCount = previousCount + 1;
//                        tvRunCount.setText(String.valueOf(nowCount));
//                    }
                    break;
            }
        }
    };






    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.scanhome_fragment, null);
        initView();
        return  view;
//
//        return inflater.inflate(R.layout.scanhome_fragment, container, false);


    }

    //初始化
    private void initView() {
        btnStart = (Button) view.findViewById(R.id.button_start);
        btnStart.setOnClickListener(this);//初始化监听
        lvEpc = (ListView) view.findViewById(R.id.listView_epc);
        checkMulti = (CheckBox) view.findViewById(R.id.checkBox_multi);
        checkMulti.setOnCheckedChangeListener(this);//多标签功能

    }
    private boolean keyControl = true;

    public void isRead() {
        Util.initSoundPool(getContext());
        if (MainActivity.mUhfrManager == null) {
            showToast(getActivity().getString(R.string.connection_failed));
            return;
        }
        if (!isStart) {
            checkMulti.setEnabled(false);
            btnStart.setText(this.getString(R.string.stop_inventory_epc));
            MainActivity.mUhfrManager.setGen2session(isMulti);
            if (isMulti) {
//                Log.e(TGA, "isMulti-true");
                MainActivity.mUhfrManager.asyncStartReading();
            }
            handler1.postDelayed(runnable_MainActivity, 0);
            isStart = true;
        } else {
            checkMulti.setEnabled(true);
            if (isMulti) {
//                Log.e(TGA, "isMulti-true");
                MainActivity.mUhfrManager.asyncStopReading();
            }
            handler1.removeCallbacks(runnable_MainActivity);
            btnStart.setText(this.getString(R.string.start_inventory_epc));
            isStart = false;
        }
    }

    //show tips
    private Toast toast;

    private void showToast(String info) {
        if (toast == null) toast = Toast.makeText(getActivity(), info, Toast.LENGTH_SHORT);
        else toast.setText(info);
        toast.show();
    }


    // 简单消息提示框


    private boolean isStart = false;
//    private CheckBox checkMulti;//multi model check box  不需要多标签


    public void onClick(View v) {


        switch (v.getId()) {

            case R.id.button_start:
                isRead();
                break;
//            case R.id.button_clear_epc:
//                clearEpc();
//                break;
//            case R.id.button_export:
//                if(listEpc!=null && listEpc.size()!=0) {
//                    save(FileName());
//                    Toast.makeText(getContext(), "Success"+listEpc.size() , Toast.LENGTH_SHORT).show();
//                }else{
//                    Toast.makeText(getContext(), "Fila", Toast.LENGTH_SHORT).show();
//                }
//                break;

//            case R.id.button_time_start:
//                statenvtick = System.currentTimeMillis();
//                Log.e(TAG,"statetime:"+statenvtick);
//                    Log.e(TGA,"isMulti-true");
//                    MainActivity.mUhfrManager.asyncStartReading();
//
//                handler1.postDelayed(runnable_MainActivity1, 0);
//                break;


        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // TODO Auto-generated method stub
        switch (buttonView.getId()) {
            case R.id.checkBox_multi:
                if (isChecked) {
                    isMulti = true;
                }else {
                    isMulti = false;
                }
                break;
//            case R.id.checkBox_sound:
//                if (isChecked) {
//                    isPlay= true;
//                }else {
//                    isPlay = false;
//                }
//                break;
//            case R.id.checkBox_tid:
//                if (isChecked) {
//                    isTid= true;
//                    tvTitle.setText("TID");
//                    isMulti = false;
//                    checkMulti.setChecked(false);
//                    checkMulti.setEnabled(false);
//                }else {
//                    isTid = false;
//                    tvTitle.setText("EPC");
//                    checkMulti.setEnabled(true);
//                }
//                break;

        }

    }



}
