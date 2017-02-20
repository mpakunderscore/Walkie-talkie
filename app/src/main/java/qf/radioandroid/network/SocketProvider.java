package qf.radioandroid.network;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by pavelkuzmin on 19/02/2017.
 */

@Deprecated
public class SocketProvider {

    public SocketProvider() throws URISyntaxException {

        final Socket socket = IO.socket("http://192.168.1.47:7000");

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {

                socket.emit("data", "data");
                socket.disconnect();

            }

        }).on("event", new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {}

        });

        socket.connect();
    }
}
