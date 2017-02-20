package qf.radioandroid;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.VideoView;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.InputStreamEntity;
//import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;

import qf.radioandroid.network.Client;
import qf.radioandroid.network.Server;

/**
 * Created by pavelkuzmin on 03/02/2017.
 */

public class VideoActivity extends Activity {

    int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LOW_PROFILE
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

    private MediaRecorder mediaRecorder;

    VideoView videoView;

    boolean recording = false;

    @Override
    protected void onResume() {

        super.onResume();

        // Go fullscreen
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(uiOptions);

        System.out.println("onResume");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Go fullscreen
//        View decorView = getWindow().getDecorView();
//        decorView.setSystemUiVisibility(uiOptions);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.video);

        Uri video = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stay1);

        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoURI(video);
        videoView.setOnPreparedListener(preparedListener);

        videoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

//                int x = (int) event.getX();
//                int y = (int) event.getY();

                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:

                        if (!recording) {
                            initMediaRecorder();
                            mediaRecorder.start();

                            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.say));
//                            videoView.setAlpha(0.9f);
                            recording = true;
                        }

                        break;

                    case MotionEvent.ACTION_UP:

                        if (recording) {

                            try {

                                mediaRecorder.stop();
                                Client.sendAudio();

                            } catch (RuntimeException e) {
                            }

                            mediaRecorder.release();

                            videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stay1));
//                            videoView.setAlpha(1.0f);
                            recording = false;
                        }

                        break;

                    default:
                        return false;
                }

                return true;
            }
        });

        Server server = new Server(7000, this);
        try {
            server.start();
            System.out.println("server start");
        } catch (IOException e) {
            e.printStackTrace();
        }

//        try {
//            socketProvider = new SocketProvider();
//        } catch (URISyntaxException e) {
//            e.printStackTrace();
//        }
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

            try {

                System.out.println("onPrepared");

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

    void startAudio() {

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.say));
    }

    void stopAudio() {

        videoView.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.stay1));
    }
}
