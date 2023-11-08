package com.kbyai.facerecognition

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.kbyai.facerecognition.SettingsActivity.Companion.getLivenessLevel
import com.kbyai.facesdk.FaceDetectionParam
import com.kbyai.facesdk.FaceSDK
import io.fotoapparat.Fotoapparat
import io.fotoapparat.parameter.Resolution
import io.fotoapparat.preview.Frame
import io.fotoapparat.preview.FrameProcessor
import io.fotoapparat.selector.front
import io.fotoapparat.view.CameraView


class CameraActivityKt : AppCompatActivity() {

    val TAG = CameraActivity::class.java.simpleName
    val PREVIEW_WIDTH = 720
    val PREVIEW_HEIGHT = 1280

    private lateinit var cameraView: CameraView
    private lateinit var faceView: FaceView
    private lateinit var fotoapparat: Fotoapparat
    private lateinit var context: Context

    private var recognized = false
    private lateinit var resultText : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera_kt)

        context = this
        cameraView = findViewById(R.id.preview)
        faceView = findViewById(R.id.faceView)

        fotoapparat = Fotoapparat.with(this)
            .into(cameraView)
            .lensPosition(front())
            .frameProcessor(FaceFrameProcessor())
            .previewResolution { Resolution(PREVIEW_HEIGHT, PREVIEW_WIDTH) }
            .build()

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_DENIED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 1)
        } else {
            fotoapparat.start()
        }
    }

    override fun onResume() {
        super.onResume()
        recognized = false
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            fotoapparat.start()
        }
    }

    override fun onPause() {
        super.onPause()
        fotoapparat.stop()
        faceView.setFaceBoxes(null)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) {
                fotoapparat.start()
            }
        }
    }
    protected fun showInputDialog() {
// get Prompts.xml view
        val layoutInflater = LayoutInflater.from(this@CameraActivityKt)
        val promptView: View = layoutInflater.inflate(R.layout.dialoglayout, null)
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this@CameraActivityKt)
        alertDialogBuilder.setView(promptView)
        val editText = promptView.findViewById<View>(R.id.nameedittext) as EditText
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
            .setPositiveButton("OK",
                DialogInterface.OnClickListener { dialog, id -> resultText.setText("Hello, " + editText.text) })
            .setNegativeButton("Cancel",
                DialogInterface.OnClickListener { dialog, id -> dialog.cancel() })

        // create an alert dialog
        val alert: AlertDialog = alertDialogBuilder.create()
        alert.show()
    }

    inner class FaceFrameProcessor : FrameProcessor {

        override fun process(frame: Frame) {

            if (recognized == true) {
                return
            }

            var cameraMode = 7
            if (SettingsActivity.getCameraLens(context) == CameraSelector.LENS_FACING_BACK) {
                cameraMode = 8
            }

            val bitmap =
                FaceSDK.yuv2Bitmap(frame.image, frame.size.width, frame.size.height, cameraMode)

            val faceDetectionParam = FaceDetectionParam()
            faceDetectionParam.check_liveness = true
            faceDetectionParam.check_liveness_level = getLivenessLevel(context)
            val faceBoxes = FaceSDK.faceDetection(bitmap, faceDetectionParam)

            runOnUiThread {
                faceView.setFrameSize(Size(bitmap.width, bitmap.height))
                faceView.setFaceBoxes(faceBoxes)
            }

            if (faceBoxes.size > 0) {
                if (faceBoxes.size == 1) {
                    val faceBox = faceBoxes[0]
                    if (faceBox.liveness > SettingsActivity.getLivenessThreshold(context)) {
                        val templates = FaceSDK.templateExtraction(bitmap, faceBox)

                        var maxSimiarlity = 0f
                        var maximiarlityPerson: Person? = null
                        for (person in DBManager.personList) {
                            val similarity =
                                FaceSDK.similarityCalculation(templates, person.templates)
                            if (similarity > maxSimiarlity) {
                                maxSimiarlity = similarity
                                maximiarlityPerson = person
                            }
                        }
                        if (maxSimiarlity > SettingsActivity.getIdentifyThreshold(context)) {
                            recognized = true
                            val identifiedPerson = maximiarlityPerson
                            val identifiedSimilarity = maxSimiarlity

                            runOnUiThread {
                                val faceImage = Utils.cropFace(bitmap, faceBox)
                                val intent = Intent(context, ResultActivity::class.java)
                                intent.putExtra("identified_face", faceImage)
                                intent.putExtra("enrolled_face", identifiedPerson!!.face)
                                intent.putExtra("identified_name", identifiedPerson!!.name)
                                intent.putExtra("similarity", identifiedSimilarity)
                                intent.putExtra("liveness", faceBox.liveness)
                                intent.putExtra("yaw", faceBox.yaw)
                                intent.putExtra("roll", faceBox.roll)
                                intent.putExtra("pitch", faceBox.pitch)
                                startActivity(intent)
                            }
                        }
                    }
                }
                
                else if (faceBoxes.size > 1){
                    Toast.makeText(this@CameraActivityKt, getString(R.string.multiple_face_detected), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
