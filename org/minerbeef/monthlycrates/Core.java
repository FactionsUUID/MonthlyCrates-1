package org.minerbeef.monthlycrates;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.minerbeef.monthlycrates.animation.FinalAnimationTimer;
import org.minerbeef.monthlycrates.animation.ScrambleTimer;

public class Core extends JavaPlugin {

	public static Core instance;
	public String prefix;

	public void onEnable() {
		instance = this;
		File file = new File(getDataFolder(), "config.yml");
		if (!file.exists()) {
			getConfig().options().copyDefaults(true);
			saveDefaultConfig();
		}
		saveConfig();
		Bukkit.getServer().getPluginManager().registerEvents(new Events(), this);
		prefix = Util.c(getConfig().getString("messages.prefix"));
		new ScrambleTimer().runTaskTimer(this, 1, getConfig().getInt("time.ScrambleAnimationSpeedInTicks"));
		new FinalAnimationTimer().runTaskTimer(this, 1, getConfig().getInt("time.FinalAnimationSpeedInTicks"));
	}

	public static Core getInstance() {
		return instance;
	}

	public boolean intIsNull(Integer integer) {
		if (integer == null) {
			return true;
		}
		return false;
	}

	public void sendUsageMessage(CommandSender sender) {
		sender.sendMessage(Util.c(getConfig().getString("messages.invalid-usage").replace("%prefix%", prefix)));
	}

	public boolean isCrate(String crateName) {
		for (String key : getConfig().getConfigurationSection("crates").getKeys(false)) {
			if (key.equals(crateName)) {
				return true;
			}
		}
		return false;
	}

	public Inventory crateStorage() {
		List<ItemStack> itemstacks = new ArrayList<ItemStack>();
		for (String crateName : getConfig().getConfigurationSection("crates").getKeys(false)) {
			List<String> lores = new ArrayList<String>();
			for (String string : getConfig().getStringList("crates." + crateName + ".crate.Lores")) {
				lores.add(string.replace("%player%", "Console"));
			}
			ItemStack item = Util.createItemStack(
					Material.valueOf(getConfig().getString("crates." + crateName + ".crate.Material")), 1,
					getConfig().getString("crates." + crateName + ".crate.Name").replace("%player%", "Console"),
					getConfig().getBoolean("crates." + crateName + ".crate.Glow"),
					getConfig().getInt("crates." + crateName + ".crate.ItemData"), lores);
			itemstacks.add(item);
		}
		Inventory inv = Bukkit.createInventory(null, getConfig().getInt("storage-inv.Size"), Util
				.c(getConfig().getString("storage-inv.Name").replace("%crate#%", Integer.toString(itemstacks.size()))));
		for (ItemStack item : itemstacks) {
			inv.addItem(item);
		}
		return inv;
	}

	public void giveCrate(String crateName, Player player, Integer amount, CommandSender sender) {
		List<String> lores = new ArrayList<String>();
		for (String string : getConfig().getStringList("crates." + crateName + ".crate.Lores")) {
			lores.add(string.replace("%player%", player.getName()));
		}
		player.getInventory()
				.addItem(Util.createItemStack(
						Material.valueOf(getConfig().getString("crates." + crateName + ".crate.Material")), amount,
						getConfig().getString("crates." + crateName + ".crate.Name"),
						getConfig().getBoolean("crates." + crateName + ".crate.Glow"),
						getConfig().getInt("crates." + crateName + ".crate.ItemData"), lores));
		sender.sendMessage(Util.c(
				getConfig().getString("messages.success-give").replace("%prefix%", prefix).replace("%crate%", crateName)
						.replace("%amount%", Integer.toString(amount)).replace("%player%", player.getName())));
		player.sendMessage(Util.c(getConfig().getString("messages.success-give-other").replace("%prefix%", prefix)
				.replace("%crate%", crateName).replace("%amount%", Integer.toString(amount))));
	}

	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (label.equalsIgnoreCase("mcrate")) {
			if (args.length == 0) {
				if (sender.hasPermission("mcrates.storage")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						player.openInventory(this.crateStorage());
						Events.inStorageInv.add(player);
					}
				}
				return true;
			}
			if (sender.hasPermission("mcrates.admin")) {
				if (args[0].equalsIgnoreCase("addItem")) {
					if (sender instanceof Player) {
						Player player = (Player) sender;
						String crateName = args[1];
						String type = args[2].toLowerCase();
						int chance = Integer.parseInt(args[3]);
						boolean giveItem = Boolean.valueOf(args[4]);
						if (this.isCrate(crateName)) {
							if (player.getItemInHand().getType() != Material.AIR) {
								StringBuilder commandBuilder = new StringBuilder();
								for (int i = 5; i < 20; i++) {
									try {
										commandBuilder.append(args[i] + " ");
									} catch (Exception e) {

									}
								}
								String cmd = null;
								try {
									cmd = commandBuilder.deleteCharAt(commandBuilder.length() - 1).toString();
								} catch (Exception e) {
								}
								new ConfigGenerator(getConfig(), player.getItemInHand(), crateName, type, chance,
										giveItem, cmd);
								player.sendMessage(Util
										.c(getConfig().getString("messages.created-item").replace("%prefix%", prefix)));
							} else {
								player.sendMessage(Util.c("&cYou must be holding an item to use this command."));
							}
						}
					}
				}
				if (args.length == 1) {
					if (args[0].equalsIgnoreCase("reload")) {
						reloadConfig();
						saveConfig();
						sender.sendMessage(
								Util.c(getConfig().getString("messages.config-reloaded").replace("%prefix%", prefix)));
					} else if (args[0].equalsIgnoreCase("help")) {
						for (String string : getConfig().getStringList("messages.help-message")) {
							sender.sendMessage(Util.c(string).replace("%prefix%", prefix));
						}
					} else {
						this.sendUsageMessage(sender);
					}
				} else if (args.length == 3) {
					if (args[0].equalsIgnoreCase("giveall")) {
						String crateName = args[1];
						if (this.isCrate(crateName)) {
							if (!this.intIsNull(Integer.parseInt(args[2]))) {
								int amount = Integer.parseInt(args[2]);
								for (Player player : Bukkit.getServer().getOnlinePlayers()) {
									this.giveCrate(crateName, player, amount, sender);
								}
							} else {
								this.sendUsageMessage(sender);
							}
						} else {
							this.sendUsageMessage(sender);
						}
					} else {
						this.sendUsageMessage(sender);
					}
				} else if (args.length == 4) {
					if (args[0].equalsIgnoreCase("give")) {
						if (Bukkit.getPlayer(args[1]) != null) {
							Player player = Bukkit.getPlayer(args[1]);
							String crateName = args[2];
							if (this.isCrate(crateName)) {
								if (!this.intIsNull(Integer.parseInt(args[3]))) {
									int amount = Integer.parseInt(args[3]);
									this.giveCrate(crateName, player, amount, sender);
								} else {
									this.sendUsageMessage(sender);
								}
							} else {
								this.sendUsageMessage(sender);
							}
						} else {
							this.sendUsageMessage(sender);
						}
					}
				}
			} else {
				sender.sendMessage(Util.c(getConfig().getString("messages.no-perms").replace("%prefix%", prefix)));
			}
		}
		return true;
	}
}
