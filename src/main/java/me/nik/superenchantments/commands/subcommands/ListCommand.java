package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.enchants.CustomEnchant;
import me.nik.superenchantments.enums.MsgType;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ListCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public ListCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "list";
    }

    @Override
    protected String getDescription() {
        return "List the registered enchants";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments list";
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
        sender.sendMessage(MsgType.LIST_HEADER.getMessage());

        for (CustomEnchant enchant : plugin.getEnchantManager().getAll()) {
            sender.sendMessage(MsgType.LIST_ENTRY.getMessage()
                    .replace("%enchant%", enchant.getDisplayName())
                    .replace("%state%", enchant.isEnabled() ? "&aenabled" : "&cdisabled"));
        }
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}
