package qf.radioandroid.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by pavelkuzmin on 02/03/2017.
 */

public class Utils {

    public static boolean checkDevice() {

        List<String> devices = new ArrayList<>();
        devices.add("ac:22:0b:64:a0:4b"); //pavel
        devices.add("a4:d1:8c:d6:8b:7c"); //nikita

        String mac = Utils.getMacAddress();

        return devices.contains(mac);
    }

    private static String getMacAddress() {

        try {

            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface networkInterface : all) {

                if (!networkInterface.getName().equalsIgnoreCase("wlan0"))
                    continue;

                byte[] macBytes = networkInterface.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder string = new StringBuilder();
                for (byte bytes : macBytes) {
                    string.append(String.format("%02X:", bytes));
//                    string.append(Integer.toHexString(bytes & 0xFF) + ":");
                }

                if (string.length() > 0) {
                    string.deleteCharAt(string.length() - 1);
                }

                return string.toString().toLowerCase();
            }

        } catch (Exception ignored) {
        }

        return "";
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
