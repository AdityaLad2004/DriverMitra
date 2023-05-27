package com.example.drowsinessdetectorapp.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.drowsinessdetectorapp.R;
import com.example.drowsinessdetectorapp.event.AllEyesClosedEvent;
import com.example.drowsinessdetectorapp.event.AllEyesOpenedEvent;
import com.example.drowsinessdetectorapp.event.MouthClosedEvent;
import com.example.drowsinessdetectorapp.event.MouthOpenedEvent;
import com.example.drowsinessdetectorapp.util.CameraSourcePreview;
import com.example.drowsinessdetectorapp.util.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";


    private CameraSource mCameraSource = null;
    private CameraSourcePreview mPreview;
    private GraphicOverlay mGraphicOverlay;
    private FaceDetector mDetector;


    private MediaPlayer mediaPlayer,mediaPlayer2;



    private static final int RC_HANDLE_GMS = 1;
    private static final int RC_HANDLE_CAMERA_PERM = 2;


    private final AtomicBoolean updating = new AtomicBoolean(false);


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {

            Log.d(TAG, "Error in Camera-Permission: " + requestCode);


            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Camera-Permission is granted");


            createCameraSource();
            return;
        }


        Log.d(TAG, "Camera-Permission is not granted");


        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Drowsiness Detector Error")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .create()
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mPreview = (CameraSourcePreview) findViewById(R.id.preview);
        mGraphicOverlay = (GraphicOverlay) findViewById(R.id.faceOverlay);


        mediaPlayer = MediaPlayer.create(this,R.raw.alert);
        mediaPlayer2 = MediaPlayer.create(this,R.raw.yawn);




        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {

            createCameraSource();
        } else {

            requestCameraPermission();
        }
    }


    private void requestCameraPermission() {
        Log.i(TAG, "Requesting for Camera-Permission");


        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {

            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }


        final Activity thisActivity = this;


        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };


        Snackbar.make(mGraphicOverlay, R.string.permission_camera_rationale,
                        Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }


    private void createCameraSource() {

        Context context = getApplicationContext();


        mDetector = new FaceDetector.Builder(context)
                .setProminentFaceOnly(true)
                .setMode(FaceDetector.SELFIE_MODE)
                .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .setTrackingEnabled(true)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .build();


        mDetector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());


        if (!mDetector.isOperational()) {
            Log.i(TAG, "Face-Detector is not operational");
        }


        mCameraSource = new CameraSource.Builder(context, mDetector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT) //Front-Camera is On
                .setAutoFocusEnabled(true)
                .setRequestedFps(14.0f)
                .build();
    }


    private void startCameraSource() {

        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());


        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }


        if (mCameraSource != null) {
            try {

                mPreview.start(mCameraSource, mGraphicOverlay);
            } catch (IOException e) {

                Log.i(TAG, "Camera-Source cannot be started.", e);


                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        private Context mContext = MainActivity.this;
        @Override
        public Tracker<Face> create(Face face) {
            try {
                return new GraphicFaceTracker(mContext,mGraphicOverlay);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private static class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;
        private Context mContext;

        GraphicFaceTracker(Context mContext,GraphicOverlay overlay) throws IOException {
            this.mContext = mContext;
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay);
        }

        @Override
        public void onNewItem(int faceId, Face item) {

        }


        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(mContext,face);
        }


        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }


        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void allEyesClosed(AllEyesClosedEvent event) {
        mediaPlayer.start();
        releaseUpdatingLock();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void allEyesOpened(AllEyesOpenedEvent event) {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mouthOpened(MouthOpenedEvent event) {
        mediaPlayer2.start();
        releaseUpdatingLock();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void mouthClosed(MouthClosedEvent event) {

    }

//    private boolean catchUpdatingLock() {
//        return !updating.getAndSet(true);
//    }


    private void releaseUpdatingLock() {
        updating.set(false);
    }

    @Override
    protected void onResume() {
        super.onResume();


        EventBus.getDefault().register(this);


        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();


        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }


        if (mediaPlayer.isPlaying()) mediaPlayer.pause();
        if (mediaPlayer2.isPlaying()) mediaPlayer2.pause();



        mPreview.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        if (mediaPlayer.isPlaying()) mediaPlayer.stop();
        if (mediaPlayer2.isPlaying()) mediaPlayer2.stop();



        if(mDetector != null) {
            mDetector.release();
        }


        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

}