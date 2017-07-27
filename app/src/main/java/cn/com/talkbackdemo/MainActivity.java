package cn.com.talkbackdemo;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.com.talkbacklib.TalkBackHandle;
import cn.com.talkbacklib.bean.DeviceBean;
import cn.com.talkbacklib.callback.DeviceBroadcastCallback;
import cn.com.talkbacklib.callback.DeviceOnLineCallback;
import cn.com.talkbacklib.callback.DeviceTalkbackCallback;
import cn.com.talkbacklib.utils.DeviceUtils;

public class MainActivity extends AppCompatActivity implements DeviceOnLineCallback, AdapterView.OnItemClickListener, DeviceTalkbackCallback, DeviceBroadcastCallback {

    private ListView mDeviceList;
    private List<DeviceBean> mDeviceBeenList;
    private DeviceListAdapter mDeviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
    }

    private void initView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("设备列表");
        setSupportActionBar(toolbar);

        mDeviceList = (ListView) findViewById(R.id.lv_device_list);
        mDeviceListAdapter = new DeviceListAdapter(this, mDeviceBeenList);
        mDeviceList.setAdapter(mDeviceListAdapter);
        mDeviceList.setOnItemClickListener(this);
    }

    private void initData() {
        mDeviceBeenList = new ArrayList<>();
        TalkBackHandle.newInstance().setContext(MainActivity.this).startWorking(this,this).onLineDevice().sreachDevice().setDeviceOnLineCallback(this);
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] strings = new String[]{Manifest.permission.RECORD_AUDIO};
            requestPermissions(strings,111);
        }
    }

    @Override
    public void onDeviceLine(final DeviceBean deviceBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int beanindex = DeviceUtils.getDeviceBeanindex(mDeviceBeenList,deviceBean);
                if (beanindex >= 0) {
                    mDeviceBeenList.set(beanindex,deviceBean);
                }
                else
                    mDeviceBeenList.add(deviceBean);

                mDeviceListAdapter.setData(mDeviceBeenList);
            }
        });
    }



    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        DeviceBean deviceBean = mDeviceBeenList.get(position);
        Log.i("---", "onItemClick: "+deviceBean.toString());
        if (deviceBean.isChecked())
            deviceBean.setChecked(false);
        else
            deviceBean.setChecked(true);
        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 111){

        }
    }

    @Override
    public void onReceiveTalkback(final DeviceBean deviceBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,deviceBean.getDevName()+"-邀请对讲-....",Toast.LENGTH_LONG).show();
            }
        });
        TalkBackHandle.newInstance().responeTalkback(deviceBean,true);
    }

    @Override
    public void onExitTalkback(final DeviceBean deviceBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,deviceBean.getDevName()+"-退出对讲-....",Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onErrorTalkback(final String errorMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this,errorMsg,Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        requestPermissions();
        LinkedList<DeviceBean> checkedDevice = getCheckedDevice();
        if (checkedDevice == null || checkedDevice.size() == 0) {
            Toast.makeText(this, "未选择对讲设备或广播设备...", Toast.LENGTH_SHORT).show();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.toolbar_start_talkback:
                TalkBackHandle.newInstance().requestTalkback(checkedDevice.getFirst());
                Toast.makeText(this, "开始对讲", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.toolbar_stop_talkback:
                TalkBackHandle.newInstance().stopTalkback();
                Toast.makeText(this, "停止对讲", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.toolbar_start_broadcast:
                TalkBackHandle.newInstance().requestBroadCast(checkedDevice);
                Toast.makeText(this, "开始广播", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.toolbar_stop_broadcast:
                TalkBackHandle.newInstance().stopBroadCast();
                Toast.makeText(this, "停止广播", Toast.LENGTH_SHORT).show();
                return true;
            case R.id.toolbar_exit_broadcast:
                TalkBackHandle.newInstance().leaveGroup();
                Toast.makeText(this, "退出广播", Toast.LENGTH_SHORT).show();
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }

    private LinkedList<DeviceBean> getCheckedDevice(){
        LinkedList<DeviceBean> deviceBeenList = new LinkedList<>();
        if (mDeviceBeenList == null)
            return deviceBeenList;
        for (DeviceBean deviceBean : mDeviceBeenList) {
            if (deviceBean.isChecked())
                deviceBeenList.add(deviceBean);
        }
        return deviceBeenList;
    }

    @Override
    public void onExitBroadcast(final DeviceBean deviceBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, deviceBean.getDevIP()+"接受到广播停止命令...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onExitMulticast(final DeviceBean deviceBean) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, deviceBean.getDevIP()+"退出了广播...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
