package kr.daejeonuinversity.lungexercise.util.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.util.Log
import com.khmelenko.lab.miband.MiBand

class MiBandReceiver(private val context: Context) {

    private var miBand: MiBand = MiBand(context)

    @SuppressLint("CheckResult")
    fun scanMiBand() {

        miBand.startScan().subscribe() { result ->

            val device = result.device

            connectMiBand(device)

        }

    }


    @SuppressLint("CheckResult")
    fun connectMiBand(device: BluetoothDevice) {

        miBand.connect(device).subscribe { connected ->
            if (connected) {
                Log.e("미밴드", "연결 성공, ${device.name.toString()}")

                // 연결 성공 후 페어링 시도
                miBand.pair().subscribe({
                    Log.e("미밴드", "페어링 성공")
                }, { error ->
                    // 에러 상세 로그 출력
                    Log.e("미밴드", "페어링 실패: ${error.localizedMessage}")
                    error.printStackTrace()  // 추가적인 스택 트레이스를 출력
                })

            } else {
                Log.e("미밴드", "연결 실패")
            }
        }


    }
}

