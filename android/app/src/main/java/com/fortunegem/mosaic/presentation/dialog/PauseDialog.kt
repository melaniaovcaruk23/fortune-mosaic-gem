package com.fortunegem.mosaic.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.ui.PressAnimator
import com.fortunegem.mosaic.databinding.DialogPauseBinding

class PauseDialog : DialogFragment() {

    private var _binding: DialogPauseBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FortuneMosaicGem_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogPauseBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        bound.pauseResumeButton.setOnClickListener {
            PressAnimator.pulse(bound.pauseResumeButton)
            report(CHOICE_RESUME)
        }
        bound.pauseRestartButton.setOnClickListener {
            PressAnimator.pulse(bound.pauseRestartButton)
            report(CHOICE_RESTART)
        }
        bound.pauseMenuButton.setOnClickListener {
            PressAnimator.pulse(bound.pauseMenuButton)
            report(CHOICE_MENU)
        }
    }

    private fun report(choice: String) {
        val payload = Bundle()
        payload.putString(RESULT_CHOICE, choice)
        setFragmentResult(REQUEST_KEY, payload)
        dismissAllowingStateLoss()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "pause_dialog"
        const val REQUEST_KEY = "pause_request"
        const val RESULT_CHOICE = "pause_choice"
        const val CHOICE_RESUME = "resume"
        const val CHOICE_RESTART = "restart"
        const val CHOICE_MENU = "menu"
    }
}
