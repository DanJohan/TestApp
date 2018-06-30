package vidioapp.smartserve.com.lastvideoimageapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;


import org.jcodec.common.model.Picture;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;



import java.io.File;
import java.io.IOException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;

import org.jcodec.common.model.ColorSpace;

import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;


import android.graphics.Bitmap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
          File  directory = new File(Environment.getDataDirectory()
                    + "/RobotiumTestLog/");
          directory.mkdir();
            File file1 = new File(directory, "output.mp4");
            SequenceEncoder sequenceEncoder = new SequenceEncoder(file1);

            for (int i = 1; i<9; i++) {
                File file = new File(path, "/img0000" + Integer.toString(i) + ".jpg");
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                org.jcodec.common.model.Picture picture = fromBitmap(bitmap);
                sequenceEncoder.encodeNativeFrame(picture);
            }

            sequenceEncoder.finish();

        } catch (IOException e) {
            e.printStackTrace();
        }
      /*  try {

            File wallpaperDirectory = new File("/sdcard/Wallpaper/");
            // have the object build the directory structure, if needed.
            wallpaperDirectory.mkdirs();

            File file = new File(wallpaperDirectory, "output.mp4");
            SequenceEncoder encoder = new SequenceEncoder(file);

            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.a1);
            encoder.encodeNativeFrame(this.fromBitmap(bitmap));

            encoder.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }


    // convert from Bitmap to Picture (jcodec native structure)
    public Picture fromBitmap(Bitmap src) {
     //   Picture dst = Picture.create(src.getWidth(), src.getHeight(), ColorSpace.RGB);
        Picture dst = Picture.create((int) src.getWidth(),
                (int) src.getHeight(), ColorSpace.RGB);
        fromBitmap(src, dst);
        return dst;
    }
   /*public void imagesVideo(){
       try {
           sequenceEncoder = new SequenceEncoder(new File(WHERE_TO_SAVE,"NAME.mp4"));

           for (int i = 1; i<9; i++) {
               File file = new File(path, "/img0000" + Integer.toString(i) + ".jpg");
               Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
               Picture picture = fromBitmap(bitmap);
               sequenceEncoder.encodeNativeFrame(picture);
           }

           sequenceEncoder.finish();

       } catch (IOException e) {
           e.printStackTrace();
       }
   }*/

    public void fromBitmap(Bitmap src, Picture dst) {
        byte[] dstData = dst.getPlaneData(0);
        int[] packed = new int[src.getWidth() * src.getHeight()];

        src.getPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(), src.getHeight());

        for (int i = 0, srcOff = 0, dstOff = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++, srcOff++, dstOff += 3) {
                int rgb = packed[srcOff];
                dstData[dstOff] = (byte) ( (rgb >> 16) & 0xff);
                dstData[dstOff + 1] =(byte) ((rgb >> 8) & 0xff);
                dstData[dstOff + 2] = (byte)(rgb & 0xff);
            }
        }
    }
}
