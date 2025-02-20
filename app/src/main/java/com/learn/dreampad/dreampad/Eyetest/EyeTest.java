package com.learn.dreampad.dreampad.Eyetest;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.learn.dreampad.dreampad.R;
import com.learn.dreampad.dreampad.screenTestHome.screenTestHome;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EyeTest extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    ImageView closeClick, Image1,Image2,Image3,Image4;
    TextView CountDisplay,StartButton,CoordinatesValues;
    LinearLayout StartLayout, PuzzleLayout;
    private CameraBridgeViewBase mOpenCvCameraView;
    private static final String    TAG                 = "OCVSample::Activity";
    // new Add
    private static final Scalar FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    public static final int        JAVA_DETECTOR       = 0;
    private static final int TM_SQDIFF = 0;
    private static final int TM_SQDIFF_NORMED = 1;
    private static final int TM_CCOEFF = 2;
    private static final int TM_CCOEFF_NORMED = 3;
    private static final int TM_CCORR = 4;
    private static final int TM_CCORR_NORMED = 5;


    private int learn_frames = 0;
    private Mat teplateR;
    private Mat teplateL;
    int method = 0;

    // matrix for zooming
    private Mat mZoomWindow;
    private Mat mZoomWindow2;

    private MenuItem mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
    // private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    Mat mRgbaF;
    Mat mRgbaT,imgCanny;
    private File mCascadeFile;
    private File                   mCascadeFileEye;
    private CascadeClassifier mJavaDetector;
    private CascadeClassifier      mJavaDetectorEye;


    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int mAbsoluteFaceSize = 0;

    private SeekBar mMethodSeekbar;
    private TextView mValue;

    double xCenter = -1;
    double yCenter = -1;
    String coordinatesGet = "";
    TextView Leftview, Rightview;
    double XCoordinate = 0;
    double XCoordinateWenStartClicked = 0;
    boolean IsStartButtonPress = false;
    boolean IsRight = false;
    boolean Isleft = false;
    int leftCount = 0;
    int RightCount = 0;
    int LpStatus = 0;
    int RpStatus = 0;
    int RpercentageFromBackend = 0;
    int LpercentageFromBackend = 0;
    int LeftImageClick = 0;
    int RightImageClick = 0;
    int handlerTime = 500;
    private long startTime;
    private long endTime;
    Handler handlerMAin;
    TextView LeftCountDisplayTopView,RightCountDisplayTopView ;
    TextView LeftCountView ,RightCountView;
    ProgressBar LeftBar,RightBar;
    String Type ="";
    static {
        if(OpenCVLoader.initDebug()){
            Log.e(TAG,"OpenCv loaded");
        }else {
            Log.e(TAG,"OpenCv NOT loaded");
        }
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");


                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "lbpcascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        // load cascade file from application resources
                        InputStream ise = getResources().openRawResource(R.raw.haarcascade_lefteye_2splits);
                        File cascadeDirEye = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFileEye = new File(cascadeDirEye, "haarcascade_lefteye_2splits.xml");
                        FileOutputStream ose = new FileOutputStream(mCascadeFileEye);

                        while ((bytesRead = ise.read(buffer)) != -1) {
                            ose.write(buffer, 0, bytesRead);
                        }
                        ise.close();
                        ose.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier");
                            mJavaDetector = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mJavaDetectorEye = new CascadeClassifier(mCascadeFileEye.getAbsolutePath());
                        if (mJavaDetectorEye.empty()) {
                            Log.e(TAG, "Failed to load cascade classifier for eye");
                            mJavaDetectorEye = null;
                        } else
                            Log.i(TAG, "Loaded cascade classifier from " + mCascadeFileEye.getAbsolutePath());

                        cascadeDir.delete();
                        cascadeDirEye.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }
                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.setCameraIndex(1);
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    int count = 0;
//    private int[] textureArrayWin = {
//            R.drawable.back,
//            R.drawable.email,
//            R.drawable.close,
//            R.drawable.info,
//            R.drawable.password,
//
//    };
    int[] AtusticImageArray;
    int[] NonAtusticImageArray;

    private int[] AtusticVehicalImageArray = {
            R.drawable.airplane,
            R.drawable.ambulance,
            R.drawable.bus,
            R.drawable.fire,
            R.drawable.hitruck,

    };
    private int[] NonAtusticVehicalImageArray = {
            R.drawable.bomb2,
            R.drawable.miletary,
            R.drawable.planes,
            R.drawable.taxi,
            R.drawable.trucks2,

    };

    private int[] AtusticAnimalImageArray = {
            R.drawable.baw,
            R.drawable.dina,
            R.drawable.dolphin,
            R.drawable.gem,
            R.drawable.panda,

    };
    private int[] NonAtusticAnimalImageArray = {
            R.drawable.dinas,
            R.drawable.dogs,
            R.drawable.frog,
            R.drawable.fish,
            R.drawable.rep1,

    };


    //Styling for double press back button
    private static long back_pressed;

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            //super.onBackPressed();
            finishAffinity();
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit", Toast.LENGTH_SHORT).show();
            back_pressed = System.currentTimeMillis();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eye_test);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        closeClick = (ImageView)findViewById(R.id.close);
        Image1 = (ImageView)findViewById(R.id.image1);
