package com.thefancychiken.teepeerequests;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TeePeeRequests extends JavaPlugin {
    private HashMap<UUID, UUID> tpaRequests = new HashMap<>();
    private HashMap<UUID, UUID> tpaHereRequests = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("tpa").setExecutor(new TpaCommand());
        this.getCommand("tphere").setExecutor(new TpHereCommand());
        this.getCommand("tpdeny").setExecutor(new TpDenyCommand());
        this.getCommand("tpaccept").setExecutor(new TpAcceptCommand());
        getLogger().info("TeePeeRequests Enabled");
    }

    @Override
    public void onDisable() {
        getLogger().info("TeePeeRequests Disabled");
    }

    private class TpHereCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /tpa <player>");
                    return false;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Player not found or not online.");
                    return false;
                }
                tpaRequests.put(target.getUniqueId(), player.getUniqueId());
                target.sendMessage(ChatColor.GREEN + player.getName() + " has requested for you to teleport to them. Use /tpaccept or /tpdeny.");
                player.sendMessage(ChatColor.GREEN + "Teleport request sent to " + target.getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return false;
            }
        }
    }

    private class TpaCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length != 1) {
                    player.sendMessage(ChatColor.RED + "Usage: /tphere <player>");
                    return false;
                }
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null || !target.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Player not found or not online.");
                    return false;
                }
                tpaHereRequests.put(target.getUniqueId(), player.getUniqueId());
                target.sendMessage(ChatColor.GREEN + player.getName() + " has requested to teleport to you. Use /tpaccept or /tpdeny.");
                player.sendMessage(ChatColor.GREEN + "Teleport request sent to " + target.getName());
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return false;
            }
        }
    }

    private class TpDenyCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID requesterId = tpaRequests.remove(player.getUniqueId());
                if (requesterId == null) {
                    requesterId = tpaHereRequests.remove(player.getUniqueId());
                }
                if (requesterId != null) {
                    Player requester = Bukkit.getPlayer(requesterId);
                    if (requester != null && requester.isOnline()) {
                        requester.sendMessage(ChatColor.RED + player.getName() + " has denied your teleport request.");
                    }
                    player.sendMessage(ChatColor.GREEN + "Teleport request denied.");
                    return true;
                } else {
                    player.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return false;
            }
        }
    }

    private class TpAcceptCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID requesterId = tpaRequests.remove(player.getUniqueId());
                boolean isTpHereRequest = false;
                if (requesterId == null) {
                    requesterId = tpaHereRequests.remove(player.getUniqueId());
                    isTpHereRequest = true;
                }
                if (requesterId != null) {
                    Player requester = Bukkit.getPlayer(requesterId);
                    int requesterLevel = requester.getLevel();
                    if (requester != null && requester.isOnline()) {
                        final Location startLocation = isTpHereRequest ? player.getLocation() : requester.getLocation();
                        final boolean finalIsTpHereRequest = isTpHereRequest;

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (startLocation.getBlock().equals((finalIsTpHereRequest ? player : requester).getLocation().getBlock())) {
                                    if (finalIsTpHereRequest) {
                                        player.sendMessage(ChatColor.GRAY + "Teleporting " + requester.getName() + " in 5 seconds. Stay still.");
                                        requester.sendMessage(ChatColor.GRAY + "You will be teleported in 5 seconds. Stay still.");
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                requester.teleport(player.getLocation());
                                                player.sendMessage(ChatColor.GREEN + requester.getName() + " has been teleported to you!");
                                                requester.sendMessage(ChatColor.GREEN + "You have been teleported to " + player.getName() + "!");
                                                requester.setLevel(requesterLevel - 1);
                                            }
                                        }.runTaskLater(TeePeeRequests.this, 100);
                                    } else {
                                        requester.sendMessage(ChatColor.GRAY + "Teleporting " + requester.getName() + " in 5 seconds. Stay still.");
                                        player.sendMessage(ChatColor.GRAY + "You will be teleported in 5 seconds. Stay still.");
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                player.teleport(requester.getLocation());
                                                player.sendMessage(ChatColor.GREEN + "You have been teleported to " + requester.getName() + "!");
                                                requester.sendMessage(ChatColor.GREEN + player.getName() + " has been teleported to you!");
                                                requester.setLevel(requesterLevel - 1);
                                            }
                                        }.runTaskLater(TeePeeRequests.this, 100);
                                    }
                                } else {
                                    requester.sendMessage(ChatColor.RED + "Movement detected, teleportation cancelled.");
                                    player.sendMessage(ChatColor.RED + "Movement detected, teleportation cancelled.");
                                }
                            }
                        }.runTaskLater(TeePeeRequests.this, 0);
                        return true;
                    } else {
                        player.sendMessage(ChatColor.RED + "Player not found or not online.");
                        return false;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "You have no pending teleport requests.");
                    return false;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return false;
            }
        }
    }
}
