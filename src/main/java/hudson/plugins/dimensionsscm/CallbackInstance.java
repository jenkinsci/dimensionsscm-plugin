package hudson.plugins.dimensionsscm;


import com.serena.dmclient.api.DimensionsConnection;

public class CallbackInstance {

    private static DimensionsAPICallback dimensionsAPICallback;

    private CallbackInstance() {}

    public static DimensionsAPICallback getInstance(DimensionsConnection connection) {

        if (dimensionsAPICallback != null)
            return dimensionsAPICallback;

        if (isNewServerVersion(connection))
            dimensionsAPICallback = new DimensionsAPICallback14();
        else
            dimensionsAPICallback = new DimensionsAPICallback12();

        return dimensionsAPICallback;
    }

    private static boolean isNewServerVersion(DimensionsConnection connection) {
        String serverVersionStr = (String) connection.getObjectFactory().getServerVersion(0).get(0);
        Double serverVersion = Double.parseDouble(serverVersionStr != null && !serverVersionStr.isEmpty() ? serverVersionStr : "0.0");
        return serverVersion.compareTo(14.0) >= 0;
    }

}
