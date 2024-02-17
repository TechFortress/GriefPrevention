package com.griefprevention.module;

import com.griefprevention.util.StringConverters;
import me.ryanhamshire.GriefPrevention.ClaimsMode;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WelcomeConfig extends ModuleConfig
{

    private long delayTicks = 600;
    private boolean sendMessages = true;
    private Set<ClaimsMode> modesForManuals = Set.of(ClaimsMode.Survival, ClaimsMode.SurvivalRequiringClaims);

    @Override
    protected void load(@NotNull ConfigurationSection in, @NotNull ConfigurationSection out)
    {
        super.load(in, out);

        Configuration root = in.getRoot();
        int legacyDelay = 30;
        if (root != null)
            legacyDelay = root.getInt("GriefPrevention.Claims.ManualDeliveryDelaySeconds", legacyDelay);

        String path = "delay";
        delayTicks = in.getInt(path, legacyDelay) * 20L;
        out.set(path, delayTicks / 20);
        out.setComments(path, List.of("Seconds to wait before messaging or delivering manual"));

        path = "SendMessages";
        sendMessages = in.getBoolean(path, true);
        out.set(path, sendMessages);
        out.setComments(path, List.of("Enable sending players basic advice"));

        if (root == null || root.getBoolean("GriefPrevention.Claims.DeliverManuals", true))
            in.addDefault(path, modesForManuals.stream().map(Enum::name).toList());

        path = "ModesForManuals";
        modesForManuals = in.getStringList(path).stream()
                .map(name -> StringConverters.toEnum(ClaimsMode.class, name))
                .filter(Objects::nonNull)
                .filter(mode -> mode != ClaimsMode.Disabled)
                .collect(Collectors.toUnmodifiableSet());
        out.set(path, modesForManuals.stream().map(Enum::name).toList());
        out.setComments(path, List.of("Claim modes to deliver manuals for", "Leave blank to not deliver manuals"));
    }

    @Override
    public boolean enabled()
    {
        // If not sending messages or delivering manuals, module is effectively disabled.
        return super.enabled() && (sendMessages || !getModesForManuals().isEmpty());
    }

    public long getDelayTicks()
    {
        return delayTicks;
    }

    public boolean sendMessages()
    {
        return sendMessages;
    }

    public @NotNull @UnmodifiableView Set<ClaimsMode> getModesForManuals()
    {
        return modesForManuals;
    }
}
