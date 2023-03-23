package gg.projecteden.commands.exceptions.preconfigured;

import gg.projecteden.commands.util.Nullables;

public class NoPermissionException extends PreConfiguredException {

	public NoPermissionException() {
		this(null);
	}

	public NoPermissionException(String extra) {
		super("You don't have permission to do that!" + (Nullables.isNullOrEmpty(extra) ? "" : " " + extra));
	}

}
