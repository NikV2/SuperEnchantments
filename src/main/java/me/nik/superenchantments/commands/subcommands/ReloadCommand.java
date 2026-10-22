package me.nik.superenchantments.commands.subcommands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.SubCommand;
import me.nik.superenchantments.enums.Permissions;
import me.nik.superenchantments.enums.MsgType;
import org.bukkit.command.CommandSender;

import java.util.List;

public class ReloadCommand extends SubCommand {

    private final SuperEnchantments plugin;

    public ReloadCommand(SuperEnchantments plugin) {
        this.plugin = plugin;
    }

    @Override
    protected String getName() {
        return "reload";
    }

    @Override
    protected String getDescription() {
        return "Reload the plugin";
    }

    @Override
    protected String getSyntax() {
        return "/superenchantments reload";
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
        plugin.getConfiguration().setup();
        plugin.getLang().reload();
        plugin.getEnchantManager().reload();
        plugin.getEnchanterManager().reload();
        sender.sendMessage(MsgType.RELOADED.getMessage());
    }

    @Override
    protected List<String> getSubcommandArguments(CommandSender sender, String[] args) {
        return null;
    }
}
