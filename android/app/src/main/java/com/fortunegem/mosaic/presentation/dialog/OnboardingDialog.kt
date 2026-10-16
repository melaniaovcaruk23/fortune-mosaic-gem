package com.fortunegem.mosaic.presentation.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.config.GameConfig
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.core.ui.PressAnimator
import com.fortunegem.mosaic.databinding.DialogOnboardingBinding

class OnboardingDialog : DialogFragment() {

    private var _binding: DialogOnboardingBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FortuneMosaicGem_Dialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = DialogOnboardingBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        ViewExt.enterStaggered(bound.onboardingLineOne, 0, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.onboardingLineTwo, 1, GameConfig.STAGGER_MS)
        ViewExt.enterStaggered(bound.onboardingLineThree, 2, GameConfig.STAGGER_MS)
        bound.onboardingConfirmButton.setOnClickListener {
            PressAnimator.pulse(bound.onboardingConfirmButton)
            dismissAllowingStateLoss()
        }
        ViewExt.describeState(bound.onboardingHeader, getString(R.string.onboarding_title))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "onboarding_dialog"
    }
}
