package com.peprally.jeremy.peprally.network;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.peprally.jeremy.peprally.utils.Constants;

import org.json.JSONObject;

import java.net.URISyntaxException;

public class SocketIO {

    public Socket mSocket;
    private String nickname;

    public SocketIO(String nickname) {
        try {
            this.nickname = nickname;
            mSocket = IO.socket(Constants.SOCKETIO_SERVER_URL);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        mSocket.connect();
    }

    public void emitString(String event, String message) {
        mSocket.emit(event, message);
    }

    public void emitJSON(String event, JSONObject json) {
        mSocket.emit(event, json);
    }

    public void registerListener(String event, Emitter.Listener listenerFunc) {
        mSocket.on(event, listenerFunc);
    }

    public void disconnect() {
        emitString("leave_chat", nickname);
        mSocket.disconnect();
    }
}
