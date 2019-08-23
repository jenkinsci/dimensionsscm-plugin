package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.DimensionsConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CallbackInstance {

    private static DimensionsAPICallback dimensionsAPICallback;

    private CallbackInstance() {
    }

    public static DimensionsAPICallback getInstance(DimensionsConnection connection, String baseline, String requests) {

        boolean needCallback12 = !Values.isNullOrEmpty(baseline) || !Values.isNullOrEmpty(requests);

        if (dimensionsAPICallback != null && dimensionsAPICallback.isCallback14() && needCallback12)
            dimensionsAPICallback = new DimensionsAPICallback12();

        if (dimensionsAPICallback != null)
            return dimensionsAPICallback;

        if (supportsChangeSets(connection) && !needCallback12)
            dimensionsAPICallback = new DimensionsAPICallback14();
        else
            dimensionsAPICallback = new DimensionsAPICallback12();

        return dimensionsAPICallback;
    }

    private static boolean supportsChangeSets(DimensionsConnection connection) {
        String serverVersionStr = (String) connection.getObjectFactory().getServerVersion(0).get(0);
        double serverVersion = Double.parseDouble(normalizeServerVersion(serverVersionStr));
        return (serverVersion < 1900.0 && serverVersion >= 14.0) || serverVersion >= 2019.0;
    }

    /*We need only number part of server version*/
    private static String normalizeServerVersion(String version) {
        Pattern regPattern = Pattern.compile("([\\d]+)(.*)");
        Matcher matcher = regPattern.matcher(version);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "0.0";
    }

}
