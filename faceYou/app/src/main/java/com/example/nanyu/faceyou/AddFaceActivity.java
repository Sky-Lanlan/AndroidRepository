package com.example.nanyu.faceyou;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
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
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.bytedeco.javacpp.presets.opencv_core;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;

public class AddFaceActivity extends AppCompatActivity implements View.OnClickListener {



    public static final int TAKE_PHOTO = 1;
    public static final int CHOOSE_PHOTO = 2;
    public static final int HAVE_PHOYO = 4;
    public static final int NOT_PHOYO = 5;
    public static int flag ;
    private Button take_photo;
    private Button choose;
    private Button submit;
    private Button rotate;
    private EditText face_name;
    private ImageView picture;
    private Uri imageUri;
    private Bitmap faceTemp = null;
    private String nameTemp;
    private Mat grayscaleImage;
    private Mat aInputImage;
    private int absoluteFaceSize;

    private CascadeClassifier cascadeClassifier;


    private static final String PATH = "/sdcard/faceYou/faceRepertory/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_face);
        getSupportActionBar().hide();

        flag = NOT_PHOYO;

        take_photo = findViewById(R.id.take_photo);
        choose = findViewById(R.id.choose);
        submit = findViewById(R.id.submit);
        rotate = findViewById(R.id.rotateBu);
        picture = findViewById(R.id.new_face);
        face_name = findViewById(R.id.face_name);

        take_photo.setOnClickListener(this);
        choose.setOnClickListener(this);
        submit.setOnClickListener(this);
        rotate.setOnClickListener(this);
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
                faceTemp = null;
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
                // 清除保存的图片
                faceTemp = null;
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

                //　截取人像部分
                Bitmap bitmap = ((BitmapDrawable)picture.getDrawable()).getBitmap();

                if (uploadFace(FaceHelper.genFaceBitmap(bitmap),
                        face_name.getText().toString())){
                    Toast.makeText(this,"提交成功",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.rotateBu:
                //旋转图像

                Bitmap origin = ((BitmapDrawable)picture.getDrawable()).getBitmap();
                int width = origin.getWidth();
                int height = origin.getHeight();
                Matrix matrix = new Matrix();
                // 围绕原地进行旋转
                matrix.setRotate(90);
                Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
                picture.setImageBitmap(newBM);
                break;

            default:
        }
    }

    private Boolean uploadFace(Bitmap row_face, String name) {

        newDirectory(PATH,name);


        grayscaleImage = new Mat(row_face.getHeight(), row_face.getHeight(), CvType.CV_8UC4);
        absoluteFaceSize = (int) (row_face.getHeight() * 0.2);
        aInputImage = new Mat(row_face.getHeight(), row_face.getHeight(), CvType.CV_8UC4);
        Utils.bitmapToMat(row_face,aInputImage);
        Imgproc.cvtColor(aInputImage, grayscaleImage, Imgproc.COLOR_RGBA2RGB);


        MatOfRect faces = new MatOfRect();

        // Use the classifier to detect faces
        if (cascadeClassifier != null) {
            cascadeClassifier.detectMultiScale(grayscaleImage, faces, 1.1, 2, 2, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
        }
        Rect[] facesArray = faces.toArray();

        // If there are any faces found, draw a rectangle around it

        if (facesArray.length==0){
            Toast.makeText(this,"提交失败，请一定将人脸保持竖立",Toast.LENGTH_SHORT).show();
            return false;
        }else {
            saveImage(aInputImage, facesArray[0], name);
            Imgproc.rectangle(aInputImage, facesArray[0].tl(), facesArray[0].br(), new Scalar(0, 255, 0, 255), 3);
        }

        return true;
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
                        // 围绕原地进行旋转
                        matrix.setRotate(-90);
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

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.e("log_wons", "OpenCV init error");
        }
        initializeOpenCVDependencies();
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

    /**
     * 特征保存
     *
     * @param image    mat
     * @param rect     人脸信息
     * @param name 　人名
     * @return
     */
    public boolean saveImage(Mat image, Rect rect, String name) {
        try {


            File rootFile = new File(PATH+name+"/");
            File[] files = rootFile.listFiles();

            // 把检测到的人脸重新定义大小后保存成文件
            Mat sub = image.submat(rect);
            Mat mat = new Mat();
            Size size = new Size(100, 100);
            Imgproc.resize(sub, mat, size);
            Imgcodecs.imwrite(PATH+name+"/"+files.length+".jpg", mat);


            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }

    private void initializeOpenCVDependencies() {


        try {
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
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

    }

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


}
