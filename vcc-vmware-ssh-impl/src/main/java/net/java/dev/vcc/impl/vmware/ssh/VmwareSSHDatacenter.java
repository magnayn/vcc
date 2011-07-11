package net.java.dev.vcc.impl.vmware.ssh;

import net.java.dev.vcc.api.*;
import net.java.dev.vcc.api.profiles.BasicProfile;
import net.java.dev.vcc.spi.AbstractDatacenter;
import net.java.dev.vcc.util.CompletedFuture;

import java.io.IOException;
import java.util.*;


public class VMWareSSHDatacenter extends AbstractDatacenter {
    VMWareSSHConnection connection;

    public VMWareSSHDatacenter(VMWareSSHConnection connection, VMWareSSHDatacenterId id, LogFactory logFactory)
    {
        super(logFactory, id, BasicProfile.getInstance());
        this.connection = connection;

    }

    public VMWareSSHConnection getConnection() {
        return connection;
    }

    public Set<PowerState> getAllowedStates() {
        return ResourceHolder.ALLOWED_TRANSITIONS.keySet();
    }

    public Set<PowerState> getAllowedStates(PowerState from) {
        Set<PowerState> states = ResourceHolder.ALLOWED_TRANSITIONS.get(from);
        if (states != null) {
            return states;
        }
        return Collections.emptySet();
    }

    public void close() {
        connection.close();
    }

    public boolean isOpen() {
        try
        {
            connection.test();
            return true;
        }
        catch(Exception ex)
        {
            return false;
        }
    }

    public Set<Class<? extends Command>> getCommands() {
        return Collections.emptySet(); // TODO get commands
    }

    public <T extends Command> T execute(T command) {
        command.setSubmitted(new CompletedFuture("Unsupported command", new UnsupportedOperationException()));
        return command;
    }

    public String getName() {
        return getId().getDatacenterUrl();
    }

     private static final class ResourceHolder {
        private static final Map<PowerState, Set<PowerState>> ALLOWED_TRANSITIONS;

        static {
            TreeMap<PowerState, Set<PowerState>> map = new TreeMap<PowerState, Set<PowerState>>();
            map.put(PowerState.STOPPED,
                    Collections.unmodifiableSet(new TreeSet<PowerState>(Arrays.asList(PowerState.RUNNING))));
            map.put(PowerState.SUSPENDED, Collections.unmodifiableSet(
                    new TreeSet<PowerState>(Arrays.asList(PowerState.STOPPED, PowerState.RUNNING))));
            map.put(PowerState.RUNNING, Collections.unmodifiableSet(
                    new TreeSet<PowerState>(Arrays.asList(PowerState.STOPPED, PowerState.SUSPENDED))));
            ALLOWED_TRANSITIONS = Collections.unmodifiableMap(map);
        }
    }

    public Set<Host> getHosts() {
        Set<Host> hosts = new HashSet<Host>();
        // Temp - 1 host per datacenter.
        hosts.add( new VMWareSSHHost(this, new VMWareSSHHostId((VMWareSSHDatacenterId) getId())));
        return hosts;
    }

    public Set<Computer> getComputers() {
      Set<Computer> computers= new HashSet<Computer>();
       try
       {

           String data = connection.getAllVMs();
           String[] lines = data.split("\n");

           for(int i=1; i<lines.length;i++)
           {
               String line = lines[i];
               String[] items = line.split("\\s+");

               String vmid = items[0];
               String name = items[1];

               VMWareSSHComputer computer = new VMWareSSHComputer(
                       new VMWareSSHComputerId((VMWareSSHDatacenterId) this.getId(), vmid ),this, name
               );

               computers.add(computer);
           }


       } catch (InterruptedException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       } catch (IOException e) {
           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
       }
       return computers;
    }
}
