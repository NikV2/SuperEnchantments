package me.nik.superenchantments.commands;

import me.nik.superenchantments.SuperEnchantments;
import me.nik.superenchantments.commands.subcommands.BookCommand;
import me.nik.superenchantments.commands.subcommands.EnchantCommand;
import me.nik.superenchantments.commands.subcommands.ListCommand;
import me.nik.superenchantments.commands.subcommands.MenuCommand;
import me.nik.superenchantments.commands.subcommands.ReloadCommand;
import me.nik.superenchantments.commands.subcommands.RemoveCommand;
import me.nik.superenchantments.enums.MsgType;
import me.nik.superenchantments.utils.ChatUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandManager implements TabExecutor {

    private final SuperEnchantments plugin;

    private final List<SubCommand> subCommands = new ArrayList<>();

    public CommandManager(SuperEnchantments plugin) {
        this.plugin = plugin;
        this.subCommands.add(new ReloadCommand(plugin));
        this.subCommands.add(new ListCommand(plugin));
        this.subCommands.add(new EnchantCommand(plugin));
        this.subCommands.add(new RemoveCommand(plugin));
        this.subCommands.add(new BookCommand(plugin));
        this.subCommands.add(new MenuCommand(plugin));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        if (args.length > 0) {

            for (SubCommand subCommand : this.subCommands) {

                if (args[0].equalsIgnoreCase(subCommand.getName())) {

                    if (!subCommand.canConsoleExecute() && sender instanceof ConsoleCommandSender) {

                        sender.sendMessage(MsgType.CONSOLE_COMMANDS.getMessage());

                        return true;
                    }

                    if (!sender.hasPermission(subCommand.getPermission())) {

                        sender.sendMessage(MsgType.NO_PERMISSION.getMessage());

                        return true;
                    }

                    if (args.length < subCommand.maxArguments()) {

                        helpMessage(sender);

                        return true;
                    }

                    subCommand.perform(sender, args);

                    return true;
                }

                if (args[0].equalsIgnoreCase("help")) {

                    helpMessage(sender);

                    return true;
                }
            }

        } else {

            sender.sendMessage(MsgType.PREFIX.getMessage() + ChatUtils.format(
                    "&fThis server is running &b"
                            + this.plugin.getDescription().getName()
                            + " &fversion &bv" + this.plugin.getDescription().getVersion()
                            + " &fby Nik"
            ));

            return true;
        }

        helpMessage(sender);

        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {

        if (args.length < 2) {
            return this.subCommands.stream().filter(subCommand -> sender.hasPermission(subCommand.getPermission()))
                    .map(SubCommand::getName).collect(Collectors.toList());
        }

        for (SubCommand subCommand : this.subCommands) {

            if (args[0].equalsIgnoreCase(subCommand.getName()) && sender.hasPermission(subCommand.getPermission())) {
                return subCommand.getSubcommandArguments(sender, args);
            }
        }

        return null;
    }

    private void helpMessage(CommandSender sender) {
        sender.sendMessage("");
        sender.sendMessage(MsgType.PREFIX.getMessage() + ChatColor.WHITE + "Available Commands");
        sender.sendMessage("");

        this.subCommands.stream()
                .filter(subCommand -> sender.hasPermission(subCommand.getPermission()))
                .forEach(subCommand ->
                        sender.sendMessage(ChatColor.AQUA + subCommand.getSyntax() + ChatColor.DARK_GRAY + " - "
                                + ChatColor.GRAY + subCommand.getDescription()));

        sender.sendMessage("");
    }
}
