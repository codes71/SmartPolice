package com.kbyai.facerecognition

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView

class ResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val identifiedFace = intent.getParcelableExtra("identified_face") as? Bitmap
        val enrolledFace = intent.getParcelableExtra("enrolled_face") as? Bitmap
        val identifiedName = intent.getStringExtra("identified_name")
        val similarity = intent.getFloatExtra("similarity", 0f)
        val livenessScore = intent.getFloatExtra("liveness", 0f)
        val yaw = intent.getFloatExtra("yaw", 0f)
        val roll = intent.getFloatExtra("roll", 0f)
        val pitch = intent.getFloatExtra("pitch", 0f)

        findViewById<ImageView>(R.id.imageEnrolled).setImageBitmap(enrolledFace)
        findViewById<ImageView>(R.id.imageIdentified).setImageBitmap(identifiedFace)
        findViewById<TextView>(R.id.textPerson).text = "Name: " + identifiedName
        findViewById<TextView>(R.id.textSimilarity).text = "Date Of Arrest: "
        findViewById<TextView>(R.id.textLiveness).text = "Location of Arrest: "
        findViewById<TextView>(R.id.textYaw).text = "Charges:"
        findViewById<TextView>(R.id.textRoll).text = "Court Records: "
        findViewById<TextView>(R.id.textPitch).text = "Case Disposition: "
        findViewById<TextView>(R.id.textPitch).text = "Incarceration History: "
        findViewById<TextView>(R.id.textPitch).text = "Criminal History: "
        findViewById<TextView>(R.id.textPitch).text = "Warrants: "

    }
}