package com.maximnara.rustore.pay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.IntentInteractor

class RustorePayActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RustorePayActivity"
    }

    private val intentInteractor: IntentInteractor by lazy {
        RuStorePayClient.instance.getIntentInteractor()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "RustorePayActivity created")

        if (savedInstanceState == null) {
            Log.d(TAG, "Processing initial intent")
            intentInteractor.proceedIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d(TAG, "Processing new intent")
        intentInteractor.proceedIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "RustorePayActivity resumed")

        // Закрываем Activity после обработки результата
        finish()
    }
}