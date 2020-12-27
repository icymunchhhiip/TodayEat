package com.jinsun.todayeat.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import com.jinsun.todayeat.R;
import com.jinsun.todayeat.databinding.ViewCustomInfoWindowBinding;
import com.jinsun.todayeat.model.poidetail.POIDetailInfo;
import com.naver.maps.map.overlay.InfoWindow;

class CustomInfoWindowAdapter extends InfoWindow.DefaultViewAdapter {

    private final Context mContext;
    private ViewCustomInfoWindowBinding mViewCustomInfoWindowBinding;
    private final POIDetailInfo mPOIDetailInfo;

    public CustomInfoWindowAdapter(@NonNull Context context, POIDetailInfo poiDetailInfo) {
        super(context);
        mContext = context;
        mPOIDetailInfo = poiDetailInfo;
    }


    @NonNull
    @Override
    protected View getContentView(@NonNull InfoWindow infoWindow) {
        if (mViewCustomInfoWindowBinding == null) {
            mViewCustomInfoWindowBinding = ViewCustomInfoWindowBinding.inflate(LayoutInflater.from(mContext));
        }

        if (infoWindow.getMarker() != null) {
            mViewCustomInfoWindowBinding.tvName.setText(mPOIDetailInfo.getName());
            String point = mPOIDetailInfo.getPoint();
            String tel = mPOIDetailInfo.getTel();
            String add = mPOIDetailInfo.getAdditionalInfo();
            add = add.replace(";","\n");
            String desc = mPOIDetailInfo.getDesc();

            if (!point.equals("")) {
                mViewCustomInfoWindowBinding.rbPoint.setRating(Float.parseFloat(point));
                mViewCustomInfoWindowBinding.rbPoint.setVisibility(View.VISIBLE);
            }
            if (!tel.equals("")) {
                mViewCustomInfoWindowBinding.tvTel.setText(tel);
                mViewCustomInfoWindowBinding.tvTel.setTextColor(getContext().getColor(R.color.colorFontDark));
            }
            if(!add.equals("")){
                mViewCustomInfoWindowBinding.tvAdditional.setText(add.substring(0,add.length()-1));
                mViewCustomInfoWindowBinding.tvAdditional.setTextColor(getContext().getColor(R.color.colorFontDark));
                mViewCustomInfoWindowBinding.tvAdditional.setVisibility(View.VISIBLE);
            }
            if (!desc.equals("")) {
                mViewCustomInfoWindowBinding.tvDesc.setText(desc);
                mViewCustomInfoWindowBinding.tvDesc.setTextColor(getContext().getColor(R.color.colorFontDark));
                mViewCustomInfoWindowBinding.tvDesc.setVisibility(View.VISIBLE);
            }
        }

        return mViewCustomInfoWindowBinding.getRoot();
    }

}
