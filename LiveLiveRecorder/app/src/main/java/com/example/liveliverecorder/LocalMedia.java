package com.example.liveliverecorder;

import android.content.Context;
import android.view.View;

import fm.liveswitch.AecContext;
import fm.liveswitch.AudioConfig;
import fm.liveswitch.AudioEncoder;
import fm.liveswitch.AudioFormat;
import fm.liveswitch.AudioSink;
import fm.liveswitch.AudioSource;
import fm.liveswitch.LayoutScale;
import fm.liveswitch.VideoEncoder;
import fm.liveswitch.VideoFormat;
import fm.liveswitch.VideoPipe;
import fm.liveswitch.VideoSink;
import fm.liveswitch.VideoSource;
import fm.liveswitch.ViewSink;
import fm.liveswitch.android.CameraPreview;

public class LocalMedia extends fm.liveswitch.RtcLocalMedia<View>{

    private Context context;
    private CameraPreview cameraPreview;
    // for cameraX
//    private PreviewView  preview;

    public LocalMedia(Context context,boolean disableAudio, boolean disableVideo, AecContext aecContext) {
        /**
         * @param disableAudio Whether to disable audio.
         * @param disableVideo Whether to disable video.
         * @param aecContext Your singleton AEC context, if using software echo cancellation.
         */
        super(disableAudio, disableVideo, aecContext);
        this.context = context;
//        this.cameraPreview = previewView;
        this.cameraPreview = new CameraPreview(context, LayoutScale.Contain);
//        cameraPreview.setViewScale(LayoutScale.Contain);
//        cameraPreview.setViewScale(LayoutScale.Cover);
        cameraPreview.setViewScale(LayoutScale.Stretch);
//        cameraPreview.getView().
        super.initialize();
    }

    @Override
    protected AudioSink createAudioRecorder(AudioFormat audioFormat) {
        return null;
    }

    @Override
    protected AudioSource createAudioSource(AudioConfig audioConfig) {
        if (this.context ==  null){
            try {
                throw new Exception("context is null, initialize LocalMedia object with context, i.e. new LocalMedia(Context context,boolean disableAudio, boolean disableVideo, AecContext aecContext)");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new fm.liveswitch.android.AudioRecordSource(context,audioConfig);
    }

    @Override
    protected VideoEncoder createH264Encoder() {
        return null;
    }

    @Override
    protected VideoPipe createImageConverter(VideoFormat videoFormat) {
        return new fm.liveswitch.yuv.ImageConverter(videoFormat);
    }

    @Override
    protected AudioEncoder createOpusEncoder(AudioConfig audioConfig) {
        return new fm.liveswitch.opus.Encoder(audioConfig);
    }

    @Override
    protected VideoSink createVideoRecorder(VideoFormat videoFormat) {
        return null;
    }

    @Override
    protected VideoSource createVideoSource() {
        //https://docs.frozenmountain.com/liveswitch/api/Android/
        this.cameraPreview = new CameraPreview(this.context, LayoutScale.Contain);
        return new fm.liveswitch.android.Camera2Source(this.cameraPreview, new fm.liveswitch.VideoConfig(640, 480, 30));
    }

    public View getView() {
        return this.cameraPreview.getView();
    }

//    public fm.liveswitch.ViewSink<...> createViewSink() {
//        return null; // we override `getView` instead
//    }

    @Override
    protected ViewSink<View> createViewSink() {
        return null;
    }

    @Override
    protected VideoEncoder createVp8Encoder() {
        return new fm.liveswitch.vp8.Encoder();
    }

    @Override
    protected VideoEncoder createVp9Encoder() {
        return new fm.liveswitch.vp9.Encoder();
    }
//    public fm.liveswitch.VideoEncoder createH264Encoder() {
//        return new fm.liveswitch.openh264.Encoder();
//    }
}
