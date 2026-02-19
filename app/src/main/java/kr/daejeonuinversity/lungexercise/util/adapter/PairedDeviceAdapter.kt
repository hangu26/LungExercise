package kr.daejeonuinversity.lungexercise.util.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import kr.daejeonuinversity.lungexercise.databinding.ItemPairingDeviceBinding
import kr.daejeonuinversity.lungexercise.util.util.MaskBluetoothManager

class PairedDeviceAdapter(
    private val context: Context,
    private val onConnectClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<PairedDeviceAdapter.DeviceViewHolder>() {

    private val deviceList = mutableListOf<BluetoothDevice>()

    @SuppressLint("NotifyDataSetChanged")
    fun setDevices(devices: List<BluetoothDevice>) {
        deviceList.clear()

        val filteredDevices = devices.filter { device ->
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 이상
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    false
                } else {
                    device.name?.contains("MASK") == true
                }
            } else {
                // Android 11 이하
                device.name?.contains("MASK") == true
            }
        }


        Log.d("페어링된 기기", "총 디바이스 수: ${devices.size}")
        Log.d("페어링된 기기", "필터링 후 디바이스 수: ${filteredDevices.size}")
        filteredDevices.forEach { device ->
            Log.d("페어링된 기기", "디바이스 이름: ${device.name}, 주소: ${device.address}")
        }

        deviceList.addAll(filteredDevices)
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(val binding: ItemPairingDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice) {
            val deviceName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12 이상: BLUETOOTH_CONNECT 필요
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    device.name ?: "알 수 없는 기기"
                } else {
                    "권한 없음"
                }
            } else {
                // Android 11 이하: 권한 체크 없이 바로 이름 사용
                device.name ?: "알 수 없는 기기"
            }

            binding.txDeviceName.text = deviceName

            binding.constraintConnectDevice.setOnClickListener {
                onConnectClick(device)
            }

            // 로그로 확인
            Log.d("PairedDeviceAdapter", "바인딩 디바이스: $deviceName, 주소: ${device.address}")
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val binding = ItemPairingDeviceBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return DeviceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.bind(deviceList[position])
    }

    override fun getItemCount() = deviceList.size
}


