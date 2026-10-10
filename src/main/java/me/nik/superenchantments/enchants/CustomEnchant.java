package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.files.commentedfiles.CommentedFileConfiguration;
import me.nik.superenchantments.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public abstract class CustomEnchant implements Listener {

    protected final SuperEnchantments plugin;

    private final String id;

    private final Map<String, Option> options = new LinkedHashMap<>();

    public CustomEnchant(SuperEnchantments plugin, String id, String displayName, String loreColor, String... appliesTo) {
        this.plugin = plugin;
        this.id = id.toLowerCase(Locale.ROOT);

        addOption("enabled", true, "Should this enchant work?");
        addOption("display-name", displayName, "The name shown in the lore, it is also how the enchant is found on items");
        addOption("lore-color", loreColor, "The color of the lore line");
        addOption("applies-to", Arrays.asList(appliesTo), "The material suffixes this enchant can be applied to, or ALL");
    }

    protected void addOption(String key, Object defaultValue, String... comments) {
        this.options.put(key, new Option(defaultValue, comments));
    }

    public void writeDefaults() {

        CommentedFileConfiguration config = this.plugin.getConfiguration().getConfig();

        String section = "enchants." + this.id;

        if (config.get(section) == null) {
            config.set(section, "", getDisplayName() + " Settings");
        }

        for (Map.Entry<String, Option> entry : this.options.entrySet()) {

            String path = section + "." + entry.getKey();

            if (config.get(path) == null) {
                config.set(path, entry.getValue().defaultValue, entry.getValue().comments);
            }
        }
    }

    private Object value(String key) {

        Object value = this.plugin.getConfiguration().getConfig().get("enchants." + this.id + "." + key);

        if (value != null) return value;

        Option option = this.options.get(key);

        return option == null ? null : option.defaultValue;
    }

    protected boolean getBoolean(String key) {
        return (boolean) value(key);
    }

    protected int getInt(String key) {
        return ((Number) value(key)).intValue();
    }

    protected double getDouble(String key) {
        return ((Number) value(key)).doubleValue();
    }

    protected String getString(String key) {
        return String.valueOf(value(key));
    }

    @SuppressWarnings("unchecked")
    protected List<String> getStringList(String key) {

        Object value = value(key);

        return value instanceof List ? (List<String>) value : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return getBoolean("enabled");
    }

    public String getDisplayName() {
        return getString("display-name");
    }

    public boolean appliesTo(Material material) {

        for (String type : getStringList("applies-to")) {

            String t = type.toUpperCase(Locale.ROOT);

            if (t.equals("ALL") || material.name().equals(t) || material.name().endsWith("_" + t)) return true;
        }

        return false;
    }

    public String formatLore() {
        return ChatUtils.format(getString("lore-color") + getDisplayName());
    }

    public boolean hasEnchant(ItemStack item) {
        return this.plugin.getEnchantManager().has(item, this);
    }

    private static class Option {

        private final Object defaultValue;
        private final String[] comments;

        private Option(Object defaultValue, String[] comments) {
            this.defaultValue = defaultValue;
            this.comments = comments;
        }
    }
}
