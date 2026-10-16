package com.fortunegem.mosaic.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.di.ServiceLocator
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.core.ui.PressAnimator
import com.fortunegem.mosaic.databinding.DialogSettingsBinding
import com.fortunegem.mosaic.domain.repository.SettingsRepository

class SettingsDialog : DialogFragment() {

    private var _binding: DialogSettingsBinding? = null
    private var settings: SettingsRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FortuneMosaicGem_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogSettingsBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        val repository = ServiceLocator.settingsRepository(requireContext())
        settings = repository
        bound.settingsSoundSwitch.isChecked = repository.soundEnabled()
        bound.settingsVibrationSwitch.isChecked = repository.vibrationEnabled()
        describeSound(repository.soundEnabled())
        describeVibration(repository.vibrationEnabled())
        bound.settingsSoundSwitch.setOnCheckedChangeListener { _, checked ->
            settings?.setSoundEnabled(checked)
            describeSound(checked)
        }
        bound.settingsVibrationSwitch.setOnCheckedChangeListener { _, checked ->
            settings?.setVibrationEnabled(checked)
            describeVibration(checked)
        }
        bound.settingsCloseButton.setOnClickListener {
            PressAnimator.pulse(bound.settingsCloseButton)
            dismissAllowingStateLoss()
        }
    }

    private fun describeSound(enabled: Boolean) {
        val bound = _binding ?: return
        val label = getString(if (enabled) R.string.state_on else R.string.state_off)
        ViewExt.describeState(bound.settingsSoundSwitch, getString(R.string.cd_sound_state, label))
    }

    private fun describeVibration(enabled: Boolean) {
        val bound = _binding ?: return
        val label = getString(if (enabled) R.string.state_on else R.string.state_off)
        ViewExt.describeState(
            bound.settingsVibrationSwitch,
            getString(R.string.cd_vibration_state, label)
        )
    }

    override fun onDestroyView() {
        val bound = _binding
        bound?.settingsSoundSwitch?.setOnCheckedChangeListener(null)
        bound?.settingsVibrationSwitch?.setOnCheckedChangeListener(null)
        settings = null
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "settings_dialog"
    }
}
