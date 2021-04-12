package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.DimensionsObjectFactory;
import java.util.Collections;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CallbackInstanceTest {

    private static final String NEW_SERVER_VERSION = "14.5";
    private static final String BASELINE = "baseline_name_ex";
    private static final String REQUEST = "QLARIUS_CR_21";

    @Test
    public void getInstanceTest() {
        final DimensionsConnection dimensionsConnection = mock(DimensionsConnection.class);
        final DimensionsObjectFactory dimensionsObjectFactory = mock(DimensionsObjectFactory.class);
        when(dimensionsConnection.getObjectFactory()).thenReturn(dimensionsObjectFactory);
        when(dimensionsConnection.getObjectFactory().getServerVersion(0)).thenReturn(Collections.singletonList(NEW_SERVER_VERSION));
        final DimensionsAPICallback apiCallback = CallbackInstance.getInstance(dimensionsConnection, BASELINE, REQUEST);
        assertThat(apiCallback, instanceOf(DimensionsAPICallback12.class));
    }
}
