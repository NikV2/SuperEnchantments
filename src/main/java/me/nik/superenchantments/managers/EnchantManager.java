package me.nik.superenchantments.managers;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EnchantManager implements AbstractManager {

    private final SuperEnchantments plugin;

    private final Map<String, CustomEnchant> enchants = new LinkedHashMap<>();
    private final Map<String, CustomEnchant> byName = new HashMap<>();

    private Material bookMaterial = Material.BOOK;

    public EnchantManager(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        loadBookMaterial();
    }

    @Override
    public void shutdown() {
        enchants.clear();
        byName.clear();
    }

    public void register(CustomEnchant... enchantments) {

        for (CustomEnchant enchant : enchantments) {
            enchants.put(enchant.getId(), enchant);
            enchant.load(plugin.getEnchantments().getConfig());
            Bukkit.getPluginManager().registerEvents(enchant, plugin);
        }

        index();

        plugin.getEnchantments().save();
    }

    public void reload() {

        plugin.getEnchantments().setup();

        for (CustomEnchant enchant : enchants.values()) enchant.load(plugin.getEnchantments().getConfig());

        index();

        loadBookMaterial();

        plugin.getEnchantments().save();
    }

    private void index() {

        byName.clear();

        for (CustomEnchant enchant : enchants.values()) byName.put(enchant.getPlainName(), enchant);
    }

    private void loadBookMaterial() {

        Material material = Material.matchMaterial(Config.Setting.BOOKS_MATERIAL.getString());

        if (material == null || material.isAir() || !material.isItem()) {

            ChatUtils.log("Invalid book material, using BOOK");

            material = Material.BOOK;
        }

        bookMaterial = material;
    }

    public Material getBookMaterial() {
        return bookMaterial;
    }

    public CustomEnchant get(String id) {
        return id == null ? null : enchants.get(id.toLowerCase(Locale.ROOT));
    }

    public Collection<CustomEnchant> getAll() {
        return Collections.unmodifiableCollection(enchants.values());
    }

    private List<String> getLore(ItemStack item) {

        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();

        return meta != null && meta.hasLore() ? meta.getLore() : null;
    }

    public boolean has(ItemStack item, CustomEnchant enchant) {

        List<String> lore = getLore(item);

        if (lore == null) return false;

        for (String line : lore) {
            if (enchant.matches(line)) return true;
        }

        return false;
    }

    public List<CustomEnchant> getEnchantsOn(ItemStack item) {

        List<String> lore = getLore(item);

        if (lore == null) return Collections.emptyList();

        List<CustomEnchant> out = new ArrayList<>();

        for (String line : lore) {

            CustomEnchant enchant = byName.get(ChatUtils.plain(line));

            if (enchant != null && !out.contains(enchant)) out.add(enchant);
        }

        return out;
    }

    public ItemStack apply(ItemStack item, CustomEnchant enchant) {

        ItemStack out = item.clone();

        ItemMeta meta = out.getItemMeta();

        if (meta == null) return out;

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        lore.removeIf(enchant::matches);

        int index = 0;

        while (index < lore.size() && byName.containsKey(ChatUtils.plain(lore.get(index)))) index++;

        lore.add(index, enchant.getLoreLine());

        meta.setLore(lore);

        out.setItemMeta(meta);

        return out;
    }

    public ItemStack remove(ItemStack item, CustomEnchant enchant) {

        ItemStack out = item.clone();

        ItemMeta meta = out.getItemMeta();

        if (meta == null || !meta.hasLore()) return out;

        List<String> lore = new ArrayList<>(meta.getLore());

        lore.removeIf(enchant::matches);

        meta.setLore(lore.isEmpty() ? null : lore);

        out.setItemMeta(meta);

        return out;
    }

    public ItemStack createBook(CustomEnchant enchant) {

        ItemStack book = apply(new ItemStack(bookMaterial), enchant);

        ItemMeta meta = book.getItemMeta();

        meta.setDisplayName(ChatUtils.format(Config.Setting.BOOKS_NAME.getString()));

        List<String> lore = Config.Setting.BOOKS_LORE.getStringList();

        if (lore != null && !lore.isEmpty()) {

            List<String> itemLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            for (String value : lore) {

                if (value.equalsIgnoreCase("%description%")) {
                    enchant.getDescription().forEach(text -> itemLore.add(ChatUtils.format(text)));
                    continue;
                }

                itemLore.add(ChatUtils.format(value.replace("%applicable%", MiscUtils.describeTypes(enchant.getAppliesTo()))));
            }

            meta.setLore(itemLore);
        }

        if (Config.Setting.BOOKS_GLOW.getBoolean() && !meta.hasEnchants()) {

            //Apply by using both methods due to 1.8 -> Latest.
            book.addUnsafeEnchantment(MiscUtils.UNBREAKING, 1);
            meta.addEnchant(MiscUtils.UNBREAKING, 1, true);

            //Hide them while keeping the glow effect.
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        book.setItemMeta(meta);

        return book;
    }
}
