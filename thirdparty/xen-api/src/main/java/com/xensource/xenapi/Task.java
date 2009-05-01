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
 * A long-running asynchronous task
 *
 * @author Citrix Systems, Inc.
 */
public class Task extends XenAPIObject {

    /**
     * The XenAPI reference to this object.
     */
    protected final String ref;

    /**
     * For internal use only.
     */
    Task(String ref) {
       this.ref = ref;
    }

    public String toWireString() {
       return this.ref;
    }

    /**
     * If obj is a Task, compares XenAPI references for equality.
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj != null && obj instanceof Task)
        {
            Task other = (Task) obj;
            return other.ref.equals(this.ref);
        } else
        {
            return false;
        }
    }

    /**
     * Represents all the fields in a Task
     */
    public static class Record implements Types.Record {
        public String toString() {
            StringWriter writer = new StringWriter();
            PrintWriter print = new PrintWriter(writer);
            print.printf("%1$20s: %2$s\n", "uuid", this.uuid);
            print.printf("%1$20s: %2$s\n", "nameLabel", this.nameLabel);
            print.printf("%1$20s: %2$s\n", "nameDescription", this.nameDescription);
            print.printf("%1$20s: %2$s\n", "allowedOperations", this.allowedOperations);
            print.printf("%1$20s: %2$s\n", "currentOperations", this.currentOperations);
            print.printf("%1$20s: %2$s\n", "created", this.created);
            print.printf("%1$20s: %2$s\n", "finished", this.finished);
            print.printf("%1$20s: %2$s\n", "status", this.status);
            print.printf("%1$20s: %2$s\n", "residentOn", this.residentOn);
            print.printf("%1$20s: %2$s\n", "progress", this.progress);
            print.printf("%1$20s: %2$s\n", "type", this.type);
            print.printf("%1$20s: %2$s\n", "result", this.result);
            print.printf("%1$20s: %2$s\n", "errorInfo", this.errorInfo);
            print.printf("%1$20s: %2$s\n", "otherConfig", this.otherConfig);
            print.printf("%1$20s: %2$s\n", "subtaskOf", this.subtaskOf);
            print.printf("%1$20s: %2$s\n", "subtasks", this.subtasks);
            return writer.toString();
        }

        /**
         * Convert a task.Record to a Map
         */
        public Map<String,Object> toMap() {
            Map<String,Object> map = new HashMap<String,Object>();
            map.put("uuid", this.uuid == null ? "" : this.uuid);
            map.put("name_label", this.nameLabel == null ? "" : this.nameLabel);
            map.put("name_description", this.nameDescription == null ? "" : this.nameDescription);
            map.put("allowed_operations", this.allowedOperations == null ? new LinkedHashSet<Types.TaskAllowedOperations>() : this.allowedOperations);
            map.put("current_operations", this.currentOperations == null ? new HashMap<String, Types.TaskAllowedOperations>() : this.currentOperations);
            map.put("created", this.created == null ? new Date(0) : this.created);
            map.put("finished", this.finished == null ? new Date(0) : this.finished);
            map.put("status", this.status == null ? Types.TaskStatusType.UNRECOGNIZED : this.status);
            map.put("resident_on", this.residentOn == null ? new Host("OpaqueRef:NULL") : this.residentOn);
            map.put("progress", this.progress == null ? 0.0 : this.progress);
            map.put("type", this.type == null ? "" : this.type);
            map.put("result", this.result == null ? "" : this.result);
            map.put("error_info", this.errorInfo == null ? new LinkedHashSet<String>() : this.errorInfo);
            map.put("other_config", this.otherConfig == null ? new HashMap<String, String>() : this.otherConfig);
            map.put("subtask_of", this.subtaskOf == null ? new Task("OpaqueRef:NULL") : this.subtaskOf);
            map.put("subtasks", this.subtasks == null ? new LinkedHashSet<Task>() : this.subtasks);
            return map;
        }

        /**
         * unique identifier/object reference
         */
        public String uuid;
        /**
         * a human-readable name
         */
        public String nameLabel;
        /**
         * a notes field containg human-readable description
         */
        public String nameDescription;
        /**
         * list of the operations allowed in this state. This list is advisory only and the server state may have changed by the time this field is read by a client.
         */
        public Set<Types.TaskAllowedOperations> allowedOperations;
        /**
         * links each of the running tasks using this object (by reference) to a current_operation enum which describes the nature of the task.
         */
        public Map<String, Types.TaskAllowedOperations> currentOperations;
        /**
         * Time task was created
         */
        public Date created;
        /**
         * Time task finished (i.e. succeeded or failed). If task-status is pending, then the value of this field has no meaning
         */
        public Date finished;
        /**
         * current status of the task
         */
        public Types.TaskStatusType status;
        /**
         * the host on which the task is running
         */
        public Host residentOn;
        /**
         * if the task is still pending, this field contains the estimated fraction complete (0.-1.). If task has completed (successfully or unsuccessfully) this should be 1.
         */
        public Double progress;
        /**
         * if the task has completed successfully, this field contains the type of the encoded result (i.e. name of the class whose reference is in the result field). Undefined otherwise.
         */
        public String type;
        /**
         * if the task has completed successfully, this field contains the result value (either Void or an object reference). Undefined otherwise.
         */
        public String result;
        /**
         * if the task has failed, this field contains the set of associated error strings. Undefined otherwise.
         */
        public Set<String> errorInfo;
        /**
         * additional configuration
         */
        public Map<String, String> otherConfig;
        /**
         * Ref pointing to the task this is a substask of.
         */
        public Task subtaskOf;
        /**
         * List pointing to all the substasks.
         */
        public Set<Task> subtasks;
    }

