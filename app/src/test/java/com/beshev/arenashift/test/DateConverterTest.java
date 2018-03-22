package com.beshev.arenashift.test;


import com.beshev.arenashift.util.DataConverter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

public class DateConverterTest {

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void getDateAsMapTest() {

        final long dateInMillis = 1521727325420L; // 22.3.2018

        HashMap<String, Integer> dateMap = DataConverter.getDateAsMap(dateInMillis);

        assertEquals(2018, (int)dateMap.get("year"));
        assertEquals(3, (int)dateMap.get("month")+1); // Month starts from 0 not 1
        assertEquals(22, (int)dateMap.get("day"));
    }
}
