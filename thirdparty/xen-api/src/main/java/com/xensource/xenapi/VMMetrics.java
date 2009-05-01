/*
 * Copyright (c) 2006-2008 Citrix Systems, Inc.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation, with the additional linking exception as
 * follows:
 * 
 *   Linking this library statically or dynamically with other modules is
 *   making a combined work based on this library. Thus, the terms and
 *   conditions of the GNU General Public License cover the whole combination.
 * 
 *   As a special exception, the copyright holders of this library give you
 *   permission to link this library with independent modules to produce an
 *   executable, regardless of the license terms of these independent modules,
 *   and to copy and distribute the resulting executable under terms of your
 *   choice, provided that you also meet, for each linked independent module,
 *   the terms and conditions of the license of that module. An independent
 *   module is a module which is not derived from or based on this library. If
 *   you modify this library, you may extend this exception to your version of
 *   the library, but you are not obligated to do so. If you do not wish to do
 *   so, delete this exception statement from your version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.xensource.xenapi;

import com.xensource.xenapi.Types.BadServerResponse;
import com.xensource.xenapi.Types.VersionException;
import com.xensource.xenapi.Types.XenAPIException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.xmlrpc.XmlRpcException;

/**
 * The metrics associated with a VM
 *
 * @author Citrix Systems, Inc.
 */
public class VMMetrics extends XenAPIObject {

    /**
     * The XenAPI reference to this object.
     */
    protected final String ref;

    /**
     * For internal use only.
     */
    VMMetrics(String ref) {
       this.ref = ref;
    }

    public String toWireString() {
       return this.ref;
    }

    /**
     * If obj is a VMMetrics, compares XenAPI references for equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof VMMetrics)
        {
            VMMetrics other = (VMMetrics) obj;
            return other.ref.equals(this.ref);
        } else
        {
            return false;
        }
    }

    /**
     * Represents all the fields in a VMMetrics
     */
    public static class Record implements Types.Record {
        public String toString() {
            StringWriter writer = new StringWriter();
            PrintWriter print = new PrintWriter(writer);
            print.printf("%1$20s: %2$s\n", "uuid", this.uuid);
            print.printf("%1$20s: %2$s\n", "memoryActual", this.memoryActual);
            print.printf("%1$20s: %2$s\n", "VCPUsNumber", this.VCPUsNumber);
            print.printf("%1$20s: %2$s\n", "VCPUsUtilisation", this.VCPUsUtilisation);
            print.printf("%1$20s: %2$s\n", "VCPUsCPU", this.VCPUsCPU);
            print.printf("%1$20s: %2$s\n", "VCPUsParams", this.VCPUsParams);
            print.printf("%1$20s: %2$s\n", "VCPUsFlags", this.VCPUsFlags);
            print.printf("%1$20s: %2$s\n", "state", this.state);
            print.printf("%1$20s: %2$s\n", "startTime", this.startTime);
            print.printf("%1$20s: %2$s\n", "installTime", this.installTime);
            print.printf("%1$20s: %2$s\n", "lastUpdated", this.lastUpdated);
            print.printf("%1$20s: %2$s\n", "otherConfig", this.otherConfig);
            return writer.toString();
        }

        /**
         * Convert a VM_metrics.Record to a Map
         */
        public Map<String,Object> toMap() {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("uuid", this.uuid == null ? "" : this.uuid);
            map.put("memory_actual", this.memoryActual == null ? 0 : this.memoryActual);
            map.put("VCPUs_number", this.VCPUsNumber == null ? 0 : this.VCPUsNumber);
            map.put("VCPUs_utilisation", this.VCPUsUtilisation == null ? new HashMap<Long, Double>() : this.VCPUsUtilisation);
            map.put("VCPUs_CPU", this.VCPUsCPU == null ? new HashMap<Long, Long>() : this.VCPUsCPU);
            map.put("VCPUs_params", this.VCPUsParams == null ? new HashMap<String, String>() : this.VCPUsParams);
            map.put("VCPUs_flags", this.VCPUsFlags == null ? new HashMap<Long, Set<String>>() : this.VCPUsFlags);
            map.put("state", this.state == null ? new LinkedHashSet<String>() : this.state);
            map.put("start_time", this.startTime == null ? new Date(0) : this.startTime);
            map.put("install_time", this.installTime == null ? new Date(0) : this.installTime);
            map.put("last_updated", this.lastUpdated == null ? new Date(0) : this.lastUpdated);
            map.put("other_config", this.otherConfig == null ? new HashMap<String, String>() : this.otherConfig);
            return map;
        }

