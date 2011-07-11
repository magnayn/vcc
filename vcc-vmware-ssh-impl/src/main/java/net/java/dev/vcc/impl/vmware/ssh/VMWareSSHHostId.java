package net.java.dev.vcc.impl.vmware.ssh;

import net.java.dev.vcc.api.Host;
import net.java.dev.vcc.api.ManagedObjectId;

/**
 * Created by IntelliJ IDEA.
 * User: magnayn
 * Date: 08/07/2011
 * Time: 17:04
 * To change this template use File | Settings | File Templates.
 */
public class VMWareSSHHostId extends ManagedObjectId<Host> {
    /**
     * Constructs a new {@link net.java.dev.vcc.api.ManagedObjectId}.
     *
     * @param datacenterId The ID of the {@link net.java.dev.vcc.api.Datacenter} hosting the instance referenced by this
     *                     ID.
     */

    VMWareSSHHostId(VMWareSSHDatacenterId datacenterId) {
        super(Host.class, datacenterId.getDatacenterUrl());
    }
}
