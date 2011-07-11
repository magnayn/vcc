package net.java.dev.vcc.impl.vmware.ssh;

import net.java.dev.vcc.api.Command;
import net.java.dev.vcc.api.Computer;
import net.java.dev.vcc.spi.AbstractHost;
import net.java.dev.vcc.util.CompletedFuture;

import java.util.Collections;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: magnayn
 * Date: 08/07/2011
 * Time: 17:06
 * To change this template use File | Settings | File Templates.
 */
public class VMWareSSHHost extends AbstractHost {

    private VMWareSSHDatacenter dc;

    public VMWareSSHHost(VMWareSSHDatacenter dc, VMWareSSHHostId id)
    {
        super(id);
        this.dc = dc;
    }
    public Set<Class<? extends Command>> getCommands() {
        return Collections.emptySet(); // TODO get commands
    }

    public <T extends Command> T execute(T command) {
        command.setSubmitted(new CompletedFuture("Unsupported command", new UnsupportedOperationException()));
        return command;
    }

    public String getName() {
        return "Host";  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Set<Computer> getComputers() {
        return dc.getComputers();
    }
}