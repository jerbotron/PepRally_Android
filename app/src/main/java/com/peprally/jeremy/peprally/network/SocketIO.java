package com.peprally.jeremy.peprally.network;

import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.peprally.jeremy.peprally.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class SocketIO {

    private Socket mSocket;
    private String senderNickname;
    private String receiverNickname;

    public SocketIO(String senderNickname, String receiverNickname) {
        try {
            this.senderNickname = senderNickname;
            this.receiverNickname = receiverNickname;

            mSocket = IO.socket(Constants.SOCKETIO_SERVER_URL);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        mSocket.connect();
        JSONObject jsonNicknames = new JSONObject();
        try {
            jsonNicknames.put("sender_nickname", senderNickname);
            jsonNicknames.put("receiver_nickname", receiverNickname);
        } catch (JSONException err) { err.printStackTrace(); }
        emitString("join_chat", jsonNicknames.toString());
    }

    public void emitString(String event, String message) {
        mSocket.emit(event, message);
    }

    public void registerListener(String event, Emitter.Listener listenerFunc) {
        mSocket.on(event, listenerFunc);
    }

    public void disconnect() {
        JSONObject jsonNicknames = new JSONObject();
        try {
            jsonNicknames.put("sender_nickname", senderNickname);
            jsonNicknames.put("receiver_nickname", receiverNickname);
        } catch (JSONException err) { err.printStackTrace(); }
        emitString("leave_chat", jsonNicknames.toString());
        mSocket.disconnect();
    }
}
