package com.dimensiondetector.core

import android.content.Context
import android.content.SharedPreferences
import com.dimensiondetector.ui.screens.MeasurementUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _measurementUnit = MutableStateFlow(loadUnit())
    val measurementUnit: StateFlow<MeasurementUnit> = _measurementUnit.asStateFlow()

    private val _hapticFeedback = MutableStateFlow(prefs.getBoolean(KEY_HAPTIC, true))
    val hapticFeedback: StateFlow<Boolean> = _hapticFeedback.asStateFlow()

    private val _visualGrid = MutableStateFlow(prefs.getBoolean(KEY_VISUAL_GRID, true))
    val visualGrid: StateFlow<Boolean> = _visualGrid.asStateFlow()

    fun setMeasurementUnit(unit: MeasurementUnit) {
        prefs.edit().putString(KEY_UNIT, unit.name).apply()
        _measurementUnit.value = unit
    }

    fun setHapticFeedback(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTIC, enabled).apply()
        _hapticFeedback.value = enabled
    }

    fun setVisualGrid(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VISUAL_GRID, enabled).apply()
        _visualGrid.value = enabled
    }

    private fun loadUnit(): MeasurementUnit {
        val name = prefs.getString(KEY_UNIT, MeasurementUnit.METRIC.name) ?: MeasurementUnit.METRIC.name
        return try {
            MeasurementUnit.valueOf(name)
        } catch (e: Exception) {
            MeasurementUnit.METRIC
        }
    }

    companion object {
        private const val PREFS_NAME = "dimension_detector_settings"
        private const val KEY_UNIT = "pref_measurement_unit"
        private const val KEY_HAPTIC = "pref_haptic_feedback"
        private const val KEY_VISUAL_GRID = "pref_visual_grid"
    }
}
