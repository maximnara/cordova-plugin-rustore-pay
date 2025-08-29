package com.maximnara.rustore.pay

import com.maximnara.rustore.pay.helpers.BaseHelper
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CallbackContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import android.content.Context
import android.util.Log
import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.model.PurchaseAvailabilityResult

class RustorePayPlugin : CordovaPlugin() {
    
    private lateinit var helper: BaseHelper

    companion object {
        private const val TAG = "RustorePayPlugin"
    }

    private var isInitialized = false
    
    override fun initialize(cordova: org.apache.cordova.CordovaInterface, webView: org.apache.cordova.CordovaWebView) {
        super.initialize(cordova, webView)
        helper = PluginHelper(this, webView)
    }

    override fun execute(
        action: String,
        args: JSONArray,
        callbackContext: CallbackContext
    ): Boolean {
        return try {
            when (action) {
                "init" -> {
                    init(args, callbackContext)
                    true
                }
                "exampleMethod" -> {
                    exampleMethod(args, callbackContext)
                    true
                }
                // Add your plugin methods here
                "getPurchaseAvailability" -> {
                    getPurchaseAvailability(args, callbackContext)
                    true
                }
                "getPurchases" -> {
                    getPurchases(args, callbackContext)
                    true
                }
                "getPurchase" -> {
                    getPurchase(args, callbackContext)
                    true
                }
                "purchase" -> {
                    purchase(args, callbackContext)
                    true
                }
                "getUserAuthorizationStatus" -> {
                    getUserAuthorizationStatus(args, callbackContext)
                    true
                }
                else -> {
                    Log.w(TAG, "Unknown action: $action")
                    callbackContext.error("Unknown action: $action")
                    false
                }
            }
        } catch (e: JSONException) {
            Log.e(TAG, "JSON Exception: ${e.message}")
            callbackContext.error("JSON Exception: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
            callbackContext.error("Exception: ${e.message}")
            false
        }
    }

    private fun init(args: JSONArray, callbackContext: CallbackContext) {
        try {
            // Get initialization parameters
            val options = if (args.length() > 0) args.getJSONObject(0) else JSONObject()
            
            // Add your initialization logic here
            helper.log("Initializing plugin with options", options.toString())
            
            // Example initialization
            isInitialized = true
            
            helper.callbackSuccess(callbackContext, "Plugin initialized successfully")
            
        } catch (e: Exception) {
            helper.log("Initialization error", e.message)
            helper.callbackError(callbackContext, "Initialization error", e.message)
        }
    }

    private fun exampleMethod(args: JSONArray, callbackContext: CallbackContext) {
        if (!helper.checkInitialized(isInitialized, callbackContext)) {
            return
        }

        try {
            // Get method parameters
            val value = if (args.length() > 0) args.getString(0) else ""
            
            // Add your method logic here
            helper.log("Example method called with value", value)
            
            // Return result
            val data = mapOf(
                "input" to value,
                "timestamp" to System.currentTimeMillis()
            )
            
            helper.callbackSuccess(callbackContext, "Example method executed", data)
            
        } catch (e: Exception) {
            helper.log("Example method error", e.message)
            helper.callbackError(callbackContext, "Example method error", e.message)
        }
    }

    // Add your plugin methods here
    
    // Helper method to send events to JavaScript
    private fun sendEvent(eventName: String, data: JSONObject? = null) {
        helper.emitWindowEvent(eventName, data)
    }
    
    // Inner helper class that extends BaseHelper
    private class PluginHelper(cordovaPlugin: CordovaPlugin, cordovaWebView: org.apache.cordova.CordovaWebView) 
        : BaseHelper(cordovaPlugin, cordovaWebView)

    private fun getPurchaseAvailability(args: JSONArray, callbackContext: CallbackContext) {
        try {
            helper.log("getPurchaseAvailability called")
            
            val purchaseInteractor = RuStorePayClient.instance.getPurchaseInteractor()
            
            purchaseInteractor.getPurchaseAvailability()
                .addOnSuccessListener { result ->
                    when (result) {
                        is PurchaseAvailabilityResult.Available -> {
                            helper.log("Purchase availability check successful", "Available")
                            val data = mapOf(
                                "available" to true,
                                "status" to "available"
                            )
                            helper.callbackSuccess(callbackContext, "Purchase availability check successful", data)
                        }
                        is PurchaseAvailabilityResult.Unavailable -> {
                            helper.log("Purchase availability check failed", result.cause?.message ?: "Unknown error")
                            val data = mapOf(
                                "available" to false,
                                "status" to "unavailable",
                                "error" to (result.cause?.message ?: "Unknown error")
                            )
                            helper.callbackSuccess(callbackContext, "Purchase availability check completed", data)
                        }
                    }
                }
                .addOnFailureListener { throwable ->
                    helper.log("getPurchaseAvailability error", throwable.message)
                    helper.callbackError(callbackContext, "getPurchaseAvailability error", throwable.message)
                }
            
        } catch (e: Exception) {
            helper.log("getPurchaseAvailability error", e.message)
            helper.callbackError(callbackContext, "getPurchaseAvailability error", e.message)
        }
    }

    private fun getPurchases(args: JSONArray, callbackContext: CallbackContext) {
        if (!helper.checkInitialized(isInitialized, callbackContext)) {
            return
        }

        try {
            helper.log("getPurchases called")
            
            // Add your method logic here
            
            helper.callbackSuccess(callbackContext, "getPurchases executed successfully")
            
        } catch (e: Exception) {
            helper.log("getPurchases error", e.message)
            helper.callbackError(callbackContext, "getPurchases error", e.message)
        }
    }

    private fun getPurchase(args: JSONArray, callbackContext: CallbackContext) {
        if (!helper.checkInitialized(isInitialized, callbackContext)) {
            return
        }

        try {
            helper.log("getPurchase called")
            
            // Add your method logic here
            
            helper.callbackSuccess(callbackContext, "getPurchase executed successfully")
            
        } catch (e: Exception) {
            helper.log("getPurchase error", e.message)
            helper.callbackError(callbackContext, "getPurchase error", e.message)
        }
    }

    private fun purchase(args: JSONArray, callbackContext: CallbackContext) {
        if (!helper.checkInitialized(isInitialized, callbackContext)) {
            return
        }

        try {
            helper.log("purchase called")
            
            // Add your method logic here
            
            helper.callbackSuccess(callbackContext, "purchase executed successfully")
            
        } catch (e: Exception) {
            helper.log("purchase error", e.message)
            helper.callbackError(callbackContext, "purchase error", e.message)
        }
    }

    private fun getUserAuthorizationStatus(args: JSONArray, callbackContext: CallbackContext) {
        if (!helper.checkInitialized(isInitialized, callbackContext)) {
            return
        }

        try {
            helper.log("getUserAuthorizationStatus called")
            
            // Add your method logic here
            
            helper.callbackSuccess(callbackContext, "getUserAuthorizationStatus executed successfully")
            
        } catch (e: Exception) {
            helper.log("getUserAuthorizationStatus error", e.message)
            helper.callbackError(callbackContext, "getUserAuthorizationStatus error", e.message)
        }
    }
}