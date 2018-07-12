package vitals2.Steven;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.util.Vector;

public class EventsClass implements Listener {
	
	public static boolean isChainmail(Material m) { return m == Material.CHAINMAIL_BOOTS || m == Material.CHAINMAIL_CHESTPLATE || m == Material.CHAINMAIL_HELMET || m == Material.CHAINMAIL_LEGGINGS; }
	public static Scoreboard sb = Bukkit.getScoreboardManager().getNewScoreboard();
	public static Objective object = sb.registerNewObjective("Player Stats", "dummy");
	public static List<UUID> safeFall = new ArrayList<>();
	
	//get config
	private Main plugin;
	
	public EventsClass(Main pl) {
		plugin = pl;
	}
				
	@EventHandler
	public void negateFall(EntityDamageEvent event) {
		
		Entity entity = event.getEntity();
		
		if (entity instanceof Player) {
			if (event.getCause() == DamageCause.FALL) {
				Material landingOn = entity.getLocation().getBlock().getRelative(BlockFace.DOWN).getType();
				if (safeFall.contains(entity.getUniqueId())) {
					event.setCancelled(true);
					safeFall.remove(entity.getUniqueId());
				}
				else if (landingOn == Material.SPONGE || landingOn == Material.EMERALD_BLOCK) {
					event.setCancelled(true);
					return;
				}
				else
					return;
			}
			else if (((Player) entity).isFlying() && !((HumanEntity) entity).getGameMode().equals(GameMode.CREATIVE)) {
				((Player) entity).setFlying(false);
				safeFall.add(entity.getUniqueId());
			}
			else
				return;
		}
		else return;
	}
	
	@EventHandler
	public void betterChain(EntityDamageEvent event) {
		
		Entity player = event.getEntity();
		
		if (player instanceof Player) {
			if (player.hasPermission("betterchain.v")) {
				if (plugin.getConfig().getBoolean("betterchain")) {
					int numChainmail = 0;
					double damageFactor = 1;
					
					for (int i = 36; i <= 39; i++) {     //checks armor slots
						if (((Player) player).getInventory().getItem(i) != null) { 	//checks if the slot has an item
							if (isChainmail(((Player) player).getInventory().getItem(i).getType())) { 	//checks if that item is a piece of chain mail armor
								numChainmail++;
								damageFactor *= 0.8;
							}
						}
					}
					if (numChainmail > 0) { 	//if the player has more then one piece of armor
						player.sendMessage(ChatColor.AQUA + "Your chainmail armor reduced the damage taken by " + Math.round(100*(1 - damageFactor)) + "%!");
						event.setDamage((int) Math.round(damageFactor * event.getDamage()));
						player.sendMessage("damageFactor = " + damageFactor);
						return;
					}
					else
						return;
				}
				else
					return;				
			}
			else
				return;
		}
		else
			return;
	}
	
	@EventHandler
	public void emeraldsAndsponges(PlayerMoveEvent event) {
		
		Player player = event.getPlayer();
		Vector vector = player.getVelocity();
		Material standingOn = event.getTo().getBlock().getRelative(BlockFace.DOWN).getType();
		
		if (plugin.getConfig().getBoolean("Movement")) {
			//SpeedBoost on Emeralds and jump on sponge
			if (standingOn == Material.EMERALD_BLOCK) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, 5), true);
				return;
			}
			else if (standingOn == Material.SPONGE) {
				vector.setY(2);
				player.setVelocity(vector);
				return;
			}
		}
		else
			return;
	}
	
	/*@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		
		Player player = event.getPlayer();
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookmeta = (BookMeta) book.getItemMeta();
		ArrayList<String> pages = new ArrayList<String>();
		
		bookmeta.setAuthor(ChatColor.RED + "6d7");
		bookmeta.setTitle(ChatColor.RED + "Implemented features.");
		
		pages.add(0,ChatColor.BLACK + "\tImplimented: \nFeatherfly, MytheralChain, FastEmerald, BouncySponge, Announce Day and Night (for future things)");
		pages.add(1,ChatColor.BLACK + "\tThings to add \nDailyRewards \nPermissions \nChatSymbols \nZones");
		pages.add(2,ChatColor.BLACK + "\tFuture additions \nArena \nGuilds (redone) \nHeroism \nCustomSideBar (to display info related to server)");
		bookmeta.setPages(pages);
		book.setItemMeta(bookmeta);
		
		player.getInventory().addItem(book);
	}*/
	
	@EventHandler
	public void Featherfly(PlayerInteractEvent event) {
		
		Action action = event.getAction();
		Player player = event.getPlayer();
		ItemStack held = player.getInventory().getItemInMainHand();
		
		if (player.hasPermission("featherfly.v")) {
			if (plugin.getConfig().getBoolean("FeatherFly")) {
				if ((action.equals(Action.RIGHT_CLICK_AIR) || action.equals(Action.RIGHT_CLICK_BLOCK)) && held.getType() == Material.FEATHER) {
					if (player.isFlying()) {
						player.sendMessage(ChatColor.BLUE + "You are no longer flying.");
						player.setAllowFlight(false);
						player.setFlying(false);
						return;
					}
					if (player.getAllowFlight() && !player.isFlying()) {
						return;
					}
					else if (!player.getAllowFlight()) {
						player.sendMessage(ChatColor.AQUA + "You started to fly.");
						player.setAllowFlight(true);
						player.getLocation().add(0, 2, 0);
						player.setFlying(true);
						held.setAmount(held.getAmount() - 1);
						return;
					}
				}
				else
					return;
			}
			else
				return;
		}
		else
			return;
	}
	
	@EventHandler
	public void blazeCast(PlayerInteractEvent event) {
		
		Player player = event.getPlayer();
		Action action = event.getAction();
		ItemStack held = player.getInventory().getItemInMainHand();
		
		if (player.hasPermission("blazecast.v")) {
			if (plugin.getConfig().getBoolean("Blaze_case")) {
				if (action.equals(Action.LEFT_CLICK_AIR) && held.getType() == Material.BLAZE_ROD) {
					player.launchProjectile(Fireball.class).setIsIncendiary(false);
					held.setAmount(held.getAmount() - 1);
					return;
					}
					else
						return;
			}
		}
		else
			return;
	}
	
	/*@EventHandler
	public void zoneMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		ZoneControl.enterRegion(player);
	}
	
	/*@EventHandler
	public void playerDeath(PlayerDeathEvent event) {
		Player player = ((OfflinePlayer) event).getPlayer();
		WorldTime.deathTracker(player);
	}*/
}