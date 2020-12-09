package com.example.livelive;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import fm.liveswitch.AecContext;
import fm.liveswitch.AudioConfig;
import fm.liveswitch.AudioDecoder;
import fm.liveswitch.AudioFormat;
import fm.liveswitch.AudioSink;
import fm.liveswitch.VideoDecoder;
import fm.liveswitch.VideoFormat;
import fm.liveswitch.VideoPipe;
import fm.liveswitch.VideoSink;
import fm.liveswitch.ViewSink;
import fm.liveswitch.android.OpenGLSink;

public class RemoteMedia extends fm.liveswitch.RtcRemoteMedia<FrameLayout>{

    Context context =  null;

    public RemoteMedia(Context context,boolean disableAudio, boolean disableVideo, AecContext aecContext) {
        /**
         * @param disableAudio Whether to disable audio.
         * @param disableVideo Whether to disable video.
         * @param aecContext Your singleton AEC context, if using software echo cancellation.
         */
        super(disableAudio, disableVideo, aecContext);
        this.context= context;
        super.initialize();
    }

    @Override
    protected AudioSink createAudioRecorder(AudioFormat audioFormat) {
        return null;
    }

    @Override
    protected AudioSink createAudioSink(AudioConfig audioConfig) {
        return new fm.liveswitch.android.AudioTrackSink(audioConfig);
    }

    @Override
    protected VideoDecoder createH264Decoder() {
        return null;
    }

    @Override
    protected VideoPipe createImageConverter(VideoFormat videoFormat) {
        return new fm.liveswitch.yuv.ImageConverter(videoFormat);
    }

    @Override
    protected AudioDecoder createOpusDecoder(AudioConfig audioConfig) {
        return new fm.liveswitch.opus.Decoder(audioConfig);
    }

    @Override
    protected VideoSink createVideoRecorder(VideoFormat videoFormat) {
        return null;
    }

    @Override
    protected ViewSink<FrameLayout> createViewSink() {
        if (this.context ==  null){
            try {
                throw new Exception("context is null, initialize RemoteMedia object with context, i.e. new RemoteMedia(Context context,boolean disableAudio, boolean disableVideo, AecContext aecContext)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new OpenGLSink(this.context);
    }

    @Override
    protected VideoDecoder createVp8Decoder() {
        return new fm.liveswitch.vp8.Decoder();
    }

    @Override
    protected VideoDecoder createVp9Decoder() {
        return new fm.liveswitch.vp9.Decoder();
    }

//    public fm.liveswitch.VideoDecoder createH264Encoder() {
//        return fm.liveswitch.openh264.Decoder();
//    }
}
