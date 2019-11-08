package hudson.plugins.dimensionsscm;

class Credentials {

    private static final String USER_DEFINED = "userDefined";
    private static final String GLOBAL_DEFINED = "globalDefined";
    private static final String PLUGIN_DEFINED = "pluginDefined";
    private static final String KEYSTORE_DEFINED = "keystoreDefined";


    static boolean isUserDefined(String credentialsType) {
        return USER_DEFINED.equalsIgnoreCase(credentialsType);
    }

    static boolean isGlobalDefined(String credentialsType) {
        return GLOBAL_DEFINED.equalsIgnoreCase(credentialsType);
    }

    static boolean isPluginDefined(String credentialsType) {
        return PLUGIN_DEFINED.equalsIgnoreCase(credentialsType);
    }

    static boolean isKeystoreDefined(String credentialsType) {
        return KEYSTORE_DEFINED.equalsIgnoreCase(credentialsType);
    }

}
