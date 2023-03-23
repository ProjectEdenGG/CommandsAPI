package gg.projecteden.commands.exceptions.postconfigured;

public class PlayerNotFoundException extends PostConfiguredException {

	public PlayerNotFoundException(String input) {
		super("Player " + input + " not found");
	}

}
