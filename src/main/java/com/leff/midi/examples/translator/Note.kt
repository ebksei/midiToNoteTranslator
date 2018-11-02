package com.leff.midi.examples.translator

class Note(
        val pitch: Int,
        val height: Int,
        duration: FigureDuration,
        startingPosition:Float,
        isExtended: Boolean = false,
        isLinked: Boolean = false
) : Figure(duration, startingPosition, isExtended, isLinked)