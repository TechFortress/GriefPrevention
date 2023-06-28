package com.griefprevention.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A container for tab completion helper methods.
 */
final class TabCompletions
{

    /**
     * Offer tab completions for visible players' names.
     *
     * @param sender the sender
     * @param args the existing command arguments
     * @return the matching players' names
     */
    static @NotNull List<String> visiblePlayers(@Nullable CommandSender sender, @NotNull String @NotNull [] args)
    {
        Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        Predicate<Player> canSee = sender instanceof Player senderPlayer ? senderPlayer::canSee : null;
        return complete(onlinePlayers, Player::getName, canSee, lastArg(args));
    }

    private static <T> @NotNull List<String> complete(
            T @NotNull [] completable,
            @NotNull Function<T, String> asString,
            @Nullable Predicate<T> filter,
            @Nullable String prefix)
    {
        List<String> completions = new ArrayList<>();

        for (T element : completable)
        {
            if (element == null || filter != null && !filter.test(element)) continue;

            String string = asString.apply(element);
            if (string != null && startsWithIgnoreCase(string, prefix))
            {
                completions.add(string);
            }
        }

        return completions;
    }

    private static @Nullable String lastArg(@NotNull String @NotNull [] args)
    {
        if (args.length < 1) return null;
        return args[args.length - 1];
    }

    private static boolean startsWithIgnoreCase(@NotNull String string, @Nullable String prefix)
    {
        if (prefix == null || prefix.isEmpty()) return true;
        if (prefix.length() > string.length()) return false;
        return string.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    private TabCompletions() {}

}
