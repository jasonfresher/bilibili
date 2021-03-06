package com.bilibili.live.home.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bilibili.live.base.RxBaseActivity;
import com.bilibili.live.base.constants.ConstantUtil;
import com.bilibili.live.base.constants.RouteInfo;
import com.bilibili.live.base.mvp.BasePresenter;
import com.bilibili.live.base.utils.PreferenceUtil;
import com.bilibili.live.home.R;
import com.bilibili.live.home.R2;
import com.bilibili.live.home.adapter.HomePagerAdapter;
import com.billy.cc.core.component.CC;
import com.billy.cc.core.component.CCResult;
import com.flyco.tablayout.SlidingTabLayout;

import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import skin.support.SkinCompatManager;

/**
 * Created by jason on 2018/9/17.
 */

public class MainActivity extends RxBaseActivity {

    @BindView(R2.id.toolbar)
    protected Toolbar mToolbar;

    @BindView(R2.id.viewpager)
    protected ViewPager mViewPager;

    @BindView(R2.id.tab_layout)
    protected SlidingTabLayout mTabLayout;

    @BindView(R2.id.iv_head_switch_mode)
    protected ImageView skinSwitch;

    private String[] fragmentRes = {
            RouteInfo.NETCASTING_COMPONENT_NAME,
            RouteInfo.RECOMMEND_COMPONENT_NAME,
            RouteInfo.BANGUMI_COMPONENT_NAME,
            RouteInfo.REGION_COMPONENT_NAME,
            RouteInfo.DISCOVERY_COMPONENT_NAME
    };

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main_layout);
//        FragmentManager fm = getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.replace(R.id.content, HomeFragment.newInstance(true));
//        ft.commit();
//    }

    @Override
    protected BasePresenter createPresenter() {
        return null;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.home_activity_layout;
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        setSwipeBackEnable(false);
        boolean flag = PreferenceUtil.getBoolean(ConstantUtil.SWITCH_MODE_KEY, false);
        if (flag) {
            skinSwitch.setImageResource(R.drawable.home_ic_switch_daily);
        } else {
            skinSwitch.setImageResource(R.drawable.home_ic_switch_night);
        }
        skinSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchNightMode();
            }
        });

        Observable.fromArray(fragmentRes)
                .map(new Function<String, Fragment>() {
                    @Override
                    public Fragment apply(String components) throws Exception {
                        CCResult result = CC.obtainBuilder(components).build().call();
                        Fragment fragment = result.getDataItem(components);
                        return fragment;
                    }
                }).toList()
                .compose(this.<List<Fragment>>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<Fragment>>() {
                    @Override
                    public void accept(List<Fragment> fragments) throws Exception {
                        HomePagerAdapter mHomeAdapter = new HomePagerAdapter(getSupportFragmentManager(),
                                getApplicationContext(),fragments);
                        mViewPager.setOffscreenPageLimit(fragments.size());
                        mViewPager.setAdapter(mHomeAdapter);
                        mTabLayout.setViewPager(mViewPager);
                        mViewPager.setCurrentItem(1);
                    }
                });
    }

    @Override
    protected void initToolBar() {

    }

    /**
     * 日夜间模式切换
     */
    private void switchNightMode() {
        boolean isNight = PreferenceUtil.getBoolean(ConstantUtil.SWITCH_MODE_KEY, false);
        if (isNight) {
            // 日间模式
            skinSwitch.setImageResource(R.drawable.home_ic_switch_night);
            SkinCompatManager.getInstance().restoreDefaultTheme();
            PreferenceUtil.putBoolean(ConstantUtil.SWITCH_MODE_KEY, false);
        } else {
            // 夜间模式
            skinSwitch.setImageResource(R.drawable.home_ic_switch_daily);
            SkinCompatManager.getInstance().loadSkin("night", SkinCompatManager.SKIN_LOADER_STRATEGY_BUILD_IN); // 后缀加载
            PreferenceUtil.putBoolean(ConstantUtil.SWITCH_MODE_KEY, true);
        }
    }
}
