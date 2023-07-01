package org.tensorflow.lite.examples.imageclassification.utils

import android.graphics.Bitmap
import java.lang.Exception

interface OnBackgroundChangeListener {

    fun onSuccess(bitmap: Bitmap)

    fun onFailed(exception: Exception)

}