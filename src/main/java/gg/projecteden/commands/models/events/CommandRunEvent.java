package gg.projecteden.commands.models.events;

import gg.projecteden.commands.Commands;
import gg.projecteden.commands.exceptions.CustomCommandException;
import gg.projecteden.commands.exceptions.preconfigured.MissingArgumentException;
import gg.projecteden.commands.models.CustomCommand;
import gg.projecteden.commands.models.annotations.Description;
import gg.projecteden.commands.models.annotations.Path;
import gg.projecteden.commands.util.JsonBuilder;
import gg.projecteden.commands.util.Nullables;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Data
@RequiredArgsConstructor
public class CommandRunEvent extends CommandEvent {
	private Method method;
	private String usage;

	public CommandRunEvent(CommandSender sender, CustomCommand command, String aliasUsed, List<String> args, List<String> originalArgs) {
		super(sender, command, aliasUsed, args, originalArgs, false);
	}

	public void setUsage(Method method) {
		this.method = method;
		Path path = method.getAnnotation(Path.class);
		if (path != null) {
			this.usage = path.value();
			Description desc = method.getAnnotation(Description.class);
			if (desc != null)
				this.usage += " &7- " + desc.value();
		}
	}

	public String getUsageMessage() {
		return "Correct usage: /" + aliasUsed + " " + usage;
	}

	public void handleException(Throwable ex) {
		if (Commands.isDebug()) {
			Commands.debug("Handling command framework exception for " + getSender().getName());
			ex.printStackTrace();
		}

		String PREFIX = command.getPrefix();
		if (Nullables.isNullOrEmpty(PREFIX))
			PREFIX = Commands.getPrefix(command);

		if (ex instanceof MissingArgumentException) {
			reply(PREFIX + "&c" + getUsageMessage());
			return;
		}

		if (ex.getCause() != null && ex.getCause() instanceof CustomCommandException commandException) {
			reply(new JsonBuilder(PREFIX + "&c").next(commandException.getJson()));
			return;
		}

		if (ex instanceof CustomCommandException commandException) {
			reply(new JsonBuilder(PREFIX + "&c").next(commandException.getJson()));
			return;
		}

		if (ex.getCause() != null && ex.getCause() instanceof CustomCommandException edenException) {
			reply(PREFIX + "&c" + edenException.getMessage());
			return;
		}

		if (ex instanceof CustomCommandException) {
			reply(PREFIX + "&c" + ex.getMessage());
			return;
		}

		if (ex instanceof IllegalArgumentException && ex.getMessage() != null && ex.getMessage().contains("type mismatch")) {
			reply(PREFIX + "&c" + getUsageMessage());
			return;
		}

		reply("&cAn internal error occurred while attempting to execute this command");

		if (ex.getCause() != null && ex instanceof InvocationTargetException)
			ex.getCause().printStackTrace();
		else
			ex.printStackTrace();
	}

}
