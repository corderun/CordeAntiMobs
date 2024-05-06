package org.corderun.cordeantimobs;

import org.bukkit.plugin.java.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import org.bukkit.command.*;
import java.util.*;
import org.bukkit.event.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.*;

public final class CordeAntiMobs extends JavaPlugin implements Listener
{
    private final Set<Player> antiMobsModePlayers;
    private final Map<String, Long> cooldowns;

    public CordeAntiMobs() {
        this.antiMobsModePlayers = new HashSet<Player>();
        this.cooldowns = new HashMap<String, Long>();
    }

    public void onEnable() {
        Bukkit.getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.saveDefaultConfig();
        this.getCommand("antimobs").setExecutor((CommandExecutor)new AntiMobsCommand());
        this.getCommand("cordeantimobs").setExecutor((CommandExecutor)new CommandExecutor() {
            public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
                if (args.length == 0) {
                    sender.sendMessage("§2§lCordeAntiMobs §21.1");
                    sender.sendMessage("§fИспользование: /antimobs");
                    return true;
                }
                if (sender.hasPermission("cordeantimobs.reload") && args[0].equals("reload")) {
                    CordeAntiMobs.this.reloadConfig();
                    sender.sendMessage(Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.prefix")).replace("&", "§") + Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.reload")).replace("&", "§"));
                    return true;
                }
                return true;
            }
        });
    }

    @EventHandler
    public void onEntityTarget(final EntityTargetEvent event) {
        if (!(event.getTarget() instanceof Player)) {
            return;
        }
        final Player player = (Player)event.getTarget();
        if (this.antiMobsModePlayers.contains(player)) {
            final String worldName = player.getWorld().getName();
            final List<String> whitelistedWorlds = CordeAntiMobs.this.getConfig().getStringList("whitelist-worlds");
            if (whitelistedWorlds.contains(worldName)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onProjectileHit(final ProjectileHitEvent event) {
        if (event.getEntity().getShooter() instanceof Player && event.getHitEntity() instanceof Monster) {
            final Player player = (Player)event.getEntity().getShooter();
            if (this.antiMobsModePlayers.contains(player)) {
                final String worldName = player.getWorld().getName();
                final List<String> whitelistedWorlds = CordeAntiMobs.this.getConfig().getStringList("whitelist-worlds");
                if (whitelistedWorlds.contains(worldName)) {
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(final EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) {
            return;
        }
        final Player player = (Player)event.getDamager();
        if (this.antiMobsModePlayers.contains(player) && event.getEntity() instanceof Monster) {
            final String worldName = player.getWorld().getName();
            final List<String> whitelistedWorlds = CordeAntiMobs.this.getConfig().getStringList("whitelist-worlds");
            if (whitelistedWorlds.contains(worldName)) {
                event.setCancelled(true);
            }
        }
    }

    private class AntiMobsCommand implements CommandExecutor
    {
        public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
            if (!sender.hasPermission("cordeantimobs.use")) {
                sender.sendMessage(Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.prefix")).replace("&", "§") + Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.no-perm")).replace("&", "§"));
                return true;
            }
            if (args.length == 0) {
                final int COOLDOWN_IN_SECONDS = CordeAntiMobs.this.getConfig().getInt("cooldown");
                final Player player = (Player)sender;
                final String playerName = player.getName();
                if (CordeAntiMobs.this.antiMobsModePlayers.contains(player)) {
                    CordeAntiMobs.this.antiMobsModePlayers.remove(player);
                    CordeAntiMobs.this.cooldowns.put(playerName, System.currentTimeMillis());
                    if (sender.hasPermission("cordeantimobs.bypass")) {
                        CordeAntiMobs.this.cooldowns.clear();
                    }
                    player.sendMessage(Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.prefix")).replace("&", "§") + Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.disable")).replace("&", "§"));
                }
                else {
                    if (CordeAntiMobs.this.cooldowns.containsKey(playerName)) {
                        final long secondsLeft = CordeAntiMobs.this.cooldowns.get(playerName) / 1000L + COOLDOWN_IN_SECONDS - System.currentTimeMillis() / 1000L;
                        if (secondsLeft > 0L) {
                            player.sendMessage(Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.prefix")).replace("&", "§") + Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.cooldown")).replace("&", "§").replace("%time%", String.valueOf(secondsLeft)));
                            return true;
                        }
                    }
                    CordeAntiMobs.this.antiMobsModePlayers.add(player);
                    player.sendMessage(Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.prefix")).replace("&", "§") + Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.enable")).replace("&", "§"));
                }
            }
            if (args.length >= 1) {
                if (args[0].equalsIgnoreCase("off") && !sender.hasPermission("cordeantimobs.admin")) {
                    sender.sendMessage(Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.prefix")).replace("&", "§") + Objects.requireNonNull(CordeAntiMobs.this.getConfig().getString("messages.no-perm")).replace("&", "§"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("off") && sender.hasPermission("cordeantimobs.admin") && args.length == 1) {
                    return true;
                }
                if (args[0].equalsIgnoreCase("off") && sender.hasPermission("cordeantimobs.admin")) {
                    final Player playeroff = Bukkit.getPlayer(args[1]);
                    CordeAntiMobs.this.antiMobsModePlayers.remove(playeroff);
                    return true;
                }
            }
            return true;
        }
    }
}
