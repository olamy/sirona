package org.apache.commons.monitoring.monitors;

import java.util.Arrays;
import java.util.Collection;

import org.apache.commons.monitoring.Counter;
import org.apache.commons.monitoring.Gauge;
import org.apache.commons.monitoring.Metric;
import org.apache.commons.monitoring.Monitor;
import org.apache.commons.monitoring.Role;
import org.apache.commons.monitoring.Unit;
import org.apache.commons.monitoring.Visitor;
import org.apache.commons.monitoring.metrics.NoOpCounter;
import org.apache.commons.monitoring.metrics.NoOpGauge;

/**
 * Monitor implementation that does nothing, and return NoOp metrics when requested. Designed for test purpose or to
 * disable monitoring to compare monitored to unmonitored performances.
 *
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
@SuppressWarnings( "unchecked" )
public class NullMonitor
    implements Monitor
{
    private static final Unit NULL = new Unit( "null" )
    {
        @Override
        public boolean isCompatible( Unit unit )
        {
            return true;
        }
    };

    private static final Role NOP_COUNTER = new Role( "NopCounter", NULL, Metric.Type.COUNTER );

    private static final Role NOP_GAUGE = new Role( "NopGauge", NULL, Metric.Type.GAUGE );

    private static final NoOpCounter counter = new NoOpCounter( NOP_COUNTER );

    private static final NoOpGauge gauge = new NoOpGauge( NOP_GAUGE );

    private Collection<Metric> metrics = Arrays.asList( new Metric[] { counter, gauge } );

    public Counter getCounter( String role )
    {
        return counter;
    }

    public Counter getCounter( Role role )
    {
        return counter;
    }

    public Gauge getGauge( String role )
    {
        return gauge;
    }

    public Gauge getGauge( Role role )
    {
        return gauge;
    }

    public Key getKey()
    {
        return new Key( "noOp", null, null );
    }

    public Metric getMetric( String role )
    {
        return counter;
    }

    public Metric getMetric( Role role )
    {
        return counter;
    }

    public Collection<Metric> getMetrics()
    {
        return metrics;
    }

    public Collection<Role> getRoles()
    {
        return Arrays.asList( new Role[] { NOP_COUNTER, NOP_GAUGE } );
    }

    public void reset()
    {
        // NoOp
    }

    private static class NullUnit extends Unit
    {
        public NullUnit()
        {
            super( "null" );
        }

        @Override
        public boolean isCompatible( Unit unit )
        {
            return true;
        }
    }

    public void accept( Visitor visitor )
    {
        // NoOp
    }
}