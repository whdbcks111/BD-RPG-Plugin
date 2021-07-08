package rpg.music;

public enum Pitch {
    FA_SHARP_0("fa#", (float) Math.pow(2, -12/12.0), 0),
    SOL_0("sol", (float) Math.pow(2, -11/12.0), 0),
    SOL_SHARP_0("sol#", (float) Math.pow(2, -10/12.0), 0),
    LA_0("la", (float) Math.pow(2, -9/12.0), 0),
    LA_SHARP_0("la#", (float) Math.pow(2, -8/12.0), 0),
    SI_0("si", (float) Math.pow(2, -7/12.0), 0),
    DO_1("do", (float) Math.pow(2, -6/12.0), 1),
    DO_SHARP_1("do#", (float) Math.pow(2, -5/12.0), 1),
    RE_1("re", (float) Math.pow(2, -4/12.0), 1),
    RE_SHARP_1("re#", (float) Math.pow(2, -3/12.0), 1),
    MI_1("mi", (float) Math.pow(2, -2/12.0), 1),
    FA_1("fa", (float) Math.pow(2, -1/12.0), 1),
    FA_SHARP_1("fa#", (float) Math.pow(2, 0/12.0), 1),
    SOL_1("sol", (float) Math.pow(2, 1/12.0), 1),
    SOL_SHARP_1("sol#", (float) Math.pow(2, 2/12.0), 1),
    LA_1("la", (float) Math.pow(2, 3/12.0), 1),
    LA_SHARP_1("la#", (float) Math.pow(2, 4/12.0), 1),
    SI_1("si", (float) Math.pow(2, 5/12.0), 1),
    DO_2("do", (float) Math.pow(2, 6/12.0), 2),
    DO_SHARP_2("do#", (float) Math.pow(2, 7/12.0), 2),
    RE_2("re", (float) Math.pow(2, 8/12.0), 2),
    RE_SHARP_2("re#", (float) Math.pow(2, 9/12.0), 2),
    MI_2("mi", (float) Math.pow(2, 10/12.0), 2),
    FA_2("fa", (float) Math.pow(2, 11/12.0), 2),
    FA_SHARP_2("fa#", (float) Math.pow(2, 12/12.0), 2)
    ;

    private final String name;
    private final float pitch;
    private final int octave;

    Pitch(String name, float pitch, int octave) {
        this.name = name;
        this.pitch = pitch;
        this.octave = octave;
    }

    public String getName() {
        return name;
    }

    public float getPitch() {
        return pitch;
    }

    public int getOctave() {
        return octave;
    }
}
