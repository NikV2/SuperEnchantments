package me.nik.superenchantments;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/** Lets players apply enchant books to tools in an anvil. */
public final class AnvilListener implements Listener {

    private final SuperEnchantments plugin;

    public AnvilListener(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent event) {
        if (!plugin.getConfig().getBoolean("anvil.enabled", true)) return;

        AnvilInventory inv = event.getInventory();
        ItemStack left = inv.getItem(0);
        ItemStack right = inv.getItem(1);
        if (left == null || right == null || right.getType() != Material.ENCHANTED_BOOK) return;

        EnchantManager manager = plugin.getEnchantManager();
        List<CustomEnchant> onBook = manager.getEnchantsOn(right);
        if (onBook.isEmpty()) return;

        // Start from vanilla's result when there is one so renames are preserved.
        ItemStack base = event.getResult();
        ItemStack result = (base != null && base.getType() != Material.AIR) ? base : left;

        int cost = 0;
        boolean changed = false;

        for (CustomEnchant enchant : onBook) {
            if (!enchant.isEnabled() || !enchant.appliesTo(left.getType())) continue;
            if (manager.has(result, enchant)) continue;

            result = manager.apply(result, enchant);
            cost += plugin.getConfig().getInt("anvil.cost", 5);
            changed = true;
        }

        if (!changed) return;
        event.setResult(result);
        inv.setRepairCost(Math.max(1, cost));
    }
}
