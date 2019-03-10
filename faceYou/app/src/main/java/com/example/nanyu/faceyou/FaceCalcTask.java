package com.example.nanyu.faceyou;


import android.graphics.Bitmap;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

import org.bytedeco.javacpp.opencv_core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.HashMap;

import static org.bytedeco.javacpp.helper.opencv_imgproc.cvCalcHist;
import static org.bytedeco.javacpp.opencv_core.CV_HIST_ARRAY;
import static org.bytedeco.javacpp.opencv_core.FONT_HERSHEY_SIMPLEX;
import static org.bytedeco.javacpp.opencv_core.IPL_DEPTH_8U;
import static org.bytedeco.javacpp.opencv_core.cvSetNew;
import static org.bytedeco.javacpp.opencv_core.doubleRand;
import static org.bytedeco.javacpp.opencv_imgproc.cvCompareHist;
import static org.bytedeco.javacpp.opencv_imgproc.cvNormalizeHist;
import static org.opencv.imgproc.Imgproc.CV_COMP_CORREL;

class FaceCalcTask extends
        AsyncTask<ArrayList<Object>, Integer, ArrayList<Object>> {

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    @Override
    protected ArrayList<Object> doInBackground(ArrayList<Object>... hashMaps) {


        ArrayList<Bitmap> list;

        ArrayList<Object> data = hashMaps[0];

        Rect rect = (Rect) data.get(0);
        Mat inputFrame = (Mat) data.get(1);
        HashMap<String, ArrayList<Bitmap>> faceMap = (HashMap<String, ArrayList<Bitmap>>) data.get(2);


        //　将新人脸转换到bitmap
        Bitmap newFace = Bitmap.createBitmap(inputFrame.cols(), inputFrame.rows(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(inputFrame, newFace);

        String predictName = "";
        double resultConfidence = 0;

        for (String _name : faceMap.keySet()) {
            double confidence = 0;
            list = faceMap.get(_name);
            for (Bitmap oldFace : list) {
                double tmp = calc(newFace, oldFace);
                confidence += tmp / list.size();
            }
            if (resultConfidence < confidence) {
                resultConfidence = confidence;
                predictName = _name;
            }
        }

        ArrayList<Object> result = new ArrayList<>();
        result.add(rect);
        result.add(inputFrame);
        result.add(predictName);
        result.add(resultConfidence);

        return result;


    }

    private double calc(Bitmap newFace, Bitmap oldFace) {

        int l_bins = 20;
        int hist_size[] = {l_bins};

        float v_ranges[] = {0, 100};
        float ranges[][] = {v_ranges};


        opencv_core.CvHistogram Histogram1;
        opencv_core.CvHistogram Histogram2;

        Histogram1 = new opencv_core.CvHistogram(opencv_core.CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1));
        Histogram2 = new opencv_core.CvHistogram(opencv_core.CvHistogram.create(1, hist_size, CV_HIST_ARRAY, ranges, 1));


        opencv_core.IplImage Image1 = opencv_core.IplImage.create(newFace.getWidth(), newFace.getHeight(), IPL_DEPTH_8U, 1);
        opencv_core.IplImage Image2 = opencv_core.IplImage.create(oldFace.getWidth(), oldFace.getHeight(), IPL_DEPTH_8U, 1);

        opencv_core.IplImage imageArr1[] = {Image1};
        opencv_core.IplImage imageArr2[] = {Image2};


        cvCalcHist(imageArr1, Histogram1, 0, null);
        cvCalcHist(imageArr2, Histogram2, 0, null);

        cvNormalizeHist(Histogram1, 100.0);
        cvNormalizeHist(Histogram2, 100.0);

        return cvCompareHist(Histogram1, Histogram2, CV_COMP_CORREL);

    }

    @Override
    protected void onPostExecute(ArrayList<Object> result) {
        super.onPostExecute(result);
        Rect rect = (Rect)result.get(0);
        Mat mat = (Mat) result.get(1);
        String name = (String) result.get(2);
        double confidence = (double) result.get(3);
        Imgproc.putText(mat, name + ":" +
                        confidence + "%", new Point(rect.x,
                        rect.y - 3), FONT_HERSHEY_SIMPLEX, 4,
                new Scalar(0, 255, 255), 2);


    }


}