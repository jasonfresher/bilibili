package com.bilibili.live.player.core;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;


import com.bilibili.live.player.R;
import com.bilibili.live.player.utils.ScreenResolution;

import java.io.IOException;

import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * 自定义VideoView
 */
public class VideoPlayerView extends SurfaceView implements IMediaPlayer {

  private static final String TAG = VideoPlayerView.class.getName();

  public static final int VIDEO_LAYOUT_ORIGIN = 0;

  public static final int VIDEO_LAYOUT_SCALE = 1;

  public static final int VIDEO_LAYOUT_STRETCH = 2;

  public static final int VIDEO_LAYOUT_ZOOM = 3;

  private static final int STATE_ERROR = -1;

  private static final int STATE_IDLE = 0;

  private static final int STATE_PREPARING = 1;

  private static final int STATE_PREPARED = 2;

  private static final int STATE_PLAYING = 3;

  private static final int STATE_PAUSED = 4;

  private static final int STATE_PLAYBACK_COMPLETED = 5;

  private static final int STATE_SUSPEND = 6;

  private static final int STATE_RESUME = 7;

  private static final int STATE_SUSPEND_UNSUPPORTED = 8;

  private Uri mUri;

  private long mDuration;

  private String mUserAgent;

  private int mCurrentState = STATE_IDLE;

  private int mTargetState = STATE_IDLE;

  private int mVideoLayout = VIDEO_LAYOUT_SCALE;

  private SurfaceHolder mSurfaceHolder = null;

  private tv.danmaku.ijk.media.player.IMediaPlayer mMediaPlayer = null;

  private int mVideoWidth;

  private int mVideoHeight;

  private int mVideoSarNum;

  private int mVideoSarDen;

  private int mSurfaceWidth;

  private int mSurfaceHeight;

  private MediaController mMediaController;

  private VideoGestureRelativeLayout mVideoGestureView;

  private View mMediaBufferingIndicator;

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener mOnCompletionListener;

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener mOnPreparedListener;

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener mOnErrorListener;

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener;

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener mOnInfoListener;

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener;

  private OnControllerEventsListener mOnControllerEventsListener;

  private int mCurrentBufferPercentage;

  private long mSeekWhenPrepared;

  private boolean mCanPause = true;

  private boolean mCanSeekBack = true;

  private boolean mCanSeekForward = true;

  private boolean mHardDecode;

  private Context mContext;

  tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener() {
    public void onVideoSizeChanged(tv.danmaku.ijk.media.player.IMediaPlayer mp, int width, int height,
                                   int sarNum, int sarDen) {
      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();
      mVideoSarNum = sarNum;
      mVideoSarDen = sarDen;
      if (mVideoWidth != 0 && mVideoHeight != 0) {
        setVideoLayout(mVideoLayout);
      }
    }
  };




  tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener mPreparedListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener() {
    public void onPrepared(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
      mCurrentState = STATE_PREPARED;
      mTargetState = STATE_PLAYING;

      if (mOnPreparedListener != null) {
        mOnPreparedListener.onPrepared(mMediaPlayer);
      }
      if (mMediaController != null) {
        mMediaController.setEnabled(true);
      }
      if(getDuration() > 0){
        attachVideoGestureView();
      }else if (mMediaController != null) {
          mMediaController.enableLiveUI();
      }
      mVideoWidth = mp.getVideoWidth();
      mVideoHeight = mp.getVideoHeight();

      long seekToPosition = mSeekWhenPrepared;

      if (seekToPosition != 0) {
        seekTo(seekToPosition);
      }
      if (mVideoWidth != 0 && mVideoHeight != 0) {
        setVideoLayout(mVideoLayout);
        if (mSurfaceWidth == mVideoWidth
            && mSurfaceHeight == mVideoHeight) {
          if (mTargetState == STATE_PLAYING) {
            start();
            if (mMediaController != null) {
              mMediaController.show();
            }
          } else if (!isPlaying()
              && (seekToPosition != 0 || getCurrentPosition() > 0)) {
            if (mMediaController != null) {
              mMediaController.show(0);
            }
          }
        }
      } else if (mTargetState == STATE_PLAYING) {
        start();
      }
    }
  };

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener mCompletionListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener() {

    public void onCompletion(tv.danmaku.ijk.media.player.IMediaPlayer mp) {

      mCurrentState = STATE_PLAYBACK_COMPLETED;
      mTargetState = STATE_PLAYBACK_COMPLETED;
      if (mMediaController != null) {
        mMediaController.hide();
      }
      if (mOnCompletionListener != null) {
        mOnCompletionListener.onCompletion(mMediaPlayer);
      }
    }
  };

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener mErrorListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener() {

    public boolean onError(tv.danmaku.ijk.media.player.IMediaPlayer mp, int framework_err, int impl_err) {

      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      if (mMediaController != null) {
        mMediaController.hide();
      }

      if (mOnErrorListener != null) {
        if (mOnErrorListener.onError(mMediaPlayer, framework_err,
            impl_err)) {
          return true;
        }
      }

      if (getWindowToken() != null) {
        int message = framework_err == tv.danmaku.ijk.media.player.IMediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK
                      ?
                      R.string.video_error_text_invalid_progressive_playback
                      : R.string.video_error_text_unknown;

        new AlertDialog.Builder(mContext)
            .setTitle(R.string.video_error_title)
            .setMessage(message)
            .setPositiveButton(
                    R.string.video_error_button,
                    new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int which) {
                        if (mOnCompletionListener != null) {
                          mOnCompletionListener.onCompletion(mMediaPlayer);
                        }
                      }
                    }).setCancelable(false).show();
      }
      return true;
    }
  };

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener() {

    public void onBufferingUpdate(tv.danmaku.ijk.media.player.IMediaPlayer mp, int percent) {

      mCurrentBufferPercentage = percent;
      if (mOnBufferingUpdateListener != null) {
        mOnBufferingUpdateListener.onBufferingUpdate(mp, percent);
      }
    }
  };

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener mInfoListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener() {

    @Override
    public boolean onInfo(tv.danmaku.ijk.media.player.IMediaPlayer mp, int what, int extra) {
      if (mOnInfoListener != null) {
        mOnInfoListener.onInfo(mp, what, extra);
      } else if (mMediaPlayer != null) {
        if (what == tv.danmaku.ijk.media.player.IMediaPlayer.MEDIA_INFO_BUFFERING_START) {
          if (mMediaBufferingIndicator != null) {
            mMediaBufferingIndicator.setVisibility(View.VISIBLE);
          }
        } else if (what == tv.danmaku.ijk.media.player.IMediaPlayer.MEDIA_INFO_BUFFERING_END) {
          if (mMediaBufferingIndicator != null) {
            mMediaBufferingIndicator.setVisibility(View.GONE);
          }
        }
      }
      return true;
    }
  };

  private tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener mSeekCompleteListener = new tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener() {

    @Override
    public void onSeekComplete(tv.danmaku.ijk.media.player.IMediaPlayer mp) {

      if (mOnSeekCompleteListener != null) {
        mOnSeekCompleteListener.onSeekComplete(mp);
      }
    }
  };

  SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

    public void surfaceChanged(SurfaceHolder holder, int format, int w,
                               int h) {
      mSurfaceHolder = holder;
      if (mMediaPlayer != null) {
        mMediaPlayer.setDisplay(mSurfaceHolder);
      }

      mSurfaceWidth = w;
      mSurfaceHeight = h;
      boolean isValidState = (mTargetState == STATE_PLAYING);
      boolean hasValidSize = (mVideoWidth == w && mVideoHeight == h);
      if (mMediaPlayer != null && isValidState && hasValidSize) {
        if (mSeekWhenPrepared != 0) {
          seekTo(mSeekWhenPrepared);
        }
        start();
        if (mMediaController != null) {
          if (mMediaController.isShowing()) {
            mMediaController.hide();
          }
          mMediaController.show();
        }
      }
    }


    public void surfaceCreated(SurfaceHolder holder) {
      mSurfaceHolder = holder;
      if (mMediaPlayer != null && mCurrentState == STATE_SUSPEND
          && mTargetState == STATE_RESUME) {
        mMediaPlayer.setDisplay(mSurfaceHolder);
        resume();
      } else {
        openVideo();
      }
    }


    public void surfaceDestroyed(SurfaceHolder holder) {
      mSurfaceHolder = null;
      if (mMediaController != null) {
        mMediaController.hide();
      }
      if (mCurrentState != STATE_SUSPEND) {
        release(true);
      }
    }
  };


  public VideoPlayerView(Context context) {
    super(context);
    initVideoView(context);
  }

  public VideoPlayerView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initVideoView(context);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
    int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
    setMeasuredDimension(width, height);
  }

  public void setVideoLayout(int layout) {
    LayoutParams lp = getLayoutParams();
    Pair<Integer, Integer> res = ScreenResolution.getResolution(mContext);
    int windowWidth = res.first, windowHeight = res.second;
    float windowRatio = windowWidth / (float) windowHeight;
    int sarNum = mVideoSarNum;
    int sarDen = mVideoSarDen;
    if (mVideoHeight > 0 && mVideoWidth > 0) {
      float videoRatio = ((float) (mVideoWidth)) / mVideoHeight;
      if (sarNum > 0 && sarDen > 0) {
        videoRatio = videoRatio * sarNum / sarDen;
      }
      mSurfaceHeight = mVideoHeight;
      mSurfaceWidth = mVideoWidth;
      if (VIDEO_LAYOUT_ORIGIN == layout && mSurfaceWidth < windowWidth
          && mSurfaceHeight < windowHeight) {
        lp.width = (int) (mSurfaceHeight * videoRatio);
        lp.height = mSurfaceHeight;
      } else if (layout == VIDEO_LAYOUT_ZOOM) {
        lp.width = windowRatio > videoRatio ? windowWidth
                                            : (int) (videoRatio * windowHeight);
        lp.height = windowRatio < videoRatio ? windowHeight
                                             : (int) (windowWidth / videoRatio);
      } else {
        boolean full = layout == VIDEO_LAYOUT_STRETCH;
        lp.width = (full || windowRatio < videoRatio) ? windowWidth
                                                      : (int) (videoRatio * windowHeight);
        lp.height = (full || windowRatio > videoRatio) ? windowHeight
                                                       : (int) (windowWidth / videoRatio);
      }
      setLayoutParams(lp);
//      View parent = (View) getParent();
//      if(parent != null && parent instanceof ViewGroup) {
//        LayoutParams parentLayoutParams = parent.getLayoutParams();
//        parentLayoutParams.height = lp.height;
//        parent.setLayoutParams(parentLayoutParams);
//      }
      getHolder().setFixedSize(mSurfaceWidth, mSurfaceHeight);
    }
    mVideoLayout = layout;
  }


  private void initVideoView(Context ctx) {
    mContext = ctx;
    mVideoWidth = 0;
    mVideoHeight = 0;
    mVideoSarNum = 0;
    mVideoSarDen = 0;
    getHolder().addCallback(mSHCallback);
    setFocusable(true);
    setFocusableInTouchMode(true);
    requestFocus();
    mCurrentState = STATE_IDLE;
    mTargetState = STATE_IDLE;
    if (ctx instanceof Activity) {
      ((Activity) ctx).setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
  }


  public boolean isValid() {

    return (mSurfaceHolder != null && mSurfaceHolder.getSurface().isValid());
  }


  public void setVideoPath(String path) {

    setVideoURI(Uri.parse(path));
  }


  public void setVideoURI(Uri uri) {

    mUri = uri;
    mSeekWhenPrepared = 0;
    openVideo();
    requestLayout();
    invalidate();
  }


  public void setUserAgent(String ua) {

    mUserAgent = ua;
  }


  public void stopPlayback() {

    if (mMediaPlayer != null) {
      mMediaPlayer.stop();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      mTargetState = STATE_IDLE;
    }
  }


  private void openVideo() {
    if (mUri == null || mSurfaceHolder == null) {
      return;
    }
    Intent i = new Intent("com.android.music.musicservicecommand");
    i.putExtra("command", "pause");
    mContext.sendBroadcast(i);

    release(false);
    try {
      mDuration = -1;
      mCurrentBufferPercentage = 0;
      // mMediaPlayer = new AndroidMediaPlayer();
      IjkMediaPlayer ijkMediaPlayer = null;
      if (mUri != null) {
        ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setLogEnabled(false);

        //                ijkMediaPlayer.setAvOption(AvFormatOption_HttpDetectRangeSupport.Disable);
        //                ijkMediaPlayer.setOverlayFormat(AvFourCC.SDL_FCC_RV32);
        //                ijkMediaPlayer.setMediaCodecEnabled(true);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", "48");
        //ijkMediaPlayer.setAvCodecOption("skip_loop_filter", "48");
        //ijkMediaPlayer.setFrameDrop(12);
        if(mHardDecode){
          ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
          ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
          ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
        }

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"framedrop",5);
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER,"max-fps",30);
        if (mUserAgent != null) {
          //ijkMediaPlayer.setAvFormatOption("user_agent", mUserAgent);
        }
      }
      mMediaPlayer = ijkMediaPlayer;
      assert mMediaPlayer != null;
      mMediaPlayer.setOnPreparedListener(mPreparedListener);
      mMediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
      mMediaPlayer.setOnCompletionListener(mCompletionListener);
      mMediaPlayer.setOnErrorListener(mErrorListener);
      mMediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);
      mMediaPlayer.setOnInfoListener(mInfoListener);
      mMediaPlayer.setOnSeekCompleteListener(mSeekCompleteListener);
      if (mUri != null) {
        mMediaPlayer.setDataSource(mUri.toString());
      }
      mMediaPlayer.setDisplay(mSurfaceHolder);
      mMediaPlayer.setScreenOnWhilePlaying(true);
      mMediaPlayer.prepareAsync();
      mCurrentState = STATE_PREPARING;
      attachMediaController();
    } catch (IOException ex) {
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer,
          tv.danmaku.ijk.media.player.IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    } catch (IllegalArgumentException ex) {
      mCurrentState = STATE_ERROR;
      mTargetState = STATE_ERROR;
      mErrorListener.onError(mMediaPlayer,
          tv.danmaku.ijk.media.player.IMediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
      return;
    }
  }


  public void setMediaController(MediaController controller) {
    if (mMediaController != null) {
      mMediaController.hide();
    }
    mMediaController = controller;
//    attachMediaController();
  }


  public void setMediaBufferingIndicator(View mediaBufferingIndicator) {
    if (mMediaBufferingIndicator != null) {
      mMediaBufferingIndicator.setVisibility(View.GONE);
    }
    mMediaBufferingIndicator = mediaBufferingIndicator;
  }

  private void attachVideoGestureView() {
    if (mMediaPlayer != null && mVideoGestureView != null) {
      mVideoGestureView.setVisibility(View.VISIBLE);
      mVideoGestureView.setMediaPlayer(this);
      mVideoGestureView.setEnabled(isInPlaybackState());
    }
  }

  private void attachMediaController() {
    if (mMediaPlayer != null && mMediaController != null) {
      mMediaController.setMediaPlayer(this);
      View anchorView = this.getParent() instanceof View ? (View) this
          .getParent() : this;
      mMediaController.setAnchorView(anchorView);
      mMediaController.setEnabled(isInPlaybackState());
    }
  }

  public View getMediaController(){
    return mMediaController;
  }


  public void setOnPreparedListener(tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener l) {
    mOnPreparedListener = l;
  }


  public void setOnCompletionListener(tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener l) {
    mOnCompletionListener = l;
  }


  public void setOnErrorListener(tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener l) {
    mOnErrorListener = l;
  }


  public void setOnBufferingUpdateListener(tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener l) {
    mOnBufferingUpdateListener = l;
  }


  public void setOnSeekCompleteListener(tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener l) {
    mOnSeekCompleteListener = l;
  }


  public void setOnInfoListener(tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener l) {
    mOnInfoListener = l;
  }


  public void setOnControllerEventsListener(OnControllerEventsListener l) {
    mOnControllerEventsListener = l;
  }


  private void release(boolean cleartargetstate) {
    if (mMediaPlayer != null) {
      mMediaPlayer.reset();
      mMediaPlayer.release();
      mMediaPlayer = null;
      mCurrentState = STATE_IDLE;
      if (cleartargetstate) {
        mTargetState = STATE_IDLE;
      }
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if(event.getAction() == MotionEvent.ACTION_DOWN) {
      toggleMediaControlsVisiblity();
    }
    return true;
  }


  @Override
  public boolean onTrackballEvent(MotionEvent event) {
    toggleMediaControl();
    return true;
  }

  public void toggleMediaControl() {
    if (isInPlaybackState() && mMediaController != null) {
      toggleMediaControlsVisiblity();
    }
  }

  public void setVideoGestureView(VideoGestureRelativeLayout videoGestureView) {
    if(mVideoGestureView != null) {
      mVideoGestureView.setVisibility(View.GONE);
    }
    mVideoGestureView = videoGestureView;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK
        && keyCode != KeyEvent.KEYCODE_VOLUME_UP
        && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN
        && keyCode != KeyEvent.KEYCODE_MENU
        && keyCode != KeyEvent.KEYCODE_CALL
        && keyCode != KeyEvent.KEYCODE_ENDCALL;
    if (isInPlaybackState() && isKeyCodeSupported
        && mMediaController != null) {
      if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK
          || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
          || keyCode == KeyEvent.KEYCODE_SPACE) {
        if (mMediaPlayer.isPlaying()) {
          pause();
          mMediaController.show();
        } else {
          start();
          mMediaController.hide();
        }
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
          && mMediaPlayer.isPlaying()) {
        pause();
        mMediaController.show();
      } else {
        toggleMediaControlsVisiblity();
      }
    }
    return super.onKeyDown(keyCode, event);
  }


  private void toggleMediaControlsVisiblity() {
    if (mMediaController.isShowing()) {
      mMediaController.hide();
    } else {
      mMediaController.show();
    }
  }

  public void showMediaControls(int timeout){
    if (isInPlaybackState() && mMediaController != null) {
      mMediaController.show(timeout);
    }
  }

  @Override
  public void start() {
    if (isInPlaybackState()) {
      mMediaPlayer.start();
      mCurrentState = STATE_PLAYING;
    }
    mTargetState = STATE_PLAYING;
    mOnControllerEventsListener.OnVideoResume();
  }





  @Override
  public void pause() {
    if (isInPlaybackState()) {
      if (mMediaPlayer.isPlaying()) {
        mMediaPlayer.pause();
        mCurrentState = STATE_PAUSED;
      }
    }
    mTargetState = STATE_PAUSED;
    mOnControllerEventsListener.onVideoPause();
  }


  public void resume() {
    if (mSurfaceHolder == null && mCurrentState == STATE_SUSPEND) {
      mTargetState = STATE_RESUME;
    } else if (mCurrentState == STATE_SUSPEND_UNSUPPORTED) {
      openVideo();
    }
  }





  @Override
  public int getDuration() {
    if (isInPlaybackState()) {
      if (mDuration > 0) {
        return (int) mDuration;
      }
      mDuration = mMediaPlayer.getDuration();
      return (int) mDuration;
    }
    mDuration = -1;
    return (int) mDuration;
  }


  @Override
  public int getCurrentPosition() {
    if (isInPlaybackState()) {
      long position = mMediaPlayer.getCurrentPosition();
      return (int) position;
    }
    return 0;
  }


  @Override
  public void seekTo(long msec) {
    if (isInPlaybackState()) {
      mMediaPlayer.seekTo(msec);
      mSeekWhenPrepared = 0;
    } else {
      mSeekWhenPrepared = msec;
    }
  }


  @Override
  public boolean isPlaying() {
    return isInPlaybackState() && mMediaPlayer.isPlaying();
  }


  @Override
  public int getBufferPercentage() {
    if (mMediaPlayer != null) {
      return mCurrentBufferPercentage;
    }
    return 0;
  }


  public int getVideoWidth() {
    return mVideoWidth;
  }


  public int getVideoHeight() {
    return mVideoHeight;
  }


  protected boolean isInPlaybackState() {
    return (mMediaPlayer != null && mCurrentState != STATE_ERROR
        && mCurrentState != STATE_IDLE && mCurrentState != STATE_PREPARING);
  }


  public boolean canPause() {
    return mCanPause;
  }


  public boolean canSeekBackward() {
    return mCanSeekBack;
  }


  public boolean canSeekForward() {
    return mCanSeekForward;
  }


  public void setHardDecode(boolean hardDecode) {
    this.mHardDecode = hardDecode;
  }


  public interface OnControllerEventsListener {
    void onVideoPause();
    void OnVideoResume();
  }
}
