package com.alkempl.rlr.ui

import android.Manifest
import android.content.*
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.alkempl.rlr.R
import com.alkempl.rlr.adapter.LocationEntityItemRecyclerViewAdapter
import com.alkempl.rlr.adapter.ObstacleEntityItemRecyclerViewAdapter
import com.alkempl.rlr.databinding.FragmentLocationItemListBinding
import com.alkempl.rlr.databinding.FragmentLocationUpdateBinding
import com.alkempl.rlr.databinding.FragmentObstacleItemListBinding
import com.alkempl.rlr.services.ScenarioService
import com.alkempl.rlr.services.TTSManager
import com.alkempl.rlr.utils.hasPermission
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import com.alkempl.rlr.viewmodel.ObstacleUpdateViewModel
import com.google.android.material.snackbar.Snackbar


private const val TAG = "LUFragment"

class LocationUpdateFragment : Fragment() {

    private var activityListener: Callbacks? = null

    private lateinit var ttsManager: TTSManager
    private lateinit var sensorManager: SensorManager
    private lateinit var heartSensor: Sensor

    private lateinit var binding: FragmentLocationUpdateBinding
    private lateinit var bindingLocationItemList: FragmentLocationItemListBinding
    private lateinit var bindingObstacleItemList: FragmentObstacleItemListBinding

