package com.musicapp.android.viewmodels

import android.media.audiofx.Equalizer
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class BandLevel(
    val band: Short,
    val centerFreq: Int,
    val minLevel: Short,
    val maxLevel: Short,
    val currentLevel: Short
)

@HiltViewModel
class EqualizerViewModel @Inject constructor() : ViewModel() {

    private var equalizer: Equalizer? = null

    private val _bands = MutableStateFlow<List<BandLevel>>(emptyList())
    val bands: StateFlow<List<BandLevel>> = _bands.asStateFlow()

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _presets = MutableStateFlow<List<String>>(emptyList())
    val presets: StateFlow<List<String>> = _presets.asStateFlow()

    private val _selectedPreset = MutableStateFlow<Short>(-1)
    val selectedPreset: StateFlow<Short> = _selectedPreset.asStateFlow()

    fun initEqualizer(audioSessionId: Int) {
        try {
            equalizer?.release()
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = true
            }
            _enabled.value = true
            loadBands()
            loadPresets()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadBands() {
        val eq = equalizer ?: return
        val numBands = eq.numberOfBands
        val range = eq.bandLevelRange
        val list = mutableListOf<BandLevel>()
        for (i in 0 until numBands) {
            val band = i.toShort()
            list.add(
                BandLevel(
                    band = band,
                    centerFreq = eq.getCenterFreq(band) / 1000,
                    minLevel = range[0],
                    maxLevel = range[1],
                    currentLevel = eq.getBandLevel(band)
                )
            )
        }
        _bands.value = list
    }

    private fun loadPresets() {
        val eq = equalizer ?: return
        val presetList = mutableListOf<String>()
        for (i in 0 until eq.numberOfPresets) {
            presetList.add(eq.getPresetName(i.toShort()))
        }
        _presets.value = presetList
    }

    fun setBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
        loadBands()
    }

    fun usePreset(preset: Short) {
        equalizer?.usePreset(preset)
        _selectedPreset.value = preset
        loadBands()
    }

    fun toggleEnabled() {
        val newState = !_enabled.value
        equalizer?.enabled = newState
        _enabled.value = newState
    }

    override fun onCleared() {
        super.onCleared()
        equalizer?.release()
        equalizer = null
    }
}
