package me.nik.superenchantments.managers;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.Rarity;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.files.commentedfiles.CommentedFileConfiguration;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.ExpUtils;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class EnchanterManager implements AbstractManager {

    private final SuperEnchantments plugin;

    private final List<Offer> offers = new ArrayList<>();

    private boolean levels = true;
    private Material filler;

    private ItemStack randomEnchantItem;

    public EnchanterManager(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        load();
        loadRandomEnchantItem();
    }

    @Override
    public void shutdown() {
        offers.clear();
    }

    public void reload() {
        load();
        loadRandomEnchantItem();
    }

    public boolean isLevels() {
        return levels;
    }

    public String getTypeName() {
        return levels ? "levels" : "points";
    }

    public Material getFiller() {
        return filler;
    }

    public List<Offer> getOffers() {
        return Collections.unmodifiableList(offers);
    }

    public Offer getOffer(int slot) {

        for (Offer offer : offers) {
            if (offer.slot == slot) return offer;
        }

        return null;
    }

    private void loadRandomEnchantItem() {

        Material material = Material.matchMaterial(Config.Setting.RANDOM_ENCHANTMENT_BOOK_MATERIAL.getString());

        if (material == null || material.isAir() || !material.isItem()) {

            ChatUtils.log("Invalid book material, using BOOK");

            material = Material.BOOK;
        }

        ItemStack book = new ItemStack(material);

        ItemMeta meta = book.getItemMeta();

        meta.setDisplayName(ChatUtils.format(Config.Setting.RANDOM_ENCHANTMENT_BOOK_NAME.getString()));

        List<String> lore = Config.Setting.RANDOM_ENCHANTMENT_BOOK_LORE.getStringList();

        if (lore != null && !lore.isEmpty()) {

            List<String> itemLore = meta.hasLore() ? meta.getLore() : new ArrayList<>();

            for (String value : lore) {
                itemLore.add(ChatUtils.format(value
                        .replace("%common_chance%", (Config.Setting.RANDOM_ENCHANTMENT_BOOK_COMMON_CHANCE.getDouble() + "%"))
                        .replace("%rare_chance%", (Config.Setting.RANDOM_ENCHANTMENT_BOOK_RARE_CHANCE.getDouble() + "%"))
                        .replace("%legendary_chance%", (Config.Setting.RANDOM_ENCHANTMENT_BOOK_LEGENDARY_CHANCE.getDouble() + "%"))
                ));
            }

            meta.setLore(itemLore);
        }

        if (Config.Setting.RANDOM_ENCHANTMENT_BOOK_GLOW.getBoolean() && !meta.hasEnchants()) {

            //Apply by using both methods due to 1.8 -> Latest.
            book.addUnsafeEnchantment(MiscUtils.UNBREAKING, 1);
            meta.addEnchant(MiscUtils.UNBREAKING, 1, true);

            //Hide them while keeping the glow effect.
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        book.setItemMeta(meta);

        randomEnchantItem = book;
    }

    public CustomEnchant getRandomCustomEnchant() {

        Collection<CustomEnchant> enchants = plugin.getEnchantManager().getAll();

        Map<Rarity, List<CustomEnchant>> grouped = new EnumMap<>(Rarity.class);

        for (CustomEnchant enchant : enchants) {
            if (enchant == null || !enchant.isEnabled() || enchant.getRarity() == null) {
                continue;
            }

            grouped.computeIfAbsent(enchant.getRarity(), rarity -> new ArrayList<>()).add(enchant);
        }

        Map<Rarity, Double> weights = new EnumMap<>(Rarity.class);
        weights.put(Rarity.LEGENDARY, Config.Setting.RANDOM_ENCHANTMENT_BOOK_LEGENDARY_CHANCE.getDouble());
        weights.put(Rarity.RARE, Config.Setting.RANDOM_ENCHANTMENT_BOOK_RARE_CHANCE.getDouble());
        weights.put(Rarity.COMMON, Config.Setting.RANDOM_ENCHANTMENT_BOOK_COMMON_CHANCE.getDouble());

        double totalWeight = 0.0D;

        for (Map.Entry<Rarity, List<CustomEnchant>> entry : grouped.entrySet()) {
            Double weight = weights.get(entry.getKey());

            if (weight != null && weight > 0.0D && !entry.getValue().isEmpty()) {
                totalWeight += weight;
            }
        }

        if (totalWeight <= 0.0D) {
            return null;
        }

        double roll = ThreadLocalRandom.current().nextDouble(totalWeight);

        for (Map.Entry<Rarity, List<CustomEnchant>> entry : grouped.entrySet()) {
            Double weight = weights.get(entry.getKey());

            if (weight == null || weight <= 0.0D || entry.getValue().isEmpty()) {
                continue;
            }

            roll -= weight;

            if (roll < 0.0D) {
                List<CustomEnchant> candidates = entry.getValue();

                return candidates.get(
                        ThreadLocalRandom.current().nextInt(candidates.size())
                );
            }
        }

        return null;
    }

    private void load() {

        CommentedFileConfiguration config = plugin.getConfiguration().getConfig();

        if (config.get("enchanter.offers") == null) {
            writeDefaults(config);
            config.save();
        }

        levels = ExpUtils.isLevels(Config.Setting.ENCHANTER_COST_TYPE.getString());

        String fillerName = Config.Setting.ENCHANTER_FILLER.getString();

        filler = fillerName.equalsIgnoreCase("NONE") ? null : Material.matchMaterial(fillerName);

        offers.clear();

        ConfigurationSection section = config.getConfigurationSection("enchanter.offers");

        if (section == null) return;

        for (String id : section.getKeys(false)) {

            ConfigurationSection offer = section.getConfigurationSection(id);

            if (offer == null) continue;

            Material material = Material.matchMaterial(offer.getString("material", "BOOK"));

            if (material == null || material.isAir() || !material.isItem()) {
                ChatUtils.log("Invalid material in the enchanter offer " + id + ", using BOOK");
                material = Material.BOOK;
            }

            Map<String, Integer> entries = new LinkedHashMap<>();

            for (String raw : offer.getStringList("enchants")) {

                String[] split = raw.split(":");

                int weight = 1;

                if (split.length > 1) {

                    try {
                        weight = Math.max(1, Integer.parseInt(split[1].trim()));
                    } catch (NumberFormatException ignored) {
                    }
                }

                String enchant = split[0].trim().toLowerCase();

                if (plugin.getEnchantManager().get(enchant) == null) {
                    ChatUtils.log("Unknown enchant " + enchant + " in the enchanter offer " + id);
                    continue;
                }

                entries.put(enchant, weight);
            }

            offers.add(new Offer(
                    offer.getInt("slot", 0),
                    material,
                    offer.getString("name", id),
                    offer.getStringList("lore"),
                    Math.max(0, offer.getInt("cost", 0)),
                    offer.getString("permission", ""),
                    entries
            ));
        }
    }

    private void writeDefaults(CommentedFileConfiguration config) {

        config.set("enchanter.offers", "", "The offers of the enchanter", "Every section is an offer, copy one and rename it to add another");

        Collection<CustomEnchant> customEnchants = plugin.getEnchantManager().getAll();

        writeOffer(config, "common", 11, "BOOK", "&a&lCommon Enchantment", 15,
                customEnchants.stream().filter(ce -> ce.getRarity() == Rarity.COMMON).map(CustomEnchant::getId).collect(Collectors.toList()),
                true);

        writeOffer(config, "rare", 13, "BOOK", "&9&lRare Enchantment", 30,
                customEnchants.stream().filter(ce -> ce.getRarity() == Rarity.RARE).map(CustomEnchant::getId).collect(Collectors.toList()),
                false);

        writeOffer(config, "legendary", 15, "BOOK", "&6&lLegendary Enchantment", 50,
                customEnchants.stream().filter(ce -> ce.getRarity() == Rarity.LEGENDARY).map(CustomEnchant::getId).collect(Collectors.toList()),
                false);
    }

    private void writeOffer(CommentedFileConfiguration config, String id, int slot, String material, String name, int cost, List<String> enchants, boolean comment) {

        String path = "enchanter.offers." + id;

        config.set(path, "", new String[0]);

        config.set(path + ".slot", slot, comment ? new String[]{"The slot of the offer in the menu, counting from 0"} : new String[0]);

        config.set(path + ".material", material, comment ? new String[]{"The item of the offer"} : new String[0]);

        config.set(path + ".name", name, comment ? new String[]{"The display name of the offer"} : new String[0]);

        config.set(path + ".lore", Arrays.asList(
                "&7Buy a random enchant book",
                "",
                "&7Cost: &f%cost% %type%",
                "&7You have: &f%balance% %type%",
                "&7Possible enchants: &f%enchants%",
                "",
                "&b&lClick &7to buy"
        ), comment ? new String[]{"The lore of the offer", "Placeholders: %cost%, %type%, %balance%, %enchants%"} : new String[0]);

        config.set(path + ".cost", cost, comment ? new String[]{"The experience the offer costs", "Levels or points, depending on enchanter.cost_type"} : new String[0]);

        config.set(path + ".permission", "", comment ? new String[]{"The permission that is needed to buy the offer", "Leave it empty for none"} : new String[0]);

        config.set(path + ".enchants", enchants, comment ? new String[]{"The enchants that can be rolled, empty means every enabled enchant", "Add a weight with a colon to change the chances, like blast:3", "The default weight is 1"} : new String[0]);
    }

    private Map<CustomEnchant, Integer> pool(Offer offer) {

        Map<CustomEnchant, Integer> pool = new LinkedHashMap<>();

        if (offer.entries.isEmpty()) {

            for (CustomEnchant enchant : plugin.getEnchantManager().getAll()) {
                if (enchant.isEnabled()) pool.put(enchant, 1);
            }

            return pool;
        }

        for (Map.Entry<String, Integer> entry : offer.entries.entrySet()) {

            CustomEnchant enchant = plugin.getEnchantManager().get(entry.getKey());

            if (enchant != null && enchant.isEnabled()) pool.put(enchant, entry.getValue());
        }

        return pool;
    }

    public ItemStack getRandomEnchantItem() {
        return randomEnchantItem == null ? new ItemStack(Material.BOOK) : randomEnchantItem;
    }

    public int available(Offer offer) {
        return pool(offer).size();
    }

    public CustomEnchant roll(Offer offer) {

        Map<CustomEnchant, Integer> pool = pool(offer);

        if (pool.isEmpty()) return null;

        int total = 0;

        for (int weight : pool.values()) total += weight;

        int roll = ThreadLocalRandom.current().nextInt(total);

        CustomEnchant last = null;

        for (Map.Entry<CustomEnchant, Integer> entry : pool.entrySet()) {

            last = entry.getKey();

            roll -= entry.getValue();

            if (roll < 0) return last;
        }

        return last;
    }

    public static class Offer {

        private final int slot;
        private final Material material;
        private final String name;
        private final List<String> lore;
        private final int cost;
        private final String permission;
        private final Map<String, Integer> entries;

        private Offer(int slot, Material material, String name, List<String> lore, int cost, String permission, Map<String, Integer> entries) {
            this.slot = slot;
            this.material = material;
            this.name = name;
            this.lore = lore;
            this.cost = cost;
            this.permission = permission;
            this.entries = entries;
        }

        public int getSlot() {
            return slot;
        }

        public Material getMaterial() {
            return material;
        }

        public String getName() {
            return name;
        }

        public List<String> getLore() {
            return lore;
        }

        public int getCost() {
            return cost;
        }

        public String getPermission() {
            return permission;
        }
    }
}