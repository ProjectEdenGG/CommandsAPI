package gg.projecteden.commands;

import gg.projecteden.commands.models.CustomCommand;
import gg.projecteden.commands.models.ICustomCommand;
import gg.projecteden.commands.models.annotations.ConverterFor;
import gg.projecteden.commands.models.annotations.DoubleSlash;
import gg.projecteden.commands.models.annotations.TabCompleterFor;
import gg.projecteden.commands.util.Env;
import gg.projecteden.commands.util.StringUtils;
import gg.projecteden.commands.util.Utils;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import static gg.projecteden.commands.util.ReflectionUtils.methodsAnnotatedWith;
import static gg.projecteden.commands.util.ReflectionUtils.subTypesOf;

@SuppressWarnings({"unused", "unchecked"})
public class Commands {

	@Getter
	private static Commands instance;

	@Getter
	private final Plugin plugin;
	private Env env = Env.PROD;
	@Getter
	private final CommandMapUtils mapUtils;
	private final Set<Class<? extends CustomCommand>> commandSet;
	@Getter
	private static final Map<String, CustomCommand> commands = new HashMap<>();
	@Getter
	private static final Map<Class<?>, Method> converters = new HashMap<>();
	@Getter
	private static final Map<Class<?>, Method> tabCompleters = new HashMap<>();
	@Getter
	private static final Map<String, String> redirects = new HashMap<>();
	@Getter
	private static final String pattern = "(\\/){1,2}[\\w\\-]+";

	public Commands(@NonNull Plugin plugin) {
		if (instance != null && !commands.isEmpty())
			throw new UnsupportedOperationException("Cannot invoke a new instance of Commands while having commands registered. Please call Commands#unregisterAll in your onDisable");
		Commands.instance = this;
		this.plugin = plugin;
		this.mapUtils = new CommandMapUtils(plugin);
		this.commandSet = new HashSet<>();
		plugin.getServer().getPluginManager().registerEvents(new CommandListener(), plugin);
	}

	public Commands add(Class<? extends CustomCommand> customCommand) {
		this.commandSet.add(customCommand);
		return this;
	}

	public Commands scan(String path) {
		this.commandSet.addAll(subTypesOf(CustomCommand.class, path));
		return this;
	}

	public Commands env(Env env) {
		this.env = env;
		return this;
	}

	public static Env getEnv() {
		return getInstance().env;
	}

	@Getter
	@Setter
	private static boolean debug = false;

	public static void debug(String message) {
		if (debug)
			getInstance().plugin.getLogger().info("[DEBUG] " + ChatColor.stripColor(message));
	}

	public static void log(String message) {
		log(Level.INFO, message);
	}

	public static void log(String message, Throwable ex) {
		log(Level.INFO, message, ex);
	}

	public static void warn(String message) {
		log(Level.WARNING, message);
	}

	public static void warn(String message, Throwable ex) {
		log(Level.WARNING, message, ex);
	}

	public static void severe(String message) {
		log(Level.SEVERE, message);
	}

	public static void severe(String message, Throwable ex) {
		log(Level.SEVERE, message, ex);
	}

	public static void log(Level level, String message) {
		log(level, message, null);
	}

	public static void log(Level level, String message, Throwable ex) {
		Commands.getInstance().plugin.getLogger().log(level, ChatColor.stripColor(message), ex);
	}

	public static void registerListener(Listener listener) {
		getInstance().plugin.getServer().getPluginManager().registerEvents(listener, getInstance().plugin);
	}

	public static Set<CustomCommand> getUniqueCommands() {
		return new HashSet<>(commands.values());
	}

	public static CustomCommand get(String alias) {
		return commands.getOrDefault(alias.toLowerCase(), null);
	}

	public static CustomCommand get(Class<? extends CustomCommand> clazz) {
		return commands.getOrDefault(prettyName(clazz), null);
	}

