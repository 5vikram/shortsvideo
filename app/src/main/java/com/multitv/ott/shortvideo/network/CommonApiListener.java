package com.multitv.ott.shortvideo.network;

public interface CommonApiListener {
    void onSuccess(String response);

    void onError(String message);
}