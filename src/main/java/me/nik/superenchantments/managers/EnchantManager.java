package me.nik.superenchantments.managers;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.CustomEnchant;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.utils.ChatUtils;
import org.bukkit.Bukkit;
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

public class EnchantManager implements AbstractManager {

    private final SuperEnchantments plugin;

    private final Map<String, CustomEnchant> enchants = new LinkedHashMap<>();

    public EnchantManager(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
        enchants.clear();
    }

    public void register(CustomEnchant... enchantments) {
        for (CustomEnchant enchant : enchantments) {
            enchants.put(enchant.getId(), enchant);
            enchant.writeDefaults();
            plugin.getConfiguration().getConfig().save();
            Bukkit.getPluginManager().registerEvents(enchant, plugin);
        }
    }

    public void reload() {

        for (CustomEnchant enchant : enchants.values()) enchant.writeDefaults();

        plugin.getConfiguration().getConfig().save();
    }

    public CustomEnchant get(String id) {
        return id == null ? null : enchants.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<CustomEnchant> getAll() {
        return Collections.unmodifiableCollection(enchants.values());
    }

    private boolean isLineOf(String line, CustomEnchant enchant) {
        return ChatUtils.stripColorCodes(line).trim().equals(ChatUtils.stripColorCodes(enchant.getDisplayName()).trim());
    }

    private boolean isEnchantLine(String line) {

        for (CustomEnchant enchant : enchants.values()) {
            if (isLineOf(line, enchant)) return true;
        }

        return false;
    }

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

        for (CustomEnchant enchant : enchants.values()) {
            if (has(item, enchant)) out.add(enchant);
        }

        return out;
    }

    public ItemStack apply(ItemStack item, CustomEnchant enchant) {

        ItemStack out = item.clone();

        ItemMeta meta = out.getItemMeta();

        if (meta == null) return out;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<String>();

        lore.removeIf(line -> isLineOf(line, enchant));

        int index = 0;

        while (index < lore.size() && isEnchantLine(lore.get(index))) index++;

        lore.add(index, enchant.formatLore());

        meta.setLore(lore);

        out.setItemMeta(meta);

        return out;
    }

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

        Material bookMaterial;

        try {
            bookMaterial = Material.valueOf(Config.Setting.BOOKS_MATERIAL.getString());
        } catch (Throwable t) {
            ChatUtils.log("Invalid book material");
            return null;
        }

        ItemStack book = apply(new ItemStack(bookMaterial), enchant);

        ItemMeta meta = book.getItemMeta();

        meta.setDisplayName(ChatUtils.format(Config.Setting.BOOKS_NAME_FORMAT.getString().replace("%name%", enchant.getDisplayName())));

        book.setItemMeta(meta);

        return book;
    }
}
