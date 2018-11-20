package com.dream.agingtest;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

public class CameraAgingTestFragment extends Fragment implements SurfaceHolder.Callback {

	private FinishCallback mCallback;
	
	private CountChangeCallback mCountChangeCallback;
    public CountChangeCallback getCountChangeCallback() {
		return mCountChangeCallback;
	}
	public void setCountChangeCallback(CountChangeCallback countChangeCallback) {
		mCountChangeCallback = countChangeCallback;
	}
	
    private Camera mCamera = null;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
//    private String resultString = Utilities.RESULT_FAIL;
    final static String TAG = "CameraAgingTest";
    private static Context mContext = null;
    public static final boolean LOG = true;

    private int flag;
    
    private int count = 0;
    private int max = 10;
    
    private boolean focusEnd = false;
    private boolean isPaused = false;

    public static CameraAgingTestFragment newInstance() {
		return new CameraAgingTestFragment();
	}
	
	public CameraAgingTestFragment() {
		// TODO Auto-generated constructor stub
	}
    
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		
		View view = inflater.inflate(R.layout.camera_aging_test, container, false);
		view.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				// TODO Auto-generated method stub
				return true;
			}
		});
		
		flag = Camera.CameraInfo.CAMERA_FACING_BACK;
        
        /* SurfaceHolder set */
        mSurfaceView = (SurfaceView) view.findViewById(R.id.mSurfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(CameraAgingTestFragment.this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mContext = getActivity();
		
		return view;
	}

    void autoFocus() {
    	try {
            if (mCamera != null) {
            	logd("autoFocus");
            	mCamera.startPreview();
            	mCamera.autoFocus(new AutoFocusCallback());
            	
            	handler.removeCallbacks(runnable);
            	handler.postDelayed(runnable, 2*1000);
            } else {
            	logd("autoFocus:null");
//                finish();
            	
//            	if (mCallback != null) {
//            		mCallback.onTestFinish(MainActivity.TEST_FAIL);
//				}

            	String cameraStr = "";
            	if (flag == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        			cameraStr = "FRONT";
        		} else {
        			cameraStr = "BACK";
        		}
            	ResultsInformation.getResultsInformation(
        				).addCameraAgingTestResult(true, 
        						cameraStr + ": auto focus fail");
        		initCamera();
            }
        } catch (Exception e) {
            fail(getString(R.string.autofocus_fail));
            loge(e);
        }
    }

    public void surfaceCreated(SurfaceHolder surfaceholder) {

//        logd("surfaceCreated");
        initCamera();
    }
    
    public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {

//        logd("surfaceChanged");
//        startCamera();
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {

//        logd("surfaceDestroyed");
        stopCamera();
    }
    
    @Override
	public void onResume() {
		super.onResume();
    	isPaused = false;
    	if (focusEnd) {
        	handler.removeCallbacks(runnable);
        	handler.postDelayed(runnable, 1000);
		} else {
			handler.removeCallbacks(runnable);
        	handler.postDelayed(runnable, 2*1000);
		}
    }
    @Override
	public void onPause() {
    	isPaused = true;
    	if (handler!=null&&runnable!=null) {
        	handler.removeCallbacks(runnable);
		}
		super.onPause();
    }
    
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			if (focusEnd) {
				String cameraStr = "";
            	if (flag == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        			cameraStr = "FRONT";
        		} else {
        			cameraStr = "BACK";
        		}
            	ResultsInformation.getResultsInformation(
        				).addCameraAgingTestResult(false, 
        						cameraStr + ": test pass");
			} else {
				String cameraStr = "";
            	if (flag == Camera.CameraInfo.CAMERA_FACING_FRONT) {
        			cameraStr = "FRONT";
        		} else {
        			cameraStr = "BACK";
        		}
            	ResultsInformation.getResultsInformation(
        				).addCameraAgingTestResult(false, 
        						cameraStr + ": auto focus fail");
			}
			
			initCamera();
		}
	};
    
    public final class AutoFocusCallback implements android.hardware.Camera.AutoFocusCallback {

        public void onAutoFocus(boolean focused, Camera camera) {
        	focusEnd = true;
        	if (!isPaused) {
        		
            	handler.removeCallbacks(runnable);
            	handler.postDelayed(runnable, 1000);
			}
        }
    };

    private void initCamera() {
    	focusEnd = false;
    	count ++;
    	if (count > max) {
    		logd("end");
    		pass();
    		return;
		}
    	if (mCountChangeCallback!=null) {
        	mCountChangeCallback.onCountChange(max, count, MainActivity.textColorNormal);
		}
    	
    	stopCamera();
    	
    	String cameraStr = "";
    	if (flag == Camera.CameraInfo.CAMERA_FACING_BACK) {
			flag = Camera.CameraInfo.CAMERA_FACING_FRONT;
			cameraStr = "FRONT";
		} else {
			flag = Camera.CameraInfo.CAMERA_FACING_BACK;
			cameraStr = "BACK";
		}
        try {
        	mCamera = null;
            mCamera = Camera.open(flag);
        } catch (Exception exception) {
        	exception.printStackTrace();
            toast(getString(R.string.cameraback_fail_open));
            mCamera = null;
        }

        if (mCamera == null) {
//        	if (mCallback != null) {
//        		mCallback.onTestFinish(MainActivity.TEST_FAIL);
//			}

    		ResultsInformation.getResultsInformation(
    				).addCameraAgingTestResult(true, 
    						cameraStr + ": open fail");
    		initCamera();
        } else {
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.setDisplayOrientation(90);
//                if (Build.VERSION.SDK_INT >= 23
//                		&& flag == Camera.CameraInfo.CAMERA_FACING_FRONT) {
//                    mCamera.setDisplayOrientation(270);
//				}
                startCamera();
            } catch (IOException exception) {
                mCamera.release();
                mCamera = null;
//                if (mCallback != null) {
//            		mCallback.onTestFinish(MainActivity.TEST_FAIL);
//				}
                ResultsInformation.getResultsInformation(
        				).addCameraAgingTestResult(true, 
        						cameraStr + ": open fail");
                initCamera();
            }
        }
    }
    
    private void startCamera() {

        if (mCamera != null) {
            try {
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setPictureFormat(PixelFormat.JPEG);
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                parameters.setRotation(CameraInfo.CAMERA_FACING_BACK);
                mCamera.setParameters(parameters);
                mCamera.startPreview();

                autoFocus();
            } catch (Exception e) {
                e.printStackTrace();
                loge(e);
            }
        }

    }

    private void stopCamera() {

        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;

            	handler.removeCallbacks(runnable);
            	
//                mCamera = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void fail(Object msg) {

        loge(msg);
        toast(msg);
//        setResult(RESULT_CANCELED);
//        resultString = Utilities.RESULT_FAIL;
//        finish();
    }

    void pass() {

        // toast(getString(R.string.test_pass));
//        setResult(RESULT_OK);
//        resultString = Utilities.RESULT_PASS;
    	if (mCallback != null) {
    		mCallback.onTestFinish(MainActivity.TEST_PASS);
		}

    }

    @Override
    public void onDestroy() {
    	logd("onDestroy");
//    	stopCamera(); //加了反而有问题
    	super.onDestroy();
    	stopCamera();
    }
    
    public FinishCallback getCallback() {
		return mCallback;
	}

	public void setCallback(FinishCallback callback) {
		mCallback = callback;
	}

	private void logd(Object d) {

        if (LOG)
            Log.d(TAG, d + "");
    }

    private void loge(Object e) {

        if (LOG)
            Log.e(TAG, e + "");
    }

    public void toast(Object s) {

        if (s == null)
            return;
        Toast.makeText(mContext, s + "", Toast.LENGTH_SHORT).show();
    }
}
