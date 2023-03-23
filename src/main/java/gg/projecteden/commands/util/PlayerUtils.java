package gg.projecteden.commands.util;

import gg.projecteden.commands.exceptions.postconfigured.InvalidInputException;
import gg.projecteden.commands.exceptions.postconfigured.PlayerNotFoundException;
import gg.projecteden.commands.util.Utils.MinMaxResult;
import lombok.AllArgsConstructor;
import net.kyori.adventure.identity.Identified;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static gg.projecteden.commands.util.Nullables.isNullOrEmpty;
import static gg.projecteden.commands.util.Utils.getMin;
import static java.util.stream.Collectors.toList;

public class PlayerUtils {

	public static void runCommand(CommandSender sender, String commandNoSlash) {
		if (sender == null)
			return;

		Runnable command = () -> Bukkit.dispatchCommand(sender, commandNoSlash);

		if (Bukkit.isPrimaryThread())
			command.run();
		else
			Tasks.sync(command);
	}

	public static void runCommandAsOp(CommandSender sender, String commandNoSlash) {
		boolean deop = !sender.isOp();
		sender.setOp(true);
		runCommand(sender, commandNoSlash);
		if (deop)
			sender.setOp(false);
	}

	public static void runCommandAsConsole(String commandNoSlash) {
		runCommand(Bukkit.getConsoleSender(), commandNoSlash);
	}

	@Contract("null, _ -> false; _, null -> false")
	public static boolean isSelf(@Nullable Player player1, @Nullable Player player2) {
		return player1 != null && player2 != null && player1.getUniqueId().equals(player2.getUniqueId());
	}

	public static void send(@Nullable Object recipient, @Nullable Object message, @NotNull Object... objects) {
		if (recipient == null || message == null)
			return;

		if (message instanceof String string && objects.length > 0)
			message = String.format(string, objects);

		if (recipient instanceof CommandSender sender) {
			if (!(message instanceof String || message instanceof ComponentLike))
				message = message.toString();

			if (message instanceof String string)
				sender.sendMessage(new JsonBuilder(string));
			else if (message instanceof ComponentLike componentLike)
				sender.sendMessage(componentLike);
		}

		else if (recipient instanceof OfflinePlayer offlinePlayer) {
			Player player = offlinePlayer.getPlayer();
			if (player != null)
				send(player, message);
		}

		else if (recipient instanceof UUID uuid)
			send(getPlayer(uuid), message);

		else if (recipient instanceof Identity identity)
			send(getPlayer(identity), message);

		else if (recipient instanceof Identified identified)
			send(getPlayer(identified.identity()), message);
	}

	public static @NotNull OfflinePlayer getPlayer(String partialName) throws InvalidInputException, PlayerNotFoundException {
		if (partialName == null || partialName.length() == 0)
			throw new InvalidInputException("No player name given");

		String original = partialName;
		partialName = partialName.toLowerCase().trim();

		if (partialName.matches("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}"))
			return getPlayer(UUID.fromString(partialName));

		final List<Player> players = OnlinePlayers.getAll();

		for (Player player : players)
			if (player.getName().equalsIgnoreCase(partialName))
				return player;

		for (Player player : players)
			if (player.getName().toLowerCase().startsWith(partialName))
				return player;

		for (Player player : players)
			if (player.getName().toLowerCase().contains((partialName)))
				return player;

		throw new PlayerNotFoundException(original);
	}

	public static @NotNull OfflinePlayer getPlayer(UUID uuid) {
		return Bukkit.getOfflinePlayer(uuid);
	}

	public static @NotNull OfflinePlayer getPlayer(Identity identity) {
		return getPlayer(identity.uuid());
	}

	/**
	 * Tests if a player can see a vanished player. Returns false if either player is null.
	 * @param viewer player who is viewing
	 * @param target target player to check
	 * @return true if the target can be seen by the viewer
	 */
	@Contract("null, _ -> false; _, null -> false")
	public static boolean canSee(@Nullable Player viewer, @Nullable Player target) {
		if (viewer == null || target == null)
			return false;

		return viewer.canSee(target);
	}

	public static MinMaxResult<Player> getNearestPlayer(Location location) {
		return getMin(OnlinePlayers.where().world(location.getWorld()).get(), player -> Distance.distance(location, location).get());
	}

	public static void giveItems(Player player, Collection<ItemStack> items) {
		List<ItemStack> finalItems = new ArrayList<>(items);
		finalItems.removeIf(item -> item == null || item.getType().isEmpty());
		finalItems.removeIf(itemStack -> itemStack.getAmount() == 0);

		dropExcessItems(player, giveItemsAndGetExcess(player.getInventory(), finalItems));
	}

	public static void dropExcessItems(Player player, List<ItemStack> excess) {
		dropItems(player.getPlayer().getLocation(), excess);
	}

	public static void dropItems(Location location, List<ItemStack> items) {
		if (!isNullOrEmpty(items))
			for (ItemStack item : items)
				if (item != null && item.getType() != Material.AIR && item.getAmount() > 0)
					location.getWorld().dropItemNaturally(location, item);
	}

