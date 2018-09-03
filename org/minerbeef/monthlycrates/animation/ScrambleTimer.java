package org.minerbeef.monthlycrates.animation;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.minerbeef.monthlycrates.Core;
import org.minerbeef.monthlycrates.Events;
import org.minerbeef.monthlycrates.Util;
import org.minerbeef.monthlycrates.enums.Sounds;

public class ScrambleTimer extends BukkitRunnable {

	public HashMap<String, DyeColor> currentColor = new HashMap<String, DyeColor>();
	public HashMap<String, ArrayList<DyeColor>> takenColors = new HashMap<String, ArrayList<DyeColor>>();
	public HashMap<String, Integer> timesRan = new HashMap<String, Integer>();

	@Override
	public void run() {
		for (String string : FinalAnimationTimer.finalAnimation.keySet()) {
			Player player = Bukkit.getPlayer(string);
			String name = player.getName();
			Inventory inv = player.getOpenInventory().getTopInventory();
			String[] split = FinalAnimationTimer.finalAnimation.get(string).split(":");
			String apath = "crates." + Events.opening.get(name) + ".gui.animation";
			DyeColor color = null;
			if (!takenColors.containsKey(name)) {
				takenColors.put(name, new ArrayList<DyeColor>());
			}
			for (int i = 0; i < 10000; i++) {
				color = FinalAnimationTimer.dyeColor(apath);
				if (takenColors.get(name).contains(color)) {
					continue;
				} else {
					break;
				}
			}
			if (currentColor.containsKey(name)) {
				color = currentColor.get(name);
			} else {
				currentColor.put(name, color);
			}
			ItemStack pane = Util.makeGUIPane(Material.STAINED_GLASS_PANE, color, 1, " ",
					Core.getInstance().getConfig().getBoolean(apath + ".Glow"), null);
			int left = Integer.parseInt(split[0]);
			int right = Integer.parseInt(split[1]);
			for (int i = 0; i < 6; i++) {
				if (inv.getItem(left).hasItemMeta()) {
					if (inv.getItem(left).getItemMeta().hasDisplayName()) {
						if (inv.getItem(left).getItemMeta().getDisplayName().equals(" ")) {
							inv.setItem(left, pane);
						}
					}
				}
				left = left + 9;
				if (inv.getItem(right).hasItemMeta()) {
					if (inv.getItem(right).getItemMeta().hasDisplayName()) {
						if (inv.getItem(right).getItemMeta().getDisplayName().equals(" ")) {
							inv.setItem(right, pane);
						}
					}
				}
				right = right + 9;
			}
			left = left - 54;
			right = right - 54;
			FinalAnimationTimer.finalAnimation.put(string,
					Integer.toString(left + 1) + ":" + Integer.toString(right - 1));
			if (left == 4 && right == 4) {
				FinalAnimationTimer.finalAnimation.put(string, "0:8");
				currentColor.remove(name);
				player.playSound(player.getLocation(),
						Sounds.valueOf(Core.getInstance().getConfig().getString("sounds.Countdown").toUpperCase())
								.bukkitSound(),
						1.0f, 1.0f);
				takenColors.get(name).add(color);
				if (!timesRan.containsKey(name)) {
					timesRan.put(name, 1);
				} else {
					timesRan.put(name, timesRan.get(name) + 1);
				}
				if (timesRan.get(name) == Core.getInstance().getConfig().getInt(apath + ".FinalAnimationRuns")) {
					takenColors.remove(name);
					timesRan.remove(name);
					FinalAnimationTimer.finalAnimation.remove(string);
					FinalAnimationTimer.changePanes(apath, player, false);
					String notPath = "crates." + Events.opening.get(name) + ".gui.not-redeemed.";
					inv.setItem(49,
							Util.createItemStack(
									Material.valueOf(Core.getInstance().getConfig().getString(notPath + "Material")), 1,
									Core.getInstance().getConfig().getString(notPath + "Name"),
									Core.getInstance().getConfig().getBoolean(notPath + "Glow"),
									Core.getInstance().getConfig().getInt(notPath + "ItemData"),
									Core.getInstance().getConfig().getStringList(notPath + "Lores")));
				}
			}
		}
	}
}