    /**
     * Get a record containing the current state of the given task.
     *
     * @return all fields from the object
     */
    public Task.Record getRecord(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_record";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTaskRecord(result);
    }

    /**
     * Get a reference to the task instance with the specified UUID.
     *
     * @param uuid UUID of object to return
     * @return reference to the object
     */
    public static Task getByUuid(Connection c, String uuid) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_by_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(uuid)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTask(result);
    }

    /**
     * Get all the task instances with the given label.
     *
     * @param label label of object to return
     * @return references to objects with matching names
     */
    public static Set<Task> getByNameLabel(Connection c, String label) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_by_name_label";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(label)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfTask(result);
    }

    /**
     * Get the uuid field of the given task.
     *
     * @return value of the field
     */
    public String getUuid(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_uuid";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the name/label field of the given task.
     *
     * @return value of the field
     */
    public String getNameLabel(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_name_label";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the name/description field of the given task.
     *
     * @return value of the field
     */
    public String getNameDescription(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_name_description";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the allowed_operations field of the given task.
     *
     * @return value of the field
     */
    public Set<Types.TaskAllowedOperations> getAllowedOperations(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_allowed_operations";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfTaskAllowedOperations(result);
    }

    /**
     * Get the current_operations field of the given task.
     *
     * @return value of the field
     */
    public Map<String, Types.TaskAllowedOperations> getCurrentOperations(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_current_operations";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringTaskAllowedOperations(result);
    }

    /**
     * Get the created field of the given task.
     *
     * @return value of the field
     */
    public Date getCreated(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_created";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDate(result);
    }

    /**
     * Get the finished field of the given task.
     *
     * @return value of the field
     */
    public Date getFinished(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_finished";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDate(result);
    }

    /**
     * Get the status field of the given task.
     *
     * @return value of the field
     */
    public Types.TaskStatusType getStatus(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_status";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTaskStatusType(result);
    }

    /**
     * Get the resident_on field of the given task.
     *
     * @return value of the field
     */
    public Host getResidentOn(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_resident_on";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toHost(result);
    }

    /**
     * Get the progress field of the given task.
     *
     * @return value of the field
     */
    public Double getProgress(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_progress";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toDouble(result);
    }

    /**
     * Get the type field of the given task.
     *
     * @return value of the field
     */
    public String getType(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_type";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the result field of the given task.
     *
     * @return value of the field
     */
    public String getResult(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_result";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toString(result);
    }

    /**
     * Get the error_info field of the given task.
     *
     * @return value of the field
     */
    public Set<String> getErrorInfo(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_error_info";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfString(result);
    }

    /**
     * Get the other_config field of the given task.
     *
     * @return value of the field
     */
    public Map<String, String> getOtherConfig(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfStringString(result);
    }

    /**
     * Get the subtask_of field of the given task.
     *
     * @return value of the field
     */
    public Task getSubtaskOf(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_subtask_of";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTask(result);
    }

    /**
     * Get the subtasks field of the given task.
     *
     * @return value of the field
     */
    public Set<Task> getSubtasks(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_subtasks";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfTask(result);
    }

    /**
     * Set the other_config field of the given task.
     *
     * @param otherConfig New value to set
     */
    public void setOtherConfig(Connection c, Map<String, String> otherConfig) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.set_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(otherConfig)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Add the given key-value pair to the other_config field of the given task.
     *
     * @param key Key to add
     * @param value Value to add
     */
    public void addToOtherConfig(Connection c, String key, String value) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.add_to_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key), Marshalling.toXMLRPC(value)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Remove the given key and its corresponding value from the other_config field of the given task.  If the key is not in that Map, then do nothing.
     *
     * @param key Key to remove
     */
    public void removeFromOtherConfig(Connection c, String key) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.remove_from_other_config";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref), Marshalling.toXMLRPC(key)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Create a new task object which must be manually destroyed.
     *
     * @param label short label for the new task
     * @param description longer description for the new task
     * @return The reference of the created task object
     */
    public static Task create(Connection c, String label, String description) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.create";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(label), Marshalling.toXMLRPC(description)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toTask(result);
    }

    /**
     * Destroy the task object
     *
     */
    public void destroy(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.destroy";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Request that a task be cancelled. Note that a task may fail to be cancelled and may complete or fail normally and note that, even when a task does cancel, it might take an arbitrary amount of time.
     *
     * @return Task
     */
    public Task cancelAsync(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException,
       Types.OperationNotAllowed {
        String method_call = "Async.task.cancel";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
        return Types.toTask(result);
    }

    /**
     * Request that a task be cancelled. Note that a task may fail to be cancelled and may complete or fail normally and note that, even when a task does cancel, it might take an arbitrary amount of time.
     *
     */
    public void cancel(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException,
       Types.OperationNotAllowed {
        String method_call = "task.cancel";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session), Marshalling.toXMLRPC(this.ref)};
        Map response = c.dispatch(method_call, method_params);
        return;
    }

    /**
     * Return a list of all the tasks known to the system.
     *
     * @return references to all objects
     */
    public static Set<Task> getAll(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_all";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toSetOfTask(result);
    }

    /**
     * Return a map of task references to task records for all tasks known to the system.
     *
     * @return records of all objects
     */
    public static Map<Task, Task.Record> getAllRecords(Connection c) throws
       BadServerResponse,
       XenAPIException,
       XmlRpcException {
        String method_call = "task.get_all_records";
        String session = c.getSessionReference();
        Object[] method_params = {Marshalling.toXMLRPC(session)};
        Map response = c.dispatch(method_call, method_params);
        Object result = response.get("Value");
            return Types.toMapOfTaskTaskRecord(result);
    }

}