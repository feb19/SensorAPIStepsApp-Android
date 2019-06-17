package jp.feb19.sensorapisteps

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.OnDataPointListener
import com.google.android.gms.fitness.request.SensorRequest
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    companion object {
        private const val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 123
    }
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        connectFitAPI()
    }

    private fun connectFitAPI() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (!GoogleSignIn.hasPermissions(account, fitnessOptions)) {
            Log.i(TAG, "has not Permissions")
            GoogleSignIn.requestPermissions(
                this,
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // この処理が成功した場合に返すリクエストコード
                GoogleSignIn.getLastSignedInAccount(this),
                fitnessOptions
            )
        } else {
            Log.i(TAG, "has Permissions")
            accessSensorAPI()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        Log.i(TAG, "onActivityResult")

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GOOGLE_FIT_PERMISSIONS_REQUEST_CODE) {
                accessSensorAPI()
            }
        }
    }

    private fun accessSensorAPI() {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        val gsa = GoogleSignIn.getAccountForExtension(this, fitnessOptions)

        Log.i(TAG, "detectSensorAPI")
        // センサーにアクセス
        Fitness.getSensorsClient(this, gsa)
            .add(
                SensorRequest.Builder()
                    .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
                    .setSamplingRate(1, TimeUnit.MINUTES)  // １分ごとに一回集計してサンプルにする
                    .build(),
                listener
            )
    }

    private val listener = OnDataPointListener { dataPoint ->
        for (field in dataPoint.dataType.fields) {
            val value = dataPoint.getValue(field)
            Log.i(TAG, "Detected DataPoint field: " + field.name)
            Log.i(TAG, "Detected DataPoint value: $value")

            val s = field.name == Field.FIELD_STEPS.name
            Log.i(TAG, s.toString())

            val t = findViewById<TextView>(R.id.textView)
            t.text = value.toString()
        }
    }
}
