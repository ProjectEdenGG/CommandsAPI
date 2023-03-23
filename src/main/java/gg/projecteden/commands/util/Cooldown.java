package gg.projecteden.commands.util;

import gg.projecteden.commands.exceptions.postconfigured.InvalidInputException;
import gg.projecteden.commands.util.TimeUtils.Timespan;
import lombok.NonNull;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;


public class Cooldown {

	private static Map<UUID, Cooldown> cooldownMap = new ConcurrentHashMap<>();

	@NonNull
	private Map<String, LocalDateTime> cooldowns = new ConcurrentHashMap<>();

	public static Cooldown of(Player player) {
		return of(player.getUniqueId());
	}

	public static Cooldown of(UUID uuid) {
		if (cooldownMap.containsKey(uuid))
			return cooldownMap.get(uuid);
		else {
			Cooldown cooldown = new Cooldown();
			cooldownMap.put(uuid, cooldown);
			return cooldown;
		}
	}

	private static final Pattern VALID_TYPE = Pattern.compile("^[\\w:#-]+$", Pattern.CASE_INSENSITIVE);

	private Cooldown() { }

	/**
	 * Ensures the type does not use invalid characters
	 * @param type an arbitrary string corresponding to the type of cooldown hopefully matching the regex ^[\w:#-]+$
	 * @throws IllegalArgumentException type uses invalid characters
	 * @return the input with spaces replaced with underscores
	 */
	private String checkType(String type) throws IllegalArgumentException {
		if (!VALID_TYPE.matcher(type).matches())
			throw new InvalidInputException("type `" + type + "` must match regex " + VALID_TYPE.pattern());
		return type.replace(' ', '_');
	}

	/**
	 * Checks if a provided type has a saved cooldown time.
	 * <p>
	 * This method does not check if the saved time has expired or not.
	 * @param type an arbitrary string corresponding to the type of cooldown matching the regex ^[\w:#-]+$
	 * @see #check(String) check(type)
	 * @return true if a cooldown time is present
	 */
	public boolean exists(String type) {
		type = checkType(type);
		return cooldowns.containsKey(type);
	}

	public boolean check(String type, long ticks) {
		if (ticks == 0)
			return true;

		if (!check(type))
			return false;

		create(type, ticks);
		return true;
	}

	/**
	 * Gets the expiry time for a provided cooldown.
	 * @param type an arbitrary string corresponding to the type of cooldown matching the regex ^[\w:#-]+$
	 * @see #check(String) check(type)
	 * @return expiration time of a cooldown or null if none is set
	 */
	public @Nullable LocalDateTime get(String type) {
		type = checkType(type);
		return cooldowns.getOrDefault(type, null);
	}

	/**
	 * Checks if a player is currently off cooldown.
	 * <p>
	 * Returns true if the player is not on cooldown.
	 * </p>
	 * @param type an arbitrary string corresponding to the type of cooldown matching the regex ^[\w:#-]+$
	 * @return true if player is not on cooldown
	 */
	public boolean check(String type) {
		type = checkType(type);
		return !exists(type) || cooldowns.get(type).isBefore(LocalDateTime.now());
	}

	public String getDiff(String type) {
		if (exists(type))
			return Timespan.of(get(type)).format();

		return ".0s";
	}

	/**
	 * Creates a cooldown.
	 * <p>
	 * This method will override existing cooldowns.
	 *
	 * @param type  an arbitrary string corresponding to the type of cooldown matching the regex ^[\w:#-]+$
	 * @param ticks how long the cooldown should last in ticks
	 * @return this object
	 */
	@NotNull
	@Contract("_, _ -> this")
	public Cooldown create(String type, long ticks) {
		type = checkType(type);
		cooldowns.put(type, LocalDateTime.now().plus(ticks * 50L, ChronoUnit.MILLIS));
		return this;
	}

	/**
	 * Clears a cooldown
	 * @param type an arbitrary string corresponding to the type of cooldown matching the regex ^[\w:#-]+$
	 */
	public void clear(String type) {
		type = checkType(type);
		cooldowns.remove(type);
	}

}

