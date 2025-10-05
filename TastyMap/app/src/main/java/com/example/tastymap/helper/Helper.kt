package com.example.tastymap.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.view.Gravity
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Helper {

    const val POINTS_FOR_NEW_LEVEL = 100;

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)

        val squareBitmap = if (bitmap.width != bitmap.height) {
            val x = (bitmap.width - size) / 2
            val y = (bitmap.height - size) / 2
            Bitmap.createBitmap(bitmap, x, y, size, size)
        } else {
            bitmap
        }

        val output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
        }
        val rect = Rect(0, 0, size, size)

        canvas.drawARGB(0, 0, 0, 0)

        val radius = (size / 2).toFloat()
        canvas.drawCircle(radius, radius, radius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(squareBitmap, rect, rect, paint)

        return output
    }

    suspend fun bitmapDescriptorFromUrl(url: String, context: Context, sizeDp: Int): BitmapDescriptor? = withContext(Dispatchers.IO) {
        try {
            if (url.isEmpty()) return@withContext null

            val density = context.resources.displayMetrics.density
            val sizePx = (sizeDp * density).toInt()

            val imageLoader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(sizePx, sizePx)
                .allowHardware(false)
                .build()

            val result = imageLoader.execute(request)

            if (result is SuccessResult) {
                val originalBitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@withContext null

                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, sizePx, sizePx, true)

                val circularBitmap = getCircularBitmap(scaledBitmap)

                return@withContext BitmapDescriptorFactory.fromBitmap(circularBitmap)
            }

            return@withContext null
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }

    fun bitmapDescriptorFromVector(context: Context, @DrawableRes vectorResId: Int, sizeDp: Int): BitmapDescriptor? {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()

        val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null

        drawable.setBounds(0, 0, sizePx, sizePx)

        val bm = Bitmap.createBitmap(
            sizePx,
            sizePx,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bm)

        drawable.draw(canvas)

        val circularBm = getCircularBitmap(bm)

        return BitmapDescriptorFactory.fromBitmap(circularBm)
    }

    fun formatTimestamp(timestamp: Long): String {
        val diff = System.currentTimeMillis() - timestamp
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "Pre $days dana"
            hours > 0 -> "Pre $hours sati"
            minutes > 0 -> "Pre $minutes minuta"
            else -> "Upravo sad"
        }
    }

    fun showToast(context: Context, msg: String) {
        val toast = Toast.makeText(context, msg, Toast.LENGTH_LONG)
        toast.setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 200)
        toast.show()
    }

    fun calculateDistanceInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371 // Radius Zemlje u km

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)

        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return r * c
    }

    var showGlobalSnackbar: ((String) -> Unit)? = null

    fun showSnackbar(message: String) {
        showGlobalSnackbar?.invoke(message)
    }
}