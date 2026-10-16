package me.nik.superenchantments.gui.menus;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.gui.PaginatedMenu;
import me.nik.superenchantments.gui.PlayerMenu;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class EnchantsGUI extends PaginatedMenu {

    public EnchantsGUI(PlayerMenu playerMenu, SuperEnchantments plugin) {
        super(playerMenu, plugin);
    }

    @Override
    protected String getMenuName() {
        return ChatUtils.format("&9Custom Enchantments");
    }

    @Override
    protected int getSlots() {
        return 54;
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();

        ItemStack item = e.getCurrentItem();
        if (!item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) return;

        switch (item.getType()) {
            case BARRIER:
                p.closeInventory();
                return;
            case WRITABLE_BOOK:
                switch (ChatColor.stripColor(item.getItemMeta().getDisplayName())) {
                    case "Previous Page":
                        if (page != 0) {
                            page = page - 1;
                            open();
                        }
                        break;
                    case "Next Page":
                        if ((page + 1) * maxItemsPerPage < plugin.getEnchantManager().getAll().size()) {
                            page = page + 1;
                            open();
                        }
                        break;
                }
                return;
            default:
                break;
        }

        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore() || meta.getLore().isEmpty()) return;

        String idLine = ChatUtils.stripColorCodes(meta.getLore().get(meta.getLore().size() - 1));

        CustomEnchant enchant = plugin.getEnchantManager().get(idLine.replace("ID:", "").trim());

        if (enchant == null) return;

        if (e.getClick().isRightClick() && p.hasPermission(Permissions.ADMIN.getPermission())) {

            if (!enchant.isEnabled()) {
                p.sendMessage(MsgType.ENCHANT_DISABLED.getMessage().replace("%enchant%", enchant.getDisplayName()));
                return;
            }

            for (ItemStack left : p.getInventory().addItem(plugin.getEnchantManager().createBook(enchant)).values()) {
                p.getWorld().dropItem(p.getLocation(), left);
            }

            p.sendMessage(MsgType.BOOK_GIVEN.getMessage()
                    .replace("%player%", p.getName())
                    .replace("%enchant%", enchant.getDisplayName()));

            return;
        }

        p.closeInventory();

        new EnchantInfoGUI(playerMenu, plugin, enchant).open();
    }

    @Override
    protected void setMenuItems() {

        addMenuBorder();

        boolean admin = playerMenu.getOwner().hasPermission(Permissions.ADMIN.getPermission());

        List<ItemStack> items = new ArrayList<>();

        for (CustomEnchant enchant : plugin.getEnchantManager().getAll()) {

            List<String> lore = new ArrayList<>(enchant.getDescription());

            if (!lore.isEmpty()) lore.add("");

            lore.add("&9Applies to: &b" + MiscUtils.describeTypes(enchant.getAppliesTo()));
            lore.add("&9Status: " + (enchant.isEnabled() ? "&aEnabled" : "&cDisabled"));
            lore.add("");
            lore.add("&b&lLeft-click &7to see the items");

            if (admin) lore.add("&b&lRight-click &7to get the book");

            lore.add("");
            lore.add("&8ID: " + enchant.getId());

            items.add(MiscUtils.makeItem(this.plugin.getEnchantManager().getBookMaterial(), enchant.getLoreLine(), lore));
        }

        for (int i = 0; i < super.maxItemsPerPage; i++) {
            index = super.maxItemsPerPage * page + i;
            if (index >= items.size()) break;
            if (items.get(index) != null) {
                inventory.addItem(items.get(index));
            }
        }
    }
}
