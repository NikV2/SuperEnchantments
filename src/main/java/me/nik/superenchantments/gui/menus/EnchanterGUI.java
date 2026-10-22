package me.nik.superenchantments.gui.menus;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.files.Config;
import me.nik.superenchantments.gui.Menu;
import me.nik.superenchantments.gui.PlayerMenu;
import me.nik.superenchantments.managers.EnchanterManager;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.ExpUtils;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnchanterGUI extends Menu {

    public EnchanterGUI(PlayerMenu playerMenu, SuperEnchantments plugin) {
        super(playerMenu, plugin);
    }

    @Override
    protected String getMenuName() {
        return ChatUtils.format(Config.Setting.ENCHANTER_TITLE.getString());
    }

    @Override
    protected int getSlots() {
        return Math.max(1, Math.min(6, Config.Setting.ENCHANTER_ROWS.getInt())) * 9;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

        Player p = (Player) e.getWhoClicked();

        EnchanterManager manager = plugin.getEnchanterManager();

        EnchanterManager.Offer offer = manager.getOffer(e.getSlot());

        if (offer == null) return;

        if (!offer.getPermission().isEmpty() && !p.hasPermission(offer.getPermission())) {
            p.sendMessage(MsgType.NO_PERMISSION.getMessage());
            return;
        }

        if (!ExpUtils.has(p, manager.isLevels(), offer.getCost())) {

            p.sendMessage(MsgType.NOT_ENOUGH_EXP.getMessage()
                    .replace("%cost%", String.valueOf(offer.getCost()))
                    .replace("%type%", manager.getTypeName()));

            MiscUtils.playSound(p, Config.Setting.ENCHANTER_FAIL_SOUND.getString());

            return;
        }

        CustomEnchant enchant = manager.roll(offer);

        if (enchant == null) {
            p.sendMessage(MsgType.OFFER_EMPTY.getMessage());
            return;
        }

        if (p.getInventory().firstEmpty() == -1) {
            p.sendMessage(MsgType.INVENTORY_FULL.getMessage());
            MiscUtils.playSound(p, Config.Setting.ENCHANTER_FAIL_SOUND.getString());
            return;
        }

        ExpUtils.take(p, manager.isLevels(), offer.getCost());

        p.getInventory().addItem(plugin.getEnchantManager().createBook(enchant));

        p.sendMessage(MsgType.ENCHANTER_BOUGHT.getMessage()
                .replace("%enchant%", enchant.getLoreLine())
                .replace("%cost%", String.valueOf(offer.getCost()))
                .replace("%type%", manager.getTypeName()));

        MiscUtils.playSound(p, Config.Setting.ENCHANTER_SUCCESS_SOUND.getString());

        refresh();
    }

    @Override
    protected void setMenuItems() {

        EnchanterManager manager = plugin.getEnchanterManager();

        Player player = playerMenu.getOwner();

        if (manager.getFiller() != null) {

            ItemStack filler = MiscUtils.makeItem(manager.getFiller(), " ", null);

            for (int i = 0; i < getSlots(); i++) inventory.setItem(i, filler);
        }

        int balance = manager.isLevels() ? player.getLevel() : ExpUtils.getTotal(player);

        for (EnchanterManager.Offer offer : manager.getOffers()) {

            if (offer.getSlot() < 0 || offer.getSlot() >= getSlots()) continue;

            String enchants = String.valueOf(manager.available(offer));

            List<String> lore = new ArrayList<>();

            for (String line : offer.getLore()) lore.add(replace(line, offer, manager, balance, enchants));

            inventory.setItem(offer.getSlot(), MiscUtils.makeItem(offer.getMaterial(), replace(offer.getName(), offer, manager, balance, enchants), lore));
        }
    }

    private String replace(String text, EnchanterManager.Offer offer, EnchanterManager manager, int balance, String enchants) {
        return text
                .replace("%cost%", String.valueOf(offer.getCost()))
                .replace("%type%", manager.getTypeName())
                .replace("%balance%", String.valueOf(balance))
                .replace("%enchants%", enchants);
    }
}