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
 * A session
 *
 * @author Citrix Systems, Inc.
 */
public class Session extends XenAPIObject {

    /**
     * The XenAPI reference to this object.
     */
    protected final String ref;

    /**
     * For internal use only.
     */
    Session(String ref) {
       this.ref = ref;
    }

    public String toWireString() {
       return this.ref;
    }

    /**
     * If obj is a Session, compares XenAPI references for equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Session)
        {
            Session other = (Session) obj;
            return other.ref.equals(this.ref);
        } else
        {
            return false;
        }
    }

    /**
     * Represents all the fields in a Session
     */
    public static class Record implements Types.Record {
        public String toString() {
            StringWriter writer = new StringWriter();
            PrintWriter print = new PrintWriter(writer);
            print.printf("%1$20s: %2$s\n", "uuid", this.uuid);
            print.printf("%1$20s: %2$s\n", "thisHost", this.thisHost);
            print.printf("%1$20s: %2$s\n", "thisUser", this.thisUser);
            print.printf("%1$20s: %2$s\n", "lastActive", this.lastActive);
            print.printf("%1$20s: %2$s\n", "pool", this.pool);
            print.printf("%1$20s: %2$s\n", "otherConfig", this.otherConfig);
            return writer.toString();
        }

        /**
         * Convert a session.Record to a Map
         */
        public Map<String,Object> toMap() {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("uuid", this.uuid == null ? "" : this.uuid);
            map.put("this_host", this.thisHost == null ? new Host("OpaqueRef:NULL") : this.thisHost);
            map.put("this_user", this.thisUser == null ? new User("OpaqueRef:NULL") : this.thisUser);
            map.put("last_active", this.lastActive == null ? new Date(0) : this.lastActive);
            map.put("pool", this.pool == null ? false : this.pool);
            map.put("other_config", this.otherConfig == null ? new HashMap<String, String>() : this.otherConfig);
            return map;
        }

        /**
         * unique identifier/object reference
         */
        public String uuid;
        /**
         * Currently connected host
         */
        public Host thisHost;
        /**
         * Currently connected user
         */
        public User thisUser;
        /**
         * Timestamp for last time session was active
         */
        public Date lastActive;
        /**
         * True if this session relates to a intra-pool login, false otherwise
         */
        public Boolean pool;
        /**
         * additional configuration
         */
        public Map<String, String> otherConfig;
    }

    /**
     * Get a record containing the current state of the given session.
     *
     * @return all fields from the object
     */
    public Session.Record getRecord(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_record";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSessionRecord(result);
    }

    /**
     * Get a reference to the session instance with the specified UUID.
     *
     * @param uuid UUID of object to return
     * @return reference to the object
     */
    public static Session getByUuid(Connection c, String uuid) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_by_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(uuid)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSession(result);
    }

    /**
     * Get the uuid field of the given session.
     *
     * @return value of the field
     */
    public String getUuid(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the this_host field of the given session.
     *
     * @return value of the field
     */
    public Host getThisHost(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_this_host";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toHost(result);
    }

    /**
     * Get the this_user field of the given session.
     *
     * @return value of the field
     */
    public User getThisUser(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_this_user";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toUser(result);
    }

    /**
     * Get the last_active field of the given session.
     *
     * @return value of the field
     */
    public Date getLastActive(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_last_active";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDate(result);
    }

    /**
     * Get the pool field of the given session.
     *
     * @return value of the field
     */
    public Boolean getPool(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_pool";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toBoolean(result);
    }

    /**
     * Get the other_config field of the given session.
     *
     * @return value of the field
     */
    public Map<String, String> getOtherConfig(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.get_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringString(result);
    }

    /**
     * Set the other_config field of the given session.
     *
     * @param otherConfig New value to set
     */
    public void setOtherConfig(Connection c, Map<String, String> otherConfig) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.set_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(otherConfig)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Add the given key-value pair to the other_config field of the given session.
     *
     * @param key Key to add
     * @param value Value to add
     */
    public void addToOtherConfig(Connection c, String key, String value) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.add_to_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key), Marshalling.toXMLRPC(value)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Remove the given key and its corresponding value from the other_config field of the given session.  If the key is not in that Map, then do nothing.
     *
     * @param key Key to remove
     */
    public void removeFromOtherConfig(Connection c, String key) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.remove_from_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Attempt to authenticate the user, returning a session reference if successful
     *
     * @param uname Username for login.
     * @param pwd Password for login.
     * @param version Client API version.
     * @return reference of newly created session
     */
    public static Session loginWithPassword(Connection c, String uname, String pwd, String version) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException,
       Types.SessionAuthenticationFailed {
        String method_call = "session.login_with_password";
        Object[] method_params = {Marshalling.toXMLRPC(uname), Marshalling.toXMLRPC(pwd), Marshalling.toXMLRPC(version)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSession(result);
    }

    /**
     * Log out of a session
     *
     */
    public static void logout(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.logout";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Change the account password; if your session is authenticated with root priviledges then the old_pwd is validated and the new_pwd is set regardless
     *
     * @param oldPwd Old password for account
     * @param newPwd New password for account
     */
    public static void changePassword(Connection c, String oldPwd, String newPwd) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.change_password";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(oldPwd), Marshalling.toXMLRPC(newPwd)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Authenticate locally against a slave in emergency mode. Note the resulting sessions are only good for use on this host.
     *
     * @param uname Username for login.
     * @param pwd Password for login.
     * @return ID of newly created session
     */
    public static Session slaveLocalLoginWithPassword(Connection c, String uname, String pwd) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.slave_local_login_with_password";
        Object[] method_params = {Marshalling.toXMLRPC(uname), Marshalling.toXMLRPC(pwd)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSession(result);
    }

    /**
     * Log out of local session.
     *
     */
    public static void localLogout(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "session.local_logout";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

}