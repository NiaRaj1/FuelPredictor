package com.machinelearning.fueleffeciency

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class MainActivity : AppCompatActivity() {
    private val meanValue = floatArrayOf(
        5.4487f,
        190.7628f,
        102.8589f,
        2926.5897f,
        15.4692f,
        76.3076f,
        0.6282f,
        0.1538f,
        0.2179f
    )

    private val standardDeviation = floatArrayOf(
        1.7406f,
        106.4947f,
        40.2552f,
        874.9004f,
        2.64929f,
        3.7218f,
        0.4864f,
        0.3631f,
        0.4155f
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val interpreter = Interpreter(loadModelFile(), null)
        val cylinders = findViewById<EditText>(R.id.editText)
        val displacement = findViewById<EditText>(R.id.editText2)
        val horsePower = findViewById<EditText>(R.id.editText3)
        val weight = findViewById<EditText>(R.id.editText4)
        val accelration = findViewById<EditText>(R.id.editText5)
        val modelYear = findViewById<EditText>(R.id.editText6)

        val origin = findViewById<Spinner>(R.id.spinner)
        val arrayAdapter = ArrayAdapter(
            applicationContext,
            R.layout.support_simple_spinner_dropdown_item,
            arrayOf("USA", "Europe", "Japan")
        )
        origin.adapter = arrayAdapter
        val result = findViewById<TextView>(R.id.textView2)

        val btn: Button = findViewById(R.id.button)
        btn.setOnClickListener {
            val floats = Array(1) {
                FloatArray(
                    9
                )
            }
            floats[0][0] =
                (cylinders.text.toString().toFloat() - meanValue.get(0)) / standardDeviation.get(0)
            floats[0][1] =
                (displacement.text.toString().toFloat() - meanValue.get(1)) / standardDeviation.get(
                    1
                )
            floats[0][2] =
                (horsePower.text.toString().toFloat() - meanValue.get(2)) / standardDeviation.get(2)
            floats[0][3] =
                (weight.text.toString().toFloat() - meanValue.get(3)) / standardDeviation.get(3)
            floats[0][4] = (accelration.text.toString()
                .toFloat() - meanValue.get(4)) / standardDeviation.get(4)
            floats[0][5] =
                (modelYear.text.toString().toFloat() - meanValue.get(5)) / standardDeviation.get(5)
            when (origin.selectedItemPosition) {
                0 -> {
                    floats[0][6] = (1 - meanValue.get(6)) / standardDeviation.get(6)
                    floats[0][7] = (0 - meanValue.get(7)) / standardDeviation.get(7)
                    floats[0][8] = (0 - meanValue.get(8)) / standardDeviation.get(8)
                }
                1 -> {
                    floats[0][6] = (0 - meanValue.get(6)) / standardDeviation.get(6)
                    floats[0][7] = (1 - meanValue.get(7)) / standardDeviation.get(7)
                    floats[0][8] = (0 - meanValue.get(8)) / standardDeviation.get(8)
                }
                2 -> {
                    floats[0][6] = (0 - meanValue.get(6)) / standardDeviation.get(6)
                    floats[0][7] = (0 - meanValue.get(7)) / standardDeviation.get(7)
                    floats[0][8] = (1 - meanValue.get(8)) / standardDeviation.get(8)
                }
            }
            val res: Float = doInference(floats, interpreter)
            result.text = res.toString() + ""
        }
    }
    private fun doInference(input: Array<FloatArray>, interpreter: Interpreter): Float {
        val output = Array(1) {
            FloatArray(
                1
            )
        }
        interpreter.run(input, output)
        return output[0][0]
    }

    private fun loadModelFile(): MappedByteBuffer {
        val model = assets.openFd("automobile.tflite")
        val fileInputSystem = FileInputStream(model.fileDescriptor)
        val channel = fileInputSystem.channel
        val startOffset = model.startOffset
        val lenght = model.length
        return channel.map(FileChannel.MapMode.READ_ONLY, startOffset, lenght)
    }
}