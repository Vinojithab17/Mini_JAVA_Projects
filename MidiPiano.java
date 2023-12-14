
//190650B
//libraries
import java.util.HashMap;
import java.util.Map;
import javax.sound.midi.*;
import java.awt.event.*;
import javax.sound.midi.MidiUnavailableException;
import java.applet.*;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;
import midi.*;
import music.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.sound.midi.MidiUnavailableException;
import midi.Midi;
import music.MusicMachine;
import music.NoteEvent;
import music.Pitch;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.sound.midi.MidiUnavailableException;
import music.NoteEvent;

/*=========================================================================*/
/**
 * Instrument represents a musical instrument.
 *
 * These instruments are the 128 standard General
 * MIDI Level 1 instruments. See
 * http://www.midi.org/about-midi/gm/gm1sound.shtml.
 */
public enum Instrument {
    // Order is important in this enumeration,
    // because an instrument's position must
    // corresponds to its MIDI program number.
    PIANO,
    BRIGHT_PIANO,
    ELECTRIC_GRAND,
    HONKY_TONK_PIANO,
    ELECTRIC_PIANO_1,
    ELECTRIC_PIANO_2,
    HARPSICHORD,
    CLAVINET,
    CELESTA,
    GLOCKENSPIEL,
    MUSIC_BOX,
    VIBRAPHONE,
    MARIMBA,
    XYLOPHONE,
    TUBULAR_BELL,
    DULCIMER,
    HAMMOND_ORGAN,
    PERC_ORGAN,
    ROCK_ORGAN,
    CHURCH_ORGAN,
    REED_ORGAN,
    ACCORDION,
    HARMONICA,
    TANGO_ACCORDION,
    NYLON_STR_GUITAR,
    STEEL_STRING_GUITAR,
    JAZZ_ELECTRIC_GTR,
    CLEAN_GUITAR,
    MUTED_GUITAR,
    OVERDRIVE_GUITAR,
    DISTORTION_GUITAR,
    GUITAR_HARMONICS,
    ACOUSTIC_BASS,
    FINGERED_BASS,
    PICKED_BASS,
    FRETLESS_BASS,
    SLAP_BASS_1,
    SLAP_BASS_2,
    SYN_BASS_1,
    SYN_BASS_2,
    VIOLIN,
    VIOLA,
    CELLO,
    CONTRABASS,
    TREMOLO_STRINGS,
    PIZZICATO_STRINGS,
    ORCHESTRAL_HARP,
    TIMPANI,
    ENSEMBLE_STRINGS,
    SLOW_STRINGS,
    SYNTH_STRINGS_1,
    SYNTH_STRINGS_2,
    CHOIR_AAHS,
    VOICE_OOHS,
    SYN_CHOIR,
    ORCHESTRA_HIT,
    TRUMPET,
    TROMBONE,
    TUBA,
    MUTED_TRUMPET,
    FRENCH_HORN,
    BRASS_ENSEMBLE,
    SYN_BRASS_1,
    SYN_BRASS_2,
    SOPRANO_SAX,
    ALTO_SAX,
    TENOR_SAX,
    BARITONE_SAX,
    OBOE,
    ENGLISH_HORN,
    BASSOON,
    CLARINET,
    PICCOLO,
    FLUTE,
    RECORDER,
    PAN_FLUTE,
    BOTTLE_BLOW,
    SHAKUHACHI,
    WHISTLE,
    OCARINA,
    SYN_SQUARE_WAVE,
    SYN_SAW_WAVE,
    SYN_CALLIOPE,
    SYN_CHIFF,
    SYN_CHARANG,
    SYN_VOICE,
    SYN_FIFTHS_SAW,
    SYN_BRASS_AND_LEAD,
    FANTASIA,
    WARM_PAD,
    POLYSYNTH,
    SPACE_VOX,
    BOWED_GLASS,
    METAL_PAD,
    HALO_PAD,
    SWEEP_PAD,
    ICE_RAIN,
    SOUNDTRACK,
    CRYSTAL,
    ATMOSPHERE,
    BRIGHTNESS,
    GOBLINS,
    ECHO_DROPS,
    SCI_FI,
    SITAR,
    BANJO,
    SHAMISEN,
    KOTO,
    KALIMBA,
    BAG_PIPE,
    FIDDLE,
    SHANAI,
    TINKLE_BELL,
    AGOGO,
    STEEL_DRUMS,
    WOODBLOCK,
    TAIKO_DRUM,
    MELODIC_TOM,
    SYN_DRUM,
    REVERSE_CYMBAL,
    GUITAR_FRET_NOISE,
    BREATH_NOISE,
    SEASHORE,
    BIRD,
    TELEPHONE,
    HELICOPTER,
    APPLAUSE,
    GUNSHOT;

