package me.nik.superenchantments.enchants;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

public class TelepathyEnchant extends CustomEnchant {

    private String sound;
    private float soundVolume;

    public TelepathyEnchant(SuperEnchantments plugin) {
        super(plugin, "telepathy", "Telepathy", "&a", "PICKAXE", "AXE", "SHOVEL");

        setDescription("&7Mined drops go straight into your inventory");

        addOption("sound", "entity.item.pickup", "NONE to disable");
        addOption("sound_volume", 0.2D, "The volume of the sound");
    }

    @Override
    protected void onLoad() {

        String name = getString("sound");

        sound = name.equalsIgnoreCase("NONE") ? null : name;

        soundVolume = (float) getDouble("sound_volume");
    }

    public static TelepathyEnchant get(SuperEnchantments plugin, ItemStack tool) {

        CustomEnchant enchant = plugin.getEnchantManager().get("telepathy");

        if (!(enchant instanceof TelepathyEnchant) || !enchant.isEnabled()) return null;

        if (!enchant.appliesTo(tool.getType()) || !enchant.hasEnchant(tool)) return null;

        return (TelepathyEnchant) enchant;
    }

    public boolean collect(Player player, Collection<ItemStack> items, Location location) {

        boolean collected = false;

        for (ItemStack item : items) {

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.clone());

            if (leftover.isEmpty()) {
                collected = true;
                continue;
            }

            for (ItemStack remaining : leftover.values()) {

                if (remaining.getAmount() < item.getAmount()) collected = true;

                location.getWorld().dropItemNaturally(location, remaining);
            }
        }

        return collected;
    }

    public void playSound(Player player) {

        if (sound != null) player.playSound(player.getLocation(), sound, soundVolume, 1.0F);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDrop(BlockDropItemEvent e) {

        if (!isEnabled()) return;

        Player player = e.getPlayer();

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!appliesTo(tool.getType()) || !hasEnchant(tool)) return;

        boolean collected = false;

        Iterator<Item> iterator = e.getItems().iterator();

        while (iterator.hasNext()) {

            Item item = iterator.next();

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item.getItemStack());

            if (leftover.isEmpty()) {
                iterator.remove();
                collected = true;
                continue;
            }

            ItemStack remaining = leftover.values().iterator().next();

            if (remaining.getAmount() < item.getItemStack().getAmount()) collected = true;

            item.setItemStack(remaining);
        }

        if (collected) playSound(player);
    }
}