package com.fortunegem.mosaic.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.fortunegem.mosaic.MainActivity
import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.core.di.ViewModelFactory
import com.fortunegem.mosaic.core.ext.ViewExt
import com.fortunegem.mosaic.databinding.FragmentSplashBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null

    private val animator = SplashAnimator()

    private val viewModel: SplashViewModel by viewModels {
        ViewModelFactory(requireContext().applicationContext)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val created = FragmentSplashBinding.inflate(inflater, container, false)
        _binding = created
        return created.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bound = _binding ?: return
        val density = resources.displayMetrics.density
        animator.enterGem(bound.splashGem)
        animator.enterText(bound.splashTitle, 220L, density)
        animator.enterText(bound.splashSubtitle, 380L, density)
        animator.enterText(bound.splashCaption, 520L, density)
        animator.breathe(bound.splashGem)
        animator.haloPulse(bound.splashHalo)
        animator.drift(bound.splashRays)
        animator.twinkle(bound.splashFacetLeft, 0L)
        animator.twinkle(bound.splashFacetRight, 420L)
        animator.twinkle(bound.splashFacetTop, 840L)
        ViewExt.describeState(bound.splashProgress, getString(R.string.splash_caption))
        observeProgress()
        observeHandOff()
    }

    private fun observeProgress() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.polishProgress.collectLatest { value ->
                    renderCaption(value)
                }
            }
        }
    }

    private fun observeHandOff() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.handOffReady.collectLatest { ready ->
                    if (ready) handOff()
                }
            }
        }
    }

    private fun renderCaption(value: Int) {
        val bound = _binding ?: return
        val captionRes = when (viewModel.captionStage()) {
            SplashViewModel.STAGE_CUT -> R.string.splash_stage_cut
            SplashViewModel.STAGE_POLISH -> R.string.splash_caption
            else -> R.string.splash_stage_set
        }
        bound.splashCaption.setText(captionRes)
        ViewExt.describeState(bound.splashProgress, value.toString())
    }

    private fun handOff() {
        if (!isAdded) return
        val host = activity as? MainActivity ?: return
        if (!host.navigator.showMenu()) return
        viewModel.consumeHandOff()
    }

    override fun onDestroyView() {
        val bound = _binding
        animator.release(
            bound?.splashGem,
            bound?.splashTitle,
            bound?.splashSubtitle,
            bound?.splashCaption,
            bound?.splashHalo,
            bound?.splashRays,
            bound?.splashFacetLeft,
            bound?.splashFacetRight,
            bound?.splashFacetTop
        )
        _binding = null
        super.onDestroyView()
    }
}
