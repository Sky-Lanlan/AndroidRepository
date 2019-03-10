package com.example.nanyu.faceyou;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;

import com.example.nanyu.faceyou.adapter.FaceAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FaceResActivity extends AppCompatActivity {


    private List<MyFace> myFaceList = new ArrayList<>();
    public static final String PATH = "/sdcard/faceYou/faceRepertory/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_res);


        initMyFace();
        RecyclerView recyclerView = findViewById(R.id.recycle_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        FaceAdapter adapter = new FaceAdapter(myFaceList);
        recyclerView.setAdapter(adapter);
    }


    private void initMyFace(){

        // 有多少个人的脸
        File rootFile = new File(PATH);
        File[] files = rootFile.listFiles();
//        List<String> filesPath = new ArrayList<>();
//        List<String> filesName = new ArrayList<>();


        for (File file:files) {
            String face_name;
            String count;
            face_name = file.getAbsolutePath().split("/")[4];

            File rootFile_ = new File(file.getAbsolutePath());

            count = "库存: "+rootFile_.listFiles().length;

            MyFace myFace = new MyFace(face_name, count);
            myFaceList.add(myFace);

        }










    }


}
