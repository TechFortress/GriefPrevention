package com.conaxgames;

import com.conaxgames.libraries.LibraryPlugin;
import com.conaxgames.libraries.hooks.HookType;
import com.conaxgames.player.CorePlayer;

import java.util.UUID;

public class ClaimEntryProvider
{

    /**
     * If the player is blocked from entering the claim.
     * @param claimOwner Owner of the claim being entered.
     * @param attemptingToEnter Name of the player attempting to enter.
     * @return true if the attempting player is blocked.
     */
    public boolean isBlocked(UUID claimOwner, String attemptingToEnter)
    {
        if (LibraryPlugin.getInstance().getHookManager().isHooked(HookType.CSUITE) && CorePlugin.getInstance() != null)
        {
            {
                CorePlayer corePlayer = CorePlugin.getInstance().getPlayerManager().getPlayer(claimOwner);
                return corePlayer != null && corePlayer.isIgnoring(attemptingToEnter);
            }
        }
        return false;
    }
}
