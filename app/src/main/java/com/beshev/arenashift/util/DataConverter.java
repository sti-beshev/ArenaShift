package com.beshev.arenashift.util;

import java.util.Calendar;
import java.util.HashMap;

public class DataConverter {

	public static HashMap<String, Integer> getDateAsMap(long dateInMillis) {

		HashMap<String, Integer> dateMap = new HashMap<>();

		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dateInMillis);

		dateMap.put("year", calendar.get(Calendar.YEAR));
		dateMap.put("month", calendar.get(Calendar.MONTH));
		dateMap.put("day", calendar.get(Calendar.DAY_OF_MONTH));

		return dateMap;
	}

}
