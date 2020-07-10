package org.kairosdb.core.reporting;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import org.junit.Test;
import org.kairosdb.core.http.rest.ResourceBase;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;


public class ThreadReporterTest extends ResourceBase {
    @Test
    public void testInjection() throws Exception
    {
        Injector injector = Guice.createInjector(new ServletModule() {
            @Override
            protected void configureServlets() {
                bind(Integer.TYPE).annotatedWith(Names.named("kairosdb.reporter.ttl")).toInstance(12345);
                requestStaticInjection(ThreadReporter.class);
            }
        });
        injector.getAllBindings();
        System.out.println(injector.getAllBindings().toString());

        // when
        final ThreadReporter.ReporterDataPoint dataPoint = ThreadReporter.addDataPoint("metric name", "value");

        // then
        assertThat(dataPoint.getTtl(), greaterThan(0));

    }
}
