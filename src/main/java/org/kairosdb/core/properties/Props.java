package org.kairosdb.core.properties;

public class Props {
    public static final String ALIGN_DATAPOINT_TTL_WITH_TIMESTAMP =
            "kairosdb.datastore.cassandra.align_datapoint_ttl_with_timestamp";
    public static final String REPORTER_TTL = "kairosdb.reporter.ttl";
    public static final String CASSANDRA_DATAPOINT_TTL = "kairosdb.datastore.cassandra.datapoint_ttl";

    private Props() {}
}
