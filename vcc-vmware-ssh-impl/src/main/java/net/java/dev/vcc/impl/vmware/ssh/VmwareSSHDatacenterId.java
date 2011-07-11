package net.java.dev.vcc.impl.vmware.ssh;

import net.java.dev.vcc.api.Datacenter;
import net.java.dev.vcc.api.ManagedObject;
import net.java.dev.vcc.api.ManagedObjectId;

/**
 * Created by IntelliJ IDEA.
 * User: magnayn
 * Date: 08/07/2011
 * Time: 15:02
 * To change this template use File | Settings | File Templates.
 */
public class VMWareSSHDatacenterId extends ManagedObjectId<Datacenter> {
    /**
     * Constructs a new {@link net.java.dev.vcc.api.ManagedObjectId}.
     *
     * @param datacenterUrl The URL of the {@link net.java.dev.vcc.api.Datacenter} hosting the instance referenced by
     *                      this ID.
     */
    VMWareSSHDatacenterId(String datacenterUrl) {
        super(Datacenter.class, datacenterUrl);
    }
}
