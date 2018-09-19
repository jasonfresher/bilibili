package com.bilibili.live.recommend.mvp.presenter;

import com.bilibili.live.recommend.bean.RecommendInfo;
import com.bilibili.live.recommend.entity.RecommendMultiItem;
import com.bilibili.live.recommend.mvp.model.IRecommendModel;
import com.bilibili.live.recommend.mvp.model.RecommendModelImpl;
import com.bilibili.live.recommend.mvp.view.IRecommendView;
import com.trello.rxlifecycle2.LifecycleTransformer;

import java.util.List;

/**
 * Created by jason on 2018/9/17.
 */

public class RecommendPresenterImpl implements IRecommendPresenter,IRecommendModel.RecommendDataCallBackListener{

    private IRecommendView mRecommendView;
    private IRecommendModel mRecommendModel;

    public RecommendPresenterImpl(IRecommendView recommendView){
        mRecommendView = recommendView;
        mRecommendModel = new RecommendModelImpl(this);
    }

    @Override
    public void getRecommendBannerData() {
        mRecommendModel.getRecommendBannerInfos();
    }

    @Override
    public void getRecommendContentData() {
        mRecommendModel.getRecommendContentInfos();
    }

    @Override
    public void onBannerInfoSuccess(RecommendMultiItem recommendMultiItem) {
        if(mRecommendView != null)
            mRecommendView.loadRecommendBannerInfo(recommendMultiItem);
    }

    @Override
    public void onContentInfoSuccess(List<RecommendInfo.ResultBean> resultBeans) {
        if(mRecommendView != null)
            mRecommendView.loadRecommendContentInfo(resultBeans);
    }

    @Override
    public void onFailure(Throwable throwable) {
        mRecommendView.errorCallback(throwable);
    }

    @Override
    public <T> LifecycleTransformer<T> bindToLifecycle() {
        return mRecommendView.bindToLifecycle();
    }
}
