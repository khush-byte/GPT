package com.khush.gpt

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.khush.gpt.tools.CustomAdapter
import com.khush.gpt.tools.MyData
import com.khush.gpt.tools.NetworkManager
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView
import java.util.Locale
class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var chatField: RecyclerView
    private lateinit var loadingAnim: GifImageView
    lateinit var talkAnim: GifImageView
    private lateinit var sendBtn: Button
    private lateinit var data: ArrayList<MyData>
    private lateinit var adapter: CustomAdapter
    lateinit var textToSpeech: TextToSpeech
    private lateinit var mSound: MediaPlayer
    var speechMode = true
    private lateinit var sharedPreferences: SharedPreferences

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("database", MODE_PRIVATE)

        val messageField: EditText = findViewById(R.id.message_field)
        initComponents()
        initWebViewSettings()
        initSpeechMode()
        initAdapter()

        sendBtn.setOnClickListener {
            if (textToSpeech.isSpeaking) {
                textToSpeech.stop()
                talkAnim.visibility = View.GONE
                sendBtn.text = "send"
            }

            if (NetworkManager.isNetworkAvailable(this)) {
                if (messageField.text.isNotEmpty()) {
                    requestWebGPT(messageField.text.toString())

                    data.add(MyData(2, messageField.text.toString()))
                    chatField.smoothScrollToPosition((chatField.adapter?.itemCount ?: 0) - 1)
                    adapter.notifyDataSetChanged()

                    messageField.text.clear()
                    loadingAnim.visibility = View.VISIBLE
                    sendBtn.isEnabled = false
                }
            } else {
                Toast.makeText(
                    applicationContext,
                    "You don't have an internet connection!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initSpeechMode() {
        mSound = MediaPlayer.create(this, R.raw.chin3)
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US
                textToSpeech.setSpeechRate(0.9f)
                textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String) {
                        //Log.i("MyTag","On Start")
                        runOnUiThread {
                            talkAnim.visibility = View.VISIBLE
                            sendBtn.text = "stop"
                        }
                    }

                    override fun onDone(utteranceId: String) {
                        //Log.i("MyTag","On Done")
                        runOnUiThread {
                            talkAnim.visibility = View.GONE
                            sendBtn.text = "send"
                        }
                    }

                    override fun onError(utteranceId: String) {
                        Log.i("MyTag", "On Error")
                    }
                })
            } else {
                Log.i("MyTag", "Initialization Failed")
            }
        }
        speechMode = sharedPreferences.getBoolean("speechMode", true)
    }

    private fun initAdapter() {
        chatField = findViewById(R.id.chatField)
        chatField.layoutManager = LinearLayoutManager(this)

        data = ArrayList()
        data.add(MyData(1, "Hi. How can I assist you today?"))

        adapter = CustomAdapter(this, data)
        chatField.adapter = adapter

        if (speechMode) {
            mSound.setOnCompletionListener {
                textToSpeech.speak(
                    "Hi. How can I assist you today?",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
                )
            }
            mSound.start()
        }
    }

    private fun initComponents() {
        sendBtn = findViewById(R.id.send_btn)
        loadingAnim = findViewById(R.id.loadingAnim)
        talkAnim = findViewById(R.id.talkAnim)
        loadingAnim.visibility = View.GONE
        talkAnim.visibility = View.GONE
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings() {
        webView = findViewById(R.id.myWeb)
        webView.settings.javaScriptEnabled = true
        val myJavaScriptInterface = MyJavascriptInterface(this@MainActivity)
        webView.addJavascriptInterface(myJavaScriptInterface, "MyFunction")
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                return true
            }
        }
    }

    fun setAnswer(msg: String) {
        // Log.d("MyTag", msg)
        data.add(MyData(1, msg))
        chatField.smoothScrollToPosition((chatField.adapter?.itemCount ?: 0) - 1)
        adapter.notifyDataSetChanged()

        if (speechMode) {
            mSound.setOnCompletionListener {
                textToSpeech.speak(
                    msg,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
                )
            }
            mSound.start()
        }
    }

    private fun requestWebGPT(mgs: String) {
        webView.loadDataWithBaseURL(
            "https://talkai.info/chat/send/",
            "<script type='text/javascript'>const formData = new FormData();formData.append('message', '${mgs}');fetch('https://talkai.info/chat/send/', {method: 'POST', mode: 'no-cors', body: formData}).then((response) => {return response.text();}).then((html) => {MyFunction.onButtonClick(html);});</script>",
            "text/html",
            "utf-8",
            null
        )
    }

    @SuppressLint("InflateParams")
    fun onSettingsButtonClick(view: View?) {
        closeKeyboard()
        // inflate the layout of the popup window
        val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val popupView = inflater.inflate(R.layout.settings_popup, null)

        // create the popup window
        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true // lets taps outside the popup also dismiss it
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
        val closeBtn = popupView.findViewById<Button>(R.id.popup_btn_close)
        val switchBtn = popupView.findViewById<SwitchMaterial>(R.id.popupSpeechModeSwitch)
        closeBtn.setOnClickListener {
            popupWindow.dismiss()
        }
        switchBtn.isChecked = speechMode

        switchBtn.setOnCheckedChangeListener { buttonView, isChecked ->
            speechMode = switchBtn.isChecked
            with(sharedPreferences.edit()) {
                putBoolean("speechMode", speechMode)
                apply()
            }
        }
    }

    private fun closeKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    class MyJavascriptInterface internal constructor(var activity: MainActivity) {
        @JavascriptInterface
        fun onButtonClick(toast: String) {
            //Log.d("MyTag", toast)
            try {
                val json = JSONObject(toast)
                val json2 = json.getJSONArray("choices").getJSONObject(0)
                val message = json2.getJSONObject("message")
                val answer: String = message.getString("content")
                activity.runOnUiThread {
                    activity.setAnswer(answer)
                    finishLoading()
                }
            } catch (e: Exception) {
                //Log.d("MyTag", e.toString())
                activity.runOnUiThread {
                    activity.setAnswer("Unfortunately, your request limit has been exceeded and I am unable to provide a response at this time. I kindly advise you to restart the application")
                    finishLoading()
                }
                editAppState()
            }
        }

        private fun finishLoading() {
            activity.runOnUiThread {
                activity.loadingAnim.visibility = View.GONE
                activity.sendBtn.isEnabled = true
            }
        }

        private fun editAppState() {
            with(activity.sharedPreferences.edit()) {
                putInt("appState", 11)
                apply()
            }
        }
    }
}