	@NotNull
	public static List<ItemStack> giveItemsAndGetExcess(Inventory inventory, List<ItemStack> items) {
		return new ArrayList<>() {{
			for (ItemStack item : ItemUtils.fixMaxStackSize(items))
				if (item != null && item.getType() != Material.AIR)
					addAll(inventory.addItem(item.clone()).values());
		}};
	}

	public static class OnlinePlayers {
		private UUID viewer;
		private World world;
		private Location origin;
		private Double radius;
		private String permission;
		private List<UUID> include;
		private List<UUID> exclude;
		private List<Predicate<Player>> filters = new ArrayList<>();

		public static OnlinePlayers where() {
			return new OnlinePlayers();
		}

		public static OnlinePlayers where(Predicate<Player> filter) {
			return new OnlinePlayers().filter(filter);
		}

		public static List<Player> getAll() {
			return where().get();
		}

		public OnlinePlayers viewer(UUID player) {
			this.viewer = player;
			return this;
		}

		public OnlinePlayers world(String world) {
			return world(Objects.requireNonNull(Bukkit.getWorld(world)));
		}

		public OnlinePlayers world(World world) {
			this.world = world;
			return this;
		}

		public OnlinePlayers radius(double radius) {
			this.radius = radius;
			return this;
		}

		public OnlinePlayers radius(Location origin, double radius) {
			this.origin = origin;
			this.radius = radius;
			return this;
		}

		public OnlinePlayers hasPermission(String permission) {
			this.permission = permission;
			return this;
		}

		public OnlinePlayers includePlayers(List<Player> players) {
			return include(players.stream().map(Player::getUniqueId).toList());
		}

		public OnlinePlayers include(List<UUID> uuids) {
			if (this.include == null)
				this.include = new ArrayList<>();
			if (uuids == null)
				uuids = new ArrayList<>();

			this.include.addAll(uuids);
			return this;
		}

		public OnlinePlayers excludeSelf() {
			return exclude(viewer);
		}

		public OnlinePlayers excludePlayers(List<Player> players) {
			return exclude(players.stream().map(Player::getUniqueId).toList());
		}

		public OnlinePlayers exclude(Player player) {
			return exclude(List.of(player.getUniqueId()));
		}

		public OnlinePlayers exclude(UUID uuid) {
			return exclude(List.of(uuid));
		}

		public OnlinePlayers exclude(List<UUID> uuids) {
			if (this.exclude == null)
				this.exclude = new ArrayList<>();
			this.exclude.addAll(uuids);
			return this;
		}

		public OnlinePlayers filter(Predicate<Player> filter) {
			this.filters.add(filter);
			return this;
		}

		public List<Player> get() {
			final Supplier<List<UUID>> online = () -> Bukkit.getOnlinePlayers().stream().map(Player::getUniqueId).collect(toList());
			final List<UUID> uuids = include == null ? online.get() : include;

			if (uuids.isEmpty())
				return Collections.emptyList();

			Stream<Player> stream = uuids.stream()
					                        .filter(uuid -> exclude == null || !exclude.contains(uuid))
					                        .map(Bukkit::getOfflinePlayer)
					                        .filter(OfflinePlayer::isOnline)
					                        .map(OfflinePlayer::getPlayer);

			if (origin == null && this.viewer != null) {
				final Player viewer = Bukkit.getPlayer(this.viewer);
				if (viewer != null)
					origin = viewer.getLocation();
			}

			for (Filter filter : Filter.values())
				stream = filter.filter(this, stream);

			for (Predicate<Player> filter : filters)
				stream = stream.filter(filter);

			return stream.collect(toList());
		}

		public <T> List<T> map(Function<Player, T> mapper) {
			return get().stream().map(mapper).collect(toList());
		}

		public int count() {
			return get().size();
		}

		public void forEach(Consumer<Player> consumer) {
			get().forEach(consumer);
		}

		@AllArgsConstructor
		private enum Filter {
			PERMISSION(
					search -> search.permission != null,
					(search, player) -> player.hasPermission(search.permission)),
			VIEWER(
					search -> search.viewer != null,
					(search, player) -> canSee(Bukkit.getPlayer(search.viewer), player)),
			WORLD(
					search -> search.world != null,
					(search, player) -> player.getWorld().equals(search.world)),
			RADIUS(
					search -> search.origin != null && search.radius != null,
					(search, player) -> search.origin.getWorld().equals(player.getWorld()) && Distance.distance(player.getLocation(), search.origin).lte(search.radius)),
			;

			private final Predicate<OnlinePlayers> canFilter;
			private final BiPredicate<OnlinePlayers, Player> predicate;

			private Stream<Player> filter(OnlinePlayers search, Stream<Player> stream) {
				if (!canFilter.test(search))
					return stream;

				return stream.filter(player -> predicate.test(search, player));
			}
		}
	}

}
