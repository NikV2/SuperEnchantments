package me.nik.superenchantments.enchants.categories;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.files.commentedfiles.CommentedFileConfiguration;
import me.nik.superenchantments.utils.ChatUtils;
import org.bukkit.Material;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public abstract class CustomEnchant implements Listener {

    protected final SuperEnchantments plugin;

    private final String id;

    private final Map<String, Option> options = new LinkedHashMap<>();
    private final Map<String, Object> values = new HashMap<>();

    private boolean enabled;
    private String displayName;
    private String plainName;
    private String loreLine;
    private List<String> description = Collections.emptyList();
    private List<String> appliesTo = Collections.emptyList();
    private Set<Material> materials = EnumSet.noneOf(Material.class);
    private List<Material> materialList = Collections.emptyList();

    protected CustomEnchant(SuperEnchantments plugin, String id, String displayName, String loreColor, String... appliesTo) {
        this.plugin = plugin;
        this.id = id.toLowerCase(Locale.ROOT);

        addOption("enabled", true, "Should this enchant work?");
        addOption("display-name", displayName, "The name shown in the lore, it is also how the enchant is found on items");
        addOption("lore-color", loreColor, "The color of the lore line");
        addOption("applies-to", Arrays.asList(appliesTo), "The material suffixes this enchant can be applied to, or ALL");
        addOption("description", new ArrayList<String>(), "The description shown in the enchantments menu");
    }

    protected void addOption(String key, Object defaultValue, String... comments) {
        options.put(key, new Option(defaultValue, comments));
    }

    protected void setDescription(String... lines) {
        addOption("description", Arrays.asList(lines), "The description shown in the enchantments menu");
    }

    protected void onLoad() {
    }

    public void load(CommentedFileConfiguration config) {

        if (config.get(id) == null) {
            config.set(id, "", options.get("display-name").defaultValue + " Settings");
        }

        for (Map.Entry<String, Option> entry : options.entrySet()) {

            String path = id + "." + entry.getKey();

            if (config.get(path) != null) continue;

            config.set(path, entry.getValue().defaultValue, entry.getValue().comments);
        }

        refresh(config);
    }

    private void refresh(CommentedFileConfiguration config) {

        values.clear();

        for (Map.Entry<String, Option> entry : options.entrySet()) {

            Object value = config.get(id + "." + entry.getKey());

            Object def = entry.getValue().defaultValue;

            if (!isValid(value, def)) {

                if (value != null) ChatUtils.log("Invalid value for " + id + "." + entry.getKey() + ", using the default");

                value = def;
            }

            if (value instanceof List) {

                List<String> list = new ArrayList<>();

                for (Object o : (List<?>) value) list.add(String.valueOf(o));

                value = Collections.unmodifiableList(list);
            }

            values.put(entry.getKey(), value);
        }

        enabled = getBoolean("enabled");
        displayName = getString("display-name");
        plainName = ChatUtils.plain(ChatUtils.format(displayName));
        loreLine = ChatUtils.format(getString("lore-color") + displayName);
        appliesTo = getStringList("applies-to");

        List<String> lines = new ArrayList<>();

        for (String line : getStringList("description")) lines.add(ChatUtils.format(line));

        description = Collections.unmodifiableList(lines);

        materials = resolve(appliesTo);
        materialList = Collections.unmodifiableList(new ArrayList<>(materials));

        onLoad();
    }

    private boolean isValid(Object value, Object def) {

        if (value == null) return false;

        if (def instanceof Boolean) return value instanceof Boolean;

        if (def instanceof Number) return value instanceof Number;

        if (def instanceof List) return value instanceof List;

        return !(value instanceof List);
    }

    private Set<Material> resolve(List<String> types) {

        Set<Material> out = EnumSet.noneOf(Material.class);

        for (Material material : Material.values()) {

            if (material.name().startsWith("LEGACY_") || !material.isItem()) continue;

            for (String type : types) {

                String t = type.toUpperCase(Locale.ROOT);

                if (t.equals("ALL") || material.name().equals(t) || material.name().endsWith("_" + t)) {
                    out.add(material);
                    break;
                }
            }
        }

        return out;
    }

    private Object get(String key) {

        Object value = values.get(key);

        if (value == null) throw new IllegalArgumentException("The enchant " + id + " has no option named " + key);

        return value;
    }

    protected boolean getBoolean(String key) {
        return (boolean) get(key);
    }

    protected int getInt(String key) {
        return ((Number) get(key)).intValue();
    }

    protected double getDouble(String key) {
        return ((Number) get(key)).doubleValue();
    }

    protected String getString(String key) {
        return String.valueOf(get(key));
    }

    @SuppressWarnings("unchecked")
    protected List<String> getStringList(String key) {
        return (List<String>) get(key);
    }

    public String getId() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPlainName() {
        return plainName;
    }

    public String getLoreLine() {
        return loreLine;
    }

    public List<String> getDescription() {
        return description;
    }

    public List<String> getAppliesTo() {
        return appliesTo;
    }

    public List<Material> getMaterials() {
        return materialList;
    }

    public boolean appliesTo(Material material) {
        return materials.contains(material);
    }

    public boolean matches(String line) {
        return line.equals(loreLine) || ChatUtils.plain(line).equals(plainName);
    }

    public boolean hasEnchant(ItemStack item) {
        return plugin.getEnchantManager().has(item, this);
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
