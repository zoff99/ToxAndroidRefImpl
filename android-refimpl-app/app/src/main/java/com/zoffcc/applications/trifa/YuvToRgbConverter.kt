@file:Suppress("DEPRECATION")

package com.zoffcc.applications.trifa

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.media.Image
import android.renderscript.*
import java.nio.ByteBuffer

/**
 * Helper class used to convert a [Image] object from
 * [ImageFormat.YUV_420_888] format to an RGB [Bitmap] object, it has equivalent
 * functionality to https://github
 * .com/androidx/androidx/blob/androidx-main/camera/camera-core/src/main/java/androidx/camera/core/ImageYuvToRgbConverter.java
 *
 * NOTE: This has been tested in a limited number of devices and is not
 * considered production-ready code. It was created for illustration purposes,
 * since this is not an efficient camera pipeline due to the multiple copies
 * required to convert each frame. For example, this
 * implementation
 * (https://stackoverflow.com/questions/52726002/camera2-captured-picture-conversion-from-yuv-420-888-to-nv21/52740776#52740776)
 * might have better performance.
 */
class YuvToRgbConverter(context: Context) {
    private val rs = RenderScript.create(context)
    private val scriptYuvToRgb =
            ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

    // Do not add getters/setters functions to these private variables
    // because yuvToRgb() assume they won't be modified elsewhere
    private var yuvBits: ByteBuffer? = null
    private var bytes: ByteArray = ByteArray(0)
    private var inputAllocation: Allocation? = null
    private var outputAllocation: Allocation? = null

    @Synchronized
    fun yuvToRgb(image: Image, output: Bitmap) {
        val yuvBuffer = YuvByteBuffer(image, yuvBits)
        yuvBits = yuvBuffer.buffer

        if (needCreateAllocations(image, yuvBuffer)) {
            val yuvType = Type.Builder(rs, Element.U8(rs))
                    .setX(image.width)
                    .setY(image.height)
                    .setYuvFormat(yuvBuffer.type)
            inputAllocation = Allocation.createTyped(
                    rs,
                    yuvType.create(),
                    Allocation.USAGE_SCRIPT
            )
            bytes = ByteArray(yuvBuffer.buffer.capacity())
            val rgbaType = Type.Builder(rs, Element.RGBA_8888(rs))
                    .setX(image.width)
                    .setY(image.height)
            outputAllocation = Allocation.createTyped(
                    rs,
                    rgbaType.create(),
                    Allocation.USAGE_SCRIPT
            )
        }

        yuvBuffer.buffer.get(bytes)
        inputAllocation!!.copyFrom(bytes)

        // Convert NV21 or YUV_420_888 format to RGB
        inputAllocation!!.copyFrom(bytes)
        scriptYuvToRgb.setInput(inputAllocation)
        scriptYuvToRgb.forEach(outputAllocation)
        outputAllocation!!.copyTo(output)
    }

    private fun needCreateAllocations(image: Image, yuvBuffer: YuvByteBuffer): Boolean {
        return (inputAllocation == null ||               // the very 1st call
                inputAllocation!!.type.x != image.width ||   // image size changed
                inputAllocation!!.type.y != image.height ||
                inputAllocation!!.type.yuv != yuvBuffer.type || // image format changed
                bytes.size == yuvBuffer.buffer.capacity())
    }
}