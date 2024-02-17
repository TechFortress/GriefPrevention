package com.griefprevention.module;

import me.ryanhamshire.GriefPrevention.ClaimsMode;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * {@link Module} adding welcome messages and a small command reference book.
 */
public class WelcomeModule extends Module
{

    WelcomeModule(@NotNull GriefPrevention plugin)
    {
        super(plugin);
    }

    @Override
    public @NotNull String getName()
    {
        return "WelcomeAdvice";
    }

    @Override
    protected @NotNull ModuleConfig createConfig()
    {
        return new WelcomeConfig();
    }

    @Override
    public @NotNull WelcomeConfig getConfig()
    {
        return (WelcomeConfig) super.getConfig();
    }

    @EventHandler
    private void onPlayerJoin(@NotNull PlayerJoinEvent event)
    {
        Player player = event.getPlayer();

        // Returning players and admins likely already know the basics.
        if (player.hasPlayedBefore() || player.hasPermission("griefprevention.adminclaims"))
        {
            return;
        }

        getPlugin().getServer().getScheduler().scheduleSyncDelayedTask(
                getPlugin(),
                () -> {
                    // Re-obtain player in case they logged out and back in before manuals were delivered.
                    Player online = player.getPlayer();
                    if (online == null)
                        return;

                    if (getConfig().sendMessages())
                    {
                        GriefPrevention.sendMessage(online, TextMode.Instr, Messages.AvoidGriefClaimLand);
                        // This is the "survival" video, but it does a bit more basic claim usage explanation.
                        GriefPrevention.sendMessage(online, TextMode.Instr, Messages.SurvivalBasicsVideo2, DataStore.SURVIVAL_VIDEO_URL);
                    }

                    ClaimsMode mode = getPlugin().config_claims_worldModes.get(online.getWorld());
                    if (getConfig().getModesForManuals().contains(mode))
                    {
                        player.getInventory().addItem(getBook());
                    }

                },
                getConfig().getDelayTicks());
    }

    public static @NotNull ItemStack getBook()
    {
        ItemFactory factory = Bukkit.getItemFactory();
        BookMeta meta = Objects.requireNonNull((BookMeta) factory.getItemMeta(Material.WRITTEN_BOOK));

        DataStore datastore = GriefPrevention.instance.dataStore;
        meta.setAuthor(datastore.getMessage(Messages.BookAuthor));
        meta.setTitle(datastore.getMessage(Messages.BookTitle));

        StringBuilder page1 = new StringBuilder();
        String url = datastore.getMessage(Messages.BookLink, DataStore.SURVIVAL_VIDEO_URL);
        String intro = datastore.getMessage(Messages.BookIntro);

        page1.append(url).append("\n\n");
        page1.append(intro).append("\n\n");
        String editToolName = GriefPrevention.instance.config_claims_modificationTool.name().replace('_', ' ').toLowerCase();
        String infoToolName = GriefPrevention.instance.config_claims_investigationTool.name().replace('_', ' ').toLowerCase();
        String configClaimTools = datastore.getMessage(Messages.BookTools, editToolName, infoToolName);
        page1.append(configClaimTools);
        if (GriefPrevention.instance.config_claims_automaticClaimsForNewPlayersRadius < 0)
        {
            page1.append(datastore.getMessage(Messages.BookDisabledChestClaims));
        }

        String page2 = datastore.getMessage(Messages.BookUsefulCommands)
                + """
                
                
                /trust /untrust /trustlist
                /claimslist
                /abandonclaim
                /claim /extendclaim
                
                /ignoreplayer
                
                /subdivideclaims
                /accesstrust
                /containertrust
                /permissiontrust
                """;

        meta.setPages(page1.toString(), page2);

        ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
        item.setItemMeta(meta);
        return item;
    }

}
