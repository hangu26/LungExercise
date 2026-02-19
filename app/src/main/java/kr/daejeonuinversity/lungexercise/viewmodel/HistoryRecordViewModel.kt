package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import com.google.android.gms.wearable.Wearable
import kr.daejeonuinversity.lungexercise.data.local.dao.StepIntervalDao
import kr.daejeonuinversity.lungexercise.util.util.StepReceiver
import java.util.Calendar

class HistoryRecordViewModel(private val dao: StepIntervalDao, application: Application) : AndroidViewModel(application) {

    private val _btnBackState = MutableLiveData<Boolean>()
    val btnBackState : LiveData<Boolean> = _btnBackState

    fun btnBack(){
        _btnBackState.value = true
    }

    val stepReceiver = StepReceiver(application, dao) { steps, intervalStart ->
        // intervalStart 구간에 steps가 들어올 때마다 로그
        val cal = Calendar.getInstance().apply { timeInMillis = intervalStart }
        val startStr =
            String.format("%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        val endCal = cal.clone() as Calendar
        endCal.add(Calendar.MINUTE, 30)
        val endStr = String.format(
            "%02d:%02d",
            endCal.get(Calendar.HOUR_OF_DAY),
            endCal.get(Calendar.MINUTE)
        )

        Log.d("StepReceiver", "📱 실시간 로그: $startStr ~ $endStr 걸음수: $steps")
    }

    fun requestStepsFromWatch() {
        val nodeClient = Wearable.getNodeClient(application)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                Wearable.getMessageClient(application)
                    .sendMessage(node.id, "/request_steps", byteArrayOf())
                    .addOnSuccessListener { Log.d("MainActivity", "워치로 요청 전송 성공") }
            }
        }
    }

    fun startReceiving() {
        stepReceiver.register()
    }

    fun stopReceiving() {
        stepReceiver.unregister()
    }

}