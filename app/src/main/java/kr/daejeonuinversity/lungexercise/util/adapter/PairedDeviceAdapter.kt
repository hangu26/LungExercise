package kr.daejeonuinversity.lungexercise.util.adapter

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
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
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                false
            } else {
                device.name?.contains("MASK") == true
            }
        }

        deviceList.addAll(filteredDevices)
        notifyDataSetChanged()
    }

    inner class DeviceViewHolder(val binding: ItemPairingDeviceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(device: BluetoothDevice) {
            // 권한 체크는 필요하지만, 이름은 항상 넣어야 함
            val deviceName = if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                device.name ?: "알 수 없는 기기"
            } else {
                "권한 없음"
            }

            binding.txDeviceName.text = deviceName

            binding.constraintConnectDevice.setOnClickListener {
                onConnectClick(device)
            }
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


