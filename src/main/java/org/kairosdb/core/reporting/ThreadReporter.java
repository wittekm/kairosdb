/*
 * Copyright 2016 KairosDB Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kairosdb.core.reporting;

import com.google.common.collect.ImmutableSortedMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.kairosdb.core.datapoints.LongDataPointFactory;
import org.kairosdb.core.datapoints.StringDataPointFactory;
import org.kairosdb.core.exception.DatastoreException;
import org.kairosdb.eventbus.Publisher;
import org.kairosdb.events.DataPointEvent;
import org.kairosdb.util.StatsMap;
import org.kairosdb.util.Tags;

import java.util.LinkedList;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 Created with IntelliJ IDEA.
 User: bhawkins
 Date: 9/4/13
 Time: 2:56 PM
 To change this template use File | Settings | File Templates.
 */
public class ThreadReporter
{
	private static final String REPORTER_TTL_KEY = "kairosdb.reporter.ttl";

	@Inject(optional = true)
	@Named(REPORTER_TTL_KEY)
	private static int DEFAULT_REPORTER_TTL = 0;

	/* package */ static class ReporterDataPoint
	{
		private final String m_metricName;
		private final long m_value;
		private final String m_strValue;
		private final ImmutableSortedMap.Builder<String, String> m_tags;
		private final int m_ttl;

		private ReporterDataPoint(String metricName, SortedMap<String, String> tags,
				long value, int ttl)
		{
			m_metricName = metricName;
			m_value = value;
			m_tags = Tags.create();
			m_tags.putAll(tags);
			m_ttl = ttl;
			m_strValue = null;
		}

		private ReporterDataPoint(String metricName, SortedMap<String, String> tags,
				String value, int ttl)
		{
			m_metricName = metricName;
			m_value = 0;
			m_tags = Tags.create();
			m_tags.putAll(tags);
			m_ttl = ttl;
			m_strValue = value;
		}

		public String getMetricName() { return (m_metricName); }
		public long getValue() { return (m_value); }
		public String getStrValue() { return m_strValue; }
		public void addTag(String name, String value)
		{
			m_tags.put(name, value);
		}

		public int getTtl() { return (m_ttl); }
		public boolean isStringValue() { return m_strValue != null; }

		public ImmutableSortedMap<String, String> getTags() { return m_tags.build(); }
	}

	private static class CurrentTags extends ThreadLocal<SortedMap<String, String>>
	{
		@Override
		protected synchronized SortedMap<String, String> initialValue()
		{
			return new TreeMap<String, String>();
		}
	}

	private static class ReporterData extends ThreadLocal<LinkedList<ReporterDataPoint>>
	{
		@Override
		protected synchronized LinkedList<ReporterDataPoint> initialValue()
		{
			return (new LinkedList<ReporterDataPoint>());
		}

		public void addDataPoint(ReporterDataPoint dps)
		{
			get().addLast(dps);
		}

		public ReporterDataPoint getNextDataPoint()
		{
			return get().removeFirst();
		}

		public int getListSize()
		{
			return get().size();
		}
	}

	private static class ReporterTime extends ThreadLocal<Long>
	{
		@Override
		protected synchronized Long initialValue()
		{
			return (0L);
		}
	}

	private static ReporterData s_reporterData = new ReporterData();
	private static CurrentTags s_currentTags = new CurrentTags();
	private static ReporterTime s_reportTime = new ReporterTime();

	private ThreadReporter()
	{
	}

	public static void setReportTime(long time)
	{
		s_reportTime.set(time);
	}

	public static long getReportTime()
	{
		return s_reportTime.get();
	}

	public static void addTag(String name, String value)
	{
		s_currentTags.get().put(name, value);
	}

	public static void removeTag(String name)
	{
		s_currentTags.get().remove(name);
	}

	public static void clearTags()
	{
		s_currentTags.get().clear();
	}

	public static ReporterDataPoint addDataPoint(String metric, long value)
	{
		return addDataPoint(metric, value, DEFAULT_REPORTER_TTL);
	}

	public static ReporterDataPoint addDataPoint(String metric, long value, int ttl)
	{
		ReporterDataPoint rdp = new ReporterDataPoint(metric, s_currentTags.get(),
				value, ttl);
		s_reporterData.addDataPoint(rdp);

		return rdp;
	}

	public static ReporterDataPoint addDataPoint(String metric, String value)
	{
		return addDataPoint(metric, value, DEFAULT_REPORTER_TTL);
	}

	public static ReporterDataPoint addDataPoint(String metric, String value, int ttl)
	{
		ReporterDataPoint rdp = new ReporterDataPoint(metric, s_currentTags.get(),
				value, ttl);
		s_reporterData.addDataPoint(rdp);

		return rdp;
	}

	public static void submitData(LongDataPointFactory longDataPointFactory,
			StringDataPointFactory stringDataPointFactory,
			Publisher<DataPointEvent> publisher) throws DatastoreException
	{
		while (s_reporterData.getListSize() != 0)
		{
			ReporterDataPoint dp = s_reporterData.getNextDataPoint();
			DataPointEvent dataPointEvent;

			if (dp.isStringValue())
			{
				dataPointEvent = new DataPointEvent(dp.getMetricName(), dp.getTags(),
						stringDataPointFactory.createDataPoint(s_reportTime.get(), dp.getStrValue()),
						dp.getTtl());
			}
			else
			{
				dataPointEvent = new DataPointEvent(dp.getMetricName(), dp.getTags(),
						longDataPointFactory.createDataPoint(s_reportTime.get(), dp.getValue()),
						dp.getTtl());
			}
			publisher.post(dataPointEvent);
		}
	}


	public static void gatherData(StatsMap statsMap)
	{
		while (s_reporterData.getListSize() != 0)
		{
			ReporterDataPoint dp = s_reporterData.getNextDataPoint();

			if (dp.isStringValue())
			{
				continue;
			}
			else
			{
				statsMap.addMetric(dp.getMetricName(), dp.getValue());
			}
		}
	}

	/**
	 Used in finally block to clear out unsent data in case an exception occurred.
	 */
	public static void clear()
	{
		while (s_reporterData.getListSize() != 0)
			s_reporterData.getNextDataPoint();
	}

}
