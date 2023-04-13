package com.example.creationownviewtest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.creationownviewtest.screenTwo.MainActivity
import com.example.creationownviewtest.databinding.ActivityMainTwoBinding
import kotlin.properties.Delegates
import kotlin.random.Random

class MainActivityTwo : AppCompatActivity() {


    lateinit var binding: ActivityMainTwoBinding
    var isFirstPlayer = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainTwoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.componentTicTakToeView.tickTacToeField = TickTacToeField(10, 10)

        binding.componentTicTakToeView.actionListener = {row, column, currentField ->
            // пользователь нажал/выбрал ячейку [row, column]

            // получить текущее значение ячейки
            val cell = currentField.getCell(row, column)
            if (cell == Cell.EMPTY) {
                // ячейка пуста, меняя ее на X или O
                if (isFirstPlayer) {
                    currentField.setCell(row, column, Cell.PLAYER_1)
                } else {
                    currentField.setCell(row, column, Cell.PLAYER_2)
                }
                isFirstPlayer = !isFirstPlayer
            }

        }
        binding.buttonRandomSize.setOnClickListener {
            binding.componentTicTakToeView.tickTacToeField = TickTacToeField(
                Random.nextInt(3, 10),
                Random.nextInt(3, 10)
            )
            for (i in 0..binding.componentTicTakToeView.tickTacToeField!!.rows) {
                for (j in 0..binding.componentTicTakToeView.tickTacToeField!!.columns)
                    if (Random.nextBoolean()) {
                        binding.componentTicTakToeView.tickTacToeField!!.setCell(
                            i,
                            j,
                            Cell.PLAYER_1
                        )
                    } else {
                        binding.componentTicTakToeView.tickTacToeField!!.setCell(
                            i,
                            j,
                            Cell.PLAYER_2
                        )
                    }
            }
        }
    }
}