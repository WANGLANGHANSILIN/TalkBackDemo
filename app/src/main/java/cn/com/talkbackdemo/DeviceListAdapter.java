package cn.com.talkbackdemo;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.com.talkbacklib.bean.DeviceBean;

/**
 * Created by wang l on 2017/6/22.
 */

public class DeviceListAdapter extends BaseAdapter {
    private List mList;
    private Context mContext;
    public DeviceListAdapter(Context context,List list) {
        if (list == null)
            list = new ArrayList();
        this.mList = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = View.inflate(mContext,R.layout.item_list_adapter,null);
        TextView tvText = (TextView) convertView.findViewById(R.id.tv_item_title);
        ImageView imageView = (ImageView) convertView.findViewById(R.id.iv_item_show);
        Object item = getItem(position);
        if (item instanceof DeviceBean){
            DeviceBean deviceBean = (DeviceBean) item;
            tvText.setText(deviceBean.getDevIP()+"\n"+deviceBean.getDevName()+"\n"+deviceBean.getDevMac());
            int color = 0;
            if (deviceBean.getDevStatus() == 0x01)
                color = mContext.getResources().getColor(R.color.colorBule);
            else
                color = mContext.getResources().getColor(R.color.colorRed);
            if (deviceBean.isChecked())
                imageView.setVisibility(View.VISIBLE);
            else
                imageView.setVisibility(View.INVISIBLE);
            convertView.setBackgroundColor(color);
        }
        return convertView;
    }

    public void setData(List<DeviceBean> deviceBeenList) {
        mList = deviceBeenList;
        notifyDataSetChanged();
    }
}
