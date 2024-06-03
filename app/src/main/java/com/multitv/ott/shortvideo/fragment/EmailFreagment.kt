package com.multitv.ott.shortvideo.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.multitv.ott.shortvideo.R
import com.multitv.ott.shortvideo.ShortsVideoActivity
import com.multitv.ott.shortvideo.model.MobileLoginData
import com.multitv.ott.shortvideo.network.CommonApiListener
import com.multitv.ott.shortvideo.network.CommonApiPresenterImpl
import com.multitv.ott.shortvideo.network.Json
import com.multitv.ott.shortvideo.utils.SharedPreference
import org.json.JSONException
import org.json.JSONObject

class EmailFreagment : Fragment() {

    companion object {
        fun newInstance(): EmailFreagment {
            val searchFragment = EmailFreagment()
            return searchFragment
        }
    }

    private lateinit var loginButton: TextView
    private lateinit var passwardEt: EditText
    private lateinit var emailEditText: EditText
    private lateinit var progressbar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val v = inflater.inflate(R.layout.fragment_email, container, false)
        loginButton = v.findViewById(R.id.loginButton)
        emailEditText = v.findViewById(R.id.emailEditText)
        passwardEt = v.findViewById(R.id.passwardEt)
        progressbar = v.findViewById(R.id.progressbar)
        loginButton.setOnClickListener {
            val username = passwardEt.text.toString()
            val phoneNumber = emailEditText.text.toString()

            if (!username.isNullOrEmpty() && !phoneNumber.isNullOrEmpty() && username.length > 7) {
                loginApiRequest(username, phoneNumber)
            } else {
                Toast.makeText(
                    activity,
                    "Please enter a vaild username and mobile number.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        return v
    }

    private fun loginApiRequest(username: String, phoneNumber: String) {
        val header = HashMap<String, String>()
        val params = HashMap<String, String>()
        progressbar.visibility = View.VISIBLE

        val jsonObject = JSONObject()
        try {
            jsonObject.put("make_model", "Chrome")
            jsonObject.put("os", "android")
            jsonObject.put("screen_resolution", "1848*543")
            jsonObject.put("push_device_token", "")
            jsonObject.put("device_type", "phone")
            jsonObject.put("platform", "android")
            jsonObject.put("device_unique_id", "222aa2750a651f277cfd271409a54836")
            jsonObject.put("one_signal_id", "fdih543f7dsgv3")
        } catch (e: JSONException) {
            e.printStackTrace()
        }


        val locationPbj = JSONObject()
        try {
            locationPbj.put("loc_country", "India")
            locationPbj.put("city", "Gaya")
            locationPbj.put("loc_state", "Bihar")
            locationPbj.put("ip", "2409:408a:2c9a:1159:43e5:5674:f9e6:e1de")
            locationPbj.put("lat", "24.7935")
            locationPbj.put("long", "85.012")
            locationPbj.put("pincode", "823002")
            locationPbj.put("isp", "Reliance Jio Infocomm Limited")
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        params["devicedetail"] = jsonObject.toString()
        params["location"] = locationPbj.toString()
        params["type"] = "email"
        params["password"] = username
        params["email"] = phoneNumber

        //var contentListUrl=authModel.masterUrls.

        CommonApiPresenterImpl(object : CommonApiListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onSuccess(response: String) {
                progressbar.visibility = View.GONE
                Log.e("Vikram:::", response)

                Log.e("Vikram:::", response)
                val authModel = Json.parse(response, MobileLoginData::class.java) as MobileLoginData

                if (authModel.code == 1) {
                    SharedPreference().setPreferenceString(
                        activity,
                        "user_id",
                        "" + authModel.result?.id.toString()
                    )

                    SharedPreference().setPreferenceString(
                        activity,
                        "user_info",
                        "" + authModel.result?.email.toString()
                    )

                    val intent = Intent(activity, ShortsVideoActivity::class.java)
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                    activity?.finish()
                } else {
                    Toast.makeText(
                        activity,
                        "Something went wrong , please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onError(message: String?) {
                progressbar.visibility = View.GONE

                Toast.makeText(
                    activity,
                    "Something went wrong , please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }).postRequest(
            "https://expo.multitvsolution.com/api/v6/user/email_login/token/15zh353kd4dese/device/web",
            "Login",
            params, header
        )
    }
}