package net.java.dev.vcc.impl.vmware.ssh;

import net.java.dev.vcc.api.*;
import net.java.dev.vcc.api.commands.RestartComputer;
import net.java.dev.vcc.api.commands.StartComputer;
import net.java.dev.vcc.api.commands.StopComputer;
import net.java.dev.vcc.api.commands.SuspendComputer;
import net.java.dev.vcc.spi.AbstractComputer;
import net.java.dev.vcc.util.CompletedFuture;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * Created by IntelliJ IDEA.
 * User: magnayn
 * Date: 08/07/2011
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class VMWareSSHComputer extends AbstractComputer {

    private VMWareSSHDatacenter datacenter;
    private String name;

    public VMWareSSHComputer(VMWareSSHComputerId id, VMWareSSHDatacenter datacenter, String name)
    {
        super(id);
        this.datacenter = datacenter;
        this.name = name;
    }

    @Override
    public VMWareSSHComputerId getId()
    {
        return (VMWareSSHComputerId)super.getId();
    }

    public Set<Class<? extends Command>> getCommands() {
        List<Class<? extends Command>> classes = new ArrayList<Class<? extends Command>>();
        switch (getState()) {
            case STOPPED:
                classes.add(StartComputer.class);
                break;
            case SUSPENDED:
                classes.add(StartComputer.class);
                classes.add(StopComputer.class);
                break;
            case RUNNING:
                classes.add(SuspendComputer.class);
                classes.add(RestartComputer.class);
                classes.add(StopComputer.class);
                break;
        }
        return Collections.unmodifiableSet(new HashSet<Class<? extends Command>>(classes));
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

        final Callable<PowerState> psc = new Callable<PowerState>() {

                    public PowerState call() throws Exception {
                        String data = getConnection().getPowerState (getId().computerId);

                        String[] lines = data.split("\n");
                        String[] stat  = lines[1].split(" ");

                        if( stat[0].equals("Suspended") )
                            return PowerState.SUSPENDED;

                        if( stat.length > 1 )
                            {
                            if( stat[1].equals("on") )
                                return PowerState.RUNNING;
                            if( stat[1].equals("off") )
                                return PowerState.STOPPED;
                        }

                        return PowerState.PAUSED;

                    }};

        FutureTask<PowerState> call = new FutureTask<PowerState>( psc );

        return call;
    }

    public Set<ComputerSnapshot> getSnapshots() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Set<Host> getAllowedHosts() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }



    public <T extends Command> T execute(T command) {
        try {
            if (command instanceof StartComputer) {
                FutureTask<Success> call = new FutureTask<Success>( new Callable<Success>() {

                    public Success call() throws Exception {
                        getConnection().powerFunction(getId().computerId, "on" );
                        return Success.getInstance();
                    }
                } );

                call.run();

                command.setSubmitted(call);

            } else if (command instanceof StopComputer) {
                    FutureTask<Success> call = new FutureTask<Success>( new Callable<Success>() {

                    public Success call() throws Exception {
                        getConnection().powerFunction(getId().computerId, "off" );
                        return Success.getInstance();
                    }
                } );

                call.run();

                command.setSubmitted(call);
            } else if (command instanceof SuspendComputer) {
                    FutureTask<Success> call = new FutureTask<Success>( new Callable<Success>() {

                    public Success call() throws Exception {
                        getConnection().powerFunction(getId().computerId, "suspend" );
                        return Success.getInstance();
                    }
                } );

                call.run();

                command.setSubmitted(call);
            } else {
                command.setSubmitted(new CompletedFuture("Unsupported command", new UnsupportedOperationException()));
            }
        } catch (Throwable e) {
            command.setSubmitted(new CompletedFuture(e.getMessage(), e));
        }
        return command;
    }

    public String getName() {
        return name;  //To change body of implemented methods use File | Settings | File Templates.
    }

    protected VMWareSSHConnection getConnection() {
        return datacenter.getConnection();
    }
}
