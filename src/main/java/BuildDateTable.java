import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BuildDateTable {
    public static void main(String[] args) throws ParseException {
        System.out.println(BuildDateTable.buildDataTable("2017-11-30", "2017-12-10"));
    }

    public static List<String> buildDataTable(String startTime, String endTime) throws ParseException {
        int startYear = Integer.valueOf(startTime.split("-")[0]);
        int endYear = Integer.valueOf(endTime.split("-")[0]);
        int startMonth = Integer.valueOf(startTime.split("-")[1]);
        int endMonth = Integer.valueOf(endTime.split("-")[1]);
        int startDay = Integer.valueOf(startTime.split("-")[2]);
        int endDay = Integer.valueOf(endTime.split("-")[2]);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate = sdf.parse(startTime);
        Date endDate = sdf.parse(endTime);
        if (startYear < endYear) {
            return getDatesBetweenTwoDate(startDate, endDate);
        } else if (startYear == endYear) {
            if (startMonth < endMonth) {
                return getDatesBetweenTwoDate(startDate, endDate);
            } else if (startMonth == endMonth) {
                if (startDay <= endDay) {
                    return getDatesBetweenTwoDate(startDate, endDate);
                } else {
                    return Collections.emptyList();
                }
            } else {
                return Collections.emptyList();
            }
        } else {
            return Collections.emptyList();
        }
    }

    private static List<String> getDatesBetweenTwoDate(Date startTime, Date endTime) {
        List<String> dateTable = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (!checkWeekends(startTime)) {
            dateTable.add(sdf.format(startTime));
        }
        if (!sdf.format(startTime).equals(sdf.format(endTime))) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(startTime);
            while (true) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
                if (endTime.after(cal.getTime())) {
                    if (!checkWeekends(cal.getTime())) {
                        dateTable.add(sdf.format(cal.getTime()));
                    }
                } else {
                    break;
                }
            }
            if (!checkWeekends(endTime)) {
                dateTable.add(sdf.format(endTime));
            }
        }
        return dateTable;
    }

    static boolean checkWeekends(String date) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        Date nowDate;
        try {
            nowDate = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
        c.setTime(nowDate);
        int timeScope = c.get(Calendar.DAY_OF_WEEK);
        return timeScope == Calendar.SUNDAY || timeScope == Calendar.SATURDAY;
    }

    static boolean checkWeekends(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        int timeScope = c.get(Calendar.DAY_OF_WEEK);
        return timeScope == Calendar.SUNDAY || timeScope == Calendar.SATURDAY;
    }
}
