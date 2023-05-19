package me.ryanhamshire.GriefPrevention;

import me.ryanhamshire.GriefPrevention.events.PreventPvPEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Llama;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Mule;
import org.bukkit.entity.Panda;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Rabbit;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WaterMob;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.function.Supplier;

public class EntityDamageHandler implements Listener
{

    private final @NotNull DataStore dataStore;
    private final @NotNull GriefPrevention instance;
    private final @NotNull NamespacedKey luredByPlayer;

    EntityDamageHandler(@NotNull DataStore dataStore, @NotNull GriefPrevention plugin)
    {
        this.dataStore = dataStore;
        instance = plugin;
        luredByPlayer = new NamespacedKey(plugin, "lured_by_player");
    }

    // Tag passive animals that can become aggressive so we can tell whether or not they are hostile later
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityTarget(EntityTargetEvent event)
    {
        if (!GriefPrevention.instance.claimsEnabledForWorld(event.getEntity().getWorld())) return;

        EntityType entityType = event.getEntityType();
        if (entityType != EntityType.HOGLIN && entityType != EntityType.POLAR_BEAR)
            return;

        if (event.getReason() == EntityTargetEvent.TargetReason.TEMPT)
            event.getEntity().getPersistentDataContainer().set(luredByPlayer, PersistentDataType.BYTE, (byte) 1);
        else
            event.getEntity().getPersistentDataContainer().remove(luredByPlayer);

    }