//        Image2 = (ImageView)findViewById(R.id.image2);
        Image3 = (ImageView)findViewById(R.id.image3);
//        Image4 = (ImageView)findViewById(R.id.image4);
        CountDisplay = (TextView)findViewById(R.id.ImageClickcount);
        StartLayout = (LinearLayout)findViewById(R.id.Start_Layout);
        PuzzleLayout = (LinearLayout)findViewById(R.id.puzzle_layout);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        StartButton = (TextView) findViewById(R.id.StartButton_click);
        CoordinatesValues = (TextView)findViewById(R.id.coordinates);
        mOpenCvCameraView.setCvCameraViewListener(this);

        // passing the info
        Bundle args = getIntent().getParcelableExtra("bundle");

        Type = args.getString("Type");
        // select imagesetOnType
        selectionAsType(Type);




        StartButton.setTextColor(getResources().getColor(R.color.red));
        StartButton.setEnabled(false);
        StartButton.setClickable(false);
        StartButton.setFocusable(false);
        Leftview = findViewById(R.id.left);
        Rightview = findViewById(R.id.right);
        // progress bar countdown
        LeftCountView = findViewById(R.id.percentageProgress1);
        RightCountView = findViewById(R.id.percentageProgress2);
        LeftBar = findViewById(R.id.circularProgressbar1);
        RightBar = findViewById(R.id.circularProgressbar2);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.circular);
        LeftBar.setProgress(0);   // Main Progress
        LeftBar.setSecondaryProgress(100); // Secondary Progress
        LeftBar.setMax(100); // Maximum Progress
        LeftBar.setProgressDrawable(drawable);

        Resources res2 = getResources();
        Drawable drawable2 = res2.getDrawable(R.drawable.circular);
        RightBar.setProgress(0);   // Main Progress
        RightBar.setSecondaryProgress(100); // Secondary Progress
        RightBar.setMax(100); // Maximum Progress
        RightBar.setProgressDrawable(drawable2);
        // end
        // top count views
        LeftCountDisplayTopView = findViewById(R.id.left_Count);
        RightCountDisplayTopView = findViewById(R.id.Right_Count);

        //        end
        buttonClicks();
       handlerMAin=new Handler();
        handlerMAin.post(new Runnable(){
            @Override
            public void run() {
                // upadte textView here
                final Handler handler=new Handler();
                handler.post(new Runnable(){
                    @Override
                    public void run() {
                        // upadte textView here
                        if(coordinatesGet != null && !coordinatesGet.equals("")){
                            CoordinatesValues.setText(coordinatesGet);
//                            Log.e(TAG, "2");
                            if(XCoordinate<600 && XCoordinate>220){
                                StartButton.setTextColor(getResources().getColor(R.color.green));
                                StartButton.setEnabled(true);
                                StartButton.setClickable(true);
                                StartButton.setFocusable(true);
                                StartButtonClick();
                                if(IsStartButtonPress == true){
                                    if(XCoordinate<XCoordinateWenStartClicked){
                                        Rightview.setTextColor(getResources().getColor(R.color.green));
                                        IsRight = true;
                                        Isleft = false;
                                        Leftview.setTextColor(getResources().getColor(R.color.red));
                                    }else {
                                        IsRight = false;
                                        Isleft = true;
                                        Leftview.setTextColor(getResources().getColor(R.color.green));
                                        Rightview.setTextColor(getResources().getColor(R.color.red));
                                    }
                                }
                            }else {
                                StartButton.setTextColor(getResources().getColor(R.color.green));
                                StartButton.setEnabled(false);
                                StartButton.setClickable(false);
                                StartButton.setFocusable(false);
                            }
                        }else {
                            XCoordinate = 0;
                            IsRight = false;
                            Isleft = false;
                            CoordinatesValues.setText(String .valueOf(XCoordinate));
                            Leftview.setTextColor(getResources().getColor(R.color.red ));
                            Rightview.setTextColor(getResources().getColor(R.color.red));
                        }


//                        Log.e(TAG, "1");
                        handler.postDelayed(this,30); // set time here to refresh textView
                    }
                });
                if(count != 10){
                    if(Isleft == true){
                        if(leftCount < 5){
                            leftCount ++;
                            LeftCountView.setText(String.valueOf(leftCount));
                            LpercentageFromBackend = LpercentageFromBackend + 20;

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    while (LpStatus < LpercentageFromBackend) {
                                        LpStatus += 1;

                                        handler.post(new Runnable() {

                                            @Override
                                            public void run() {
                                                // TODO Auto-generated method stub
                                                LeftBar.setProgress(LpStatus);
                                            }
                                        });
                                        try {
                                            // Sleep for 200 milliseconds.
                                            // Just to display the progress slowly
                                            Thread.sleep(20); //thread will take approx 3 seconds to finish
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();
                        }else {
                            LeftImageClick ++;
                            count ++;
                            Image1.setImageResource(getRandom(AtusticImageArray));
                            String NetcountDisp = "Net Count : "+count;
                            String LeftCountv = "Left Count : " +LeftImageClick;
                            CountDisplay.setText(NetcountDisp);
                            LeftCountDisplayTopView.setText(LeftCountv);

                            Toast.makeText(getBaseContext(), "OneImageClicked", Toast.LENGTH_SHORT).show();
                            leftCount =0;
                            LpercentageFromBackend =0;
                            LpStatus = 0;
                            LeftBar.setProgress(0);
                            LeftCountView.setText("0");
                        }

                    }
                    if(IsRight == true){
                        if(RightCount < 5){
                            RightCount ++;
                            RightCountView.setText(String.valueOf(RightCount));
                            RpercentageFromBackend = RpercentageFromBackend + 20;

                            new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    // TODO Auto-generated method stub
                                    while (RpStatus < RpercentageFromBackend) {
                                        RpStatus += 1;

                                        handler.post(new Runnable() {

                                            @Override
                                            public void run() {
                                                // TODO Auto-generated method stub
                                                RightBar.setProgress(RpStatus);
                                            }
                                        });
                                        try {
                                            // Sleep for 200 milliseconds.
                                            // Just to display the progress slowly
                                            Thread.sleep(20); //thread will take approx 3 seconds to finish
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).start();
                        }else {

                            RightImageClick ++;
                            count ++;
                            Image3.setImageResource(getRandom(NonAtusticImageArray));
                            String NetcountDisp = "Net Count : "+count;
                            String RightCountv = "Right Count : " +RightImageClick;
                            CountDisplay.setText(NetcountDisp);
                            RightCountDisplayTopView.setText(RightCountv);

                            Toast.makeText(getBaseContext(), "OneImageClicked", Toast.LENGTH_SHORT).show();
                            RightCount =0;
                            RpercentageFromBackend =0;
                            RpStatus = 0;
                            RightBar.setProgress(0);
                            RightCountView.setText("0");
                        }

                    }
                    handlerMAin.postDelayed(this,handlerTime);                 // set time here to refresh textView

                }else {

                    long elaspedTime = System.currentTimeMillis() - startTime;
                    SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
                    String time = String.format("%02d:%02d:%02d",
                            TimeUnit.MILLISECONDS.toHours(elaspedTime),
                            TimeUnit.MILLISECONDS.toMinutes(elaspedTime) -
                                    TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elaspedTime)), // The change is in this line
                            TimeUnit.MILLISECONDS.toSeconds(elaspedTime) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elaspedTime)));

                    Log.e("elaspedTime", String.valueOf(elaspedTime));
                    Log.e("time", time);
                    Intent intent = new Intent(EyeTest.this,EyeTestResult.class);
                    intent.putExtra("Count",String.valueOf(count));
                    intent.putExtra("LeftCount",String.valueOf(LeftImageClick));
                    intent.putExtra("RightCount",String.valueOf(RightImageClick));
                    intent.putExtra("time",time);
                    startActivity(intent);
                    finish();
                    mOpenCvCameraView.setVisibility(View.GONE);
                    mOpenCvCameraView.disableView();
                    handlerTime = 0;
                    handlerMAin.removeCallbacksAndMessages(null);                // set time here to refresh textView


                }

//                Toast.makeText(getBaseContext(), String.valueOf(XCoordinate), Toast.LENGTH_SHORT).show();

            }
        });


    }
    public void StartButtonClick(){
        // Start button press
        StartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mOpenCvCameraView.setVisibility(View.VISIBLE);
                StartLayout.setVisibility(View.GONE);
//                PuzzleLayout.setVisibility(View.INVISIBLE);
                String val = String.valueOf(XCoordinate);
                XCoordinateWenStartClicked = XCoordinate;
                Log.e(TAG, val);
                Toast.makeText(getBaseContext(), val, Toast.LENGTH_SHORT).show();
                IsStartButtonPress = true;
                startTime = System.currentTimeMillis();

            }
        });
    }
    public void selectionAsType(String Type){
        if(Type.equals("Vehicles")){
            AtusticImageArray = AtusticVehicalImageArray;
            NonAtusticImageArray = NonAtusticVehicalImageArray;
            Image1.setImageResource(R.drawable.airplane);
            Image3.setImageResource(R.drawable.planes);
        }
        if(Type.equals("Animals")){
            AtusticImageArray = AtusticAnimalImageArray;
            NonAtusticImageArray = NonAtusticAnimalImageArray;
            Image1.setImageResource(R.drawable.baw);
            Image3.setImageResource(R.drawable.frog);
        }
    }
    public void buttonClicks(){
        // close button press
        closeClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(EyeTest.this,EyeTestSelection.class);
                startActivity(intent);
                finish();
                handlerTime = 0;
                handlerMAin.removeCallbacksAndMessages(null);                // set time here to refresh textView

            }
        });
