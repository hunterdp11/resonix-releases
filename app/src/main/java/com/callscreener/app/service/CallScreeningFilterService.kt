package com.callscreener.app.service

import android.os.Build
import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class CallScreeningFilterService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: "Unknown"
        Log.d(TAG, "Screening incoming call from: $phoneNumber")

        val blockManager = com.callscreener.app.engine.CallBlockManager.getInstance(applicationContext)
        val blockedContact = blockManager.getBlockedContact(phoneNumber)
        if (blockedContact != null) {
            Log.i(TAG, "BLOCKED CALL $phoneNumber screened at CallScreening layer (Mode: ${blockManager.blockMode.name})")

            val response = CallResponse.Builder()
                .setDisallowCall(false)
                .setRejectCall(false)
                .setSkipCallLog(false)
                .setSkipNotification(true)
                .setSilenceCall(true)
                .build()
            respondToCall(callDetails, response)
            return
        }

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
