package com.beshev.arenashift.test.ins;

import android.support.test.runner.AndroidJUnit4;

import com.beshev.arenashift.beans.ArenaShiftEvent;
import com.beshev.arenashift.database.EventsManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

@RunWith(AndroidJUnit4.class)
public class EventsManagerTest {

    private EventsManager eventsManager = new EventsManager(getTargetContext());
    private Date firstWhenIsChekedDate = new Date();
    private Date secondWhenIsChekedDate = new Date();
    private Date thirdWhenIsChekedDate = new Date();

    @Before
    public void setUp() throws Exception {

        eventsManager.emptyTableEvents();

        eventsManager.logDayEvent("Венци", "2017-2-19", firstWhenIsChekedDate);
        eventsManager.logDayEvent("Венци", "2017-2-20", secondWhenIsChekedDate);
        eventsManager.logDayEvent("Венци", "2017-2-28", thirdWhenIsChekedDate);

        eventsManager.closeDataBase();
    }

    @After
    public void tearDown() throws Exception {

        eventsManager.emptyTableEvents();
        eventsManager.closeDataBase();
    }

    @Test
    public void logEventTest() {

        ArrayList<ArenaShiftEvent> resultList = eventsManager.getEvents();

        assertEquals("Венци отвори: 2017-2-19", resultList.get(0).getEvent());
        assertEquals(firstWhenIsChekedDate, resultList.get(0).getDate());
    }

    @Test
    public void logException() {

        Date date = new Date();
        eventsManager.logException("Венци", "NullPointerException", date);

        ArrayList<ArenaShiftEvent> resultList = eventsManager.getEvents();

        ArenaShiftEvent event = resultList.get(3);

        assertEquals("Венци грешка: NullPointerException", event.getEvent());
        assertEquals(date, event.getDate());
    }

    @Test
    public void getEventsTest() {

        ArrayList<ArenaShiftEvent> resultList = eventsManager.getEvents();

        assertEquals(3, resultList.size());
        assertEquals("Венци отвори: 2017-2-19", resultList.get(0).getEvent());
        assertEquals(firstWhenIsChekedDate, resultList.get(0).getDate());
        assertEquals("Венци отвори: 2017-2-20", resultList.get(1).getEvent());
        assertEquals(secondWhenIsChekedDate, resultList.get(1).getDate());
        assertEquals("Венци отвори: 2017-2-28", resultList.get(2).getEvent());
        assertEquals(thirdWhenIsChekedDate, resultList.get(2).getDate());
    }

    @Test
    public void getEventsAsHashMapTest() {

        HashMap<Integer, ArenaShiftEvent> resultHashMap = eventsManager.getEventsAsHashMap();
        List<ArenaShiftEvent> resultList = new ArrayList<>(resultHashMap.values());

        assertEquals(3, resultList.size());
        assertEquals("Венци отвори: 2017-2-19", resultList.get(0).getEvent());
        assertEquals(firstWhenIsChekedDate, resultList.get(0).getDate());
        assertEquals("Венци отвори: 2017-2-28", resultList.get(1).getEvent());
        assertEquals(secondWhenIsChekedDate, resultList.get(1).getDate());
        assertEquals("Венци отвори: 2017-2-20", resultList.get(2).getEvent());
        assertEquals(thirdWhenIsChekedDate, resultList.get(2).getDate());
    }

    @Test
    public void deleteEventsTest() {

        HashMap<Integer, ArenaShiftEvent> resultHashMap = eventsManager.getEventsAsHashMap();

        assertNotNull(resultHashMap);

        eventsManager.deleteEvents(resultHashMap);

        resultHashMap = eventsManager.getEventsAsHashMap();

        assertNull(resultHashMap);
    }

    @Test
    public void emptyTableEventsTest() {

        eventsManager.emptyTableEvents();

        ArrayList<ArenaShiftEvent> resultList = eventsManager.getEvents();

        assertNull(resultList);
    }

}
