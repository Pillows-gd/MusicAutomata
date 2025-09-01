package MusicAutomata;

import java.util.LinkedHashMap;

public class Notation {
    
    public LinkedHashMap<String,Double> notes = new LinkedHashMap<>();
    public LinkedHashMap<String, Integer[]> sequences = new LinkedHashMap<>();
    
    public Notation() {
        
        // Notes (4th octave frequencies * 0.25 -> 2th octave frequencies)
        notes.put("A2", 440.00 * 0.25);
        notes.put("A#2", 466.16 * 0.25);
        notes.put("B2", 493.88 * 0.25);
        notes.put("C2", 523.25 * 0.25);
        notes.put("C#2", 554.37 * 0.25);
        notes.put("D2", 587.33 * 0.25);
        notes.put("D#2", 622.25 * 0.25);
        notes.put("E2", 659.26 * 0.25);
        notes.put("F2", 698.46 * 0.25);
        notes.put("F#2", 739.99 * 0.25);
        notes.put("G2", 783.99 * 0.25);
        notes.put("G#2", 830.61 * 0.25);
        
        // Scales
        sequences.put("Majour scale", new Integer[]{ 0, 2, 4, 5, 7, 9, 11 });
        sequences.put("Natural Minor scale", new Integer[]{ 0, 2, 3, 5, 7, 8, 10 });
        sequences.put("Harmonic Minor scale", new Integer[]{ 0, 2, 3, 5, 7, 8, 11 });
        sequences.put("Melodic Minor scale", new Integer[]{ 0, 2, 3, 5, 7, 9, 11 });
        
        // Chords
        sequences.put("Majour chord", new Integer[]{ 0, 4, 7 });
        sequences.put("Minor chord", new Integer[]{ 0, 3, 7 });
        sequences.put("Diminished chord", new Integer[]{ 0, 3, 6 });
        sequences.put("Dominant 7th chord", new Integer[]{ 0, 4, 7, 10});
        sequences.put("Majour 7th chord", new Integer[]{ 0, 4, 7, 11});
        sequences.put("Minor 7th chord", new Integer[]{ 0, 3, 7, 10});
        sequences.put("Half-Diminished 7th chord", new Integer[]{ 0, 3, 6, 10});
        sequences.put("Diminished 7th chord", new Integer[]{ 0, 3, 6, 9});
        
    }
    
}
