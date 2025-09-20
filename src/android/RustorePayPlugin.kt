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
                            helper.callbackSuccessRaw(callbackContext, data)
                        }
                        is PurchaseAvailabilityResult.Unavailable -> {
                            helper.log("Purchase availability check failed", result.cause?.message ?: "Unknown error")
                            val data = mapOf(
                                "available" to false,
                                "status" to "unavailable",
                                "error" to (result.cause?.message ?: "Unknown error")
                            )
                            helper.callbackSuccessRaw(callbackContext, data)
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

            // Parse filter parameters if provided (single values, not arrays)
            var productType: ProductType? = null
            var purchaseStatus: Any? = null // Can be ProductPurchaseStatus or SubscriptionPurchaseStatus

            if (args.length() > 0) {
                val params = args.getJSONObject(0)

                // Parse productType (single value)
                if (params.has("productType")) {
                    val typeString = params.getString("productType")
                    productType = when (typeString) {
                        "CONSUMABLE" -> ProductType.CONSUMABLE_PRODUCT
                        "NON_CONSUMABLE" -> ProductType.NON_CONSUMABLE_PRODUCT
                        "SUBSCRIPTION" -> ProductType.SUBSCRIPTION
                        else -> null
                    }
                }

                // Parse purchaseStatus only if productType is specified
                if (params.has("purchaseStatus")) {
                    if (productType == null) {
                        helper.log("getPurchases warning", "purchaseStatus can only be used when productType is specified")
                        helper.callbackError(callbackContext, "getPurchases error", "purchaseStatus can only be used when productType is specified")
                        return
                    }

                    val statusString = params.getString("purchaseStatus")

                    // Use appropriate status enum based on product type
                    purchaseStatus = when (productType) {
                        ProductType.CONSUMABLE_PRODUCT, ProductType.NON_CONSUMABLE_PRODUCT -> {
                            when (statusString) {
                                "INVOICE_CREATED" -> ru.rustore.sdk.pay.model.ProductPurchaseStatus.INVOICE_CREATED
                                "PAID" -> ru.rustore.sdk.pay.model.ProductPurchaseStatus.PAID
                                "CONFIRMED" -> ru.rustore.sdk.pay.model.ProductPurchaseStatus.CONFIRMED
                                "CANCELLED" -> ru.rustore.sdk.pay.model.ProductPurchaseStatus.CANCELLED
                                "REFUNDED" -> ru.rustore.sdk.pay.model.ProductPurchaseStatus.REFUNDED
                                "REJECTED" -> ru.rustore.sdk.pay.model.ProductPurchaseStatus.REJECTED
                                else -> {
                                    helper.log("getPurchases warning", "Invalid purchaseStatus for product type: $statusString")
                                    null
                                }
                            }
                        }
                        ProductType.SUBSCRIPTION -> {
                            when (statusString) {
                                "INVOICE_CREATED" -> ru.rustore.sdk.pay.model.SubscriptionPurchaseStatus.INVOICE_CREATED
                                "ACTIVE" -> ru.rustore.sdk.pay.model.SubscriptionPurchaseStatus.ACTIVE
                                "CANCELLED" -> ru.rustore.sdk.pay.model.SubscriptionPurchaseStatus.CANCELLED
                                "PAUSED" -> ru.rustore.sdk.pay.model.SubscriptionPurchaseStatus.PAUSED
                                "EXPIRED" -> ru.rustore.sdk.pay.model.SubscriptionPurchaseStatus.EXPIRED
                                else -> {
                                    helper.log("getPurchases warning", "Invalid purchaseStatus for subscription: $statusString")
                                    null
                                }
                            }
                        }
                        else -> null
                    }
                }
            }

            helper.log("getPurchases", "Filters - productType: $productType, purchaseStatus: $purchaseStatus")

            // Call getPurchases with filters
            purchaseInteractor.getPurchases(
                productType = productType,
                purchaseStatus = purchaseStatus as? ru.rustore.sdk.pay.model.PurchaseStatus
            )
                .addOnSuccessListener { purchases ->
                    helper.log("getPurchases success", "Retrieved ${purchases.size} purchases")

                    val purchasesArray = mutableListOf<Map<String, Any?>>()

                    for (purchase in purchases) {
                        // Общие поля для всех типов покупок (из общего интерфейса)
                        val purchaseMap = mutableMapOf<String, Any?>(
                            "purchaseId" to purchase.purchaseId.value,
                            "invoiceId" to purchase.invoiceId.value,
                            "purchaseTime" to purchase.purchaseTime?.time,
                            "orderId" to purchase.orderId?.value,
                            "purchaseType" to purchase.purchaseType.toString(),
                            "description" to purchase.description.value,
                            "amountLabel" to purchase.amountLabel.value,
                            "price" to purchase.price.value,
                            "currency" to purchase.currency.value,
                            "status" to purchase.status.toString(),
                            "developerPayload" to purchase.developerPayload?.value,
                            "sandbox" to purchase.sandbox
                        )

                        // Добавляем специфичные поля в зависимости от типа покупки
                        when (purchase) {
                            is ru.rustore.sdk.pay.model.ProductPurchase -> {
                                // Специфичные поля для обычных продуктов (CONSUMABLE/NON_CONSUMABLE)
                                purchaseMap["productId"] = purchase.productId.value
                                purchaseMap["quantity"] = purchase.quantity.value
                                purchaseMap["productType"] = purchase.productType.toString()
                                purchaseMap["type"] = "PRODUCT" // Для идентификации типа
                            }
                            is ru.rustore.sdk.pay.model.SubscriptionPurchase -> {
                                // Специфичные поля для подписок
                                purchaseMap["productId"] = purchase.productId.value
                                purchaseMap["expirationDate"] = purchase.expirationDate?.time
                                purchaseMap["gracePeriodEnabled"] = purchase.gracePeriodEnabled
                                purchaseMap["type"] = "SUBSCRIPTION" // Для идентификации типа
                            }
                        }

                        purchasesArray.add(purchaseMap)
                    }

                    val data = mapOf("purchases" to purchasesArray)
                    helper.callbackSuccessRaw(callbackContext, data)
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
                                "purchaseId" to result.purchaseId?.value,
                                "productId" to result.productId?.value,
                                "invoiceId" to result.invoiceId?.value
                            )
                            helper.callbackSuccessRaw(callbackContext, data)
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

                    val data = when (status) {
                        UserAuthorizationStatus.AUTHORIZED -> mapOf(
                            "isAuthorized" to true,
                            "status" to "authorized"
                        )
                        UserAuthorizationStatus.UNAUTHORIZED -> mapOf(
                            "isAuthorized" to false,
                            "status" to "unauthorized"
                        )
                        else -> mapOf(
                            "isAuthorized" to false,
                            "status" to "unknown"
                        )
                    }
                    helper.callbackSuccessRaw(callbackContext, data)
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

            // JavaScript отправляет массив productIds напрямую как первый аргумент
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
                            "productId" to product.productId.value,
                            "type" to product.type.toString(),
                            "amountLabel" to product.amountLabel?.value,
                            "price" to product.price?.value,
                            "currency" to product.currency?.value,
                            "imageUrl" to product.imageUrl?.value,
                            "title" to product.title?.value,
                            "description" to product.description?.value
                        )
                        productsArray.add(productMap)
                    }

                    val data = mapOf("products" to productsArray)
                    helper.callbackSuccessRaw(callbackContext, data)
                }
                .addOnFailureListener { throwable ->
                    helper.log("getProducts error", throwable.message)
                    helper.callbackError(callbackContext, "getProducts error", throwable.message)
                }

        } catch (e: Exception) {
            helper.log("getProducts error", "Exception: ${e.javaClass.simpleName}: ${e.message}")
            helper.log("getProducts error", "Stack trace: ${e.stackTrace.take(5).joinToString("\n")}")
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
                val data = mapOf(
                    "message" to "RuStore is already installed on this device",
                    "isInstalled" to true
                )
                helper.callbackSuccessRaw(callbackContext, data)
                return
            }

            // Открываем инструкцию по установке RuStore
            RuStoreUtils.openRuStoreDownloadInstruction(this.cordova.activity)
            helper.log("openRuStoreDownloadInstruction", "RuStore download instruction opened")
            val data = mapOf(
                "message" to "RuStore download instruction opened successfully",
                "opened" to true
            )
            helper.callbackSuccessRaw(callbackContext, data)

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
            val data = mapOf(
                "message" to "RuStore opened successfully",
                "opened" to true
            )
            helper.callbackSuccessRaw(callbackContext, data)

        } catch (e: Exception) {
            helper.log("openRuStore error", e.message)
            helper.callbackError(callbackContext, "openRuStore error", e.message)
        }
    }

}