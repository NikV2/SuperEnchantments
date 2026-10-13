package me.nik.superenchantments.gui;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Material;

public abstract class PaginatedMenu extends Menu {

    protected int page = 0;

    protected int maxItemsPerPage = 45;

    protected int index = 0;

    public PaginatedMenu(PlayerMenu playerMenu, SuperEnchantments plugin) {
        super(playerMenu, plugin);
    }

    public void addMenuBorder() {
        inventory.setItem(48, MiscUtils.makeItem(Material.WRITABLE_BOOK, "&6Previous Page", null));
        inventory.setItem(49, MiscUtils.makeItem(Material.BARRIER, "&cExit", null));
        inventory.setItem(50, MiscUtils.makeItem(Material.WRITABLE_BOOK, "&6Next Page", null));
    }
}