    /**
     * @return the next instrument in the standard ordering (or the first
     *         in the ordering if this is the last)
     */
    public Instrument next() {
        for (Instrument i : Instrument.values()) {
            if (i.ordinal() == (ordinal() + 1) % 128)
                return i;
        }
        return this;
    }
}

/* ========================================================================= */
/**
 * Midi represents a MIDI synthesis device.
 */
public class Midi {
    private Synthesizer synthesizer;
    public static Instrument DEFAULT_INSTRUMENT = Instrument.PIANO;

    // active MIDI channels, assigned to instruments
    private final Map<midi.Instrument, MidiChannel> channels = new HashMap<midi.Instrument, MidiChannel>();

    // next available channel number (unassigned to an instrument yet)
    private int nextChannel = 0;

    // volume -- a percentage?
    private static final int VELOCITY = 100;

    private void checkRep() {
        assert synthesizer != null;
        assert channels != null;
        assert nextChannel >= 0;
    }

    /**
     * Make a Midi.
     * 
     * @throws MidiUnavailableException if MIDI is not available
     */
    public Midi() throws MidiUnavailableException {
        synthesizer = MidiSystem.getSynthesizer();
        synthesizer.open();
        synthesizer.loadAllInstruments(synthesizer.getDefaultSoundbank());
        checkRep();
    }

    /**
     * Play a note on the Midi scale for a duration in milliseconds
     * using a specified instrument.
     * 
     * @requires 0 <= note < 256, duration >= 0, instr != null
     */
    public void play(int note, int duration, midi.Instrument instr) {
        MidiChannel channel = getChannel(instr);
        synchronized (channel) {
            channel.noteOn(note, VELOCITY);
        }
        wait(duration);
        synchronized (channel) {
            channel.noteOff(note);
        }
    }

    /**
     * Start playing a note on the Midi scale
     * using a specified instrument.
     * 
     * @requires 0 <= note < 256, instr != null
     */
    public void beginNote(int note, midi.Instrument instr) {
        MidiChannel channel = getChannel(instr);
        synchronized (channel) {
            channel.noteOn(note, VELOCITY);
        }
    }

    public void beginNote(int note) {
        beginNote(note, DEFAULT_INSTRUMENT);
    }

    /**
     * Stop playing a note on the Midi scale
     * using a specified instrument.
     * 
     * @requires 0 <= note < 256, instr != null
     */
    public void endNote(int note, midi.Instrument instr) {
        MidiChannel channel = getChannel(instr);
        synchronized (channel) {
            channel.noteOff(note, VELOCITY);
        }
    }

    public void endNote(int note) {
        endNote(note, DEFAULT_INSTRUMENT);
    }

    /**
     * Wait for a duration in milliseconds.
     * 
     * @requires duration >= 0
     */
    public static void wait(int duration) {
        long now = System.currentTimeMillis();
        long end = now + duration;
        while (now < end) {
            try {
                Thread.sleep((int) (end - now));
            } catch (InterruptedException e) {
            }
            now = System.currentTimeMillis();
        }
    }

    private MidiChannel getChannel(midi.Instrument instr) {
        synchronized (channels) {
            // check whether this instrument already has a channel
            MidiChannel channel = channels.get(instr);
            if (channel != null)
                return channel;

            channel = allocateChannel();
            patchInstrumentIntoChannel(channel, instr);
            channels.put(instr, channel);
            checkRep();
            return channel;
        }
    }

