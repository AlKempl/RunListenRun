package com.alkempl.rlr.ui

import android.Manifest
import android.content.*
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.alkempl.rlr.R
import com.alkempl.rlr.databinding.FragmentLocationItemListBinding
import com.alkempl.rlr.databinding.FragmentLocationUpdateBinding
import com.alkempl.rlr.databinding.FragmentObstacleItemListBinding
import com.alkempl.rlr.services.*
import com.alkempl.rlr.utils.hasPermission
import com.alkempl.rlr.viewmodel.LocationUpdateViewModel
import com.alkempl.rlr.viewmodel.ObstacleUpdateViewModel
import com.google.android.material.snackbar.Snackbar


private const val TAG = "LUFragment"

class LocationUpdateFragment : Fragment() {

    private var activityListener: Callbacks? = null

    private lateinit var ttsManager: TTSManager
    private lateinit var soundManager: SoundManager
    private lateinit var scenarioManager: ScenarioManager
    private lateinit var actionsManager: ActionsManager

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

        container?.removeAllViews()

        ttsManager = TTSManager.getInstance(this.requireContext())
        soundManager = SoundManager.getInstance(this.requireContext())
        scenarioManager = ScenarioManager.getInstance(this.requireContext())
        actionsManager = ActionsManager.getInstance(this.requireContext())

        /*
        * https://stackoverflow.com/questions/44337896/get-heart-rate-from-android-wear
        * */

        binding = FragmentLocationUpdateBinding.inflate(inflater, container, false)
        bindingLocationItemList =
            FragmentLocationItemListBinding.inflate(inflater, container, false)
        bindingObstacleItemList =
            FragmentObstacleItemListBinding.inflate(inflater, container, false)

        binding.scenarioControlButton.setOnClickListener {
            shutdown()
        }

        binding.chapterProgressCircular.indeterminateMode = true;

        return binding.root
    }


    private fun startScenario(){
        // Bind to LocalService
        Intent(context, ScenarioService::class.java).also { ssintent ->
            requireActivity().bindService(
                ssintent,
                scenarioServiceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
        scenarioServiceBounded = !scenarioServiceBounded
        updateScenarioButtonState()
    }

    private fun shutdown(){
        val scenarioServiceIntent = Intent(context, ScenarioService::class.java)
        Log.d(TAG, "stop com.alkempl.rlr.data.model.scenario.Scenario Service")
        requireActivity().unbindService(scenarioServiceConnection)
        scenarioServiceBounded = !scenarioServiceBounded
        updateScenarioButtonState()
        activityListener?.displayHomeUI()
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
        } else {
            throw RuntimeException("$context must implement PermissionRequestFragment.Callbacks")
        }
    }

    private val onJsonReceived: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null) {
                /* intent.getStringExtra("json")?.let {
                     val data = JSONObject(it)
                 }*/

                when (intent.action){
                    "scenarioShutdownHealth" -> shutdown()
                    "scenarioShutdownChapterEnd" -> shutdown()
                    else -> {}
                }

                val text = when (intent.action){
                    "scenarioShutdownHealth" -> getString(R.string.training_suspense_by_health_text)
                    "scenarioShutdownChapterEnd" -> getString(R.string.training_suspense_by_chapter_end_text)
                    else -> "Что-то пошло не так."
                }

                val title = when (intent.action){
                    "scenarioShutdownHealth" -> getString(R.string.health_protection_title)
                    "scenarioShutdownChapterEnd" -> "Тренировка завершена"
                    else -> "Что-то пошло не так."
                }

                val btnText = when (intent.action){
                    "scenarioShutdownHealth" -> R.string.ok
                    "scenarioShutdownChapterEnd" -> R.string.hooray
                    else -> R.string.ok
                }

                val dialogIcon = when (intent.action){
                    "scenarioShutdownHealth" -> R.drawable.ic_baseline_warning_24
                    "scenarioShutdownChapterEnd" -> R.drawable.ic_baseline_directions_run_24
                    else -> R.drawable.ic_baseline_info_24
                }

                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                alertDialogBuilder.setTitle(title)
                alertDialogBuilder.setIcon(dialogIcon)
                alertDialogBuilder.setMessage(text)
                alertDialogBuilder.setNeutralButton(btnText, { dialogInterface: DialogInterface, i: Int -> })

                val alertDialog = alertDialogBuilder.create()
                alertDialog.show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationUpdateViewModel.progressNow.observe(viewLifecycleOwner
        ) { newProgressNow ->
            if (newProgressNow > 0){
                binding.chapterProgressCircular.progress = newProgressNow.toFloat()
            }
        }

        locationUpdateViewModel.progressMax.observe(viewLifecycleOwner
        ) { newProgressMax ->
            if(newProgressMax > 0){
                binding.chapterProgressCircular.progressMax = newProgressMax.toFloat()
            }
        }

        locationUpdateViewModel.geofenceStatus.observe(viewLifecycleOwner
        ) { newHint ->
            if(newHint.isNotEmpty()){
                binding.geofencingStatusCaption.visibility = View.VISIBLE
                binding.geofencingStatusCaption.text = newHint
            }else{
                binding.geofencingStatusCaption.visibility = View.GONE
            }
        }

        locationUpdateViewModel.currentChapter.observe(viewLifecycleOwner
        ) { newChapter ->
            if(newChapter != null){
                binding.chapterCaption.visibility = View.VISIBLE
                binding.chapterCaption.text = newChapter.name

                binding.chapterDescCaption.visibility = View.VISIBLE
                binding.chapterDescCaption.text = newChapter.description
            }else{
                binding.chapterCaption.visibility = View.GONE
                binding.chapterDescCaption.visibility = View.GONE
            }
        }

        locationUpdateViewModel.obstacleImg.observe(viewLifecycleOwner
        ) { newObstacleImg ->
            if(newObstacleImg.isNotEmpty()){
                binding.obstacleImage.visibility = View.VISIBLE
                val id = resources.getIdentifier(newObstacleImg,
                    "drawable", requireContext().packageName)
                binding.obstacleImage.setImageResource(id)
            }else{
                binding.chapterCaption.visibility = View.GONE
            }
        }

        locationUpdateViewModel.scenarioRunning.observe(viewLifecycleOwner)
        { newStatusIsRunning ->
            binding.chapterProgressCircular.indeterminateMode = !newStatusIsRunning
        }

        startScenario()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as AppCompatActivity).supportActionBar!!.hide()
        updateScenarioButtonState()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as AppCompatActivity).supportActionBar!!.show()
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().unbindService(scenarioServiceConnection)
        bm!!.unregisterReceiver(onJsonReceived)
    }

    private fun showBackgroundButton(): Boolean {
        return !requireContext().hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
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
        fun displayHomeUI()
    }
}