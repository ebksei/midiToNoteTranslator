package com.leff.midi.examples;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.examples.translator.Translator;

public class MidiManipulation {

    public static void main(String[] args) {
        MidiFile mf = null;
        String pathname = "/Users/alejandro.platero/Downloads/";
        pathname+="twinkle_twinkle.mid";
        //pathname+="happy_birthday.mid";
        File input = new File(pathname);

        try {
            mf = new MidiFile(input);
        } catch (IOException e) {
            System.err.println("Error parsing MIDI file:");
            e.printStackTrace();
            return;
        }

        Translator translator = new Translator();
        translator.translateMidiFile(mf);
    }
}
