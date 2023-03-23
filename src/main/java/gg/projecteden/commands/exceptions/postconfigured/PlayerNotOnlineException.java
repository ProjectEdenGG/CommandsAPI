package gg.projecteden.commands.exceptions.postconfigured;

import org.bukkit.OfflinePlayer;

public class PlayerNotOnlineException extends PostConfiguredException {

	public PlayerNotOnlineException(OfflinePlayer player) {
		super(player.getName() + " is not online");
	}

}
