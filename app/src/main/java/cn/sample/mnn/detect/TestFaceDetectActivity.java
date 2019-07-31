package cn.sample.mnn.detect;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sample.mnn.detect.Facetest;
import cn.sample.mnn.detect.R;
import cn.sample.mnn.detect.Faceresults;
import cn.sample.mnn.detect.FaceDetectResult;


/**
 * Created by GDD on 2019/4/16.
 */

public class TestFaceDetectActivity extends Activity {
    private static final int SELECT_IMAGE = 2;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE"};


    private TextView infoResult;
    private ImageView imageView;
    private Bitmap yourSelectedImage = null;

    private Facetest m_detect = new Facetest();

    private String modelDir;

    TextView tvCheckResult;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_face);

        modelDir = Environment.getExternalStorageDirectory().toString() + "/facedetect";
        verifyStoragePermissions();


        infoResult = (TextView) findViewById(R.id.infoResult);
        tvCheckResult = (TextView) findViewById(R.id.tvCheckResult);
        imageView = (ImageView) findViewById(R.id.imageView);

        findViewById(R.id.buttonImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText editText1 =(EditText)findViewById(R.id.Registerinfo);
                editText1.setVisibility(View.GONE);
                Intent i = new Intent(Intent.ACTION_PICK);
                i.setType("image/*");
                startActivityForResult(i, SELECT_IMAGE);
            }
        });

        findViewById(R.id.buttonDetect).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                if (yourSelectedImage == null)
                    return;
                //检测流程
                int width = yourSelectedImage.getWidth();
                int height = yourSelectedImage.getHeight();
                byte[] imageDate = getPixelsRGBA(yourSelectedImage);

                long timeDetectFace = System.currentTimeMillis();
                String faceInfo = m_detect.FaceDetect(imageDate, width, height, 4);

                timeDetectFace = System.currentTimeMillis() - timeDetectFace;


                if (faceInfo == null || faceInfo.isEmpty()) {
                    infoResult.setText("未检测到人脸"+"图宽：" + width + "高：" + height + "人脸检测时间：" + timeDetectFace );
                }
                else {
                    faceInfo = faceInfo.replace("\\r\\n", "").replace("\n", "");
                    Bitmap drawBitmap = yourSelectedImage.copy(Bitmap.Config.ARGB_8888, true);

                    FaceDetectResult DetectResult = new Gson().fromJson(faceInfo, FaceDetectResult.class);
                    String detect_status = DetectResult.getDetecttatus();
                    if(detect_status.equals("-1"))
                    {
                        String info = "没有detect到人脸\n";
                        infoResult.setText("没有detect到人脸;"+"图宽：" + width + "高：" + height + "人脸检测时间：" + timeDetectFace  );
                    }
                    else
                    {
                        Faceresults entity = new Gson().fromJson(faceInfo, Faceresults.class);

                        List<Faceresults.ResultsBean> results = entity.getResults();
                        int faceNum = results.size();

                        int X, Y, W, H, status;
                        String cropdata;

                        infoResult.setText("图宽：" + width + "高：" + height + "人脸检测时间：" + timeDetectFace + " 数目：" + faceNum);

                        for (int i = 0; i < results.size(); i++) {
                            Faceresults.ResultsBean bean = results.get(i);
                            X = bean.getX();
                            Y = bean.getY();

                            W = bean.getW();
                            H = bean.getH();

                            // base64字符串转化成图片
                            Canvas canvas = new Canvas(drawBitmap);
                            Paint paint = new Paint();
                            paint.setColor(Color.RED);
                            paint.setStyle(Paint.Style.STROKE);//不填充
                            paint.setStrokeWidth(5);  //线的宽度
                            canvas.drawRect(X, Y, W + X, H + Y, paint);
                        }
                        imageView.setImageBitmap(drawBitmap);

                    }//*/
                }
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.

    }

    public void verifyStoragePermissions() {


        //检测是否有写的权限
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            boolean readDenied = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;
            boolean writeDenied = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED;


            if (readDenied || writeDenied) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_EXTERNAL_STORAGE);
            } else {
                copyAssets();
            }
        } else {
            copyAssets();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        copyAssets();
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        // Decode image size
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

        // The new size we want to scale to
        final int REQUIRED_SIZE = 400;

        // Find the correct scale value. It should be the power of 2.
        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE
                    || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        // Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
    }

    //提取像素点
    private byte[] getPixelsRGBA(Bitmap image) {
        // calculate how many bytes our image consists of
        int bytes = image.getByteCount();
        ByteBuffer buffer = ByteBuffer.allocate(bytes); // Create a new buffer
        image.copyPixelsToBuffer(buffer); // Move the byte data to the buffer
        byte[] temp = buffer.array(); // Get the underlying array containing the

        return temp;
    }

    private void copyAssets() {

        File dir = new File(modelDir);
        if (!dir.exists()) {
            boolean mkdirRet = dir.mkdirs();
        }

        try {
            copyBigDataToSD("det1.mnn");
            copyBigDataToSD("det2.mnn");
            copyBigDataToSD("det3-half.mnn");


        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean initRet = m_detect.FaceDetectionModelInit(modelDir + "/",40);


    }

    private void copyBigDataToSD(String name) throws IOException {
        InputStream inputStream = getAssets().open(name);

        File toFile = new File(modelDir + "/" + name);
        if (toFile.exists())
            return;
        toFile.createNewFile();
        OutputStream outputStream = new FileOutputStream(toFile);
        byte bt[] = new byte[1024];
        int c;
        while ((c = inputStream.read(bt)) > 0) {
            outputStream.write(bt, 0, c);
        }
        outputStream.flush();
        inputStream.close();
        outputStream.close();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();

            try {
                if (requestCode == SELECT_IMAGE) {
                    Bitmap bitmap = decodeUri(selectedImage);

                    Bitmap rgba = bitmap.copy(Bitmap.Config.ARGB_8888, true);

                    // resize to 227x227
                    //yourSelectedImage = Bitmap.createScaledBitmap(rgba, 227, 227, false);
                    yourSelectedImage = rgba;

                    imageView.setImageBitmap(yourSelectedImage);
                }
            } catch (FileNotFoundException e) {
                Log.e("MainActivity", "FileNotFoundException");
                return;
            }
        }

    }


    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private void exec(String cmd) {
        String[] cmdline = {"sh", "-c", cmd};
        try {
            Runtime.getRuntime().exec(cmdline);
//            toast("执行命令结束！");
        } catch (IOException e) {
            e.printStackTrace();
//            toast("执行命令异常！");
        }

    }

}