 private MidiChannel allocateChannel() {
 MidiChannel[] channels = synthesizer.getChannels();
 if (nextChannel >= channels.length) throw new RuntimeException("tried to use too 
many instruments: limited to " + channels.length);
 MidiChannel channel = channels[nextChannel];
 // quick hack by DNJ to allow more instruments to be used
 nextChannel = (nextChannel + 1) % channels.length;
 return channel;
 }

    private void patchInstrumentIntoChannel(MidiChannel channel, midi.Instrument instr) {
        channel.programChange(0, instr.ordinal());
    }

    /**
     * Change the default playing instrument
     */
    public void changeDefaultInstrument() {
        DEFAULT_INSTRUMENT = DEFAULT_INSTRUMENT.next();
    }

    /**
     * Change back to default instrument
     */
    public void changeInstrumentPiano() {
        DEFAULT_INSTRUMENT = Instrument.PIANO;
    }

}

/* ========================================================================= */
/**
 * Pitch represents the frequency of a musical note.
 * Standard music notation represents pitches by letters: A, B, C, ..., G.
 * Pitches can be sharp or flat, or whole octaves up or down from these
 * primitive
 * generators.
 * For example:
 * new Pitch('C') makes middle C.
 * new Pitch('C').transpose(1) makes C-sharp.
 * new Pitch('E').transpose(-1) makes E-flat.
 * new Pitch('C').transpose(OCTAVE) makes high C.
 * new Pitch('C').transpose(-OCTAVE) makes low C.
 */
public class Pitch {
    private final int value;
    // Rep invariant: true.
    // Abstraction function AF(value):
    // AF(0),...,AF(12) map to middle C, C-sharp, D, ..., A, A-sharp, B.
    // AF(i+12n) maps to n octaves above middle AF(i)
    // AF(i-12n) maps to n octaves below middle AF(i)

    private static final int[] scale = {
            /* A */ 9,
            /* B */ 11,
            /* C */ 0,
            /* D */ 2,
            /* E */ 4,
            /* F */ 5,
            /* G */ 7,
    };

    // middle C in the Pitch data type, used to
    // map pitches to Midi frequency numbers.
    private final static Pitch C = new Pitch('C');

    public Pitch(int value) {
        this.value = value;
    }

    /**
     * Make a Pitch.
     * 
     * @param c
     * @requires c in {'A',...,'G'}
     * @returns Pitch named c in the middle octave of the piano keyboard.
     *          For example, new Pitch('C') constructs middle C
     */
    public Pitch(char c) {
        try {
            value = scale[c - 'A'];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException(c + " must be in the range A-G");
        }
    }

    /**
     * Number of pitches in an octave.
     */
    public static final int OCTAVE = 12;

    /**
     * @param semitonesUp
     * @return pitch made by transposing this pitch by semitonesUp semitones.
     *         For example, middle C transposed by 12 semitones is high C;
     *         E transposed by -1 semitones is E flat.
     */
    public Pitch transpose(int semitonesUp) {
        return new Pitch(value + semitonesUp);
    }

    /**
     * @param that
     * @return number of semitones between this and that; i.e.,
     *         n such that that.transpose(n).equals(this).
     */
    public int difference(Pitch that) {
        return this.value - that.value;
    }

    /**
     * @param that
     * @return true iff this pitch is lower than that pitch
     */
    public boolean lessThan(Pitch that) {
        return this.difference(that) < 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass())
            return false;
        Pitch that = (Pitch) obj;
        return this.value == that.value;
    }

    @Override
    public int hashCode() {
        return value;
    }

    public int toMidiFrequency() {
        return difference(C) + 60;
    }

    public Pitch getPitch() {
        return this;
    }

    /**
     * @return this pitch in abc music notation
     *         (see http://www.walshaw.plus.com/abc/examples/)
     */
    @Override
    public String toString() {
        String suffix = "";
        int v = value;

        while (v < 0) {
            suffix += ",";
            v += 12;
        }

        while (v >= 12) {
            suffix += "'";
            v -= 12;
        }

        return valToString[v] + suffix;
    }

    private static final String[] valToString = {
            "C", "^C", "D", "^D", "E", "F", "^F", "G", "G^", "A", "^A", "B"
    };
}

/* ========================================================================= */
// abstract class for handling the note events
public abstract class NoteEvent {
    // can only used by itself and subclasses
    protected final Pitch pitch;
    protected final long delay; // time delay of key press

    public NoteEvent(Pitch pitch) {
        this(pitch, 0);
    }

    public NoteEvent(Pitch pitch, long delay) {
        this.delay = delay;
        this.pitch = pitch;
    }

    // abtract methods
    abstract public NoteEvent delayed(long delay);

    abstract public void execute(MusicMachine m);

    abstract public long getDelay();

    abstract public Pitch getPitch();
}

/* ========================================================================= */
public class BeginNote extends NoteEvent {
    private Pitch pitch;

    public BeginNote(Pitch pitch) {
        this(pitch, 0);
    }

    public BeginNote(Pitch pitch, long delay) {
        super(pitch, delay);
        this.pitch = pitch;
    }

    @Override
    public void execute(MusicMachine m) {
        m.beginNote(this);
    }

