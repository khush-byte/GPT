package com.khush.gpt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import com.khush.gpt.tools.NetworkManager

class CheckActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check)

        val intBtn: Button = findViewById(R.id.intBtn)
        intBtn.setOnClickListener {
            if (NetworkManager.isNetworkAvailable(this)) {
                val intent = Intent(this, SplashActivity::class.java)
                val bundle = ActivityOptionsCompat.makeCustomAnimation(
                    applicationContext,
                    android.R.anim.fade_in, android.R.anim.fade_out
                ).toBundle()
                startActivity(intent, bundle)
                this.finish()
            }else{
                Toast.makeText(
                    applicationContext,
                    "You don't have an internet connection!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}