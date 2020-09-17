package io.github.douglasb2310;

import io.github.douglasb2310.commands.sub.HelpCommand;
import io.github.douglasb2310.commands.sub.ToggleCommand;
import io.github.douglasb2310.utils.CommandHandler;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
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
    private final boolean PERMISSIONS_ENABLED = this.getConfig().getBoolean("general.permissions_enabled");
    private final boolean DEFAULT_TOGGLE = this.getConfig().getBoolean("general.default_toggle");
    private final double KNOCKBACK_MULTIPLIER = this.getConfig().getDouble("general.knockback_multiplier");
    private final double VELOCITY_MULTIPLIER = this.getConfig().getDouble("general.velocity_multiplier");
    private final int COOLDOWN_TIME = this.getConfig().getInt("general.cooldown");
    private final int DURABILITY_COST = this.getConfig().getInt("general.durability_cost");
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Boolean> toggled = new HashMap<>();

    @Override
    public void onDisable() {
        getServer().getLogger().info("[OldArrowsNBows] The plugin has been disabled!");
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLeave(PlayerQuitEvent event) {
        cooldowns.remove(event.getPlayer().getUniqueId());
    }

    public void toggleRapidfire(Player target, Boolean sendMsg) {
        if (target != null) {
            if (target.isOnline()) {
                if (toggled.containsKey(target.getUniqueId())) {
                    if (toggled.get(target.getUniqueId())) {
                        toggled.replace(target.getUniqueId(), false);
                        if (sendMsg) {
                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.deactivated")));
                        }
                    } else {
                        toggled.replace(target.getUniqueId(), true);
                        if (sendMsg) {
                            target.sendMessage(ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("messages.activated")));
                        }
                    }
                } else {
                    toggled.put(target.getUniqueId(), DEFAULT_TOGGLE);
                }
            }

        }
    }

    @Override
    public void onEnable() {
        getServer().getLogger().info("[OldArrowsNBows] The plugin has been enabled!");
        this.saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        // register commands
        CommandHandler handler = new CommandHandler();
        handler.registerCmd("help", new HelpCommand(this));
        handler.registerCmd("toggle", new ToggleCommand(this));
        getCommand("oldarrowsnbows").setExecutor(handler);
        // Cooldown times "cleaning":
        // Create a task that runs every X ticks to prevent overflow in time differences.
        // (really unnecessary, but it cleans the memory, so why not?)
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, () -> {
            for (Map.Entry<UUID, Long> entry : cooldowns.entrySet()) {
                entry.setValue(System.currentTimeMillis());
            }
        }, 0L, 72000L); // 72000 ticks -> 1 IRL hour
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onArrowShoot(PlayerInteractEvent event) {
        Player eventPlayer = event.getPlayer();
        if (eventPlayer.getInventory().getItemInMainHand().getType().equals(Material.BOW)) {
            if (!(toggled.containsKey(eventPlayer.getUniqueId()))) {
                toggled.put(eventPlayer.getUniqueId(), DEFAULT_TOGGLE);
            }
            if ((event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)
            & (eventPlayer.hasPermission("oldarrowsnbows.rapidfire") || !PERMISSIONS_ENABLED)
            & (toggled.get(eventPlayer.getUniqueId()))) {
                ItemStack itemInMainHand = eventPlayer.getInventory().getItemInMainHand();
                eventPlayer.getInventory().setItemInMainHand(null);
                event.setCancelled(true);
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
                                float unbreakingChance = 100.0f / itemInMainHand.getEnchantmentLevel(Enchantment.DURABILITY) + 1;
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
                        // NORMAL ARROWS
                        if (arrowInv.getType().equals(Material.ARROW)) {
                            Arrow a = eventPlayer.launchProjectile(Arrow.class);
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_FIRE)) {
                                a.setFireTicks(40);
                            }
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                                // knockback_multiplier * [(base knockback) + (knockback enchantment level) * 3]
                                a.setKnockbackStrength((int) ((a.getKnockbackStrength() + itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) * 3) * KNOCKBACK_MULTIPLIER));
                            }
                            a.setVelocity(a.getVelocity().multiply(VELOCITY_MULTIPLIER));
                            a.setBounce(false);
                        }// NORMAL ARROWS

                        // TIPPED ARROWS
                        else if (arrowInv.getType().equals(Material.TIPPED_ARROW)) {
                            PotionMeta meta = (PotionMeta) arrowInv.getItemMeta();
                            TippedArrow a = eventPlayer.launchProjectile(TippedArrow.class);
                            a.setBasePotionData(meta.getBasePotionData());
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_FIRE)) {
                                a.setFireTicks(40);
                            }
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                                // knockback_multiplier * [(base knockback) + (knockback enchantment level) * 3]
                                a.setKnockbackStrength((int) ((a.getKnockbackStrength() + itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) * 3) * KNOCKBACK_MULTIPLIER));
                            }
                            a.setVelocity(a.getVelocity().multiply(VELOCITY_MULTIPLIER));
                            a.setBounce(false);
                        }// TIPPED ARROWS

                        // SPECTRAL ARROWS
                        else if (arrowInv.getType().equals(Material.SPECTRAL_ARROW)) {
                            SpectralArrow a = eventPlayer.launchProjectile(SpectralArrow.class);
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_FIRE)) {
                                a.setFireTicks(40);
                            }
                            // Punch enchantment
                            if (itemInMainHand.containsEnchantment(Enchantment.ARROW_KNOCKBACK)) {
                                // knockback_multiplier * [(base knockback) + (knockback enchantment level) * 3]
                                a.setKnockbackStrength((int) ((a.getKnockbackStrength() + itemInMainHand.getEnchantmentLevel(Enchantment.ARROW_KNOCKBACK) * 3) * KNOCKBACK_MULTIPLIER));
                            }
                            a.setVelocity(a.getVelocity().multiply(VELOCITY_MULTIPLIER));
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
