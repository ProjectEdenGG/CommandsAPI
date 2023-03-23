package gg.projecteden.commands.exceptions.preconfigured;

import gg.projecteden.commands.exceptions.CustomCommandException;
import net.md_5.bungee.api.ChatColor;

public class PreConfiguredException extends CustomCommandException {

	public PreConfiguredException(String message) {
		super(ChatColor.RED + message);
	}
}
