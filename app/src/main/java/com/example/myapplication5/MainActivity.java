package com.example.myapplication5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AutoFitTextureView mTextureView;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder mPreviewBuiler;
    private CameraCaptureSession mPreviewSession;
    private CameraManager manager;
    // 카메라 설정에 관한 변수
    private Size mPreviewSize;
    private StreamConfigurationMap map;
    private final String TAG = "TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // permission check
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 권한을 요청한다.
            requestPermissions(
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    200);
        } else {
            // 권한이 있을 경우에만 layout을 전개한다.
            initLayout();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode==200 && grantResults.length>0){
            boolean permissionGranted=true;
            for(int result : grantResults){
                if(result != PackageManager.PERMISSION_GRANTED){
                    // 사용자가 권한을 거절했다.
                    permissionGranted=false;
                    break;
                }
            }

            if(permissionGranted){
                // 권한 요청을 수락한 경우에 layout을 전개한다.
                initLayout();
            }else{
                Toast.makeText(this,
                        "권한을 수락해야 어플 이용이 가능합니다",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    private void initLayout() {
        setContentView(R.layout.activity_main);
//        mTextureView=findViewById(R.id.preview);
        mTextureView = (AutoFitTextureView) findViewById(R.id.texture);
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
    }

    // textureView가 화면에 정상적으로 출력되면 onSurfaceTextureAvailable()호출
    private TextureView.SurfaceTextureListener mSurfaceTextureListener=
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                    // cameraManager생성하는 메소드
                    openCamera(width, height);
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                }
            }; //TextureView.SurfaceTextureListener

//    private void openCamera(int width, int height) {
//        // CameraManager 객체 생성
//        manager= (CameraManager) getSystemService(CAMERA_SERVICE);
//        try {
//            // default 카메라를 선택한다.
//            String cameraId = manager.getCameraIdList()[0];
//
//            // 카메라 특성 알아보기
//            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//            int level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
//            Range<Integer> fps[]=characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
//            Log.d(TAG,"maximum frame rate is :"+fps[fps.length-1]+"hardware level = "+level);
//
//            // StreamConfigurationMap 객체에는 카메라의 각종 지원 정보가 담겨있다.
//            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//
//            // 미리보기용 textureview 화면크기용을 설정 <- 제공할 수 있는 최대크기를 가리킨다.
//            mPreviewSize=map.getOutputSizes(SurfaceTexture.class)[0];
//            Range<Integer> fpsForVideo[] = map.getHighSpeedVideoFpsRanges();
//            Log.e(TAG, "for video :"+fpsForVideo[fpsForVideo.length-1]+" preview Size width:"+mPreviewSize.getWidth()+", height"+height);
//
//            // 권한에 대한
//            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
//                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//                // 권한이 없으면 권한을 요청한다.
//                ActivityCompat.requestPermissions(this,
//                        new String[]{Manifest.permission.CAMERA,
//                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                        200);
//            }else {
//                // CameraDevice생성
//                manager.openCamera(cameraId, mStateCallback, null);
//            }
//
//        } catch (CameraAccessException e) {
//            Log.e(TAG, "openCamera() :카메라 디바이스에 정상적인 접근이 안됩니다.");
//        }
//    }

    /*비율 종횡비 시작*/
    private void openCamera(int width, int height) {
        // CameraManager 객체 생성
        manager= (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            // default 카메라를 선택한다.
            String cameraId = manager.getCameraIdList()[0];

            // 카메라 특성 알아보기
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            int level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            Range<Integer> fps[]=characteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Log.d(TAG,"maximum frame rate is :"+fps[fps.length-1]+"hardware level = "+level);

            // StreamConfigurationMap 객체에는 카메라의 각종 지원 정보가 담겨있다.
            map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size largest = Collections.max(
                    Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                    new CompareSizesByArea());

            Point displaySize = new Point();
            getWindowManager().getDefaultDisplay().getSize(displaySize);
            int rotatedPreviewWidth = width;
            int rotatedPreviewHeight = height;
            int maxPreviewWidth = displaySize.x;
            int maxPreviewHeight = displaySize.y;

            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                    rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                    maxPreviewHeight, largest);


            // 화면 방향에 따라 뷰를 그릴때, 가로 세로의 크기를 정한다.
            int orientation = getResources().getConfiguration().orientation;
            if(orientation == Configuration.ORIENTATION_LANDSCAPE){
                mTextureView.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            }else{
                mTextureView.setAspectRatio(
                        mPreviewSize.getHeight(), mPreviewSize.getWidth());
            }

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                // 권한이 없으면 권한을 요청한다.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        200);
            }else {
                // CameraDevice생성
                manager.openCamera(cameraId, mStateCallback, null);
            }


        } catch (CameraAccessException e) {
            Log.e(TAG, "openCamera() :카메라 디바이스에 정상적인 접근이 안됩니다.");
        }
    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e("Camera2", "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    private void configureTransform(int width, int height){
        if(mTextureView==null || mPreviewSize==null){
            return;
        }
        //단말기 방향이 방향이 변경되어있는지 확인
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix  = new Matrix();
        RectF viewRect = new RectF(0,0,width,height);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) height / mPreviewSize.getHeight(),
                    (float) width / mPreviewSize.getWidth());
            Log.d(TAG,"width :"+mPreviewSize.getWidth()+", height : "+mPreviewSize.getHeight());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }



    /*비율 종횡비 끝*/

    private CameraDevice.StateCallback mStateCallback
            = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            // CameraDevice 객체 생성
            cameraDevice = camera;
            // CaptureRequest.Builder객체와 CaptureSession 객체 생성하여 미리보기 화면을 실행시킨다.
            startPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private void startPreview() {
        if( cameraDevice==null ||
                !mTextureView.isAvailable() ||
                mPreviewSize==null){
            Log.e(TAG, "startPreview() fail , return ");
            return;
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        Surface surface = new Surface(texture);
        try {
            mPreviewBuiler = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            Log.e(TAG, "CaptureRequest 객체 생성 실패");
            e.printStackTrace();
        }
        mPreviewBuiler.addTarget(surface);

        try {
            cameraDevice.createCaptureSession(Arrays.asList(surface),  // / 미리보기용으로 위에서 생성한 surface객체 사용
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession=session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                        }
                    }, null );
        } catch (CameraAccessException e) {
            Log.e(TAG, "CaptureSession 객체 생성 실패");
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if(cameraDevice==null){
            return;
        }
        mPreviewBuiler.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());
        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuiler.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}