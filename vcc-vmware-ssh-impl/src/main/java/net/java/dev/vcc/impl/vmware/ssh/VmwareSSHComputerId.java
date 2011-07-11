package net.java.dev.vcc.impl.vmware.ssh;

import net.java.dev.vcc.api.Computer;
import net.java.dev.vcc.api.ManagedObjectId;

/**
 * Created by IntelliJ IDEA.
 * User: magnayn
 * Date: 08/07/2011
 * Time: 15:00
 * To change this template use File | Settings | File Templates.
 */
public class VMWareSSHComputerId extends ManagedObjectId<Computer> {

    public String computerId;
    /**
     * Constructs a new {@link net.java.dev.vcc.api.ManagedObjectId}.
     *
     * @param datacenterId The ID of the {@link net.java.dev.vcc.api.Datacenter} hosting the instance referenced by this
     *                     ID.
     * @param computerId           The VMware ESX Managed Object Reference for this {@link Computer}.
     */
    VMWareSSHComputerId(VMWareSSHDatacenterId datacenterId, String computerId) {
        super(Computer.class, datacenterId.getDatacenterUrl());
        this.computerId = computerId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        VMWareSSHComputerId that = (VMWareSSHComputerId) o;

        if (!computerId.equals(that.computerId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + computerId.hashCode();
        return result;
    }
}
