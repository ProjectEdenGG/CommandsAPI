package gg.projecteden.commands.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.projecteden.commands.exceptions.CustomCommandException;
import gg.projecteden.commands.util.SerializationUtils.Json.LocalDateGsonSerializer;
import gg.projecteden.commands.util.SerializationUtils.Json.LocalDateTimeGsonSerializer;
import gg.projecteden.commands.util.SerializationUtils.Json.LocationGsonSerializer;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Helper methods for modifying Strings
 */
public class StringUtils {

	@Getter
	private static final String colorChar = "§";
	@Getter
	private static final String altColorChar = "&";
	@Getter
	private static final String colorCharsRegex = "[" + colorChar + altColorChar + "]";
	@Getter
	private static final Pattern colorPattern = Pattern.compile(colorCharsRegex + "[\\da-fA-F]");
	@Getter
	private static final Pattern formatPattern = Pattern.compile(colorCharsRegex + "[k-orK-OR]");
	@Getter
	private static final Pattern hexPattern = Pattern.compile(colorCharsRegex + "#[a-fA-F\\d]{6}");
	@Getter
	private static final Pattern hexColorizedPattern = Pattern.compile(colorCharsRegex + "x(" + colorCharsRegex + "[a-fA-F\\d]){6}");
	@Getter
	private static final Pattern colorGroupPattern = Pattern.compile("(" + colorPattern + "|(" + hexPattern + "|" + hexColorizedPattern + "))((" + formatPattern + ")+)?");

	public static final String CHECK = "&a✔";
	public static final String X = "&c✗";
	public static final String COMMA_SPLIT_REGEX = ",(?=[^}]*(?:\\{|$))";

	public static String colorize(String input) {
		if (input == null)
			return null;

		while (true) {
			Matcher matcher = hexPattern.matcher(input);
			if (!matcher.find()) break;

			String color = matcher.group();
			input = input.replace(color, net.md_5.bungee.api.ChatColor.of(color.replaceFirst(colorCharsRegex, "")).toString());
		}

		return ChatColor.translateAlternateColorCodes(altColorChar.charAt(0), input);
	}

	public static String getPrefix(Class<?> clazz) {
		return getPrefix(clazz.getSimpleName());
	}

	public static String getPrefix(String prefix) {
		return colorize("&8&l[&e" + prefix + "&8&l]&3 ");
	}

	/**
	 * Removes any color from a message
	 *
	 * @param input The colored message
	 *
	 * @return The uncolored message
	 */
	public static String stripColor(String input) {
		return ChatColor.stripColor(colorize(input));
	}

	/**
	 * Removes the last character from the end of a string, leaving the rest
	 *
	 * @param string The string to trim the last character from
	 * @return The modified string
	 */
	public static String trimFirst(String string) {
		return right(string, string.length() - 1);
	}

	/**
	 * Gets the specified amount of characters from the right of the given string
	 *
	 * @param string The string to modify
	 * @param number The number of characters to get
	 *
	 * @return The given amount of characters from the right of the given string
	 */
	public static String right(String string, int number) {
		return string.substring(Math.max(string.length() - number, 0));
	}

	/**
	 * Gets the specified amount of characters from the left of the given string
	 *
	 * @param string The string to modify
	 * @param number The number of characters to get
	 *
	 * @return The given amount of characters from the left of the given string
	 */
	public static String left(String string, int number) {
		return string.substring(0, Math.min(number, string.length()));
	}

