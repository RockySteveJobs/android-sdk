package com.qonversion.android.sdk

import com.android.billingclient.api.BillingClient
import com.qonversion.android.sdk.billing.BillingError
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response
import java.io.IOException

fun BillingError.toQonversionError(): QonversionError {
    val errorCode = when (this.billingResponseCode) {
        BillingClient.BillingResponseCode.SERVICE_TIMEOUT,
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED,
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE,
        BillingClient.BillingResponseCode.ERROR -> QonversionErrorCode.PlayStoreError

        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> QonversionErrorCode.FeatureNotSupported
        BillingClient.BillingResponseCode.OK -> QonversionErrorCode.UnknownError
        BillingClient.BillingResponseCode.USER_CANCELED -> QonversionErrorCode.CanceledPurchase

        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> QonversionErrorCode.BillingUnavailable
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> QonversionErrorCode.ProductUnavailable

        BillingClient.BillingResponseCode.DEVELOPER_ERROR -> QonversionErrorCode.PurchaseInvalid
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> QonversionErrorCode.ProductAlreadyOwned
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> QonversionErrorCode.ProductNotOwned
        else -> QonversionErrorCode.UnknownError
    }

    return QonversionError(errorCode, this.message)
}

fun Throwable.toQonversionError(): QonversionError {
    return when (this) {
        is JSONException ->{
            QonversionError(QonversionErrorCode.ParseResponseFailed, localizedMessage ?: "")
        }

        is IOException -> {
            QonversionError( QonversionErrorCode.NetworkConnectionFailed,  localizedMessage ?: "")
        }

        else -> QonversionError(QonversionErrorCode.UnknownError, localizedMessage ?: "")
    }
}

fun <T> Response<T>.toQonversionError(): QonversionError {
    val data = "data"
    val message = "message"
    var errorMessage = ""

    errorBody()?.let {
        val jsonObjError = JSONObject(it.string())
        if (jsonObjError.has(data)) {
            val jsonObjData = jsonObjError.getJSONObject(data)
            errorMessage += if (jsonObjData.has(message)) jsonObjData.getString(message) else ""
        }
    }

    return QonversionError(QonversionErrorCode.BackendError,  "HTTP status code=${this.code()}, errorMessage=$errorMessage")
}