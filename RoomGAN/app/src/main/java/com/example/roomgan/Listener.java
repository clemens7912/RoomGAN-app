package com.example.roomgan;

import org.json.JSONObject;

public interface Listener {
    public void receiveMessage(JSONObject data);
}
