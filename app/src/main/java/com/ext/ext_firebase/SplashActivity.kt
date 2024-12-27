package com.ext.ext_firebase

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ext.ext_firebase.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Handler().postDelayed({
            if (isUserLoggedIn()) {
                navigateToNameActivity()
            }
            else {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
        }, 2000)
    }

    private fun isUserLoggedIn(): Boolean {
        return SharedPreferenceManager.isLoggedIn(this@SplashActivity)
    }

    private fun navigateToNameActivity() {
        val intent = Intent(this@SplashActivity, NameActivity::class.java)
        startActivity(intent)
        finish()
    }
}