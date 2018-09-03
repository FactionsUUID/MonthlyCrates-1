package org.minerbeef.monthlycrates.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.minerbeef.monthlycrates.Core;
import org.minerbeef.monthlycrates.Events;
import org.minerbeef.monthlycrates.Util;

public class FinalAnimationTimer extends BukkitRunnable {

	public static HashMap<String, String> finalAnimation = new HashMap<String, String>();
	public static HashMap<Player, ArrayList<ItemStack>> takenItems = new HashMap<Player, ArrayList<ItemStack>>();

	@Override
	public void run() {
		for (int i = 0; i < 2; i++) {
			for (String key : Events.animCount.keySet()) {
				Events.animationCounter.put(key, Events.animCount.get(key));
			}
			for (String key : Events.animationCounter.keySet()) {
				Integer slot = Events.animationCounter.get(key);
				String[] split = key.split(":");
				String playerName = split[0];
				Integer specialNumber = Integer.parseInt(split[1]);
				Player player = Bukkit.getPlayer(playerName);
				Inventory inv = player.getOpenInventory().getTopInventory();
				String path = "crates." + Events.opening.get(player.getName()) + ".";
				String guiPath = path + "gui.animation";
				String rewardPath = path + "normal-rewards";
				rewardPath = rewardPath + "." + Integer.toString(generateReward(rewardPath)) + ".";
				ItemStack itemstack = item(rewardPath);

				changePanes(guiPath, player, true);
				inv.setItem(slot, itemstack);
				Events.animCount.remove(key);
				Events.animCount.put(playerName + ":" + String.valueOf(specialNumber - 1), slot);
				if (specialNumber - 1 <= 0) {
					if (Core.getInstance().getConfig().getBoolean("once-per-item") == true) {
						if (!takenItems.containsKey(player)) {
							takenItems.put(player, new ArrayList<ItemStack>());
						}
						for (int o = 0; o < 1000; o++) {
							rewardPath = path + "normal-rewards";
							rewardPath = rewardPath + "." + Integer.toString(generateReward(rewardPath)) + ".";
							itemstack = item(rewardPath);
							if (!takenItems.get(player).contains(itemstack)) {
								break;
							}
						}
						takenItems.get(player).add(itemstack);
						inv.setItem(slot, itemstack);
					}
					if (Core.getInstance().getConfig().getBoolean(rewardPath + "GiveItem") == true) {
						player.getInventory().addItem(itemstack);
					}
					for (String command : Core.getInstance().getConfig().getStringList(rewardPath + "Commands")) {
						Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(),
								command.replace("%player%", playerName));
					}
					Events.animCount.remove(playerName + ":" + String.valueOf(specialNumber - 1));
				}

				if (animationFinished(inv, player) && !animationStillRunning(player)) {
					finalAnimation.put(player.getName(), "0:8");
					changePanes(guiPath, player, false);
					if (takenItems.containsKey(player)) {
						takenItems.remove(player);
					}
				} else if (!animationFinished(inv, player) && !animationStillRunning(player)) {
					changePanes(guiPath, player, false);
				}
			}
			Events.animationCounter.clear();
		}
	}

	public static ItemStack item(String path) {
		ItemStack itemstack = null;
		if (Material.valueOf(Core.getInstance().getConfig().getString(path + "Material")).equals(Material.SKULL_ITEM)) {
			itemstack = Util.createItemStackSkull(Core.getInstance().getConfig().getString(path + "SkullOwner"),
					Core.getInstance().getConfig().getInt(path + "Amount"),
					Core.getInstance().getConfig().getString(path + "Name"),
					Core.getInstance().getConfig().getStringList(path + "Lores"));
		} else {
			itemstack = Util.createItemStack(
					Material.valueOf(Core.getInstance().getConfig().getString(path + "Material")),
					Core.getInstance().getConfig().getInt(path + "Amount"),
					Core.getInstance().getConfig().getString(path + "Name"),
					Core.getInstance().getConfig().getBoolean(path + "Glow"),
					Core.getInstance().getConfig().getInt(path + "ItemData"),
					Core.getInstance().getConfig().getStringList(path + "Lores"));
			for (String ench : Core.getInstance().getConfig().getStringList(path + "Enchantments")) {
				Enchantment enchant = Enchantment.getByName(ench.split(":")[0]);
				int level = Integer.parseInt(ench.split(":")[1]);
				itemstack.addUnsafeEnchantment(enchant, level);
			}

			if (itemstack.getType().getMaxDurability() == 0) {
				itemstack.setDurability((short) 0);
			}
		}
		return itemstack;
	}

	public static Integer generateReward(String path) {
		int chances = 0;
		ArrayList<Integer> chancesSplit = new ArrayList<Integer>();
		for (String item : Core.getInstance().getConfig().getConfigurationSection(path).getKeys(false)) {
			chances = Core.getInstance().getConfig().getInt(path + "." + item + ".Chance") + chances;
			chancesSplit.add(Core.getInstance().getConfig().getInt(path + "." + item + ".Chance"));
		}
		int randInt = Util.randInt(1, chances);
		int i = 0;
		for (Integer chance : chancesSplit) {
			int lower = 0;
			int higher = 0;
			if (i != chancesSplit.size() - 1) {
				if (i == 0) {
					lower = 0;
					higher = chance;
				} else {
					for (int o = i; o > -1; o--) {
						lower = lower + chancesSplit.get(o);
					}
					higher = lower + chancesSplit.get(i + 1);
				}
			}
			if (randInt <= higher && randInt >= lower || i == chancesSplit.size() - 1) {
				break;
			}
			i++;
		}
		return i;
	}

	public static void changePanes(String apath, Player player, boolean changeAll) {
		Inventory inv = player.getOpenInventory().getTopInventory();
		int n = 0;
		for (ItemStack animationItem : inv.getContents()) {
			if (animationItem.hasItemMeta()) {
				if (animationItem.getItemMeta().hasDisplayName()) {
					if (animationItem.getItemMeta().getDisplayName().equals(" ")) {
						if (changeAll == true) {
							inv.setItem(n, Util.makeGUIPane(Material.STAINED_GLASS_PANE, dyeColor(apath), 1, " ",
									Core.getInstance().getConfig().getBoolean(apath + ".Glow"), null));
						} else {
							inv.setItem(n, Util.makeGUIPane(Material.STAINED_GLASS_PANE,
									DyeColor.valueOf(
											Core.getInstance().getConfig().getString(apath + ".animation-off-color")),
									1, " ", Core.getInstance().getConfig().getBoolean(apath + ".Glow"), null));
						}
					}
				}
			}
			n++;
		}
	}

	public boolean animationStillRunning(Player player) {
		for (String string : Events.animCount.keySet()) {
			if (string.split(":")[0].equals(player.getName())) {
				return true;
			}
		}
		return false;
	}

	public boolean animationFinished(Inventory inv, Player player) {
		for (ItemStack item : inv.getContents()) {
			String path = "crates." + Events.opening.get(player.getName()) + ".gui.not-redeemed.Name";
			if (Util.c(Core.getInstance().getConfig().getString(path)).equals(item.getItemMeta().getDisplayName())) {
				return false;
			}
		}
		return true;
	}

	public static DyeColor dyeColor(String apath) {
		List<String> colors = Core.getInstance().getConfig().getStringList(apath + ".animation-on-colors");
		int size = colors.size();
		int element = Util.randInt(0, size - 1);
		return DyeColor.valueOf(colors.get(element));
	}
}
