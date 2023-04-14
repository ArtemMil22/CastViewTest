package com.example.creationownviewtest.screenTwoComponentView

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.creationownviewtest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val token = Any()
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var binding: ActivityMainBinding

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        with(binding.includeInmain) {
            negativeButton.text = "Cancel"
            positiveButton.text = "Oк"
            positiveButton.setOnClickListener {
                progressBars.visibility = View.VISIBLE
                positiveButton.visibility = View.INVISIBLE
                negativeButton.visibility = View.INVISIBLE
            }
        }

        binding.castButton.setListener {

            if (it == BottomButtonAction.POSITIVE) {
                binding.castButton.isProgressMode = true
                handler.postDelayed({
                    binding.castButton.setPositiveButtonText("Update Ok")
                    Toast.makeText(this, "Positive", Toast.LENGTH_SHORT).show()
                    binding.castButton.isProgressMode = false
                }, token, 2000)
            } else if (it == BottomButtonAction.NEGATIVE) {
                binding.castButton.setNegativeButtonText("Update Cancel")
                Toast.makeText(this, "Negative", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(token)
    }
}