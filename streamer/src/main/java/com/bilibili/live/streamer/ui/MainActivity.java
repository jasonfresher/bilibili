package com.bilibili.live.streamer.ui;

import com.bilibili.live.base.application.BaseApplication;
import com.bilibili.live.streamer.R;
import com.bilibili.live.streamer.jni.PushNative;
import com.bilibili.live.streamer.listener.LiveStateChangeListener;
import com.bilibili.live.streamer.pusher.LivePusher;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity implements LiveStateChangeListener {

	static final String URL = "rtmp://47.98.113.140:1935/live/jason";
	private LivePusher live;
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case PushNative.CONNECT_FAILED:
				Toast.makeText(BaseApplication.getInstance(), "连接失败", Toast.LENGTH_SHORT).show();
				//Log.d("jason", "连接失败..");
				break;
			case PushNative.INIT_FAILED:
				Toast.makeText(BaseApplication.getInstance(), "初始化失败", Toast.LENGTH_SHORT).show();
				break;	
			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surface);
		//相机图像的预览
		live = new LivePusher(surfaceView);
	}

	/**
	 * 开始直播
	 * @param
	 */
	public void mStartLive(View view) {
		Button btn = (Button)view;
		if(btn.getText().equals("开始直播")){
			live.startPush(URL,this);
			btn.setText("停止直播");
		}else{
			live.stopPush();
			btn.setText("开始直播");
		}
	}

	/**
	 * 切换摄像头
	 * @param btn
	 */
	public void mSwitchCamera(View btn) {
		live.switchCamera();
	}

	//改方法执行仍然在子线程中，发送消息到UI主线程
	@Override
	public void onError(int code) {
		handler.sendEmptyMessage(code);
	}

}
