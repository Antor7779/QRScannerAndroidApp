package com.example.qrscanner;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private FrameLayout cameraContainer;
    private SurfaceView cameraPreview;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private TextView qrCodeTextView;

    private static final int CAMERA_PERMISSION_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraContainer = findViewById(R.id.cameraContainer);
        qrCodeTextView = findViewById(R.id.qrCodeTextView);

        // Initialize the barcode detector
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        // Initialize the camera source
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK) // Specify rear camera
                .build();

        // Request camera permission if not granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            startCamera();
        }
    }

    private void startCamera() {
        cameraPreview = new SurfaceView(this);
        cameraContainer.addView(cameraPreview);

        cameraPreview.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    // Start the camera when the surface is created
                    cameraSource.start(cameraPreview.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                // Not used in this example
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // Release the camera when the surface is destroyed
                cameraSource.stop();
            }
        });

        // Set the barcode detector processor
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Not used in this example
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                // Retrieve detected QR codes
                final SparseArray<Barcode> qrCodes = detections.getDetectedItems();
                if (qrCodes.size() > 0) {
                    // Process the detected QR codes and display the result in the TextView
                    final String qrCodeData = qrCodes.valueAt(0).displayValue;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (qrCodeData != null) {
                                qrCodeTextView.setText(qrCodeData);
                            } else {
                                qrCodeTextView.setText("QR code data not available");
                            }
                        }
                    });

                    // Log the detected QR code data
                    for (int i = 0; i < qrCodes.size(); i++) {
                        Barcode qrCode = qrCodes.valueAt(i);
                        String format = qrCode.format == Barcode.QR_CODE ? "QR Code" : "Other Format";
                        String value = qrCode.rawValue;
                        int valueType = qrCode.valueFormat;
                        String valueTypeString = "Unknown";
                        if (valueType == Barcode.TEXT) {
                            valueTypeString = "Text";
                        } else if (valueType == Barcode.URL) {
                            valueTypeString = "URL";
                        } // Add other value types if needed
                        Log.d("QRCodeScanner", "Detected QR Code: Format=" + format + ", Value=" + value + ", ValueType=" + valueTypeString);
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                // Camera permission not granted, handle accordingly (e.g., show a message or close the activity)
                finish();
            }
        }
    }
}
