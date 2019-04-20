package com.apicraft.locationpicker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * Created by wizard on 21/04/19.
 */
class SplashScreenActivity : AppCompatActivity() {
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.splash_main)
		setSupportActionBar(toolbar)
		showSplashScreen()
	}
	
	private fun showSplashScreen() {
		Handler().postDelayed(
			{
				val intent = Intent(this, MainActivity::class.java)
				intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
				this.startActivity(intent)
				this.finish()
			}, SPLASH_TIME_OUT.toLong()
		)
	}
	
	companion object {
		private const val SPLASH_TIME_OUT = 3000
	}
}