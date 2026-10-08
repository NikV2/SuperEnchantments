package me.nik.superenchantments;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

/**
 * Base class for every enchant. Subclasses are registered as Listeners, so just
 * add @EventHandler methods. All settings live in config.yml under enchants.<id>.
 */
public abstract class CustomEnchant implements Listener {

    protected final SuperEnchantments plugin;
    private final String id;

    protected CustomEnchant(SuperEnchantments plugin, String id) {
        this.plugin = plugin;
        this.id = id.toLowerCase(Locale.ROOT);
    }

    public String getId() {
        return id;
    }

    /** This enchant's config section (re-read each call, so /se reload takes effect). */
    public ConfigurationSection config() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("enchants." + id);
        return section != null ? section : plugin.getConfig().createSection("enchants." + id);
    }

    public boolean isEnabled() {
        return config().getBoolean("enabled", true);
    }

    public String getDisplayName() {
        return config().getString("display-name", id);
    }

    public boolean appliesTo(Material material) {
        List<String> types = config().getStringList("applies-to");
        for (String type : types) {
            String t = type.toUpperCase(Locale.ROOT);
            if (t.equals("ALL") || material.name().equals(t) || material.name().endsWith("_" + t)) {
                return true;
            }
        }
        return false;
    }

    /** The lore line representing this enchant. */
    public String formatLore() {
        String color = config().getString("lore-color", "&7");
        return Text.color(color + getDisplayName());
    }

    /** True if the item carries this enchant's lore line. */
    public boolean hasEnchant(ItemStack item) {
        return plugin.getEnchantManager().has(item, this);
    }
}
