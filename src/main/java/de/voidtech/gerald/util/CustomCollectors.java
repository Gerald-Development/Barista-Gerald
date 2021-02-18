package main.java.de.voidtech.gerald.util;

import java.util.stream.Collector;
import java.util.stream.Collectors;

public class CustomCollectors {
	public static <T> Collector<T, ?, T> toSingleton() {
		return Collectors.collectingAndThen(Collectors.toList(), list -> {
			if (list.size() > 0) {
				return list.get(0);
			}
			return null;
		});
	}
}
