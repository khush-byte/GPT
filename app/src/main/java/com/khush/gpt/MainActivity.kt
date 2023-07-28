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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.khush.gpt.tools.CustomAdapter
import com.khush.gpt.tools.MyData
import com.khush.gpt.tools.NetworkManager
import org.json.JSONObject
import pl.droidsonroids.gif.GifImageView

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private lateinit var chatField: RecyclerView
    private lateinit var loadingAnim: GifImageView
    private lateinit var talkAnim: GifImageView
    private lateinit var sendBtn: Button
    private lateinit var data: ArrayList<MyData>
    private lateinit var adapter: CustomAdapter

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val messageField: EditText = findViewById(R.id.message_field)
        initComponents()
        initWebViewSettings()
        initAdapter()

        sendBtn.setOnClickListener {
            if (NetworkManager.isNetworkAvailable(this)) {
                if (messageField.text.isNotEmpty()) {
                    requestWebGPT(messageField.text.toString())

                    data.add(MyData(2,messageField.text.toString()))
                    chatField.smoothScrollToPosition((chatField.adapter?.itemCount ?: 0) - 1)
                    adapter.notifyDataSetChanged()

                    messageField.text.clear()
                    loadingAnim.visibility = View.VISIBLE
                    sendBtn.isEnabled = false
                }
            }else{
                Toast.makeText(
                    applicationContext,
                    "You don't have an internet connection!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun initAdapter(){
        chatField = findViewById(R.id.chatField)
        chatField.layoutManager = LinearLayoutManager(this)
        data = ArrayList()
        data.add(MyData(1,"Hi. How can I assist you today?"))
        adapter = CustomAdapter(this, data)
        chatField.adapter = adapter
    }

    private fun initComponents(){
        sendBtn = findViewById(R.id.send_btn)
        loadingAnim = findViewById(R.id.loadingAnim)
        talkAnim = findViewById(R.id.talkAnim)
        loadingAnim.visibility = View.GONE
        talkAnim.visibility = View.GONE
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebViewSettings(){
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
            Log.d("MyTag", toast)
            try {
                val json = JSONObject(toast)
                val json2 = json.getJSONArray("choices").getJSONObject(0)
                val message = json2.getJSONObject("message")
                val answer: String = message.getString("content")
                activity.runOnUiThread {
                    activity.setAnswer(answer)
                    finishLoading()
                }
            }catch (e: Exception){
                Log.d("MyTag", e.toString())
                activity.runOnUiThread {
                    activity.setAnswer("Unfortunately, your request limit has been exceeded and I am unable to provide a response at this time. I kindly advise you to restart the application")
                    finishLoading()
                    editAppState()
                }
            }
        }

        private fun finishLoading(){
            activity.runOnUiThread {
                activity.loadingAnim.visibility = View.GONE
                activity.sendBtn.isEnabled = true
            }
        }

        private fun editAppState(){
            val sharedPreferences = activity.getSharedPreferences("database", MODE_PRIVATE)
            with(sharedPreferences.edit()){
                putInt("appState", 11)
                apply()
            }
        }
    }
}