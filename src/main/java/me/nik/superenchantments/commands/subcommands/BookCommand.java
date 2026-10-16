package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import org.bukkit.command.CommandSender;

import java.util.List;

public class BookCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public BookCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "book";
    }

    @Override
    protected String getDescription() {
        return "Give an enchant book";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments book <enchant> [player]";
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
        return true;
    }

    @Override
    protected void perform(CommandSender sender, String[] args) {
        CustomEnchant enchant = plugin.getEnchantManager().get(args[1]);

        if (enchant == null) {
            sender.sendMessage(MsgType.UNKNOWN_ENCHANT.getMessage().replace("%enchant%", args[1]));
            return;
        }

        if (!enchant.isEnabled()) {
            sender.sendMessage(MsgType.ENCHANT_DISABLED.getMessage().replace("%enchant%", enchant.getDisplayName()));
            return;
        }

        Player target;

        if (args.length > 2) {

            target = Bukkit.getPlayerExact(args[2]);

            if (target == null) {
                sender.sendMessage(MsgType.PLAYER_NOT_FOUND.getMessage());
                return;
            }

        } else if (sender instanceof Player) {

            target = (Player) sender;

        } else {

            sender.sendMessage(MsgType.CONSOLE_COMMANDS.getMessage());
            return;
        }

        for (ItemStack left : target.getInventory().addItem(plugin.getEnchantManager().createBook(enchant)).values()) {
            target.getWorld().dropItem(target.getLocation(), left);
        }

        sender.sendMessage(MsgType.BOOK_GIVEN.getMessage()
                .replace("%player%", target.getName())
                .replace("%enchant%", enchant.getDisplayName()));
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) {

            List<String> ids = new ArrayList<>();

            for (CustomEnchant enchant : plugin.getEnchantManager().getAll()) ids.add(enchant.getId());

            return ids;
        }

        if (args.length == 3) {

            List<String> names = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());

            return names;
        }

        return null;
    }
}
