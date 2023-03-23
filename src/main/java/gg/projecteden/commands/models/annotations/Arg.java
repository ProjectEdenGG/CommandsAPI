package gg.projecteden.commands.models.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies parameters for an argument inside of a command
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {
	/**
	 * Sets the default value for this argument.
	 * <p>
	 * <code>self</code> will use the executing player as a default for commands requiring player-like objects.
	 * @return default value or an empty string
	 */
	String value() default "";

	/**
	 * The permission node required to use this argument
	 * @return permission node or an empty string
	 */
	String permission() default "";

	/**
	 * For arguments which require more context, you must
	 * set this value to point to the argument which provides the required context.
	 * <p>
	 * This value is one-indexed.
	 */
	int context() default -1;

	/**
	 * Uses the tab completer for the provided class instead of the default
	 * <p>
	 * Example: if you have a parameter <code>String color</code> and want to provide default tab completion, you would
	 * set this value to <code>ColorType.class</code> or similar.
	 * @return
	 */
	Class<?> tabCompleter() default void.class;

	/**
	 * For arguments affected by erasure, i.e. {@link java.util.List}, specifies what objects the list contains.
	 * <p>
	 * For example, if an argument is of type <code>List&lt;Player&gt;</code>, you should include an argument with this
	 * value set to <code>Player.class</code>
	 * @return type of List object
	 */
	Class<?> type() default void.class;

	/**
	 * Specifies the minimum allowed value for number arguments
	 * <p>
	 * Defaults to {@link Short#MIN_VALUE}
	 * @return a double
	 */
	double min() default Short.MIN_VALUE;

	/**
	 * Specifies the maximum allowed value for number arguments
	 * <p>
	 * Defaults to {@link Short#MAX_VALUE}
	 * @return a double
	 */
	double max() default Short.MAX_VALUE;

	/**
	 * Sets the permission node required to bypass {@link #min()} and {@link #max()}
	 * @return permission node or an empty string
	 */
	String minMaxBypass() default "";

	/**
	 * Specifies that this argument must match the provided regex or else will error
	 * <p>
	 * Set to an empty string (the default) to disable
	 * @return a regex filter or an empty string
	 */
	String regex() default "";

	/**
	 * Specifies whether to strip the color from the input
	 * @return whether to strip the color from the input
	 */
	boolean stripColor() default false;

}
