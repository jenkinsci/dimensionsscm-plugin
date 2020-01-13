package hudson.plugins.dimensionsscm;

import com.serena.dmclient.api.AuthCertificateProver;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.Signature;

public class CertificateProver implements AuthCertificateProver {

    private final PrivateKey privateKey;
    private final Provider provider;
    private final String signatureAlgorithm;
    private static final String SIGN_ALGORITHM_NONE_WITH_RSA = "NONEwithRSA";

    public CertificateProver(final PrivateKey privateKey, final Provider provider, final String signatureAlgorithm) {
        this.privateKey = privateKey;
        this.provider = provider;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    @Override
    public SignResult sign(final byte[] data) throws Exception {
        Signature signature = Signature.getInstance(signatureAlgorithm, provider);
        signature.initSign(privateKey);
        signature.update(data);
        return new SignResultImpl(signatureAlgorithm.equals(SIGN_ALGORITHM_NONE_WITH_RSA)
                ? AUTH_SIGNATURE_METHOD_NONE : AUTH_SIGNATURE_METHOD_SHA1, signature.sign());
    }
}
