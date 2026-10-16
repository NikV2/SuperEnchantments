package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;

import java.util.List;

public class EnchantCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public EnchantCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "enchant";
    }

    @Override
    protected String getDescription() {
        return "Add an enchant to the item in your hand";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments enchant <enchant>";
    }

    @Override
    protected String getPermission() {
        return Permissions.ADMIN.getPermission();
    }

    @Override
    protected int maxArguments() {
        return 2;
    }

    @Override
    protected boolean canConsoleExecute() {
        return false;
    }

    @Override
    protected void perform(CommandSender sender, String[] args) {
        Player player = (Player) sender;

        CustomEnchant enchant = plugin.getEnchantManager().get(args[1]);

        if (enchant == null) {
            sender.sendMessage(MsgType.UNKNOWN_ENCHANT.getMessage().replace("%enchant%", args[1]));
            return;
        }

        if (!enchant.isEnabled()) {
            sender.sendMessage(MsgType.ENCHANT_DISABLED.getMessage().replace("%enchant%", enchant.getDisplayName()));
            return;
        }

        ItemStack held = player.getInventory().getItemInMainHand();

        if (held.getType() == Material.AIR) {
            sender.sendMessage(MsgType.HOLD_ITEM.getMessage());
            return;
        }

        if (!enchant.appliesTo(held.getType())) {
            sender.sendMessage(MsgType.CANNOT_APPLY.getMessage().replace("%enchant%", enchant.getDisplayName()));
            return;
        }

        player.getInventory().setItemInMainHand(plugin.getEnchantManager().apply(held, enchant));

        sender.sendMessage(MsgType.APPLIED.getMessage().replace("%enchant%", enchant.getDisplayName()));
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length != 2) return null;

        List<String> ids = new ArrayList<>();

        for (CustomEnchant enchant : plugin.getEnchantManager().getAll()) ids.add(enchant.getId());

        return ids;
    }
}
