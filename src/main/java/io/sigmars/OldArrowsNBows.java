package io.sigmars;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TippedArrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public final class OldArrowsNBows extends JavaPlugin implements Listener {

    private final boolean DEFAULT_TOGGLE = this.getConfig().getBoolean("general.default_toggle");
    private final boolean PERMISSIONS_ENABLED = this.getConfig().getBoolean("general.permissions_enabled");
    private final boolean TOGGLE_ALLOWED = this.getConfig().getBoolean("no_permissions_plugin.toggle_allowed");
    private final double KNOCKBACK_MULTIPLIER = this.getConfig().getDouble("general.knockback_multiplier");
    private final double VELOCITY_MULTIPLIER = this.getConfig().getDouble("general.velocity_multiplier");
    private final int COOLDOWN_TIME = this.getConfig().getInt("general.cooldown");
    private final int DURABILITY_COST = this.getConfig().getInt("general.durability_cost");
    private final boolean HELP_ALLOWED = this.getConfig().getBoolean("no_permissions_plugin.help_allowed");
    private HashMap<UUID, Long> cooldowns = new HashMap<>();
    private HashMap<UUID, Boolean> toggled = new HashMap<>();

    private void toggleRapidfire(Player target, Boolean sendMsg){
        // Firstly, check if the player is online and if it exists:
        if (target != null){
            if(target.isOnline()){
                // If the toggle hashmap contains the player toggle status:
                if (toggled.containsKey(target.getUniqueId())){
                    // Toggle the rapidfire
                    if(toggled.get(target.getUniqueId())){
                        // if rapidfire is activated
                        toggled.replace(target.getUniqueId(), false);
                        if (sendMsg){
                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.deactivated")));
                        }
                    } else {
                        // if rapidfire is not activated
                        toggled.replace(target.getUniqueId(), true);
                        if(sendMsg){
                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.activated")));
                        }
                    }
                } else {
                    // Create the player toggle status in the hashmap
                    // if there isn't one
                    toggled.put(target.getUniqueId(), DEFAULT_TOGGLE);
                }
            }

        }
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getLogger().info("[OldArrowsNBows] The plugin has been enabled!");
        // Cooldown times "cleaning":
        // Create a task that runs every X ticks to prevent overflow in time differences.
        // (It's a joke, but it cleans the memory, so why not?)
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<UUID, Long> entry : cooldowns.entrySet()) {
                    entry.setValue(System.currentTimeMillis());
                }
            }
        }, 0L, 72000L); // 72000 ticks -> 1 hour in real life
    }

    @Override
    public void onDisable() {
        getServer().getLogger().info("[OldArrowsNBows] The plugin has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if (cmd.getName().equalsIgnoreCase("oldarrowsnbows")){
            // Help command
            getServer().getLogger().info(String.format("Argumentos: %d", args.length));
            if((args.length == 0)|(args.length == 1 && args[0].equalsIgnoreCase("help"))){
                // If the sender has permission to see the plugin help
                if((!PERMISSIONS_ENABLED & HELP_ALLOWED)|
                !(sender instanceof Player)|
                sender.hasPermission("oldarrowsnbows.seehelp")){
                    if(sender instanceof Player && !sender.hasPermission("oldarrowsnbows.*")){
                        // Regular players
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.help_message")));
                        return true;
                    } else {
                        // Admins, moderators, etc.
                        if (sender instanceof Player){
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.help_message_master")));
                        } else {
                            sender.sendMessage(this.getConfig().getString("messages.console_toggle_help"));
                        }
                        return true;
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.no_permission")));
                    return false;
                }
            }
            // Toggle command (regular players)
            if(args.length == 1 && args[0].equalsIgnoreCase("toggle")){
                if(sender instanceof Player){
                    if(sender.hasPermission("oldarrowsnbows.toggle")|(!PERMISSIONS_ENABLED & TOGGLE_ALLOWED)){
                        Player commandPlayer = ((Player) sender).getPlayer();
                        toggleRapidfire(commandPlayer, true);
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.no_permission")));
                        return false;
                    }
                } else {
                    sender.sendMessage(this.getConfig().getString("messages.console_toggle_help"));
                    return false;
                }
            }
            // Toggle command (OP)
            if(args.length == 2 && args[0].equalsIgnoreCase("toggle")){
                if(!(sender instanceof Player)|sender.hasPermission("oldarrows.toggleothers")){
                    Player target = getServer().getPlayer(args[1]);
                    toggleRapidfire(target, false);
                    return true;
                    }
                } else {
                    if(sender instanceof Player){
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.no_permission")));
                    } else {
                        sender.sendMessage(ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.no_permission"))));
                    }
                    return false;
                }
            }
        return false;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        // Remove the cooldown key of a player after leaving the server.
        if (cooldowns.containsKey(event.getPlayer().getUniqueId())) {
            cooldowns.remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onArrowShoot(PlayerInteractEvent event) {
        Player eventPlayer = event.getPlayer();
        if (eventPlayer.getInventory().getItemInMainHand().getType().equals(Material.BOW)) {
            // Create the player toggle status in the hashmap
            // if there isn't one
            if (!(toggled.containsKey(eventPlayer.getUniqueId()))){
                toggled.put(eventPlayer.getUniqueId(), DEFAULT_TOGGLE);
            }
            // Bow rapidfire
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
            &(eventPlayer.hasPermission("oldarrowsnbows.rapidfire")||!PERMISSIONS_ENABLED)
            &(toggled.get(eventPlayer.getUniqueId()))) { // If toggled key == true
                ItemStack itemInMainHand = eventPlayer.getInventory().getItemInMainHand();
                eventPlayer.getInventory().setItemInMainHand(null);
                event.setCancelled(true);
                // Check if the player has any arrow in the inventory
                if (eventPlayer.getInventory().contains(Material.ARROW)
                | eventPlayer.getInventory().contains(Material.SPECTRAL_ARROW)
                | eventPlayer.getInventory().contains(Material.TIPPED_ARROW)) {
                    int i = 0;
                    int arrowIndex = 0;
                    // Cycle through the inventory, stop after finding arrows and store the ItemStack.
                    // This code part could be cleaner... using for(ItemStack item: eventplayer.getInventory().getContents())
                    while (i <= 35) {
                        if (eventPlayer.getInventory().getItem(i) == null) {
                            i++;
                            continue;
                        }
                        if (eventPlayer.getInventory().getItem(i).getType() == Material.ARROW
                        | eventPlayer.getInventory().getItem(i).getType() == Material.TIPPED_ARROW
                        | eventPlayer.getInventory().getItem(i).getType() == Material.SPECTRAL_ARROW) {
                            arrowIndex = i;
                            i = 45;
                        }
                        i++;
                    }
                    ItemStack arrowInv = new ItemStack(eventPlayer.getInventory().getItem(arrowIndex));
                    arrowInv.setAmount(1);
                    // Some cooldown code to prevent arrow spamming:
                    // Create a cooldown key in the HashMap for the player if there isn't one.
                    if (!cooldowns.containsKey(eventPlayer.getUniqueId())) {
                        cooldowns.put(eventPlayer.getUniqueId(), System.currentTimeMillis() - COOLDOWN_TIME);
                    }
                    // Check if the cooldown has ran out. (Current time) - (Last time) > (Cooldown)
                    if (System.currentTimeMillis() - cooldowns.get(eventPlayer.getUniqueId()) > COOLDOWN_TIME) {
                        cooldowns.replace(eventPlayer.getUniqueId(), System.currentTimeMillis());
                        // Consume arrows and reduce bow durability on survival and adventure mode
                        if (eventPlayer.getGameMode() == GameMode.SURVIVAL || eventPlayer.getGameMode() == GameMode.ADVENTURE) {
                            // Unbreaking enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.DURABILITY)) {
                                Random r = new Random();
                                float unbreakingChance = 100 / itemInMainHand.getEnchantmentLevel(Enchantment.DURABILITY) + 1;
                                float random = r.nextFloat() * 100;
                                if (random < unbreakingChance) {
                                    itemInMainHand.setDurability((short) (itemInMainHand.getDurability() + DURABILITY_COST));
                                }
                            } else {
                                itemInMainHand.setDurability((short) (itemInMainHand.getDurability() + DURABILITY_COST));
                            }
                            // Infinity Enchantment
                            if (!itemInMainHand.containsEnchantment(Enchantment.ARROW_INFINITE)) {
                                eventPlayer.getInventory().removeItem(arrowInv);
                            }
                        }
                        eventPlayer.getWorld().playSound(eventPlayer.getLocation(), Sound.ENTITY_ARROW_SHOOT, 1.0F, 1.0F);
                        // Fire the bow
                        // NORMAL ARROWS
                        if (arrowInv.getType().equals(Material.ARROW)) {
                            Arrow a = eventPlayer.launchProjectile(Arrow.class);
                            // Flame enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_FIRE)) {
                                a.setFireTicks(40);
                            }
                            // Punch enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                                // knockback:
                                // knockback_multiplier * [(base knockback) + (knockback enchantment level) * 3]
                                a.setKnockbackStrength((int) ((a.getKnockbackStrength() + itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) * 3) * KNOCKBACK_MULTIPLIER));
                            }
                            // Reduce arrow velocity by 15% to prevent abuse
                            a.setVelocity(a.getVelocity().multiply(VELOCITY_MULTIPLIER));
                            //Disable bouncing
                            a.setBounce(false);
                        }// NORMAL ARROWS

                        // TIPPED ARROWS
                        else if (arrowInv.getType().equals(Material.TIPPED_ARROW)) {
                            PotionMeta meta = (PotionMeta) arrowInv.getItemMeta();
                            TippedArrow a = eventPlayer.launchProjectile(TippedArrow.class);
                            a.setBasePotionData(meta.getBasePotionData());
                            // Flame enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_FIRE)) {
                                a.setFireTicks(40);
                            }
                            // Punch enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                                // knockback:
                                // knockback_multiplier * [(base knockback) + (knockback enchantment level) * 3]
                                a.setKnockbackStrength((int) ((a.getKnockbackStrength() + itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) * 3) * KNOCKBACK_MULTIPLIER));
                            }
                            // Reduce arrow velocity by 15% to prevent abuse
                            a.setVelocity(a.getVelocity().multiply(VELOCITY_MULTIPLIER));
                            //Disable bouncing
                            a.setBounce(false);
                        }// TIPPED ARROWS

                        // SPECTRAL ARROWS
                        else if (arrowInv.getType().equals(Material.SPECTRAL_ARROW)) {
                            SpectralArrow a = eventPlayer.launchProjectile(SpectralArrow.class);
                            // Flame enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_FIRE)) {
                                a.setFireTicks(40);
                            }
                            // Punch enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                                // knockback:
                                // knockback_multiplier * [(base knockback) + (knockback enchantment level) * 3]
                                a.setKnockbackStrength((int) ((a.getKnockbackStrength() + itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) * 3) * KNOCKBACK_MULTIPLIER));
                            }
                            // Reduce arrow velocity by 15% to prevent abuse
                            a.setVelocity(a.getVelocity().multiply(VELOCITY_MULTIPLIER));
                            //Disable bouncing
                            a.setBounce(false);
                        }// SPECTRAL ARROWS
                    }
                }
                if (itemInMainHand.getDurability() < itemInMainHand.getType().getMaxDurability() + 1) {
                    eventPlayer.getInventory().setItemInMainHand(itemInMainHand);
                }
            }
        }
    }
}
