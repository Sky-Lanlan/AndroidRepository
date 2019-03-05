package com.example.nanyu.pic_test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends AppCompatActivity {

    Button btnProcess;
    Bitmap srcBitmap;
    Bitmap grayBitmap;
    ImageView imgHuaishi;
    private static boolean flag = true;
    private static boolean isFirst = true;
    private static final String TAG = "MainActivity";

    //OpenCV库加载并初始化成功后的回调函数
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {

        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.i(TAG, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(TAG, "加载失败");
                    break;
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        btnProcess.setOnClickListener(new ProcessClickListener());
    }

    //初始化界面
    public void initUI(){
        btnProcess = findViewById(R.id.btn_gray_process);
        imgHuaishi = findViewById(R.id.img_huaishi);

        Log.i(TAG, "initUI sucess...");

    }

    //转换灰度图像
    public void procSrc2Gray(){
        Mat rgbMat = new Mat(); //原图
        Mat grayMat = new Mat(); //灰度图

        srcBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lean);

        //获得原图的宽与高
        grayBitmap = Bitmap.createBitmap(srcBitmap.getWidth(), srcBitmap.getHeight(), Bitmap.Config.RGB_565);

        //convert original bitmap to Mat, R G B.
        Utils.bitmapToMat(srcBitmap, rgbMat);
        //rgbMat to gray grayMat
        Imgproc.cvtColor(rgbMat, grayMat, Imgproc.COLOR_RGB2GRAY);
        Utils.matToBitmap(grayMat, grayBitmap); //convert mat to bitmap
        Log.i(TAG, "procSrc2Gray sucess...");
    }

    private class ProcessClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            if(isFirst)
            {
                procSrc2Gray();
                isFirst = false;
            }
            if(flag){
                imgHuaishi.setImageBitmap(grayBitmap);
                btnProcess.setText("查看原图");
                flag = false;
            }
            else{
                imgHuaishi.setImageBitmap(srcBitmap);
                btnProcess.setText("灰度化");
                flag = true;
            }
        }

    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
}
