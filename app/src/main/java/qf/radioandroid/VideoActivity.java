package qf.radioandroid;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.InputStreamEntity;
//import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import qf.radioandroid.network.Client;
import qf.radioandroid.network.Server;
import qf.radioandroid.network.Utils;

import android.provider.Settings.Secure;

/**
 * Created by pavelkuzmin on 03/02/2017.
 */

public class VideoActivity extends Activity {

    Uri staymp4;
    Uri saymp4;

    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    public String serverIP;

    private MediaRecorder mediaRecorder;

    boolean recording = false;

    private MediaPlayer audioPlayer;

    List<File> audioPlaylist = new ArrayList<>();

    Timer timer;

    public VideoView videoView;

    private TextView recordView;

    @Override
    protected void onResume() {

        super.onResume();

        // Go fullscreen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

        System.out.println("onResume");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        if (!Utils.checkDevice())
//            finish();

        checkPermissions();

        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
        Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_LONG).show();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 5, 0); //mic volume

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.video);

        initVideoView();

        recordView = (TextView) findViewById(R.id.recordView);
        recordView.setText("transmission in progress".toUpperCase());
        recordView.setAlpha(0.0f);

        Server server = new Server(7000, this);
        try {
            server.start();
            System.out.println("server start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPermissions() {

        int recordCheck = ContextCompat.checkSelfPermission(VideoActivity.this, Manifest.permission.RECORD_AUDIO);
        if (recordCheck != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        int storageCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (storageCheck != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(VideoActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void initVideoView() {

        staymp4 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stay3gp2);
        saymp4 = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.say3gp2);

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoURI(staymp4);
        videoView.setOnPreparedListener(preparedListener);

        videoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

//                int x = (int) event.getX();
//                int y = (int) event.getY();

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        if (audioPlaylist.size() > 0)
                            break;

                        if (!recording) {
                            initMediaRecorder();
                            mediaRecorder.start();

                            showRecording();
                            recording = true;
                        }

                        break;

                    case MotionEvent.ACTION_UP:

                        Timer wait = new Timer();

                        wait.schedule(new TimerTask() {

                            @Override
                            public void run() {

                                if (recording) {

                                    try {

                                        mediaRecorder.stop();
                                        Client.sendAudio(serverIP);

                                    } catch (RuntimeException e) {
                                    }

                                    mediaRecorder.release();

                                    showVideo();
                                    recording = false;
                                }
                            }

                        }, 500);

                        break;

                    default:
                        return false;
                }

                return true;
            }
        });
    }

    private void showRecording() {
//        videoView.setAlpha(0.0f);
        recordView.setAlpha(0.8f);
    }

    private void showVideo() {
//        videoView.setAlpha(1.0f);
        recordView.setAlpha(0.0f);
    }

    private void initMediaRecorder() {

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(new File(Environment.getExternalStorageDirectory(), "audio.3gp").getAbsolutePath());

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener(){

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
//                    System.out.println("onCompletion");
                }
            });

            try {

//                System.out.println("onPrepared");

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    mediaPlayer = new MediaPlayer();
                }

                mediaPlayer.setVolume(0f, 0f);
                mediaPlayer.setLooping(true);
                mediaPlayer.start();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void startAudio(File audio) throws IOException {

        audioPlaylist.add(audio);

        if (audioPlaylist.size() == 1) {

            playNext();

            try {
                videoView.setVideoURI(saymp4);
            } catch (Exception e) {
            }
        }

//        System.out.println("audioPlaylist.size(): " + audioPlaylist.size());
    }

    private void playNext() {

//        System.out.println("playNext(): " + audioPlaylist.get(0));

        timer = new Timer();

//        float speed = 0.75f;

        audioPlayer = MediaPlayer.create(this, Uri.parse(audioPlaylist.get(0).getAbsolutePath()));
//        audioPlayer.setPlaybackParams(audioPlayer.getPlaybackParams().setSpeed(speed));

        timer.schedule(new TimerTask() {

            @Override
            public void run() {

                audioPlaylist.get(0).delete();
                audioPlaylist.remove(0);

                if (audioPlaylist.size() > 0)
                    playNext();

                else {

                    try {
                        videoView.setVideoURI(staymp4);
                    } catch (Exception e) {
                    }
                }
            }

        }, audioPlayer.getDuration());

        audioPlayer.start();
    }
}
