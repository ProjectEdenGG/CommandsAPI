package gg.projecteden.commands.exceptions.postconfigured;

import gg.projecteden.commands.util.JsonBuilder;
import net.kyori.adventure.text.ComponentLike;

public class InvalidInputException extends PostConfiguredException {

	public InvalidInputException(JsonBuilder json) {
		super(json);
	}

	public InvalidInputException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public InvalidInputException(String message) {
		this(new JsonBuilder(message));
	}

}
