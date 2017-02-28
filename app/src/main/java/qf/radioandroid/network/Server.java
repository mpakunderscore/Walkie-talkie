package qf.radioandroid.network;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
//import android.support.test.espresso.core.deps.guava.io.Files;
import android.support.annotation.RequiresApi;
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public Response serve(IHTTPSession session) {

        Map<String, String> params = session.getParms();

        try {

            System.out.println(session.getMethod());

            if (session.getMethod().toString().equals("POST")) {

                Map<String, String> files = new HashMap<>();
                session.parseBody(files);
                Set<String> keys = files.keySet();

                for (String key : keys) {

                    String location = files.get(key);

                    File tempFile = new File(location);

                    System.out.println("tempFile.length(): " + tempFile.length());

                    File audio = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath()
                            + "/" + System.currentTimeMillis());

                    copy(tempFile, audio);

                    System.out.println("NEXT FILE: " + audio.exists());

                    videoActivity.startAudio(audio);
                }

            } else {

                String ip = session.getHeaders().get("http-client-ip");
                videoActivity.serverIP = ip;
                System.out.println(ip);
            }

        } catch (IOException | ResponseException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Error");
        }

        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Communicator Android");
    }

    public static void copy(File src, File dst) throws IOException {

        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
}
