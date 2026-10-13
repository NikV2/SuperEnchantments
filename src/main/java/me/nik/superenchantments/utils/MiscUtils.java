package me.nik.superenchantments.utils;

import com.google.common.base.Charsets;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class MiscUtils {

    private MiscUtils() {
    }

    public static final Enchantment UNBREAKING = Enchantment.getByName("DURABILITY") != null
            ? Enchantment.getByName("DURABILITY")
            : Enchantment.getByName("UNBREAKING");

    public static YamlConfiguration loadConfigurationUTF_8(final File file) {

        final YamlConfiguration config = new YamlConfiguration();

        try {

            final FileInputStream stream = new FileInputStream(file);

            config.load(new InputStreamReader(stream, Charsets.UTF_8));

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return config;
    }

    public static ItemStack makeItem(Material material, String displayName, List<String> lore) {

        ItemStack item = new ItemStack(material);

        ItemMeta itemMeta = item.getItemMeta();

        itemMeta.setDisplayName(ChatUtils.format(displayName));

        if (lore != null) {

            List<String> loreList = new ArrayList<>();

            for (String l : lore) {

                loreList.add(ChatUtils.format(l));
            }

            itemMeta.setLore(loreList);
        }

        item.setItemMeta(itemMeta);

        return item;
    }

    public static String prettify(String name) {

        StringBuilder builder = new StringBuilder();

        for (String word : name.toLowerCase(Locale.ROOT).split("_")) {

            if (word.isEmpty()) continue;

            if (builder.length() > 0) builder.append(' ');

            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }

        return builder.toString();
    }

    public static String describeTypes(List<String> types) {

        List<String> names = new ArrayList<>();

        for (String type : types) {

            String t = type.toUpperCase(Locale.ROOT);

            if (t.equals("ALL")) return "All items";

            String name = prettify(t);

            if (Material.matchMaterial(t) == null && !name.endsWith("s")) name += "s";

            names.add(name);
        }

        return String.join(", ", names);
    }

    public static Material getIcon(List<String> types) {

        if (types.isEmpty()) return Material.BOOK;

        String t = types.get(0).toUpperCase(Locale.ROOT);

        Material material = Material.matchMaterial("DIAMOND_" + t);

        if (material == null) material = Material.matchMaterial(t);

        return material == null ? Material.BOOK : material;
    }
}
