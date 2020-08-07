package org.kairosdb.core.properties;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.kairosdb.core.exception.KairosDBException;
import org.kairosdb.core.reporting.MetricReporterService;
import org.kairosdb.datastore.cassandra.CassandraConfiguration;

/**
 * @author wittekm
 * Ensure that nothing in the kairosdb.properties file would result in
 * unexpected behavior. Throw a KairosDBException upon startup if so.
 */
public class PropertiesValidator {
    // Field DI is not ideal, but you can't provide defaults with constructor DI.
    @Inject(optional = true)
    @Named(CassandraConfiguration.ALIGN_DATAPOINT_TTL_WITH_TIMESTAMP)
    private boolean m_alignDatapointTtlWithTimestamp = CassandraConfiguration.ALIGN_DATAPOINT_TTL_WITH_TIMESTAMP_DEFAULT;

    @Inject(optional = true)
    @Named(CassandraConfiguration.FORCE_DEFAULT_DATAPOINT_TTL)
    private boolean m_forceDefaultDatapointTttl = CassandraConfiguration.FORCE_DEFAULT_DATAPOINT_TTL_DEFAULT;

    @Inject(optional = true)
    @Named(MetricReporterService.REPORTER_TTL)
    private int m_reporterTtl = 0;

    @Inject(optional = true)
    @Named(CassandraConfiguration.DATAPOINT_TTL)
    private int m_cassandraDatapointTtl = 0;

    public void validate() throws KairosDBException {
        validateSelfReportingTtlsWorkWithAlignment();
    }

    private void validateSelfReportingTtlsWorkWithAlignment() throws KairosDBException {
        /**
         * If you flip on align_datapoint_ttl, you must choose a non-zero TTL for Kairos's self-reporting
         * metrics; otherwise, it'll complain about:
         * "aligned ttl for {} with tags {} is negative"
         * from BatchHandler
         */
        if (m_alignDatapointTtlWithTimestamp) {
            if (m_reporterTtl == 0) {
                throw new KairosDBException(String.format(
                        "Since %s is turned on, you must set %s to a non-zero value.",
                        CassandraConfiguration.ALIGN_DATAPOINT_TTL_WITH_TIMESTAMP,
                        MetricReporterService.REPORTER_TTL
                ));
            }
            if (m_cassandraDatapointTtl == 0 && m_forceDefaultDatapointTttl) {
                throw new KairosDBException(String.format(
                        "Since %s and %s are turned on, you must set %s to a non-zero value.",
                        CassandraConfiguration.ALIGN_DATAPOINT_TTL_WITH_TIMESTAMP,
                        CassandraConfiguration.FORCE_DEFAULT_DATAPOINT_TTL,
                        CassandraConfiguration.DATAPOINT_TTL
                ));
            }
        }
    }
}
