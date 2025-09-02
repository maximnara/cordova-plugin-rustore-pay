package com.maximnara.rustore.pay

import com.maximnara.rustore.pay.helpers.BaseHelper
import org.apache.cordova.CordovaPlugin
import org.apache.cordova.CallbackContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.content.Intent
import android.app.Activity
import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.IntentInteractor
import ru.rustore.sdk.pay.model.DeveloperPayload
import ru.rustore.sdk.pay.model.ProductId
import ru.rustore.sdk.pay.model.PurchaseAvailabilityResult
import ru.rustore.sdk.pay.model.ProductPurchaseParams
import ru.rustore.sdk.pay.model.Quantity
import ru.rustore.sdk.pay.model.UserAuthorizationStatus
import ru.rustore.sdk.core.util.RuStoreUtils
import ru.rustore.sdk.pay.model.AppUserEmail
import ru.rustore.sdk.pay.model.AppUserId
import ru.rustore.sdk.pay.model.ProductType

//import ru.rustore.sdk.pay.model.UserAuthorizationStatus

class RustorePayPlugin : CordovaPlugin() {

    private lateinit var helper: BaseHelper
    companion object {
        private const val TAG = "RustorePayPlugin"
    }


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
                // Add your plugin methods here
                "getPurchaseAvailability" -> {
                    getPurchaseAvailability(args, callbackContext)
                    true
                }
                "getPurchases" -> {
                    getPurchases(args, callbackContext)
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
                "getProducts" -> {
                    getProducts(args, callbackContext)
                    true
                }
                "openRuStoreDownloadInstruction" -> {
                    openRuStoreDownloadInstruction(args, callbackContext)
                    true
                }
                "openRuStore" -> {
                    openRuStore(args, callbackContext)
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

    // Add your plugin methods here

    // Helper method to send events to JavaScript
    private fun sendEvent(eventName: String, data: JSONObject? = null) {
        helper.emitWindowEvent(eventName, data)
    }


    // Helper methods to expose BaseHelper functionality to VKAuthManager
    fun callbackSuccess(
        callbackContext: CallbackContext,
        message: String,
        data: Any? = null
    ) {
        helper.callbackSuccess(callbackContext, message, data)
    }

    fun callbackError(
        callbackContext: CallbackContext,
        message: String,
        error: Any? = null
    ) {
        helper.callbackError(callbackContext, message, error)
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
        try {
            helper.log("getPurchases called")

            val purchaseInteractor = RuStorePayClient.instance.getPurchaseInteractor()

            purchaseInteractor.getPurchases()
                .addOnSuccessListener { purchases ->
                    helper.log("getPurchases success", "Retrieved ${purchases.size} purchases")

                    val purchasesArray = mutableListOf<Map<String, Any?>>()

                    for (purchase in purchases) {
                        val purchaseMap = mapOf(
                            "purchaseId" to purchase.purchaseId,
                            "invoiceId" to purchase.invoiceId,
                            "description" to purchase.description,
                            "purchaseTime" to purchase.purchaseTime,
                            "orderId" to purchase.orderId,
                            "amountLabel" to purchase.amountLabel,
                            "currency" to purchase.currency,
                            "developerPayload" to purchase.developerPayload
                        )
                        purchasesArray.add(purchaseMap)
                    }

                    val data = mapOf("purchases" to purchasesArray)
                    helper.callbackSuccess(callbackContext, "Purchases retrieved successfully", data)
                }
                .addOnFailureListener { throwable ->
                    helper.log("getPurchases error", throwable.message)
                    helper.callbackError(callbackContext, "getPurchases error", throwable.message)
                }

        } catch (e: Exception) {
            helper.log("getPurchases error", e.message)
            helper.callbackError(callbackContext, "getPurchases error", e.message)
        }
    }


    private fun purchase(args: JSONArray, callbackContext: CallbackContext) {
        try {
            helper.log("purchase called")

            // Проверяем наличие параметров
            if (args.length() == 0) {
                helper.log("purchase error", "No parameters provided")
                helper.callbackError(callbackContext, "purchase error", "Parameters are required")
                return
            }

            val params = args.getJSONObject(0)

            // Проверяем productId
            val productId = params.optString("productId")
            if (productId.isNullOrEmpty()) {
                helper.log("purchase error", "productId is required")
                helper.callbackError(callbackContext, "purchase error", "productId parameter is required and must be a non-empty string")
                return
            }

            // Проверяем quantity
            val requestedQuantity = if (params.has("quantity")) {
                val qty = params.optInt("quantity", -1)
                if (qty <= 0) {
                    helper.log("purchase error", "Invalid quantity: $qty")
                    helper.callbackError(callbackContext, "purchase error", "quantity parameter must be a positive integer")
                    return
                }
                qty
            } else {
                1 // значение по умолчанию
            }

            helper.log("purchase", "Product: $productId, Requested Quantity: $requestedQuantity")

            // Получаем информацию о продукте через getProducts
            val productIds = listOf(ProductId(productId))
            RuStorePayClient.instance.getProductInteractor().getProducts(productsId = productIds)
                .addOnSuccessListener { products ->
                    if (products.isEmpty()) {
                        helper.log("purchase error", "Product not found: $productId")
                        helper.callbackError(callbackContext, "purchase error", "Product not found: $productId")
                        return@addOnSuccessListener
                    }

                    val product = products[0]
                    helper.log("purchase", "Product type: ${product.type}, Title: ${product.title}")

                    // Определяем финальное количество в зависимости от типа продукта
                    val finalQuantity = if (product.type == ProductType.NON_CONSUMABLE_PRODUCT || 
                                            product.type == ProductType.SUBSCRIPTION) {
                        helper.log("purchase", "${product.type} product detected, setting quantity to 1")
                        1
                    } else {
                        helper.log("purchase", "CONSUMABLE product detected, using requested quantity: $requestedQuantity")
                        requestedQuantity
                    }

                    helper.log("purchase", "Product: $productId, Type: ${product.type}, Final Quantity: $finalQuantity")

                    // Создаем параметры покупки
                    val purchaseParams = ru.rustore.sdk.pay.model.ProductPurchaseParams(
                        productId = ProductId(productId),
                        quantity = Quantity(finalQuantity),
                    )
                    
                    helper.log("purchase", "Created purchase params for ${product.type}")

                    RuStorePayClient.instance.getPurchaseInteractor().purchase(purchaseParams)
                        .addOnSuccessListener { result ->
                            helper.log("purchase success", "Purchase completed")

                            val data = mapOf(
                                "purchaseId" to result.purchaseId,
                                "productId" to result.productId,
                                "invoiceId" to result.invoiceId
                            )

                            helper.callbackSuccess(callbackContext, "Purchase completed successfully", data)
                        }
                        .addOnFailureListener { throwable ->
                            val errorMessage = throwable.message ?: "Unknown error"
                            helper.log("purchase error", "Purchase failed for ${product.type}: $errorMessage")
                            helper.log("purchase error", "Full error: ${throwable.cause?.toString() ?: throwable.toString()}")
                            
                            val errorData = mapOf(
                                "error" to errorMessage,
                                "productType" to product.type.toString(),
                                "productId" to productId,
                                "cause" to (throwable.cause?.toString() ?: "Unknown cause")
                            )
                            
                            helper.callbackError(callbackContext, "Purchase failed for ${product.type}", errorData)
                        }
                }
                .addOnFailureListener { throwable ->
                    helper.log("purchase error", "Failed to get product info: ${throwable.message}")
                    helper.callbackError(callbackContext, "purchase error", "Failed to get product info: ${throwable.message}")
                }

        } catch (e: Exception) {
            helper.log("purchase error", e.message)
            helper.callbackError(callbackContext, "purchase error", e.message)
        }
    }

    private fun getUserAuthorizationStatus(args: JSONArray, callbackContext: CallbackContext) {
        try {
            helper.log("getUserAuthorizationStatus called")

            RuStorePayClient.instance.getUserInteractor().getUserAuthorizationStatus()
                .addOnSuccessListener { status ->
                    helper.log("getUserAuthorizationStatus success", "Status: $status")

                    val statusData = when (status) {
                        UserAuthorizationStatus.AUTHORIZED -> {
                            mapOf(
                                "isAuthorized" to true,
                                "status" to "authorized"
                            )
                        }
                        UserAuthorizationStatus.UNAUTHORIZED -> {
                            mapOf(
                                "isAuthorized" to false,
                                "status" to "unauthorized"
                            )
                        }
                        else -> {
                            mapOf(
                                "isAuthorized" to false,
                                "status" to "unknown"
                            )
                        }
                    }

                    helper.callbackSuccess(callbackContext, "User authorization status retrieved", statusData)
                }
                .addOnFailureListener { throwable ->
                    helper.log("getUserAuthorizationStatus error", throwable.message)
                    helper.callbackError(callbackContext, "getUserAuthorizationStatus error", throwable.message)
                }

        } catch (e: Exception) {
            helper.log("getUserAuthorizationStatus error", e.message)
            helper.callbackError(callbackContext, "getUserAuthorizationStatus error", e.message)
        }
    }



    private fun getProducts(args: JSONArray, callbackContext: CallbackContext) {
        try {
            helper.log("getProducts called")

            // Проверяем наличие параметров
            if (args.length() == 0) {
                helper.log("getProducts error", "No product IDs provided")
                helper.callbackError(callbackContext, "getProducts error", "Product IDs are required")
                return
            }

            val productIdsArray = args.getJSONArray(0)
            if (productIdsArray.length() == 0) {
                helper.log("getProducts error", "Empty product IDs array")
                helper.callbackError(callbackContext, "getProducts error", "At least one product ID is required")
                return
            }

            val productIds = mutableListOf<ProductId>()
            for (i in 0 until productIdsArray.length()) {
                val productId = productIdsArray.getString(i)
                if (productId.isNotEmpty()) {
                    productIds.add(ProductId(productId))
                }
            }

            if (productIds.isEmpty()) {
                helper.log("getProducts error", "No valid product IDs found")
                helper.callbackError(callbackContext, "getProducts error", "No valid product IDs provided")
                return
            }

            helper.log("getProducts", "Requesting ${productIds.size} products")

            RuStorePayClient.instance.getProductInteractor().getProducts(productsId = productIds)
                .addOnSuccessListener { products ->
                    helper.log("getProducts success", "Retrieved ${products.size} products")

                    val productsArray = mutableListOf<Map<String, Any?>>()

                    for (product in products) {
                        val productMap = mapOf(
                            "productId" to product.productId,
                            "type" to product.type,
                            "amountLabel" to product.amountLabel,
                            "price" to product.price,
                            "currency" to product.currency,
                            "imageUrl" to product.imageUrl,
                            "title" to product.title,
                            "description" to product.description
                        )
                        productsArray.add(productMap)
                    }

                    val data = mapOf("products" to productsArray)
                    helper.callbackSuccess(callbackContext, "Products retrieved successfully", data)
                }
                .addOnFailureListener { throwable ->
                    helper.log("getProducts error", throwable.message)
                    helper.callbackError(callbackContext, "getProducts error", throwable.message)
                }

        } catch (e: Exception) {
            helper.log("getProducts error", e.message)
            helper.callbackError(callbackContext, "getProducts error", e.message)
        }
    }

    private fun openRuStoreDownloadInstruction(args: JSONArray, callbackContext: CallbackContext) {
        try {
            helper.log("openRuStoreDownloadInstruction called")

            // Проверяем, установлен ли RuStore
            val isInstalled = RuStoreUtils.isRuStoreInstalled(this.cordova.activity)
            helper.log("openRuStoreDownloadInstruction", "RuStore installed: $isInstalled")

            if (isInstalled) {
                helper.log("openRuStoreDownloadInstruction", "RuStore is already installed")
                helper.callbackSuccess(callbackContext, "RuStore is already installed on this device")
                return
            }

            // Открываем инструкцию по установке RuStore
            RuStoreUtils.openRuStoreDownloadInstruction(this.cordova.activity)
            helper.log("openRuStoreDownloadInstruction", "RuStore download instruction opened")
            helper.callbackSuccess(callbackContext, "RuStore download instruction opened successfully")

        } catch (e: Exception) {
            helper.log("openRuStoreDownloadInstruction error", e.message)
            helper.callbackError(callbackContext, "openRuStoreDownloadInstruction error", e.message)
        }
    }

    private fun openRuStore(args: JSONArray, callbackContext: CallbackContext) {
        try {
            helper.log("openRuStore called")

            // Проверяем, установлен ли RuStore
            val isInstalled = RuStoreUtils.isRuStoreInstalled(this.cordova.activity)
            helper.log("openRuStore", "RuStore installed: $isInstalled")

            if (!isInstalled) {
                helper.log("openRuStore error", "RuStore is not installed")
                helper.callbackError(callbackContext, "RuStore is not installed on this device")
                return
            }

            // Открываем RuStore
            RuStoreUtils.openRuStore(this.cordova.activity)
            helper.log("openRuStore", "RuStore opened successfully")
            helper.callbackSuccess(callbackContext, "RuStore opened successfully")

        } catch (e: Exception) {
            helper.log("openRuStore error", e.message)
            helper.callbackError(callbackContext, "openRuStore error", e.message)
        }
    }

}