package com.example.nanyu.faceyou;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.bytedeco.javacv.*;
import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.indexer.*;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_imgproc.*;
import static org.bytedeco.javacpp.opencv_calib3d.*;
import static org.bytedeco.javacpp.opencv_objdetect.*;

import org.opencv.imgcodecs.Imgcodecs;


import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


//import static org.opencv.imgcodecs.Imgcodecs.;
import static org.opencv.imgcodecs.Imgcodecs.imwrite;
import static org.opencv.imgproc.Imgproc.CV_COMP_CORREL;

public class FaceActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private CameraBridgeViewBase openCvCameraView;
    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;
    private HashMap<String, Integer> faceMap;
    CvFont name;

    private static final String PATH = "/sdcard/faceYou/FaceDetect/";
    private static final String PATHREPER = "/sdcard/faceYou/faceRepertory/";


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    initializeOpenCVDependencies();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    private void initializeOpenCVDependencies() {


        try {
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
        }

        // And we are ready to go
        openCvCameraView.enableView();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);

        getSupportActionBar().hide();

        faceMap = readPicture();

        Toast.makeText(this, "请使用横屏，以便识别", Toast.LENGTH_LONG).show();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        openCvCameraView = new JavaCameraView(this, 1);
        setContentView(openCvCameraView);
        openCvCameraView.setCvCameraViewListener(this);


//        cvInitFont(name,CV_FONT_HERSHEY_COMPLEX,1.0,1.0,0,2,8);


    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {
        grayscaleImage.release();
    }

    @Override
    public Mat onCameraFrame(Mat aInputFrame) {

        Core.flip(aInputFrame, aInputFrame, 1);
        // Create a grayscale image
        Imgproc.cvtColor(aInputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);


        MatOfRect faces = new MatOfRect();

        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();


        Double confidenceResult = 0.0;
        String predictName = "";
        // If there are any faces found, draw a rectangle around it
        for (int i = 0; i < facesArray.length; i++) {
            Double maxConfidence = 0.0;

//            saveImage(aInputFrame, facesArray[i], "newFace");
            // 获取新的人脸

            saveImage(aInputFrame, facesArray[0], "newFace");


            for (String name : faceMap.keySet()) {

                double maxConfidenceEachPerson = 0;
                int number = faceMap.get(name);
                for (int j = 0; j < number; j++) {

                    String pathOld = name + "/" + j;
                    String pathNew = "newFace";


                    double tmp = cmpPic(pathNew, pathOld);
                    // 找出最大的置信度
                    maxConfidenceEachPerson = maxConfidenceEachPerson < tmp ? tmp : maxConfidenceEachPerson;
                }
                if (maxConfidenceEachPerson > maxConfidence) {
                    confidenceResult = maxConfidenceEachPerson;
                    predictName = name;
                }


            }


            Imgproc.rectangle(aInputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);

            // 暂未解决中文显示不了的问题
            Imgproc.putText(aInputFrame, predictName + ":" + confidenceResult, new Point(facesArray[i].x, facesArray[i].y - 3), FONT_HERSHEY_SCRIPT_COMPLEX, 4, new Scalar(0, 255, 255), 2);


        }


        return aInputFrame;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e("log_wons", "OpenCV init error");
        }
        initializeOpenCVDependencies();
    }

    /**
     * 特征保存
     *
     * @param image    mat
     * @param rect     人脸信息
     * @param fileName 　文件名
     * @return
     */
    public boolean saveImage(Mat image, Rect rect, String fileName) {
        try {

            // 把检测到的人脸重新定义大小后保存成文件
            Mat sub = image.submat(rect);
            Mat mat = new Mat();
            Size size = new Size(100, 100);
            Imgproc.resize(sub, mat, size);

            Imgcodecs.imwrite(PATH + fileName + ".jpg", mat);


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    /**
     * 提取特征
     *
     * @param fileName 文件名
     * @return 特征图片
     */
    public Bitmap getImage(String fileName) {
        try {
            return BitmapFactory.decodeFile(PATHREPER + fileName + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取手机人脸仓库
     *
     * @return 人名:图片数量
     */

    public HashMap<String, Integer> readPicture() {

        HashMap<String, Integer> faceMap = new HashMap<>();
        //　获取人名文件夹
        File nameFile = new File(PATHREPER);
        File[] nameFiles = nameFile.listFiles();
        for (File file : nameFiles) {
            String face_name;
            String count;
            face_name = file.getAbsolutePath().split("/")[4];
            //　获取人脸图片
            File[] faceFiles = new File(file.getAbsolutePath()).listFiles();
            faceMap.put(face_name, faceFiles.length);
        }
        return faceMap;
    }


    /**
     * 特征对比
     *
     * @param newFace 相机检测人脸特征 人名/i
     * @param oldFace 已保存人脸特征   人名/i
     * @return 相似度
     */
    public double cmpPic(String newFace, String oldFace) {
        int l_bins = 20;
        int hist_size[] = {l_bins};

        float v_ranges[] = {0, 100};
        float ranges[][] = {v_ranges};


        CvHistogram Histogram1 = CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);
        CvHistogram Histogram2 = CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1);


//        String c = Environment.getExternalStorageDirectory()+ oldFace + ".jpg";
//        opencv_core.IplImage Image2 = cvLoadImage(c, CV_LOAD_IMAGE_GRAYSCALE);
//        cvLoadImage(Environment.getExternalStorageDirectory().getAbsolutePath(),CV_LOAD_IMAGE_GRAYSCALE);
        opencv_core.IplImage Image2 = cvLoadImage(PATHREPER + oldFace + ".jpg", 0);
        opencv_core.IplImage Image1 = cvLoadImage(PATH + newFace + ".jpg", 0);

        opencv_core.IplImage imageArr1[] = {Image1};
        opencv_core.IplImage imageArr2[] = {Image2};


        cvCalcHist(imageArr1, Histogram1, 0, null);
        cvCalcHist(imageArr2, Histogram2, 0, null);

        cvNormalizeHist(Histogram1, 100.0);
        cvNormalizeHist(Histogram2, 100.0);

        return cvCompareHist(Histogram1, Histogram2, CV_COMP_CORREL);
    }


}
