package com.github.onsdigital.brian.publishers;

import com.github.onsdigital.content.page.base.PageDescription;
import com.github.onsdigital.content.page.statistics.data.timeseries.TimeSeries;
import com.github.onsdigital.content.partial.TimeseriesValue;
import com.github.onsdigital.content.partial.markdown.MarkdownSection;
import com.github.onsdigital.brian.data.TimeSeriesObject;
import com.github.onsdigital.brian.data.objects.TimeSeriesPoint;

import java.util.*;

import static com.github.onsdigital.logging.v2.event.SimpleEvent.info;

/**
 * Created by thomasridd on 09/06/15.
 */
public class TimeSeriesPublisher {

    public static List<TimeSeries> convertToContentLibraryTimeSeriesList(List<TimeSeriesObject> timeSeriesObjectList) {

        info().log("converting timeseries list");

        List<TimeSeries> results = new ArrayList<>();
        for (TimeSeriesObject timeSeriesObject: timeSeriesObjectList) {
            results.add(TimeSeriesPublisher.convertToContentLibraryTimeSeries(timeSeriesObject));
        }

        info().log("completed converting timeseries list");

        return results;
    }

    public static TimeSeries convertToContentLibraryTimeSeries(TimeSeriesObject timeSeriesObject) {

        info().data("cdid",timeSeriesObject.taxi).log("converting timeseries");

        TimeSeries timeSeriesPage = new TimeSeries();

        PageDescription description = new PageDescription();
        description.setCdid(timeSeriesObject.taxi);
        description.setTitle(timeSeriesObject.name);

        MarkdownSection section = new MarkdownSection();
        section.setTitle("description");
        section.setMarkdown(timeSeriesObject.name);

        timeSeriesPage.setSection(new MarkdownSection());
        timeSeriesPage.setDescription(description);

        for (String key: timeSeriesObject.points.keySet()) {
            TimeSeriesPoint point = timeSeriesObject.points.get(key);
            TimeseriesValue value = convertToContentLibaryTimeseriesValue(point);

            timeSeriesPage.add(value);
        }

        TimeseriesValue value = getTimeSeriesHighlightPoint(timeSeriesPage);
        timeSeriesPage.getDescription().setNumber(value.value);
        timeSeriesPage.getDescription().setDate(value.date);

        return timeSeriesPage;
    }

    static TimeseriesValue convertToContentLibaryTimeseriesValue(TimeSeriesPoint point) {
        final String[] shortMonths = {"JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"};
        final String[] longMonths = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};

        TimeseriesValue value = new TimeseriesValue();
        value.value = point.value;
        value.year = point.timeLabel.substring(0,4);
        value.date = value.year;

        value.month = "";
        if (point.period.equalsIgnoreCase("months")) {
            String monthNumber = point.timeLabel.substring(point.timeLabel.length()-2, point.timeLabel.length());
            int monthIndex = Integer.parseInt(monthNumber) - 1;

            value.month = longMonths[monthIndex];
            value.date = value.year + " " + shortMonths[monthIndex];
        }

        value.quarter = "";
        if (point.period.equalsIgnoreCase("quarters")) {
            value.quarter = point.timeLabel.substring(point.timeLabel.length()-2, point.timeLabel.length());
            value.date = value.year + " " + value.quarter;
        }

        return value;
    }

    static TimeseriesValue getTimeSeriesHighlightPoint(TimeSeries series) {
        List<TimeseriesValue> values = new ArrayList<>();

        if (series.months != null && series.months.size() > 0) {
            Iterator<TimeseriesValue> iterator = series.months.iterator();
            while (iterator.hasNext()) {
                values.add(iterator.next());
            }
        } else if (series.quarters != null && series.quarters.size() > 0) {
            Iterator<TimeseriesValue> iterator = series.quarters.iterator();
            while (iterator.hasNext()) {
                values.add(iterator.next());
            }
        } else if (series.years != null && series.years.size() > 0) {
            Iterator<TimeseriesValue> iterator = series.years.iterator();
            while (iterator.hasNext()) {
                values.add(iterator.next());
            }
        }

        class CustomComparator implements Comparator<TimeseriesValue> {
            @Override
            public int compare(TimeseriesValue o1, TimeseriesValue o2) {
                return o1.toDate().compareTo(o2.toDate());
            }
        }

        if (values.size() > 0) {
            Collections.sort(values, new CustomComparator());
            TimeseriesValue value = values.get(values.size() - 1);
            return value;
        }
        return null;
    }
}
