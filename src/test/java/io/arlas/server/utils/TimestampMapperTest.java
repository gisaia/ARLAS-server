package io.arlas.server.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.arlas.server.exceptions.ArlasException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimestampMapperTest {

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    public void testGetTimestamp() throws ArlasException, JsonProcessingException {
        assertEquals((new DateTime(2004, 05, 25, 0, 0, 0)).getMillis(),
                TimestampTypeMapper.getTimestamp("2004-05-25", "basic_date||date||epoch_second").longValue());
        assertEquals((new DateTime(1993, 07, 20, 05, 59, 40, 100)).getMillis(),
                TimestampTypeMapper.getTimestamp("20/07/1993 05:59:40.100", "dd/MM/yyy HH:mm:ss.SSS").longValue());
        assertEquals((new DateTime(1970, 01, 1, 20, 15, 54, 125)).getMillis(),
                TimestampTypeMapper.getTimestamp("20:15:54.125", "weekyear||hour_minute_second_millis").longValue());
        assertEquals((new DateTime(2017, 04, 14, 9, 13, 41, 275, DateTimeZone.UTC)).getMillis(),
                TimestampTypeMapper.getTimestamp("2017-04-14T09:13:41.275Z", "strict_date_optional_time").longValue());
        assertEquals(1085436000000l,
                TimestampTypeMapper.getTimestamp(1085436000000L, "epoch_millis").longValue());
        assertTrue(TimestampTypeMapper.getTimestamp("2004-05-25", "epoch_millis") == null);
    }
}
