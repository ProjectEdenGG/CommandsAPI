package gg.projecteden.commands.exceptions.postconfigured;

import gg.projecteden.commands.exceptions.CustomCommandException;
import gg.projecteden.commands.util.JsonBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.NamedTextColor;

public class PostConfiguredException extends CustomCommandException {

	public PostConfiguredException(JsonBuilder json) {
		super(new JsonBuilder(NamedTextColor.RED).next(json));
	}

	public PostConfiguredException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public PostConfiguredException(String message) {
		this(new JsonBuilder(message));
	}

}
