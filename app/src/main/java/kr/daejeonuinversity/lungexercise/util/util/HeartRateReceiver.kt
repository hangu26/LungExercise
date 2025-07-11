package kr.daejeonuinversity.lungexercise.util.util

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable

class HeartRateReceiver(
    private val context: Context,
    private val onDataReceived: (Map<String, Any>) -> Unit
) : DataClient.OnDataChangedListener {

    fun register() {
        Wearable.getDataClient(context).addListener(this)
            .addOnSuccessListener {
                Log.d("HeartRateReceiver", "✅ 등록 성공")
            }
            .addOnFailureListener {
                Log.e("HeartRateReceiver", "❌ 등록 실패: $it")
            }
    }

    fun unregister() {
        Wearable.getDataClient(context).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                val path = item.uri.path ?: continue
                val dataMap = DataMapItem.fromDataItem(item).dataMap
                val timestamp = dataMap.getLong("timestamp")

                when {
                    path.startsWith("/heart_rate") -> {
                        val heartRate = dataMap.getFloat("heart_rate")
                        onDataReceived(mapOf("type" to "heart_rate", "value" to heartRate, "timestamp" to timestamp))
                    }

                    path.startsWith("/step_count") -> {
                        val steps = dataMap.getInt("step_count")
                        onDataReceived(mapOf("type" to "step_count", "value" to steps, "timestamp" to timestamp))
                    }

                    path.startsWith("/spo2") -> {
                        val spo2 = dataMap.getFloat("spo2")
                        onDataReceived(mapOf("type" to "spo2", "value" to spo2, "timestamp" to timestamp))
                    }

                    else -> {
                        Log.d("HeartRateReceiver", "⚠️ 알 수 없는 path: $path")
                    }
                }
            }
        }
    }
}

