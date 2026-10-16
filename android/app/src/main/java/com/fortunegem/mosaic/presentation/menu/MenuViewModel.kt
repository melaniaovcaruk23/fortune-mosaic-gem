package com.fortunegem.mosaic.presentation.menu

import androidx.lifecycle.ViewModel
import com.fortunegem.mosaic.domain.repository.ArtworkRepository
import com.fortunegem.mosaic.domain.repository.ProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuViewModel(
    private val progress: ProgressRepository,
    private val artworks: ArtworkRepository
) : ViewModel() {

    private val state = MutableStateFlow(MenuUiState.empty())
    val uiState: StateFlow<MenuUiState> = state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        val all = artworks.artworks()
        val unlocked = progress.unlockedArtwork()
        val restored = progress.restoredCount(all.size)
        val chips = all.map { artwork ->
            ArtworkChip(
                index = artwork.index,
                nameRes = artwork.nameRes,
                gemRes = artwork.gemRes,
                unlocked = artwork.index <= unlocked,
                stars = progress.starsFor(artwork.index)
            )
        }
        state.value = MenuUiState(
            bestScore = progress.bestScore(),
            restored = restored,
            total = all.size,
            level = restored + 1,
            nextArtwork = if (unlocked >= all.size) all.size - 1 else unlocked,
            chips = chips,
            onboardingPending = !progress.onboardingSeen()
        )
    }

    fun markOnboardingSeen() {
        progress.markOnboardingSeen()
        state.value = state.value.copy(onboardingPending = false)
    }

    fun selectArtwork(index: Int): Int {
        val current = state.value
        if (index < 0 || index >= current.chips.size) return current.nextArtwork
        val chip = current.chips[index]
        if (!chip.unlocked) return current.nextArtwork
        state.value = current.copy(nextArtwork = index)
        return index
    }
}