	public static String prettyName(ICustomCommand customCommand) {
		return prettyName(customCommand.getClass());
	}

	public static String prettyName(Class<? extends ICustomCommand> clazz) {
		return clazz.getSimpleName().replaceAll("Command$", "");
	}

	public static String getPrefix(ICustomCommand customCommand) {
		return getPrefix(customCommand.getClass());
	}

	public static String getPrefix(Class<? extends ICustomCommand> clazz) {
		return StringUtils.getPrefix(prettyName(clazz));
	}

	public void registerAll() {
		registerConvertersAndTabCompleters();
		Commands.debug(" Registering " + commandSet.size() + StringUtils.plural(" command", commands.size()));
		commandSet.forEach(this::register);
		Commands.log("Registered " + commands.size() + StringUtils.plural(" command", commands.size()));
	}

	private void register(Class<? extends CustomCommand>... customCommands) {
		for (Class<? extends CustomCommand> clazz : customCommands)
			try {
				if (Utils.canEnable(clazz))
					register(Utils.singletonOf(clazz));
			} catch (Throwable ex) {
				plugin.getLogger().info("Error while registering command " + prettyName(clazz));
				ex.printStackTrace();
			}
	}

	private void registerExcept(Class<? extends CustomCommand>... customCommands) {
		List<Class<? extends CustomCommand>> excluded = Arrays.asList(customCommands);
		for (Class<? extends CustomCommand> clazz : commandSet)
			if (!excluded.contains(clazz))
				register(clazz);
	}

	private void register(CustomCommand customCommand) {
		try {
			for (String alias : customCommand.getAllAliases()) {
				mapUtils.register(alias, customCommand);

				if (customCommand.getClass().getAnnotation(DoubleSlash.class) != null)
					alias = "/" + alias;

				commands.put(alias.toLowerCase(), customCommand);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		Utils.tryRegisterListener(customCommand);
	}

	public static void unregisterAll() {
		for (Class<? extends CustomCommand> clazz : getInstance().commandSet)
			try {
				getInstance().unregister(clazz);
			} catch (Throwable ex) {
				getInstance().plugin.getLogger().info("Error while unregistering command " + prettyName(clazz));
				ex.printStackTrace();
			}
	}

	public void unregister(Class<? extends CustomCommand>... customCommands) {
		for (Class<? extends CustomCommand> clazz : customCommands)
			if (Utils.canEnable(clazz))
				unregister(Utils.singletonOf(clazz));
	}

	public void unregisterExcept(Class<? extends CustomCommand>... customCommands) {
		List<Class<? extends CustomCommand>> excluded = Arrays.asList(customCommands);
		for (Class<? extends CustomCommand> clazz : commandSet)
			if (!excluded.contains(clazz))
				unregister(clazz);
	}

	private void unregister(CustomCommand customCommand) {
		try {
			mapUtils.unregister(customCommand.getName());
			for (String alias : customCommand.getAllAliases())
				commands.remove(alias);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			customCommand._shutdown();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void registerConvertersAndTabCompleters() {
		commandSet.forEach(this::registerTabCompleters);
		commandSet.forEach(this::registerConverters);
		registerTabCompleters(CustomCommand.class);
		registerConverters(CustomCommand.class);
	}

	private void registerTabCompleters(Class<?> clazz) {
		methodsAnnotatedWith(clazz, TabCompleterFor.class).forEach(method -> {
			for (Class<?> classFor : method.getAnnotation(TabCompleterFor.class).value()) {
				method.setAccessible(true);
				tabCompleters.put(classFor, method);
			}
		});
	}

	private void registerConverters(Class<?> clazz) {
		methodsAnnotatedWith(clazz, ConverterFor.class).forEach(method -> {
			for (Class<?> classFor : method.getAnnotation(ConverterFor.class).value()) {
				method.setAccessible(true);
				converters.put(classFor, method);
			}
		});
	}

}