	/**
	 * Modify a string to be camel case, or every word capitalized
	 *
	 * @param text The string to camel case
	 *
	 * @return The camel cased string
	 */
	public static String camelCase(String text) {
		if (text == null || text.isEmpty()) {
			return text;
		}

		return Arrays.stream(text.replaceAll("_", " ").split(" "))
				.map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1).toLowerCase())
				.collect(Collectors.joining(" "));
	}

	/**
	 * Modifies a string to get the string after the last instance of the given delimiter
	 *
	 * @param string The string to modify
	 * @param delimiter The character to look for
	 *
	 * @return The modified string
	 */
	public static String listLast(String string, String delimiter) {
		return string.substring(string.lastIndexOf(delimiter) + 1);
	}

	/**
	 * Get a string version of a location in simple forms
	 *
	 * @param loc The location to turn into a short string
	 *
	 * @return The string version of the given location
	 */
	public static String getShortLocationString(Location loc) {
		DecimalFormat nf = new DecimalFormat("0.00");
		return nf.format(loc.getX()) + ", " + nf.format(loc.getY()) + ", " +  nf.format(loc.getZ());
	}

	/**
	 * Helper method to get the last color of a string
	 * @see JsonBuilder
	 *
	 * @param text The string to get the last color of
	 *
	 * @return The last color code in form of a String
	 */
	public static String getLastColor(String text) {
		Matcher matcher = colorGroupPattern.matcher(text);
		String last = "";
		while (matcher.find())
			last = matcher.group();
		return last.toLowerCase();
	}

	private static final int APPROX_LORE_LINE_LENGTH = 40;

	public static List<String> loreize(String string) {
		return loreize(string, APPROX_LORE_LINE_LENGTH);
	}

	public static List<String> loreize(String string, int length) {
		return new ArrayList<>() {{
			final String[] split = string.split(" ");
			StringBuilder line = new StringBuilder();
			for (String word : split) {
				final int oldLength = stripColor(line.toString()).length();
				final int newLength = oldLength + stripColor(word).length();

				boolean append = Math.abs(length - oldLength) >= Math.abs(length - newLength);
				if (!append) {
					String newline = line.toString().trim();
					add(line.toString().trim());
					line = new StringBuilder(getLastColor(newline));
				}

				line.append(word).append(" ");
			}

			add(line.toString().trim());
		}};
	}

	@Getter
	protected static final DecimalFormat df = new DecimalFormat("#.00");

	@Getter
	protected static final DecimalFormat nf = new DecimalFormat("#");

	@Getter
	protected static final DecimalFormat cdf = new DecimalFormat("#,###.00"); // comma decimal formatter

	@Getter
	protected static final DecimalFormat cnf = new DecimalFormat("#,###"); // comma number formatter

	public static DecimalFormat getFormatter(Class<?> type) {
		if (Integer.class == type || Integer.TYPE == type) return nf;
		if (Double.class == type || Double.TYPE == type) return df;
		if (Float.class == type || Float.TYPE == type) return df;
		if (Short.class == type || Short.TYPE == type) return nf;
		if (Long.class == type || Long.TYPE == type) return nf;
		if (Byte.class == type || Byte.TYPE == type) return nf;
		if (BigDecimal.class == type) return df;
		throw new CustomCommandException("No formatter found for class " + type.getSimpleName());
	}

	public static String getShortishLocationString(Location loc) {
		String coords = (int) loc.getX() + " " + (int) loc.getY() + " " + (int) loc.getZ();
		if (loc.getYaw() != 0 || loc.getPitch() != 0)
			coords += " " + df.format(loc.getYaw()) + " " + df.format(loc.getPitch());
		return coords + " " + loc.getWorld().getName();
	}

	@NotNull
	public static String an(@NotNull String text) {
		return an(text, null);
	}

	@NotNull
	public static String an(@NotNull String text, String color) {
		return "a" + (text.matches("(?i)^[AEIOU].*") ? "n" : "") + " " + (color == null ? "" : color) + text;
	}

	public static String plural(String label, Number number) {
		return label + (number.doubleValue() == 1 ? "" : "s");
	}

	public static String plural(String labelSingle, String labelPlural, Number number) {
		return number.doubleValue() == 1 ? labelSingle : labelPlural;
	}

	private static final Gson prettyPrinter = new GsonBuilder().setPrettyPrinting()
			.registerTypeAdapter(Location.class, new LocationGsonSerializer())
			.registerTypeAdapter(LocalDate.class, new LocalDateGsonSerializer())
			.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeGsonSerializer())
			.create();

	public static String toPrettyString(Object object) {
		if (object == null) return null;
		try {
			return prettyPrinter.toJson(object);
		} catch (Throwable ignored) {
			return object.toString();
		}
	}

	public static String getNumberWithSuffix(int number) {
		String text = String.valueOf(number);
		if (text.endsWith("1"))
			if (text.endsWith("11"))
				return number + "th";
			else
				return number + "st";
		else if (text.endsWith("2"))
			if (text.endsWith("12"))
				return number + "th";
			else
				return number + "nd";
		else if (text.endsWith("3"))
			if (text.endsWith("13"))
				return number + "th";
			else
				return number + "rd";
		else
			return number + "th";
	}

}
