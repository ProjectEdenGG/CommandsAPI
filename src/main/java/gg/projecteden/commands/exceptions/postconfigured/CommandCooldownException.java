package gg.projecteden.commands.exceptions.postconfigured;

import gg.projecteden.commands.util.Cooldown;
import gg.projecteden.commands.util.TimeUtils.Timespan;
import org.bukkit.OfflinePlayer;

import java.time.LocalDateTime;
import java.util.UUID;

public class CommandCooldownException extends PostConfiguredException {

	public CommandCooldownException(OfflinePlayer player, String type) {
		this(player.getUniqueId(), type);
	}

	public CommandCooldownException(UUID uuid, String type) {
		super("You can run this command again in &e" + Cooldown.of(uuid).getDiff(type));
	}

	public CommandCooldownException(LocalDateTime when) {
		super("You can run this command again in &e" + Timespan.of(when).format());
	}

}
