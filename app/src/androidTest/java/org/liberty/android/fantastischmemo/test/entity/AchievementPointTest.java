package org.liberty.android.fantastischmemo.test.entity;
import org.junit.Test;
import org.liberty.android.fantastischmemo.entity.AchievementPoint;
import org.liberty.android.fantastischmemo.entity.Deck;
import org.liberty.android.fantastischmemo.entity.Tag;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
/**
 * Created by dylanfernandes on 2018-03-02.
 */

public class AchievementPointTest {
    private AchievementPoint p = new AchievementPoint();

    @Test
    public void testSetGetId(){
        p.setId(1);
        assertEquals(1,(int)p.getId());
    }
    @Test
    public void testGetSetTime(){
        Date d = new Date();
        p.setTime(d);
        assertEquals(d, p.getTime());
    }

    @Test
    public void testGetSetTag(){
        Tag t = new Tag("test");
        Tag insertedTag;
        p.setTag(t);
        insertedTag = p.getTag();
        assertEquals(t.getName(),insertedTag.getName());
        assertEquals(t,insertedTag);
    }

    @Test
    public void testGetSetValue(){
        p.setValue(1);
        assertEquals(1,(int)p.getValue());
    }

    @Test
    public void testGetSetDeck(){
        Deck d = new Deck();
        Deck insertedDeck;
        d.setName("test");
        p.setDeck(d);
        insertedDeck = p.getDeck();
        assertEquals(d.getName(), insertedDeck.getName());
        assertEquals(d,insertedDeck);
    }

}
