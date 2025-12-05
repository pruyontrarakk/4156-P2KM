package com.example.market.model.stock;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockBarEqualsBranchesTest {

    private StockBar bar(String ts, String o, String h, String l, String c, long v) {
        return new StockBar(
                ts,
                new BigDecimal(o),
                new BigDecimal(h),
                new BigDecimal(l),
                new BigDecimal(c),
                v
        );
    }

    @Test
    void testEqualsSelf() {
        StockBar a = bar("T", "1","2","3","4", 10);
        assertTrue(a.equals(a)); // covers "this == o"
    }

    @Test
    void testEqualsNull() {
        StockBar a = bar("T", "1","2","3","4", 10);
        assertFalse(a.equals(null));  // covers o == null
    }

    @Test
    void testEqualsDifferentType() {
        StockBar a = bar("T","1","2","3","4",10);
        assertFalse(a.equals("not a StockBar")); // covers instanceof false
    }

    @Test
    void testEqualsTimestampMismatch() {
        StockBar a = bar("2025","1","2","3","4",10);
        StockBar b = bar("2024","1","2","3","4",10);
        assertFalse(a.equals(b)); // covers timestamp !=
    }

    @Test
    void testEqualsOpenMismatch() {
        StockBar a = bar("2025","1","2","3","4",10);
        StockBar b = bar("2025","1.01","2","3","4",10);
        assertFalse(a.equals(b)); // covers open !=
    }

    @Test
    void testEqualsHighMismatch() {
        StockBar a = bar("2025","1","2","3","4",10);
        StockBar b = bar("2025","1","2.22","3","4",10);
        assertFalse(a.equals(b)); // covers high !=
    }

    @Test
    void testEqualsLowMismatch() {
        StockBar a = bar("2025","1","2","3","4",10);
        StockBar b = bar("2025","1","2","9.99","4",10);
        assertFalse(a.equals(b)); // covers low !=
    }

    @Test
    void testEqualsVolumeMismatch() {
        StockBar a = bar("2025","1","2","3","4",10);
        StockBar b = bar("2025","1","2","3","4",20);
        assertFalse(a.equals(b)); // covers volume !=
    }

    @Test
    void testEqualsAllFieldsMatch() {
        StockBar a = bar("2025","1","2","3","4",10);
        StockBar b = bar("2025","1","2","3","4",10);
        assertTrue(a.equals(b));  // covers all TRUE path
    }
}