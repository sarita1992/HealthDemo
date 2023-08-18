/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.healthtest.view

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.healthtest.model.MainViewModel
import com.example.healthtest.TAG
import com.example.healthtest.model.UiState
import com.example.healthtest.database.AppDataBase
import com.example.healthtest.databinding.ActivityHeartRateBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Activity displaying the app UI. Notably, this binds data from [MainViewModel] to views on screen,
 * and performs the permission check when enabling passive data.
 */
@AndroidEntryPoint
class HeartRateActivity : AppCompatActivity() {

    var job: Job? = null
    private var userName= ""
    private lateinit var binding: ActivityHeartRateBinding
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    val time = 60000L

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHeartRateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userName= intent?.extras?.getString("NAME")?:"Welcome"
        binding.userName.text=userName
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                when (result) {
                    true -> {
                        Log.i(TAG, "Body sensors permission granted")
                        viewModel.togglePassiveData(true)
                    }

                    false -> {
                        Log.i(TAG, "Body sensors permission not granted")
                        viewModel.togglePassiveData(false)
                    }
                }
            }

        permissionLauncher.launch(android.Manifest.permission.BODY_SENSORS)
        startUpdates()
        // Bind viewmodel state to the UI.
        lifecycleScope.launchWhenStarted {

            viewModel.uiState.collect {
                updateViewVisiblity(it)
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.latestHeartRate.collect {
                binding.lastMeasuredValue.text = it.toString()
            }
        }
        lifecycleScope.launchWhenStarted {
            viewModel.passiveDataEnabled.collect {

            }
        }
    }


    private fun startUpdates() {
        job = CoroutineScope(Dispatchers.Default).launch {
            while (NonCancellable.isActive) {
                // add your task here
                viewModel.latestHeartRateJson.collect {
          val dao  =  AppDataBase.getDatabase(this@HeartRateActivity).userDao()
                    viewModel.insert(it,dao)
                }
                delay(time)
            }
        }
    }


    private fun stopUpdates() {
        job?.cancel()
        job = null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopUpdates()
    }

    private fun updateViewVisiblity(uiState: UiState) {
        // These views are visible when heart rate capability is not available.
        (uiState is UiState.HeartRateNotAvailable).let {
            binding.brokenHeart.isVisible = it
            binding.notAvailable.isVisible = it
        }
        // These views are visible when the capability is available.
        (uiState is UiState.HeartRateAvailable).let {
            binding.lastMeasuredLabel.isVisible = it
            binding.lastMeasuredValue.isVisible = it
            binding.heart.isVisible = it
        }
    }
}
