package com.astraldomain.server;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class AstralDomainPlugin extends JavaPlugin implements Listener, CommandExecutor {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        if (getCommand("spawn") == null || getCommand("setspawn") == null) {
            throw new IllegalStateException("Plugin commands are missing from plugin.yml");
        }
        getCommand("spawn").setExecutor(this);
        getCommand("setspawn").setExecutor(this);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(message("join", event.getPlayer().getName()));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(message("quit", event.getPlayer().getName()));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setspawn")) {
            return setSpawn(sender);
        }
        if (command.getName().equalsIgnoreCase("spawn")) {
            return teleportToSpawn(sender);
        }
        return false;
    }

    private boolean setSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(message("player-only", null));
            return true;
        }

        Location location = player.getLocation();
        getConfig().set("spawn.world", location.getWorld().getName());
        getConfig().set("spawn.x", location.getX());
        getConfig().set("spawn.y", location.getY());
        getConfig().set("spawn.z", location.getZ());
        getConfig().set("spawn.yaw", location.getYaw());
        getConfig().set("spawn.pitch", location.getPitch());
        saveConfig();
        player.sendMessage(message("spawn-set", player.getName()));
        return true;
    }

    private boolean teleportToSpawn(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(message("player-only", null));
            return true;
        }
        if (!getConfig().isString("spawn.world")) {
            player.sendMessage(message("spawn-unset", player.getName()));
            return true;
        }

        World world = Bukkit.getWorld(getConfig().getString("spawn.world"));
        if (world == null) {
            player.sendMessage(message("spawn-world-unavailable", player.getName()));
            return true;
        }

        Location spawn = new Location(
                world,
                getConfig().getDouble("spawn.x"),
                getConfig().getDouble("spawn.y"),
                getConfig().getDouble("spawn.z"),
                (float) getConfig().getDouble("spawn.yaw"),
                (float) getConfig().getDouble("spawn.pitch"));
        if (player.teleport(spawn)) {
            player.sendMessage(message("spawn-success", player.getName()));
        } else {
            player.sendMessage(message("spawn-teleport-failed", player.getName()));
        }
        return true;
    }

    private String message(String key, String playerName) {
        String text = getConfig().getString("messages." + key);
        if (text == null) {
            getLogger().warning("Missing configured message: messages." + key);
            text = "&c[Missing message: " + key + "]";
        }
        if (playerName != null) {
            text = text.replace("%player%", playerName);
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
