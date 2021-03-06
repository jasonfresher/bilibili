package debug;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.bilibili.live.recommend.R;
import com.bilibili.live.recommend.ui.RecommendFragment;

import skin.support.SkinCompatManager;

/**
 * Created by jason on 2018/9/17.
 */

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content,RecommendFragment.newInstance(true));
        ft.commit();
    }
}
