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
    Bitmap cs420Y, cs420Cb, cs420Cr;
    Bitmap cs411Y, cs411Cb, cs411Cr;
    Bitmap P2Y, P2refY, P2difY;
    Bitmap P4Y, P4refY, P4difY;

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

        // hides image views to only show starting original image
        imageMiddle.setVisibility(View.GONE);
        imageRight.setVisibility(View.GONE);
        imageLeft.setImageResource(R.drawable.flat_small); // sets image view to default image
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

    // function to apply 4:2:0 chroma sub sampling
    private void convertCS420(){


        //checks if bitmap has already been generated so it doesnt remake it
        //if it has already been created just sets the image view to that bitmap

        if(cs420Y != null){
            imageLeft.setImageBitmap(cs420Y);
            imageMiddle.setImageBitmap(cs420Cb);
            imageRight.setImageBitmap(cs420Cr);
            return;
        }

        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        cs420Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // bit maps for Cb/Cr width and height are reduced by factor of 2
        cs420Cb = Bitmap.createBitmap(width/2, height/2, Bitmap.Config.ARGB_8888);
        cs420Cr = Bitmap.createBitmap(width/2, height/2, Bitmap.Config.ARGB_8888);

        // for loop to apply 4:2:0 sub sampling in the YCbCr colour space
        for(int x = 0; x < width/2; x++){
            for(int y = 0; y < height/2; y++){
                // we get the 4 pixels as we're only iterating through width/2 and height/2
                // we are required to get all pixels for the Y bitmap
                int p1 = imageBmap.getPixel(2 * x, 2 * y);
                int p2 = imageBmap.getPixel(2 * x + 1, 2 * y);
                int p3 = imageBmap.getPixel(2 * x, 2 * y + 1);
                int p4 = imageBmap.getPixel(2 * x + 1, 2 * y + 1);

                // gets average pixel values
                int avgCb = (Color.green(p1) + Color.green(p2) + Color.green(p3) + Color.green(p4)) / 4;
                int avgCr = (Color.blue(p1) + Color.blue(p2) + Color.blue(p3) + Color.blue(p4)) / 4;

                // sets new pixel values
                cs420Y.setPixel(2 * x, 2 * y, Color.rgb(Color.red(p1), Color.red(p1), Color.red(p1)));
                cs420Y.setPixel(2 * x + 1, 2 * y, Color.rgb(Color.red(p2), Color.red(p2), Color.red(p2)));
                cs420Y.setPixel(2 * x, 2 * y + 1, Color.rgb(Color.red(p3), Color.red(p3), Color.red(p3)));
                cs420Y.setPixel(2 * x + 1, 2 * y + 1, Color.rgb(Color.red(p4), Color.red(p4), Color.red(p4)));
                cs420Cb.setPixel(x,y,Color.rgb(avgCb,avgCb,avgCb));
                cs420Cr.setPixel(x,y,Color.rgb(avgCr,avgCr,avgCr));
            }
        }

        imageLeft.setImageBitmap(cs420Y);
        imageMiddle.setImageBitmap(cs420Cb);
        imageRight.setImageBitmap(cs420Cr);

        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);


    }

    // function to apply 4:2:0 chroma sub sampling
    private void convert411(){

        //checks if bitmap has already been generated so it doesnt remake it
        //if it has already been created just sets the image view to that bitmap

        if(cs411Y != null){
            imageLeft.setImageBitmap(cs411Y);
            imageMiddle.setImageBitmap(cs411Cb);
            imageRight.setImageBitmap(cs411Cr);
            return;
        }

        // converts image to YCbCr
        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // creates Y bitmap
        cs411Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // bit maps for Cb/Cr width is reduced by factor of 4
        cs411Cb = Bitmap.createBitmap(width/4, height, Bitmap.Config.ARGB_8888);
        cs411Cr = Bitmap.createBitmap(width/4, height, Bitmap.Config.ARGB_8888);

        // for loop iterates through pixels
        for(int x = 0; x < width/4; x++){
            for(int y = 0; y < height; y++){

                // since the width is reduces by a factor of 4 we need to get the row of pixels for Y
                int p1 = imageBmap.getPixel(4 * x, y);
                int p2 = imageBmap.getPixel(4 * x + 1, y);
                int p3 = imageBmap.getPixel(4 * x + 2, y );
                int p4 = imageBmap.getPixel(4 * x + 3, y);

                // finds average pixel values
                int avgCb = (Color.green(p1) + Color.green(p2) + Color.green(p3) + Color.green(p4)) / 4;
                int avgCr = (Color.blue(p1) + Color.blue(p2) + Color.blue(p3) + Color.blue(p4)) / 4;

                // sets Y pixels
                cs411Y.setPixel(4 * x, y, Color.rgb(Color.red(p1), Color.red(p1), Color.red(p1)));
                cs411Y.setPixel(4 * x + 1, y, Color.rgb(Color.red(p2), Color.red(p2), Color.red(p2)));
                cs411Y.setPixel(4 * x+ 2, y, Color.rgb(Color.red(p3), Color.red(p3), Color.red(p3)));
                cs411Y.setPixel(4 * x + 3, y, Color.rgb(Color.red(p4), Color.red(p4), Color.red(p4)));

                // sets Cb and Cr pixels
                cs411Cb.setPixel(x,y,Color.rgb(avgCb,avgCb,avgCb));
                cs411Cr.setPixel(x,y,Color.rgb(avgCr,avgCr,avgCr));
            }
        }

        // updated image views
        imageLeft.setImageBitmap(cs411Y);
        imageMiddle.setImageBitmap(cs411Cb);
        imageRight.setImageBitmap(cs411Cr);

        // shows image views
        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);

    }

    // function uses P2 intra prediction to generate bitmap
    private void IntraPredictionP2(){

        //checks if bitmap has already been generated so it doesnt remake it
        //if it has already been created just sets the image view to that bitmap

        if(P2Y != null){
            imageLeft.setImageBitmap(P2Y);
            imageMiddle.setImageBitmap(P2refY);
            imageRight.setImageBitmap(P2difY);
            return;
        }

        // converts iamge to YCbCr
        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // creates bit maps
        P2Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        P2refY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        P2difY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // for loop iterates through pixels
        // uses P2 prediction to generate bitmap
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){

                // finds x/y index of b pixel
                int xb = x;
                int yb = y-1;

                // ensures index is within bounds
                if(yb < 0) yb = 0;

                // gets pixel values
                int X = Color.red(imageBmap.getPixel(x,y));
                int B = Color.red(imageBmap.getPixel(xb, yb));

                // finds diff of pixel values
                int diff = X-B+128;

                // checks if diff is within bounds
                if(diff < 0) diff = 0;
                else if (diff > 255) diff =255;

                // sets new pixel values
                P2Y.setPixel(x,y, Color.rgb(X,X,X));
                P2refY.setPixel(x,y, Color.rgb(B,B,B));
                P2difY.setPixel(x,y,Color.rgb(diff,diff,diff));
            }
        }

        imageLeft.setImageBitmap(P2Y);
        imageMiddle.setImageBitmap(P2refY);
        imageRight.setImageBitmap(P2difY);

        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);


    }

    // function uses P4 intra prediction to generate bitmap
    private void IntraPredictionP4(){

        //checks if bitmap has already been generated so it doesnt remake it
        //if it has already been created just sets the image view to that bitmap

        if(P4Y != null){
            imageLeft.setImageBitmap(P4Y);
            imageMiddle.setImageBitmap(P4refY);
            imageRight.setImageBitmap(P4difY);
            return;
        }

        Bitmap imageBmap = toYCbCr(originalImage);
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        P4Y = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        P4refY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        P4difY = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        // for loop iterates through pixels
        // uses P4 prediction to generate bitmap
        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){

                // finds x/y index of A pixel
                int xa = x-1;
                int ya = y;

                // finds x/y index of B pixel
                int xb = x;
                int yb = y-1;

                // finds x/y index of C pixel
                int xc = x-1;
                int yc = y-1;

                // checks if indexes are within bounds
                if(yb < 0) yb = 0;
                if(xa < 0) xa = 0;
                if(xc < 0) xc = 0;
                if(yc < 0) yc = 0;

                // gets pixel values
                int X = Color.red(imageBmap.getPixel(x,y));
                int A = Color.red(imageBmap.getPixel(xa, ya));
                int B = Color.red(imageBmap.getPixel(xb, yb));
                int C = Color.red(imageBmap.getPixel(xc, yc));

                // predicted pixel value
                int pred = A + B - C;

                // dif pixel value
                int diff = X-B+128;

                // checks to ensure pixel value is within bounds
                if(diff < 0) diff = 0;
                else if (diff > 255) diff =255;

                // sets new pixel values
                P4Y.setPixel(x,y, Color.rgb(X,X,X));
                P4refY.setPixel(x,y, Color.rgb(pred,pred,pred));
                P4difY.setPixel(x,y,Color.rgb(diff,diff,diff));
            }
        }

        // sets image views
        imageLeft.setImageBitmap(P4Y);
        imageMiddle.setImageBitmap(P4refY);
        imageRight.setImageBitmap(P4difY);

        // sets visibility of image views
        imageLeft.setVisibility(View.VISIBLE);
        imageMiddle.setVisibility(View.VISIBLE);
        imageRight.setVisibility(View.VISIBLE);

    }
}