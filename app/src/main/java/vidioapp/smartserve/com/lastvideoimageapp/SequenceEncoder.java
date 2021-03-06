package vidioapp.smartserve.com.lastvideoimageapp;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.util.ArrayList;


import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;

import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;

import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;


import org.jcodec.containers.mp4. MP4Muxer.CompressedTrack;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.RgbToYuv420;

import android.graphics.Bitmap;

public class SequenceEncoder {

    private SeekableByteChannel ch;
    private Picture toEncode;
    private RgbToYuv420 transform;
    private H264Encoder encoder;
    private ArrayList<ByteBuffer> spsList;
    private ArrayList<ByteBuffer> ppsList;
    private FramesMP4MuxerTrack outTrack;
    private ByteBuffer _out;
    private int frameNo;
    private MP4Muxer muxer;

    public SequenceEncoder(File out) throws IOException {
        this.ch = NIOUtils.writableFileChannel(out);

        // Transform to convert between RGB and YUV
        transform = new RgbToYuv420(0, 0);

        // Muxer that will store the encoded frames
        muxer = new MP4Muxer(ch, Brand.MP4);

        // Add video track to muxer
        outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, 25);

        // Allocate a buffer big enough to hold output frames
        _out = ByteBuffer.allocate(1920 * 1080 * 6);

        // Create an instance of encoder
        encoder = new H264Encoder();

        // Encoder extra data ( SPS, PPS ) to be stored in a special place of
        // MP4
        spsList = new ArrayList<ByteBuffer>();
        ppsList = new ArrayList<ByteBuffer>();

    }

    public void encodeImage(Bitmap bi) throws IOException {
        // encodeNativeFrame(AWTUtil.fromBufferedImage(bi));
        encodeNativeFrame(fromBitmap(bi));
    }

    public void encodeNativeFrame(Picture pic) throws IOException {
        if (toEncode == null) {
            toEncode = Picture.create(pic.getWidth(), pic.getHeight(),
                    ColorSpace.YUV420);
        }

        // Perform conversion
        transform.transform(pic, toEncode);

        // Encode image into H.264 frame, the result is stored in '_out' buffer
        _out.clear();
        ByteBuffer result = encoder.encodeFrame(_out, toEncode);

        // Based on the frame above form correct MP4 packet
        spsList.clear();
        ppsList.clear();
        H264Utils.encodeMOVPacket(result, spsList, ppsList);

        // Add packet to video track
        outTrack.addFrame(new MP4Packet(result, frameNo, 25, 1, frameNo, true,
                null, frameNo, 0));
        frameNo++;
    }

    public void finish() throws IOException {
        // Push saved SPS/PPS to a special storage in MP4
        outTrack.addSampleEntry(H264Utils
                .createMOVSampleEntry(spsList, ppsList));

        // Write MP4 header and finalize recording
        muxer.writeHeader();
        NIOUtils.closeQuietly(ch);
    }

    public static Picture fromBitmap(Bitmap src) {
        Picture dst = Picture.create((int) src.getWidth(),
                (int) src.getHeight(), ColorSpace.RGB);
        fromBitmap(src, dst);
        return dst;
    }

    public static void fromBitmap(Bitmap src, Picture dst) {
        byte[] dstData = dst.getPlaneData(0);
        int[] packed = new int[src.getWidth() * src.getHeight()];

        src.getPixels(packed, 0, src.getWidth(), 0, 0, src.getWidth(),
                src.getHeight());

        for (int i = 0, srcOff = 0, dstOff = 0; i < src.getHeight(); i++) {
            for (int j = 0; j < src.getWidth(); j++, srcOff++, dstOff += 3) {
                int rgb = packed[srcOff];
                dstData[dstOff] = (byte) ((rgb >> 16) & 0xff);
                dstData[dstOff + 1] = (byte) ((rgb >> 8) & 0xff);
                dstData[dstOff + 2] = (byte) (rgb & 0xff);
            }
        }
    }

}
