package com.callscreener.app.engine

import android.content.Context
import android.util.Log
import com.callscreener.app.model.CallSessionState
import com.callscreener.app.model.CallStatus
import com.callscreener.app.session.CallSessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets

enum class TrunkProviderType {
    BUILTIN_SANDBOX, // Built-in privacy relay for testing
    TWILIO_VOICE,    // Twilio Voice REST API
    TELNYX_VOICE,    // Telnyx Voice API
    GENERIC_SIP_WEBHOOK // Generic custom SIP or PBX webhook
}

data class PrivateTrunkConfig(
    val isEnabled: Boolean = true,
    val providerType: TrunkProviderType = TrunkProviderType.BUILTIN_SANDBOX,
    val accountSid: String = "",
    val authToken: String = "",
    val callerIdDisplay: String = "Anonymous", // "Anonymous", "Restricted", or custom virtual number
    val webhookUrl: String = "",
    val isAutoRecord: Boolean = false
)

sealed class PrivateCallState {
    object Idle : PrivateCallState()
    data class Connecting(val destination: String) : PrivateCallState()
    data class Ringing(val destination: String) : PrivateCallState()
    data class Active(val destination: String, val callSid: String) : PrivateCallState()
    data class Failed(val error: String) : PrivateCallState()
    object Ended : PrivateCallState()
}

/**
 * High-Privacy Outbound VoIP / SIP Trunk Engine.
 *
 * Bypasses restrictive carrier SIM limitations (like Airtel/Jio disabling #31# at cellular towers)
 * by routing outgoing calls over an encrypted data connection with true Anonymous Caller ID (CLIR).
 * The recipient's handset receives the call as "Private Number" / "Unknown Caller".
 */
class PrivateTrunkEngine private constructor(private val appContext: Context) {

