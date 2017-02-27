package qf.radioandroid.network;

import android.os.AsyncTask;
import android.os.Environment;

import com.android.internal.http.multipart.MultipartEntity;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by pavelkuzmin on 18/02/2017.
 */

public class Client extends AsyncTask<String, Void, String> {

    static String audioFile = "audio.3gp";

    static String serverUrl;

    public static void sendAudio(String ip) {

        serverUrl = "http://" + ip + ":7000/audio";

        System.out.println(new File(Environment.getExternalStorageDirectory(), audioFile).length());

        new Client().execute(serverUrl);
    }

    @Override
    protected String doInBackground(String... urls) {

        sendFile(urls[0]);
        return "";
    }

    private static void sendFile(String url) {

        System.out.println("sendFile");

        long time = System.currentTimeMillis();

        String filePath = Environment.getExternalStorageDirectory() + "/" + audioFile;

        try {
            send(url, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("sendFile time: " + (System.currentTimeMillis() - time) / 1000);
    }

    private static void send(String serverUrl, String selectedFilePath) throws IOException {

        int serverResponseCode = 0;

        HttpURLConnection connection;
        DataOutputStream dataOutputStream;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";


        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File selectedFile = new File(selectedFilePath);


        if (!selectedFile.isFile()) {

            System.out.println("NOT A FILE: " + selectedFilePath);
            return;
        }


        String[] parts = selectedFilePath.split("/");
        final String fileName = parts[parts.length - 1];

        FileInputStream fileInputStream = new FileInputStream(selectedFile);
        URL url = new URL(serverUrl);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true); //Allow Inputs
        connection.setDoOutput(true); //Allow Outputs
        connection.setUseCaches(false); //Don't use a cached Copy
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setRequestProperty("ENCTYPE", "multipart/form-data");
        connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        connection.setRequestProperty("uploaded_file", selectedFilePath);

        //creating new dataoutputstream
        dataOutputStream = new DataOutputStream(connection.getOutputStream());

        //writing bytes to data outputstream
        dataOutputStream.writeBytes(twoHyphens + boundary + lineEnd);
        dataOutputStream.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                + selectedFilePath + "\"" + lineEnd);

        dataOutputStream.writeBytes(lineEnd);

        //returns no. of bytes present in fileInputStream
        bytesAvailable = fileInputStream.available();
        //selecting the buffer size as minimum of available bytes or 1 MB
        bufferSize = Math.min(bytesAvailable, maxBufferSize);
        //setting the buffer as byte array of size of bufferSize
        buffer = new byte[bufferSize];

        //reads bytes from FileInputStream(from 0th index of buffer to buffersize)
        bytesRead = fileInputStream.read(buffer, 0, bufferSize);

        //loop repeats till bytesRead = -1, i.e., no bytes are left to read
        while (bytesRead > 0) {

            //write the bytes read from inputstream
            dataOutputStream.write(buffer, 0, bufferSize);
            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);
        }

        dataOutputStream.writeBytes(lineEnd);
        dataOutputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

        serverResponseCode = connection.getResponseCode();
        String serverResponseMessage = connection.getResponseMessage();

        System.out.println("Server Response is: " + serverResponseMessage + ": " + serverResponseCode);

        //response code of 200 indicates the server status OK
        if (serverResponseCode == 200) {
            System.out.println("UPLOAD DONE");
        }

        //closing the input and output streams
        fileInputStream.close();
        dataOutputStream.flush();
        dataOutputStream.close();
    }
}
