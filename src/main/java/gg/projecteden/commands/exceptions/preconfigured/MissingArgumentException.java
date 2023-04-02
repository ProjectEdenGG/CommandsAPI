package gg.projecteden.commands.exceptions.preconfigured;

public class MissingArgumentException extends PreConfiguredException {

	public MissingArgumentException(String message) {
		super(message);
	}

	public MissingArgumentException() {
		super("Missing argument");
	}

}
