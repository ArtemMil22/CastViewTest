package com.example.creationownviewtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.creationownviewtest.activityOne.MainActivity
import com.example.creationownviewtest.databinding.ActivityMainTwoBinding

class MainActivityTwo : AppCompatActivity() {
    lateinit var binding: ActivityMainTwoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}