//        // image1  button press
//        Image1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                count = count+1;
//                if(count == 10){
//                    Intent intent = new Intent(EyeTest.this,EyeTestResult.class);
//                    startActivity(intent);
//                    finish();
//                }else{
//                    String countDisp = "Count : "+count;
//                    CountDisplay.setText(countDisp);
//                    Image1.setImageResource(getRandom(textureArrayWin));
////                    Image2.setImageResource(getRandom(textureArrayWin));
//                    Image3.setImageResource(getRandom(textureArrayWin));
////                    Image4.setImageResource(getRandom(textureArrayWin));
//                }
//
//
//
//            }
//        });
////        // image2  button press
////        Image2.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                count = count+1;
////                if(count == 10){
////                    Intent intent = new Intent(EyeTest.this,EyeTestResult.class);
////                    startActivity(intent);
////                    finish();
////                }else{
////                    String countDisp = "Count : "+count;
////                    CountDisplay.setText(countDisp);
////                    Image1.setImageResource(getRandom(textureArrayWin));
////                    Image2.setImageResource(getRandom(textureArrayWin));
////                    Image3.setImageResource(getRandom(textureArrayWin));
////                    Image4.setImageResource(getRandom(textureArrayWin));
////                }
////
////            }
////        });
//        // image3  button press
//        Image3.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                count = count+1;
//                if(count == 10){
//                    Intent intent = new Intent(EyeTest.this,EyeTestResult.class);
//                    startActivity(intent);
//                    finish();
//                }else{
//
//                    String countDisp = "Count : "+count;
//                    CountDisplay.setText(countDisp);
//                    Image1.setImageResource(getRandom(textureArrayWin));
////                    Image2.setImageResource(getRandom(textureArrayWin));
//                    Image3.setImageResource(getRandom(textureArrayWin));
////                    Image4.setImageResource(getRandom(textureArrayWin));
//                }
//            }
//        });
//        // image4  button press
//        Image4.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                count = count+1;
//                if(count == 10){
//                    Intent intent = new Intent(EyeTest.this,EyeTestResult.class);
//                    startActivity(intent);
//                    finish();
//                }else{
//                    String countDisp = "Count : "+count;
//                    CountDisplay.setText(countDisp);
//                    Image1.setImageResource(getRandom(textureArrayWin));
//                    Image2.setImageResource(getRandom(textureArrayWin));
//                    Image3.setImageResource(getRandom(textureArrayWin));
//                    Image4.setImageResource(getRandom(textureArrayWin));
//                }
//
//            }
//        });
    }
    public static int getRandom(int[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat(height,width, CvType.CV_8UC1);
        mRgba = new Mat(height,width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
        imgCanny = new Mat(height,width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
        mZoomWindow.release();
        mZoomWindow2.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }

        }

        if (mZoomWindow == null || mZoomWindow2 == null)
            CreateAuxiliaryMats();

        MatOfRect faces = new MatOfRect();

        if (mDetectorType == JAVA_DETECTOR) {
            if (mJavaDetector != null)
                mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
                        new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());
        }
        else {
            Log.e(TAG, "Detection method is not selected!");
        }

        Rect[] facesArray = faces.toArray();
        if(facesArray.length == 0){
            coordinatesGet = "";
            XCoordinate = 0;
        }else {
            for (int i = 0; i < facesArray.length; i++)
            {	Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(),
                    FACE_RECT_COLOR, 3);// green box
                xCenter = (facesArray[i].x + facesArray[i].width + facesArray[i].x) / 2;
                yCenter = (facesArray[i].y + facesArray[i].y + facesArray[i].height) / 2;
                Point center = new Point(xCenter, yCenter);

                Imgproc.circle(mRgba, center, 10, new Scalar(255, 0, 0, 255), 6);// center circle

                coordinatesGet = String.valueOf(center);
                XCoordinate = center.x;
                Log.e(TAG, coordinatesGet);
                Imgproc.putText(mRgba, "[" + center.x + "," + center.y + "]",
                        new Point(center.x + 20, center.y + 20),
                        Core.FONT_HERSHEY_SIMPLEX, 0.7, new Scalar(255, 255, 255,
                                255));

                Rect r = facesArray[i];
                // compute the eye area
                Rect eyearea = new Rect(r.x + r.width / 8,
                        (int) (r.y + (r.height / 4.5)), r.width - 2 * r.width / 8,
                        (int) (r.height / 3.0));
                // split it
                Rect eyearea_right = new Rect(r.x + r.width / 16,
                        (int) (r.y + (r.height / 4.5)),
                        (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
                Rect eyearea_left = new Rect(r.x + r.width / 16
                        + (r.width - 2 * r.width / 16) / 2,
                        (int) (r.y + (r.height / 4.5)),
                        (r.width - 2 * r.width / 16) / 2, (int) (r.height / 3.0));
                // draw the area - mGray is working grayscale mat, if you want to
                // see area in rgb preview, change mGray to mRgba
                Imgproc.rectangle(mRgba, eyearea_left.tl(), eyearea_left.br(),
                        new Scalar(255, 0, 0, 255), 2);
                Imgproc.rectangle(mRgba, eyearea_right.tl(), eyearea_right.br(),
                        new Scalar(255, 0, 0, 255), 2);

                if (learn_frames < 5) {
                    teplateR = get_template(mJavaDetectorEye, eyearea_right, 24);
                    teplateL = get_template(mJavaDetectorEye, eyearea_left, 24);
                    learn_frames++;
                } else {
                    // Learning finished, use the new templates for template
                    // matching
                    match_eye(eyearea_right, teplateR, method);
                    match_eye(eyearea_left, teplateL, method);

                }


                // cut eye areas and put them to zoom windows
//            Imgproc.resize(mRgba.submat(eyearea), mZoomWindow2, mZoomWindow2.size());
//            Imgproc.resize(mRgba.submat(eyearea_right), mZoomWindow, mZoomWindow.size());


            }
        }

        //        // Rotate mRgba 90 degrees
//        Core.transpose(mRgba, mRgbaT);
//        Imgproc.resize(mRgba, mRgbaF, mRgbaF.size(), 0,0, 0);
//        Core.flip(mRgbaF, mRgba, 1 );
//
//        Imgproc.cvtColor(mRgba,mGray,Imgproc.COLOR_RGB2GRAY);
//        Imgproc.Canny(mGray,imgCanny,50,150);
//        Mat thresh = new Mat();

//        Imgproc.threshold(mGray, thresh, 20, 255, Imgproc.THRESH_BINARY_INV);
        return mRgba;
    }
    private void CreateAuxiliaryMats() {
        if (mGray.empty())
            return;

        int rows = mGray.rows();
        int cols = mGray.cols();

        if (mZoomWindow == null) {
            mZoomWindow = mRgba.submat(rows / 2 + rows / 10, rows, cols / 2
                    + cols / 10, cols);
            mZoomWindow2 = mRgba.submat(0, rows / 2 - rows / 10, cols / 2
                    + cols / 10, cols);
        }

    }

    private void match_eye(Rect area, Mat mTemplate, int type) {
        Point matchLoc;
        Mat mROI = mGray.submat(area);
        int result_cols = mROI.cols() - mTemplate.cols() + 1;
        int result_rows = mROI.rows() - mTemplate.rows() + 1;
        // Check for bad template size
        if (mTemplate.cols() == 0 || mTemplate.rows() == 0) {
            return ;
        }
        Mat mResult = new Mat(result_cols, result_rows, CvType.CV_8U);

        switch (type) {
            case TM_SQDIFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_SQDIFF);
                break;
            case TM_SQDIFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_SQDIFF_NORMED);
                break;
            case TM_CCOEFF:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCOEFF);
                break;
            case TM_CCOEFF_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_CCOEFF_NORMED);
                break;
            case TM_CCORR:
                Imgproc.matchTemplate(mROI, mTemplate, mResult, Imgproc.TM_CCORR);
                break;
            case TM_CCORR_NORMED:
                Imgproc.matchTemplate(mROI, mTemplate, mResult,
                        Imgproc.TM_CCORR_NORMED);
                break;
        }

        Core.MinMaxLocResult mmres = Core.minMaxLoc(mResult);
        // there is difference in matching methods - best match is max/min value
        if (type == TM_SQDIFF || type == TM_SQDIFF_NORMED) {
            matchLoc = mmres.minLoc;
        } else {
            matchLoc = mmres.maxLoc;
        }

        Point matchLoc_tx = new Point(matchLoc.x + area.x, matchLoc.y + area.y);
        Point matchLoc_ty = new Point(matchLoc.x + mTemplate.cols() + area.x,
                matchLoc.y + mTemplate.rows() + area.y);

        Imgproc.rectangle(mRgba, matchLoc_tx, matchLoc_ty, new Scalar(255, 0, 255, 255));
        Rect rec = new Rect(matchLoc_tx,matchLoc_ty);


    }

    private Mat get_template(CascadeClassifier clasificator, Rect area, int size) {
        Mat template = new Mat();
        Mat mROI = mGray.submat(area);
        MatOfRect eyes = new MatOfRect();
        Point iris = new Point();
        Rect eye_template = new Rect();
        clasificator.detectMultiScale(mROI, eyes, 1.15, 2,
                Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                        | Objdetect.CASCADE_SCALE_IMAGE, new Size(30, 30),
                new Size());

        Rect[] eyesArray = eyes.toArray();
        for (int i = 0; i < eyesArray.length;) {
            Rect e = eyesArray[i];
            e.x = area.x + e.x;
            e.y = area.y + e.y;
            Rect eye_only_rectangle = new Rect((int) e.tl().x,
                    (int) (e.tl().y + e.height * 0.4), (int) e.width,
                    (int) (e.height * 0.6));
            mROI = mGray.submat(eye_only_rectangle);
            Mat vyrez = mRgba.submat(eye_only_rectangle);


            Core.MinMaxLocResult mmG = Core.minMaxLoc(mROI);

            Imgproc.circle(vyrez, mmG.minLoc, 2, new Scalar(255, 255, 255, 255), 2);
            iris.x = mmG.minLoc.x + eye_only_rectangle.x;
            iris.y = mmG.minLoc.y + eye_only_rectangle.y;
            eye_template = new Rect((int) iris.x - size / 2, (int) iris.y
                    - size / 2, size, size);
            Imgproc.rectangle(mRgba, eye_template.tl(), eye_template.br(),
                    new Scalar(255, 0, 0, 255), 2);
            template = (mGray.submat(eye_template)).clone();
            return template;
        }
        return template;
    }
}
