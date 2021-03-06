package com.bilibili.live.player.ui;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bilibili.live.base.constants.ParamsConstant;
import com.bilibili.live.player.R;
import com.bilibili.live.player.R2;
import com.bilibili.live.player.listener.DanmukuSwitchListener;
import com.bilibili.live.player.listener.VideoBackListener;
import com.bilibili.live.player.core.MediaController;
import com.bilibili.live.player.core.VideoGestureRelativeLayout;
import com.bilibili.live.player.core.VideoPlayerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by jason on 2018/10/10.
 */

public class VideoPlayerActivity extends AppCompatActivity{

    @BindView(R2.id.playerView)
    VideoPlayerView mPlayerView;

    @BindView(R2.id.media_controller)
    MediaController mMediaController;

    @BindView(R2.id.video_gesture_view)
    VideoGestureRelativeLayout mVideoGestureView;

    @BindView(R2.id.buffering_indicator)
    View mBufferingIndicator;

    @BindView(R2.id.video_start)
    RelativeLayout mVideoPrepareLayout;

    @BindView(R2.id.bili_anim)
    ImageView mAnimImageView;

    @BindView(R2.id.video_start_info)
    TextView mPrepareText;

    private AnimationDrawable mLoadingAnim;

    private String playUrl;

    private String title = "";

    private String startText;

    private boolean hardDecode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setBackgroundDrawable(null);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_video_player_layout);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        if (intent != null) {
            playUrl = intent.getStringExtra(ParamsConstant.EXTRA_PLAYER_URL);
            title = intent.getStringExtra(ParamsConstant.EXTRA_PLAYER_TITLE);
            hardDecode = intent.getBooleanExtra(ParamsConstant.EXTRA_PLAYER_HARDDECODE,false);
        }
        initAnimation();
        initMediaPlayer();
    }

    /**
     * 初始化加载动画
     */
    private void initAnimation() {
        mVideoPrepareLayout.setVisibility(View.VISIBLE);
//        startText = startText + "【完成】\n解析视频地址...【完成】\n全舰弹幕填装...";
//        mPrepareText.setText(startText);
        mLoadingAnim = (AnimationDrawable) mAnimImageView.getBackground();
        mLoadingAnim.start();
    }

    private void initMediaPlayer() {
        //配置播放器
        mMediaController.setTitle(title);
        mPlayerView.setMediaController(mMediaController);
        mPlayerView.setVideoGestureView(mVideoGestureView);
        mPlayerView.setHardDecode(hardDecode);
        mPlayerView.setMediaBufferingIndicator(mBufferingIndicator);
        mPlayerView.requestFocus();
        mPlayerView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
                    if (mBufferingIndicator != null) {
                        mBufferingIndicator.setVisibility(View.VISIBLE);
                    }
                } else if (what == IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
                    if (mBufferingIndicator != null) {
                        mBufferingIndicator.setVisibility(View.GONE);
                    }
                }
                return true;
            }
        });
        mPlayerView.setOnSeekCompleteListener(new IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(IMediaPlayer iMediaPlayer) {

            }
        });
        mPlayerView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer iMediaPlayer) {
                mPlayerView.pause();
            }
        });
        mPlayerView.setOnControllerEventsListener(new VideoPlayerView.OnControllerEventsListener() {
            @Override
            public void onVideoPause() {

            }

            @Override
            public void OnVideoResume() {

            }
        });
        //设置弹幕开关监听
        mMediaController.setDanmakuSwitchListener(new DanmukuSwitchListener() {
            @Override
            public void setDanmakushow(boolean isShow) {

            }
        });
        //设置返回键监听
        mMediaController.setVideoBackEvent(new VideoBackListener() {
            @Override
            public void back() {
                onBackPressed();
            }
        });
        mPlayerView.setVideoPath(playUrl);
        mPlayerView.setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                mLoadingAnim.stop();
//                startText = startText + "【完成】\n视频缓冲中...";
//                mPrepareText.setText(startText);
                mVideoPrepareLayout.setVisibility(View.GONE);
            }
        });
    }

}
