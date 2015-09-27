package net.techcable.phanatic.ftweaks.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.techcable.phanatic.ftweaks.FTweaks;

import lombok.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

@RequiredArgsConstructor
public class LootProtectionListener implements Listener {
    private final FTweaks plugin;

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        Player defender = (Player) event.getEntity();
        setLastAttacker(defender, attacker.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent event) {
        if (!plugin.getSettings().isProtectLoot()) return;
        World world = event.getEntity().getWorld();
        Location deathLocation = event.getEntity().getLocation();
        List<Item> items = new ArrayList<>(event.getDrops().size());
        // Bukkit gives us no way to actually get the dropped item entities
        // We have to clear the drops and drop them ourselves
        for (ItemStack toDrop : event.getDrops()) {
            Item itemEntity = world.dropItemNaturally(deathLocation, toDrop);
            items.add(itemEntity);
        }
        event.getDrops().clear(); // Prevent dropping twice
        UUID lastAttacker = getLastAttacker(event.getEntity());
        if (lastAttacker == null) return; // No one attacked this player last
        for (Item item : items) {
            setOwner(item, lastAttacker); // The last attacker owns the drops
        }
        setLastAttacker(event.getEntity(), null); // Clear the last attacker so future deaths aren't affected
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemPickup(PlayerPickupItemEvent event) {
        if (!plugin.getSettings().isProtectLoot()) return;
        int secondsExisted = event.getItem().getTicksLived() * 20;
        if (secondsExisted > plugin.getSettings().getProtectLootTime()) return; // This loot is no longer protected
        UUID owningPlayer = getOwner(event.getItem());
        if (owningPlayer == null) return; // This loot is not owned
        if (!owningPlayer.equals(event.getPlayer().getUniqueId())) {
            event.setCancelled(true); // Not the owner
        }
    }

    //
    // Metadata Delegates
    //

    public static final String LOOT_OWNER_KEY = "owner";
    public static final String LAST_ATTACKER_KEY = "lastAttacker";

    public UUID getOwner(Item item) {
        for (MetadataValue meta : item.getMetadata(LOOT_OWNER_KEY)) {
            if (meta.getOwningPlugin() != plugin) continue;
            return (UUID) meta.value();
        }
        return null;
    }

    public void setOwner(Item item, UUID owner) {
        if (owner != null) {
            MetadataValue meta = new FixedMetadataValue(plugin, owner);
            item.setMetadata(LOOT_OWNER_KEY, meta);
        } else {
            item.removeMetadata(LOOT_OWNER_KEY, plugin);
        }
    }

    public UUID getLastAttacker(Player player) {
        for (MetadataValue meta : player.getMetadata(LAST_ATTACKER_KEY)) {
            if (meta.getOwningPlugin() != plugin) continue;
            return (UUID) meta.value();
        }
        return null;
    }

    public void setLastAttacker(Player player, UUID lastAttacker) {
        if (lastAttacker != null) {
            MetadataValue meta = new FixedMetadataValue(plugin, lastAttacker);
            player.setMetadata(LAST_ATTACKER_KEY, meta);
        } else {
            player.removeMetadata(LAST_ATTACKER_KEY, plugin);
        }
    }
}
