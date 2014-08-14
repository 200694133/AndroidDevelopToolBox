package com.hanyanan.tools;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.hanyanan.tools.magicbox.view.NetworkImageView;

/**
 * Created by hanyanan on 2014/8/14.
 */
public class TestImageAdapter extends BaseAdapter {
    private final Context mContext;
    public TestImageAdapter(Context context){
        mContext = context;
    }
    @Override
    public int getCount() {
        return 200;
    }

    @Override
    public Object getItem(int position) {
        return TestDiskCacheThread.urls[position%TestDiskCacheThread.urls.length];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NetworkImageView iv = null;
        if(null != convertView){
            iv = (NetworkImageView) convertView;
        }else{
            iv = new NetworkImageView(mContext);
        }
        iv.setMaxHeight(200);
        iv.setMaxWidth(200);
        iv.setUrl((String)getItem(position));
        return iv;
    }
}
