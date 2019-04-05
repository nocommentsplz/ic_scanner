package com.avicenna.icscanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.lib.detector.ContourDetector;
import net.doo.snap.ui.PolygonView;

import android.support.v4.app.DialogFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * {@link ScanbotCameraView} integrated in {@link DialogFragment} example
 */
public class CameraDialogFragment extends DialogFragment implements PictureCallback {
    private ScanbotCameraView cameraView;
    private ImageView resultView;
    private static Integer counter = 0;
    private Activity activity = null;

    boolean flashEnabled = false;

    /**
     * Create a new instance of CameraDialogFragment
     */
    static CameraDialogFragment newInstance(Activity activity) {
        CameraDialogFragment fragment = new CameraDialogFragment();
        fragment.activity = activity;
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View baseView = getActivity().getLayoutInflater().inflate(R.layout.scanbot_camera_view, container, false);

        cameraView = (ScanbotCameraView) baseView.findViewById(R.id.camera);
        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.continuousFocus();
                        cameraView.useFlash(flashEnabled);
                    }
                }, 700);
            }
        });

        resultView = (ImageView) baseView.findViewById(R.id.result);

        ContourDetectorFrameHandler contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);

        PolygonView polygonView = (PolygonView) baseView.findViewById(R.id.polygonView);
        contourDetectorFrameHandler.addResultHandler(polygonView);

        AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);

        cameraView.addPictureCallback(this);

        baseView.findViewById(R.id.snap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraView.takePicture(false);
            }
        });

//        baseView.findViewById(R.id.flash).setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                flashEnabled = !flashEnabled;
//                cameraView.useFlash(flashEnabled);
//            }
//        });

        return baseView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    public void onPictureTaken(final byte[] image, int imageOrientation) {
        // Here we get the full image from the camera.
        // Implement a suitable async(!) detection and image handling here.
        // This is just a demo showing detected image as downscaled preview image.

        // Decode Bitmap from bytes of original image:
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 8; // use 1 for original size (if you want no downscale)!
        // in this demo we downscale the image to 1/8 for the preview.
        Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

        // rotate original image if required:
        if (imageOrientation > 0) {
            final Matrix matrix = new Matrix();
            matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
        }

        // Run document detection on original image:
        final ContourDetector detector = new ContourDetector();
        detector.detect(originalBitmap);
        final Bitmap documentImage = detector.processImageAndRelease(originalBitmap, detector.getPolygonF(), ContourDetector.IMAGE_FILTER_NONE);

        resultView.post(new Runnable() {
            @Override
            public void run() {
                File file = saveImage(documentImage);
                if (null != file) {
                    if (null != activity) {
                        Intent intent = new Intent();
                        intent.putExtra("FILEPATH", file.getAbsolutePath());
                        intent.putExtra("from_ic_scanner", true);
                        activity.setResult(RESULT_OK, intent);
                        activity.finish();//finishing activity
                    } else {
                        resultView.setImageBitmap(documentImage);
                        cameraView.continuousFocus();
                        cameraView.startPreview();
                    }
                } else {
                    handleError();
                }
            }
        });
    }

    private void handleError() {
        if (null != activity) {
            Intent intent = new Intent();
            intent.putExtra("FILEPATH", "");
            activity.setResult(RESULT_CANCELED, intent);
            activity.finish();//finishing activity
        }
    }

    private File saveImage(Bitmap documentImage) {
        String path;

        PackageManager packageManager = IcScannerPlugin.application.getPackageManager();
        String packageName = IcScannerPlugin.application.getPackageName();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            path = packageInfo.applicationInfo.dataDir;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        File file = new File(path/*IcScannerPlugin.application.getDataDir()*/, "IC_SCAN_" + (counter++) + ".png"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.

        try (FileOutputStream out = new FileOutputStream(file)) {
            documentImage.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is y
            out.flush();
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return file;
    }
}

