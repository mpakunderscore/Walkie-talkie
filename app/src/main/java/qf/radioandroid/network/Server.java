package qf.radioandroid.network;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.VideoView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import fi.iki.elonen.NanoHTTPD;
import qf.radioandroid.VideoActivity;

/**
 * Created by pavelkuzmin on 08/01/2017.
 */

//Server

public class Server extends NanoHTTPD {

    VideoActivity video;

    Server(int port) {
        super(port);
    }

    public Server(int port, VideoActivity video) {
        super(port);
        this.video = video;
    }

    @Override
    public Response serve(IHTTPSession session) {

        Method method = session.getMethod();

        Map<String, String> files = new HashMap<String, String>();
        try {
            session.parseBody(files);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }

        Map<String, String> params = session.getParms();

        video.startAudio();

        return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, "Communicator Android");
    }
}
