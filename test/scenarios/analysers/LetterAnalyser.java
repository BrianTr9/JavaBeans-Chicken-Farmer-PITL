package scenarios.analysers;

import builder.ui.SpriteGallery;

import engine.art.sprites.Sprite;
import engine.art.sprites.SpriteGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A {@link Analyser} intended to provided helper methods for common checks around the state of
 * various {@link engine.renderer.Renderable}s believed to be using sprites from
 * SpriteGallery.letters.
 */
public class LetterAnalyser extends Analyser {

    private static final SpriteGroup art = SpriteGallery.letters;
    private final List<Sprite> digits = new ArrayList<>();
    private final List<Sprite> letters = new ArrayList<>();

    /**
     * Constructs a new {@link LetterAnalyser} using the given {@link Analyser} analysers internal
     * {@link FrameRecord} state.
     *
     * @param analyser {@link Analyser} we extract the relevant {@link FrameRecord} state from.
     */
    public LetterAnalyser(Analyser analyser) {
        super(analyser.getId(), analyser.getFrames());
        this.digits.add(art.getSprite("0"));
        this.digits.add(art.getSprite("1"));
        this.digits.add(art.getSprite("2"));
        this.digits.add(art.getSprite("3"));
        this.digits.add(art.getSprite("4"));
        this.digits.add(art.getSprite("5"));
        this.digits.add(art.getSprite("6"));
        this.digits.add(art.getSprite("7"));
        this.digits.add(art.getSprite("8"));
        this.digits.add(art.getSprite("9"));

        for (char c = 'A'; c <= 'Z'; c++) {
            this.letters.add(art.getSprite(String.valueOf(c)));
        }
    }

    /**
     * Confirms if this letter matches the given symbol for all frames
     *
     * @param symbol symbol we check against
     * @return if this letter matches the given symbol for all frames
     */
    public boolean is(String symbol) {
        final Sprite symbolSprite = art.getSprite(symbol);
        return this.is(symbolSprite);
    }

    /**
     * Confirms if this Letter matches the given sprite for all frames.
     *
     * @param sprite the sprite we are matching against.
     * @return if this letter matches the given sprite for all frames.
     */
    public boolean is(Sprite sprite) {
        for (FrameRecord frame : this.getFrames()) {
            if (!Objects.equals(frame.getSprite(), sprite)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Confirms if the given Letter is alphabetic for all frames.
     *
     * @return if the given Letter is alphabetic for all frames.
     */
    public boolean isAlphabetic() {
        for (FrameRecord frame : this.getFrames()) {
            if (!this.letters.contains(frame.getSprite())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Confirms if the given Letter is numeric for all frames.
     *
     * @return if the given Letter is numeric for all frames.
     */
    public boolean isNumeric() {
        for (FrameRecord frame : this.getFrames()) {
            if (!this.digits.contains(frame.getSprite())) {
                return false;
            }
        }
        return true;
    }
}
