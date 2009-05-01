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
 * The metrics associated with a physical network interface
 *
 * @author Citrix Systems, Inc.
 */
public class PIFMetrics extends XenAPIObject {

    /**
     * The XenAPI reference to this object.
     */
    protected final String ref;

    /**
     * For internal use only.
     */
    PIFMetrics(String ref) {
       this.ref = ref;
    }

    public String toWireString() {
       return this.ref;
    }

    /**
     * If obj is a PIFMetrics, compares XenAPI references for equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof PIFMetrics)
        {
            PIFMetrics other = (PIFMetrics) obj;
            return other.ref.equals(this.ref);
        } else
        {
            return false;
        }
    }

    /**
     * Represents all the fields in a PIFMetrics
     */
    public static class Record implements Types.Record {
        public String toString() {
            StringWriter writer = new StringWriter();
            PrintWriter print = new PrintWriter(writer);
            print.printf("%1$20s: %2$s\n", "uuid", this.uuid);
            print.printf("%1$20s: %2$s\n", "ioReadKbs", this.ioReadKbs);
            print.printf("%1$20s: %2$s\n", "ioWriteKbs", this.ioWriteKbs);
            print.printf("%1$20s: %2$s\n", "carrier", this.carrier);
            print.printf("%1$20s: %2$s\n", "vendorId", this.vendorId);
            print.printf("%1$20s: %2$s\n", "vendorName", this.vendorName);
            print.printf("%1$20s: %2$s\n", "deviceId", this.deviceId);
            print.printf("%1$20s: %2$s\n", "deviceName", this.deviceName);
            print.printf("%1$20s: %2$s\n", "speed", this.speed);
            print.printf("%1$20s: %2$s\n", "duplex", this.duplex);
            print.printf("%1$20s: %2$s\n", "pciBusPath", this.pciBusPath);
            print.printf("%1$20s: %2$s\n", "lastUpdated", this.lastUpdated);
            print.printf("%1$20s: %2$s\n", "otherConfig", this.otherConfig);
            return writer.toString();
        }

        /**
         * Convert a PIF_metrics.Record to a Map
         */
        public Map<String,Object> toMap() {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("uuid", this.uuid == null ? "" : this.uuid);
            map.put("io_read_kbs", this.ioReadKbs == null ? 0.0 : this.ioReadKbs);
            map.put("io_write_kbs", this.ioWriteKbs == null ? 0.0 : this.ioWriteKbs);
            map.put("carrier", this.carrier == null ? false : this.carrier);
            map.put("vendor_id", this.vendorId == null ? "" : this.vendorId);
            map.put("vendor_name", this.vendorName == null ? "" : this.vendorName);
            map.put("device_id", this.deviceId == null ? "" : this.deviceId);
            map.put("device_name", this.deviceName == null ? "" : this.deviceName);
            map.put("speed", this.speed == null ? 0 : this.speed);
            map.put("duplex", this.duplex == null ? false : this.duplex);
            map.put("pci_bus_path", this.pciBusPath == null ? "" : this.pciBusPath);
            map.put("last_updated", this.lastUpdated == null ? new Date(0) : this.lastUpdated);
            map.put("other_config", this.otherConfig == null ? new HashMap<String, String>() : this.otherConfig);
            return map;
        }

        /**
         * unique identifier/object reference
         */
        public String uuid;
        /**
         * Read bandwidth (KiB/s)
         */
        public Double ioReadKbs;
        /**
         * Write bandwidth (KiB/s)
         */
        public Double ioWriteKbs;
        /**
         * Report if the PIF got a carrier or not
         */
        public Boolean carrier;
        /**
         * Report vendor ID
         */
        public String vendorId;
        /**
         * Report vendor name
         */
        public String vendorName;
        /**
         * Report device ID
         */
        public String deviceId;
        /**
         * Report device name
         */
        public String deviceName;
        /**
         * Speed of the link (if available)
         */
        public Long speed;
        /**
         * Full duplex capability of the link (if available)
         */
        public Boolean duplex;
        /**
         * PCI bus path of the pif (if available)
         */
        public String pciBusPath;
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
     * Get a record containing the current state of the given PIF_metrics.
     *
     * @return all fields from the object
     */
    public PIFMetrics.Record getRecord(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_record";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toPIFMetricsRecord(result);
    }

    /**
     * Get a reference to the PIF_metrics instance with the specified UUID.
     *
     * @param uuid UUID of object to return
     * @return reference to the object
     */
    public static PIFMetrics getByUuid(Connection c, String uuid) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_by_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(uuid)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toPIFMetrics(result);
    }

    /**
     * Get the uuid field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public String getUuid(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the io/read_kbs field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public Double getIoReadKbs(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_io_read_kbs";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDouble(result);
    }

    /**
     * Get the io/write_kbs field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public Double getIoWriteKbs(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_io_write_kbs";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDouble(result);
    }

    /**
     * Get the carrier field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public Boolean getCarrier(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_carrier";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toBoolean(result);
    }

    /**
     * Get the vendor_id field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public String getVendorId(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_vendor_id";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the vendor_name field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public String getVendorName(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_vendor_name";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the device_id field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public String getDeviceId(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_device_id";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the device_name field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public String getDeviceName(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_device_name";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the speed field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public Long getSpeed(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_speed";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toLong(result);
    }

    /**
     * Get the duplex field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public Boolean getDuplex(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_duplex";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toBoolean(result);
    }

    /**
     * Get the pci_bus_path field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public String getPciBusPath(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_pci_bus_path";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the last_updated field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public Date getLastUpdated(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_last_updated";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDate(result);
    }

    /**
     * Get the other_config field of the given PIF_metrics.
     *
     * @return value of the field
     */
    public Map<String, String> getOtherConfig(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringString(result);
    }

    /**
     * Set the other_config field of the given PIF_metrics.
     *
     * @param otherConfig New value to set
     */
    public void setOtherConfig(Connection c, Map<String, String> otherConfig) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.set_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(otherConfig)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Add the given key-value pair to the other_config field of the given PIF_metrics.
     *
     * @param key Key to add
     * @param value Value to add
     */
    public void addToOtherConfig(Connection c, String key, String value) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.add_to_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key), Marshalling.toXMLRPC(value)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Remove the given key and its corresponding value from the other_config field of the given PIF_metrics.  If the key is not in that Map, then do nothing.
     *
     * @param key Key to remove
     */
    public void removeFromOtherConfig(Connection c, String key) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.remove_from_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Return a list of all the PIF_metrics instances known to the system.
     *
     * @return references to all objects
     */
    public static Set<PIFMetrics> getAll(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_all";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfPIFMetrics(result);
    }

    /**
     * Return a map of PIF_metrics references to PIF_metrics records for all PIF_metrics instances known to the system.
     *
     * @return records of all objects
     */
    public static Map<PIFMetrics, PIFMetrics.Record> getAllRecords(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "PIF_metrics.get_all_records";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfPIFMetricsPIFMetricsRecord(result);
    }

}