package me.prisonranksx.lists;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public interface PrestigesTextList {
    void setup();

    void send(CommandSender sender, @Nullable String pageNumber);

    void sendList(CommandSender sender);

    void sendPagedList(CommandSender sender, String pageNumber);
}
