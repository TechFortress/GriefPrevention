package com.griefprevention.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
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
     * Offer completions for visible players' names.
     *
     * @param sender the sender
     * @param args the existing command arguments
     * @return the matching players' names
     */
    static @NotNull List<String> visiblePlayers(@Nullable CommandSender sender, @NotNull String @NotNull [] args)
    {
        // Bukkit returns a view of the player list. So that Craftbukkit doesn't have to hack around type limitations,
        // this is actually a view of the player implementation, represented via Bukkit as a generic extending Player.
        // Unfortunately, this leads to our own type limitations. We can work around those by converting to an array,
        // which has the side benefit of allowing us to support every other data type with the same generic method
        // instead of having to have otherwise-identical array and iterable methods.
        Player[] onlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        // Require sender to be able to see a player to complete their name.
        Predicate<Player> canSee = sender instanceof Player senderPlayer ? senderPlayer::canSee : null;
        return complete(onlinePlayers, Player::getName, canSee, args);
    }

    /**
     * Offer completions matching an array of options. Options can be filtered.
     *
     * @param completable the array of completable options
     * @param asString the method for converting an option to a String
     * @param filter the filter to apply, or null for no filtering
     * @param args the existing command arguments
     * @return a {@link List} of all matching completable options in {@code String} form
     * @param <T> the type of the option
     */
    private static <T> @NotNull List<String> complete(
            T @NotNull [] completable,
            @NotNull Function<T, String> asString,
            @Nullable Predicate<T> filter,
            @NotNull String @NotNull [] args)
    {
        // Length should never be 0 because that case should be handled by Bukkit completing the raw command name.
        String prefix = args.length == 0 ? "" : args[args.length - 1];

        List<String> completions = new ArrayList<>();

        for (T element : completable)
        {
            // Require element to be present and match filter, if provided.
            if (element == null || filter != null && !filter.test(element)) continue;

            String string = asString.apply(element);
            // Require element string to be non-empty and start with user's existing text.
            if (string != null
                    && !string.isEmpty()
                    && (prefix.isEmpty() || StringUtil.startsWithIgnoreCase(string, prefix)))
            {
                completions.add(string);
            }
        }

        return completions;
    }

    private TabCompletions() {}

}
