/*
 *                  Copyright (c) Norkart AS 2006-2007
 *
 *            This source code is the property of Norkart AS.
 * Its use by other parties is regulated by license or agreement with Norkart.
 *
 *  IsoCalendarParser.java
 *
 * Created on 15. januar 2008, 11:52
 *
 */

package com.norkart.virtualglobe.util;

import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 *
 * @author runaas
 */
public class IsoCalendarParser {
    
    
    private static boolean check(StringTokenizer st, String token)
    throws NumberFormatException {
        try {
            if (st.nextToken().equals(token)) {
                return true;
            } else {
                throw new NumberFormatException("Missing ["+token+"]");
            }
        } catch (NoSuchElementException ex) {
            return false;
        }
    }
    
    public static Calendar getCalendar(String isodate)
    throws NumberFormatException {
        // YYYY-MM-DDThh:mm:ss.sTZD
        StringTokenizer st = new StringTokenizer(isodate, "-T:.+Z", true);
        
        Calendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        calendar.clear();
        try {
            // Year
            if (st.hasMoreTokens()) {
                int year = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.YEAR, year);
            } else {
                return calendar;
            }
            // Month
            if (check(st, "-") && (st.hasMoreTokens())) {
                int month = Integer.parseInt(st.nextToken()) -1;
                calendar.set(Calendar.MONTH, month);
            } else {
                return calendar;
            }
            // Day
            if (check(st, "-") && (st.hasMoreTokens())) {
                int day = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.DAY_OF_MONTH, day);
            } else {
                return calendar;
            }
            // Hour
            if (check(st, "T") && (st.hasMoreTokens())) {
                int hour = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.HOUR_OF_DAY, hour);
            } else {
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }
            // Minutes
            if (check(st, ":") && (st.hasMoreTokens())) {
                int minutes = Integer.parseInt(st.nextToken());
                calendar.set(Calendar.MINUTE, minutes);
            } else {
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar;
            }
            
            //
            // Not mandatory now
            //
            
            // Secondes
            if (! st.hasMoreTokens()) {
                return calendar;
            }
            String tok = st.nextToken();
            if (tok.equals(":")) { // secondes
                if (st.hasMoreTokens()) {
                    int secondes = Integer.parseInt(st.nextToken());
                    calendar.set(Calendar.SECOND, secondes);
                    if (! st.hasMoreTokens()) {
                        return calendar;
                    }
                    // frac sec
                    tok = st.nextToken();
                    if (tok.equals(".")) {
                        // bug fixed, thx to Martin Bottcher
                        String nt = st.nextToken();
                        while(nt.length() < 3) {
                            nt += "0";
                        }
                        nt = nt.substring( 0, 3 ); //Cut trailing chars..
                        int millisec = Integer.parseInt(nt);
                        //int millisec = Integer.parseInt(st.nextToken()) * 10;
                        calendar.set(Calendar.MILLISECOND, millisec);
                        if (! st.hasMoreTokens()) {
                            return calendar;
                        }
                        tok = st.nextToken();
                    } else {
                        calendar.set(Calendar.MILLISECOND, 0);
                    }
                } else {
                    throw new NumberFormatException("No secondes specified");
                }
            } else {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
            }
            // Timezone
            if (! tok.equals("Z")) { // UTC
                if (! (tok.equals("+") || tok.equals("-"))) {
                    throw new NumberFormatException("only Z, + or - allowed");
                }
                boolean plus = tok.equals("+");
                if (! st.hasMoreTokens()) {
                    throw new NumberFormatException("Missing hour field");
                }
                int tzhour = Integer.parseInt(st.nextToken());
                int tzmin  = 0;
                if (check(st, ":") && (st.hasMoreTokens())) {
                    tzmin = Integer.parseInt(st.nextToken());
                } else {
                    throw new NumberFormatException("Missing minute field");
                }
                if (plus) {
                    calendar.add(Calendar.HOUR, -tzhour);
                    calendar.add(Calendar.MINUTE, -tzmin);
                } else {
                    calendar.add(Calendar.HOUR, tzhour);
                    calendar.add(Calendar.MINUTE, tzmin);
                }
            }
        } catch (NumberFormatException ex) {
            throw new NumberFormatException("["+ex.getMessage()+
                    "] is not an integer");
        }
        return calendar;
    }
    
}
