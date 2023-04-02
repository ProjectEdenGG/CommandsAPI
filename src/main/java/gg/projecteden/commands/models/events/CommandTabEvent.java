package gg.projecteden.commands.models.events;

import gg.projecteden.commands.exceptions.CustomCommandException;
import gg.projecteden.commands.exceptions.preconfigured.NoPermissionException;
import gg.projecteden.commands.models.CustomCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;

public class CommandTabEvent extends CommandEvent {

	public CommandTabEvent(CommandSender sender, CustomCommand command, String aliasUsed, List<String> args, List<String> originalArgs) {
		super(sender, command, aliasUsed, args, originalArgs, true);
	}

	@Override
	public void handleException(Throwable ex) {
		if (ex instanceof NoPermissionException)
			return;
		if (ex instanceof CustomCommandException customCommandException) {
			sender.sendMessage(customCommandException.getJson());
			if (!(sender instanceof ConsoleCommandSender))
				Bukkit.getConsoleSender().sendMessage(customCommandException.getJson());
			return;
		}
		ex.printStackTrace();

	}

}
