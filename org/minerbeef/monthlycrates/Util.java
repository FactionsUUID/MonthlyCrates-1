package org.minerbeef.monthlycrates;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Util {

	public static String c(String string) {
		return ChatColor.translateAlternateColorCodes('&', string);
	}

	public static ItemStack createItemStack(Material type, int amt, String name, boolean glow, int data,
			List<String> list) {
		ItemStack stack = null;
		if (data != -1) {
			stack = new ItemStack(type, amt, (short) data);
		} else {
			stack = new ItemStack(type, amt);
		}
		ItemMeta im = stack.getItemMeta();
		if (name != null) {
			im.setDisplayName(Util.c(name));
		}
		if (list != null) {
			ArrayList<String> lore = new ArrayList<String>();
			for (String str : list) {
				lore.add(Util.c(str));
			}
			im.setLore(lore);
		}
		stack.setItemMeta(im);
		if (glow == true) {
			glow(stack);
		}
		return stack;
	}

	public static ItemStack createPotion(PotionEffectType type, int duration, int level) {
		ItemStack potion = new ItemStack(Material.POTION);
		PotionMeta potionmeta = (PotionMeta) potion.getItemMeta();
		potionmeta.setMainEffect(PotionEffectType.SPEED);
		PotionEffect effect = new PotionEffect(type, duration * 20, level);
		potionmeta.addCustomEffect(effect, true);
		potion.setItemMeta(potionmeta);
		return potion;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack createItemStack(Material type, int amt, String name, int data, List<String> lore) {
		ItemStack stack = new org.bukkit.inventory.ItemStack(type, amt);
		ItemMeta im = stack.getItemMeta();
		if (name != null) {
			im.setDisplayName(c(name));
		}
		if (lore != null) {
			ArrayList<String> lorelist = new ArrayList<String>();
			for (String str : lore) {
				lorelist.add(Util.c(str));
			}
			im.setLore(lorelist);
		}
		stack.setItemMeta(im);
		if (data != -1) {
			stack.setTypeId(data);
		}
		return stack;
	}

	public static ItemStack createItemStackSkull(String playerName, int amount, String skullName, List<String> lores) {
		ItemStack stack = new ItemStack(Material.SKULL_ITEM, amount, (short) 3);
		SkullMeta im = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
		im.setOwner(playerName);
		im.setDisplayName(Util.c(skullName));
		if (lores != null) {
			ArrayList<String> lore = new ArrayList<String>();
			for (String str : lores) {
				lore.add(Util.c(str));
			}
			im.setLore(lore);
		}
		stack.setItemMeta(im);
		return stack;
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;

		return randomNum;
	}

	@SuppressWarnings("deprecation")
	public static ItemStack makeGUIPane(Material glasstype, DyeColor color, int amount, String name, boolean glow,
			List<String> lore) {
		ItemStack g = new ItemStack(glasstype, amount, color.getWoolData());
		ItemMeta im = g.getItemMeta();
		im.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
		ArrayList<String> lorelist = new ArrayList<String>();
		if (lore != null) {
			for (int i = 0; i < lore.size(); i++) {
				lorelist.add(ChatColor.translateAlternateColorCodes('&', lore.get(i)));
			}
			im.setLore(lorelist);
		}
		g.setItemMeta(im);
		if (glow == true) {
			glow(g);
		}
		return g;
	}

	public static ItemStack glow(ItemStack itemStack) {
		itemStack.addUnsafeEnchantment(Enchantment.DURABILITY, 0);
		ItemMeta meta = itemStack.getItemMeta();
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);
		itemStack.setItemMeta(meta);
		return itemStack;
	}
}