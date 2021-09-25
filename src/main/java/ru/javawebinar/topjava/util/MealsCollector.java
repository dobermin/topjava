package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalTime;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class MealsCollector implements Collector<List<UserMeal>, List<UserMeal>, List<UserMealWithExcess>> {

	private static LocalTime startTime, endTime;
	private static int caloriesPerDay;

	private MealsCollector () {
	}

	public static MealsCollector toUserMealWithExcess (LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
		MealsCollector.startTime = startTime;
		MealsCollector.endTime = endTime;
		MealsCollector.caloriesPerDay = caloriesPerDay;

		return new MealsCollector();
	}

	@Override
	public Supplier<List<UserMeal>> supplier () {
		return LinkedList::new;
	}

	@Override
	public BiConsumer<List<UserMeal>, List<UserMeal>> accumulator () {
		return List::addAll;
	}

	@Override
	public BinaryOperator<List<UserMeal>> combiner () {
		return (a, b) -> {
			a.addAll(b);
			return a;
		};
	}

	@Override
	public Function<List<UserMeal>, List<UserMealWithExcess>> finisher () {
		return userMeals -> {
			Map<Integer, Integer> dayCalories =
				userMeals.stream().collect(Collectors.groupingBy(s -> s.getDateTime().getDayOfMonth(),
					Collectors.summingInt(UserMeal::getCalories)));
			return userMeals.stream()
				.filter(s -> s.getDateTime().getHour() >= startTime.getHour())
				.filter(s -> s.getDateTime().getMinute() >= startTime.getMinute())
				.filter(e -> e.getDateTime().getHour() < endTime.getHour() + (endTime.getMinute() == 0 ? 0 : 1))
				.filter(e -> e.getDateTime().getMinute() <= (endTime.getMinute() == 0 ? 59 : endTime.getMinute() - 1))
				.map(
					m -> new UserMealWithExcess(m.getDateTime(), m.getDescription(), m.getCalories(),
						dayCalories.get(m.getDateTime().getDayOfMonth()) > caloriesPerDay)
				).collect(Collectors.toList());
		};
	}

	@Override
	public Set<Characteristics> characteristics () {
		return EnumSet.of(Characteristics.CONCURRENT);
	}
}
