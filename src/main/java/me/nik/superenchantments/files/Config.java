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

        ANVIL("anvil", "", "Anvil Settings"),
        ANVIL_ENABLED("anvil.enabled", true, "Should players be able to apply enchant books to tools in an anvil?"),
        ANVIL_COST("anvil.cost", 5, "The XP level cost of applying an enchant in the anvil");

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
