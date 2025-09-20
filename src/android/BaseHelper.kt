package com.maximnara.rustore.pay.helpers

import android.util.Log
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CordovaWebView
import org.json.JSONObject
import org.json.JSONArray
import com.google.gson.Gson

internal abstract class BaseHelper(
    protected val cordovaPlugin: CordovaPlugin,
    protected val cordovaWebView: CordovaWebView
) {
    protected val cordova = cordovaPlugin.cordova
    private val gson = Gson()

    companion object {
        private const val FIRE_WINDOW_EVENT = "javascript:cordova.fireWindowEvent('%s');"
        private const val FIRE_WINDOW_EVENT_WITH_DATA = "javascript:cordova.fireWindowEvent('%s', %s);"
        protected const val TAG = "RustorePayPlugin"
    }

    /**
     * Emits a window event to JavaScript
     * @param event Event name
     * @param data Optional data to send with the event
     * @param logEventDescription Optional description for logging
     */
    fun emitWindowEvent(
        event: String, 
        data: JSONObject? = null, 
        logEventDescription: String? = null
    ) {
        log(event, logEventDescription)

        cordovaPlugin.cordova.activity.runOnUiThread {
            val jsCode = if (data != null) {
                String.format(FIRE_WINDOW_EVENT_WITH_DATA, event, data.toString())
            } else {
                String.format(FIRE_WINDOW_EVENT, event)
            }
            cordovaWebView.loadUrl(jsCode)
        }
    }

    /**
     * Logs an event with optional description
     * @param event Event name
     * @param logEventDescription Optional description
     */
    fun log(event: String, logEventDescription: String? = null) {
        val logEvent = if (logEventDescription != null) {
            "$event: $logEventDescription"
        } else {
            event
        }
        Log.d(TAG, logEvent)
    }

    /**
     * Helper method to create success response
     * @param message Success message
     * @param data Optional data to include
     */
    fun createSuccessResult(message: String, data: Any? = null): JSONObject {
        val result = JSONObject()
        result.put("success", true)
        result.put("message", message)
        if (data != null) {
            // Convert data to JSONObject/JSONArray for proper JSON structure
            val jsonData = when (data) {
                is JSONObject -> data
                is JSONArray -> data
                is String -> {
                    try {
                        JSONObject(data)
                    } catch (e: Exception) {
                        try {
                            JSONArray(data)
                        } catch (e2: Exception) {
                            data // Keep as string if not valid JSON
                        }
                    }
                }
                else -> {
                    // Convert object to JSON string then parse back to JSONObject/JSONArray
                    val jsonString = gson.toJson(data)
                    try {
                        JSONObject(jsonString)
                    } catch (e: Exception) {
                        try {
                            JSONArray(jsonString)
                        } catch (e2: Exception) {
                            jsonString // Fallback to string
                        }
                    }
                }
            }
            result.put("data", jsonData)
        }
        return result
    }

    /**
     * Helper method to create error response
     * @param message Error message
     * @param error Optional error details
     */
    fun createErrorResult(message: String, error: Any? = null): JSONObject {
        val result = JSONObject()
        result.put("success", false)
        result.put("message", message)
        if (error != null) {
            // Convert error to JSONObject/JSONArray for proper JSON structure
            val jsonError = when (error) {
                is JSONObject -> error
                is JSONArray -> error
                is String -> {
                    try {
                        JSONObject(error)
                    } catch (e: Exception) {
                        try {
                            JSONArray(error)
                        } catch (e2: Exception) {
                            error // Keep as string if not valid JSON
                        }
                    }
                }
                else -> {
                    // Convert object to JSON string then parse back to JSONObject/JSONArray
                    val jsonString = gson.toJson(error)
                    try {
                        JSONObject(jsonString)
                    } catch (e: Exception) {
                        try {
                            JSONArray(jsonString)
                        } catch (e2: Exception) {
                            jsonString // Fallback to string
                        }
                    }
                }
            }
            result.put("error", jsonError)
        }
        return result
    }

    /**
     * Helper method to safely execute callback with success result
     * @param callbackContext Cordova callback context
     * @param message Success message
     * @param data Optional data
     */
    fun callbackSuccess(
        callbackContext: CallbackContext,
        message: String,
        data: Any? = null
    ) {
        try {
            val result = createSuccessResult(message, data)
            callbackContext.success(result)
        } catch (e: Exception) {
            log("callbackSuccess error", e.message)
            callbackContext.error("Callback error: ${e.message}")
        }
    }

    /**
     * Helper method to return raw data without wrapper
     * @param callbackContext Cordova callback context
     * @param data Data to return
     */
    fun callbackSuccessRaw(
        callbackContext: CallbackContext,
        data: Any
    ) {
        try {
            when (data) {
                is Map<*, *> -> {
                    val jsonObject = JSONObject()
                    for ((key, value) in data) {
                        when (value) {
                            null -> jsonObject.put(key.toString(), JSONObject.NULL)
                            is List<*> -> {
                                val jsonArray = JSONArray()
                                for (item in value) {
                                    when (item) {
                                        is Map<*, *> -> {
                                            val itemJson = JSONObject()
                                            for ((itemKey, itemValue) in item) {
                                                when (itemValue) {
                                                    null -> itemJson.put(itemKey.toString(), JSONObject.NULL)
                                                    else -> itemJson.put(itemKey.toString(), itemValue)
                                                }
                                            }
                                            jsonArray.put(itemJson)
                                        }
                                        else -> jsonArray.put(item)
                                    }
                                }
                                jsonObject.put(key.toString(), jsonArray)
                            }
                            else -> jsonObject.put(key.toString(), value)
                        }
                    }
                    callbackContext.success(jsonObject)
                }
                is String -> callbackContext.success(data)
                is Boolean -> {
                    val jsonObject = JSONObject()
                    jsonObject.put("success", data)
                    callbackContext.success(jsonObject)
                }
                is Int -> {
                    val jsonObject = JSONObject()
                    jsonObject.put("success", data)
                    callbackContext.success(jsonObject)
                }
                is Number -> {
                    val jsonObject = JSONObject()
                    jsonObject.put("success", data)
                    callbackContext.success(jsonObject)
                }
                else -> {
                    val jsonString = gson.toJson(data)
                    val jsonObject = JSONObject(jsonString)
                    callbackContext.success(jsonObject)
                }
            }
        } catch (e: Exception) {
            log("callbackSuccessRaw error", e.message)
            callbackContext.error("Callback error: ${e.message}")
        }
    }

    /**
     * Helper method to safely execute callback with error result
     * @param callbackContext Cordova callback context
     * @param message Error message
     * @param error Optional error details
     */
    fun callbackError(
        callbackContext: CallbackContext,
        message: String,
        error: Any? = null
    ) {
        try {
            val result = createErrorResult(message, error)
            callbackContext.error(result)
        } catch (e: Exception) {
            log("callbackError error", e.message)
            callbackContext.error("Callback error: ${e.message}")
        }
    }

    /**
     * Helper method to check if plugin is initialized
     * @param isInitialized Current initialization state
     * @param callbackContext Callback context for error response
     * @return true if initialized, false otherwise
     */
    fun checkInitialized(
        isInitialized: Boolean,
        callbackContext: CallbackContext
    ): Boolean {
        if (!isInitialized) {
            callbackError(callbackContext, "Plugin not initialized")
            return false
        }
        return true
    }
}