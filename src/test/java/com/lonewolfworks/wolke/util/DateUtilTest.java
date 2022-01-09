package com.lonewolfworks.wolke.util;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;

public class DateUtilTest {

    @Test
    public void shouldGenerateDateString() {
        DateTime dateTime = new DateTime("2018-05-25T09:31:43.086-04:00");
        String date = DateUtil.getDateAsString(dateTime);
        
        DateTimeFormatter fmt = DateTimeFormat.forPattern("MM-dd-yy-hh-mm-ss");
        String expected = fmt.print(dateTime);
        
        Assert.assertEquals(expected, date);
    }
}
