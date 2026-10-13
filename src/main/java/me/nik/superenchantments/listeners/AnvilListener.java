package me.nik.superenchantments.listeners;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.CustomEnchant;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.managers.EnchantManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@SuppressWarnings("removal")
public class AnvilListener implements Listener {

    private final SuperEnchantments plugin;

    public AnvilListener(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPrepare(PrepareAnvilEvent e) {

        if (!Config.Setting.ANVIL_ENABLED.getBoolean()) return;

        AnvilInventory inv = e.getInventory();

        ItemStack left = inv.getItem(0);
        ItemStack right = inv.getItem(1);

        if (left == null || right == null || right.getType() != this.plugin.getEnchantManager().getBookMaterial()) return;

        EnchantManager manager = plugin.getEnchantManager();

        List<CustomEnchant> onBook = manager.getEnchantsOn(right);

        if (onBook.isEmpty()) return;

        if ((onBook.size() + 1) > Config.Setting.ANVIL_LIMIT.getInt()) return;

        ItemStack base = e.getResult();

        ItemStack result = (base != null && base.getType() != Material.AIR) ? base : left;

        int cost = 0;

        boolean changed = false;

        for (CustomEnchant enchant : onBook) {

            if (!enchant.isEnabled() || !enchant.appliesTo(left.getType())) continue;

            if (manager.has(result, enchant)) continue;

            result = manager.apply(result, enchant);

            cost += Config.Setting.ANVIL_COST.getInt();

            changed = true;
        }

        if (!changed) return;

        e.setResult(result);

        inv.setRepairCost(Math.max(1, cost));
    }
}
