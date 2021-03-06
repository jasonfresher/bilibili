package com.bilibili.live.details.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.bilibili.live.base.constants.ParamsConstant;
import com.bilibili.live.base.constants.RouteInfo;
import com.bilibili.live.details.ui.VideoDetailsActivity;
import com.billy.cc.core.component.CC;
import com.billy.cc.core.component.CCResult;
import com.billy.cc.core.component.IComponent;

/**
 * Created by jason on 2018/11/1.
 */

public class VideoDetailsComponent implements IComponent {
    @Override
    public String getName() {
        return RouteInfo.VIDEODETAILS_COMPONENT_NAME;
    }

    @Override
    public boolean onCall(CC cc) {
        Context context = cc.getContext();
        Intent intent = new Intent(context, VideoDetailsActivity.class);
        int av = cc.getParamItem(ParamsConstant.EXTRA_AV);
        String imgUrl = cc.getParamItem(ParamsConstant.EXTRA_IMG_URL);
        intent.putExtra(ParamsConstant.EXTRA_AV,av);
        intent.putExtra(ParamsConstant.EXTRA_IMG_URL,imgUrl);
        if (!(context instanceof Activity)) {
            //调用方没有设置context或app间组件跳转，context为application
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        CC.sendCCResult(cc.getCallId(), CCResult.success());
        return false;
    }
}
