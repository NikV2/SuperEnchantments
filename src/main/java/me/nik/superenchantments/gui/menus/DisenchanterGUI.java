package me.nik.superenchantments.gui.menus;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.gui.PaginatedMenu;
import me.nik.superenchantments.gui.PlayerMenu;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.ExpUtils;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class DisenchanterGUI extends PaginatedMenu {

    public DisenchanterGUI(PlayerMenu playerMenu, SuperEnchantments plugin) {
        super(playerMenu, plugin);
    }

    @Override
    protected String getMenuName() {
        return ChatUtils.format(Config.Setting.DISENCHANTER_TITLE.getString());
    }

    @Override
    protected int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        switch (e.getSlot()) {
            case 48:
                if (page != 0) {
                    page = page - 1;
                    open();
                }
                return;
            case 49:
                p.closeInventory();
                return;
            case 50:
                if ((page + 1) * maxItemsPerPage < plugin.getEnchantManager().getEnchantsOn(p.getInventory().getItemInMainHand()).size()) {
                    page = page + 1;
                    open();
                }
                return;
            default:
                break;
        }

        ItemStack item = e.getCurrentItem();

        if (!item.hasItemMeta() || !item.getItemMeta().hasLore()) return;

        List<String> lore = item.getItemMeta().getLore();

        CustomEnchant enchant = plugin.getEnchantManager().get(ChatUtils.stripColorCodes(lore.get(lore.size() - 1)).replace("ID:", "").trim());

        if (enchant == null) return;

        remove(p, enchant);
    }

    private void remove(Player p, CustomEnchant enchant) {

        ItemStack held = p.getInventory().getItemInMainHand();

        if (held.getType() == Material.AIR) {
            p.sendMessage(MsgType.HOLD_ITEM.getMessage());
            p.closeInventory();
            return;
        }

        if (held.getAmount() > 1) {
            p.sendMessage(MsgType.HOLD_SINGLE_ITEM.getMessage());
            p.closeInventory();
            return;
        }

        if (!plugin.getEnchantManager().has(held, enchant)) {
            p.sendMessage(MsgType.ENCHANT_MISSING.getMessage());
            refresh();
            return;
        }

        boolean levels = ExpUtils.isLevels(Config.Setting.DISENCHANTER_COST_TYPE.getString());

        int cost = Math.max(0, Config.Setting.DISENCHANTER_COST.getInt());

        if (!ExpUtils.has(p, levels, cost)) {

            p.sendMessage(MsgType.NOT_ENOUGH_EXP.getMessage()
                    .replace("%cost%", String.valueOf(cost))
                    .replace("%type%", levels ? "levels" : "points"));

            MiscUtils.playSound(p, Config.Setting.DISENCHANTER_FAIL_SOUND.getString());

            return;
        }

        if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(MsgType.INVENTORY_FULL.getMessage());
            MiscUtils.playSound(p, Config.Setting.DISENCHANTER_FAIL_SOUND.getString());
            return;
        }

        ExpUtils.take(p, levels, cost);

        p.getInventory().setItemInMainHand(plugin.getEnchantManager().remove(held, enchant));

        p.getInventory().addItem(plugin.getEnchantManager().createBook(enchant));

        p.sendMessage(MsgType.ENCHANT_REMOVED.getMessage().replace("%enchant%", enchant.getLoreLine()));

        MiscUtils.playSound(p, Config.Setting.DISENCHANTER_SUCCESS_SOUND.getString());

        refresh();
    }

    @Override
    protected void setMenuItems() {

        addMenuBorder();

        ItemStack item = playerMenu.getOwner().getInventory().getItemInMainHand();

        if (item.getType() == plugin.getEnchantManager().getBookMaterial()) return;

        List<CustomEnchant> enchants = plugin.getEnchantManager().getEnchantsOn(item);

        boolean levels = ExpUtils.isLevels(Config.Setting.DISENCHANTER_COST_TYPE.getString());

        int cost = Math.max(0, Config.Setting.DISENCHANTER_COST.getInt());

        for (int i = 0; i < super.maxItemsPerPage; i++) {

            index = super.maxItemsPerPage * page + i;

            if (index >= enchants.size()) break;

            CustomEnchant enchant = enchants.get(index);

            List<String> lore = new ArrayList<>(enchant.getDescription());

            if (!lore.isEmpty()) lore.add("");

            if (cost > 0) lore.add("&9Cost: &b" + cost + " " + (levels ? "levels" : "points"));

            lore.add("&b&lClick &7to remove it and get the book back");
            lore.add("");
            lore.add("&8ID: " + enchant.getId());

            ItemStack itemStack = MiscUtils.makeItem(item.getType(), enchant.getLoreLine(), lore);

            inventory.addItem(itemStack);
        }
    }
}