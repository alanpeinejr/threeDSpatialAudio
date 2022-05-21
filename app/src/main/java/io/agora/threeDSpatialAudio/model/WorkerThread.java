package io.agora.threeDSpatialAudio.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
//import io.agora.rtc2.Constants;
//import io.agora.rtc2.RtcEngine;
//import io.agora.rtc2.video.VideoCanvas;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;


import io.agora.threeDSpatialAudio.R;
import io.agora.threeDSpatialAudio.model.pojos.Sound;

public class WorkerThread extends Thread {
    private final static Logger log = LoggerFactory.getLogger(WorkerThread.class);
    private final Context mContext;

    private static final int ACTION_WORKER_THREAD_QUIT = 0X1010; // quit this thread

    private static final int ACTION_WORKER_JOIN_CHANNEL = 0X2010;
    private static final int ACTION_WORKER_LEAVE_CHANNEL = 0X2011;

    private static final int ACTION_WORKER_START_AUDIO_MIX = 0X2012;
    private static final int ACTION_WORKER_END_AUDIO_MIX = 0X2013;

    private static final int ACTION_WORKER_LOCAL_VIDEO_START = 0X2014;
    private static final int ACTION_WORKER_LOCAL_VIDEO_END = 0X2015;
    private static final int ACTION_WORKER_REMOTE_VIDEO_START = 0X2016;
    private static final int ACTION_WORKER_REMOTE_VIDEO_END = 0X2017;

    private WorkerThreadHandler mWorkerHandler;
    private RtcEngine mRtcEngine;
    private boolean mReady;
    private EngineConfig mEngineConfig;
    private final MyEngineEventHandler mEngineEventHandler;