    @Override
    public BeginNote delayed(long delay) {
        return new BeginNote(pitch, delay);
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public Pitch getPitch() {
        return pitch;
    }

    @Override
    public String toString() {
        return (pitch.toString());
    }
}

/* ========================================================================= */
public class EndNote extends NoteEvent {
    private Pitch pitch;

    public EndNote(Pitch pitch) {
        this(pitch, 0);
    }

    public EndNote(Pitch pitch, long delay) {
        super(pitch, delay);
        this.pitch = pitch;
    }

    @Override
    public EndNote delayed(long delay) {
        return new EndNote(pitch, delay);
    }

    @Override
    public void execute(MusicMachine m) {
        m.endNote(this);
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public Pitch getPitch() {
        return pitch;
    }

    @Override
    public String toString() {
        return (pitch.toString());
    }
}

/* ========================================================================= */
// connects PianoMachine and NoteEvents
public interface MusicMachine {
    public void beginNote(NoteEvent event);

    public void endNote(NoteEvent event);
}

/* ========================================================================= */
/*
 * <applet code="PianoApplet" width=500 height=500> </applet>
 */
public class PianoApplet extends Applet {
    private static PianoPlayer player;
    private final Set<Character> availableNoteKeys = new HashSet<>();

    @Override
    public void init() {

        final Midi midi;
        setBackground(Color.GREEN);

        /*
         * keys
         * 
         * 1 --> C, 2 --> C-Sharp
         * 3 --> D, 4 --> D-Sharp
         * 5 --> E
         * 6 --> F, 7 --> F-Sharp
         * 8 --> G, 9 --> G-Sharp
         * 0 --> A, '-' --> A-Sharp
         * '=' --> B
         */

        availableNoteKeys.add('1');
        availableNoteKeys.add('2');
        availableNoteKeys.add('3');
        availableNoteKeys.add('4');
        availableNoteKeys.add('5');
        availableNoteKeys.add('6');
        availableNoteKeys.add('7');
        availableNoteKeys.add('8');
        availableNoteKeys.add('9');
        availableNoteKeys.add('0');
        availableNoteKeys.add('-');
        availableNoteKeys.add('=');

        try {
            player = new PianoPlayer();
        } catch (MidiUnavailableException e) {
            return;
        } /* Add a KeyAdapter to the keylisteners to check the key presses */
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                char key = (char) e.getKeyCode();
                switch (key) {
                    case 'S':
                        player.changeInstrumentPiano();
                        return;
                    case 'I':
                        player.changeInstrument();
                        return;
                    case 'P':
                        if (player.isRecording())
                            System.out.println("Plying while recording!");
                        player.requestPlayback();
                        return;
                    case 'R':
                        if (!player.isRecording())
                            setBackground(Color.RED);
                        else
                            setBackground(Color.GREEN);

                        player.toggleRecording();
                        return;
                }
                if (availableNoteKeys.contains(key) &&
                        !player.isRecordingPlaying()) {
                    NoteEvent ne = new BeginNote(keyToPitch(key));
                    player.request(ne);
                }
            }
        });
        /* Add a KeyAdapter to the keylisteners to check the key releases */
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                char key = (char) e.getKeyCode();
                if (availableNoteKeys.contains(key) &&
                        !player.isRecordingPlaying()) {
                    NoteEvent ne = new EndNote(keyToPitch(key));
                    player.request(ne);
                }
            }
        });
    }

    /* Convert the key to the Pitch */
    private Pitch keyToPitch(char key) {
        switch (key) {
            case '1':
                return new Pitch('C');
            case '2':
                return new Pitch('C').transpose(1);
            case '3':
                return new Pitch('D');
            case '4':
                return new Pitch('D').transpose(1);
            case '5':
                return new Pitch('E');
            case '6':
                return new Pitch('F');
            case '7':
                return new Pitch('F').transpose(1);
            case '8':
                return new Pitch('G');
            case '9':
                return new Pitch('G').transpose(1);
            case '0':
                return new Pitch('A');
            case '-':
                return new Pitch('A').transpose(1);
            case '=':
                return new Pitch('B');
        }
        return null;
    }
}

/* ========================================================================= */
public class PianoMachine implements MusicMachine {
    private final PianoPlayer player;
    private final Midi midi;
    private List<NoteEvent> recording, lastRecording;
    private final Set<Pitch> pitchesPlaying;
    private volatile boolean isRecording;
    private volatile long currentTime;

