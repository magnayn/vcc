package net.java.dev.vcc.impl.vmware.ssh;

import net.java.dev.vcc.api.LogFactory;
import net.java.dev.vcc.spi.AbstractDatacenter;
import net.java.dev.vcc.spi.DatacenterConnection;

import java.io.IOException;


public class VMWareSSHDatacenterConnection implements DatacenterConnection {
    public boolean acceptsUrl(String url) {
        return url.startsWith("vcc+vmware+ssh://");
    }

     public AbstractDatacenter connect(String url, String username, char[] password, LogFactory logFactory)
            throws IOException {
        assert url.startsWith("vcc+vmware+ssh");
        logFactory.getClass();
        try {
            //ExecutorService executorService = Executors.newCachedThreadPool(new ViThreadFactory());
            VMWareSSHConnection connection = new VMWareSSHConnection(url.substring("vcc+vmware+ssh://".length()), username, password);
            return new VMWareSSHDatacenter(connection, new VMWareSSHDatacenterId(url), logFactory);
        } catch (Exception e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
}
