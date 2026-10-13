package me.nik.superenchantments.gui;

import me.nik.superenchantments.SuperEnchantments;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public abstract class Menu implements InventoryHolder {

    protected final SuperEnchantments plugin;

    protected Inventory inventory;

    protected PlayerMenu playerMenu;

    public Menu(PlayerMenu playerMenu, SuperEnchantments plugin) {
        this.playerMenu = playerMenu;
        this.plugin = plugin;
    }

    protected abstract String getMenuName();

    protected abstract int getSlots();

    public abstract void handleMenu(InventoryClickEvent e);

    protected abstract void setMenuItems();

    public void open() {
        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());

        setMenuItems();

        playerMenu.getOwner().openInventory(inventory);
    }

    protected void refresh() {
        inventory.clear();

        setMenuItems();

        playerMenu.getOwner().updateInventory();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
