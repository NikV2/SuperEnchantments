package me.nik.superenchantments.listeners;

import me.nik.superenchantments.gui.Menu;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

public class GuiListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e) {

        InventoryHolder holder = e.getInventory().getHolder();

        if (!(holder instanceof Menu)) return;

        e.setCancelled(true);

        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getInventory()) return;

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        Menu menu = (Menu) holder;

        menu.handleMenu(e);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof Menu) e.setCancelled(true);
    }
}
