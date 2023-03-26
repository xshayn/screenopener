import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    private static final String TAG = "MainActivity";

    private Camera mCamera;
    private SurfaceView mPreview;
    private boolean mCameraCovered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mPreview = findViewById(R.id.preview);
        mPreview.getHolder().addCallback(this);

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCameraCovered = false;
                startCameraPreview();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    private void startCameraPreview() {
        mCamera = Camera.open();

        try {
            mCamera.setPreviewDisplay(mPreview.getHolder());
            mCamera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Error starting camera preview: " + e.getMessage());
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                int previewSizeWidth = parameters.getPreviewSize().width;
                int previewSizeHeight = parameters.getPreviewSize().height;
                byte[] previewData = new byte[previewSizeWidth * previewSizeHeight * 3 / 2];
                mCamera.addCallbackBuffer(previewData);
                mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        if (data == null) {
                            return;
                        }

                        int previewFormat = parameters.getPreviewFormat();
                        if (previewFormat != ImageFormat.NV21) {
                            return;
                        }

                        if (isCameraCovered(data, previewSizeWidth, previewSizeHeight)) {
                            unlockPhone();
                        }
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error setting up camera preview callback: " + e.getMessage());
            }
        }
    }

    private boolean isCameraCovered(byte[] data, int width, int height) {
        int sum = 0;

        for (int i = 0; i < width * height; i++) {
            sum += (data[i] & 0xff);
        }

        int average = sum / (width * height);

        if (average < 20) {
            if (!mCameraCovered) {
                mCameraCovered = true;
                return true;
            }
        } else {
            mCameraCovered = false;
        }

        return false;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)

    // Method to unlock the phone's keyguard
private void unlockPhone() {
KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
KeyguardManager.KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("MyApp");
keyguardLock.disableKeyguard();
}
}



