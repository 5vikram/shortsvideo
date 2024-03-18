package com.multitv.ott.shortvideo.network;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.multitv.ott.shortvideo.appcontroller.ApplicationController;

import java.util.Map;
import java.util.Set;

public class CommonApiPresenterImpl implements CommonApiPresenter {
    private CommonApiListener networkResponseListener;

    public CommonApiPresenterImpl(CommonApiListener networkResponseListener) {
        this.networkResponseListener = networkResponseListener;
    }


    @Override
    public void postRequest(String url, String apiName, Map<String, String> params, Map<String, String> headers) {

        Log.e("Api Request::::", apiName + "  url::::" + url);

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {

                    Log.e("Api Request::::", apiName + "  response::::" + response);
                    networkResponseListener.onSuccess(response);

                } catch (Exception e) {
                    e.printStackTrace();
                    networkResponseListener.onError(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Api Request::::", apiName + "  response::::" + error.getMessage());
                networkResponseListener.onError(error.getMessage());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                if (headers != null && headers.size() > 0) {
                    Set<String> keySet = headers.keySet();
                    for (String key : keySet) {
                        Log.e("Api Request::::", apiName + "  header::::" + key + "   " + headers.get(key));
                    }
                }

                return headers;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {


                if (params != null && params.size() > 0) {
                    Set<String> keySet = params.keySet();
                    for (String key : keySet) {
                        Log.e("Api Request::::", apiName + "  params::::" + key + "   " + params.get(key));
                    }

                }

                return params;
            }
        };
        // Adding request to request queue
        ApplicationController.Companion.getInstance().addToRequestQueue(jsonObjReq);
    }

    @Override
    public void getRequest(String url, String apiName, Map<String, String> headers) {
        StringRequest jsonObjReq = new StringRequest(Request.Method.GET,
                url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                try {
                    Log.e("api request", apiName + "  response::::" + response);
                    networkResponseListener.onSuccess(response);
                } catch (Exception e) {
                    e.printStackTrace();
                    networkResponseListener.onError(e.getMessage());
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("api request", apiName + "  response::::" + error.getMessage());
                networkResponseListener.onError(error.getMessage());
            }
        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                if (headers != null && headers.size() > 0) {
                    Set<String> keySet = headers.keySet();
                    for (String key : keySet) {
                        Log.e("api request", apiName + "  header::::" + key + "   " + headers.get(key));
                    }
                }

                return headers;
            }

        };
        ApplicationController.Companion.getInstance().addToRequestQueue(jsonObjReq);
    }

}
