package com.fortunegem.mosaic.data.sample

import com.fortunegem.mosaic.R
import com.fortunegem.mosaic.domain.model.Artwork
import com.fortunegem.mosaic.domain.model.GemPalette

object SampleData {

    val amethyst = GemPalette(
        base = 0xFF6D5CE7.toInt(),
        light = 0xFF9B8CF5.toInt(),
        dark = 0xFF4A3C96.toInt(),
        spark = 0xFFF4B942.toInt()
    )

    val aquamarine = GemPalette(
        base = 0xFF00B4D8.toInt(),
        light = 0xFF5BD6EF.toInt(),
        dark = 0xFF0077A6.toInt(),
        spark = 0xFFFFFFFF.toInt()
    )

    val citrine = GemPalette(
        base = 0xFFF4B942.toInt(),
        light = 0xFFFFD98A.toInt(),
        dark = 0xFFC28A1E.toInt(),
        spark = 0xFFFFFFFF.toInt()
    )

    val roseQuartz = GemPalette(
        base = 0xFFE0607E.toInt(),
        light = 0xFFF295AC.toInt(),
        dark = 0xFFA93C57.toInt(),
        spark = 0xFFFFE4EC.toInt()
    )

    val emerald = GemPalette(
        base = 0xFF3FC9A3.toInt(),
        light = 0xFF86E3C7.toInt(),
        dark = 0xFF21906F.toInt(),
        spark = 0xFFFFFFFF.toInt()
    )

    val prism = GemPalette(
        base = 0xFF8E7BF0.toInt(),
        light = 0xFFC9BEF5.toInt(),
        dark = 0xFF5B4BB8.toInt(),
        spark = 0xFF00B4D8.toInt()
    )

    val artworks: List<Artwork> = listOf(
        Artwork(
            index = 0,
            nameRes = R.string.artwork_amethyst,
            gemRes = R.drawable.sprite_gem_violet,
            palette = amethyst,
            carving = listOf(
                listOf(2, 3, 8, 9),
                listOf(7, 13),
                listOf(10, 16),
                listOf(12, 18, 19),
                listOf(14, 15, 20, 21),
                listOf(17, 22, 23)
            ),
            parScore = 900
        ),
        Artwork(
            index = 1,
            nameRes = R.string.artwork_aqua,
            gemRes = R.drawable.sprite_gem_cyan,
            palette = aquamarine,
            carving = listOf(
                listOf(1, 2, 7, 8),
                listOf(3, 4, 9, 10),
                listOf(6, 12, 13, 19),
                listOf(11, 16, 17, 22),
                listOf(14, 15, 20, 21)
            ),
            parScore = 800
        ),
        Artwork(
            index = 2,
            nameRes = R.string.artwork_amber,
            gemRes = R.drawable.sprite_gem_amber,
            palette = citrine,
            carving = listOf(
                listOf(2, 3, 8, 9),
                listOf(6, 7, 12, 13),
                listOf(10, 11, 16, 17),
                listOf(14, 15, 20, 21),
                listOf(18, 19),
                listOf(22, 23),
                listOf(26, 27)
            ),
            parScore = 1050
        ),
        Artwork(
            index = 3,
            nameRes = R.string.artwork_rose,
            gemRes = R.drawable.sprite_gem_rose,
            palette = roseQuartz,
            carving = listOf(
                listOf(2, 3, 8, 9),
                listOf(7, 12, 13, 18),
                listOf(10, 16, 17, 23),
                listOf(14, 15, 20, 21),
                listOf(19, 24, 25),
                listOf(22, 28, 29),
                listOf(26, 27)
            ),
            parScore = 1050
        ),
        Artwork(
            index = 4,
            nameRes = R.string.artwork_mint,
            gemRes = R.drawable.sprite_gem_mint,
            palette = emerald,
            carving = listOf(
                listOf(0, 1, 6, 7),
                listOf(4, 5, 10, 11),
                listOf(8, 9, 14, 15),
                listOf(12, 13, 18, 19),
                listOf(16, 17, 22, 23),
                listOf(20, 21),
                listOf(24, 25),
                listOf(28, 29)
            ),
            parScore = 1180
        ),
        Artwork(
            index = 5,
            nameRes = R.string.artwork_fortune,
            gemRes = R.drawable.sprite_gem_prism,
            palette = prism,
            carving = listOf(
                listOf(1, 2, 7, 8),
                listOf(3, 4, 9, 10),
                listOf(6, 12, 13, 19),
                listOf(11, 16, 17, 22),
                listOf(14, 15, 20, 21),
                listOf(18, 24, 25),
                listOf(23, 28, 29),
                listOf(26, 27, 32, 33)
            ),
            parScore = 1200
        )
    )
}
