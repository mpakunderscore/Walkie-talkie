package qf.radioandroid.network;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.test.espresso.core.deps.guava.io.Files;
import android.widget.VideoView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import fi.iki.elonen.NanoHTTPD;
import qf.radioandroid.VideoActivity;

import static android.R.attr.name;

/**
 * Created by pavelkuzmin on 08/01/2017.
 */

//Server

public class Server extends NanoHTTPD {

    private VideoActivity videoActivity;

    public Server(int port, VideoActivity videoActivity) {
        super(port);
        this.videoActivity = videoActivity;
    }

    @Override
    public Response serve(IHTTPSession session) {

        Map<String, String> params = session.getParms();

        try {

            System.out.println(session.getMethod());
            String ip = session.getHeaders().get("http-client-ip");

            if (session.getMethod().toString().equals("POST")) {

                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                Set<String> keys = files.keySet();

                for (String key : keys) {

                    String location = files.get(key);

                    File tempFile = new File(location);

                    File audio = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".in");

                    Utils.copy(tempFile, audio);

                    System.out.println("next file: " + audio.exists());

                    videoActivity.startAudio(audio);
                }
            }

            if (!Objects.equals(videoActivity.serverIP, ip)) {
                videoActivity.setIP(ip);
                System.out.println("new ip: " + ip);
            }

        } catch (IOException | ResponseException e) {

            return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Error");
        }

        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Communicator Android");
    }
}
