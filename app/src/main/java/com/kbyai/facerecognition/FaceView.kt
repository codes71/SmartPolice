package com.kbyai.facerecognition

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Size
import android.view.View
import com.kbyai.facerecognition.SettingsActivity.Companion.getLivenessThreshold
import com.kbyai.facesdk.FaceBox

class FaceView(private var context: Context?, attrs: AttributeSet?) : View(
    context, attrs
) {
    private var realPaint: Paint? = null
    private var spoofPaint: Paint? = null
    private var frameSize: Size? = null
    private var faceBoxes: List<FaceBox>? = null

    constructor(context: Context?) : this(context, null) {
        this.context = context
        init()
    }

    init {
        init()
    }

    fun init() {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
        realPaint = Paint()
        realPaint!!.style = Paint.Style.STROKE
        realPaint!!.strokeWidth = 3f
        realPaint!!.color = Color.BLUE
        realPaint!!.isAntiAlias = true
        realPaint!!.textSize = 50f
        spoofPaint = Paint()
        spoofPaint!!.style = Paint.Style.STROKE
        spoofPaint!!.strokeWidth = 3f
        spoofPaint!!.color = Color.RED
        spoofPaint!!.isAntiAlias = true
        spoofPaint!!.textSize = 50f
    }

    fun setFrameSize(frameSize: Size?) {
        this.frameSize = frameSize
    }

    fun setFaceBoxes(faceBoxes: List<FaceBox>?) {
        this.faceBoxes = faceBoxes
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (frameSize != null && faceBoxes != null) {
            val x_scale = frameSize!!.width / canvas.width.toFloat()
            val y_scale = frameSize!!.height / canvas.height.toFloat()
            for (i in faceBoxes!!.indices) {
                val faceBox = faceBoxes!![i]
                if (faceBox.liveness < getLivenessThreshold(context!!)) {
                    spoofPaint!!.strokeWidth = 3f
                    spoofPaint!!.style = Paint.Style.FILL_AND_STROKE
                    canvas.drawText(
                        "SPOOF " + faceBox.liveness,
                        faceBox.x1 / x_scale + 10,
                        faceBox.y1 / y_scale - 30,
                        spoofPaint!!
                    )
                    spoofPaint!!.strokeWidth = 5f
                    spoofPaint!!.style = Paint.Style.STROKE
                    canvas.drawRect(
                        Rect(
                            (faceBox.x1 / x_scale).toInt(),
                            (faceBox.y1 / y_scale).toInt(),
                            (faceBox.x2 / x_scale).toInt(),
                            (faceBox.y2 / y_scale).toInt()
                        ), spoofPaint!!
                    )
                } else {
                    realPaint!!.strokeWidth = 3f
                    realPaint!!.style = Paint.Style.FILL_AND_STROKE
                    canvas.drawText(
                        "REAL " + faceBox.liveness,
                        faceBox.x1 / x_scale + 10,
                        faceBox.y1 / y_scale - 30,
                        realPaint!!
                    )
                    realPaint!!.style = Paint.Style.STROKE
                    realPaint!!.strokeWidth = 5f
                    canvas.drawRect(
                        Rect(
                            (faceBox.x1 / x_scale).toInt(),
                            (faceBox.y1 / y_scale).toInt(),
                            (faceBox.x2 / x_scale).toInt(),
                            (faceBox.y2 / y_scale).toInt()
                        ), realPaint!!
                    )
                }
            }
        }
    }
}