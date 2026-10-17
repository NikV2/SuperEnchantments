package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SmithingEnchant extends CustomEnchant {

    private int amount;

    public SmithingEnchant(SuperEnchantments plugin) {
        super(plugin, "smithing", "Smithing", "&a", "HELMET", "CHESTPLATE", "LEGGINGS", "BOOTS", "SWORD", "AXE", "HOE", "PICKAXE", "SHOVEL", "SPEAR", "TRIDENT", "MACE", "ELYTRA", "SHIELD", "SHEARS", "FLINT_AND_STEEL", "FISHING_ROD");

        setDescription("&7Slowly repairs your item every second");

        addOption("amount", 1, "The repair amount to apply on an item");

        plugin.getUpdateTask().add(this::updatePlayer);
    }

    @Override
    protected void onLoad() {
        amount = getInt("amount");
    }

    private void updatePlayer(Player player) {

        if (!isEnabled() || player.isDead()) return;

        List<ItemStack> items = getItems(player);

        if (items.isEmpty()) return;

        for (ItemStack item : items) {

            if (item == null || item.getType() == Material.AIR) continue;

            if (appliesTo(item.getType()) && hasEnchant(item)) {

                short damage = item.getDurability();

                if (damage <= 0) continue;

                item.setDurability((short) Math.max(0, damage - amount));
            }
        }
    }

    private List<ItemStack> getItems(Player player) {

        List<ItemStack> list = new ArrayList<>();

        Collections.addAll(list, player.getInventory().getArmorContents());
        list.add(player.getInventory().getItemInMainHand());
        list.add(player.getInventory().getItemInOffHand());

        return list;
    }
}