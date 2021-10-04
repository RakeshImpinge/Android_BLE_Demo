package com.bledemoapp

import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.*
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bleadvertiseapplication.RecyclerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class MainActivity : AppCompatActivity() {
    private val REQUEST_LOCATION_PERMISSION: Int = 100
    private var mBluetoothLeScanner: BluetoothLeScanner? = null
    private val mDeviceList = ArrayList<String>()
    lateinit var mBluetoothAdapter: BluetoothAdapter
    val SERVICE_UUID = UUID.fromString("10000001-0554-4CCF-A6E4-ADE12325C4F0")
    val CHARACTERSTICS_UUID = UUID.fromString("10000001-0554-4CCF-A6E4-ADE12325C4E1")
    var isScanning: Boolean = false
    private val DEVICE_BUTTON_NOTIFY = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = RecyclerAdapter(mDeviceList)
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        mBluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner


        btn_scan.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                checkPermissions()

            }
        })
    }

    private fun checkPermissions() {


        if ((ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED) &&
            (ContextCompat.checkSelfPermission(
                this@MainActivity,
                android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
                , REQUEST_LOCATION_PERMISSION
            )
        } else {
            checkForBluetooth()
        }


    }

    private fun checkForBluetooth() {
        if ((getSystemService(BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled) {

            checkForLocation()

        } else {
            Toast.makeText(this, getString(R.string.enable_bluetooth), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun checkForLocation() {
        val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            discover()
        } else {
            Toast.makeText(this, getString(R.string.enable_location), Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun discover() {
        llProgressBar.visibility = View.VISIBLE

        isScanning = true
        btn_scan.isEnabled = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val builder = ScanFilter.Builder()
            val filter =
                builder
                    .build()
            val filterList = ArrayList<ScanFilter>()
            filterList.add(filter)
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            mBluetoothLeScanner?.startScan(filterList, scanSettings, mScanCallback)

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) run {
            val scanSettings =
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build()
            mBluetoothLeScanner?.startScan(null, scanSettings, mScanCallback)
        }
    }

    private val mScanCallback: ScanCallback = object : ScanCallback() {
        @RequiresApi(Build.VERSION_CODES.M)
        override fun onScanResult(
            callbackType: Int,
            result: ScanResult?
        ) {
            super.onScanResult(callbackType, result)


//            Log.e("Tag", "device name is" + result?.device!!.name)


            if (result == null || result.device == null) return

             if (result.scanRecord != null && result.scanRecord!!.deviceName == "test") {

                btn_scan.isEnabled = true

                stopScan()
                var device = result.device
                connect(device)

                llProgressBar.visibility = View.GONE

                // val deviceUuid = result.scanRecord!!.serviceUuids[0].uuid.toString()
                val devicename = result.scanRecord!!.deviceName
                if (!mDeviceList.contains(devicename)) {
                    mDeviceList.add(devicename.toString())
                    val adapter = RecyclerAdapter(mDeviceList)
                    recycler_view.adapter = adapter
                }

            }
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            super.onBatchScanResults(results)

        }

        override fun onScanFailed(errorCode: Int) {

            Toast.makeText(
                this@MainActivity,
                "Discovery onScanFailed: $errorCode",
                Toast.LENGTH_SHORT
            ).show()

            super.onScanFailed(errorCode)
        }

    }

    private fun stopScan() {
        if (mBluetoothAdapter != null)
            if (mBluetoothAdapter!!.isEnabled) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    mBluetoothAdapter!!.bluetoothLeScanner.stopScan(mScanCallback)
                    mBluetoothAdapter!!.bluetoothLeScanner.flushPendingScanResults(mScanCallback)
                } else {
                    /*@Suppress("DEPRECATION")
                    mBluetoothAdapter!!.stopLeScan(mScanCallback)*/
                }
            } /*else {
                mBluetoothAdapter!!.enable()
            }*/

    }

    private fun connect(device: BluetoothDevice) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            device.connectGatt(

                applicationContext, false, mBluetoothGattCallback,BluetoothDevice.TRANSPORT_LE
            )
        } else {

            device.connectGatt(applicationContext, false, mBluetoothGattCallback)
        }
    }

    private var mBluetoothGatt: BluetoothGatt? = null

    private val mBluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)


            if (newState == BluetoothGatt.STATE_CONNECTED) {


                Handler(Looper.getMainLooper()).post {

                    btn_status.text = "DEVICE CONNECTED"

                    btn_send.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(v: View?) {
                            if (btn_status.text.equals("DEVICE CONNECTED")) {

                                gatt?.discoverServices()

                            } else {
                                Toast.makeText(
                                    this@MainActivity,
                                    getString(R.string.please_connect_first),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    })


                }

                //  gatt?.discoverServices()

            } else if (newState == BluetoothGatt.STATE_DISCONNECTED) {

                btn_status.text = "DISCONNECTED"

                peformDisconnection()

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(
                        this@MainActivity,
                        "Disconnected",
                        Toast.LENGTH_SHORT
                    ).show()

                }


                }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            mBluetoothGatt = gatt

            writeCharacteristicDescriptor(SERVICE_UUID, CHARACTERSTICS_UUID, DEVICE_BUTTON_NOTIFY, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)


        }


        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            super.onDescriptorWrite(gatt, descriptor, status)
            Log.e("Tag", "On Descriptor write called "+ status)
            writeCharacteristicNotifications(SERVICE_UUID, CHARACTERSTICS_UUID, true)
        }


        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            try {



            } catch (e: Exception) {


            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)

        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            super.onCharacteristicChanged(gatt, characteristic)


            try {
                Log.e("Tag", "Char ${characteristic!!.value[0].toChar()} changed called")
                val str:String= characteristic!!.value[0].toChar().toString()

                recived_txt.text=str

                disconnect()



            }catch (e:java.lang.Exception){

            }

        }


    }



    private fun writeCharacteristicDescriptor(service: UUID,
                                              characteristic: UUID,
                                              descriptor: UUID,
                                              data: ByteArray) {

        try {
            if (mBluetoothGatt == null) return
            Thread(Runnable {
                try {
                    val out = mBluetoothGatt?.getService(service)?.getCharacteristic(characteristic)?.getDescriptor(descriptor)
                    out?.value = data
                    if (mBluetoothGatt!!.writeDescriptor(out)) {
                        Log.e("Tag","Descriptor write successfully")

                    } else {
                        Log.e("Tag","Descriptor write Failed")
                        writeCharacteristicDescriptor(service, characteristic,
                            descriptor, data)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }).start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




    private fun peformDisconnection() {
        if (mBluetoothGatt != null) {
            clearCache(mBluetoothGatt!!)
            mBluetoothGatt?.close()
            mBluetoothGatt = null
        }
    }

    private fun clearCache(bluetoothGatt: BluetoothGatt) {
        try {
            val localMethod = bluetoothGatt.javaClass.getMethod("refresh")
            if (localMethod != null) {
                val result = localMethod.invoke(bluetoothGatt) as Boolean
                if (result) {
                    Log.d("BLE", "Bluetooth refresh cache cleared")
                } else {
                    Log.d("BLE", "Bluetooth refresh cache not cleared; failed to clear;")
                }
            }
        } catch (ignored: Exception) {
            Log.d("BLE", "Bluetooth refresh cache not cleared; method does not exist;")
        }

    }


    private fun writeCharacteristicNotifications(
        service: UUID,
        characteristic: UUID,
        enable: Boolean
    ) {
        try {
            if (mBluetoothGatt == null) return
            Thread(Runnable {
                if (mBluetoothGatt != null) {

                    val out = mBluetoothGatt?.getService(service)!!
                        .getCharacteristic(characteristic)
                    if (mBluetoothGatt != null && mBluetoothGatt!!.setCharacteristicNotification(
                            out,
                            enable
                        )
                    ) {

                        writeCharacteristic(
                            SERVICE_UUID,
                            CHARACTERSTICS_UUID,
                            "a".toByteArray(Charset.forName("UTF-8"))
                        )
                    } else {

                        writeCharacteristicNotifications(
                            service, characteristic,
                            enable
                        )
                    }
                }
            }).start()
        } catch (ignored: Exception) {
        }

    }

    private fun writeCharacteristic(
        service: UUID,
        characteristic: UUID,
        value: ByteArray
    ) {
        try {
            if (mBluetoothGatt == null) return


            val out = mBluetoothGatt!!.getService(service)
                .getCharacteristic(characteristic)
            out.value = value

            if (mBluetoothGatt!!.writeCharacteristic(out)) {

                Log.e("Tag", "Write done")


            } else {
                Log.e("Tag", "Fail to write")

            }
        } catch (e: Exception) {

            e.printStackTrace()
        }

    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkForBluetooth()
        } else {
            Toast.makeText(
                this@MainActivity,
                getString(R.string.permission),
                Toast.LENGTH_SHORT
            ).show()
        }
    }



    fun disconnect() {
        try {

            mBluetoothGatt?.disconnect()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}