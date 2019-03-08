package com.example.nanyu.faceyou;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    protected Button faceRec;
    protected Button add_face;
    @SuppressLint("SdCardPath") String rootPath = "/sdcard/";
    protected String home = "faceYou/";

    protected String faceDetect = "faceDetect/";
    protected String faceRepertory = "faceRepertory/";




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //申请成功


                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();

                }
                break;
            default:
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        faceRec = findViewById(R.id.face_rec);
        add_face = findViewById(R.id.add_face);


        faceRec.setOnClickListener(this);
        add_face.setOnClickListener(this);


        if (ContextCompat.checkSelfPermission(
                MainActivity.this, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {

            //申请权限
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    }, 1);


        }

        //创建必要的文件夹
        newDirectory(rootPath, home);
        newDirectory(rootPath+home,faceDetect);
        newDirectory(rootPath+home, faceRepertory);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //人脸识别
            case R.id.face_rec:
                Intent intent1 = new Intent(MainActivity.this, FaceActivity.class);
                startActivity(intent1);
                break;
            case R.id.add_face:
                Intent intent2 = new Intent(MainActivity.this, AddFaceActivity.class);
                startActivity(intent2);
                break;
            //　人脸仓库
            case R.id.face_res:
                Intent intent3 = new Intent(MainActivity.this, FaceResActivity.class);
                startActivity(intent3);


            default:
                break;
        }
    }

    /**
     * 创建新文件夹
     *
     * @param _path  根目录
     * @param dirName　　要创建的目录
     */
    public Boolean newDirectory(String _path, String dirName) {
        File file = new File(_path  + dirName);
        try {
            if (!file.exists()) {
                if(!file.mkdir()){
                    return false;
                }else {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
        return false;
    }
}
