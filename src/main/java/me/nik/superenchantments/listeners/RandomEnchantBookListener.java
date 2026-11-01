package me.nik.superenchantments.listeners;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RandomEnchantBookListener implements Listener {

    private final SuperEnchantments plugin;

    public RandomEnchantBookListener(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;

        Player player = e.getPlayer();

        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem == null || handItem.getType() == Material.AIR || !handItem.isSimilar(plugin.getEnchanterManager().getRandomEnchantItem())) return;

        CustomEnchant enchant = plugin.getEnchanterManager().getRandomCustomEnchant();

        if (enchant == null) return;

        if (handItem.getAmount() == 1) {

            player.getInventory().setItemInMainHand(null);

            player.updateInventory();

        } else handItem.setAmount(handItem.getAmount() - 1);

        for (ItemStack left : player.getInventory().addItem(plugin.getEnchantManager().createBook(enchant)).values()) {
            player.getWorld().dropItem(player.getLocation(), left);
        }
    }
}