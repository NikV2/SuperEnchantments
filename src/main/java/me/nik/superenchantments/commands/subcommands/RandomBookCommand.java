package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enchants.categories.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.enums.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RandomBookCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public RandomBookCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "randombook";
    }

    @Override
    protected String getDescription() {
        return "Give a random enchantment book";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments randombook [player]";
    }

    @Override
    protected String getPermission() {
        return Permissions.ADMIN.getPermission();
    }

    @Override
    protected int maxArguments() {
        return 1;
    }

    @Override
    protected boolean canConsoleExecute() {
        return true;
    }

    @Override
    protected void perform(CommandSender sender, String[] args) {

        Player target;

        if (args.length < 2) {
            target = (Player) sender;
        } else target = Bukkit.getPlayerExact(args[1]);

        if (target == null) {
            sender.sendMessage(MsgType.PLAYER_NOT_FOUND.getMessage());
            return;
        }

        for (ItemStack left : target.getInventory().addItem(plugin.getEnchanterManager().getRandomEnchantItem()).values()) {
            target.getWorld().dropItem(target.getLocation(), left);
        }

        sender.sendMessage(MsgType.RANDOM_BOOK_GIVEN.getMessage().replace("%player%", target.getName()));
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        if (args.length == 2) {

            List<String> names = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()) names.add(player.getName());

            return names;
        }

        return null;
    }
}