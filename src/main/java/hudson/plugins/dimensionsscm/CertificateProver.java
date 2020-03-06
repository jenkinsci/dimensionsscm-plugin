package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.AuthCertificateProver;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;

public class CertificateProver implements AuthCertificateProver {

    private final PrivateKey privateKey;
    private final Provider provider;

    public CertificateProver(final PrivateKey privateKey, final Provider provider) {
        this.privateKey = privateKey;
        this.provider = provider;
    }

    @Override
    public SignResult sign(final byte[] data) throws Exception {
        Signature signature = Signature.getInstance("SHA1withRSA", provider);
        signature.initSign(privateKey);
        signature.update(data);
        return new SignResultImpl(AUTH_SIGNATURE_METHOD_SHA1, signature.sign());
    }
}
