package builder.ui;

import engine.art.ArtNotFoundException;
import engine.art.loader.ArtLoader;
import engine.art.loader.MalformedArtException;
import engine.art.sprites.SpriteGroup;

import java.io.IOException;

/**
 * A repository of sprites to use throughout the game
 *
 * @provided
 */
public class SpriteGallery {
    // UI
    /** UI icons used in overlays. */
    public static final SpriteGroup icons = load("Icons", "icons");

    /** Inventory UI icons used for the inventory bar. */
    public static final SpriteGroup inventory = load("Inventory", "inventory");

    /** Sprites for items rendered in the inventory. */
    public static final SpriteGroup tools = load("Tools", "tools");

    // Tiles
    /** Tilled dirt field. */
    public static final SpriteGroup tilled = load("Tilled", "tilled");

    /** Grass tiles. */
    public static final SpriteGroup grass = load("Grass", "grass");

    /** Dirt field tile. */
    public static final SpriteGroup field = load("Field", "field");

    /** Water tiles. */
    public static final SpriteGroup water = load("Water", "water");

    // Entities
    /** Brutus character sprites. */
    public static final SpriteGroup brutus = load("Brutus", "brutus");

    /** Chicken farmer character sprites. */
    public static final SpriteGroup chickenFarmer = load("ChickenFarmer", "chickenFarmer");

    /** Rock/mine/ore resource sprites. */
    public static final SpriteGroup rock = load("Rock", "rock");

    /** Cabbage resource sprites. */
    public static final SpriteGroup cabbage = load("Cabbage", "cabbage");

    // Npcs
    /** Eagle sprites. */
    public static final SpriteGroup eagle = load("Eagle", "eagle");

    /** Pigeon sprites. */
    public static final SpriteGroup pigeon = load("Pigeon", "pigeon");

    /** Magpie sprites. */
    public static final SpriteGroup magpie = load("Magpie", "magpie");

    /** Hive sprites. */
    public static final SpriteGroup hive = load("Hive", "hive");

    /** Bee sprites. */
    public static final SpriteGroup bee = load("Bee", "bee");

    /** Scarecrow sprites. */
    public static final SpriteGroup scarecrow = load("Scarecrow", "scarecrow");

    public static final SpriteGroup letters = load("Letters", "letter");

    // Enemies

    private SpriteGallery() {}

    /**
     * Load a sprite image from an art file at resources/art/[spriteFilename].art. The group of
     * assets under groupName are returned.
     *
     * @param spriteFilename The name of the file under resources/art/ to load.
     * @param groupName The common prefix of sprites within the given file.
     */
    private static SpriteGroup load(String spriteFilename, String groupName) {
        try {
            return ArtLoader.load("resources/art/" + spriteFilename + ".art").lookup(groupName);
        } catch (IOException | ArtNotFoundException | MalformedArtException e) {
            // Cannot throw a checked exception when instantiating a static field.
            // Wrap up any thrown exception as RuntimeException
            // This should crash the JVM when starting up the game
            throw new RuntimeException(e);
        }
    }
}
