import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

public class TaxCalTest {
    private TaxCal taxCal;

    @BeforeEach
    void setUp() {
        taxCal = new TaxCal();
        taxCal.setThreshold(new BigDecimal("5000"));
        taxCal.setNumLevel(7);
        taxCal.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("3000"),  new BigDecimal("0.03")},
                {new BigDecimal("12000"), new BigDecimal("0.10")},
                {new BigDecimal("25000"), new BigDecimal("0.20")},
                {new BigDecimal("35000"), new BigDecimal("0.25")},
                {new BigDecimal("55000"), new BigDecimal("0.30")},
                {new BigDecimal("80000"), new BigDecimal("0.35")},
                {new BigDecimal("-1"),    new BigDecimal("0.45")},
        });
    }

    // 算法正确性检测
    @Test
    void testAccuracy() {
        // 测例1: 题目中张三的例子
        // 起征点5000，标准7档税率，薪资50000，预期税款9090
        TaxCal tc1 = new TaxCal();
        tc1.setThreshold(new BigDecimal("5000"));
        tc1.setNumLevel(7);
        tc1.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("3000"),  new BigDecimal("0.03")},
                {new BigDecimal("12000"), new BigDecimal("0.10")},
                {new BigDecimal("25000"), new BigDecimal("0.20")},
                {new BigDecimal("35000"), new BigDecimal("0.25")},
                {new BigDecimal("55000"), new BigDecimal("0.30")},
                {new BigDecimal("80000"), new BigDecimal("0.35")},
                {new BigDecimal("-1"),    new BigDecimal("0.45")},
        });
        // 45000应纳税所得额: 3000*0.03+9000*0.1+13000*0.2+10000*0.25+10000*0.3
        // = 90+900+2600+2500+3000 = 9090
        assertEquals(0, tc1.calTax(new BigDecimal("50000")).compareTo(new BigDecimal("9090")));

        // 测例2: 起征点0，单级税率
        // 薪资1000，税率10%，预期税款100
        TaxCal tc2 = new TaxCal();
        tc2.setThreshold(BigDecimal.ZERO);
        tc2.setNumLevel(1);
        tc2.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("-1"), new BigDecimal("0.1")},
        });
        assertEquals(0, tc2.calTax(new BigDecimal("1000")).compareTo(new BigDecimal("100")));

        // 测例3: 起征点3000，2档税率
        // 薪资8000，应纳税所得额5000
        // 0-2000部分: 2000*0.2=400
        // 2000以上部分: 3000*0.4=1200
        // 合计: 1600
        TaxCal tc3 = new TaxCal();
        tc3.setThreshold(new BigDecimal("3000"));
        tc3.setNumLevel(2);
        tc3.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("2000"), new BigDecimal("0.20")},
                {new BigDecimal("-1"),   new BigDecimal("0.40")},
        });
        assertEquals(0, tc3.calTax(new BigDecimal("8000")).compareTo(new BigDecimal("1600")));

        // 测例4: 薪资恰好等于起征点，预期税款为0
        TaxCal tc4 = new TaxCal();
        tc4.setThreshold(new BigDecimal("5000"));
        tc4.setNumLevel(1);
        tc4.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("-1"), new BigDecimal("0.1")},
        });
        assertEquals(0, tc4.calTax(new BigDecimal("5000")).compareTo(BigDecimal.ZERO));

        // 测例5: 税率不随税级递增的情况
        // 起征点200，3档：0-400税率30%，400-600税率10%，600以上税率40%
        // 薪资900，应纳税所得额700
        // 400*0.3+200*0.1+100*0.4 = 120+20+40 = 180
        TaxCal tc5 = new TaxCal();
        tc5.setThreshold(new BigDecimal("200"));
        tc5.setNumLevel(3);
        tc5.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("400"), new BigDecimal("0.3")},
                {new BigDecimal("600"), new BigDecimal("0.1")},
                {new BigDecimal("-1"),  new BigDecimal("0.4")},
        });
        assertEquals(0, tc5.calTax(new BigDecimal("900")).compareTo(new BigDecimal("180")));
    }

    @Test
    void testBelowThreshold() {
        assertEquals(0, taxCal.calTax(new BigDecimal("4000")).compareTo(BigDecimal.ZERO));
    }

    @Test
    void testFirstBracket() {
        // 8000-5000=3000, 3000*0.03-0=90
        assertEquals(0, taxCal.calTax(new BigDecimal("8000")).compareTo(new BigDecimal("90")));
    }

    @Test
    void testMiddleBracket() {
        // 20000-5000=15000, 15000*0.20-1410=1590
        assertEquals(0, taxCal.calTax(new BigDecimal("20000")).compareTo(new BigDecimal("1590")));
    }

    @Test
    void testTopBracket() {
        // 100000-5000=95000, 95000*0.45-15160=27590
        assertEquals(0, taxCal.calTax(new BigDecimal("100000")).compareTo(new BigDecimal("27590")));
    }

    // 起征点校验
    @Test
    void testNegativeThreshold() {
        assertThrows(RuntimeException.class, () ->
                taxCal.setThreshold(new BigDecimal("-1")));
    }

    @Test
    void testZeroThreshold() {
        assertDoesNotThrow(() -> taxCal.setThreshold(BigDecimal.ZERO));
    }

    // 税级数校验
    @Test
    void testZeroNumLevel() {
        assertThrows(RuntimeException.class, () -> taxCal.setNumLevel(0));
    }

    @Test
    void testNegativeNumLevel() {
        assertThrows(RuntimeException.class, () -> taxCal.setNumLevel(-1));
    }

    // 税表校验
    @Test
    void testTaxTableLengthMismatch() {
        taxCal.setNumLevel(3);
        assertThrows(RuntimeException.class, () ->
                taxCal.setTaxTable(new BigDecimal[][] {
                        {new BigDecimal("3000"), new BigDecimal("0.03")},
                        {new BigDecimal("-1"),   new BigDecimal("0.10")},
                }));
    }

    @Test
    void testNonIncreasingRate() {
        // 税率不递增应当正常接受
        TaxCal tc = new TaxCal();
        tc.setThreshold(BigDecimal.ZERO);
        tc.setNumLevel(3);
        assertDoesNotThrow(() -> tc.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("400"), new BigDecimal("0.3")},
                {new BigDecimal("600"), new BigDecimal("0.1")},
                {new BigDecimal("-1"),  new BigDecimal("0.4")},
        }));
    }

    // 薪资校验
    @Test
    void testZeroSalary() {
        assertThrows(RuntimeException.class, () ->
                taxCal.calTax(BigDecimal.ZERO));
    }

    @Test
    void testNegativeSalary() {
        assertThrows(RuntimeException.class, () ->
                taxCal.calTax(new BigDecimal("-100")));
    }

    // 单级税表
    @Test
    void testSingleLevel() {
        TaxCal tc = new TaxCal();
        tc.setThreshold(BigDecimal.ZERO);
        tc.setNumLevel(1);
        tc.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("-1"), new BigDecimal("0.1")},
        });
        // 1000*0.1=100
        assertEquals(0, tc.calTax(new BigDecimal("1000")).compareTo(new BigDecimal("100")));
    }

    // 恰好在档位边界上
    @Test
    void testOnBracketBoundary() {
        // 5000+3000=8000，恰好在第一档上界
        // 3000*0.03-0=90
        assertEquals(0, taxCal.calTax(new BigDecimal("8000")).compareTo(new BigDecimal("90")));
    }

    // 税率不递增时计算是否正确
    @Test
    void testNonIncreasingRateCalTax() {
        TaxCal tc = new TaxCal();
        tc.setThreshold(new BigDecimal("200"));
        tc.setNumLevel(3);
        tc.setTaxTable(new BigDecimal[][] {
                {new BigDecimal("400"), new BigDecimal("0.3")},
                {new BigDecimal("600"), new BigDecimal("0.1")},
                {new BigDecimal("-1"),  new BigDecimal("0.4")},
        });
        // payable=700, 400*0.3+200*0.1+100*0.4=120+20+40=180
        assertEquals(0, tc.calTax(new BigDecimal("900")).compareTo(new BigDecimal("180")));
    }
}