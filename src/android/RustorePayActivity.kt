package com.maximnara.rustore.pay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.IntentInteractor
import ru.rustore.sdk.core.util.RuStoreUtils

class RustorePayActivity : Activity() {

    companion object {
        private const val TAG = "RustorePayActivity"
        const val EXTRA_ACTION = "action"
        const val ACTION_AUTHORIZE = "authorize"
        const val ACTION_PAYMENT = "payment"
    }

    private val intentInteractor: IntentInteractor by lazy {
        RuStorePayClient.instance.getIntentInteractor()
    }
    
    private var authorizationStarted = false
    private var isFinishing = false
    private var shouldCheckOnResume = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "RustorePayActivity created")

        val action = intent.getStringExtra(EXTRA_ACTION)
        Log.d(TAG, "Action: $action")

        when (action) {
            ACTION_AUTHORIZE -> {
                Log.d(TAG, "Starting authorization")
                startAuthorization()
            }
            ACTION_PAYMENT -> {
                Log.d(TAG, "Processing payment intent")
                if (savedInstanceState == null) {
                    intentInteractor.proceedIntent(intent)
                }
            }
            else -> {
                Log.d(TAG, "Processing default intent")
                if (savedInstanceState == null) {
                    intentInteractor.proceedIntent(intent)
                }
            }
        }
    }

    private fun startAuthorization() {
        if (authorizationStarted) {
            Log.d(TAG, "Authorization already started, skipping")
            return
        }
        
        authorizationStarted = true
        
        try {
            Log.d(TAG, "Calling RuStoreUtils.openRuStoreAuthorization from Activity context")
            RuStoreUtils.openRuStoreAuthorization(this)
            Log.d(TAG, "Authorization started successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Authorization failed: ${e.message}", e)
            setResult(Activity.RESULT_CANCELED)
            isFinishing = true
            finish()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "Processing new intent")
        intentInteractor.proceedIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "RustorePayActivity resumed, shouldCheckOnResume: $shouldCheckOnResume")

        val action = intent.getStringExtra(EXTRA_ACTION)
        
        if (action == ACTION_AUTHORIZE && shouldCheckOnResume && !isFinishing) {
            Log.d(TAG, "User returned from RuStore, checking authorization status")
            isFinishing = true
            
            // Добавляем небольшую задержку чтобы RuStore успел обновить статус
            handler.postDelayed({
                checkAuthorizationStatus()
            }, 1000)
            
        } else if (action != ACTION_AUTHORIZE) {
            // Для платежей закрываем как обычно
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "RustorePayActivity paused")

        val action = intent.getStringExtra(EXTRA_ACTION)
        
        if (action == ACTION_AUTHORIZE && authorizationStarted) {
            Log.d(TAG, "Activity paused - RuStore probably opened, will check on resume")
            shouldCheckOnResume = true
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "RustorePayActivity stopped")
        // Не проверяем статус в onStop, так как он вызывается слишком рано
    }
    
    private fun checkAuthorizationStatus() {
        RuStorePayClient.instance.getUserInteractor().getUserAuthorizationStatus()
            .addOnSuccessListener { status ->
                Log.d(TAG, "Authorization status check result: $status")
                when (status) {
                    ru.rustore.sdk.pay.model.UserAuthorizationStatus.AUTHORIZED -> {
                        Log.d(TAG, "Authorization successful")
                        setResult(Activity.RESULT_OK)
                    }
                    else -> {
                        Log.d(TAG, "Authorization failed or cancelled")
                        setResult(Activity.RESULT_CANCELED)
                    }
                }
                isFinishing = true
                finish()
            }
            .addOnFailureListener { throwable ->
                Log.e(TAG, "Failed to check auth status: ${throwable.message}")
                setResult(Activity.RESULT_CANCELED)
                isFinishing = true
                finish()
            }
    }
}