package com.peprally.jeremy.peprally.network;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.peprally.jeremy.peprally.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class SocketIO {

    private Socket mSocket;
    private String senderUsername;
    private String receiverUsername;

    public SocketIO(String senderUsername, String receiverUsername) {
        try {
            this.senderUsername = senderUsername;
            this.receiverUsername = receiverUsername;

            mSocket = IO.socket(Constants.SOCKETIO_SERVER_URL);
        }
        catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        mSocket.connect();
        JSONObject jsonUsernames = new JSONObject();
        try {
            jsonUsernames.put("sender_username", senderUsername);
            jsonUsernames.put("receiver_username", receiverUsername);
        } catch (JSONException err) { err.printStackTrace(); }
        emitString("join_chat", jsonUsernames.toString());
    }

    public void emitString(String event, String message) {
        mSocket.emit(event, message);
    }

    public void registerListener(String event, Emitter.Listener listenerFunc) {
        mSocket.on(event, listenerFunc);
    }

    public void disconnect() {
        JSONObject jsonUsernames = new JSONObject();
        try {
            jsonUsernames.put("sender_username", senderUsername);
            jsonUsernames.put("receiver_username", receiverUsername);
        } catch (JSONException err) { err.printStackTrace(); }
        emitString("leave_chat", jsonUsernames.toString());
        mSocket.disconnect();
    }
}
