package me.nik.superenchantments;

import me.nik.superenchantments.enchants.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Registry for custom enchants. The lore line (e.g. "Blast") is the source of truth.
 */
public final class EnchantManager {

    private final SuperEnchantments plugin;
    private final Map<String, CustomEnchant> enchants = new LinkedHashMap<>();

    public EnchantManager(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    public void register(CustomEnchant enchant) {
        enchants.put(enchant.getId(), enchant);
        plugin.getServer().getPluginManager().registerEvents(enchant, plugin);
    }

    public CustomEnchant get(String id) {
        return id == null ? null : enchants.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<CustomEnchant> getAll() {
        return Collections.unmodifiableCollection(enchants.values());
    }

    // ---- Lore matching ----

    private boolean isLineOf(String line, CustomEnchant enchant) {
        String plain = Text.strip(line).trim();
        String name = Text.strip(Text.color(enchant.getDisplayName())).trim();
        return plain.equals(name);
    }

    private boolean isEnchantLine(String line) {
        for (CustomEnchant e : enchants.values()) {
            if (isLineOf(line, e)) return true;
        }
        return false;
    }

    // ---- Public item API ----

    public boolean has(ItemStack item, CustomEnchant enchant) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasLore()) return false;
        for (String line : meta.getLore()) {
            if (isLineOf(line, enchant)) return true;
        }
        return false;
    }

    public List<CustomEnchant> getEnchantsOn(ItemStack item) {
        List<CustomEnchant> out = new ArrayList<>();
        for (CustomEnchant e : enchants.values()) {
            if (has(item, e)) out.add(e);
        }
        return out;
    }

    /** Returns a copy of the item with the enchant added (no-op if already present). */
    public ItemStack apply(ItemStack item, CustomEnchant enchant) {
        ItemStack out = item.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null) return out;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> isLineOf(line, enchant));

        // Keep all enchant lines grouped at the top, above any other lore.
        int index = 0;
        while (index < lore.size() && isEnchantLine(lore.get(index))) index++;
        lore.add(index, enchant.formatLore());

        meta.setLore(lore);
        out.setItemMeta(meta);
        return out;
    }

    /** Returns a copy of the item with the enchant removed. */
    public ItemStack remove(ItemStack item, CustomEnchant enchant) {
        ItemStack out = item.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null || !meta.hasLore()) return out;

        List<String> lore = new ArrayList<>(meta.getLore());
        lore.removeIf(line -> isLineOf(line, enchant));
        meta.setLore(lore.isEmpty() ? null : lore);
        out.setItemMeta(meta);
        return out;
    }

    public ItemStack createBook(CustomEnchant enchant) {
        ItemStack book = apply(new ItemStack(Material.ENCHANTED_BOOK), enchant);
        ItemMeta meta = book.getItemMeta();
        String format = plugin.getConfig().getString("books.name-format", "&b{name} Book");
        meta.setDisplayName(Text.color(format.replace("{name}", enchant.getDisplayName())));
        book.setItemMeta(meta);
        return book;
    }
}
