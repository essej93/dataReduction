package com.jrs251.csci366au23.datareduction;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;

public class MainActivity extends AppCompatActivity {

    // variables to hold views/bitmaps
    Bitmap originalImage;
    ImageView imageLeft, imageMiddle, imageRight;
    RadioGroup radioGroup;
    Bitmap cs420Left, cs420Mid, cs420Right;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // assigns views to variables using id's
        imageLeft = findViewById(R.id.imageViewLeft);
        imageMiddle = findViewById(R.id.imageViewMiddle);
        imageRight = findViewById(R.id.imageViewRight);
        radioGroup = findViewById(R.id.imageRadioGroup);

        originalImage = BitmapFactory.decodeResource(getResources(), R.drawable.flat_small);

        // sets radio group listener
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch(checkedId){
                    case R.id.CS420:
                        convertCS420();
                        break;
                    case R.id.CS411:
                        convert411();
                        break;
                    case R.id.intraP2:
                        IntraPredictionP2();

                        break;
                    case R.id.intraP4:
                        IntraPredictionP4();
                        break;
                }
            }
        });

        imageMiddle.setVisibility(View.GONE);
        imageRight.setVisibility(View.GONE);
        imageLeft.setImageResource(R.drawable.flat_small);
    }

    //Convert RGB image to YCbCr
    private Bitmap toYCbCr(Bitmap src)
    {
        int width=src.getWidth();
        int height=src.getHeight();

        //create new bitmap with the same size as src to store YCbCr, all YCbCr values are in [0, 255]
        // use R channel for Y, G channel for Cb and B channel for Cr
        Bitmap ycbcrBitmap = Bitmap.createBitmap((width), height,Bitmap.Config.ARGB_8888);
        for (int y=0; y<height;y++) {
            for (int x = 0; x < width; x++) {
                int pval = src.getPixel(x, y);
                int[] YCbCr = RGB2YCbCr(pval);
                ycbcrBitmap.setPixel(x, y, Color.rgb(YCbCr[0], YCbCr[1], YCbCr[2]));
            }
        }
        return (ycbcrBitmap);
    }

    // Convert RGB values to YCbCr, all are in [0,255]
    private int[] RGB2YCbCr(int color)
    {
        int[] YCbCr= new int[3];
        int R = Color.red(color);
        int G = Color.green(color);
        int B = Color.blue(color);

        // conversion
        YCbCr[0] = (int)(0.299*R+0.587*G+0.114*B);
        YCbCr[1] = (int)(-0.168736*R-0.331264*G+0.5*B + 128);
        YCbCr[2] = (int)(0.5*R-0.418688*G-0.081312*B + 128);
        return YCbCr;
    }

    private void convertCS420(){


//         checks if bitmap has already been generated so it doesnt remake it
//         if it has already been created just sets the image view to that bitmap

//        if(cs420Mid != null){
//            imageLeft.setImageBitmap(cs420Left);
//            imageMiddle.setImageBitmap(cs420Mid);
//            imageRight.setImageBitmap(cs420Right);
//        }

        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Bitmap Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // bit maps for Cb/Cr width and height are reduced by factor of 2
        Bitmap Cb420 = Bitmap.createBitmap(width/2, height/2, Bitmap.Config.ARGB_8888);
        Bitmap Cr420 = Bitmap.createBitmap(width/2, height/2, Bitmap.Config.ARGB_8888);

        // for loop to apply 4:2:0 sub sampling in the YCbCr colour space
        for(int x = 0; x < width/2; x++){
            for(int y = 0; y < height/2; y++){
                // we get the 4 pixels as we're only iterating through width/2 and height/2
                // we are required to get all pixels for the Y bitmap
                int p1 = imageBmap.getPixel(2 * x, 2 * y);
                int p2 = imageBmap.getPixel(2 * x + 1, 2 * y);
                int p3 = imageBmap.getPixel(2 * x, 2 * y + 1);
                int p4 = imageBmap.getPixel(2 * x + 1, 2 * y + 1);


                int avgCb = (Color.green(p1) + Color.green(p2) + Color.green(p3) + Color.green(p4)) / 4;
                int avgCr = (Color.blue(p1) + Color.blue(p2) + Color.blue(p3) + Color.blue(p4)) / 4;

                Y.setPixel(2 * x, 2 * y, Color.rgb(Color.red(p1), Color.red(p1), Color.red(p1)));
                Y.setPixel(2 * x + 1, 2 * y, Color.rgb(Color.red(p2), Color.red(p2), Color.red(p2)));
                Y.setPixel(2 * x, 2 * y + 1, Color.rgb(Color.red(p3), Color.red(p3), Color.red(p3)));
                Y.setPixel(2 * x + 1, 2 * y + 1, Color.rgb(Color.red(p4), Color.red(p4), Color.red(p4)));
                Cb420.setPixel(x,y,Color.rgb(avgCb,avgCb,avgCb));
                Cr420.setPixel(x,y,Color.rgb(avgCr,avgCr,avgCr));
            }
        }

        imageLeft.setImageBitmap(Y);
        imageMiddle.setImageBitmap(Cb420);
        imageRight.setImageBitmap(Cr420);

        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);


    }

    private void convert411(){


//         checks if bitmap has already been generated so it doesnt remake it
//         if it has already been created just sets the image view to that bitmap

//        if(cs420Mid != null){
//            imageLeft.setImageBitmap(cs420Left);
//            imageMiddle.setImageBitmap(cs420Mid);
//            imageRight.setImageBitmap(cs420Right);
//        }

        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Bitmap Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // bit maps for Cb/Cr width is reduced by factor of 4
        Bitmap Cb411 = Bitmap.createBitmap(width/4, height, Bitmap.Config.ARGB_8888);
        Bitmap Cr411 = Bitmap.createBitmap(width/4, height, Bitmap.Config.ARGB_8888);


        for(int x = 0; x < width/4; x++){
            for(int y = 0; y < height; y++){

                // since the width is reduces by a factor of 4 we need to get the row of pixels for Y
                int p1 = imageBmap.getPixel(4 * x, y);
                int p2 = imageBmap.getPixel(4 * x + 1, y);
                int p3 = imageBmap.getPixel(4 * x + 2, y );
                int p4 = imageBmap.getPixel(4 * x + 3, y);


                int avgCb = (Color.green(p1) + Color.green(p2) + Color.green(p3) + Color.green(p4)) / 4;
                int avgCr = (Color.blue(p1) + Color.blue(p2) + Color.blue(p3) + Color.blue(p4)) / 4;

                Y.setPixel(4 * x, y, Color.rgb(Color.red(p1), Color.red(p1), Color.red(p1)));
                Y.setPixel(4 * x + 1, y, Color.rgb(Color.red(p2), Color.red(p2), Color.red(p2)));
                Y.setPixel(4 * x+ 2, y, Color.rgb(Color.red(p3), Color.red(p3), Color.red(p3)));
                Y.setPixel(4 * x + 3, y, Color.rgb(Color.red(p4), Color.red(p4), Color.red(p4)));
                Cb411.setPixel(x,y,Color.rgb(avgCb,avgCb,avgCb));
                Cr411.setPixel(x,y,Color.rgb(avgCr,avgCr,avgCr));
            }
        }

        imageLeft.setImageBitmap(Y);
        imageMiddle.setImageBitmap(Cb411);
        imageRight.setImageBitmap(Cr411);

        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);

    }

    private void IntraPredictionP2(){

        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Bitmap Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap refY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap difY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){

                int xb = x;
                int yb = y-1;

                if(yb < 0) yb = 0;
                int X = Color.red(imageBmap.getPixel(x,y));
                int B = Color.red(imageBmap.getPixel(xb, yb));

                int diff = X-B+128;

                if(diff < 0) diff = 0;
                else if (diff > 255) diff =255;

                Y.setPixel(x,y, Color.rgb(X,X,X));
                refY.setPixel(x,y, Color.rgb(B,B,B));
                difY.setPixel(x,y,Color.rgb(diff,diff,diff));
            }
        }

        imageLeft.setImageBitmap(Y);
        imageMiddle.setImageBitmap(refY);
        imageRight.setImageBitmap(difY);

        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);


    }


    private void IntraPredictionP4(){

        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        Bitmap Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap refY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Bitmap difY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);


        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){

                int xa = x-1;
                int ya = y;

                int xb = x;
                int yb = y-1;

                int xc = x-1;
                int yc = y-1;

                if(yb < 0) yb = 0;
                if(xa < 0) xa = 0;
                if(xc < 0) xc = 0;
                if(yc < 0) yc = 0;

                int X = Color.red(imageBmap.getPixel(x,y));
                int A = Color.red(imageBmap.getPixel(xa, ya));
                int B = Color.red(imageBmap.getPixel(xb, yb));
                int C = Color.red(imageBmap.getPixel(xc, yc));

                int pred = A + B - C;


                int diff = X-B+128;

                if(diff < 0) diff = 0;
                else if (diff > 255) diff =255;

                Y.setPixel(x,y, Color.rgb(X,X,X));
                refY.setPixel(x,y, Color.rgb(pred,pred,pred));
                difY.setPixel(x,y,Color.rgb(diff,diff,diff));
            }
        }

        imageLeft.setImageBitmap(Y);
        imageMiddle.setImageBitmap(refY);
        imageRight.setImageBitmap(difY);

        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);

    }
}