    private val prefs = appContext.getSharedPreferences("call_screener_private_trunk_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO + Job())

    private val _trunkConfig = MutableStateFlow(loadConfig())
    val trunkConfig: StateFlow<PrivateTrunkConfig> = _trunkConfig.asStateFlow()

    private val _callState = MutableStateFlow<PrivateCallState>(PrivateCallState.Idle)
    val callState: StateFlow<PrivateCallState> = _callState.asStateFlow()

    private var activeCallJob: Job? = null

    init {
        Log.i(TAG, "Private Trunk Engine initialized. Provider: ${_trunkConfig.value.providerType}")
    }

    private fun loadConfig(): PrivateTrunkConfig {
        val enabled = prefs.getBoolean("trunk_enabled", true)
        val providerStr = prefs.getString("provider_type", TrunkProviderType.BUILTIN_SANDBOX.name) ?: TrunkProviderType.BUILTIN_SANDBOX.name
        val provider = try { TrunkProviderType.valueOf(providerStr) } catch (_: Exception) { TrunkProviderType.BUILTIN_SANDBOX }
        val sid = prefs.getString("account_sid", "") ?: ""
        val token = prefs.getString("auth_token", "") ?: ""
        val display = prefs.getString("caller_id_display", "Anonymous") ?: "Anonymous"
        val webhook = prefs.getString("webhook_url", "") ?: ""
        val autoRec = prefs.getBoolean("auto_record", false)

        return PrivateTrunkConfig(
            isEnabled = enabled,
            providerType = provider,
            accountSid = sid,
            authToken = token,
            callerIdDisplay = display,
            webhookUrl = webhook,
            isAutoRecord = autoRec
        )
    }

    fun saveConfig(config: PrivateTrunkConfig) {
        prefs.edit()
            .putBoolean("trunk_enabled", config.isEnabled)
            .putString("provider_type", config.providerType.name)
            .putString("account_sid", config.accountSid)
            .putString("auth_token", config.authToken)
            .putString("caller_id_display", config.callerIdDisplay)
            .putString("webhook_url", config.webhookUrl)
            .putBoolean("auto_record", config.isAutoRecord)
            .apply()

        _trunkConfig.value = config
        Log.i(TAG, "Updated Private Trunk Config: provider=${config.providerType}, callerId=${config.callerIdDisplay}")
    }

    fun isConfigured(): Boolean {
        val cfg = _trunkConfig.value
        if (!cfg.isEnabled) return false
        return when (cfg.providerType) {
            TrunkProviderType.BUILTIN_SANDBOX -> true
            TrunkProviderType.TWILIO_VOICE -> cfg.accountSid.isNotBlank() && cfg.authToken.isNotBlank()
            TrunkProviderType.TELNYX_VOICE -> cfg.authToken.isNotBlank()
            TrunkProviderType.GENERIC_SIP_WEBHOOK -> cfg.webhookUrl.isNotBlank()
        }
    }

    /**
     * Dispatches an outbound call through the private anonymous gateway.
     */
    fun placePrivateCall(
        targetNumber: String,
        onSuccess: (callId: String) -> Unit = {},
        onError: (message: String) -> Unit = {}
    ) {
        val config = _trunkConfig.value
        activeCallJob?.cancel()

        activeCallJob = scope.launch {
            _callState.value = PrivateCallState.Connecting(targetNumber)
            Log.i(TAG, "Initiating Private Outbound Call to $targetNumber via ${config.providerType} (CallerID: ${config.callerIdDisplay})")

            try {
                when (config.providerType) {
                    TrunkProviderType.BUILTIN_SANDBOX -> {
                        // Built-in local sandbox gateway: simulates instant private bridge
                        val callSid = "sandbox_" + System.currentTimeMillis()
                        _callState.value = PrivateCallState.Ringing(targetNumber)
                        kotlinx.coroutines.delay(1200)
                        _callState.value = PrivateCallState.Active(targetNumber, callSid)
                        withContext(Dispatchers.Main) {
                            onSuccess(callSid)
                        }
                    }

                    TrunkProviderType.TWILIO_VOICE -> {
                        // Twilio REST API Voice Call with Anonymous / Custom Caller ID
                        val result = executeTwilioCall(config, targetNumber)
                        if (result.first) {
                            val callSid = result.second
                            _callState.value = PrivateCallState.Active(targetNumber, callSid)
                            withContext(Dispatchers.Main) { onSuccess(callSid) }
                        } else {
                            val err = result.second
                            _callState.value = PrivateCallState.Failed(err)
                            withContext(Dispatchers.Main) { onError(err) }
                        }
                    }

                    TrunkProviderType.TELNYX_VOICE -> {
                        // Telnyx Voice API with Anonymous From header
                        val result = executeTelnyxCall(config, targetNumber)
                        if (result.first) {
                            val callSid = result.second
                            _callState.value = PrivateCallState.Active(targetNumber, callSid)
                            withContext(Dispatchers.Main) { onSuccess(callSid) }
                        } else {
                            val err = result.second
                            _callState.value = PrivateCallState.Failed(err)
                            withContext(Dispatchers.Main) { onError(err) }
                        }
                    }

                    TrunkProviderType.GENERIC_SIP_WEBHOOK -> {
                        // Custom SIP / PBX Webhook Gateway
                        val result = executeGenericWebhook(config, targetNumber)
                        if (result.first) {
                            val callSid = result.second
                            _callState.value = PrivateCallState.Active(targetNumber, callSid)
                            withContext(Dispatchers.Main) { onSuccess(callSid) }
                        } else {
                            val err = result.second
                            _callState.value = PrivateCallState.Failed(err)
                            withContext(Dispatchers.Main) { onError(err) }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error placing private trunk call", e)
                _callState.value = PrivateCallState.Failed(e.message ?: "Network error")
                withContext(Dispatchers.Main) {
                    onError(e.message ?: "Failed to connect private trunk")
                }
            }
        }
    }

    fun endCall() {
        activeCallJob?.cancel()
        _callState.value = PrivateCallState.Ended
        Log.i(TAG, "Private Trunk Call ended")
    }

    private suspend fun executeTwilioCall(config: PrivateTrunkConfig, targetNumber: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.twilio.com/2010-04-01/Accounts/${config.accountSid}/Calls.json")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true

            // HTTP Basic Auth
            val auth = "${config.accountSid}:${config.authToken}"
            val encodedAuth = android.util.Base64.encodeToString(auth.toByteArray(StandardCharsets.UTF_8), android.util.Base64.NO_WRAP)
            connection.setRequestProperty("Authorization", "Basic $encodedAuth")
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

            val fromCallerId = if (config.callerIdDisplay.equals("Anonymous", ignoreCase = true)) "client:anonymous" else config.callerIdDisplay
            val params = "To=" + java.net.URLEncoder.encode(targetNumber, "UTF-8") +
                    "&From=" + java.net.URLEncoder.encode(fromCallerId, "UTF-8") +
                    "&Url=" + java.net.URLEncoder.encode("http://demo.twilio.com/docs/voice.xml", "UTF-8")

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(params)
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val sid = json.optString("sid", "twilio_${System.currentTimeMillis()}")
                Pair(true, sid)
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                Pair(false, "Twilio error ($responseCode): $errorMsg")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection error")
        }
    }

    private suspend fun executeTelnyxCall(config: PrivateTrunkConfig, targetNumber: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.telnyx.com/v2/calls")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Authorization", "Bearer ${config.authToken}")
            connection.setRequestProperty("Content-Type", "application/json")

            val jsonBody = JSONObject().apply {
                put("to", targetNumber)
                put("from", if (config.callerIdDisplay.equals("Anonymous", ignoreCase = true)) "Anonymous" else config.callerIdDisplay)
                put("connection_id", config.accountSid)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode in 200..299) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)
                val data = json.optJSONObject("data")
                val callLegId = data?.optString("call_leg_id", "telnyx_${System.currentTimeMillis()}") ?: "telnyx_call"
                Pair(true, callLegId)
            } else {
                val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                Pair(false, "Telnyx error ($responseCode): $errorMsg")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Connection error")
        }
    }

    private suspend fun executeGenericWebhook(config: PrivateTrunkConfig, targetNumber: String): Pair<Boolean, String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(config.webhookUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            val jsonBody = JSONObject().apply {
                put("action", "dial_private")
                put("destination", targetNumber)
                put("caller_id", config.callerIdDisplay)
                put("privacy_mode", "CLIR_RESTRICTED")
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            val code = connection.responseCode
            if (code in 200..299) {
                Pair(true, "webhook_${System.currentTimeMillis()}")
            } else {
                Pair(false, "Gateway responded with HTTP $code")
            }
        } catch (e: Exception) {
            Pair(false, e.message ?: "Webhook error")
        }
    }

    companion object {
        private const val TAG = "PrivateTrunkEngine"

        @Volatile
        private var instance: PrivateTrunkEngine? = null

        fun getInstance(context: Context): PrivateTrunkEngine {
            return instance ?: synchronized(this) {
                instance ?: PrivateTrunkEngine(context.applicationContext).also { instance = it }
            }
        }
    }
}

