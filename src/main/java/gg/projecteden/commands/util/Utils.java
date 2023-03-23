package gg.projecteden.commands.util;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gg.projecteden.commands.Commands;
import gg.projecteden.commands.exceptions.postconfigured.InvalidInputException;
import gg.projecteden.commands.models.annotations.Disabled;
import gg.projecteden.commands.models.annotations.Environments;
import io.github.classgraph.AnnotationEnumValue;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.ClassInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.Contract;
import org.objenesis.ObjenesisStd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Stream;

import static gg.projecteden.commands.util.ReflectionUtils.methodsAnnotatedWith;

public class Utils {

	public static final UUID UUID0 = new UUID(0, 0);

	public static void tryRegisterListener(Class<?> clazz) {
		if (canEnable(clazz))
			tryRegisterListener(singletonOf(clazz));
	}

	public static void tryRegisterListener(Object object) {
		try {
			final Class<?> clazz = object.getClass();
			if (!canEnable(clazz))
				return;

			boolean hasNoArgsConstructor = Stream.of(clazz.getConstructors()).anyMatch(c -> c.getParameterCount() == 0);
			if (object instanceof Listener listener) {
				if (hasNoArgsConstructor)
					Commands.registerListener(listener);
				else
					Commands.warn("Cannot register listener on " + clazz.getSimpleName() + ", needs @NoArgsConstructor");
			} else if (methodsAnnotatedWith(clazz, EventHandler.class).size() > 0)
				Commands.warn("Found @EventHandlers in " + clazz.getSimpleName() + " which does not implement Listener"
						              + (hasNoArgsConstructor ? "" : " or have a @NoArgsConstructor"));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static <T> List<T> reverse(List<T> list) {
		Collections.reverse(list);
		return list;
	}

	private static Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

	public static <T> T singletonOf(Class<T> clazz) {
		return (T) singletons.computeIfAbsent(clazz, $ -> {
			try {
				return clazz.getConstructor().newInstance();
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException |
			         NoSuchMethodException ex) {
				Commands.log(Level.FINE, "Failed to create singleton of " + clazz.getName() + ", falling back to Objenesis", ex);
				try {
					return new ObjenesisStd().newInstance(clazz);
				} catch (Throwable t) {
					throw new IllegalStateException("Failed to create singleton of " + clazz.getName() + " using Objenesis", t);
				}
			}
		});
	}

	public static boolean canEnable(Class<?> clazz) {
		if (clazz.getSimpleName().startsWith("_"))
			return false;
		if (Modifier.isAbstract(clazz.getModifiers()))
			return false;
		if (Modifier.isInterface(clazz.getModifiers()))
			return false;
		if (clazz.getAnnotation(Disabled.class) != null)
			return false;
		if (clazz.getAnnotation(Environments.class) != null && !Env.applies(clazz.getAnnotation(Environments.class).value()))
			return false;

		return true;
	}

	public static boolean canEnable(ClassInfo clazz) {
		if (clazz.getSimpleName().startsWith("_"))
			return false;
		if (Modifier.isAbstract(clazz.getModifiers()))
			return false;
		if (Modifier.isInterface(clazz.getModifiers()))
			return false;
		if (clazz.getAnnotationInfo(Disabled.class) != null)
			return false;

		final AnnotationInfo environments = clazz.getAnnotationInfo(Environments.class);
		if (environments != null) {
			final List<Env> envs = Arrays.stream((Object[]) environments.getParameterValues().get("value").getValue())
					                       .map(obj -> (AnnotationEnumValue) obj)
					                       .map(value -> Env.valueOf(value.getValueName()))
					                       .toList();

			if (!Env.applies(envs))
				return false;
		}

		return true;
	}

	@SneakyThrows
	public static Number getMaxValue(Class<?> type) {
		return (Number) getMinMaxHolder(type).getDeclaredField("MAX_VALUE").get(null);
	}

	@SneakyThrows
	public static Number getMinValue(Class<?> type) {
		return (Number) getMinMaxHolder(type).getDeclaredField("MIN_VALUE").get(null);
	}

	public static Class<?> getMinMaxHolder(Class<?> type) {
		if (Integer.class == type || Integer.TYPE == type) return Integer.class;
		if (Double.class == type || Double.TYPE == type) return Double.class;
		if (Float.class == type || Float.TYPE == type) return Float.class;
		if (Short.class == type || Short.TYPE == type) return Short.class;
		if (Long.class == type || Long.TYPE == type) return Long.class;
		if (Byte.class == type || Byte.TYPE == type) return Byte.class;
		if (BigDecimal.class == type) return Double.class;
		throw new InvalidInputException("No min/max holder defined for " + type.getSimpleName());
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface SerializedExclude {
	}

	private static final ExclusionStrategy strategy = new ExclusionStrategy() {
		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}

		@Override
		public boolean shouldSkipField(FieldAttributes field) {
			return field.getAnnotation(SerializedExclude.class) != null;
		}
	};

	@Getter
	private static final Gson gson = new GsonBuilder().addSerializationExclusionStrategy(strategy).create();

	@Data
	@AllArgsConstructor
	public static class MinMaxResult<T> {
		private final T object;
		private final Number value;

		public int getInteger() {
			return value.intValue();
		}

		public double getDouble() {
			return value.doubleValue();
		}

		public float getFloat() {
			return value.floatValue();
		}

		public byte getByte() {
			return value.byteValue();
		}

		public short getShort() {
			return value.shortValue();
		}

		public long getLong() {
			return value.longValue();
		}
	}

	@AllArgsConstructor
	public enum ArithmeticOperator {
		ADD((n1, n2) -> n1.doubleValue() + n2.doubleValue()),
		SUBTRACT((n1, n2) -> n1.doubleValue() - n2.doubleValue()),
		MULTIPLY((n1, n2) -> n1.doubleValue() * n2.doubleValue()),
		DIVIDE((n1, n2) -> n1.doubleValue() / n2.doubleValue()),
		POWER((n1, n2) -> Math.pow(n1.doubleValue(), n2.doubleValue()));

		private final BiFunction<Number, Number, Number> function;

		public Number run(Number number1, Number number2) {
			return function.apply(number1, number2);
		}
	}

	@AllArgsConstructor
	public enum ComparisonOperator {
		LESS_THAN((n1, n2) -> n1.doubleValue() < n2.doubleValue()),
		GREATER_THAN((n1, n2) -> n1.doubleValue() > n2.doubleValue()),
		LESS_THAN_OR_EQUAL_TO((n1, n2) -> n1.doubleValue() <= n2.doubleValue()),
		GREATER_THAN_OR_EQUAL_TO((n1, n2) -> n1.doubleValue() >= n2.doubleValue());

		private final BiPredicate<Number, Number> predicate;

		public boolean run(Number number1, Number number2) {
			return predicate.test(number1, number2);
		}
	}

	public static <T> MinMaxResult<T> getMax(Collection<T> things, Function<T, Number> getter) {
		return getMinMax(things, getter, ComparisonOperator.GREATER_THAN);
	}

	public static <T> MinMaxResult<T> getMin(Collection<T> things, Function<T, Number> getter) {
		return getMinMax(things, getter, ComparisonOperator.LESS_THAN);
	}

	private static <T> MinMaxResult<T> getMinMax(Collection<T> things, Function<T, Number> getter, ComparisonOperator operator) {
		Number number = operator == ComparisonOperator.LESS_THAN ? Double.MAX_VALUE : 0;
		T result = null;

		for (T thing : things) {
			Number value = getter.apply(thing);
			if (value == null)
				continue;

			if (operator.run(value.doubleValue(), number.doubleValue())) {
				number = value;
				result = thing;
			}
		}

		return new MinMaxResult<>(result, number);
	}

	public static boolean isBoolean(Parameter parameter) {
		return parameter.getType() == Boolean.class || parameter.getType() == Boolean.TYPE;
	}

	public static <T> T getDefaultPrimitiveValue(Class<T> clazz) {
		return (T) Array.get(Array.newInstance(clazz, 1), 0);
	}

	public static String asParsableDecimal(String value) {
		if (value == null)
			return "0";

		value = value.replace("$", "");
		if (value.contains(",") && value.contains("."))
			if (value.indexOf(",") < value.indexOf("."))
				value = value.replaceAll(",", "");
			else {
				value = value.replaceAll("\\.", "");
				value = value.replaceAll(",", ".");
			}
		else if (value.contains(",") && value.indexOf(",") == value.lastIndexOf(","))
			if (value.indexOf(",") == value.length() - 3)
				value = value.replace(",", ".");
			else
				value = value.replace(",", "");
		return value;
	}

	public static boolean isLong(String text) {
		try {
			Long.parseLong(text);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isInt(String text) {
		try {
			Integer.parseInt(text);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	public static boolean isDouble(String text) {
		try {
			Double.parseDouble(text);
			return true;
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Removes any element from an iterable that passes the {@code predicate}.
	 *
	 * @param predicate the predicate which returns true when an element should be removed
	 * @param from      collection to remove an object from
	 * @return whether an object was removed
	 */
	@Contract(mutates = "param2")
	public static <T> boolean removeIf(Predicate<T> predicate, Iterable<T> from) {
		Objects.requireNonNull(predicate, "predicate");
		Objects.requireNonNull(from, "from");

		boolean removed = false;
		Iterator<T> iterator = from.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.test(item)) {
				iterator.remove();
				removed = true;
			}
		}
		return removed;
	}

	/**
	 * Removes any element from an iterable that passes the {@code predicate}
	 * and applies it to {@code consumer}.
	 *
	 * @param predicate the predicate which returns true when an element should be removed
	 * @param consumer  consumer to perform an action on removed elements
	 * @param from      collection to remove an object from
	 * @return whether an object was removed
	 */
	@Contract(mutates = "param2")
	public static <T> boolean removeIf(Predicate<T> predicate, Consumer<T> consumer, Iterable<T> from) {
		Objects.requireNonNull(predicate, "predicate");
		Objects.requireNonNull(from, "from");

		boolean removed = false;
		Iterator<T> iterator = from.iterator();
		while (iterator.hasNext()) {
			T item = iterator.next();
			if (predicate.test(item)) {
				consumer.accept(item);
				iterator.remove();
				removed = true;
			}
		}
		return removed;
	}

}
