package kr.daejeonuinversity.lungexercise.util.util

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.local.dao.StepIntervalDao
import kr.daejeonuinversity.lungexercise.data.local.entity.StepIntervalEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class StepReceiver(
    private val context: Context,
    private val dao: StepIntervalDao,
    private val onStepReceived: (steps: Int, timestamp: Long) -> Unit
) : DataClient.OnDataChangedListener {

    fun register() {
        Wearable.getDataClient(context).addListener(this)
            .addOnSuccessListener {
                Log.d("StepReceiver", "✅ 등록 성공")
                fetchExistingData()
            }
            .addOnFailureListener { Log.e("StepReceiver", "❌ 등록 실패: $it") }
    }

    fun unregister() {
        Wearable.getDataClient(context).removeListener(this)
    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        for (event in dataEvents) {
            if (event.type != DataEvent.TYPE_CHANGED) continue
            val item = event.dataItem
            if (item.uri.path != "/step_interval") continue

            val dataMap = DataMapItem.fromDataItem(item).dataMap
            val steps = dataMap.getInt("steps")
            val intervalStart = dataMap.getLong("intervalStart")

            saveToRoom(intervalStart, steps)
            onStepReceived(steps, intervalStart)
            logStep(steps, intervalStart)
        }
    }

    private fun fetchExistingData() {
        Wearable.getDataClient(context)
            .getDataItems()
            .addOnSuccessListener { dataItems ->
                for (item in dataItems) {
                    if (item.uri.path != "/step_interval") continue
                    val dataMap = DataMapItem.fromDataItem(item).dataMap
                    val steps = dataMap.getInt("steps")
                    val intervalStart = dataMap.getLong("intervalStart")

                    saveToRoom(intervalStart, steps)
                    onStepReceived(steps, intervalStart)
                    logStep(steps, intervalStart)
                }
            }
    }

    private fun saveToRoom(intervalStart: Long, steps: Int) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(intervalStart)
        val entity = StepIntervalEntity(intervalStart, dateKey, steps)
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertOrUpdate(entity) // Insert or Replace
        }
    }

    private fun logStep(steps: Int, timestamp: Long) {
        val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
        val startTimeStr = String.format("%04d-%02d-%02d %02d:%02d",
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH)+1,
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE)
        )
        val endCal = cal.clone() as Calendar
        endCal.add(Calendar.MINUTE, 30)
        val endTimeStr = String.format("%02d:%02d",
            endCal.get(Calendar.HOUR_OF_DAY),
            endCal.get(Calendar.MINUTE)
        )
        Log.d("StepReceiver", "📥 $startTimeStr 부터 $endTimeStr 까지 걸음수: $steps")
    }
}






