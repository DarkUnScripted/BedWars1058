package com.andrei1058.bedwars.arena.feature;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.configuration.ConfigPath;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.events.shop.ShopBuyEvent;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import java.util.LinkedList;

public class SpoilPlayerTNTFeature {

    private static SpoilPlayerTNTFeature instance;
    private final LinkedList<Player> playersWithTnt = new LinkedList<>();

    private SpoilPlayerTNTFeature() {
        Bukkit.getPluginManager().registerEvents(new TNTListener(), BedWars.plugin);
        Bukkit.getScheduler().runTaskTimer(BedWars.plugin, new ParticleTask(), 20, 1L);
    }

    public static void init() {
        if (BedWars.config.getBoolean(ConfigPath.GENERAL_CONFIGURATION_PERFORMANCE_SPOIL_TNT_PLAYERS)) {
            if (instance == null) {
                instance = new SpoilPlayerTNTFeature();
            }
        }
    }

    private static class ParticleTask implements Runnable {

        @Override
        public void run() {
            for (Player player : instance.playersWithTnt) {
                if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
                    BedWars.nms.playRedStoneDot(player);
                }
            }
        }
    }

    private static class TNTListener implements Listener {

        @EventHandler
        public void onLeave(PlayerLeaveArenaEvent event){
            instance.playersWithTnt.remove(event.getPlayer());
        }

        @EventHandler(ignoreCancelled = true)
        public void onPickUp(PlayerPickupItemEvent event) {
            if (event.getItem().getItemStack().getType() == Material.TNT) {
                IArena arena = Arena.getArenaByPlayer(event.getPlayer());
                if (arena != null) {
                    if (instance.playersWithTnt.contains(event.getPlayer())) {
                        return;
                    }
                    instance.playersWithTnt.add(event.getPlayer());
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onDrop(PlayerDropItemEvent event) {
            if (event.getItemDrop().getItemStack().getType() == Material.TNT) {
                IArena arena = Arena.getArenaByPlayer(event.getPlayer());
                if (arena != null) {
                    if (instance.playersWithTnt.contains(event.getPlayer())) {
                        if (!event.getPlayer().getInventory().contains(Material.TNT)) {
                            instance.playersWithTnt.remove(event.getPlayer());
                        }
                    }
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void onPlace(BlockPlaceEvent event) {
            ItemStack inHand = event.getItemInHand();
            if (inHand.getType() == Material.TNT) {
                IArena arena = Arena.getArenaByPlayer(event.getPlayer());
                if (arena != null) {
                    if (instance.playersWithTnt.contains(event.getPlayer())) {
                        Bukkit.getScheduler().runTaskLater(BedWars.plugin,
                                () -> {
                                    if (!event.getPlayer().getInventory().contains(Material.TNT)) {
                                        instance.playersWithTnt.remove(event.getPlayer());
                                    }
                                }, 1L);
                    }
                }
            }
        }

        @EventHandler(ignoreCancelled = true)
        public void inventorySwitch(InventoryCloseEvent event) {
            if (event.getPlayer() == null) return;
            if (instance.playersWithTnt.contains(event.getPlayer())) {
                if (!event.getPlayer().getInventory().contains(Material.TNT)) {
                    instance.playersWithTnt.remove(event.getPlayer());
                }
            } else {
                if (event.getPlayer().getInventory().contains(Material.TNT)) {
                    instance.playersWithTnt.add((Player) event.getPlayer());
                }
            }
        }

        @EventHandler
        public void onBuy(ShopBuyEvent event) {
            if (event.getBuyer().getInventory().contains(Material.TNT)) {
                if (!instance.playersWithTnt.contains(event.getBuyer())) {
                    instance.playersWithTnt.add(event.getBuyer());
                }
            }
        }
    }
}
