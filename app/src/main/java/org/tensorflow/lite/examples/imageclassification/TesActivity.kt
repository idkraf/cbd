package org.tensorflow.lite.examples.imageclassification

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import org.tensorflow.lite.DataType
import org.tensorflow.lite.examples.imageclassification.databinding.ActivityTesBinding
import org.tensorflow.lite.examples.imageclassification.ml.Model
import org.tensorflow.lite.examples.imageclassification.utils.BackgroundRemover
import org.tensorflow.lite.examples.imageclassification.utils.OnBackgroundChangeListener
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder


class TesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTesBinding
    var imageSize = 224
    private val imageResult =
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let { uri ->
                binding.img.setImageURI(uri)
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        binding.toolbar.setTitle("Gallery")


        imageResult.launch("image/*")

        binding.removeBgBtn.setOnClickListener {
            removeBg()
        }

        binding.classifyBtn.setOnClickListener{
            binding.img.invalidate()
            classifyImage(Bitmap.createScaledBitmap(binding.img.drawable.toBitmap(), imageSize, imageSize, false))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun removeBg() {
        binding.img.invalidate()
        //var bitmap = Bitmap.createScaledBitmap(binding.img.drawable.toBitmap(), imageSize, imageSize, false)
        BackgroundRemover.bitmapForProcessing(
            Bitmap.createScaledBitmap(binding.img.drawable.toBitmap(), imageSize, imageSize, false),
            true,
            object : OnBackgroundChangeListener {
                override fun onSuccess(bitmap: Bitmap) {
                    binding.img.setImageBitmap(bitmap)
                }

                override fun onFailed(exception: Exception) {
                    Toast.makeText(this@TesActivity, "Error Occur", Toast.LENGTH_SHORT).show()
                }

            })
    }

    fun classifyImage(image: Bitmap) {
        try {
            val model: Model = Model.newInstance(applicationContext)

            // Creates inputs for reference.
            val inputFeature0 =
                TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)
            val byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3)
            byteBuffer.order(ByteOrder.nativeOrder())
            val intValues = IntArray(imageSize * imageSize)
            image.getPixels(intValues, 0, image.width, 0, 0, image.width, image.height)
            var pixel = 0
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (i in 0 until imageSize) {
                for (j in 0 until imageSize) {
                    val `val` = intValues[pixel++] // RGB
                    byteBuffer.putFloat((`val` shr 16 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` shr 8 and 0xFF) * (1f / 1))
                    byteBuffer.putFloat((`val` and 0xFF) * (1f / 1))
                }
            }
            inputFeature0.loadBuffer(byteBuffer)

            // Runs model inference and gets result.
            val outputs: Model.Outputs = model.process(inputFeature0)
            val outputFeature0: TensorBuffer = outputs.getOutputFeature0AsTensorBuffer()
            val confidences = outputFeature0.floatArray
            // find the index of the class with the biggest confidence.
            var maxPos = 0
            var maxConfidence = 0f
            for (i in confidences.indices) {
                if (confidences[i] > maxConfidence) {
                    maxConfidence = confidences[i]
                    maxPos = i
                }
            }
            val classes =
                arrayOf("DarkRoasting", "GreenRoasting", "LightRoasting", "MediumRoasting")
            binding.result.setText(classes[maxPos])
            var s = ""
            for (i in classes.indices) {
                s += String.format("%s : %.1f%%\n", classes[i], confidences[i] * 100)
            }
            binding.confidence.setText(s)
            // Releases model resources if no longer used.
            model.close()
        } catch (e: IOException) {
            // TODO Handle the exception
        }
    }

}