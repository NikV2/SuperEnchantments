package me.nik.superenchantments.files;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.files.commentedfiles.CommentedFileConfiguration;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Config {

    private static final String[] HEADER = new String[]{
            "+----------------------------------------------------------------------------------------------+",
            "|                                                                                              |",
            "|                                        SuperEnchantments                                     |",
            "|                                                                                              |",
            "|                                           Author: Nik                                        |",
            "|                                                                                              |",
            "+----------------------------------------------------------------------------------------------+"
    };

    private final SuperEnchantments plugin;
    private CommentedFileConfiguration configuration;
    private static boolean exists;

    public Config(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    public void setup() {

        File configFile = new File(this.plugin.getDataFolder(), "config.yml");

        exists = configFile.exists();

        boolean setHeaderFooter = !exists;

        boolean changed = setHeaderFooter;

        this.configuration = CommentedFileConfiguration.loadConfiguration(this.plugin, configFile);

        if (setHeaderFooter) {
            this.configuration.addComments(HEADER);
        }

        for (Setting setting : Setting.values()) {

            setting.reset();

            changed |= setting.setIfNotExists(this.configuration);
        }

        if (changed) this.configuration.save();

        for (Setting setting : Setting.values()) setting.loadValue();
    }

    public void reset() {
        for (Setting setting : Setting.values()) setting.reset();
    }

    /**
     * @return the config.yml as a CommentedFileConfiguration
     */
    public CommentedFileConfiguration getConfig() {
        return this.configuration;
    }

    public enum Setting {
        CHECK_UPDATES("check_updates", true, "Should we check for updates on startup?"),

        BOOKS("books", "", "Enchant Book Settings"),
        BOOKS_NAME("books.name", "&bEnchanted Book", "The display name of the custom enchantment books"),
        BOOKS_MATERIAL("books.material", "BOOK", "The material to use for custom enchantment books"),
        BOOKS_GLOW("books.glow", true, "Should the custom enchantment book glow?"),
        BOOKS_LORE("books.lore", Arrays.asList(
                "",
                "%description%",
                "",
                "&6Applies to: &a%applicable%"
        ), "The lore of the custom enchantment book", "NOTE: The name of the enchantment will always show first", "Placeholders: %description%, %applicable%"),

        RANDOM_ENCHANTMENT_BOOK("random_enchantment_book", "", "Random Enchantment Book Settings"),
        RANDOM_ENCHANTMENT_BOOK_NAME("random_enchantment_book.name", "&#FF0000&lR&#FF1D00&la&#FF3A00&ln&#FF5700&ld&#FF7300&lo&#FF9000&lm&#FFAE00&l &#FFCB00&lE&#FFE800&ln&#F3FF00&lc&#B9FF00&lh&#80FF00&la&#46FF00&ln&#0CFF00&lt&#00D12E&lm&#009768&le&#005DA2&ln&#0023DC&lt&#0D00FB&l &#2F00F1&lB&#5100E7&lo&#7200DD&lo&#9400D3&lk", "The display name of the random enchantment book"),
        RANDOM_ENCHANTMENT_BOOK_MATERIAL("random_enchantment_book.material", "BOOK", "The material to use for the random enchantment book"),
        RANDOM_ENCHANTMENT_BOOK_GLOW("random_enchantment_book.glow", true, "Should the random enchantment book glow?"),
        RANDOM_ENCHANTMENT_BOOK_LORE("random_enchantment_book.lore", Arrays.asList(
                "",
                "&6Right Click",
                "&6To receive a random enchantment book!",
                "",
                "&2&nCommon:&f %common_chance%",
                "&3&nRare:&f %rare_chance%",
                "&5&nLegendary:&f %legendary_chance%",
                ""
        ), "The lore of the random enchantment book"),
        RANDOM_ENCHANTMENT_BOOK_COMMON_CHANCE("random_enchantment_book.common_chance", 50.0, "The chance of the book giving a common rarity enchantment"),
        RANDOM_ENCHANTMENT_BOOK_RARE_CHANCE("random_enchantment_book.rare_chance", 30.0, "The chance of the book giving a rare rarity enchantment"),
        RANDOM_ENCHANTMENT_BOOK_LEGENDARY_CHANCE("random_enchantment_book.legendary_chance", 20.0, "The chance of the book giving a legendary rarity enchantment"),

        ANVIL("anvil", "", "Anvil Settings"),
        ANVIL_ENABLED("anvil.enabled", true, "Should players be able to apply enchant books to tools in an anvil?"),
        ANVIL_LIMIT("anvil.limit", 3, "The maximum limit of custom enchantments allowed in a single piece"),
        ANVIL_COST("anvil.cost", 5, "The XP level cost of applying an enchant in the anvil"),

        ENCHANTER("enchanter", "", "Enchanter Settings", "Players spend experience on a random custom enchantment book"),
        ENCHANTER_ENABLED("enchanter.enabled", true, "Should the enchanter menu and its command work?"),
        ENCHANTER_TITLE("enchanter.title", "&9Enchanter", "The title of the menu"),
        ENCHANTER_ROWS("enchanter.rows", 3, "The rows of the menu", "From 1 to 6"),
        ENCHANTER_FILLER("enchanter.filler", "GRAY_STAINED_GLASS_PANE", "The item that fills the empty slots of the menu", "NONE to leave them empty"),
        ENCHANTER_COST_TYPE("enchanter.cost_type", "LEVELS", "What the offers cost", "LEVELS or POINTS (experience points)"),
        ENCHANTER_SUCCESS_SOUND("enchanter.success_sound", "entity.player.levelup", "NONE to disable", "Played when a book is bought"),
        ENCHANTER_FAIL_SOUND("enchanter.fail_sound", "entity.villager.no", "NONE to disable", "Played when a purchase fails"),

        DISENCHANTER("disenchanter", "", "Disenchanter Settings", "Players remove a custom enchant from the item in their hand and get its book back"),
        DISENCHANTER_ENABLED("disenchanter.enabled", true, "Should the disenchanter menu and its command work?"),
        DISENCHANTER_TITLE("disenchanter.title", "&9Remove Enchantments", "The title of the menu"),
        DISENCHANTER_BORDER("disenchanter.border", "BLACK_STAINED_GLASS_PANE", "The item of the border of the menu", "NONE to leave it empty"),
        DISENCHANTER_FILLER("disenchanter.filler", "GRAY_STAINED_GLASS_PANE", "The item that fills the empty slots of the menu", "NONE to leave them empty"),
        DISENCHANTER_COST("disenchanter.cost", 0, "What removing an enchant costs", "0 is free"),
        DISENCHANTER_COST_TYPE("disenchanter.cost_type", "LEVELS", "LEVELS or POINTS (experience points)"),
        DISENCHANTER_SUCCESS_SOUND("disenchanter.success_sound", "block.anvil.use", "NONE to disable", "Played when an enchant is removed"),
        DISENCHANTER_FAIL_SOUND("disenchanter.fail_sound", "entity.villager.no", "NONE to disable", "Played when removing an enchant fails");

        private final String key;
        private final Object defaultValue;
        private boolean excluded;
        private final String[] comments;
        private Object value = null;

        Setting(String key, Object defaultValue, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
        }

        Setting(String key, Object defaultValue, boolean excluded, String... comments) {
            this.key = key;
            this.defaultValue = defaultValue;
            this.comments = comments != null ? comments : new String[0];
            this.excluded = excluded;
        }

        /**
         * Gets the setting as a boolean
         *
         * @return The setting as a boolean
         */
        public boolean getBoolean() {
            this.loadValue();
            return (boolean) this.value;
        }

        public String getKey() {
            return this.key;
        }

        /**
         * @return the setting as an int
         */
        public int getInt() {
            this.loadValue();
            return (int) this.getNumber();
        }

        /**
         * @return the setting as a short
         */
        public short getShort() {
            this.loadValue();
            return (short) this.getNumber();
        }

        /**
         * @return the setting as a long
         */
        public long getLong() {
            this.loadValue();
            return (long) this.getNumber();
        }

        /**
         * @return the setting as a double
         */
        public double getDouble() {
            this.loadValue();
            return this.getNumber();
        }

        /**
         * @return the setting as a float
         */
        public float getFloat() {
            this.loadValue();
            return (float) this.getNumber();
        }

        /**
         * @return the setting as a String
         */
        public String getString() {
            this.loadValue();
            return String.valueOf(this.value);
        }

        private double getNumber() {
            if (this.value instanceof Integer) {
                return (int) this.value;
            } else if (this.value instanceof Short) {
                return (short) this.value;
            } else if (this.value instanceof Byte) {
                return (byte) this.value;
            } else if (this.value instanceof Float) {
                return (float) this.value;
            } else if (this.value instanceof Long) {
                return (long) this.value;
            }

            return (double) this.value;
        }

        /**
         * @return the setting as a string list
         */
        @SuppressWarnings("unchecked")
        public List<String> getStringList() {
            this.loadValue();
            return (List<String>) this.value;
        }

        private boolean setIfNotExists(CommentedFileConfiguration fileConfiguration) {
            this.loadValue();

            if (exists && this.excluded) return false;

            if (fileConfiguration.get(this.key) == null) {
                List<String> comments = Stream.of(this.comments).collect(Collectors.toList());
                if (this.defaultValue != null) {
                    fileConfiguration.set(this.key, this.defaultValue, comments.toArray(new String[0]));
                } else {
                    fileConfiguration.addComments(comments.toArray(new String[0]));
                }

                return true;
            }

            return false;
        }

        /**
         * Resets the cached value
         */
        public void reset() {
            this.value = null;
        }

        /**
         * @return true if this setting is only a section and doesn't contain an actual value
         */
        public boolean isSection() {
            return this.defaultValue == null;
        }

        /**
         * Loads the value from the config and caches it if it isn't set yet
         */
        public void loadValue() {
            if (this.value != null) return;
            this.value = SuperEnchantments.getInstance().getConfiguration().getConfig().get(this.key);
        }
    }
}
