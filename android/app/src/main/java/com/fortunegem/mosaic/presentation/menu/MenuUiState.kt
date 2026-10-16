package com.fortunegem.mosaic.presentation.menu

data class MenuUiState(
    val bestScore: Int,
    val restored: Int,
    val total: Int,
    val level: Int,
    val nextArtwork: Int,
    val chips: List<ArtworkChip>,
    val onboardingPending: Boolean
) {
    companion object {
        fun empty(): MenuUiState = MenuUiState(
            bestScore = 0,
            restored = 0,
            total = 0,
            level = 1,
            nextArtwork = 0,
            chips = emptyList(),
            onboardingPending = false
        )
    }
}
