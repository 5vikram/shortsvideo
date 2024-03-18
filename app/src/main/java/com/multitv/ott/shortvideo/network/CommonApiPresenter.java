package com.multitv.ott.shortvideo.network;

import java.util.Map;

public interface CommonApiPresenter {
    void postRequest(String url, String apiName, Map<String, String> params, Map<String, String> header);

    void getRequest(String url, String apiName, Map<String, String> header);

}