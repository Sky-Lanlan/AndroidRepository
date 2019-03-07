package com.example.nanyu.faceyou;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.bytedeco.javacpp.presets.opencv_core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int HAVE_PHOYO = 4;
    public static final int NOT_PHOYO = 5;
    public static int flag ;
    private Button take_photo;
    private Button choose;
    private Button submit;
    private ImageView picture;
    private Uri imageUri;
    private Bitmap faceTemp = null;
    private String nameTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face);
        getSupportActionBar().hide();

        flag = NOT_PHOYO;

        take_photo = findViewById(R.id.take_photo);
        choose = findViewById(R.id.choose);
        submit = findViewById(R.id.submit);
        picture = findViewById(R.id.new_face);

        take_photo.setOnClickListener(this);
        choose.setOnClickListener(this);
        submit.setOnClickListener(this);
        Toast.makeText(this, "create", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
//        faceTemp = picture.getDrawingCache();
    }



    @Override
    protected void onRestart() {
        super.onRestart();
        Toast.makeText(this, "restart", Toast.LENGTH_SHORT).show();
        if (faceTemp != null)
            picture.setImageBitmap(faceTemp);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){

            faceTemp = ((BitmapDrawable)picture.getDrawable()).getBitmap();
            moveTaskToBack(true);
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choose:
                if (ContextCompat.checkSelfPermission(AddFaceActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddFaceActivity.this,
                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                }else {
                    openAlbum();
                }

                break;
            case R.id.take_photo:
                //　创建File对象,用于存储拍照后的图片
                File outputImage = new File(getExternalCacheDir(),
                        "output_image.jpg");
                try {
                    if (outputImage.exists()) {
                        outputImage.delete();
                    }
                    outputImage.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    imageUri = FileProvider.getUriForFile(AddFaceActivity.this,
                            "com.example.cameraalbumtest.fileprovider", outputImage);
                } else {
                    imageUri = Uri.fromFile(outputImage);
                }
                //　启动相机
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, TAKE_PHOTO);
                break;
            case R.id.submit:
                //　提交人像
//                uploadFace();
            default:
        }
    }

    private void openAlbum() {

        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.
                        PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this, "You denied the permission",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    try {
                        //　显示拍摄后的图片
                        Bitmap origin = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(imageUri));

                        int width = origin.getWidth();
                        int height = origin.getHeight();
                        Matrix matrix = new Matrix();
                        matrix.setRotate(-90);
                        // 围绕原地进行旋转
                        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
                        picture.setImageBitmap(newBM);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case CHOOSE_PHOTO:
                if (resultCode == RESULT_OK){
                    // 判断版本号
                    if (Build.VERSION.SDK_INT >= 19){
                        //4.4 以上
                        handleImageOnKitKat(data);
                    }else {
                        handleImageBeforeKitKat(data);
                    }
                }
            default:
                break;

        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
    }

    private void displayImage(String imagePath) {
        if (imagePath != null){
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            picture.setImageBitmap(bitmap);
            flag = HAVE_PHOYO;
        }else {
            Toast.makeText(this, "failed to get image", Toast.LENGTH_SHORT).show();
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        //通过uri 和delectiong　来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection,null,null);
        if (cursor != null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.
                Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }


    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)){
            //　如果是document类型的uri,　则通过document id 处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];// 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.
                        EXTERNAL_CONTENT_URI,selection);

            }else if("com.android.providers.dowloads.documents".
                    equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content:" +
                        "//downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())){
            imagePath = getImagePath(uri,null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())){
            imagePath = uri.getPath();
        }
        displayImage(imagePath);//根据图片路径显示图片

    }


}
