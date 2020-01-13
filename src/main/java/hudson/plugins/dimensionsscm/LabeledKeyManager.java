package hudson.plugins.dimensionsscm;

import javax.net.ssl.X509KeyManager;
import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

class LabeledKeyManager implements X509KeyManager {

    private final X509KeyManager parentKeyManager;
    private final String alias;

    public LabeledKeyManager(X509KeyManager parentKeyManager, String alias) {
        this.parentKeyManager = parentKeyManager;
        this.alias = alias;
    }

    @Override
    public String chooseClientAlias(String[] keyType, Principal[] issuers, Socket socket) {
        return alias;
    }

    @Override
    public String chooseServerAlias(String keyType, Principal[] issuers, Socket socket) {
        return parentKeyManager.chooseServerAlias(keyType, issuers, socket);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) {
        return parentKeyManager.getCertificateChain(alias);
    }

    @Override
    public String[] getClientAliases(String keyType, Principal[] issuers) {
        return parentKeyManager.getClientAliases(keyType, issuers);
    }

    @Override
    public PrivateKey getPrivateKey(String alias) {
        return parentKeyManager.getPrivateKey(alias);
    }

    @Override
    public String[] getServerAliases(String keyType, Principal[] issuers) {
        return parentKeyManager.getServerAliases(keyType, issuers);
    }

}
