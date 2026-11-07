package me.nik.superenchantments.utils;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public final class MiscUtils {

    private MiscUtils() {
    }

    public static final Enchantment UNBREAKING = Enchantment.getByName("DURABILITY") != null
            ? Enchantment.getByName("DURABILITY")
            : Enchantment.getByName("UNBREAKING");

    private static final Method ATTACK_COOLDOWN = findAttackCooldown();

    private static Method findAttackCooldown() {

        try {
            return Player.class.getMethod("getAttackCooldown");
        } catch (NoSuchMethodException ignored) {
        }

        try {
            return Player.class.getMethod("getCooledAttackStrength", float.class);
        } catch (NoSuchMethodException ignored) {
        }

        return null;
    }

    public static float getAttackCharge(Player player) {

        if (ATTACK_COOLDOWN == null) return 1.0F;

        try {

            Object value = ATTACK_COOLDOWN.getParameterTypes().length == 0 ? ATTACK_COOLDOWN.invoke(player) : ATTACK_COOLDOWN.invoke(player, 0.0F);

            return ((Number) value).floatValue();

        } catch (ReflectiveOperationException e) {
            return 1.0F;
        }
    }

    public static YamlConfiguration loadConfigurationUTF_8(final File file) {

        final YamlConfiguration config = new YamlConfiguration();

        try {

            final FileInputStream stream = new FileInputStream(file);

            config.load(new InputStreamReader(stream, StandardCharsets.UTF_8));

        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        return config;
    }

    public static boolean isMelee(EntityDamageByEntityEvent e) {
        return e.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || e.getCause() == EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK;
    }

    public static void playSound(Player player, String sound) {

        if (sound.isEmpty() || sound.equalsIgnoreCase("NONE")) return;

        player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
    }

    public static void ignoreMitigation(EntityDamageEvent e, double portion) {

        double base = e.getDamage();
        double damage = e.getFinalDamage();

        if (damage <= 0.0D || damage >= base) return;

        double ignored = Math.max(0.0D, Math.min(1.0D, portion));

        e.setDamage(base * ((damage + (base - damage) * ignored) / damage));
    }

    public static Particle getParticle(String name) {

        if (name.equalsIgnoreCase("NONE")) return null;

        try {
            return Particle.valueOf(name.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
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

    public static boolean damageTool(ItemStack tool, int amount, boolean respectUnbreaking) {

        int maxDurability = tool.getType().getMaxDurability();

        if (maxDurability <= 0) return true;

        ItemMeta meta = tool.getItemMeta();

        if (!(meta instanceof Damageable)) return true;

        Damageable damageable = (Damageable) meta;

        int damage = amount;

        if (respectUnbreaking && UNBREAKING != null) {

            int level = tool.getEnchantmentLevel(UNBREAKING);

            if (level > 0) {

                damage = 0;

                for (int i = 0; i < amount; i++) {

                    if (ThreadLocalRandom.current().nextInt(level + 1) == 0) damage++;
                }
            }
        }

        if (damageable.getDamage() + damage >= maxDurability - 1) return false;

        damageable.setDamage(damageable.getDamage() + damage);

        tool.setItemMeta(meta);

        return true;
    }

    public static void playBreakEffect(Block block) {

        try {
            block.getWorld().playEffect(block.getLocation(), Effect.STEP_SOUND, block.getBlockData());
        } catch (IllegalArgumentException ignored) {
        }
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