        /**
         * unique identifier/object reference
         */
        public String uuid;
        /**
         * Guest's actual memory (bytes)
         */
        public Long memoryActual;
        /**
         * Current number of VCPUs
         */
        public Long VCPUsNumber;
        /**
         * Utilisation for all of guest's current VCPUs
         */
        public Map<Long, Double> VCPUsUtilisation;
        /**
         * VCPU to PCPU map
         */
        public Map<Long, Long> VCPUsCPU;
        /**
         * The live equivalent to VM.VCPUs_params
         */
        public Map<String, String> VCPUsParams;
        /**
         * CPU flags (blocked,online,running)
         */
        public Map<Long, Set<String>> VCPUsFlags;
        /**
         * The state of the guest, eg blocked, dying etc
         */
        public Set<String> state;
        /**
         * Time at which this VM was last booted
         */
        public Date startTime;
        /**
         * Time at which the VM was installed
         */
        public Date installTime;
        /**
         * Time at which this information was last updated
         */
        public Date lastUpdated;
        /**
         * additional configuration
         */
        public Map<String, String> otherConfig;
    }

    /**
     * Get a record containing the current state of the given VM_metrics.
     *
     * @return all fields from the object
     */
    public VMMetrics.Record getRecord(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_record";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toVMMetricsRecord(result);
    }

    /**
     * Get a reference to the VM_metrics instance with the specified UUID.
     *
     * @param uuid UUID of object to return
     * @return reference to the object
     */
    public static VMMetrics getByUuid(Connection c, String uuid) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_by_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(uuid)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toVMMetrics(result);
    }

    /**
     * Get the uuid field of the given VM_metrics.
     *
     * @return value of the field
     */
    public String getUuid(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the memory/actual field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Long getMemoryActual(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_memory_actual";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toLong(result);
    }

    /**
     * Get the VCPUs/number field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Long getVCPUsNumber(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_VCPUs_number";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toLong(result);
    }

    /**
     * Get the VCPUs/utilisation field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Map<Long, Double> getVCPUsUtilisation(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_VCPUs_utilisation";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfLongDouble(result);
    }

    /**
     * Get the VCPUs/CPU field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Map<Long, Long> getVCPUsCPU(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_VCPUs_CPU";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfLongLong(result);
    }

    /**
     * Get the VCPUs/params field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Map<String, String> getVCPUsParams(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_VCPUs_params";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringString(result);
    }

    /**
     * Get the VCPUs/flags field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Map<Long, Set<String>> getVCPUsFlags(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_VCPUs_flags";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfLongSetOfString(result);
    }

    /**
     * Get the state field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Set<String> getState(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_state";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfString(result);
    }

    /**
     * Get the start_time field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Date getStartTime(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_start_time";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDate(result);
    }

    /**
     * Get the install_time field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Date getInstallTime(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_install_time";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDate(result);
    }

    /**
     * Get the last_updated field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Date getLastUpdated(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_last_updated";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDate(result);
    }

    /**
     * Get the other_config field of the given VM_metrics.
     *
     * @return value of the field
     */
    public Map<String, String> getOtherConfig(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringString(result);
    }

    /**
     * Set the other_config field of the given VM_metrics.
     *
     * @param otherConfig New value to set
     */
    public void setOtherConfig(Connection c, Map<String, String> otherConfig) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.set_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(otherConfig)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Add the given key-value pair to the other_config field of the given VM_metrics.
     *
     * @param key Key to add
     * @param value Value to add
     */
    public void addToOtherConfig(Connection c, String key, String value) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.add_to_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key), Marshalling.toXMLRPC(value)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Remove the given key and its corresponding value from the other_config field of the given VM_metrics.  If the key is not in that Map, then do nothing.
     *
     * @param key Key to remove
     */
    public void removeFromOtherConfig(Connection c, String key) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.remove_from_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Return a list of all the VM_metrics instances known to the system.
     *
     * @return references to all objects
     */
    public static Set<VMMetrics> getAll(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_all";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfVMMetrics(result);
    }

    /**
     * Return a map of VM_metrics references to VM_metrics records for all VM_metrics instances known to the system.
     *
     * @return records of all objects
     */
    public static Map<VMMetrics, VMMetrics.Record> getAllRecords(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "VM_metrics.get_all_records";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfVMMetricsVMMetricsRecord(result);
    }

}