    public WorkerThread(Context context) {
        this.mContext = context;

        this.mEngineConfig = new EngineConfig();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        this.mEngineConfig.mUid = pref.getInt(ConstantApp.PrefManager.PREF_PROPERTY_UID, 0);

        this.mEngineEventHandler = new MyEngineEventHandler(mContext, this.mEngineConfig);
    }

    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("wait for " + WorkerThread.class.getSimpleName());
        }
    }

    @Override
    public void run() {
        log.trace("start to run");
        Looper.prepare();

        mWorkerHandler = new WorkerThreadHandler(this);

        ensureRtcEngineReadyLock();

        mReady = true;

        // enter thread looper
        Looper.loop();
    }

    /**
     * call this method to exit
     * should ONLY call this method when this thread is running
     */
    public final void exit() {
        if (Thread.currentThread() != this) {
            log.warn("exit() - exit app thread asynchronously");
            mWorkerHandler.sendEmptyMessage(ACTION_WORKER_THREAD_QUIT);
            return;
        }

        mReady = false;

        // TODO should remove all pending(read) messages

        log.debug("exit() > start");

        // exit thread looper
        Looper.myLooper().quit();

        mWorkerHandler.release();

        log.debug("exit() > end");
    }

    /*****************************Channel Management*************************************************/

    public final void joinChannel(final String channel, int uid) {
        joinChannel(channel, uid, Constants.CLIENT_ROLE_AUDIENCE);
    }

    public final void hostChannel(final String channel, int uid) {
        joinChannel(channel, uid, Constants.CLIENT_ROLE_BROADCASTER);
    }

    public final void joinChannel(final String channel, int uid, int role) {
        if (Thread.currentThread() != this) {
            log.warn("joinChannel() - worker thread asynchronously {} {}", channel, uid);
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_JOIN_CHANNEL;
            envelop.obj = new String[]{channel};
            envelop.arg1 = uid;
            envelop.arg2 = role;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        mRtcEngine.setClientRole(role);
        mRtcEngine.joinChannel(null, channel, "3DSpatialAudio", uid);

        mEngineConfig.mChannel = channel;

        log.debug("joinChannel " + channel + " " + uid);
    }


    public final void leaveChannel(String channel) {
        if (Thread.currentThread() != this) {
            log.warn("leaveChannel() - worker thread asynchronously {}", channel);
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LEAVE_CHANNEL;
            envelop.obj = channel;
            mWorkerHandler.sendMessage(envelop);
            return;
        }

        if (mRtcEngine != null) {
            mRtcEngine.leaveChannel();
        }

        mEngineConfig.reset();
        log.debug("leaveChannel " + channel);
    }

    /******************************Audio Mixing **************************************************/
    public final void startAudioMixing(Sound sound) {
        if (Thread.currentThread() != this) {
            log.warn("startAudioMixing - worker thread asynchronously {}", sound.displayName);
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_START_AUDIO_MIX;
            envelop.obj = sound;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        log.debug("Start Audio Mixing {}", sound);
        mRtcEngine.adjustRecordingSignalVolume(0);
        mRtcEngine.adjustAudioMixingPublishVolume(99);
        mRtcEngine.adjustAudioMixingVolume(99);

        mRtcEngine.stopAudioMixing();

        Uri fileToMixUri = getUriForSound(sound);
        mRtcEngine.startAudioMixing(fileToMixUri.toString(), false, false, -1);

    }

    public final void stopAudioMixing() {
        if (Thread.currentThread() != this) {
            log.warn("stopAudio - worker thread asynchronously ");
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_END_AUDIO_MIX;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        log.debug("Stop Audio Mixing");

        mRtcEngine.adjustRecordingSignalVolume(100);
        mRtcEngine.adjustAudioMixingPublishVolume(0);
        mRtcEngine.stopAudioMixing();

    }
    public void startLocalVideo(int viewGroupId, Activity activity){
        if (Thread.currentThread() != this) {
            log.warn("startVideo - worker thread asynchronously ");
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LOCAL_VIDEO_START;
            envelop.obj = activity;
            envelop.arg1 = viewGroupId;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        log.debug("Start Local Video");
        //cannot set a childView onn worker thread, but dont want Fragment to know about engine
        activity.runOnUiThread(() ->{
            SurfaceView surfaceView = RtcEngine.CreateRendererView(activity.getBaseContext());
            ViewGroup viewGroup = activity.findViewById(viewGroupId);
            viewGroup.addView(surfaceView);
            mRtcEngine.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, mEngineConfig.mUid));

        });

    }

    public void endLocalVideo(int viewGroupId, Activity activity){
        if (Thread.currentThread() != this) {
            log.warn("stopVideo - worker thread asynchronously ");
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_LOCAL_VIDEO_END;
            envelop.obj = activity;
            envelop.arg1 = viewGroupId;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        log.debug("Stop Local Video");
        activity.runOnUiThread(() ->{
            ViewGroup viewGroup = activity.findViewById(viewGroupId);
            if(viewGroup.getChildCount()> 0){
                viewGroup.removeView(viewGroup.getChildAt(0));
            }
        });
    }

    public void startRemoteVideo(int uid, int viewGroupId, Activity activity){
        if (Thread.currentThread() != this) {
            log.warn("startRemoteVideo - worker thread asynchronously ");
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_REMOTE_VIDEO_START;
            envelop.obj = activity;
            envelop.arg1 = uid;
            envelop.arg2 = viewGroupId;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        log.debug("Start Remote Video");
        activity.runOnUiThread(() ->{
            ViewGroup container = activity.findViewById(viewGroupId);
            SurfaceView surfaceView = RtcEngine.CreateRendererView((activity.getBaseContext()));
            container.addView(surfaceView);
            mRtcEngine.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        });
        mRtcEngine.setRemoteVideoStreamType(uid, Constants.VIDEO_STREAM_LOW);
    }

    public void endRemoteVideo(int viewGroupId, Activity activity){
        if (Thread.currentThread() != this) {
            log.warn("endRemoteVideo - worker thread asynchronously ");
            Message envelop = new Message();
            envelop.what = ACTION_WORKER_REMOTE_VIDEO_END;
            envelop.obj = activity;
            envelop.arg1 = viewGroupId;
            mWorkerHandler.sendMessage(envelop);
            return;
        }
        ensureRtcEngineReadyLock();
        log.debug("End Remote Video");
        activity.runOnUiThread(() ->{
            ViewGroup viewGroup = activity.findViewById(viewGroupId);
            if(viewGroup.getChildCount()> 0){
                viewGroup.removeView(viewGroup.getChildAt(0));
            }

            //TODO best practices for multi stream video says to do this? doesnt seem right
            mRtcEngine.setupRemoteVideo(null);
        });
    }


    public static String getDeviceID(Context context) {
        // XXX according to the API docs, this value may change after factory reset
        // use Android id as device id
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public MyEngineEventHandler eventHandler() {
        return mEngineEventHandler;
    }

    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    public final EngineConfig getEngineConfig() {
        return mEngineConfig;
    }


    private RtcEngine ensureRtcEngineReadyLock() {
        if (mRtcEngine == null) {
            String appId = mContext.getString(R.string.agora_app_id);
            if (TextUtils.isEmpty(appId)) {
                throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
            }
            try {
                // Creates an RtcEngine instance and sets up a base configuration
                mRtcEngine = RtcEngine.create(mContext, appId, mEngineEventHandler.mRtcEventHandler);
                mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
                mRtcEngine.setAudioProfile(Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY_STEREO, Constants.AUDIO_SCENARIO_GAME_STREAMING);
//                mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(
//                        VideoEncoderConfiguration.VD_640x480,
//                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
//                        VideoEncoderConfiguration.STANDARD_BITRATE,
//                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
                mRtcEngine.enableAudio();
                mRtcEngine.enableVideo();
                mRtcEngine.enableLocalVideo(true);
                mRtcEngine.enableLocalAudio(true);
                mRtcEngine.muteLocalAudioStream(false);
                mRtcEngine.enableDualStreamMode(true);
                mRtcEngine.adjustRecordingSignalVolume(100);
                mRtcEngine.adjustAudioMixingPublishVolume(0);

               /*
                Enables the onAudioVolumeIndication callback at a set time interval to report on which
                users are speaking and the speakers' volume.
                Once this method is enabled, the SDK returns the volume indication in the
                onAudioVolumeIndication callback at the set time interval, regardless of whether any user
                is speaking in the channel.
               */
                mRtcEngine.enableAudioVolumeIndication(200, 3, false); // 200 ms
                mRtcEngine.setLogFile(Environment.getExternalStorageDirectory()
                        + File.separator + mContext.getPackageName() + "/log/agora-rtc.log");
            } catch (Exception e) {
                log.error(Log.getStackTraceString(e));
                throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
            }


        }
        return mRtcEngine;
    }

    /**
     * Agora cannot access an apk resource to play the file so this method handles keeping the demo
     * both self contained and easier to read when not bound by packaging the sounds in the apk.
     *
     * @param sound Sound enum desired to be played
     * @return Uri of the file that will be played
     */
    private Uri getUriForSound(Sound sound) {
        File path = mContext.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        File file = new File(path, sound.filePath);
        if (!file.exists()) {
            moveToAGAccessibleLocation(sound, path);
            file = new File(path, sound.filePath);
        }

        return Uri.fromFile(file);
    }

    private void moveToAGAccessibleLocation(Sound sound, File path) {
        File file = new File(path, sound.filePath);
        try {
            InputStream is = mContext.getAssets().openFd(sound.filePath).createInputStream();
            OutputStream os = new FileOutputStream(file);
            byte[] data = new byte[16 * 1024];
            is.read(data);
            os.write(data);
            int read;
            while ((read = is.read(data)) != -1) {
                os.write(data, 0, read);
            }
            is.close();
            os.close();

            // Tell the media scanner about the new file so that it is
            // immediately available to the user.
            MediaScannerConnection.scanFile(mContext,
                    new String[]{file.toString()}, null,
                    (path1, uri) -> {
                        log.info("ExternalStorage Scanned {}: ", path1);
                        log.info("ExternalStorage -> uri={}", uri);
                    });
        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
        }
    }


    private static final class WorkerThreadHandler extends Handler {

        private WorkerThread mWorkerThread;


        WorkerThreadHandler(WorkerThread thread) {
            this.mWorkerThread = thread;
        }

        public void release() {
            mWorkerThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mWorkerThread == null) {
                log.warn("handler is already released! " + msg.what);
                return;
            }

            switch (msg.what) {
                case ACTION_WORKER_THREAD_QUIT:
                    mWorkerThread.exit();
                    break;
                case ACTION_WORKER_JOIN_CHANNEL:
                    String[] data = (String[]) msg.obj;
                    mWorkerThread.joinChannel(data[0], msg.arg1, msg.arg2);
                    break;
                case ACTION_WORKER_LEAVE_CHANNEL:
                    String channel = (String) msg.obj;
                    mWorkerThread.leaveChannel(channel);
                    break;
                case ACTION_WORKER_START_AUDIO_MIX:
                    mWorkerThread.startAudioMixing((Sound) msg.obj);
                    break;
                case ACTION_WORKER_END_AUDIO_MIX:
                    mWorkerThread.stopAudioMixing();
                    break;
                case ACTION_WORKER_LOCAL_VIDEO_START:
                    mWorkerThread.startLocalVideo(msg.arg1, (Activity) msg.obj);
                    break;
                case ACTION_WORKER_LOCAL_VIDEO_END:
                    mWorkerThread.endLocalVideo(msg.arg1, (Activity) msg.obj);
                    break;
                case ACTION_WORKER_REMOTE_VIDEO_START:
                    mWorkerThread.startRemoteVideo(msg.arg1, msg.arg2, (Activity) msg.obj);
                    break;
                case ACTION_WORKER_REMOTE_VIDEO_END:
                    mWorkerThread.endRemoteVideo(msg.arg1, (Activity) msg.obj);
                    break;
            }
        }
    }
}
