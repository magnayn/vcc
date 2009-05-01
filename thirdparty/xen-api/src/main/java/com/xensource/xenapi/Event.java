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
 * Asynchronous event registration and handling
 *
 * @author Citrix Systems, Inc.
 */
public class Event extends XenAPIObject {

    /**
     * The XenAPI reference to this object.
     */
    protected final String ref;

    /**
     * For internal use only.
     */
    Event(String ref) {
       this.ref = ref;
    }

    public String toWireString() {
       return this.ref;
    }

    /**
     * If obj is a Event, compares XenAPI references for equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Event)
        {
            Event other = (Event) obj;
            return other.ref.equals(this.ref);
        } else
        {
            return false;
        }
    }

    /**
     * Represents all the fields in a Event
     */
    public static class Record implements Types.Record {
        public String toString() {
            StringWriter writer = new StringWriter();
            PrintWriter print = new PrintWriter(writer);
            print.printf("%1$20s: %2$s\n", "id", this.id);
            print.printf("%1$20s: %2$s\n", "timestamp", this.timestamp);
            print.printf("%1$20s: %2$s\n", "clazz", this.clazz);
            print.printf("%1$20s: %2$s\n", "operation", this.operation);
            print.printf("%1$20s: %2$s\n", "ref", this.ref);
            print.printf("%1$20s: %2$s\n", "objUuid", this.objUuid);
            print.printf("%1$20s: %2$s\n", "snapshot", this.snapshot);
            return writer.toString();
        }

        /**
         * Convert a event.Record to a Map
         */
        public Map<String,Object> toMap() {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("id", this.id == null ? 0 : this.id);
            map.put("timestamp", this.timestamp == null ? new Date(0) : this.timestamp);
            map.put("class", this.clazz == null ? "" : this.clazz);
            map.put("operation", this.operation == null ? Types.EventOperation.UNRECOGNIZED : this.operation);
            map.put("ref", this.ref == null ? "" : this.ref);
            map.put("obj_uuid", this.objUuid == null ? "" : this.objUuid);
            map.put("snapshot", this.snapshot);
            return map;
        }

        /**
         * An ID, monotonically increasing, and local to the current session
         */
        public Long id;
        /**
         * The time at which the event occurred
         */
        public Date timestamp;
        /**
         * The name of the class of the object that changed
         */
        public String clazz;
        /**
         * The operation that was performed
         */
        public Types.EventOperation operation;
        /**
         * A reference to the object that changed
         */
        public String ref;
        /**
         * The uuid of the object that changed
         */
        public String objUuid;
        /**
         * The record of the database object that was added, changed or deleted
         * (the actual type will be VM.Record, VBD.Record or similar)
         */
        public Object snapshot;
    }

    /**
     * Registers this session with the event system.  Specifying the empty list will register for all classes.
     *
     * @param classes register for events for the indicated classes
     * @return Task
     */
    public static Task registerAsync(Connection c, Set<String> classes) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "Async.event.register";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(classes)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
        return Types.toTask(result);
    }

    /**
     * Registers this session with the event system.  Specifying the empty list will register for all classes.
     *
     * @param classes register for events for the indicated classes
     */
    public static void register(Connection c, Set<String> classes) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "event.register";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(classes)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Unregisters this session with the event system
     *
     * @param classes remove this session's registration for the indicated classes
     * @return Task
     */
    public static Task unregisterAsync(Connection c, Set<String> classes) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "Async.event.unregister";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(classes)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
        return Types.toTask(result);
    }

    /**
     * Unregisters this session with the event system
     *
     * @param classes remove this session's registration for the indicated classes
     */
    public static void unregister(Connection c, Set<String> classes) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "event.unregister";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(classes)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Blocking call which returns a (possibly empty) batch of events
     *
     * @return the batch of events
     */
    public static Set<Event.Record> next(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException,
       Types.SessionNotRegistered,
       Types.EventsLost {
        String method_call = "event.next";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfEventRecord(result);
    }

    /**
     * Return the ID of the next event to be generated by the system
     *
     * @return the event ID
     */
    public static Long getCurrentId(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "event.get_current_id";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toLong(result);
    }

}