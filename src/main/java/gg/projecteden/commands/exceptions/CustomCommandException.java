package gg.projecteden.commands.exceptions;

import gg.projecteden.commands.util.JsonBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.ComponentLike;

@Data
@NoArgsConstructor
public class CustomCommandException extends RuntimeException {
	private JsonBuilder json;

	public CustomCommandException(JsonBuilder json) {
		super(json.toString());
		this.json = json;
	}

	public CustomCommandException(String message, Throwable cause) {
		super(message, cause);
	}

	public CustomCommandException(ComponentLike component) {
		this(new JsonBuilder(component));
	}

	public CustomCommandException(String message) {
		this(new JsonBuilder(message));
	}

	public ComponentLike withPrefix(String prefix) {
		return new JsonBuilder(prefix).next(getJson());
	}

}
