package com.beshev.arenashift.test.ins;


import android.database.Cursor;
import android.support.test.runner.AndroidJUnit4;

import com.beshev.arenashift.beans.Shift;
import com.beshev.arenashift.database.ShiftsManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ShiftsManagerTest {

    private ShiftsManager shiftsManager = new ShiftsManager(getTargetContext());

    @Before
    public void setUp() throws Exception {

        Shift firstShift = new Shift();
        firstShift.setYear(2016);
        firstShift.setMonth(3);
        firstShift.setDay(1);
        firstShift.setPanMehanik("Венци");
        firstShift.setPanKasaOne("Наталия");
        firstShift.setPanKasaTwo("Елица");
        firstShift.setPanKasaThree("няма");
        firstShift.setRazporeditelOne("Дафинка");
        firstShift.setRazporeditelTwo("Бинка");

        shiftsManager.addShift(firstShift);

        shiftsManager.closeDataBase();
    }

    @After
    public void tearDown() throws Exception {

        shiftsManager.closeDataBase();
    }

    @Test
    public void testAddShiftUpdateShift() {

        Shift updateShift = new Shift();
        updateShift.setYear(2016);
        updateShift.setMonth(3);
        updateShift.setDay(1);
        updateShift.setPanMehanik("Александър");
        updateShift.setPanKasaOne("Наталия");
        updateShift.setPanKasaTwo("Елица");
        updateShift.setPanKasaThree("няма");
        updateShift.setRazporeditelOne("Дафинка");
        updateShift.setRazporeditelTwo("Бинка");

        shiftsManager.addShift(updateShift);

        assertEquals("Александър", shiftsManager.getShift(2016, 3, 1).getPanMehanik());
    }

    @Test
    public void testAddShiftCreateNewTable() {

        Shift newShift = new Shift();
        newShift.setYear(2017);
        newShift.setMonth(3);
        newShift.setDay(1);
        newShift.setPanMehanik("Александър");
        newShift.setPanKasaOne("Наталия");
        newShift.setPanKasaTwo("Елица");
        newShift.setPanKasaThree("няма");
        newShift.setRazporeditelOne("Дафинка");
        newShift.setRazporeditelTwo("Бинка");

        shiftsManager.addShift(newShift);

        assertEquals("Александър", shiftsManager.getShift(2017, 3, 1).getPanMehanik());

    }

    @Test
    public void testGetShift() {

        Shift shift = shiftsManager.getShift(2016, 3, 1);

        assertEquals("Венци", shift.getPanMehanik());
        assertEquals("Наталия", shift.getPanKasaOne());
        assertEquals("Елица", shift.getPanKasaTwo());
        assertEquals("няма", shift.getPanKasaThree());
        assertEquals("Дафинка", shift.getRazporeditelOne());
        assertEquals("Бинка", shift.getRazporeditelTwo());

    }

    @Test
    public void testGetShiftNoShift() {

        assertNull(shiftsManager.getShift(2016, 3, 2));	// Day is empty

        // Table '2224' does not exist
        assertNull(shiftsManager.getShift(2224, 3, 2));

    }

    @Test
    public void testGetShiftWithCursor() {

        Cursor cursor = shiftsManager.getShiftWithCursor(2016, 3, 1);

        assertTrue(cursor.moveToFirst());
        assertEquals("Венци", cursor.getString(3));
        assertEquals("Наталия", cursor.getString(4));
        assertEquals("Елица", cursor.getString(5));
        assertEquals("няма", cursor.getString(6));
        assertEquals("Дафинка", cursor.getString(7));
        assertEquals("Бинка", cursor.getString(8));

    }
}
