package net.java.dev.vcc.impl.vmware.esx;

import com.vmware.vim25.ManagedObjectReference;
import net.java.dev.vcc.api.DatacenterResourceGroup;

/**
 * Created by IntelliJ IDEA. User: connollys Date: Jul 31, 2009 Time: 10:53:49 AM To change this template use File |
 * Settings | File Templates.
 */
class ViDatacenterResourceGroupId
        extends ViManagedObjectId<DatacenterResourceGroup> {

    /**
     * Constructs a new {@link net.java.dev.vcc.api.ManagedObjectId}.
     *
     * @param datacenterId The ID of the {@link net.java.dev.vcc.api.Datacenter} hosting the instance referenced by this
     *                     ID.
     * @param mo           The VMware ESX Managed Object Reference for this {@link net.java.dev.vcc.api.DatacenterResourceGroup}.
     */
    ViDatacenterResourceGroupId(ViDatacenterId datacenterId, ManagedObjectReference mo) {
        super(DatacenterResourceGroup.class, datacenterId, mo);
    }

}