package com.leff.midi.examples.translator

abstract class Figure(
        val duration: FigureDuration,
        val startingPosiotion: Float,
        val isExtended: Boolean = false,
        val isLinked: Boolean = false
) {

}