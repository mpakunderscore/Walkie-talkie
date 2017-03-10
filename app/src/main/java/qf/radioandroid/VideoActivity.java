package qf.radioandroid;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.format.Formatter;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import qf.radioandroid.network.Client;
import qf.radioandroid.network.Server;

/**
 * Created by pavelkuzmin on 03/02/2017.
 */

public class VideoActivity extends Activity {

    String recordingText = "transmission in progress".toUpperCase();

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

    private String audioFileOut;

    boolean recording = false;

    private MediaPlayer audioPlayer;

    List<File> audioPlaylist = new ArrayList<>();

    public VideoView videoView;

    private TextView recordView;

    @Override
    protected void onResume() {

        super.onResume();

        // Go fullscreen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

        WifiManager manager = (WifiManager) getSystemService(WIFI_SERVICE);
        String ip = Formatter.formatIpAddress(manager.getConnectionInfo().getIpAddress());
        Toast.makeText(getApplicationContext(),
                ip + " / PC " + serverIP,
                Toast.LENGTH_LONG).show();

        System.out.println("onResume");
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

//        if (!Utils.checkDevice())
//            finish();

        checkPermissions();

        checkPCIP();

        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.setSpeakerphoneOn(true);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.video);

        initVideoView();

        recordView = (TextView) findViewById(R.id.recordView);
        recordView.setText(recordingText);
        recordView.setAlpha(0.0f);

        Server server = new Server(7000, this);
        try {
            server.start();
            System.out.println("server start");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkPCIP() {

        SharedPreferences settings = getSharedPreferences("settings", 0);
        String pcip = settings.getString("pcip", "");
        System.out.println("pcip: " + pcip);

        if (pcip.length() > 0)
            serverIP = pcip;
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

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        if (audioPlaylist.size() > 0)
                            break;

                        if (!recording) {

                            try {
                                initMediaRecorder();
                            } catch (Exception e) {
                                break;
                            }

                            recording = true;

                            mediaRecorder.start();

                            showRecording();
                        }

                        break;

                    case MotionEvent.ACTION_UP:


                        new Timer().schedule(new TimerTask() {

                            @Override
                            public void run() {

                                if (!recording)
                                    return;

                                try {

                                    mediaRecorder.stop();

                                    Client.sendAudio(serverIP, audioFileOut);

                                } catch (Exception ignored) {
                                }

                                mediaRecorder.reset();

                                showVideo();

                                audioFileOut = "";

                                recording = false;
                            }

                        }, 300);

                        break;

                    default:
                        return false;
                }

                return true;
            }
        });
    }

    private void showRecording() {
        recordView.setAlpha(0.8f);
    }

    private void showVideo() {
        recordView.setAlpha(0.0f);
    }

    private void initMediaRecorder() throws IOException {

        File out = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".out");
        audioFileOut = out.getAbsolutePath();

        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(audioFileOut);

        mediaRecorder.prepare();
    }

    MediaPlayer.OnPreparedListener preparedListener = new MediaPlayer.OnPreparedListener() {

        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
//                    System.out.println("onCompletion");
                }
            });

            try {

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

        //TODO check order here
        audioPlaylist.add(audio);

        if (audioPlaylist.size() == 1) {

            playNext();

            try {
                videoView.setVideoURI(saymp4);
            } catch (Exception ignored) {
            }
        }
    }

    private void playNext() {

        audioPlayer = MediaPlayer.create(this, Uri.parse(audioPlaylist.get(0).getAbsolutePath()));

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {

                audioPlaylist.get(0).delete();
                audioPlaylist.remove(0);

                if (audioPlaylist.size() > 0)
                    playNext();

                else {

                    try {
                        videoView.setVideoURI(staymp4);
                    } catch (Exception ignored) {
                    }
                }
            }

        }, audioPlayer.getDuration());

        audioPlayer.start();
    }

    public void setIP(String ip) {

        serverIP = ip;

        SharedPreferences settings = getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("pcip", ip);
        editor.commit();
    }
}
