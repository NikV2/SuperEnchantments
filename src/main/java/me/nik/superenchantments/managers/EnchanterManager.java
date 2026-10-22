package me.nik.superenchantments.managers;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.files.commentedfiles.CommentedFileConfiguration;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.ExpUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class EnchanterManager implements AbstractManager {

    private final SuperEnchantments plugin;

    private final List<Offer> offers = new ArrayList<>();

    private boolean levels = true;
    private Material filler;

    public EnchanterManager(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    public void initialize() {
        load();
    }

    @Override
    public void shutdown() {
        offers.clear();
    }

    public void reload() {
        load();
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

        writeOffer(config, "common", 11, "BOOK", "&a&lCommon Enchantment", 15, new ArrayList<>(), true);

        writeOffer(config, "rare", 13, "BOOK", "&9&lRare Enchantment", 30, Arrays.asList(
                "charge", "juggernaut", "penetration", "piercingsoul", "executioner", "windburst"
        ), false);

        writeOffer(config, "legendary", 15, "BOOK", "&6&lLegendary Enchantment", 50, Arrays.asList(
                "blast", "lumberjack", "thunder", "aegis"
        ), false);
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