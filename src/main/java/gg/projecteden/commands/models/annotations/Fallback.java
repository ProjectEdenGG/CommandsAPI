package gg.projecteden.commands.models.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used on parent commands to specify a plugin to fall back to if a subcommand is not found.
 * <p>
 * This is effectively used to add new subcommands to existing commands from other plugins.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Fallback {
	/**
	 * Name of the plugin to fall back to. This should be all-lowercase, as it is the plugin name found in <i><code>/&lt;plugin&gt;:&lt;command&gt;</code></i>
	 * @return plugin name
	 */
	String value();
}
