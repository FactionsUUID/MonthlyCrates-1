package org.minerbeef.monthlycrates;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.minerbeef.monthlycrates.animation.FinalAnimationTimer;
import org.minerbeef.monthlycrates.enums.Sounds;

public class Events implements Listener {

	public static HashMap<String, String> opening = new HashMap<String, String>();
	public static HashMap<String, Integer> animationCounter = new HashMap<String, Integer>();
	public static HashMap<String, Integer> animCount = new HashMap<String, Integer>();
	public static ArrayList<Player> inStorageInv = new ArrayList<Player>();

	@EventHandler
	public void inventoryClick(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		Inventory inv = event.getView().getTopInventory();
		int slot = event.getRawSlot();
		if (inStorageInv.contains(player)) {
			event.setCancelled(true);
		} else if (opening.containsKey(player.getName()) || opening.containsKey(player.getName() + "isStarting")) {
			event.setCancelled(true);
			String path;
			if (opening.containsKey(player.getName() + "isStarting")) {
				path = "crates." + opening.get(player.getName() + "isStarting") + ".gui.";
			} else {
				path = "crates." + opening.get(player.getName()) + ".gui.";
			}
			if (event.getCurrentItem() != null) {
				if (event.getCurrentItem().hasItemMeta() && event.getCurrentItem().getItemMeta().hasDisplayName()) {
					if (Util.c(Core.getInstance().getConfig().getString(path + "not-redeemed.Name"))
							.equals(event.getCurrentItem().getItemMeta().getDisplayName())) {
						if (opening.containsKey(player.getName() + "isStarting")) {
							opening.put(player.getName(), opening.get(player.getName() + "isStarting"));
							opening.remove(player.getName() + "isStarting");

							// 50% of completed removal method

							// Removing Crate from Inventory
							int crateItem = player.getInventory().first(Material.ENDER_CHEST);
							int crateAmount = player.getInventory().getItem(crateItem).getAmount();
							if (crateAmount == 1) {
								player.getInventory().clear(crateItem);
							} else {
								player.getInventory().getItem(crateItem).setAmount(crateAmount - 1);
							}
						}

						if (slot != 49) {
							animCount.put(player.getName() + ":" + Core.getInstance().getConfig()
									.getString(path + "animation.ScrambleAnimationRunsPerItem"), slot);
						} else {
							String rewardPath = "crates." + Events.opening.get(player.getName()) + ".final-rewards.";
							rewardPath = rewardPath + "."
									+ Integer.toString(FinalAnimationTimer.generateReward(rewardPath)) + ".";
							ItemStack finalReward = FinalAnimationTimer.item(rewardPath);
							inv.setItem(49, finalReward);
							if (Core.getInstance().getConfig().getBoolean(rewardPath + "GiveItem") == true) {
								player.getInventory().addItem(finalReward);
							}
							for (String command : Core.getInstance().getConfig()
									.getStringList(rewardPath + "Commands")) {
								Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
										command.replace("%player%", player.getName()));
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event) {
		Player player = event.getPlayer();
		if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			if (player.getItemInHand() != null) {
				ItemStack hand = player.getItemInHand();
				if (hand.hasItemMeta()) {
					if (hand.getItemMeta().hasDisplayName()) {
						for (String key : Core.getInstance().getConfig().getConfigurationSection("crates")
								.getKeys(false)) {
							if (hand.getItemMeta().getDisplayName().equals(Util
									.c(Core.getInstance().getConfig().getString("crates." + key + ".crate.Name")))) {
								event.setCancelled(true);
								opening.put(player.getName() + "isStarting", key);
								player.openInventory(monthlyCrate(player.getName(), Core.getInstance().getConfig()
										.getString("crates." + key + ".gui.Name").replace("%name%", key)));
								player.playSound(player.getLocation(),
										Sounds.valueOf(
												Core.getInstance().getConfig().getString("sounds.Open").toUpperCase())
												.bukkitSound(),
										1.0f, 1.0f);
							}
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void inventoryCloseEvent(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		Inventory inv = player.getOpenInventory().getTopInventory();
		if (inStorageInv.contains(player)) {
			inStorageInv.remove(player);
		}
		if (opening.containsKey(player.getName() + "isStarting")) {
			opening.remove(player.getName() + "isStarting");
		} else if (opening.containsKey(player.getName())) {
			String playerName = player.getName();
			String name = inv.getItem(49).getItemMeta().getDisplayName();
			String notPath = "crates." + opening.get(playerName) + ".gui.not-redeemed.";
			String finalPath = "crates." + opening.get(playerName) + ".gui.final-not-redeemable.";
			if (name.equals(Util.c(Core.getInstance().getConfig().getString(notPath + "Name")))
					|| name.equals(Util.c(Core.getInstance().getConfig().getString(finalPath + "Name")))) {
				openInventory(player, inv);
			} else {
				opening.remove(player.getName());
			}
			player.playSound(player.getLocation(), Sounds
					.valueOf(Core.getInstance().getConfig().getString("sounds.Close").toUpperCase()).bukkitSound(),
					1.0f, 1.0f);
		}
	}

	public void openInventory(Player player, Inventory inv) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Core.instance, new Runnable() {
			public void run() {
				player.openInventory(inv);
			}
		}, 1);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (inStorageInv.contains(player)) {
			inStorageInv.remove(player);
		}
		if (opening.containsKey(player.getName())) {
			opening.remove(player.getName());
			for (String string : animationCounter.keySet()) {
				if (string.contains(player.getName())) {
					animationCounter.remove(string);
				}
			}
		} else if (opening.containsKey(player.getName() + "isStarting")) {
			opening.remove(player.getName() + "isStarting");
		}
	}

	public Inventory monthlyCrate(String playerName, String name) {
		Inventory inv = Bukkit.createInventory(null, 54, Util.c(name));
		String string = opening.get(playerName + "isStarting");
		String notPath = "crates." + string + ".gui.not-redeemed.";
		String finalPath = "crates." + string + ".gui.final-not-redeemable.";
		String animPath = "crates." + string + ".gui.animation.animation-off-color";
		String glowPath = "crates." + string + ".gui.animation.Glow";
		for (int i = 12; i < 33; i = i + 9) {
			for (int o = 0; o < 3; o++) {
				inv.setItem(i + o,
						Util.createItemStack(
								Material.valueOf(Core.getInstance().getConfig().getString(notPath + "Material")), 1,
								Core.getInstance().getConfig().getString(notPath + "Name"),
								Core.getInstance().getConfig().getBoolean(notPath + "Glow"),
								Core.getInstance().getConfig().getInt(notPath + "ItemData"),
								Core.getInstance().getConfig().getStringList(notPath + "Lores")));
			}
		}
		inv.setItem(49,
				Util.makeGUIPane(Material.valueOf(Core.getInstance().getConfig().getString(finalPath + "Material")),
						DyeColor.valueOf(Core.getInstance().getConfig().getString(finalPath + "PaneColor")), 1,
						Core.getInstance().getConfig().getString(finalPath + "Name"),
						Core.getInstance().getConfig().getBoolean(finalPath + "Glow"),
						Core.getInstance().getConfig().getStringList(finalPath + "Lores")));
		for (int i = 0; i < 54; i++) {
			if (inv.getItem(i) == null) {
				inv.setItem(i,
						Util.makeGUIPane(Material.STAINED_GLASS_PANE,
								DyeColor.valueOf(Core.getInstance().getConfig().getString(animPath)), 1, " ",
								Core.getInstance().getConfig().getBoolean(glowPath), null));
			}
		}
		return inv;
	}
}