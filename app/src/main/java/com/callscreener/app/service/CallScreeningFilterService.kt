package com.callscreener.app.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallScreeningFilterService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: "Unknown"
        Log.d(TAG, "Screening incoming call from: $phoneNumber")

        // Build default screening response (allows call to ring, InCallService takes over)
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }

    companion object {
        private const val TAG = "CallScreenFilter"
    }
}

