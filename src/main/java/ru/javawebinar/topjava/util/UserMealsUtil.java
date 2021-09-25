package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserMealsUtil {
	public static void main (String[] args) {
		List<UserMeal> meals = Arrays.asList(
			new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
			new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
			new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
			new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение",
				100),
			new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
			new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
			new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
		);

		List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0),
			2000);
		mealsTo.forEach(System.out::println);

		System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
		filteredByStreamsOptionTwo(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000)
			.forEach(System.out::println);
	}

	public static List<UserMealWithExcess> filteredByCycles (List<UserMeal> meals, LocalTime startTime,
															 LocalTime endTime, int caloriesPerDay) {
		List<UserMeal> userMeals = getFiltered(meals, startTime, endTime);
		if (userMeals.isEmpty()) return new ArrayList<>();
		return getUserMealWithExcess(userMeals, getDayCalories(meals), caloriesPerDay);
	}

	private static HashMap<Integer, Integer> getDayCalories (List<UserMeal> meals) {
		int day = meals.get(0).getDateTime().getDayOfMonth();
		int calories = 0;
		HashMap<Integer, Integer> dayAndCalories = new HashMap<>();
		for (UserMeal userMeal : meals) {
			int d = userMeal.getDateTime().getDayOfMonth();
			if (day == d) {
				calories += userMeal.getCalories();
			} else {
				dayAndCalories.put(day, calories);
				day = d;
				calories = userMeal.getCalories();
			}
		}
		dayAndCalories.put(day, calories);

		return dayAndCalories;
	}

	private static List<UserMeal> getFiltered (List<UserMeal> meals, LocalTime startTime, LocalTime endTime) {
		List<UserMeal> userMeals = new ArrayList<>();
		for (UserMeal userMeal : meals) {
			int hour = userMeal.getDateTime().getHour();
			int minute = userMeal.getDateTime().getMinute();
			if (
				hour >= startTime.getHour() &&
					minute >= startTime.getMinute() &&
					hour < endTime.getHour() + (endTime.getMinute() == 0 ? 0 : 1) &&
					minute <= (endTime.getMinute() == 0 ? 59 : endTime.getMinute() - 1)
			) userMeals.add(userMeal);
		}

		return userMeals;
	}

	private static List<UserMealWithExcess> getUserMealWithExcess (List<UserMeal> meals,
																   HashMap<Integer, Integer> dayCalories,
																   int caloriesPerDay) {
		List<UserMealWithExcess> userMealWithExcesses = new ArrayList<>();
		for (UserMeal userMeal : meals) {
			userMealWithExcesses.add(
				new UserMealWithExcess(userMeal.getDateTime(), userMeal.getDescription(), userMeal.getCalories(),
					dayCalories.get(userMeal.getDateTime().getDayOfMonth()) > caloriesPerDay)
			);
		}
		return userMealWithExcesses;
	}

	public static List<UserMealWithExcess> filteredByStreams (List<UserMeal> meals, LocalTime startTime,
															  LocalTime endTime, int caloriesPerDay) {
		List<UserMeal> userMeals = getFilteredStream(meals, startTime, endTime);
		if (userMeals.isEmpty()) return new ArrayList<>();
		return getUserMealWithExcessStreams(userMeals, getDayCaloriesStream(meals), caloriesPerDay);
	}

	private static List<UserMealWithExcess> getUserMealWithExcessStreams (List<UserMeal> meals,
																		  Map<Integer, Integer> dayCalories,
																		  int caloriesPerDay) {
		return meals.stream()
			.map(m -> new UserMealWithExcess(m.getDateTime(), m.getDescription(), m.getCalories(),
				dayCalories.get(m.getDateTime().getDayOfMonth()) > caloriesPerDay))
			.collect(Collectors.toList());
	}

	private static Map<Integer, Integer> getDayCaloriesStream (List<UserMeal> meals) {
		return meals.stream()
			.collect(Collectors.groupingBy(s -> s.getDateTime().getDayOfMonth(),
				Collectors.summingInt(UserMeal::getCalories)));
	}

	private static List<UserMeal> getFilteredStream (List<UserMeal> meals, LocalTime startTime, LocalTime endTime) {
		return meals.stream()
			.filter(s -> s.getDateTime().getHour() >= startTime.getHour())
			.filter(s -> s.getDateTime().getMinute() >= startTime.getMinute())
			.filter(e -> e.getDateTime().getHour() < endTime.getHour() + (endTime.getMinute() == 0 ? 0 : 1))
			.filter(e -> e.getDateTime().getMinute() <= (endTime.getMinute() == 0 ? 59 : endTime.getMinute() - 1))
			.collect(Collectors.toList());
	}

	private static List<UserMealWithExcess> filteredByStreamsOptionTwo (List<UserMeal> meals, LocalTime startTime,
													   LocalTime endTime, int caloriesPerDay) {
		return Stream.of(new ArrayList<>(meals))
			.collect(MealsCollector.toUserMealWithExcess(startTime, endTime, caloriesPerDay));
	}

}
