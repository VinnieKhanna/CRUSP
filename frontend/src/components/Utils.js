import moment from 'moment';

const JAVA_MAX_INT = 2147483647;

export function nanosToDate(nanos) {
   let millisSinceEpoch = Math.round(nanos/1000000);

   let epochTime = new Date(0);
   epochTime.setUTCMilliseconds(millisSinceEpoch);

   return epochTime.toLocaleDateString("de-DE");
}

export function nanosToTime(nanos) {
   let millisSinceEpoch = Math.round(nanos/1000000);

   let epochTime = new Date(0);
   epochTime.setUTCMilliseconds(millisSinceEpoch);

   return epochTime.toLocaleTimeString("de-DE");
}

export function dateToNanos(date) { //gets a date in the format YYYY-MM-DD and returns a string representing the date in nano-seconds
   return date.getTime() + "000000";
}

export function getNextDayForDate(date) {
   let nextDay = moment(date).add(1, "days");
   return new Date(nextDay.year(),nextDay.month(), nextDay.date());
}

export function normDate(date) {
   date.setHours(0);
   date.setMinutes(0);
   date.setSeconds(0);
   date.setMilliseconds(0);
   return date;
}

export const fnRoundTwoAfterComma = ({value}) => (value != null && (value !== 0) ? Math.round(value*100)/100 : "-");

export const fnFormatCell = ({value}) => (value !== JAVA_MAX_INT && (value != null) ? value : "-");

export const fnFormatCellAndMap = ({value}, fnMapper) => (value !== JAVA_MAX_INT && (value != null) ? fnMapper(value) : "-");