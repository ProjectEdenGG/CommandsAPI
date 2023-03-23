package gg.projecteden.commands.util;

import gg.projecteden.commands.exceptions.postconfigured.InvalidInputException;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemUtils {

	public static boolean isNullOrAir(ItemStack item) {
		return item == null || item.getType() == Material.AIR;
	}

	public static List<Enchantment> getApplicableEnchantments(ItemStack item) {
		List<Enchantment> applicable = new ArrayList<>();
		for (Enchantment enchantment : Enchant.values()) {
			try {
				item = new ItemStack(item.getType());
				item.addEnchantment(enchantment, 1);
				applicable.add(enchantment); // if it gets here it hasn't errored, so its valid
			} catch (Exception ignore) { /* Not applicable, do nothing */ }
		}
		return applicable;
	}

	public static List<ItemStack> fixMaxStackSize(List<ItemStack> items) {
		List<ItemStack> fixed = new ArrayList<>();
		for (ItemStack item : items) {
			if (item == null || item.getType() == Material.AIR)
				continue;

			final Material material = item.getType();

			while (item.getAmount() > material.getMaxStackSize()) {
				final ItemStack replacement = item.clone();
				final int moving = Math.min(material.getMaxStackSize(), item.getAmount() - material.getMaxStackSize());
				replacement.setAmount(moving);
				item.setAmount(item.getAmount() - moving);

				fixed.add(replacement);
			}
			fixed.add(item);
		}

		return fixed;
	}

	public static ItemStack getTool(Player player) {
		return getTool(player, (Material) null);
	}

	public static ItemStack getTool(Player player, Material material) {
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		ItemStack offHand = player.getInventory().getItemInOffHand();
		if (!isNullOrAir(mainHand) && (material == null || mainHand.getType() == material))
			return mainHand;
		else if (!isNullOrAir(offHand) && (material == null || offHand.getType() == material))
			return offHand;
		return null;
	}

	public static ItemStack getToolRequired(Player player) {
		ItemStack item = getTool(player);
		if (isNullOrAir(item))
			throw new InvalidInputException("You are not holding anything");
		return item;
	}

	public static EquipmentSlot getHandWithTool(Player player) {
		return getHandWithTool(player, null);
	}

	public static EquipmentSlot getHandWithTool(Player player, Material material) {
		ItemStack mainHand = player.getInventory().getItemInMainHand();
		ItemStack offHand = player.getInventory().getItemInOffHand();
		if (!isNullOrAir(mainHand) && (material == null || mainHand.getType() == material))
			return EquipmentSlot.HAND;
		else if (!isNullOrAir(offHand) && (material == null || offHand.getType() == material))
			return EquipmentSlot.OFF_HAND;
		return null;
	}

	public static EquipmentSlot getHandWithToolRequired(Player player) {
		EquipmentSlot hand = getHandWithTool(player);
		if (hand == null)
			throw new InvalidInputException("You are not holding anything");
		return hand;
	}

}
