
package com.blogspot.dibargatin.counterspro.event;

import java.util.Calendar;

import com.blogspot.dibargatin.counterspro.database.Event;
import com.blogspot.dibargatin.counterspro.database.Event.RepeatType;

public class EventManager {
    // ===========================================================
    // Constants
    // ===========================================================

    // ===========================================================
    // Fields
    // ===========================================================

    // ===========================================================
    // Constructors
    // ===========================================================

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods for/from SuperClass/Interfaces
    // ===========================================================

    // ===========================================================
    // Methods
    // ===========================================================
    public long nextEventTime(Event pEvent, Calendar pCalendar) {
        long result = 0;

        pCalendar.setLenient(true);
        pCalendar.set(Calendar.SECOND, 0);
        pCalendar.set(Calendar.MILLISECOND, 0);

        final long nowTime = pCalendar.getTimeInMillis();
        final long eventTime = pEvent.getDate().getTime();

        if (pEvent.getRepeatType() == RepeatType.ONCE) {
            // Один раз
            result = eventTime <= nowTime ? 0 : eventTime;

        } else if (pEvent.getRepeatType() == RepeatType.EVERYDAY) {
            // Ежедневно
            if (eventTime <= nowTime) {
                pCalendar.setTimeInMillis(eventTime);

                final int eventHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int eventMinute = pCalendar.get(Calendar.MINUTE);

                pCalendar.setTimeInMillis(nowTime);

                final int nowHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int nowMinute = pCalendar.get(Calendar.MINUTE);

                if (!(eventHour >= nowHour && eventMinute > nowMinute)) {
                    pCalendar.set(Calendar.DAY_OF_MONTH, pCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                }

                pCalendar.set(Calendar.HOUR_OF_DAY, eventHour);
                pCalendar.set(Calendar.MINUTE, eventMinute);

                result = pCalendar.getTimeInMillis();

            } else {
                result = eventTime;
            }

        } else if (pEvent.getRepeatType() == RepeatType.EVERY_WORKDAY) {
            // Ежедневно по рабочим дням (Пример: для РФ с пн. по пт.; для США с
            // вс. по чт.)
            pCalendar.setTimeInMillis(eventTime);

            final int eventDay = pCalendar.get(Calendar.DAY_OF_WEEK);
            final int eventHour = pCalendar.get(Calendar.HOUR_OF_DAY);
            final int eventMinute = pCalendar.get(Calendar.MINUTE);

            pCalendar.setTimeInMillis(nowTime);

            final int firstDay = pCalendar.getFirstDayOfWeek();
            final int lastDay = pCalendar.getFirstDayOfWeek() + 4;

            final int nowDay = pCalendar.get(Calendar.DAY_OF_WEEK);
            final int nowHour = pCalendar.get(Calendar.HOUR_OF_DAY);
            final int nowMinute = pCalendar.get(Calendar.MINUTE);

            // Судя по времени, сегодня событие уже свершилось, либо сегодня
            // выходной
            if (eventTime <= nowTime || !(nowDay >= firstDay && nowDay <= lastDay)) {

                if (!(eventHour >= nowHour && eventMinute > nowMinute)
                        || !(nowDay >= firstDay && nowDay <= lastDay)) {

                    // Если рабочий день не является последним днем
                    if (nowDay >= firstDay && nowDay < lastDay) {

                        // Следующий за текущим днем
                        pCalendar.set(Calendar.DAY_OF_MONTH,
                                pCalendar.get(Calendar.DAY_OF_MONTH) + 1);
                    } else {
                        // Следующий первый рабочий день
                        pCalendar.set(Calendar.WEEK_OF_MONTH,
                                pCalendar.get(Calendar.WEEK_OF_MONTH) + 1);
                        pCalendar.set(Calendar.DAY_OF_WEEK, pCalendar.getFirstDayOfWeek());
                    }
                }

                pCalendar.set(Calendar.HOUR_OF_DAY, eventHour);
                pCalendar.set(Calendar.MINUTE, eventMinute);

                result = pCalendar.getTimeInMillis();

            } else {
                // Если событие выпадает на рабочий день
                if (eventDay >= firstDay && eventDay <= lastDay) {
                    result = eventTime;

                } else {
                    // Следующий первый рабочий день
                    pCalendar
                            .set(Calendar.WEEK_OF_MONTH, pCalendar.get(Calendar.WEEK_OF_MONTH) + 1);
                    pCalendar.set(Calendar.DAY_OF_WEEK, pCalendar.getFirstDayOfWeek());

                    result = pCalendar.getTimeInMillis();
                }
            }

        } else if (pEvent.getRepeatType() == RepeatType.EVERY_WEEK) {
            // Каждую неделю (Например: во вторник)
            if (eventTime <= nowTime) {
                pCalendar.setTimeInMillis(eventTime);

                final int eventDayOfWeekNum = (pCalendar.get(Calendar.DAY_OF_WEEK) + 7 - pCalendar
                        .getFirstDayOfWeek()) % 7;
                final int eventDayOfWeek = pCalendar.get(Calendar.DAY_OF_WEEK);
                final int eventHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int eventMinute = pCalendar.get(Calendar.MINUTE);

                pCalendar.setTimeInMillis(nowTime);

                final int nowDayOfWeekNum = (pCalendar.get(Calendar.DAY_OF_WEEK) + 7 - pCalendar
                        .getFirstDayOfWeek()) % 7;

                if (nowDayOfWeekNum < eventDayOfWeekNum) {
                    pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);

                } else if ((nowDayOfWeekNum == eventDayOfWeekNum)
                        && !(eventHour >= pCalendar.get(Calendar.HOUR_OF_DAY) && eventMinute > pCalendar
                                .get(Calendar.MINUTE))) {
                    pCalendar
                            .set(Calendar.WEEK_OF_MONTH, pCalendar.get(Calendar.WEEK_OF_MONTH) + 1);
                    pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);

                } else if (nowDayOfWeekNum > eventDayOfWeekNum) {
                    pCalendar
                            .set(Calendar.WEEK_OF_MONTH, pCalendar.get(Calendar.WEEK_OF_MONTH) + 1);
                    pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);
                }

                pCalendar.set(Calendar.HOUR_OF_DAY, eventHour);
                pCalendar.set(Calendar.MINUTE, eventMinute);

                result = pCalendar.getTimeInMillis();

            } else {
                result = eventTime;
            }

        } else if (pEvent.getRepeatType() == RepeatType.EVERY_MONTH) {
            // Ежемесячно (Например: в 17 день)
            if (eventTime <= nowTime) {
                pCalendar.setTimeInMillis(eventTime);

                final int eventDay = pCalendar.get(Calendar.DAY_OF_MONTH);
                final int eventHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int eventMinute = pCalendar.get(Calendar.MINUTE);

                pCalendar.setTimeInMillis(nowTime);

                final int nowDay = pCalendar.get(Calendar.DAY_OF_MONTH);
                final int nowHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int nowMinute = pCalendar.get(Calendar.MINUTE);

                if (nowDay >= eventDay && !(eventHour >= nowHour && eventMinute > nowMinute)) {
                    pCalendar.set(Calendar.MONTH, pCalendar.get(Calendar.MONTH) + 1);
                }

                pCalendar.set(Calendar.DAY_OF_MONTH, eventDay);
                pCalendar.set(Calendar.HOUR_OF_DAY, eventHour);
                pCalendar.set(Calendar.MINUTE, eventMinute);

                result = pCalendar.getTimeInMillis();

            } else {
                result = eventTime;
            }

        } else if (pEvent.getRepeatType() == RepeatType.EVERY_MONTH_DAY_OF_WEEK) {
            // Ежемесячно (Например: каждый третий вт.)
            if (eventTime <= nowTime) {
                pCalendar.setTimeInMillis(eventTime);

                final int eventDayOfWeek = pCalendar.get(Calendar.DAY_OF_WEEK);
                final int eventDayOfWeekInMonth = pCalendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                final int eventHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int eventMinute = pCalendar.get(Calendar.MINUTE);

                pCalendar.setTimeInMillis(nowTime);

                final int nowDayOfWeek = pCalendar.get(Calendar.DAY_OF_WEEK);
                final int nowDayOfWeekInMonth = pCalendar.get(Calendar.DAY_OF_WEEK_IN_MONTH);
                final int nowHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int nowMinute = pCalendar.get(Calendar.MINUTE);

                if (nowDayOfWeek != eventDayOfWeek) {
                    pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);

                    if (pCalendar.get(Calendar.DAY_OF_WEEK_IN_MONTH) >= eventDayOfWeekInMonth) {
                        pCalendar.set(Calendar.MONTH, pCalendar.get(Calendar.MONTH) + 1);
                        pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);
                    }

                    while (pCalendar.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH) < eventDayOfWeekInMonth) {
                        pCalendar.set(Calendar.MONTH, pCalendar.get(Calendar.MONTH) + 1);
                        pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);
                    }

                    pCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, eventDayOfWeekInMonth);

                } else if (nowDayOfWeek == eventDayOfWeek
                        && nowDayOfWeekInMonth > eventDayOfWeekInMonth) {

                    do {
                        pCalendar.set(Calendar.MONTH, pCalendar.get(Calendar.MONTH) + 1);
                        pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);

                    } while (pCalendar.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH) < eventDayOfWeekInMonth);

                    pCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, eventDayOfWeekInMonth);

                } else if (nowDayOfWeek == eventDayOfWeek
                        && nowDayOfWeekInMonth == eventDayOfWeekInMonth
                        && !(eventHour >= nowHour && eventMinute > nowMinute)) {

                    do {
                        pCalendar.set(Calendar.MONTH, pCalendar.get(Calendar.MONTH) + 1);
                        pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);

                    } while (pCalendar.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH) < eventDayOfWeekInMonth);

                    pCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, eventDayOfWeekInMonth);

                } else if (nowDayOfWeek == eventDayOfWeek
                        && nowDayOfWeekInMonth < eventDayOfWeekInMonth) {

                    while (pCalendar.getActualMaximum(Calendar.DAY_OF_WEEK_IN_MONTH) < eventDayOfWeekInMonth) {
                        pCalendar.set(Calendar.MONTH, pCalendar.get(Calendar.MONTH) + 1);
                        pCalendar.set(Calendar.DAY_OF_WEEK, eventDayOfWeek);
                    }

                    pCalendar.set(Calendar.DAY_OF_WEEK_IN_MONTH, eventDayOfWeekInMonth);
                }

                pCalendar.set(Calendar.HOUR_OF_DAY, eventHour);
                pCalendar.set(Calendar.MINUTE, eventMinute);

                result = pCalendar.getTimeInMillis();

            } else {
                result = eventTime;
            }

        } else if (pEvent.getRepeatType() == RepeatType.EVERY_YEAR) {
            // Ежегодно (Например: 17 сентября)
            if (eventTime <= nowTime) {
                pCalendar.setTimeInMillis(eventTime);

                final int eventMonth = pCalendar.get(Calendar.MONTH);
                final int eventDay = pCalendar.get(Calendar.DAY_OF_MONTH);
                final int eventHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int eventMinute = pCalendar.get(Calendar.MINUTE);

                pCalendar.setTimeInMillis(nowTime);

                final int nowMonth = pCalendar.get(Calendar.MONTH);
                final int nowDay = pCalendar.get(Calendar.DAY_OF_MONTH);
                final int nowHour = pCalendar.get(Calendar.HOUR_OF_DAY);
                final int nowMinute = pCalendar.get(Calendar.MINUTE);

                if (nowMonth > eventMonth) {
                    pCalendar.set(Calendar.YEAR, pCalendar.get(Calendar.YEAR) + 1);

                } else if (nowMonth == eventMonth && nowDay > eventDay) {
                    pCalendar.set(Calendar.YEAR, pCalendar.get(Calendar.YEAR) + 1);

                } else if (nowMonth == eventMonth && nowDay == eventDay
                        && !(eventHour >= nowHour && eventMinute > nowMinute)) {

                    pCalendar.set(Calendar.YEAR, pCalendar.get(Calendar.YEAR) + 1);
                }

                pCalendar.set(Calendar.MONTH, eventMonth);
                pCalendar.set(Calendar.DAY_OF_MONTH, eventDay);
                pCalendar.set(Calendar.HOUR_OF_DAY, eventHour);
                pCalendar.set(Calendar.MINUTE, eventMinute);

                result = pCalendar.getTimeInMillis();

            } else {
                result = eventTime;
            }
        }

        return result;
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================
}
