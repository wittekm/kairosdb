package org.kairosdb.core.properties;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kairosdb.core.exception.KairosDBException;
import org.kairosdb.core.reporting.MetricReporterService;
import org.kairosdb.datastore.cassandra.CassandraConfiguration;

import java.util.Optional;


public class PropertiesValidatorTest {
    private static final int SOME_POSITIVE_INT = 1234;
    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void test_WhenNothingSpecified_ValidatorPasses() throws KairosDBException {
        ValidatorArgs args = new ValidatorArgs();

        validatorWithInjection(args).validate();
    }

    @Test
    public void test_WhenOnlyAlignIsOn_ValidatorFails() throws KairosDBException {
        exceptionRule.expect(KairosDBException.class);
        exceptionRule.expectMessage("to a non-zero value");

        ValidatorArgs args = new ValidatorArgs();
        args.alignDatapointTtlWithTimestamp = Optional.of(true);

        validatorWithInjection(args).validate();
    }

    @Test
    public void test_invalidDatapointTtl_ValidatorFails() throws KairosDBException {
        exceptionRule.expect(KairosDBException.class);
        exceptionRule.expectMessage("to a non-zero value");

        ValidatorArgs args = new ValidatorArgs();
        args.alignDatapointTtlWithTimestamp = Optional.of(true);
        args.forceDefaultDatapointTtl = Optional.of(true);

        validatorWithInjection(args).validate();
    }

    @Test
    public void test_WhenAlignOff_ValidatorSucceeds() throws KairosDBException {
        ValidatorArgs args = new ValidatorArgs();
        args.reporterTtl = Optional.of(SOME_POSITIVE_INT);
        args.cassandraDatapointTtl = Optional.of(SOME_POSITIVE_INT);

        validatorWithInjection(args).validate();
    }

    @Test
    public void test_WhenAlignIsOn_AndBothTtlsSpecified_ValidatorSucceeds() throws KairosDBException {
        ValidatorArgs args = new ValidatorArgs();
        args.alignDatapointTtlWithTimestamp = Optional.of(true);
        args.reporterTtl = Optional.of(SOME_POSITIVE_INT);
        args.cassandraDatapointTtl = Optional.of(SOME_POSITIVE_INT);

        validatorWithInjection(args).validate();
    }

    private PropertiesValidator validatorWithInjection(final ValidatorArgs args) {
        final Injector injector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                args.alignDatapointTtlWithTimestamp.ifPresent(val -> bind(Boolean.TYPE)
                        .annotatedWith(Names.named(CassandraConfiguration.ALIGN_DATAPOINT_TTL_WITH_TIMESTAMP))
                        .toInstance(val)
                );
                args.forceDefaultDatapointTtl.ifPresent(val -> bind(Boolean.TYPE)
                        .annotatedWith(Names.named(CassandraConfiguration.FORCE_DEFAULT_DATAPOINT_TTL))
                        .toInstance(val)
                );
                args.reporterTtl.ifPresent(val -> bind(Integer.TYPE)
                        .annotatedWith(Names.named(MetricReporterService.REPORTER_TTL))
                        .toInstance(val)
                );
                args.cassandraDatapointTtl.ifPresent(val -> bind(Integer.TYPE)
                        .annotatedWith(Names.named(CassandraConfiguration.DATAPOINT_TTL))
                        .toInstance(val)
                );
            }
        });
        return injector.getInstance(PropertiesValidator.class);
    }

    private static class ValidatorArgs {
        // a struct! in java? sacrilege
        Optional<Boolean> alignDatapointTtlWithTimestamp = Optional.empty();
        Optional<Boolean> forceDefaultDatapointTtl = Optional.empty();
        Optional<Integer> reporterTtl = Optional.empty();
        Optional<Integer> cassandraDatapointTtl = Optional.empty();
    }
}
