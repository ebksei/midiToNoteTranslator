package com.leff.midi.examples.translator

import com.leff.midi.MidiFile
import com.leff.midi.event.NoteOff
import com.leff.midi.event.NoteOn
import com.leff.midi.event.meta.TimeSignature

class Translator {

    private val notes = mutableListOf<Note>()
    private val onEvents = mutableListOf<NoteOn>()
    private val offEvents = mutableListOf<NoteOff>()
    private val notePrecision = 16.toFloat()
    private val minFigureDuration = 1 / notePrecision
    private var denominator = 1
    private var resolution = 0

    fun translateMidiFile(mf: MidiFile): Array<Note> {
        denominator = searchTempoBase(mf)
        resolution = mf.resolution
        onEvents.clear()
        offEvents.clear()

        mf.tracks.flatMap { it.events }
                .forEach {
                    when (it) {
                        is NoteOn -> {
                            if (it.velocity != 0) onEvents.add(it) // NoteOn with 0 velocity are the end of a sound
                            else offEvents.add(NoteOff(it.tick, it.delta, it.channel, it.noteValue, it.velocity))
                        }
                        is NoteOff -> offEvents.add(it)
                    }
                }

        for (event in onEvents) {
            addNoteFromEvent(event)
        }

        return notes.toTypedArray()
    }

    private fun searchTempoBase(mf: MidiFile): Int {
        val track = mf.tracks.first { it.events.any { it is TimeSignature } }
        return (track.events.first { it is TimeSignature } as TimeSignature).realDenominator
    }

    fun getTempoBase() = this.transformToFigureDuration(denominator.toFloat())

    private fun transformToFigureDuration(duration: Float) = when (duration) {
        in 1f..Float.MAX_VALUE -> FigureDuration.WHOLE_NOTE
        in (1 / 2f)..1f -> FigureDuration.HALF_NOTE
        in (1 / 4f)..(1 / 2f) -> FigureDuration.QUARTER_NOTE
        in (1 / 8f)..(1 / 4f) -> FigureDuration.EIGHT_NOTE
        else -> FigureDuration.SIXTEENTH_NOTE
    }


    private fun addNoteFromEvent(note: NoteOn) {
        val pitch = note.noteValue % 12
        val octave = note.noteValue / 12 - 1
        val durationToAdd = calcNoteDuration(note)
        val startingPosition = note.tick / resolution.toFloat()
        addNote(pitch, octave, startingPosition, durationToAdd)
    }

    private fun addNote(pitch: Int, octave: Int, startingPosition: Float, duration: Float) {
        val containingDuration = findGreaterDurationContaining(duration)

        when {
            duration >= (containingDuration * 1.5f) -> {
                val remainingDuration = (duration - containingDuration * 1.5f)
                val isLinked = remainingDuration > minFigureDuration
                notes.add(Note(pitch, octave, transformToFigureDuration(duration), startingPosition, true,
                        isLinked))
                if (isLinked) addNote(pitch, octave,
                        (startingPosition + (containingDuration * denominator) * 1.5f),
                        remainingDuration)
            }
            else -> {
                val remainingDuration = (duration - containingDuration)
                val isLinked = remainingDuration > minFigureDuration
                notes.add(Note(pitch, octave, transformToFigureDuration(duration), startingPosition,
                        isLinked = isLinked))
                if (isLinked) addNote(pitch, octave, (startingPosition + containingDuration * denominator),
                        remainingDuration)
            }
        }
    }

    private fun findGreaterDurationContaining(duration: Float): Float = when (duration) {
        in 1f..Float.MAX_VALUE -> 1f
        in (1 / 2f)..1f -> 1 / 2f
        in (1 / 4f)..(1 / 2f) -> 1 / 4f
        in (1 / 8f)..(1 / 4f) -> 1 / 8f
        in (1 / 16f)..(1 / 8f) -> 1 / 16f
        else -> minFigureDuration
    }

    private fun calcNoteDuration(onNote: NoteOn): Float {
        val offNote = offEvents.first {
            it.noteValue == onNote.noteValue
                    && it.tick >= onNote.tick
                    && it.channel == onNote.channel
        }
        return Math.ceil((((offNote.tick - onNote.tick) / resolution.toFloat()) / denominator) * notePrecision.toDouble()).toFloat() / notePrecision
    }

    private fun pitchToString(pitch: Int) = when (pitch) {
        0 -> "C"
        1 -> "C#"
        2 -> "D"
        3 -> "D#"
        4 -> "E"
        5 -> "F"
        6 -> "F#"
        7 -> "G"
        8 -> "G#"
        9 -> "A"
        10 -> "A#"
        11 -> "B"
        else -> ""
    }
}