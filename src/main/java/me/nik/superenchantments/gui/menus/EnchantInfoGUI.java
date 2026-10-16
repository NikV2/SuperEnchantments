package me.nik.superenchantments.gui.menus;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.gui.PaginatedMenu;
import me.nik.superenchantments.gui.PlayerMenu;
import me.nik.superenchantments.utils.ChatUtils;
import me.nik.superenchantments.utils.MiscUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EnchantInfoGUI extends PaginatedMenu {

    private final CustomEnchant enchant;

    public EnchantInfoGUI(PlayerMenu playerMenu, SuperEnchantments plugin, CustomEnchant enchant) {
        super(playerMenu, plugin);
        this.enchant = enchant;
    }

    @Override
    protected String getMenuName() {
        return ChatUtils.format("&9" + enchant.getPlainName() + " &8- &7Items");
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
                break;
            case 49:
                p.closeInventory();
                new EnchantsGUI(playerMenu, plugin).open();
                break;
            case 50:
                if ((page + 1) * maxItemsPerPage < enchant.getMaterials().size()) {
                    page = page + 1;
                    open();
                }
                break;
            case 52:
                if (!p.hasPermission(Permissions.ADMIN.getPermission())) return;

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
                break;
            default:
                break;
        }
    }

    @Override
    protected void setMenuItems() {

        addMenuBorder();

        inventory.setItem(49, MiscUtils.makeItem(Material.BARRIER, "&cBack", null));

        List<String> lore = new ArrayList<>(enchant.getDescription());

        if (!lore.isEmpty()) lore.add("");

        lore.add("&9Applies to: &b" + MiscUtils.describeTypes(enchant.getAppliesTo()));
        lore.add("&9Status: " + (enchant.isEnabled() ? "&aEnabled" : "&cDisabled"));

        inventory.setItem(46, MiscUtils.makeItem(MiscUtils.getIcon(enchant.getAppliesTo()), enchant.getLoreLine(), lore));

        if (playerMenu.getOwner().hasPermission(Permissions.ADMIN.getPermission())) {

            List<String> bookLore = new ArrayList<>();
            bookLore.add("");
            bookLore.add("&b&lClick &7to receive the book");

            inventory.setItem(52, MiscUtils.makeItem(Material.BOOK, "&aGet Book", bookLore));
        }

        List<Material> materials = enchant.getMaterials();

        for (int i = 0; i < super.maxItemsPerPage; i++) {
            index = super.maxItemsPerPage * page + i;
            if (index >= materials.size()) break;
            inventory.addItem(new ItemStack(materials.get(index)));
        }
    }
}
