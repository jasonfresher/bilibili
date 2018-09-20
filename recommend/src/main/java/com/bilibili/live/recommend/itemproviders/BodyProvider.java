package com.bilibili.live.recommend.itemproviders;

import android.widget.ImageView;

import com.bilibili.live.recommend.R;
import com.bilibili.live.recommend.bean.RecommendInfo;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.provider.BaseItemProvider;

public class BodyProvider extends BaseItemProvider<RecommendInfo.ResultBean.BodyBean,BaseViewHolder> {
    @Override
    public int viewType() {
        return RecommendInfo.ResultBean.BodyBean.VIEW_TYPE_ITEM_LOADED;
    }

    @Override
    public int layout() {
        return R.layout.recommend_item_body_layout;
    }

    @Override
    public void convert(BaseViewHolder helper, RecommendInfo.ResultBean.BodyBean bodyBean, int position) {
        helper.setText(R.id.video_title,bodyBean.getTitle());
        helper.setText(R.id.video_play_num,bodyBean.getPlay());
        helper.setText(R.id.video_review_count,bodyBean.getDanmaku());
        ImageView videoImg = helper.getView(R.id.video_preview);
        Glide.with(mContext)
                .load(bodyBean.getCover())
                .placeholder(com.bilibili.live.base.R.drawable.bili_default_image_tv)
                .error(R.drawable.bili_default_image_tv)
                .centerCrop()
                .dontAnimate()
                .into(videoImg);
    }

}