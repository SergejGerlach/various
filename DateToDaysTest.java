package de.sergejgerlach.various;

import org.junit.Test;

import java.time.LocalDate;
import java.time.chrono.IsoChronology;

import static org.assertj.core.api.Assertions.assertThat;
/*
#define DATE_MIN_YEAR 1800
#define DATE_MIN_DAY 657072L // serialisierte Tageszahl von 01.01.DATE_MIN_YEAR

#define DATE_MAX_YEAR 2200
#define DATE_MAX_DAY 803533L // serialisierte Tageszahl von 31.12.DATE_MAX_YEAR


dateday(daydate(730484;"D.M.Y")) liefert 730485 anstatt 730484.
Umgekehrt:
daydate(dateday("30.12.2000");"D.M.Y") liefert 31.12.2000 anstatt 30.12.2000
 */
public class DateToDaysTest {

    private static int[][] daysofmonth = {
            {0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31},
            {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31}};

    private int mathIsLeapYear(int iYear) {
        if(iYear % 400 == 0) return(1);
        if(iYear % 100 == 0) return(0);
        return (iYear % 4 == 0) ? 1 : 0;
    }

    private int mathDate2Julianday(int iDay, int iMonth, int iYear) {
        int lValue = (iYear - 1) * 365 + (iYear - 1) / 4 + (iYear - 1) / 400 - (iYear - 1) / 100;
        while(--iMonth > 0) lValue += daysofmonth[mathIsLeapYear(iYear)][iMonth];
        return(lValue + iDay);
    }

    private LocalDate mathJulianday2Date(int lValue) {
        int uIdx;
        int piDay, piMonth, piYear = 0;
        // nothing to do:
        if(lValue < 0) return null;

        // convert to year:
        piYear = 400 * (lValue / 146097);
        lValue %= 146097;
        if(lValue > 0) {
            piYear += 100 * (lValue / 36524);
            lValue %= 36524;
            if(lValue == 0) {
                // Mantis #6120 : 31.12.1800 [1800, 1900, 2100, 2200, 2300, 2500] rechnet false
                // sge: check a 400 year cycle [... 1600, 2000, 2400, ...]
                lValue = mathIsLeapYear(piYear) == 1 && piYear % 400 != 0 ? 366 : 365;
                (piYear)--;
            } else if(lValue > 0) {
                piYear += 4 * (lValue / 1461);
                lValue %= 1461;
                for(uIdx = 0; (uIdx < 4) && (lValue > 365); uIdx++) {
                    (piYear)++;
                    lValue -= 365;
                }
            }
        }
        (piYear)++;

        // convert to month:
        for(piMonth = 1; piMonth <= 12; (piMonth)++) {
            if(lValue <= daysofmonth[mathIsLeapYear(piYear)][piMonth]) break;
            lValue -= daysofmonth[mathIsLeapYear(piYear)][piMonth];
        }

        if((lValue == 0) && (piMonth == 1)) {
            (piYear)--;
            lValue = 31;
            piMonth = 12;
        }
        piDay = lValue;
        return LocalDate.of(piYear, piMonth, piDay);

    }

    @Test
    public void check_mathIsLeapYear() {
        for (int i = 1800; i <= 2200; i++) {
            boolean result = IsoChronology.INSTANCE.isLeapYear(i) == (mathIsLeapYear(i) == 1);
            assertThat(result).isTrue();
        }
    }

    @Test
    public void check_mathDate2Julianday() {

        LocalDate date = LocalDate.of(1800, 1, 1);
        for (int i = 657072; i <= 803533; i++) {
            int date2Julianday = mathDate2Julianday(date.getDayOfMonth(), date.getMonthValue(), date.getYear());
            // System.out.println(String.format("wrong day = %d, expected = %d, by date %s", date2Julianday, i, date.toString()));
            assertThat(date2Julianday).isEqualTo(i);
            date = date.plusDays(1);
        }
    }

    @Test
    public void check_mathJulianday2Date() {

        LocalDate date = LocalDate.of(1600, 1, 1);
        final int startDays = mathDate2Julianday(date.getDayOfMonth(), date.getMonthValue(), date.getYear()); // 657072;
        final int endDays = mathDate2Julianday(31, 12, 2600); // 803533;
        for (int i = startDays; i <= endDays; i++) {
            LocalDate julianday2Date = mathJulianday2Date(i);
            if (!julianday2Date.equals(date)) {
                System.out.println(String.format("wrong date = %s, expected = %s, by day %d", julianday2Date.toString(), date.toString(), i));
            }
            assertThat(julianday2Date).isEqualTo(date);
            date = date.plusDays(1);
        }
        assertThat(mathJulianday2Date(657072)).isEqualTo(LocalDate.of(1800, 1, 1));
    }

}