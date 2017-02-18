package qf.radioandroid;

import android.os.AsyncTask;
import android.os.Environment;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by pavelkuzmin on 18/02/2017.
 */

public class Client extends AsyncTask<String, Void, String> {

    static String audioFile = "audio.3gp";

    static String serverUrl = "http://192.168.1.47:7000";

    static String audioUrl = "/audio";

    static void sendAudio() {

        System.out.println(new File(Environment.getExternalStorageDirectory(), audioFile).length());

//        sendFile(serverUrl + audioUrl);

        new Client().execute(serverUrl + audioUrl);
    }

    private static void sendFile(String url) {

        System.out.println("sendFile");

        long time = System.currentTimeMillis();

        try {

            File file = new File(Environment.getExternalStorageDirectory(), audioFile);

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            InputStreamEntity reqEntity = new InputStreamEntity(new FileInputStream(file), -1);
            reqEntity.setContentType("binary/octet-stream");
            reqEntity.setChunked(true); // Client in multiple parts if needed
            httppost.setEntity(reqEntity);
            HttpResponse response = httpclient.execute(httppost);
            //Do something with response...



            System.out.println(response.getStatusLine());
            System.out.println("sendFile time: " + (System.currentTimeMillis() - time) / 1000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected String doInBackground(String... urls) {

        sendFile(urls[0]);

        return "";
    }
}
