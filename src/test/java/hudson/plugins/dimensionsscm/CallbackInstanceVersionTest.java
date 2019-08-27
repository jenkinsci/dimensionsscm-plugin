package hudson.plugins.dimensionsscm;


import com.serena.dmclient.api.DimensionsConnection;
import com.serena.dmclient.api.DimensionsObjectFactory;
import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class CallbackInstanceVersionTest extends TestCase {

    private String serverVersion;
    private Class callbackClass;

    public CallbackInstanceVersionTest(String serverVersion, Class callbackClass) {
        this.callbackClass = callbackClass;
        this.serverVersion = serverVersion;
    }

    @Parameterized.Parameters
    public static Collection serverVersions() {
        return Arrays.asList(new Object[][]{
                {
                        "14.3.3", DimensionsAPICallback14.class
                },
                {
                        "12.2.2.5", DimensionsAPICallback12.class
                },
                {
                        "2009R", DimensionsAPICallback12.class
                }
        });
    }


    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field instance = CallbackInstance.class.getDeclaredField("dimensionsAPICallback");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    @Test
    public void getInstanceTest() {
        DimensionsConnection dimensionsConnection = mock(DimensionsConnection.class);
        DimensionsObjectFactory dimensionsObjectFactory = mock(DimensionsObjectFactory.class);

        when(dimensionsConnection.getObjectFactory()).thenReturn(dimensionsObjectFactory);
        when(dimensionsConnection.getObjectFactory().getServerVersion(0)).thenReturn(Collections.singletonList(this.serverVersion));

        DimensionsAPICallback apiCallback = CallbackInstance.getInstance(dimensionsConnection, null, null);
        assertThat(apiCallback, instanceOf(this.callbackClass));

        DimensionsAPICallback apiCallbackSameObject = CallbackInstance.getInstance(dimensionsConnection, null, null);
        assertEquals(apiCallback, apiCallbackSameObject);
    }

}