    public PianoMachine(PianoPlayer player) throws MidiUnavailableException {
        lastRecording = new ArrayList<>();
        recording = new ArrayList<>();
        pitchesPlaying = new HashSet<>();
        isRecording = false;
        midi = new Midi();
        this.player = player;
    }

    public void toggleRecording() {
        if (isRecording) {
            lastRecording = recording;
            System.out.println("stoped Recording!");
        } else {
            currentTime = System.currentTimeMillis();
            recording = new ArrayList<>();
            System.out.println("Started Recording!");
        }
        isRecording = !(isRecording);

    }

    @Override
    public void beginNote(NoteEvent event) {
        Pitch pitch = event.getPitch();
        if (pitchesPlaying.contains(pitch))
            return;
        event = event.delayed((System.currentTimeMillis() - currentTime));
        currentTime = System.currentTimeMillis();
        pitchesPlaying.add(pitch);
        System.out.println(event);
        midi.beginNote(pitch.toMidiFrequency());
        if (isRecording)
            recording.add(event);
    }

    @Override
    public void endNote(NoteEvent event) {
        Pitch pitch = event.getPitch();
        if (pitchesPlaying.contains(pitch)) {
            midi.endNote(pitch.toMidiFrequency());
            pitchesPlaying.remove(pitch);
            if (isRecording) {
                event = event.delayed((System.currentTimeMillis() - currentTime));
                currentTime = System.currentTimeMillis();
                recording.add(event);
            }
        }

    }

    public void requestPlayback() {
        player.playbackRecording(lastRecording);
    }

    // returns the status of recording
    public boolean isRecording() {
        return isRecording;
    }

    // to change the music instrument
    public void changeInstrument() {
        midi.changeDefaultInstrument();
    }

    /**
     * Change back to default instrument
     */
    void changeInstrumentPiano() {
        midi.changeInstrumentPiano();
    }
}

/* ========================================================================= */
public class PianoPlayer {
    private final BlockingQueue<NoteEvent> queue, delayQueue;
    private final PianoMachine machine;
    private boolean isRecordingPlaying;
    private Thread processQueueThread, processDelayQueueThread;

    public PianoPlayer() throws MidiUnavailableException {
        queue = new LinkedBlockingQueue<>();
        delayQueue = new LinkedBlockingQueue<>();
        machine = new PianoMachine(this);
        isRecordingPlaying = false;
        /*
         * start two threads:
         * 1. Check the queue and play the note events on the queue
         * 2. Check the delayQueue and put that into the queue to play
         * the notes in case of playing back the recording.
         */

        processQueueThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Process_Queue started!");
                processQueue();
            }
        });
        processDelayQueueThread = new Thread(new Runnable() {
            public void run() {
                System.out.println("Process_Delay_Queue started!");
                processDelayQueue();
            }
        });

        // starting threads
        processQueueThread.start();
        processDelayQueueThread.start();
    }

    public void request(NoteEvent e) {
        try {
            queue.put(e);
        } catch (InterruptedException ex) {
            System.out.println("Error!!! in request");
        }
    }

    public void requestPlayback() {
        System.out.println("\nplayback mood!\n");
        machine.requestPlayback();
    }

    public void toggleRecording() {
        machine.toggleRecording();
    }

    public void playbackRecording(List<NoteEvent> recording) {
        isRecordingPlaying = true;
        recording.forEach((e) -> {
            try {
                delayQueue.put(e);
            } catch (InterruptedException ex) {
                System.out.println("Error while playback recording!!!");
            }
        });
    }

    public void processQueue() {
        while (true) {
            try {
                NoteEvent e = queue.take();
                e.execute(machine);
            } catch (InterruptedException ex) {
                System.out.println("Error in processqueue");
            }
        }
    }

    public void processDelayQueue() {
        while (true) {
            try {
                NoteEvent e = delayQueue.take();
                midi.Midi.wait((int) (e.getDelay()));
                queue.put(e);
                isRecordingPlaying = !delayQueue.isEmpty();
            } catch (InterruptedException ex) {
                System.out.println("Error in process_delay_queue");
            }
        }
    }

    public boolean isRecording() {
        return machine.isRecording();
    }

    public boolean isRecordingPlaying() {
        return isRecordingPlaying;
    }

    public void changeInstrument() {
        machine.changeInstrument();
    }

    // change back to PIANO instrument
    public void changeInstrumentPiano() {
        machine.changeInstrumentPiano();
    }
}
/* ========================================================================= */