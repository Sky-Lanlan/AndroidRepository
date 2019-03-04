package com.example.lan.mymusicapp.utils;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;


import com.example.lan.mymusicapp.adapter.MyFragmentPagerAdapter;
import com.example.lan.mymusicapp.R;
import com.example.lan.mymusicapp.playMode.Play;


public class ListActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ListActivity", "onStart start");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("ListActivity", "onStop start");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("ListActivity", "onPause start");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("ListActivity", "onResume start");
    }

    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyFragmentPagerAdapter myFragmentPagerAdapter;
    private TabLayout.Tab one;
    private TabLayout.Tab two;
    private Toast toast;
    private long mExitTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        //初始化视图
        initViews();



    }

    private void initViews() {

        //使用适配器将ViewPager与Fragment绑定在一起
        mViewPager = findViewById(R.id.viewPager);
        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(myFragmentPagerAdapter);

        //将TabLayout与ViewPager绑定在一起
        mTabLayout = findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabTextColors(ContextCompat.getColor(this, R.color.gray), ContextCompat.getColor(this, R.color.white));
        mTabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(this, R.color.white));
        //指定Tab的位置
        one = mTabLayout.getTabAt(0);
        two = mTabLayout.getTabAt(1);


        //设置Tab的图标，假如不需要则把下面的代码删去
//        one.setIcon(R.mipmap.ic_launcher);
//        two.setIcon(R.mipmap.ic_launcher);


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Play.savePlayInfo(this);

    }

    @SuppressLint("ShowToast")
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //判断用户是否点击了“返回键”
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //与上次点击返回键时刻作差
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                //大于2000ms则认为是误操作，使用Toast进行提示
                if (toast != null) {
                    toast.cancel();
                    toast = Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT);

                } else {
                    toast = Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT);

                }
                toast.show();
                //并记录下本次点击“返回键”的时刻，以便下次进行判断
                mExitTime = System.currentTimeMillis();
            } else {
                //小于2000ms则认为是用户确实希望退出程序-调用System.exit()方法进行退出
                onDestroy();
                System.exit(0);
            }
            return true;
        }


        return super.onKeyDown(keyCode, event);
    }
}



