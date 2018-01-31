package hudson.plugins.dimensionsscm;

import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Callable to find out if the current host is remote to the master.
 */
public class GetHostDetailsTask extends BaseCallable {
    private final String masteripaddr;

    public GetHostDetailsTask(String master) {
        this.masteripaddr = master;
    }

    @Override
    public Boolean invoke(File area, VirtualChannel channel) throws IOException {
        // This here code is executed on the slave.
        try {
            InetAddress netAddr = InetAddress.getLocalHost();
            // Get hostname and compare.
            return netAddr.getHostName().equals(masteripaddr);
        } catch (UnknownHostException e) {
            throw (IOException) new IOException(Values.exceptionMessage("Exception getting hostname", e,
                    "no message")).initCause(e);
        }
    }
}
