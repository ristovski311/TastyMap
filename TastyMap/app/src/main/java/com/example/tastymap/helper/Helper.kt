package com.example.tastymap.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory

object Helper {

    fun getCircularBitmap(bitmap: Bitmap): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        val paint = Paint().apply {
            isAntiAlias = true
        }
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        canvas.drawARGB(0, 0, 0, 0)

        val radius = (bitmap.width / 2).toFloat()
        canvas.drawCircle(radius, radius, radius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
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


}