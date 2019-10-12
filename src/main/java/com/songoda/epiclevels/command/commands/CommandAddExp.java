package com.songoda.epiclevels.command.commands;

import com.songoda.epiclevels.EpicLevels;
import com.songoda.epiclevels.command.AbstractCommand;
import com.songoda.epiclevels.players.EPlayer;
import com.songoda.epiclevels.utils.Methods;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;

public class CommandAddExp extends AbstractCommand {

    public CommandAddExp(AbstractCommand parent) {
        super(parent, false, "AddExp");
    }

    @Override
    protected ReturnType runCommand(EpicLevels instance, CommandSender sender, String... args) {
        if (args.length != 3) return ReturnType.SYNTAX_ERROR;

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);

        if (!player.hasPlayedBefore() && !player.isOnline()) {
            instance.getLocale().getMessage("command.general.notonline")
                    .processPlaceholder("notonline", args[1])
                    .sendPrefixedMessage(sender);
            return ReturnType.FAILURE;
        }

        if (!Methods.isInt(args[2]) && !player.isOnline()) {
            instance.getLocale().getMessage("command.general.notint")
                    .processPlaceholder("number", args[2])
                    .sendPrefixedMessage(sender);
            return ReturnType.SYNTAX_ERROR;
        }

        long amount = Long.parseLong(args[2]);
        EPlayer ePlayer = instance.getPlayerManager().getPlayer(player);
        ePlayer.addExperience(amount);
        instance.getDataManager().updatePlayer(ePlayer);

        instance.getLocale().getMessage("command.addexp.success")
                .processPlaceholder("amount", amount)
                .processPlaceholder("player", player.getName())
                .sendPrefixedMessage(sender);

        return ReturnType.SUCCESS;
    }

    @Override
    protected List<String> onTab(EpicLevels instance, CommandSender sender, String... args) {
        return null;
    }

    @Override
    public String getPermissionNode() {
        return "epiclevels.addexp";
    }

    @Override
    public String getSyntax() {
        return "/levels AddExp <Player> <Amount>";
    }

    @Override
    public String getDescription() {
        return "Add experience to a player.";
    }
}
