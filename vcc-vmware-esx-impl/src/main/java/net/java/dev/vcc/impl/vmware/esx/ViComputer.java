package net.java.dev.vcc.impl.vmware.esx;

import net.java.dev.vcc.api.Command;
import net.java.dev.vcc.api.Computer;
import net.java.dev.vcc.api.ComputerSnapshot;
import net.java.dev.vcc.api.Host;
import net.java.dev.vcc.api.ManagedObjectId;
import net.java.dev.vcc.api.PowerState;
import net.java.dev.vcc.spi.AbstractComputer;
import net.java.dev.vcc.util.CompletedFuture;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Future;

public class ViComputer extends AbstractComputer {

    protected ViComputer(ManagedObjectId<Computer> id) {
        super(id);
    }

    public Set<Class<? extends Command>> getCommands() {
        return Collections.emptySet(); // TODO get commands
    }

    public <T extends Command> T execute(T command) {
        command.setSubmitted(new CompletedFuture("Unsupported command", new UnsupportedOperationException()));
        return command;
    }

    public Host getHost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isHostChanging() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Host> getFutureHost() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public PowerState getState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public boolean isStateChanging() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<PowerState> getFutureState() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<ComputerSnapshot> getSnapshots() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<Host> getAllowedHosts() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getName() {
        return "";
    }

    public String getDescription() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}