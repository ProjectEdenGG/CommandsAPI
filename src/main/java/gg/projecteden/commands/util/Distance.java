package gg.projecteden.commands.util;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

public class Distance implements Comparable<Distance> {
	private final Location from;
	private final Location to;
	private final double distanceSquared;

	private Distance(Location from, Location to) {
		this.from = from;
		this.to = to;
		this.distanceSquared = from.distanceSquared(to);
	}

	public static Distance distance(Location from, Location to) {
		return new Distance(from, to);
	}

	public double get() {
		return distanceSquared;
	}

	// Math.sqrt is costly, only use for display
	public double getRealDistance() {
		return Math.sqrt(distanceSquared);
	}

	private double square(double distance) {
		return distance * distance;
	}

	public boolean lt(double distance) {
		return from.distanceSquared(to) < square(distance);
	}

	public boolean gt(double distance) {
		return from.distanceSquared(to) > square(distance);
	}

	public boolean lte(double distance) {
		return from.distanceSquared(to) <= square(distance);
	}

	public boolean gte(double distance) {
		return from.distanceSquared(to) >= square(distance);
	}

	public boolean eq(double distance) {
		return from.distanceSquared(to) == square(distance);
	}

	@Override
	public int compareTo(@NotNull Distance o) {
		return Double.compare(distanceSquared, o.distanceSquared);
	}

}
