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
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisenchanterGUI extends PaginatedMenu {

    private static final int[] SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final int PREVIEW = 4;
    private static final int PREVIOUS = 45;
    private static final int INFO = 47;
    private static final int CLOSE = 49;
    private static final int NEXT = 53;

    private List<CustomEnchant> shown = new ArrayList<>();

    public DisenchanterGUI(PlayerMenu playerMenu, SuperEnchantments plugin) {
        super(playerMenu, plugin);
        this.maxItemsPerPage = SLOTS.length;
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

        int slot = e.getSlot();

        if (slot == CLOSE) {
            p.closeInventory();
            return;
        }

        if (slot == PREVIOUS) {

            if (page > 0) {
                page = page - 1;
                open();
            }

            return;
        }

        if (slot == NEXT) {

            if ((page + 1) * maxItemsPerPage < getEnchants(p).size()) {
                page = page + 1;
                open();
            }

            return;
        }

        for (int i = 0; i < SLOTS.length && i < shown.size(); i++) {

            if (SLOTS[i] == slot) {
                remove(p, shown.get(i));
                return;
            }
        }
    }

    private List<CustomEnchant> getEnchants(Player player) {
        return plugin.getEnchantManager().getEnchantsOn(player.getInventory().getItemInMainHand());
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

        Player player = playerMenu.getOwner();

        ItemStack held = player.getInventory().getItemInMainHand();

        List<CustomEnchant> enchants = getEnchants(player);

        int pages = Math.max(1, (enchants.size() + maxItemsPerPage - 1) / maxItemsPerPage);

        if (page >= pages) page = pages - 1;

        boolean levels = ExpUtils.isLevels(Config.Setting.DISENCHANTER_COST_TYPE.getString());

        int cost = Math.max(0, Config.Setting.DISENCHANTER_COST.getInt());

        String type = levels ? "levels" : "points";

        drawFrame();

        inventory.setItem(PREVIEW, preview(held, enchants.size()));

        shown = new ArrayList<>();

        for (int i = 0; i < maxItemsPerPage; i++) {

            index = maxItemsPerPage * page + i;

            if (index >= enchants.size()) break;

            CustomEnchant enchant = enchants.get(index);

            shown.add(enchant);

            List<String> lore = new ArrayList<>(enchant.getDescription());

            if (!lore.isEmpty()) lore.add("");

            lore.add("&9Applies to: &b" + MiscUtils.describeTypes(enchant.getAppliesTo()));
            lore.add("&9Removal cost: " + (cost > 0 ? "&b" + cost + " " + type : "&aFree"));
            lore.add("");
            lore.add("&e&lClick &7to remove it from the item");
            lore.add("&7and get the book back");

            inventory.setItem(SLOTS[i], MiscUtils.makeItem(MiscUtils.getIcon(enchant.getAppliesTo()), enchant.getLoreLine(), lore));
        }

        if (enchants.isEmpty()) {

            inventory.setItem(22, MiscUtils.makeItem(Material.BARRIER, "&cNothing to remove", Arrays.asList(
                    "&7Hold a single item that has",
                    "&7custom enchants and open this again"
            )));
        }

        int balance = levels ? player.getLevel() : ExpUtils.getTotal(player);

        inventory.setItem(INFO, MiscUtils.makeItem(Material.EXPERIENCE_BOTTLE, "&aExperience", Arrays.asList(
                "&7Removal cost: " + (cost > 0 ? "&f" + cost + " " + type : "&aFree"),
                "&7You have: &f" + balance + " " + type
        )));

        inventory.setItem(CLOSE, MiscUtils.makeItem(Material.BARRIER, "&cClose", null));

        if (page > 0) {
            inventory.setItem(PREVIOUS, MiscUtils.makeItem(Material.ARROW, "&ePrevious Page", Arrays.asList("&7Page &f" + page + "&7/&f" + pages)));
        }

        if (page + 1 < pages) {
            inventory.setItem(NEXT, MiscUtils.makeItem(Material.ARROW, "&eNext Page", Arrays.asList("&7Page &f" + (page + 2) + "&7/&f" + pages)));
        }
    }

    private void drawFrame() {

        Material border = material(Config.Setting.DISENCHANTER_BORDER.getString(), Material.BLACK_STAINED_GLASS_PANE);
        Material filler = material(Config.Setting.DISENCHANTER_FILLER.getString(), Material.GRAY_STAINED_GLASS_PANE);

        ItemStack borderItem = border == null ? null : MiscUtils.makeItem(border, " ", null);
        ItemStack fillerItem = filler == null ? null : MiscUtils.makeItem(filler, " ", null);

        for (int i = 0; i < getSlots(); i++) {

            boolean edge = i < 9 || i >= 45 || i % 9 == 0 || i % 9 == 8;

            inventory.setItem(i, edge ? borderItem : fillerItem);
        }
    }

    private ItemStack preview(ItemStack held, int amount) {

        if (held.getType() == Material.AIR) {
            return MiscUtils.makeItem(Material.BARRIER, "&cNo item in your hand", Arrays.asList("&7Hold the item you want to remove enchants from"));
        }

        ItemStack item = held.clone();

        item.setAmount(1);

        ItemMeta meta = item.getItemMeta();

        List<String> lore = meta != null && meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<String>();

        lore.add("");
        lore.add(ChatUtils.format("&7Custom enchants: &f" + amount));

        if (meta != null) {
            meta.setLore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private Material material(String name, Material fallback) {

        if (name.equalsIgnoreCase("NONE")) return null;

        Material material = Material.matchMaterial(name);

        return material == null || material.isAir() || !material.isItem() ? fallback : material;
    }
}