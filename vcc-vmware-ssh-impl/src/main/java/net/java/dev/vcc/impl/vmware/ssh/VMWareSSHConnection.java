package net.java.dev.vcc.impl.vmware.ssh;

import com.trilead.ssh2.Connection;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class VMWareSSHConnection {
    Connection sshConnection;

    public VMWareSSHConnection(String host, String username, char[] password) throws IOException, InterruptedException {
        sshConnection = new Connection(host);

        sshConnection.connect();

        sshConnection.authenticateWithPassword(username, new String(password ));

    }

    public String getAllVMs() throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sshConnection.exec("vim-cmd \"vmsvc/getallvms\" ", baos);

        return  new String(baos.toByteArray());
    }

    public synchronized String getPowerState(String vmid) throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sshConnection.exec("vim-cmd \"vmsvc/power.getstate\" "+vmid, baos);

        return  new String(baos.toByteArray());
    }

    public synchronized String powerFunction(String vmid, String function) throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sshConnection.exec("vim-cmd \"vmsvc/power." + function + "\" "+vmid, baos);

        return  new String(baos.toByteArray());
    }
    public void close() {
        sshConnection.close();
    }

    public void test() throws IOException, InterruptedException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        sshConnection.exec("true", baos);
    }
}