    private var scenarioService: ScenarioService? = null
    private var scenarioServiceBounded = false
    private val scenarioServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as ScenarioService.LocalBinder
            scenarioService = binder.getService()
            scenarioServiceBounded = true
            updateScenarioButtonState()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            scenarioServiceBounded = false
            scenarioService = null
            updateScenarioButtonState()
        }
    }

    private val locationUpdateViewModel by lazy {
        ViewModelProviders.of(this).get(LocationUpdateViewModel::class.java)
    }

    private val obstacleUpdateViewModel by lazy {
        ViewModelProviders.of(this).get(ObstacleUpdateViewModel::class.java)
    }

    private var bm: LocalBroadcastManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        ttsManager = TTSManager.getInstance(this.requireContext())

        /*
        * https://stackoverflow.com/questions/44337896/get-heart-rate-from-android-wear
        * */
        sensorManager = this.requireContext().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        heartSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        sensorManager.registerListener(mSensorEventListener, heartSensor, SensorManager.SENSOR_DELAY_FASTEST);

        binding = FragmentLocationUpdateBinding.inflate(inflater, container, false)
        bindingLocationItemList =
            FragmentLocationItemListBinding.inflate(inflater, container, false)
        bindingObstacleItemList =
            FragmentObstacleItemListBinding.inflate(inflater, container, false)

        binding.enableBackgroundLocationButton.setOnClickListener {
            activityListener?.requestBackgroundLocationPermission()
        }

        binding.scenarioControlButton.setOnClickListener {
            manageScenarioService()
        }

        return binding.root
    }

    private fun manageScenarioService() {
        val scenarioServiceIntent = Intent(context, ScenarioService::class.java)

        if (scenarioServiceBounded) {
            Log.d(TAG, "stop Scenario Service")
            requireActivity().unbindService(scenarioServiceConnection)
//            requireActivity().stopService(scenarioServiceIntent)
        } else {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                Log.d(TAG, "start Scenario ForegroundService")
//                requireActivity().startForegroundService(scenarioServiceIntent)
//            } else {
//                Log.d(TAG, "start Scenario Service")
//                requireActivity().startService(scenarioServiceIntent)
//            }
            // Bind to LocalService
            Intent(context, ScenarioService::class.java).also { ssintent ->
                requireActivity().bindService(
                    ssintent,
                    scenarioServiceConnection,
                    Context.BIND_AUTO_CREATE
                )
            }
        }
        scenarioServiceBounded = !scenarioServiceBounded
        updateScenarioButtonState()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val actionReceiver = IntentFilter()
        bm = LocalBroadcastManager.getInstance(context)
        actionReceiver.addAction("scenarioShutdownHealth")
        actionReceiver.addAction("scenarioShutdownChapterEnd")
        bm!!.registerReceiver(onJsonReceived, actionReceiver)

        if (context is Callbacks) {
            activityListener = context

            // If fine location permission isn't approved, instructs the parent Activity to replace
            // this fragment with the permission request fragment.
            if (!context.hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                activityListener?.requestFineLocationPermission()
            }
        } else {
            throw RuntimeException("$context must implement LocationUpdateFragment.Callbacks")
        }
    }

    private val onJsonReceived: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                /* intent.getStringExtra("json")?.let {
                     val data = JSONObject(it)
                 }*/

                manageScenarioService()

                val text = when (intent.action){
                    "scenarioShutdownHealth" -> getString(R.string.training_suspense_by_health_text)
                    "scenarioShutdownChapterEnd" -> getString(R.string.training_suspense_by_chapter_end_text)
                    else -> "Что-то пошло не так."
                }

                val btnText = when (intent.action){
                    "scenarioShutdownHealth" -> R.string.ok
                    "scenarioShutdownChapterEnd" -> R.string.hooray
                    else -> R.string.ok
                }

                val sb = Snackbar.make(
                    binding.root,
                    text,
                    Snackbar.LENGTH_INDEFINITE
                )

                sb.setAction(btnText) {
                    sb.dismiss()
                }

                val snbid = com.google.android.material.R.id.snackbar_text
                sb.view.findViewById<TextView>(snbid).maxLines = 5
                sb.show()
            }
        }
    }
    private val mSensorEventListener: SensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        override fun onSensorChanged(event: SensorEvent) {
//            mStub.setOnLayoutInflatedListener(object : OnLayoutInflatedListener() {
//                fun onLayoutInflated(stub: WatchViewStub) {
//                    mTextView = stub.findViewById(R.id.text) as TextView
//                    mTextView.setText(java.lang.Float.toString(event.values[0]))
//                }
//            })
            Log.d("$TAG/HeartSensor", "Value: ${event.values[0]}")
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.locationListLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { locations ->
                locations?.let {
                    val det: RecyclerView =
                        binding.fragmentLocationContainerView.findViewById(R.id.locations_list)
                    with(det.adapter!!) {
                        if (this is LocationEntityItemRecyclerViewAdapter) {
                            updateUserList(locations)
                        }
                    }
                }
            }
        )

        obstacleUpdateViewModel.obstacleListLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { obstacles ->
                obstacles?.let {
                    val det: RecyclerView =
                        binding.fragmentObstaclesContainerView.findViewById(R.id.obstacles_list)
                    with(det.adapter!!) {
                        if (this is ObstacleEntityItemRecyclerViewAdapter) {
                            updateUserList(obstacles)
                        }
                    }
                }
            }
        )


        /*locationUpdateViewModel.locationListLiveData.observe(
            viewLifecycleOwner,
            androidx.lifecycle.Observer { locations ->
                locations?.let {
                    Log.d(TAG, "Got ${locations.size} locations")

                    if (locations.isEmpty()) {
                        binding.locationOutputTextView.text =
                            getString(R.string.emptyLocationDatabaseMessage)
                    } else {
                        val outputStringBuilder = StringBuilder("")
                        for (location in locations) {
                            outputStringBuilder.append(location.toString() + "\n")
                        }

                        binding.locationOutputTextView.text = outputStringBuilder.toString()
                        *//* bindingRecyclerViewAdapter.list.adapter.updateUserList(locations) *//*
                    }
                }
            }
        )*/
    }

    override fun onResume() {
        super.onResume()
        updateBackgroundButtonState()
        updateScenarioButtonState()
    }

    override fun onDetach() {
        super.onDetach()
        activityListener = null
        requireActivity().unbindService(scenarioServiceConnection)
        bm!!.unregisterReceiver(onJsonReceived)
    }

    private fun showBackgroundButton(): Boolean {
        return !requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }

    private fun updateBackgroundButtonState() {
        if (showBackgroundButton()) {
            binding.enableBackgroundLocationButton.visibility = View.VISIBLE
        } else {
            binding.enableBackgroundLocationButton.visibility = View.GONE
        }
    }

    private fun updateScenarioButtonState() {
        if (showBackgroundButton()) {
            binding.scenarioControlButton.visibility = View.GONE
        } else {
            binding.scenarioControlButton.visibility = View.VISIBLE

            if (scenarioServiceBounded) {
                binding.scenarioControlButton.setBackgroundColor(Color.RED)
                binding.scenarioControlButton.text =
                    getString(R.string.scenario_stop_button_caption)
            } else {
                binding.scenarioControlButton.setBackgroundColor(Color.GREEN)
                binding.scenarioControlButton.text =
                    getString(R.string.scenario_start_button_caption)
            }
        }
    }


    companion object {
        fun newInstance() = LocationUpdateFragment()
    }

    interface Callbacks {
        fun requestFineLocationPermission()
        fun requestBackgroundLocationPermission()
    }
}