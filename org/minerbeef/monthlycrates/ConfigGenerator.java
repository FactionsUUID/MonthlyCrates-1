package org.minerbeef.monthlycrates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class ConfigGenerator {

	public ConfigGenerator(FileConfiguration config, ItemStack item, String crateName, String type, int chance,
			boolean giveItem, String command) {

		String path = "crates." + crateName + "." + type + "-rewards";
		path = path + "." + Integer.toString(getFinalKey(config.getConfigurationSection(path)) + 1) + ".";

		config.set(path + "Material", item.getType().name());
		config.set(path + "Amount", item.getAmount());
		config.set(path + "Glow", false);
		if (item.getType().getMaxDurability() == 0) {
			config.set(path + "ItemData", item.getDurability());
		} else {
			config.set(path + "ItemData", -1);
		}
		config.set(path + "GiveItem", giveItem);
		if (item.hasItemMeta()) {
			if (item.getItemMeta().hasDisplayName()) {
				config.set(path + "Name", item.getItemMeta().getDisplayName());
			}
			if (item.getItemMeta().hasLore()) {
				config.set(path + "Lores", item.getItemMeta().getLore());
			}
		}

		List<String> enchantments = new ArrayList<String>();
		for (Enchantment ench : item.getEnchantments().keySet()) {
			enchantments.add(ench.getName() + ":" + Integer.toString(item.getEnchantments().get(ench)));
		}
		config.set(path + "Enchantments", enchantments);
		config.set(path + "Commands", Arrays.asList(command));
		config.set(path + "Chance", chance);
		Core.instance.saveConfig();
		Core.instance.reloadConfig();
		Core.instance.saveConfig();
	}

	public int getFinalKey(ConfigurationSection section) {
		String finalKey = null;
		for (String key : section.getKeys(false)) {
			finalKey = key;
		}
		return Integer.parseInt(finalKey.replace("'", ""));
	}
}
