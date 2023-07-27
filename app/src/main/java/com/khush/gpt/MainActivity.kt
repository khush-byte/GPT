package com.khush.gpt

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var chatField: RecyclerView
    private lateinit var loadingAnim: GifImageView
    private lateinit var talkAnim: GifImageView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val messageField: EditText = findViewById(R.id.message_field)
        val sendBtn: Button = findViewById(R.id.send_btn)
        initComponents()
        initWebViewSettings()

        sendBtn.setOnClickListener {
            if(messageField.text.isNotEmpty()){
                requestWebGPT(messageField.text.toString())
                messageField.text.clear()
                loadingAnim.visibility = View.VISIBLE
            }
        }
    }

    private fun initComponents(){
        webView = findViewById(R.id.myWeb)
        loadingAnim = findViewById(R.id.loadingAnim)
        talkAnim = findViewById(R.id.talkAnim)
        chatField = findViewById(R.id.chatField)
        loadingAnim.visibility = View.GONE
        talkAnim.visibility = View.GONE
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings(){
        webView.settings.javaScriptEnabled = true
        val myJavaScriptInterface = MyJavascriptInterface(this@MainActivity)
        webView.addJavascriptInterface(myJavaScriptInterface, "MyFunction")
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                return true
            }
        }
    }

    fun setAnswer(msg: String) {
        Log.d("MyTag", msg)
    }

    private fun requestWebGPT(mgs: String){
        webView.loadDataWithBaseURL(
            "https://talkai.info/chat/send/",
            "<script type='text/javascript'>const formData = new FormData();formData.append('message', '${mgs}');fetch('https://talkai.info/chat/send/', {method: 'POST', mode: 'no-cors', body: formData}).then((response) => {return response.text();}).then((html) => {MyFunction.onButtonClick(html);});</script>",
            "text/html",
            "utf-8",
            null
        )
    }

    class MyJavascriptInterface internal constructor(var activity: MainActivity) {
        @JavascriptInterface
        fun onButtonClick(toast: String) {
            try {
                val json = JSONObject(toast)
                val json2 = json.getJSONArray("choices").getJSONObject(0)
                val message = json2.getJSONObject("message")
                val answer: String = message.getString("content")
                activity.setAnswer(answer)
                finishLoading()
            }catch (e: Exception){
                Log.d("MyTag", e.toString())
                finishLoading()
            }
        }

        private fun finishLoading(){
            activity.runOnUiThread {
                activity.loadingAnim.visibility = View.GONE
            }
        }
    }
}