    //when an entity is damaged
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageEvent event)
    {
        this.handleEntityDamageEvent(event, true);
    }

    //when an entity is set on fire
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityCombustByEntity(EntityCombustByEntityEvent event)
    {
        //handle it just like we would an entity damge by entity event, except don't send player messages to avoid double messages
        //in cases like attacking with a flame sword or flame arrow, which would ALSO trigger the direct damage event handler

        EntityDamageByEntityEvent eventWrapper = new EntityDamageByEntityEvent(event.getCombuster(), event.getEntity(), EntityDamageEvent.DamageCause.FIRE_TICK, event.getDuration());
        this.handleEntityDamageEvent(eventWrapper, false);
        event.setCancelled(eventWrapper.isCancelled());
    }

    private void handleEntityDamageEvent(EntityDamageEvent event, boolean sendErrorMessagesToPlayers)
    {
        //monsters are never protected
        if (isMonster(event.getEntity())) return;

        //horse protections can be disabled
        if (event.getEntity() instanceof Horse && !GriefPrevention.instance.config_claims_protectHorses) return;
        if (event.getEntity() instanceof Donkey && !GriefPrevention.instance.config_claims_protectDonkeys) return;
        if (event.getEntity() instanceof Mule && !GriefPrevention.instance.config_claims_protectDonkeys) return;
        if (event.getEntity() instanceof Llama && !GriefPrevention.instance.config_claims_protectLlamas) return;
        //protected death loot can't be destroyed, only picked up or despawned due to expiration
        if (event.getEntityType() == EntityType.DROPPED_ITEM)
        {
            if (event.getEntity().hasMetadata("GP_ITEMOWNER"))
            {
                event.setCancelled(true);
            }
        }

        // Handle environmental damage to tamed animals that could easily be caused maliciously.
        if (handlePetDamageByEnvironment(event)) return;

        if (handleBlockExplosionDamage(event)) return;

        //the rest is only interested in entities damaging entities (ignoring environmental damage)
        if (!(event instanceof EntityDamageByEntityEvent)) return;

        EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;

        if (subEvent.getDamager() instanceof LightningStrike && subEvent.getDamager().hasMetadata("GP_TRIDENT"))
        {
            event.setCancelled(true);
            return;
        }
        //determine which player is attacking, if any
        Player attacker = null;
        Projectile arrow = null;
        Entity damageSource = subEvent.getDamager();

        if (damageSource != null)
        {
            if (damageSource.getType() == EntityType.PLAYER)
            {
                attacker = (Player) damageSource;
            }
            else if (damageSource instanceof Projectile)
            {
                arrow = (Projectile) damageSource;
                if (arrow.getShooter() instanceof Player)
                {
                    attacker = (Player) arrow.getShooter();
                }
            }

            //protect players from lingering potion damage when protected from pvp
            if (damageSource.getType() == EntityType.AREA_EFFECT_CLOUD && event.getEntityType() == EntityType.PLAYER && GriefPrevention.instance.pvpRulesApply(event.getEntity().getWorld()))
            {
                Player damaged = (Player) event.getEntity();
                PlayerData damagedData = GriefPrevention.instance.dataStore.getPlayerData(damaged.getUniqueId());

                //case 1: recently spawned
                if (GriefPrevention.instance.config_pvp_protectFreshSpawns && damagedData.pvpImmune)
                {
                    event.setCancelled(true);
                    return;
                }

                //case 2: in a pvp safe zone
                else
                {
                    Claim damagedClaim = GriefPrevention.instance.dataStore.getClaimAt(damaged.getLocation(), false, damagedData.lastClaim);
                    if (damagedClaim != null)
                    {
                        damagedData.lastClaim = damagedClaim;
                        if (GriefPrevention.instance.claimIsPvPSafeZone(damagedClaim))
                        {
                            PreventPvPEvent pvpEvent = new PreventPvPEvent(damagedClaim, attacker, damaged);
                            Bukkit.getPluginManager().callEvent(pvpEvent);
                            if (!pvpEvent.isCancelled())
                            {
                                event.setCancelled(true);
                            }
                            return;
                        }
                    }
                }
            }
        }

        //if the attacker is a player and defender is a player (pvp combat)
        if (attacker != null && event.getEntityType() == EntityType.PLAYER && GriefPrevention.instance.pvpRulesApply(attacker.getWorld()))
        {
            //FEATURE: prevent pvp in the first minute after spawn, and prevent pvp when one or both players have no inventory

            Player defender = (Player) (event.getEntity());

            if (attacker != defender)
            {
                PlayerData defenderData = this.dataStore.getPlayerData(((Player) event.getEntity()).getUniqueId());
                PlayerData attackerData = this.dataStore.getPlayerData(attacker.getUniqueId());

                //otherwise if protecting spawning players
                if (GriefPrevention.instance.config_pvp_protectFreshSpawns)
                {
                    if (defenderData.pvpImmune)
                    {
                        event.setCancelled(true);
                        if (sendErrorMessagesToPlayers)
                            GriefPrevention.sendMessage(attacker, TextMode.Err, Messages.ThatPlayerPvPImmune);
                        return;
                    }

                    if (attackerData.pvpImmune)
                    {
                        event.setCancelled(true);
                        if (sendErrorMessagesToPlayers)
                            GriefPrevention.sendMessage(attacker, TextMode.Err, Messages.CantFightWhileImmune);
                        return;
                    }
                }

                //FEATURE: prevent players from engaging in PvP combat inside land claims (when it's disabled)
                if (GriefPrevention.instance.config_pvp_noCombatInPlayerLandClaims || GriefPrevention.instance.config_pvp_noCombatInAdminLandClaims)
                {
                    Claim attackerClaim = this.dataStore.getClaimAt(attacker.getLocation(), false, attackerData.lastClaim);
                    if (!attackerData.ignoreClaims)
                    {
                        if (attackerClaim != null && //ignore claims mode allows for pvp inside land claims
                                !attackerData.inPvpCombat() &&
                                GriefPrevention.instance.claimIsPvPSafeZone(attackerClaim))
                        {
                            attackerData.lastClaim = attackerClaim;
                            PreventPvPEvent pvpEvent = new PreventPvPEvent(attackerClaim, attacker, defender);
                            Bukkit.getPluginManager().callEvent(pvpEvent);
                            if (!pvpEvent.isCancelled())
                            {
                                event.setCancelled(true);
                                if (sendErrorMessagesToPlayers)
                                    GriefPrevention.sendMessage(attacker, TextMode.Err, Messages.CantFightWhileImmune);
                            }
                            return;
                        }

                        Claim defenderClaim = this.dataStore.getClaimAt(defender.getLocation(), false, defenderData.lastClaim);
                        if (defenderClaim != null &&
                                !defenderData.inPvpCombat() &&
                                GriefPrevention.instance.claimIsPvPSafeZone(defenderClaim))
                        {
                            defenderData.lastClaim = defenderClaim;
                            PreventPvPEvent pvpEvent = new PreventPvPEvent(defenderClaim, attacker, defender);
                            Bukkit.getPluginManager().callEvent(pvpEvent);
                            if (!pvpEvent.isCancelled())
                            {
                                event.setCancelled(true);
                                if (sendErrorMessagesToPlayers)
                                    GriefPrevention.sendMessage(attacker, TextMode.Err, Messages.PlayerInPvPSafeZone);
                            }
                            return;
                        }
                    }
                }
            }
        }

        if (event instanceof EntityDamageByEntityEvent)
        {
            //don't track in worlds where claims are not enabled
            if (!GriefPrevention.instance.claimsEnabledForWorld(event.getEntity().getWorld())) return;

            //protect players from being attacked by other players' pets when protected from pvp
            if (event.getEntityType() == EntityType.PLAYER)
            {
                Player defender = (Player) event.getEntity();

                //if attacker is a pet
                Entity damager = subEvent.getDamager();
                if (damager != null && damager instanceof Tameable)
                {
                    Tameable pet = (Tameable) damager;
                    if (pet.isTamed() && pet.getOwner() != null)
                    {
                        //if defender is NOT in pvp combat and not immune to pvp right now due to recent respawn
                        PlayerData defenderData = GriefPrevention.instance.dataStore.getPlayerData(event.getEntity().getUniqueId());
                        if (!defenderData.pvpImmune && !defenderData.inPvpCombat())
                        {
                            //if defender is not in a protected area
                            Claim defenderClaim = this.dataStore.getClaimAt(defender.getLocation(), false, defenderData.lastClaim);
                            if (defenderClaim != null &&
                                    !defenderData.inPvpCombat() &&
                                    GriefPrevention.instance.claimIsPvPSafeZone(defenderClaim))
                            {
                                defenderData.lastClaim = defenderClaim;
                                PreventPvPEvent pvpEvent = new PreventPvPEvent(defenderClaim, attacker, defender);
                                Bukkit.getPluginManager().callEvent(pvpEvent);

                                //if other plugins aren't making an exception to the rule
                                if (!pvpEvent.isCancelled())
                                {
                                    event.setCancelled(true);
                                    if (damager instanceof Creature) ((Creature) damager).setTarget(null);
                                }
                                return;
                            }
                        }
                    }
                }
            }

            //if the damaged entity is a claimed item frame or armor stand, the damager needs to be a player with build trust in the claim
            if (subEvent.getEntityType() == EntityType.ITEM_FRAME
                    || subEvent.getEntityType() == EntityType.GLOW_ITEM_FRAME
                    || subEvent.getEntityType() == EntityType.ARMOR_STAND
                    || subEvent.getEntityType() == EntityType.VILLAGER
                    || subEvent.getEntityType() == EntityType.ENDER_CRYSTAL)
            {
                //allow for disabling villager protections in the config
                if (subEvent.getEntityType() == EntityType.VILLAGER && !GriefPrevention.instance.config_claims_protectCreatures)
                    return;

                //don't protect polar bears, they may be aggressive
                if (subEvent.getEntityType() == EntityType.POLAR_BEAR) return;

                //decide whether it's claimed
                Claim cachedClaim = null;
                PlayerData playerData = null;
                if (attacker != null)
                {
                    playerData = this.dataStore.getPlayerData(attacker.getUniqueId());
                    cachedClaim = playerData.lastClaim;
                }

                Claim claim = this.dataStore.getClaimAt(event.getEntity().getLocation(), false, cachedClaim);

                //if it's claimed
                if (claim != null)
                {
                    //if attacker isn't a player, cancel
                    if (attacker == null)
                    {
                        //exception case
                        if (event.getEntityType() == EntityType.VILLAGER && damageSource != null && damageSource instanceof Zombie)
                        {
                            return;
                        }

                        event.setCancelled(true);
                        return;
                    }

                    //otherwise player must have container trust in the claim
                    Supplier<String> failureReason = claim.checkPermission(attacker, ClaimPermission.Build, event);
                    if (failureReason != null)
                    {
                        event.setCancelled(true);
                        if (sendErrorMessagesToPlayers)
                            GriefPrevention.sendMessage(attacker, TextMode.Err, failureReason.get());
                        return;
                    }
                }
            }

            //if the entity is an non-monster creature (remember monsters disqualified above), or a vehicle
            if (((subEvent.getEntity() instanceof Creature || subEvent.getEntity() instanceof WaterMob) && GriefPrevention.instance.config_claims_protectCreatures))
            {
                //if entity is tameable and has an owner, apply special rules
                if (subEvent.getEntity() instanceof Tameable tameable)
                {
                    AnimalTamer owner = tameable.getOwner();
                    if (tameable.isTamed() && owner != null)
                    {
                        //limit attacks by players to owners and admins in ignore claims mode
                        if (attacker != null)
                        {
                            //if the player interacting is the owner, always allow
                            if (attacker.equals(owner)) return;

                            //allow for admin override
                            PlayerData attackerData = this.dataStore.getPlayerData(attacker.getUniqueId());
                            if (attackerData.ignoreClaims) return;

                            //otherwise disallow in non-pvp worlds (and also pvp worlds if configured to do so)
                            if (!GriefPrevention.instance.pvpRulesApply(subEvent.getEntity().getWorld()) || (GriefPrevention.instance.config_pvp_protectPets && subEvent.getEntityType() != EntityType.WOLF))
                            {
                                String ownerName = GriefPrevention.lookupPlayerName(owner);
                                String message = GriefPrevention.instance.dataStore.getMessage(Messages.NoDamageClaimedEntity, ownerName);
                                if (attacker.hasPermission("griefprevention.ignoreclaims"))
                                    message += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
                                if (sendErrorMessagesToPlayers)
                                    GriefPrevention.sendMessage(attacker, TextMode.Err, message);
                                PreventPvPEvent pvpEvent = new PreventPvPEvent(new Claim(subEvent.getEntity().getLocation(), subEvent.getEntity().getLocation(), null, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), null), attacker, tameable);
                                Bukkit.getPluginManager().callEvent(pvpEvent);
                                if (!pvpEvent.isCancelled())
                                {
                                    event.setCancelled(true);
                                }
                                return;
                            }
                            //and disallow if attacker is pvp immune
                            else if (attackerData.pvpImmune)
                            {
                                event.setCancelled(true);
                                if (sendErrorMessagesToPlayers)
                                    GriefPrevention.sendMessage(attacker, TextMode.Err, Messages.CantFightWhileImmune);
                                return;
                            }
                            // disallow players attacking tamed wolves (dogs) unless under attack by said wolf
                            else if (tameable.getType() == EntityType.WOLF)
                            {
                                if (tameable.getTarget() != null)
                                {
                                    if (tameable.getTarget() == attacker) return;
                                }
                                event.setCancelled(true);
                                String ownerName = GriefPrevention.lookupPlayerName(owner);
                                String message = GriefPrevention.instance.dataStore.getMessage(Messages.NoDamageClaimedEntity, ownerName);
                                if (attacker.hasPermission("griefprevention.ignoreclaims"))
                                    message += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
                                if (sendErrorMessagesToPlayers)
                                    GriefPrevention.sendMessage(attacker, TextMode.Err, message);
                                return;
                            }
                        }
                    }
                }

                Claim cachedClaim = null;
                PlayerData playerData = null;

                //if not a player or an explosive, allow
                //RoboMWM: Or a lingering potion, or a witch
                if (attacker == null
                        && damageSource != null
                        && damageSource.getType() != EntityType.CREEPER
                        && damageSource.getType() != EntityType.WITHER
                        && damageSource.getType() != EntityType.ENDER_CRYSTAL
                        && damageSource.getType() != EntityType.AREA_EFFECT_CLOUD
                        && damageSource.getType() != EntityType.WITCH
                        && !(damageSource instanceof Projectile)
                        && !(damageSource instanceof Explosive)
                        && !(damageSource instanceof ExplosiveMinecart))
                {
                    return;
                }

                if (attacker != null)
                {
                    playerData = this.dataStore.getPlayerData(attacker.getUniqueId());
                    cachedClaim = playerData.lastClaim;
                }

                Claim claim = this.dataStore.getClaimAt(event.getEntity().getLocation(), false, cachedClaim);

                //if it's claimed
                if (claim != null)
                {
                    //if damaged by anything other than a player (exception villagers injured by zombies in admin claims), cancel the event
                    //why exception?  so admins can set up a village which can't be CHANGED by players, but must be "protected" by players.
                    //TODO: Discuss if this should only apply to admin claims...?
                    if (attacker == null)
                    {
                        //exception case
                        if (event.getEntityType() == EntityType.VILLAGER && damageSource != null && (damageSource.getType() == EntityType.ZOMBIE || damageSource.getType() == EntityType.VINDICATOR || damageSource.getType() == EntityType.EVOKER || damageSource.getType() == EntityType.EVOKER_FANGS || damageSource.getType() == EntityType.VEX))
                        {
                            return;
                        }

                        //all other cases
                        else
                        {
                            event.setCancelled(true);
                            if (damageSource instanceof Projectile)
                            {
                                damageSource.remove();
                            }
                        }
                    }

                    //otherwise the player damaging the entity must have permission, unless it's a dog in a pvp world
                    else if (!(event.getEntity().getWorld().getPVP() && event.getEntity().getType() == EntityType.WOLF))
                    {
                        Supplier<String> override = null;
                        if (sendErrorMessagesToPlayers)
                        {
                            final Player finalAttacker = attacker;
                            override = () ->
                            {
                                String message = GriefPrevention.instance.dataStore.getMessage(Messages.NoDamageClaimedEntity, claim.getOwnerName());
                                if (finalAttacker.hasPermission("griefprevention.ignoreclaims"))
                                    message += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
                                return message;
                            };
                        }
                        Supplier<String> noContainersReason = claim.checkPermission(attacker, ClaimPermission.Inventory, event, override);
                        if (noContainersReason != null)
                        {
                            event.setCancelled(true);

                            //kill the arrow to avoid infinite bounce between crowded together animals //RoboMWM: except for tridents
                            if (arrow != null && arrow.getType() != EntityType.TRIDENT) arrow.remove();
                            if (damageSource != null && damageSource.getType() == EntityType.FIREWORK && event.getEntity().getType() != EntityType.PLAYER)
                                return;

                            if (sendErrorMessagesToPlayers)
                            {
                                GriefPrevention.sendMessage(attacker, TextMode.Err, noContainersReason.get());
                            }
                            event.setCancelled(true);
                        }

                        //cache claim for later
                        if (playerData != null)
                        {
                            playerData.lastClaim = claim;
                        }
                    }
                }
            }
        }
    }

    private boolean isMonster(Entity entity)
    {
        if (entity instanceof Monster) return true;

        EntityType type = entity.getType();
        if (type == EntityType.GHAST || type == EntityType.MAGMA_CUBE || type == EntityType.SHULKER)
            return true;

        if (type == EntityType.SLIME)
            return ((Slime) entity).getSize() > 0;

        if (type == EntityType.RABBIT)
            return ((Rabbit) entity).getRabbitType() == Rabbit.Type.THE_KILLER_BUNNY;

        if (type == EntityType.PANDA)
            return ((Panda) entity).getMainGene() == Panda.Gene.AGGRESSIVE;

        if ((type == EntityType.HOGLIN || type == EntityType.POLAR_BEAR) && entity instanceof Mob)
            return !entity.getPersistentDataContainer().has(luredByPlayer, PersistentDataType.BYTE) && ((Mob) entity).getTarget() != null;

        return false;
    }

    /**
     * Handle damage to {@link Tameable} entities by environmental sources.
     *
     * @param event the {@link EntityDamageEvent}
     * @return true if the damage is handled
     */
    private boolean handlePetDamageByEnvironment(@NotNull EntityDamageEvent event)
    {
        if (event.getEntity() instanceof Tameable && !GriefPrevention.instance.pvpRulesApply(event.getEntity().getWorld()))
        {
            Tameable tameable = (Tameable) event.getEntity();
            if (tameable.isTamed())
            {
                EntityDamageEvent.DamageCause cause = event.getCause();
                if (cause != null && (
                        cause == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION ||
                                cause == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                                cause == EntityDamageEvent.DamageCause.FALLING_BLOCK ||
                                cause == EntityDamageEvent.DamageCause.FIRE ||
                                cause == EntityDamageEvent.DamageCause.FIRE_TICK ||
                                cause == EntityDamageEvent.DamageCause.LAVA ||
                                cause == EntityDamageEvent.DamageCause.SUFFOCATION ||
                                cause == EntityDamageEvent.DamageCause.CONTACT ||
                                cause == EntityDamageEvent.DamageCause.DROWNING))
                {
                    event.setCancelled(true);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Handles entity damage caused by block explosions.
     *
     * @param event the EntityDamageEvent
     * @return true if the damage has been handled
     */
    private boolean handleBlockExplosionDamage(EntityDamageEvent event)
    {
        if (event.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION) return false;

        Entity entity = event.getEntity();

        // Skip players - does allow players to use block explosions to bypass PVP protections,
        // but also doesn't disable self-damage.
        if (entity instanceof Player) return false;

        Claim claim = GriefPrevention.instance.dataStore.getClaimAt(entity.getLocation(), false, null);

        // Only block explosion damage inside claims.
        if (claim == null) return false;

        event.setCancelled(true);
        return true;
    }

    //when an entity is damaged
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDamageMonitor(EntityDamageEvent event)
    {
        //FEATURE: prevent players who very recently participated in pvp combat from hiding inventory to protect it from looting
        //FEATURE: prevent players who are in pvp combat from logging out to avoid being defeated

        if (event.getEntity().getType() != EntityType.PLAYER) return;

        Player defender = (Player) event.getEntity();

        //only interested in entities damaging entities (ignoring environmental damage)
        if (!(event instanceof EntityDamageByEntityEvent)) return;

        //Ignore "damage" from snowballs, eggs, etc. from triggering the PvP timer
        if (event.getDamage() == 0) return;

        EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;

        //if not in a pvp rules world, do nothing
        if (!GriefPrevention.instance.pvpRulesApply(defender.getWorld())) return;

        //determine which player is attacking, if any
        Player attacker = null;
        Projectile arrow = null;
        Entity damageSource = subEvent.getDamager();

        if (damageSource != null)
        {
            if (damageSource.getType() == EntityType.PLAYER)
            {
                attacker = (Player) damageSource;
            }
            else if (damageSource instanceof Projectile)
            {
                arrow = (Projectile) damageSource;
                if (arrow.getShooter() instanceof Player)
                {
                    attacker = (Player) arrow.getShooter();
                }
            }
        }

        //if attacker not a player, do nothing
        if (attacker == null) return;

        PlayerData defenderData = this.dataStore.getPlayerData(defender.getUniqueId());
        PlayerData attackerData = this.dataStore.getPlayerData(attacker.getUniqueId());

        if (attacker != defender)
        {
            long now = Calendar.getInstance().getTimeInMillis();
            defenderData.lastPvpTimestamp = now;
            defenderData.lastPvpPlayer = attacker.getName();
            attackerData.lastPvpTimestamp = now;
            attackerData.lastPvpPlayer = defender.getName();
        }
    }

    //when a vehicle is damaged
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onVehicleDamage(VehicleDamageEvent event)
    {
        //all of this is anti theft code
        if (!GriefPrevention.instance.config_claims_preventTheft) return;

        //input validation
        if (event.getVehicle() == null) return;

        //don't track in worlds where claims are not enabled
        if (!GriefPrevention.instance.claimsEnabledForWorld(event.getVehicle().getWorld())) return;

        //determine which player is attacking, if any
        Player attacker = null;
        Entity damageSource = event.getAttacker();
        EntityType damageSourceType = null;

        //if damage source is null or a creeper, don't allow the damage when the vehicle is in a land claim
        if (damageSource != null)
        {
            damageSourceType = damageSource.getType();

            if (damageSource.getType() == EntityType.PLAYER)
            {
                attacker = (Player) damageSource;
            }
            else if (damageSource instanceof Projectile)
            {
                Projectile arrow = (Projectile) damageSource;
                if (arrow.getShooter() instanceof Player)
                {
                    attacker = (Player) arrow.getShooter();
                }
            }
        }

        //if not a player and not an explosion, always allow
        if (attacker == null && damageSourceType != EntityType.CREEPER && damageSourceType != EntityType.WITHER && damageSourceType != EntityType.PRIMED_TNT)
        {
            return;
        }

        //NOTE: vehicles can be pushed around.
        //so unless precautions are taken by the owner, a resourceful thief might find ways to steal anyway
        Claim cachedClaim = null;
        PlayerData playerData = null;

        if (attacker != null)
        {
            playerData = this.dataStore.getPlayerData(attacker.getUniqueId());
            cachedClaim = playerData.lastClaim;
        }

        Claim claim = this.dataStore.getClaimAt(event.getVehicle().getLocation(), false, cachedClaim);

        //if it's claimed
        if (claim != null)
        {
            //if damaged by anything other than a player, cancel the event
            if (attacker == null)
            {
                event.setCancelled(true);
            }

            //otherwise the player damaging the entity must have permission
            else
            {
                final Player finalAttacker = attacker;
                Supplier<String> override = () ->
                {
                    String message = GriefPrevention.instance.dataStore.getMessage(Messages.NoDamageClaimedEntity, claim.getOwnerName());
                    if (finalAttacker.hasPermission("griefprevention.ignoreclaims"))
                        message += "  " + GriefPrevention.instance.dataStore.getMessage(Messages.IgnoreClaimsAdvertisement);
                    return message;
                };
                Supplier<String> noContainersReason = claim.checkPermission(attacker, ClaimPermission.Inventory, event, override);
                if (noContainersReason != null)
                {
                    event.setCancelled(true);
                    GriefPrevention.sendMessage(attacker, TextMode.Err, noContainersReason.get());
                }

                //cache claim for later
                if (playerData != null)
                {
                    playerData.lastClaim = claim;
                }
            }
        }
    }

    //when a splash potion effects one or more entities...
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onPotionSplash(PotionSplashEvent event)
    {
        ThrownPotion potion = event.getPotion();

        ProjectileSource projectileSource = potion.getShooter();
        // Ignore potions with no source.
        if (projectileSource == null) return;
        Player thrower = null;
        if ((projectileSource instanceof Player))
            thrower = (Player) projectileSource;
        boolean messagedPlayer = false;

        Collection<PotionEffect> effects = potion.getEffects();
        for (PotionEffect effect : effects)
        {
            PotionEffectType effectType = effect.getType();

            // Restrict some potions on claimed villagers and animals.
            // Griefers could use potions to kill entities or steal them over fences.
            if (PotionEffectType.HARM.equals(effectType)
                    || PotionEffectType.POISON.equals(effectType)
                    || PotionEffectType.JUMP.equals(effectType)
                    || PotionEffectType.WITHER.equals(effectType))
            {
                Claim cachedClaim = null;
                for (LivingEntity affected : event.getAffectedEntities())
                {
                    // Always impact the thrower.
                    if (affected == thrower) continue;

                    if (affected.getType() == EntityType.VILLAGER || affected instanceof Animals)
                    {
                        Claim claim = this.dataStore.getClaimAt(affected.getLocation(), false, cachedClaim);
                        if (claim != null)
                        {
                            cachedClaim = claim;

                            if (thrower == null)
                            {
                                // Non-player source: Witches, dispensers, etc.
                                if (!EntityEventHandler.isBlockSourceInClaim(projectileSource, claim))
                                {
                                    // If the source is not a block in the same claim as the affected entity, disallow.
                                    event.setIntensity(affected, 0);
                                }
                            }
                            else
                            {
                                // Source is a player. Determine if they have permission to access entities in the claim.
                                Supplier<String> override = () -> instance.dataStore.getMessage(Messages.NoDamageClaimedEntity, claim.getOwnerName());
                                final Supplier<String> noContainersReason = claim.checkPermission(thrower, ClaimPermission.Inventory, event, override);
                                if (noContainersReason != null)
                                {
                                    event.setIntensity(affected, 0);
                                    if (!messagedPlayer)
                                    {
                                        GriefPrevention.sendMessage(thrower, TextMode.Err, noContainersReason.get());
                                        messagedPlayer = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            //Otherwise, ignore potions not thrown by players
            if (thrower == null) return;

            //otherwise, no restrictions for positive effects
            if (positiveEffects.contains(effectType)) continue;

            for (LivingEntity effected : event.getAffectedEntities())
            {
                //always impact the thrower
                if (effected == thrower) continue;

                //always impact non players
                if (effected.getType() != EntityType.PLAYER) continue;

                    //otherwise if in no-pvp zone, stop effect
                    //FEATURE: prevent players from engaging in PvP combat inside land claims (when it's disabled)
                else if (GriefPrevention.instance.config_pvp_noCombatInPlayerLandClaims || GriefPrevention.instance.config_pvp_noCombatInAdminLandClaims)
                {
                    Player effectedPlayer = (Player) effected;
                    PlayerData defenderData = this.dataStore.getPlayerData(effectedPlayer.getUniqueId());
                    PlayerData attackerData = this.dataStore.getPlayerData(thrower.getUniqueId());
                    Claim attackerClaim = this.dataStore.getClaimAt(thrower.getLocation(), false, attackerData.lastClaim);
                    if (attackerClaim != null && GriefPrevention.instance.claimIsPvPSafeZone(attackerClaim))
                    {
                        attackerData.lastClaim = attackerClaim;
                        PreventPvPEvent pvpEvent = new PreventPvPEvent(attackerClaim, thrower, effectedPlayer);
                        Bukkit.getPluginManager().callEvent(pvpEvent);
                        if (!pvpEvent.isCancelled())
                        {
                            event.setIntensity(effected, 0);
                            if (!messagedPlayer)
                            {
                                GriefPrevention.sendMessage(thrower, TextMode.Err, Messages.CantFightWhileImmune);
                                messagedPlayer = true;
                            }
                        }
                        continue;
                    }

                    Claim defenderClaim = this.dataStore.getClaimAt(effectedPlayer.getLocation(), false, defenderData.lastClaim);
                    if (defenderClaim != null && GriefPrevention.instance.claimIsPvPSafeZone(defenderClaim))
                    {
                        defenderData.lastClaim = defenderClaim;
                        PreventPvPEvent pvpEvent = new PreventPvPEvent(defenderClaim, thrower, effectedPlayer);
                        Bukkit.getPluginManager().callEvent(pvpEvent);
                        if (!pvpEvent.isCancelled())
                        {
                            event.setIntensity(effected, 0);
                            if (!messagedPlayer)
                            {
                                GriefPrevention.sendMessage(thrower, TextMode.Err, Messages.PlayerInPvPSafeZone);
                                messagedPlayer = true;
                            }
                        }
                    }
                }
            }
        }
    }

    public static final HashSet<PotionEffectType> positiveEffects = new HashSet<>(Arrays.asList
            (
                    PotionEffectType.ABSORPTION,
                    PotionEffectType.DAMAGE_RESISTANCE,
                    PotionEffectType.FAST_DIGGING,
                    PotionEffectType.FIRE_RESISTANCE,
                    PotionEffectType.HEAL,
                    PotionEffectType.HEALTH_BOOST,
                    PotionEffectType.INCREASE_DAMAGE,
                    PotionEffectType.INVISIBILITY,
                    PotionEffectType.JUMP,
                    PotionEffectType.NIGHT_VISION,
                    PotionEffectType.REGENERATION,
                    PotionEffectType.SATURATION,
                    PotionEffectType.SPEED,
                    PotionEffectType.WATER_BREATHING
            ));

}
