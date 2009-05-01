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

import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.LinkedHashSet;
import java.io.IOException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.xmlrpc.XmlRpcException;

/**
 * This class holds vital marshalling functions, enum types and exceptions.
 * 
 * @author Citrix Systems, Inc.
 */
public class Types
{
    /**
     * Interface for all Record classes
     */
    public static interface Record
    {
        /**
         * Convert a Record to a Map
         */
        Map<String, Object> toMap();
    }

    /**
     * Helper method.
     */
    private static String[] ObjectArrayToStringArray(Object[] objArray)
    {
        String[] result = new String[objArray.length];
        for (int i = 0; i < objArray.length; i++)
        {
            result[i] = (String) objArray[i];
        }
        return result;
    }

    /**
     * Base class for all XenAPI Exceptions
     */
    public static class XenAPIException extends IOException {
        public final String shortDescription;
        public final String[] errorDescription;

        XenAPIException(String shortDescription)
        {
            this.shortDescription = shortDescription;
            this.errorDescription = null;
        }

        XenAPIException(String[] errorDescription)
        {
            this.errorDescription = errorDescription;

            if (errorDescription.length > 0)
            {
                shortDescription = errorDescription[0];
            } else
            {
                shortDescription = "";
            }
        }

        public String toString()
        {
            if (errorDescription == null)
            {
                return shortDescription;
            } else if (errorDescription.length == 0)
            {
                return "";
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < errorDescription.length - 1; i++)
            {
                sb.append(errorDescription[i]);
            }
            sb.append(errorDescription[errorDescription.length - 1]);

            return sb.toString();
        }
    }
    /**
     * Thrown if the response from the server contains an invalid status.
     */
    public static class BadServerResponse extends XenAPIException
    {
        public BadServerResponse(Map response)
        {
            super(ObjectArrayToStringArray((Object[]) response.get("ErrorDescription")));
        }
    }

    public static class BadAsyncResult extends XenAPIException
    {
        public final String result;

        public BadAsyncResult(String result)
        {
            super(result);
            this.result = result;
        }
    }

    /*
     * A call has been made which should not be made against this version of host.
     * Probably the host is out of date and cannot handle this call, or is
     * unable to comply with the details of the call. For instance SR.create
     * on Miami (4.1) hosts takes an smConfig parameter, which must be an empty map 
     * when making this call on Rio (4.0) hosts.
     */
    public static class VersionException extends XenAPIException
    {
        public final String result;

        public VersionException(String result)
        {
            super(result);
            this.result = result;
        }
    }

    private static String parseResult(String result) throws BadAsyncResult
    {
        Pattern pattern = Pattern.compile("<value>(.*)</value>");
        Matcher matcher = pattern.matcher(result);
        matcher.find();

        if (matcher.groupCount() != 1)
        {
            throw new Types.BadAsyncResult("Can't interpret: " + result);
        }

        return matcher.group(1);
    }
      /**
     * Checks the provided server response was successful. If the call failed, throws a XenAPIException. If the server
     * returned an invalid response, throws a BadServerResponse. Otherwise, returns the server response as passed in.
     */
    static Map checkResponse(Map response) throws XenAPIException, BadServerResponse
    {
        if (response.get("Status").equals("Success"))
        {
            return response;
        }

        if (response.get("Status").equals("Failure"))
        {
            String[] ErrorDescription = ObjectArrayToStringArray((Object[]) response.get("ErrorDescription"));

            if (ErrorDescription[0].equals("RESTORE_TARGET_MISSING_DEVICE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.RestoreTargetMissingDevice(p1);
            }
            if (ErrorDescription[0].equals("MAC_DOES_NOT_EXIST"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.MacDoesNotExist(p1);
            }
            if (ErrorDescription[0].equals("HANDLE_INVALID"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.HandleInvalid(p1, p2);
            }
            if (ErrorDescription[0].equals("DEVICE_ALREADY_ATTACHED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.DeviceAlreadyAttached(p1);
            }
            if (ErrorDescription[0].equals("INVALID_IP_ADDRESS_SPECIFIED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.InvalidIpAddressSpecified(p1);
            }
            if (ErrorDescription[0].equals("SR_NOT_EMPTY"))
            {
                throw new Types.SrNotEmpty();
            }
            if (ErrorDescription[0].equals("VM_HVM_REQUIRED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmHvmRequired(p1);
            }
            if (ErrorDescription[0].equals("PIF_BOND_NEEDS_MORE_MEMBERS"))
            {
                throw new Types.PifBondNeedsMoreMembers();
            }
            if (ErrorDescription[0].equals("PIF_ALREADY_BONDED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PifAlreadyBonded(p1);
            }
            if (ErrorDescription[0].equals("VLAN_TAG_INVALID"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VlanTagInvalid(p1);
            }
            if (ErrorDescription[0].equals("HOST_IS_SLAVE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostIsSlave(p1);
            }
            if (ErrorDescription[0].equals("CANNOT_FIND_OEM_BACKUP_PARTITION"))
            {
                throw new Types.CannotFindOemBackupPartition();
            }
            if (ErrorDescription[0].equals("PIF_DEVICE_NOT_FOUND"))
            {
                throw new Types.PifDeviceNotFound();
            }
            if (ErrorDescription[0].equals("PATCH_PRECHECK_FAILED_VM_RUNNING"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PatchPrecheckFailedVmRunning(p1);
            }
            if (ErrorDescription[0].equals("HA_HOST_CANNOT_SEE_PEERS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.HaHostCannotSeePeers(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("PERMISSION_DENIED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PermissionDenied(p1);
            }
            if (ErrorDescription[0].equals("SR_ATTACH_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SrAttachFailed(p1);
            }
            if (ErrorDescription[0].equals("HA_LOST_STATEFILE"))
            {
                throw new Types.HaLostStatefile();
            }
            if (ErrorDescription[0].equals("HA_NOT_ENABLED"))
            {
                throw new Types.HaNotEnabled();
            }
            if (ErrorDescription[0].equals("HA_HEARTBEAT_DAEMON_STARTUP_FAILED"))
            {
                throw new Types.HaHeartbeatDaemonStartupFailed();
            }
            if (ErrorDescription[0].equals("SESSION_NOT_REGISTERED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SessionNotRegistered(p1);
            }
            if (ErrorDescription[0].equals("VM_NO_SUSPEND_SR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmNoSuspendSr(p1);
            }
            if (ErrorDescription[0].equals("SR_FULL"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.SrFull(p1, p2);
            }
            if (ErrorDescription[0].equals("PATCH_APPLY_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PatchApplyFailed(p1);
            }
            if (ErrorDescription[0].equals("VDI_READONLY"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VdiReadonly(p1);
            }
            if (ErrorDescription[0].equals("VDI_NOT_AVAILABLE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VdiNotAvailable(p1);
            }
            if (ErrorDescription[0].equals("XMLRPC_UNMARSHAL_FAILURE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.XmlrpcUnmarshalFailure(p1, p2);
            }
            if (ErrorDescription[0].equals("HOST_MASTER_CANNOT_TALK_BACK"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostMasterCannotTalkBack(p1);
            }
            if (ErrorDescription[0].equals("XAPI_HOOK_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                String p4 = ErrorDescription.length >= 4 ? ErrorDescription[4] : "";
                throw new Types.XapiHookFailed(p1, p2, p3, p4);
            }
            if (ErrorDescription[0].equals("UNKNOWN_BOOTLOADER"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.UnknownBootloader(p1, p2);
            }
            if (ErrorDescription[0].equals("IMPORT_INCOMPATIBLE_VERSION"))
            {
                throw new Types.ImportIncompatibleVersion();
            }
            if (ErrorDescription[0].equals("SR_VDI_LOCKING_FAILED"))
            {
                throw new Types.SrVdiLockingFailed();
            }
            if (ErrorDescription[0].equals("PIF_IS_PHYSICAL"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PifIsPhysical(p1);
            }
            if (ErrorDescription[0].equals("MAP_DUPLICATE_KEY"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                String p4 = ErrorDescription.length >= 4 ? ErrorDescription[4] : "";
                throw new Types.MapDuplicateKey(p1, p2, p3, p4);
            }
            if (ErrorDescription[0].equals("BOOTLOADER_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.BootloaderFailed(p1, p2);
            }
            if (ErrorDescription[0].equals("SYSTEM_STATUS_RETRIEVAL_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SystemStatusRetrievalFailed(p1);
            }
            if (ErrorDescription[0].equals("HOST_NOT_LIVE"))
            {
                throw new Types.HostNotLive();
            }
            if (ErrorDescription[0].equals("VDI_IN_USE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VdiInUse(p1, p2);
            }
            if (ErrorDescription[0].equals("SR_HAS_NO_PBDS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SrHasNoPbds(p1);
            }
            if (ErrorDescription[0].equals("INVALID_PATCH"))
            {
                throw new Types.InvalidPatch();
            }
            if (ErrorDescription[0].equals("HA_ABORT_NEW_MASTER"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HaAbortNewMaster(p1);
            }
            if (ErrorDescription[0].equals("POOL_JOINING_HOST_MUST_HAVE_PHYSICAL_MANAGEMENT_NIC"))
            {
                throw new Types.PoolJoiningHostMustHavePhysicalManagementNic();
            }
            if (ErrorDescription[0].equals("PATCH_PRECHECK_FAILED_UNKNOWN_ERROR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.PatchPrecheckFailedUnknownError(p1, p2);
            }
            if (ErrorDescription[0].equals("DUPLICATE_VM"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.DuplicateVm(p1);
            }
            if (ErrorDescription[0].equals("HOST_CANNOT_ATTACH_NETWORK"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.HostCannotAttachNetwork(p1, p2);
            }
            if (ErrorDescription[0].equals("HOST_CANNOT_DESTROY_SELF"))
            {
                throw new Types.HostCannotDestroySelf();
            }
            if (ErrorDescription[0].equals("HOST_BROKEN"))
            {
                throw new Types.HostBroken();
            }
            if (ErrorDescription[0].equals("VM_TOO_MANY_VCPUS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmTooManyVcpus(p1);
            }
            if (ErrorDescription[0].equals("HOST_IS_LIVE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostIsLive(p1);
            }
            if (ErrorDescription[0].equals("VBD_NOT_UNPLUGGABLE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VbdNotUnpluggable(p1);
            }
            if (ErrorDescription[0].equals("CANNOT_EVACUATE_HOST"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.CannotEvacuateHost(p1);
            }
            if (ErrorDescription[0].equals("NO_HOSTS_AVAILABLE"))
            {
                throw new Types.NoHostsAvailable();
            }
            if (ErrorDescription[0].equals("DEVICE_ATTACH_TIMEOUT"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.DeviceAttachTimeout(p1, p2);
            }
            if (ErrorDescription[0].equals("PBD_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.PbdExists(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("INVALID_DEVICE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.InvalidDevice(p1);
            }
            if (ErrorDescription[0].equals("HOST_CANNOT_READ_METRICS"))
            {
                throw new Types.HostCannotReadMetrics();
            }
            if (ErrorDescription[0].equals("LICENSE_DOES_NOT_SUPPORT_POOLING"))
            {
                throw new Types.LicenseDoesNotSupportPooling();
            }
            if (ErrorDescription[0].equals("HOST_UNKNOWN_TO_MASTER"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostUnknownToMaster(p1);
            }
            if (ErrorDescription[0].equals("VM_REQUIRES_SR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VmRequiresSr(p1, p2);
            }
            if (ErrorDescription[0].equals("VM_NO_CRASHDUMP_SR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmNoCrashdumpSr(p1);
            }
            if (ErrorDescription[0].equals("HA_NOT_INSTALLED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HaNotInstalled(p1);
            }
            if (ErrorDescription[0].equals("VM_BAD_POWER_STATE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.VmBadPowerState(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("HA_HOST_CANNOT_ACCESS_STATEFILE"))
            {
                throw new Types.HaHostCannotAccessStatefile();
            }
            if (ErrorDescription[0].equals("VM_FAILED_SHUTDOWN_ACKNOWLEDGMENT"))
            {
                throw new Types.VmFailedShutdownAcknowledgment();
            }
            if (ErrorDescription[0].equals("HOST_IN_EMERGENCY_MODE"))
            {
                throw new Types.HostInEmergencyMode();
            }
            if (ErrorDescription[0].equals("HOST_DISABLED_UNTIL_REBOOT"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostDisabledUntilReboot(p1);
            }
            if (ErrorDescription[0].equals("DEVICE_ALREADY_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.DeviceAlreadyExists(p1);
            }
            if (ErrorDescription[0].equals("SR_NOT_SHARABLE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.SrNotSharable(p1, p2);
            }
            if (ErrorDescription[0].equals("DEFAULT_SR_NOT_FOUND"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.DefaultSrNotFound(p1);
            }
            if (ErrorDescription[0].equals("LICENSE_CANNOT_DOWNGRADE_WHILE_IN_POOL"))
            {
                throw new Types.LicenseCannotDowngradeWhileInPool();
            }
            if (ErrorDescription[0].equals("TOO_MANY_PENDING_TASKS"))
            {
                throw new Types.TooManyPendingTasks();
            }
            if (ErrorDescription[0].equals("SR_UUID_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SrUuidExists(p1);
            }
            if (ErrorDescription[0].equals("PATCH_ALREADY_APPLIED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PatchAlreadyApplied(p1);
            }
            if (ErrorDescription[0].equals("OPERATION_BLOCKED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.OperationBlocked(p1, p2);
            }
            if (ErrorDescription[0].equals("PROVISION_ONLY_ALLOWED_ON_TEMPLATE"))
            {
                throw new Types.ProvisionOnlyAllowedOnTemplate();
            }
            if (ErrorDescription[0].equals("VM_SHUTDOWN_TIMEOUT"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VmShutdownTimeout(p1, p2);
            }
            if (ErrorDescription[0].equals("NETWORK_CONTAINS_PIF"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.NetworkContainsPif(p1);
            }
            if (ErrorDescription[0].equals("JOINING_HOST_SERVICE_FAILED"))
            {
                throw new Types.JoiningHostServiceFailed();
            }
            if (ErrorDescription[0].equals("VBD_TRAY_LOCKED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VbdTrayLocked(p1);
            }
            if (ErrorDescription[0].equals("VDI_MISSING"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VdiMissing(p1, p2);
            }
            if (ErrorDescription[0].equals("UUID_INVALID"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.UuidInvalid(p1, p2);
            }
            if (ErrorDescription[0].equals("LICENCE_RESTRICTION"))
            {
                throw new Types.LicenceRestriction();
            }
            if (ErrorDescription[0].equals("VIF_IN_USE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VifInUse(p1, p2);
            }
            if (ErrorDescription[0].equals("ONLY_ALLOWED_ON_OEM_EDITION"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.OnlyAllowedOnOemEdition(p1);
            }
            if (ErrorDescription[0].equals("VDI_IS_A_PHYSICAL_DEVICE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VdiIsAPhysicalDevice(p1);
            }
            if (ErrorDescription[0].equals("LICENSE_PROCESSING_ERROR"))
            {
                throw new Types.LicenseProcessingError();
            }
            if (ErrorDescription[0].equals("TASK_CANCELLED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.TaskCancelled(p1);
            }
            if (ErrorDescription[0].equals("HA_SHOULD_BE_FENCED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HaShouldBeFenced(p1);
            }
            if (ErrorDescription[0].equals("VM_UNSAFE_BOOT"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmUnsafeBoot(p1);
            }
            if (ErrorDescription[0].equals("PIF_HAS_NO_NETWORK_CONFIGURATION"))
            {
                throw new Types.PifHasNoNetworkConfiguration();
            }
            if (ErrorDescription[0].equals("TOO_BUSY"))
            {
                throw new Types.TooBusy();
            }
            if (ErrorDescription[0].equals("VALUE_NOT_SUPPORTED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.ValueNotSupported(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("SESSION_INVALID"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SessionInvalid(p1);
            }
            if (ErrorDescription[0].equals("HA_CONSTRAINT_VIOLATION_NETWORK_NOT_SHARED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HaConstraintViolationNetworkNotShared(p1);
            }
            if (ErrorDescription[0].equals("HA_FAILED_TO_FORM_LIVESET"))
            {
                throw new Types.HaFailedToFormLiveset();
            }
            if (ErrorDescription[0].equals("PIF_CANNOT_BOND_CROSS_HOST"))
            {
                throw new Types.PifCannotBondCrossHost();
            }
            if (ErrorDescription[0].equals("HA_OPERATION_WOULD_BREAK_FAILOVER_PLAN"))
            {
                throw new Types.HaOperationWouldBreakFailoverPlan();
            }
            if (ErrorDescription[0].equals("CANNOT_FIND_PATCH"))
            {
                throw new Types.CannotFindPatch();
            }
            if (ErrorDescription[0].equals("DB_UNIQUENESS_CONSTRAINT_VIOLATION"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.DbUniquenessConstraintViolation(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("CANNOT_FETCH_PATCH"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.CannotFetchPatch(p1);
            }
            if (ErrorDescription[0].equals("VM_REQUIRES_NETWORK"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VmRequiresNetwork(p1, p2);
            }
            if (ErrorDescription[0].equals("VBD_NOT_EMPTY"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VbdNotEmpty(p1);
            }
            if (ErrorDescription[0].equals("HOST_NOT_ENOUGH_FREE_MEMORY"))
            {
                throw new Types.HostNotEnoughFreeMemory();
            }
            if (ErrorDescription[0].equals("VM_MIGRATE_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                String p4 = ErrorDescription.length >= 4 ? ErrorDescription[4] : "";
                throw new Types.VmMigrateFailed(p1, p2, p3, p4);
            }
            if (ErrorDescription[0].equals("DEVICE_NOT_ATTACHED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.DeviceNotAttached(p1);
            }
            if (ErrorDescription[0].equals("HOST_DISABLED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostDisabled(p1);
            }
            if (ErrorDescription[0].equals("SR_OPERATION_NOT_SUPPORTED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SrOperationNotSupported(p1);
            }
            if (ErrorDescription[0].equals("SYSTEM_STATUS_MUST_USE_TAR_ON_OEM"))
            {
                throw new Types.SystemStatusMustUseTarOnOem();
            }
            if (ErrorDescription[0].equals("JOINING_HOST_CANNOT_CONTAIN_SHARED_SRS"))
            {
                throw new Types.JoiningHostCannotContainSharedSrs();
            }
            if (ErrorDescription[0].equals("VM_NO_VCPUS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmNoVcpus(p1);
            }
            if (ErrorDescription[0].equals("INVALID_PATCH_WITH_LOG"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.InvalidPatchWithLog(p1);
            }
            if (ErrorDescription[0].equals("SR_DEVICE_IN_USE"))
            {
                throw new Types.SrDeviceInUse();
            }
            if (ErrorDescription[0].equals("HA_HOST_IS_ARMED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HaHostIsArmed(p1);
            }
            if (ErrorDescription[0].equals("LICENSE_EXPIRED"))
            {
                throw new Types.LicenseExpired();
            }
            if (ErrorDescription[0].equals("SESSION_AUTHENTICATION_FAILED"))
            {
                throw new Types.SessionAuthenticationFailed();
            }
            if (ErrorDescription[0].equals("PIF_IS_VLAN"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PifIsVlan(p1);
            }
            if (ErrorDescription[0].equals("JOINING_HOST_CANNOT_BE_MASTER_OF_OTHER_HOSTS"))
            {
                throw new Types.JoiningHostCannotBeMasterOfOtherHosts();
            }
            if (ErrorDescription[0].equals("HOST_HAS_RESIDENT_VMS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostHasResidentVms(p1);
            }
            if (ErrorDescription[0].equals("PIF_IS_MANAGEMENT_INTERFACE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PifIsManagementInterface(p1);
            }
            if (ErrorDescription[0].equals("MAC_INVALID"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.MacInvalid(p1);
            }
            if (ErrorDescription[0].equals("VBD_IS_EMPTY"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VbdIsEmpty(p1);
            }
            if (ErrorDescription[0].equals("CANNOT_FIND_STATE_PARTITION"))
            {
                throw new Types.CannotFindStatePartition();
            }
            if (ErrorDescription[0].equals("NOT_IN_EMERGENCY_MODE"))
            {
                throw new Types.NotInEmergencyMode();
            }
            if (ErrorDescription[0].equals("PATCH_PRECHECK_FAILED_WRONG_SERVER_VERSION"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.PatchPrecheckFailedWrongServerVersion(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("NETWORK_ALREADY_CONNECTED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.NetworkAlreadyConnected(p1, p2);
            }
            if (ErrorDescription[0].equals("VDI_INCOMPATIBLE_TYPE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VdiIncompatibleType(p1, p2);
            }
            if (ErrorDescription[0].equals("IMPORT_ERROR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.ImportError(p1);
            }
            if (ErrorDescription[0].equals("SR_UNKNOWN_DRIVER"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SrUnknownDriver(p1);
            }
            if (ErrorDescription[0].equals("XENAPI_PLUGIN_FAILURE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.XenapiPluginFailure(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("MAC_STILL_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.MacStillExists(p1);
            }
            if (ErrorDescription[0].equals("HOST_IN_USE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.HostInUse(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("HA_TOO_FEW_HOSTS"))
            {
                throw new Types.HaTooFewHosts();
            }
            if (ErrorDescription[0].equals("PATCH_IS_APPLIED"))
            {
                throw new Types.PatchIsApplied();
            }
            if (ErrorDescription[0].equals("SR_HAS_PBD"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.SrHasPbd(p1);
            }
            if (ErrorDescription[0].equals("HOST_STILL_BOOTING"))
            {
                throw new Types.HostStillBooting();
            }
            if (ErrorDescription[0].equals("OBJECT_NOLONGER_EXISTS"))
            {
                throw new Types.ObjectNolongerExists();
            }
            if (ErrorDescription[0].equals("HOSTS_NOT_HOMOGENEOUS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostsNotHomogeneous(p1);
            }
            if (ErrorDescription[0].equals("PIF_VLAN_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PifVlanExists(p1);
            }
            if (ErrorDescription[0].equals("OUT_OF_SPACE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.OutOfSpace(p1);
            }
            if (ErrorDescription[0].equals("PATCH_ALREADY_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PatchAlreadyExists(p1);
            }
            if (ErrorDescription[0].equals("VM_MEMORY_SIZE_TOO_LOW"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmMemorySizeTooLow(p1);
            }
            if (ErrorDescription[0].equals("HOST_NOT_DISABLED"))
            {
                throw new Types.HostNotDisabled();
            }
            if (ErrorDescription[0].equals("FIELD_TYPE_ERROR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.FieldTypeError(p1);
            }
            if (ErrorDescription[0].equals("SLAVE_REQUIRES_MANAGEMENT_INTERFACE"))
            {
                throw new Types.SlaveRequiresManagementInterface();
            }
            if (ErrorDescription[0].equals("VM_IS_TEMPLATE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmIsTemplate(p1);
            }
            if (ErrorDescription[0].equals("VM_IS_PROTECTED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmIsProtected(p1);
            }
            if (ErrorDescription[0].equals("VM_REQUIRES_VDI"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VmRequiresVdi(p1, p2);
            }
            if (ErrorDescription[0].equals("VBD_CDS_MUST_BE_READONLY"))
            {
                throw new Types.VbdCdsMustBeReadonly();
            }
            if (ErrorDescription[0].equals("JOINING_HOST_CANNOT_HAVE_VMS_WITH_CURRENT_OPERATIONS"))
            {
                throw new Types.JoiningHostCannotHaveVmsWithCurrentOperations();
            }
            if (ErrorDescription[0].equals("CANNOT_CREATE_STATE_FILE"))
            {
                throw new Types.CannotCreateStateFile();
            }
            if (ErrorDescription[0].equals("MESSAGE_PARAMETER_COUNT_MISMATCH"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.MessageParameterCountMismatch(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("RESTORE_INCOMPATIBLE_VERSION"))
            {
                throw new Types.RestoreIncompatibleVersion();
            }
            if (ErrorDescription[0].equals("DEVICE_DETACH_REJECTED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.DeviceDetachRejected(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("JOINING_HOST_CANNOT_HAVE_RUNNING_OR_SUSPENDED_VMS"))
            {
                throw new Types.JoiningHostCannotHaveRunningOrSuspendedVms();
            }
            if (ErrorDescription[0].equals("PATCH_PRECHECK_FAILED_PREREQUISITE_MISSING"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.PatchPrecheckFailedPrerequisiteMissing(p1, p2);
            }
            if (ErrorDescription[0].equals("CANNOT_CONTACT_HOST"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.CannotContactHost(p1);
            }
            if (ErrorDescription[0].equals("VM_MISSING_PV_DRIVERS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmMissingPvDrivers(p1);
            }
            if (ErrorDescription[0].equals("VDI_LOCATION_MISSING"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VdiLocationMissing(p1, p2);
            }
            if (ErrorDescription[0].equals("PIF_VLAN_STILL_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PifVlanStillExists(p1);
            }
            if (ErrorDescription[0].equals("NETWORK_CONTAINS_VIF"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.NetworkContainsVif(p1);
            }
            if (ErrorDescription[0].equals("INVALID_VALUE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.InvalidValue(p1, p2);
            }
            if (ErrorDescription[0].equals("XENAPI_MISSING_PLUGIN"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.XenapiMissingPlugin(p1);
            }
            if (ErrorDescription[0].equals("RESTORE_TARGET_MGMT_IF_NOT_IN_BACKUP"))
            {
                throw new Types.RestoreTargetMgmtIfNotInBackup();
            }
            if (ErrorDescription[0].equals("JOINING_HOST_CONNECTION_FAILED"))
            {
                throw new Types.JoiningHostConnectionFailed();
            }
            if (ErrorDescription[0].equals("MESSAGE_METHOD_UNKNOWN"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.MessageMethodUnknown(p1);
            }
            if (ErrorDescription[0].equals("VM_CANNOT_DELETE_DEFAULT_TEMPLATE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VmCannotDeleteDefaultTemplate(p1);
            }
            if (ErrorDescription[0].equals("VDI_IS_NOT_ISO"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.VdiIsNotIso(p1, p2);
            }
            if (ErrorDescription[0].equals("INTERNAL_ERROR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.InternalError(p1);
            }
            if (ErrorDescription[0].equals("NOT_ALLOWED_ON_OEM_EDITION"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.NotAllowedOnOemEdition(p1);
            }
            if (ErrorDescription[0].equals("RESTORE_SCRIPT_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.RestoreScriptFailed(p1);
            }
            if (ErrorDescription[0].equals("LICENSE_DOES_NOT_SUPPORT_XHA"))
            {
                throw new Types.LicenseDoesNotSupportXha();
            }
            if (ErrorDescription[0].equals("VBD_NOT_REMOVABLE_MEDIA"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.VbdNotRemovableMedia(p1);
            }
            if (ErrorDescription[0].equals("DEVICE_ALREADY_DETACHED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.DeviceAlreadyDetached(p1);
            }
            if (ErrorDescription[0].equals("LOCATION_NOT_UNIQUE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.LocationNotUnique(p1, p2);
            }
            if (ErrorDescription[0].equals("NOT_IMPLEMENTED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.NotImplemented(p1);
            }
            if (ErrorDescription[0].equals("CANNOT_PLUG_VIF"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.CannotPlugVif(p1);
            }
            if (ErrorDescription[0].equals("BACKUP_SCRIPT_FAILED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.BackupScriptFailed(p1);
            }
            if (ErrorDescription[0].equals("OPERATION_NOT_ALLOWED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.OperationNotAllowed(p1);
            }
            if (ErrorDescription[0].equals("HA_NO_PLAN"))
            {
                throw new Types.HaNoPlan();
            }
            if (ErrorDescription[0].equals("DEVICE_DETACH_TIMEOUT"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.DeviceDetachTimeout(p1, p2);
            }
            if (ErrorDescription[0].equals("VM_DUPLICATE_VBD_DEVICE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.VmDuplicateVbdDevice(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("EVENTS_LOST"))
            {
                throw new Types.EventsLost();
            }
            if (ErrorDescription[0].equals("SR_BACKEND_FAILURE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.SrBackendFailure(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("VM_OLD_PV_DRIVERS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                String p3 = ErrorDescription.length >= 3 ? ErrorDescription[3] : "";
                throw new Types.VmOldPvDrivers(p1, p2, p3);
            }
            if (ErrorDescription[0].equals("PIF_DOES_NOT_ALLOW_UNPLUG"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.PifDoesNotAllowUnplug(p1);
            }
            if (ErrorDescription[0].equals("CHANGE_PASSWORD_REJECTED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.ChangePasswordRejected(p1);
            }
            if (ErrorDescription[0].equals("OTHER_OPERATION_IN_PROGRESS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.OtherOperationInProgress(p1, p2);
            }
            if (ErrorDescription[0].equals("HOST_OFFLINE"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostOffline(p1);
            }
            if (ErrorDescription[0].equals("HOST_HAS_NO_MANAGEMENT_IP"))
            {
                throw new Types.HostHasNoManagementIp();
            }
            if (ErrorDescription[0].equals("HA_IS_ENABLED"))
            {
                throw new Types.HaIsEnabled();
            }
            if (ErrorDescription[0].equals("HOST_NAME_INVALID"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HostNameInvalid(p1);
            }
            if (ErrorDescription[0].equals("DOMAIN_EXISTS"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.DomainExists(p1, p2);
            }
            if (ErrorDescription[0].equals("HA_POOL_IS_ENABLED_BUT_HOST_IS_DISABLED"))
            {
                throw new Types.HaPoolIsEnabledButHostIsDisabled();
            }
            if (ErrorDescription[0].equals("MESSAGE_DEPRECATED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.MessageDeprecated(p1);
            }
            if (ErrorDescription[0].equals("HA_CONSTRAINT_VIOLATION_SR_NOT_SHARED"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.HaConstraintViolationSrNotShared(p1);
            }
            if (ErrorDescription[0].equals("NOT_SUPPORTED_DURING_UPGRADE"))
            {
                throw new Types.NotSupportedDuringUpgrade();
            }
            if (ErrorDescription[0].equals("PIF_CONFIGURATION_ERROR"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                String p2 = ErrorDescription.length >= 2 ? ErrorDescription[2] : "";
                throw new Types.PifConfigurationError(p1, p2);
            }
            if (ErrorDescription[0].equals("INTERFACE_HAS_NO_IP"))
            {
                String p1 = ErrorDescription.length >= 1 ? ErrorDescription[1] : "";
                throw new Types.InterfaceHasNoIp(p1);
            }

            // An unknown error occurred
            throw new Types.XenAPIException(ErrorDescription);
        }

        throw new BadServerResponse(response);
    }

    public enum VdiOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * Scanning backends for new or deleted VDIs
         */
        SCAN,
        /**
         * Cloning the VDI
         */
        CLONE,
        /**
         * Copying the VDI
         */
        COPY,
        /**
         * Resizing the VDI
         */
        RESIZE,
        /**
         * Resizing the VDI which may or may not be online
         */
        RESIZE_ONLINE,
        /**
         * Snapshotting the VDI
         */
        SNAPSHOT,
        /**
         * Destroying the VDI
         */
        DESTROY,
        /**
         * Forget about the VDI
         */
        FORGET,
        /**
         * Refreshing the fields of the VDI
         */
        UPDATE,
        /**
         * Forcibly unlocking the VDI
         */
        FORCE_UNLOCK,
        /**
         * Generating static configuration
         */
        GENERATE_CONFIG
    };

    public enum VdiType {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * a disk that may be replaced on upgrade
         */
        SYSTEM,
        /**
         * a disk that is always preserved on upgrade
         */
        USER,
        /**
         * a disk that may be reformatted on upgrade
         */
        EPHEMERAL,
        /**
         * a disk that stores a suspend image
         */
        SUSPEND,
        /**
         * a disk that stores VM crashdump information
         */
        CRASHDUMP,
        /**
         * a disk used for HA storage heartbeating
         */
        HA_STATEFILE,
        /**
         * a disk used for HA Pool metadata
         */
        METADATA
    };

    public enum Cls {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * VM
         */
        VM,
        /**
         * Host
         */
        HOST,
        /**
         * SR
         */
        SR,
        /**
         * Pool
         */
        POOL
    };

    public enum AfterApplyGuidance {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * This patch requires HVM guests to be restarted once applied.
         */
        RESTARTHVM,
        /**
         * This patch requires PV guests to be restarted once applied.
         */
        RESTARTPV,
        /**
         * This patch requires the host to be restarted once applied.
         */
        RESTARTHOST,
        /**
         * This patch requires XAPI to be restarted once applied.
         */
        RESTARTXAPI
    };

    public enum EventOperation {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * An object has been created
         */
        ADD,
        /**
         * An object has been deleted
         */
        DEL,
        /**
         * An object has been modified
         */
        MOD
    };

    public enum TaskAllowedOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * refers to the operation "cancel"
         */
        CANCEL
    };

    public enum TaskStatusType {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * task is in progress
         */
        PENDING,
        /**
         * task was completed successfully
         */
        SUCCESS,
        /**
         * task has failed
         */
        FAILURE,
        /**
         * task is being cancelled
         */
        CANCELLING,
        /**
         * task has been cancelled
         */
        CANCELLED
    };

    public enum NetworkOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * Indicates this network is attaching to a VIF or PIF
         */
        ATTACHING
    };

    public enum OnCrashBehaviour {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * destroy the VM state
         */
        DESTROY,
        /**
         * record a coredump and then destroy the VM state
         */
        COREDUMP_AND_DESTROY,
        /**
         * restart the VM
         */
        RESTART,
        /**
         * record a coredump and then restart the VM
         */
        COREDUMP_AND_RESTART,
        /**
         * leave the crashed VM paused
         */
        PRESERVE,
        /**
         * rename the crashed VM and start a new copy
         */
        RENAME_RESTART
    };

    public enum ConsoleProtocol {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * VT100 terminal
         */
        VT100,
        /**
         * Remote FrameBuffer protocol (as used in VNC)
         */
        RFB,
        /**
         * Remote Desktop Protocol
         */
        RDP
    };

    public enum OnNormalExit {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * destroy the VM state
         */
        DESTROY,
        /**
         * restart the VM
         */
        RESTART
    };

    public enum VifOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * Attempting to attach this VIF to a VM
         */
        ATTACH,
        /**
         * Attempting to hotplug this VIF
         */
        PLUG,
        /**
         * Attempting to hot unplug this VIF
         */
        UNPLUG
    };

    public enum XenAPIObjects {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * A session
         */
        SESSION,
        /**
         * A long-running asynchronous task
         */
        TASK,
        /**
         * Asynchronous event registration and handling
         */
        EVENT,
        /**
         * Pool-wide information
         */
        POOL,
        /**
         * Pool-wide patches
         */
        POOL_PATCH,
        /**
         * A virtual machine (or 'guest').
         */
        VM,
        /**
         * The metrics associated with a VM
         */
        VM_METRICS,
        /**
         * The metrics reported by the guest (as opposed to inferred from outside)
         */
        VM_GUEST_METRICS,
        /**
         * A physical host
         */
        HOST,
        /**
         * Represents a host crash dump
         */
        HOST_CRASHDUMP,
        /**
         * Represents a patch stored on a server
         */
        HOST_PATCH,
        /**
         * The metrics associated with a host
         */
        HOST_METRICS,
        /**
         * A physical CPU
         */
        HOST_CPU,
        /**
         * A virtual network
         */
        NETWORK,
        /**
         * A virtual network interface
         */
        VIF,
        /**
         * The metrics associated with a virtual network device
         */
        VIF_METRICS,
        /**
         * A physical network interface (note separate VLANs are represented as several PIFs)
         */
        PIF,
        /**
         * The metrics associated with a physical network interface
         */
        PIF_METRICS,
        /**
         * 
         */
        BOND,
        /**
         * A VLAN mux/demux
         */
        VLAN,
        /**
         * A storage manager plugin
         */
        SM,
        /**
         * A storage repository
         */
        SR,
        /**
         * A virtual disk image
         */
        VDI,
        /**
         * A virtual block device
         */
        VBD,
        /**
         * The metrics associated with a virtual block device
         */
        VBD_METRICS,
        /**
         * The physical block devices through which hosts access SRs
         */
        PBD,
        /**
         * A VM crashdump
         */
        CRASHDUMP,
        /**
         * A virtual TPM device
         */
        VTPM,
        /**
         * A console
         */
        CONSOLE,
        /**
         * A user of the system
         */
        USER,
        /**
         * Data sources for logging in RRDs
         */
        DATA_SOURCE,
        /**
         * A placeholder for a binary blob
         */
        BLOB,
        /**
         * An message for the attention of the administrator
         */
        MESSAGE
    };

    public enum HostAllowedOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * Indicates this host is able to provision another VM
         */
        PROVISION,
        /**
         * Indicates this host is evacuating
         */
        EVACUATE,
        /**
         * Indicates this host is in the process of shutting itself down
         */
        SHUTDOWN,
        /**
         * Indicates this host is in the process of rebooting
         */
        REBOOT,
        /**
         * Indicates this host is in the process of being powered on
         */
        POWER_ON,
        /**
         * This host is starting a VM
         */
        VM_START,
        /**
         * This host is resuming a VM
         */
        VM_RESUME,
        /**
         * This host is the migration target of a VM
         */
        VM_MIGRATE
    };

    public enum VbdMode {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * only read-only access will be allowed
         */
        RO,
        /**
         * read-write access will be allowed
         */
        RW
    };

    public enum VbdType {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * VBD will appear to guest as CD
         */
        CD,
        /**
         * VBD will appear to guest as disk
         */
        DISK
    };

    public enum VbdOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * Attempting to attach this VBD to a VM
         */
        ATTACH,
        /**
         * Attempting to eject the media from this VBD
         */
        EJECT,
        /**
         * Attempting to insert new media into this VBD
         */
        INSERT,
        /**
         * Attempting to hotplug this VBD
         */
        PLUG,
        /**
         * Attempting to hot unplug this VBD
         */
        UNPLUG,
        /**
         * Attempting to forcibly unplug this VBD
         */
        UNPLUG_FORCE,
        /**
         * Attempting to pause a block device backend
         */
        PAUSE,
        /**
         * Attempting to unpause a block device backend
         */
        UNPAUSE
    };

    public enum VmPowerState {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * VM is offline and not using any resources
         */
        HALTED,
        /**
         * All resources have been allocated but the VM itself is paused and its vCPUs are not running
         */
        PAUSED,
        /**
         * Running
         */
        RUNNING,
        /**
         * VM state has been saved to disk and it is nolonger running. Note that disks remain in-use while the VM is suspended.
         */
        SUSPENDED,
        /**
         * Some other unknown state
         */
        UNKNOWN
    };

    public enum VmOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * refers to the operation "snapshot"
         */
        SNAPSHOT,
        /**
         * refers to the operation "clone"
         */
        CLONE,
        /**
         * refers to the operation "copy"
         */
        COPY,
        /**
         * refers to the operation "provision"
         */
        PROVISION,
        /**
         * refers to the operation "start"
         */
        START,
        /**
         * refers to the operation "start_on"
         */
        START_ON,
        /**
         * refers to the operation "pause"
         */
        PAUSE,
        /**
         * refers to the operation "unpause"
         */
        UNPAUSE,
        /**
         * refers to the operation "clean_shutdown"
         */
        CLEAN_SHUTDOWN,
        /**
         * refers to the operation "clean_reboot"
         */
        CLEAN_REBOOT,
        /**
         * refers to the operation "hard_shutdown"
         */
        HARD_SHUTDOWN,
        /**
         * refers to the operation "power_state_reset"
         */
        POWER_STATE_RESET,
        /**
         * refers to the operation "hard_reboot"
         */
        HARD_REBOOT,
        /**
         * refers to the operation "suspend"
         */
        SUSPEND,
        /**
         * refers to the operation "csvm"
         */
        CSVM,
        /**
         * refers to the operation "resume"
         */
        RESUME,
        /**
         * refers to the operation "resume_on"
         */
        RESUME_ON,
        /**
         * refers to the operation "pool_migrate"
         */
        POOL_MIGRATE,
        /**
         * refers to the operation "migrate"
         */
        MIGRATE,
        /**
         * refers to the operation "get_boot_record"
         */
        GET_BOOT_RECORD,
        /**
         * refers to the operation "send_sysrq"
         */
        SEND_SYSRQ,
        /**
         * refers to the operation "send_trigger"
         */
        SEND_TRIGGER,
        /**
         * Changing the memory settings
         */
        CHANGING_MEMORY_LIVE,
        /**
         * Waiting for the memory settings to change
         */
        AWAITING_MEMORY_LIVE,
        /**
         * Changing the shadow memory settings
         */
        CHANGING_SHADOW_MEMORY_LIVE,
        /**
         * Changing either the VCPUs_number or VCPUs_params
         */
        CHANGING_VCPUS_LIVE,
        /**
         * 
         */
        ASSERT_OPERATION_VALID,
        /**
         * Add, remove, query or list data sources
         */
        DATA_SOURCE_OP,
        /**
         * 
         */
        UPDATE_ALLOWED_OPERATIONS,
        /**
         * Turning this VM into a template
         */
        MAKE_INTO_TEMPLATE,
        /**
         * importing a VM from a network stream
         */
        IMPORT,
        /**
         * exporting a VM to a network stream
         */
        EXPORT,
        /**
         * refers to the act of uninstalling the VM
         */
        DESTROY
    };

    public enum IpConfigurationMode {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * Do not acquire an IP address
         */
        NONE,
        /**
         * Acquire an IP address by DHCP
         */
        DHCP,
        /**
         * Static IP address configuration
         */
        STATIC
    };

    public enum StorageOperations {
        /**
         * The value does not belong to this enumeration
         */
        UNRECOGNIZED,
        /**
         * Scanning backends for new or deleted VDIs
         */
        SCAN,
        /**
         * Destroying the SR
         */
        DESTROY,
        /**
         * Forgetting about SR
         */
        FORGET,
        /**
         * Plugging a PBD into this SR
         */
        PLUG,
        /**
         * Unplugging a PBD from this SR
         */
        UNPLUG,
        /**
         * Refresh the fields on the SR
         */
        UPDATE,
        /**
         * Creating a new VDI
         */
        VDI_CREATE,
        /**
         * Introducing a new VDI
         */
        VDI_INTRODUCE,
        /**
         * Destroying a VDI
         */
        VDI_DESTROY,
        /**
         * Resizing a VDI
         */
        VDI_RESIZE,
        /**
         * Cloneing a VDI
         */
        VDI_CLONE,
        /**
         * Snapshotting a VDI
         */
        VDI_SNAPSHOT
    };


    /**
     * The restore could not be performed because a network interface is missing
     */
    public static class RestoreTargetMissingDevice extends XenAPIException {
        public final String device;

        /**
         * Create a new RestoreTargetMissingDevice
         *
         * @param device
         */
        public RestoreTargetMissingDevice(String device) {
            super("The restore could not be performed because a network interface is missing");
            this.device = device;
        }

    }

    /**
     * The MAC address specified doesn't exist on this host.
     */
    public static class MacDoesNotExist extends XenAPIException {
        public final String MAC;

        /**
         * Create a new MacDoesNotExist
         *
         * @param MAC
         */
        public MacDoesNotExist(String MAC) {
            super("The MAC address specified doesn't exist on this host.");
            this.MAC = MAC;
        }

    }

    /**
     * You gave an invalid object reference.  The object may have recently been deleted.  The class parameter gives the type of reference given, and the handle parameter echoes the bad value given.
     */
    public static class HandleInvalid extends XenAPIException {
        public final String clazz;
        public final String handle;

        /**
         * Create a new HandleInvalid
         *
         * @param clazz
         * @param handle
         */
        public HandleInvalid(String clazz, String handle) {
            super("You gave an invalid object reference.  The object may have recently been deleted.  The class parameter gives the type of reference given, and the handle parameter echoes the bad value given.");
            this.clazz = clazz;
            this.handle = handle;
        }

    }

    /**
     * The device is already attached to a VM
     */
    public static class DeviceAlreadyAttached extends XenAPIException {
        public final String device;

        /**
         * Create a new DeviceAlreadyAttached
         *
         * @param device
         */
        public DeviceAlreadyAttached(String device) {
            super("The device is already attached to a VM");
            this.device = device;
        }

    }

    /**
     * A required parameter contained an invalid IP address
     */
    public static class InvalidIpAddressSpecified extends XenAPIException {
        public final String parameter;

        /**
         * Create a new InvalidIpAddressSpecified
         *
         * @param parameter
         */
        public InvalidIpAddressSpecified(String parameter) {
            super("A required parameter contained an invalid IP address");
            this.parameter = parameter;
        }

    }

    /**
     * The SR operation cannot be performed because the SR is not empty.
     */
    public static class SrNotEmpty extends XenAPIException {

        /**
         * Create a new SrNotEmpty
         */
        public SrNotEmpty() {
            super("The SR operation cannot be performed because the SR is not empty.");
        }

    }

    /**
     * HVM is required for this operation
     */
    public static class VmHvmRequired extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmHvmRequired
         *
         * @param vm
         */
        public VmHvmRequired(String vm) {
            super("HVM is required for this operation");
            this.vm = vm;
        }

    }

    /**
     * A bond must consist of at least two member interfaces
     */
    public static class PifBondNeedsMoreMembers extends XenAPIException {

        /**
         * Create a new PifBondNeedsMoreMembers
         */
        public PifBondNeedsMoreMembers() {
            super("A bond must consist of at least two member interfaces");
        }

    }

    /**
     * This operation cannot be performed because the pif is bonded.
     */
    public static class PifAlreadyBonded extends XenAPIException {
        public final String PIF;

        /**
         * Create a new PifAlreadyBonded
         *
         * @param PIF
         */
        public PifAlreadyBonded(String PIF) {
            super("This operation cannot be performed because the pif is bonded.");
            this.PIF = PIF;
        }

    }

    /**
     * You tried to create a VLAN, but the tag you gave was invalid -- it must be between 0 and 4094.  The parameter echoes the VLAN tag you gave.
     */
    public static class VlanTagInvalid extends XenAPIException {
        public final String VLAN;

        /**
         * Create a new VlanTagInvalid
         *
         * @param VLAN
         */
        public VlanTagInvalid(String VLAN) {
            super("You tried to create a VLAN, but the tag you gave was invalid -- it must be between 0 and 4094.  The parameter echoes the VLAN tag you gave.");
            this.VLAN = VLAN;
        }

    }

    /**
     * You cannot make regular API calls directly on a slave. Please pass API calls via the master host.
     */
    public static class HostIsSlave extends XenAPIException {
        public final String masterIPAddress;

        /**
         * Create a new HostIsSlave
         *
         * @param masterIPAddress
         */
        public HostIsSlave(String masterIPAddress) {
            super("You cannot make regular API calls directly on a slave. Please pass API calls via the master host.");
            this.masterIPAddress = masterIPAddress;
        }

    }

    /**
     * The backup partition to stream the updat to cannot be found
     */
    public static class CannotFindOemBackupPartition extends XenAPIException {

        /**
         * Create a new CannotFindOemBackupPartition
         */
        public CannotFindOemBackupPartition() {
            super("The backup partition to stream the updat to cannot be found");
        }

    }

    /**
     * The specified device was not found.
     */
    public static class PifDeviceNotFound extends XenAPIException {

        /**
         * Create a new PifDeviceNotFound
         */
        public PifDeviceNotFound() {
            super("The specified device was not found.");
        }

    }

    /**
     * The patch precheck stage failed: there are one or more VMs still running on the server.  All VMs must be suspended before the patch can be applied.
     */
    public static class PatchPrecheckFailedVmRunning extends XenAPIException {
        public final String patch;

        /**
         * Create a new PatchPrecheckFailedVmRunning
         *
         * @param patch
         */
        public PatchPrecheckFailedVmRunning(String patch) {
            super("The patch precheck stage failed: there are one or more VMs still running on the server.  All VMs must be suspended before the patch can be applied.");
            this.patch = patch;
        }

    }

    /**
     * The operation failed because the HA software on the specified host could not see a subset of other hosts. Check your network connectivity.
     */
    public static class HaHostCannotSeePeers extends XenAPIException {
        public final String host;
        public final String all;
        public final String subset;

        /**
         * Create a new HaHostCannotSeePeers
         *
         * @param host
         * @param all
         * @param subset
         */
        public HaHostCannotSeePeers(String host, String all, String subset) {
            super("The operation failed because the HA software on the specified host could not see a subset of other hosts. Check your network connectivity.");
            this.host = host;
            this.all = all;
            this.subset = subset;
        }

    }

    /**
     * Caller not allowed to perform this operation.
     */
    public static class PermissionDenied extends XenAPIException {
        public final String message;

        /**
         * Create a new PermissionDenied
         *
         * @param message
         */
        public PermissionDenied(String message) {
            super("Caller not allowed to perform this operation.");
            this.message = message;
        }

    }

    /**
     * Attaching this SR failed.
     */
    public static class SrAttachFailed extends XenAPIException {
        public final String sr;

        /**
         * Create a new SrAttachFailed
         *
         * @param sr
         */
        public SrAttachFailed(String sr) {
            super("Attaching this SR failed.");
            this.sr = sr;
        }

    }

    /**
     * This host lost access to the HA statefile.
     */
    public static class HaLostStatefile extends XenAPIException {

        /**
         * Create a new HaLostStatefile
         */
        public HaLostStatefile() {
            super("This host lost access to the HA statefile.");
        }

    }

    /**
     * The operation could not be performed because HA is not enabled on the Pool
     */
    public static class HaNotEnabled extends XenAPIException {

        /**
         * Create a new HaNotEnabled
         */
        public HaNotEnabled() {
            super("The operation could not be performed because HA is not enabled on the Pool");
        }

    }

    /**
     * The host could not join the liveset because the HA daemon failed to start.
     */
    public static class HaHeartbeatDaemonStartupFailed extends XenAPIException {

        /**
         * Create a new HaHeartbeatDaemonStartupFailed
         */
        public HaHeartbeatDaemonStartupFailed() {
            super("The host could not join the liveset because the HA daemon failed to start.");
        }

    }

    /**
     * This session is not registered to receive events.  You must call event.register before event.next.  The session handle you are using is echoed.
     */
    public static class SessionNotRegistered extends XenAPIException {
        public final String handle;

        /**
         * Create a new SessionNotRegistered
         *
         * @param handle
         */
        public SessionNotRegistered(String handle) {
            super("This session is not registered to receive events.  You must call event.register before event.next.  The session handle you are using is echoed.");
            this.handle = handle;
        }

    }

    /**
     * This VM does not have a suspend SR specified.
     */
    public static class VmNoSuspendSr extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmNoSuspendSr
         *
         * @param vm
         */
        public VmNoSuspendSr(String vm) {
            super("This VM does not have a suspend SR specified.");
            this.vm = vm;
        }

    }

    /**
     * The SR is full. Requested new size exceeds the maximum size
     */
    public static class SrFull extends XenAPIException {
        public final String requested;
        public final String maximum;

        /**
         * Create a new SrFull
         *
         * @param requested
         * @param maximum
         */
        public SrFull(String requested, String maximum) {
            super("The SR is full. Requested new size exceeds the maximum size");
            this.requested = requested;
            this.maximum = maximum;
        }

    }

    /**
     * The patch apply failed.  Please see attached output.
     */
    public static class PatchApplyFailed extends XenAPIException {
        public final String output;

        /**
         * Create a new PatchApplyFailed
         *
         * @param output
         */
        public PatchApplyFailed(String output) {
            super("The patch apply failed.  Please see attached output.");
            this.output = output;
        }

    }

    /**
     * The operation required write access but this VDI is read-only
     */
    public static class VdiReadonly extends XenAPIException {
        public final String vdi;

        /**
         * Create a new VdiReadonly
         *
         * @param vdi
         */
        public VdiReadonly(String vdi) {
            super("The operation required write access but this VDI is read-only");
            this.vdi = vdi;
        }

    }

    /**
     * This operation cannot be performed because this VDI could not be properly attached to the VM.
     */
    public static class VdiNotAvailable extends XenAPIException {
        public final String vdi;

        /**
         * Create a new VdiNotAvailable
         *
         * @param vdi
         */
        public VdiNotAvailable(String vdi) {
            super("This operation cannot be performed because this VDI could not be properly attached to the VM.");
            this.vdi = vdi;
        }

    }

    /**
     * The server failed to unmarshal the XMLRPC message; it was expecting one element and received something else.
     */
    public static class XmlrpcUnmarshalFailure extends XenAPIException {
        public final String expected;
        public final String received;

        /**
         * Create a new XmlrpcUnmarshalFailure
         *
         * @param expected
         * @param received
         */
        public XmlrpcUnmarshalFailure(String expected, String received) {
            super("The server failed to unmarshal the XMLRPC message; it was expecting one element and received something else.");
            this.expected = expected;
            this.received = received;
        }

    }

    /**
     * The master reports that it cannot talk back to the slave on the supplied management IP address.
     */
    public static class HostMasterCannotTalkBack extends XenAPIException {
        public final String ip;

        /**
         * Create a new HostMasterCannotTalkBack
         *
         * @param ip
         */
        public HostMasterCannotTalkBack(String ip) {
            super("The master reports that it cannot talk back to the slave on the supplied management IP address.");
            this.ip = ip;
        }

    }

    /**
     * 3rd party xapi hook failed
     */
    public static class XapiHookFailed extends XenAPIException {
        public final String hookName;
        public final String reason;
        public final String stdout;
        public final String exitCode;

        /**
         * Create a new XapiHookFailed
         *
         * @param hookName
         * @param reason
         * @param stdout
         * @param exitCode
         */
        public XapiHookFailed(String hookName, String reason, String stdout, String exitCode) {
            super("3rd party xapi hook failed");
            this.hookName = hookName;
            this.reason = reason;
            this.stdout = stdout;
            this.exitCode = exitCode;
        }

    }

    /**
     * The requested bootloader is unknown
     */
    public static class UnknownBootloader extends XenAPIException {
        public final String vm;
        public final String bootloader;

        /**
         * Create a new UnknownBootloader
         *
         * @param vm
         * @param bootloader
         */
        public UnknownBootloader(String vm, String bootloader) {
            super("The requested bootloader is unknown");
            this.vm = vm;
            this.bootloader = bootloader;
        }

    }

    /**
     * The import failed because this export has been created by a different (incompatible) product verion
     */
    public static class ImportIncompatibleVersion extends XenAPIException {

        /**
         * Create a new ImportIncompatibleVersion
         */
        public ImportIncompatibleVersion() {
            super("The import failed because this export has been created by a different (incompatible) product verion");
        }

    }

    /**
     * The operation could not proceed because necessary VDIs were already locked at the storage level.
     */
    public static class SrVdiLockingFailed extends XenAPIException {

        /**
         * Create a new SrVdiLockingFailed
         */
        public SrVdiLockingFailed() {
            super("The operation could not proceed because necessary VDIs were already locked at the storage level.");
        }

    }

    /**
     * You tried to destroy a PIF, but it represents an aspect of the physical host configuration, and so cannot be destroyed.  The parameter echoes the PIF handle you gave.
     */
    public static class PifIsPhysical extends XenAPIException {
        public final String PIF;

        /**
         * Create a new PifIsPhysical
         *
         * @param PIF
         */
        public PifIsPhysical(String PIF) {
            super("You tried to destroy a PIF, but it represents an aspect of the physical host configuration, and so cannot be destroyed.  The parameter echoes the PIF handle you gave.");
            this.PIF = PIF;
        }

    }

    /**
     * You tried to add a key-value pair to a map, but that key is already there.
     */
    public static class MapDuplicateKey extends XenAPIException {
        public final String type;
        public final String paramName;
        public final String uuid;
        public final String key;

        /**
         * Create a new MapDuplicateKey
         *
         * @param type
         * @param paramName
         * @param uuid
         * @param key
         */
        public MapDuplicateKey(String type, String paramName, String uuid, String key) {
            super("You tried to add a key-value pair to a map, but that key is already there.");
            this.type = type;
            this.paramName = paramName;
            this.uuid = uuid;
            this.key = key;
        }

    }

    /**
     * The bootloader returned an error
     */
    public static class BootloaderFailed extends XenAPIException {
        public final String vm;
        public final String msg;

        /**
         * Create a new BootloaderFailed
         *
         * @param vm
         * @param msg
         */
        public BootloaderFailed(String vm, String msg) {
            super("The bootloader returned an error");
            this.vm = vm;
            this.msg = msg;
        }

    }

    /**
     * Retrieving system status from the host failed.  A diagnostic reason suitable for support organisations is also returned.
     */
    public static class SystemStatusRetrievalFailed extends XenAPIException {
        public final String reason;

        /**
         * Create a new SystemStatusRetrievalFailed
         *
         * @param reason
         */
        public SystemStatusRetrievalFailed(String reason) {
            super("Retrieving system status from the host failed.  A diagnostic reason suitable for support organisations is also returned.");
            this.reason = reason;
        }

    }

    /**
     * This operation cannot be completed as the host is not live.
     */
    public static class HostNotLive extends XenAPIException {

        /**
         * Create a new HostNotLive
         */
        public HostNotLive() {
            super("This operation cannot be completed as the host is not live.");
        }

    }

    /**
     * This operation cannot be performed because this VDI is in use by some other operation
     */
    public static class VdiInUse extends XenAPIException {
        public final String vdi;
        public final String operation;

        /**
         * Create a new VdiInUse
         *
         * @param vdi
         * @param operation
         */
        public VdiInUse(String vdi, String operation) {
            super("This operation cannot be performed because this VDI is in use by some other operation");
            this.vdi = vdi;
            this.operation = operation;
        }

    }

    /**
     * The SR has no attached PBDs
     */
    public static class SrHasNoPbds extends XenAPIException {
        public final String sr;

        /**
         * Create a new SrHasNoPbds
         *
         * @param sr
         */
        public SrHasNoPbds(String sr) {
            super("The SR has no attached PBDs");
            this.sr = sr;
        }

    }

    /**
     * The uploaded patch file is invalid
     */
    public static class InvalidPatch extends XenAPIException {

        /**
         * Create a new InvalidPatch
         */
        public InvalidPatch() {
            super("The uploaded patch file is invalid");
        }

    }

    /**
     * This host cannot accept the proposed new master setting at this time.
     */
    public static class HaAbortNewMaster extends XenAPIException {
        public final String reason;

        /**
         * Create a new HaAbortNewMaster
         *
         * @param reason
         */
        public HaAbortNewMaster(String reason) {
            super("This host cannot accept the proposed new master setting at this time.");
            this.reason = reason;
        }

    }

    /**
     * The host joining the pool must have a physical management NIC (i.e. the management NIC must not be on a VLAN or bonded PIF).
     */
    public static class PoolJoiningHostMustHavePhysicalManagementNic extends XenAPIException {

        /**
         * Create a new PoolJoiningHostMustHavePhysicalManagementNic
         */
        public PoolJoiningHostMustHavePhysicalManagementNic() {
            super("The host joining the pool must have a physical management NIC (i.e. the management NIC must not be on a VLAN or bonded PIF).");
        }

    }

    /**
     * The patch precheck stage failed with an unknown error.  See attached info for more details.
     */
    public static class PatchPrecheckFailedUnknownError extends XenAPIException {
        public final String patch;
        public final String info;

        /**
         * Create a new PatchPrecheckFailedUnknownError
         *
         * @param patch
         * @param info
         */
        public PatchPrecheckFailedUnknownError(String patch, String info) {
            super("The patch precheck stage failed with an unknown error.  See attached info for more details.");
            this.patch = patch;
            this.info = info;
        }

    }

    /**
     * Cannot restore this VM because it would create a duplicate
     */
    public static class DuplicateVm extends XenAPIException {
        public final String vm;

        /**
         * Create a new DuplicateVm
         *
         * @param vm
         */
        public DuplicateVm(String vm) {
            super("Cannot restore this VM because it would create a duplicate");
            this.vm = vm;
        }

    }

    /**
     * Host cannot attach network (in the case of NIC bonding, this may be because attaching the network on this host would require other networks [that are currently active] to be taken down).
     */
    public static class HostCannotAttachNetwork extends XenAPIException {
        public final String host;
        public final String network;

        /**
         * Create a new HostCannotAttachNetwork
         *
         * @param host
         * @param network
         */
        public HostCannotAttachNetwork(String host, String network) {
            super("Host cannot attach network (in the case of NIC bonding, this may be because attaching the network on this host would require other networks [that are currently active] to be taken down).");
            this.host = host;
            this.network = network;
        }

    }

    /**
     * This host cannot destroy itself.
     */
    public static class HostCannotDestroySelf extends XenAPIException {

        /**
         * Create a new HostCannotDestroySelf
         */
        public HostCannotDestroySelf() {
            super("This host cannot destroy itself.");
        }

    }

    /**
     * This host failed in the middle of an automatic failover operation and needs to retry the failover action
     */
    public static class HostBroken extends XenAPIException {

        /**
         * Create a new HostBroken
         */
        public HostBroken() {
            super("This host failed in the middle of an automatic failover operation and needs to retry the failover action");
        }

    }

    /**
     * Too many VCPUs to start this VM
     */
    public static class VmTooManyVcpus extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmTooManyVcpus
         *
         * @param vm
         */
        public VmTooManyVcpus(String vm) {
            super("Too many VCPUs to start this VM");
            this.vm = vm;
        }

    }

    /**
     * This operation cannot be completed as the host is still live.
     */
    public static class HostIsLive extends XenAPIException {
        public final String host;

        /**
         * Create a new HostIsLive
         *
         * @param host
         */
        public HostIsLive(String host) {
            super("This operation cannot be completed as the host is still live.");
            this.host = host;
        }

    }

    /**
     * Drive could not be hot-unplugged because it is not marked as unpluggable
     */
    public static class VbdNotUnpluggable extends XenAPIException {
        public final String vbd;

        /**
         * Create a new VbdNotUnpluggable
         *
         * @param vbd
         */
        public VbdNotUnpluggable(String vbd) {
            super("Drive could not be hot-unplugged because it is not marked as unpluggable");
            this.vbd = vbd;
        }

    }

    /**
     * This host cannot be evacuated.
     */
    public static class CannotEvacuateHost extends XenAPIException {
        public final String errors;

        /**
         * Create a new CannotEvacuateHost
         *
         * @param errors
         */
        public CannotEvacuateHost(String errors) {
            super("This host cannot be evacuated.");
            this.errors = errors;
        }

    }

    /**
     * There were no hosts available to complete the specified operation.
     */
    public static class NoHostsAvailable extends XenAPIException {

        /**
         * Create a new NoHostsAvailable
         */
        public NoHostsAvailable() {
            super("There were no hosts available to complete the specified operation.");
        }

    }

    /**
     * A timeout happened while attempting to attach a device to a VM.
     */
    public static class DeviceAttachTimeout extends XenAPIException {
        public final String type;
        public final String ref;

        /**
         * Create a new DeviceAttachTimeout
         *
         * @param type
         * @param ref
         */
        public DeviceAttachTimeout(String type, String ref) {
            super("A timeout happened while attempting to attach a device to a VM.");
            this.type = type;
            this.ref = ref;
        }

    }

    /**
     * A PBD already exists connecting the SR to the host
     */
    public static class PbdExists extends XenAPIException {
        public final String sr;
        public final String host;
        public final String pbd;

        /**
         * Create a new PbdExists
         *
         * @param sr
         * @param host
         * @param pbd
         */
        public PbdExists(String sr, String host, String pbd) {
            super("A PBD already exists connecting the SR to the host");
            this.sr = sr;
            this.host = host;
            this.pbd = pbd;
        }

    }

    /**
     * The device name is invalid
     */
    public static class InvalidDevice extends XenAPIException {
        public final String device;

        /**
         * Create a new InvalidDevice
         *
         * @param device
         */
        public InvalidDevice(String device) {
            super("The device name is invalid");
            this.device = device;
        }

    }

    /**
     * The metrics of this host could not be read.
     */
    public static class HostCannotReadMetrics extends XenAPIException {

        /**
         * Create a new HostCannotReadMetrics
         */
        public HostCannotReadMetrics() {
            super("The metrics of this host could not be read.");
        }

    }

    /**
     * This host cannot join a pool because it's license does not support pooling
     */
    public static class LicenseDoesNotSupportPooling extends XenAPIException {

        /**
         * Create a new LicenseDoesNotSupportPooling
         */
        public LicenseDoesNotSupportPooling() {
            super("This host cannot join a pool because it's license does not support pooling");
        }

    }

    /**
     * The master says the host is not known to it. Perhaps the Host was deleted from the master's database?
     */
    public static class HostUnknownToMaster extends XenAPIException {
        public final String host;

        /**
         * Create a new HostUnknownToMaster
         *
         * @param host
         */
        public HostUnknownToMaster(String host) {
            super("The master says the host is not known to it. Perhaps the Host was deleted from the master's database?");
            this.host = host;
        }

    }

    /**
     * You attempted to run a VM on a host which doesn't have access to an SR needed by the VM. The VM has at least one VBD attached to a VDI in the SR.
     */
    public static class VmRequiresSr extends XenAPIException {
        public final String vm;
        public final String sr;

        /**
         * Create a new VmRequiresSr
         *
         * @param vm
         * @param sr
         */
        public VmRequiresSr(String vm, String sr) {
            super("You attempted to run a VM on a host which doesn't have access to an SR needed by the VM. The VM has at least one VBD attached to a VDI in the SR.");
            this.vm = vm;
            this.sr = sr;
        }

    }

    /**
     * This VM does not have a crashdump SR specified.
     */
    public static class VmNoCrashdumpSr extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmNoCrashdumpSr
         *
         * @param vm
         */
        public VmNoCrashdumpSr(String vm) {
            super("This VM does not have a crashdump SR specified.");
            this.vm = vm;
        }

    }

    /**
     * The operation could not be performed because the HA software is not installed on this host.
     */
    public static class HaNotInstalled extends XenAPIException {
        public final String host;

        /**
         * Create a new HaNotInstalled
         *
         * @param host
         */
        public HaNotInstalled(String host) {
            super("The operation could not be performed because the HA software is not installed on this host.");
            this.host = host;
        }

    }

    /**
     * You attempted an operation on a VM that was not in an appropriate power state at the time; for example, you attempted to start a VM that was already running.  The parameters returned are the VM's handle, and the expected and actual VM state at the time of the call.
     */
    public static class VmBadPowerState extends XenAPIException {
        public final String vm;
        public final String expected;
        public final String actual;

        /**
         * Create a new VmBadPowerState
         *
         * @param vm
         * @param expected
         * @param actual
         */
        public VmBadPowerState(String vm, String expected, String actual) {
            super("You attempted an operation on a VM that was not in an appropriate power state at the time; for example, you attempted to start a VM that was already running.  The parameters returned are the VM's handle, and the expected and actual VM state at the time of the call.");
            this.vm = vm;
            this.expected = expected;
            this.actual = actual;
        }

    }

    /**
     * The host could not join the liveset because the HA daemon could not access the heartbeat disk.
     */
    public static class HaHostCannotAccessStatefile extends XenAPIException {

        /**
         * Create a new HaHostCannotAccessStatefile
         */
        public HaHostCannotAccessStatefile() {
            super("The host could not join the liveset because the HA daemon could not access the heartbeat disk.");
        }

    }

    /**
     * VM didn't acknowledge the need to shutdown.
     */
    public static class VmFailedShutdownAcknowledgment extends XenAPIException {

        /**
         * Create a new VmFailedShutdownAcknowledgment
         */
        public VmFailedShutdownAcknowledgment() {
            super("VM didn't acknowledge the need to shutdown.");
        }

    }

    /**
     * Cannot perform operation as the host is running in emergency mode.
     */
    public static class HostInEmergencyMode extends XenAPIException {

        /**
         * Create a new HostInEmergencyMode
         */
        public HostInEmergencyMode() {
            super("Cannot perform operation as the host is running in emergency mode.");
        }

    }

    /**
     * The specified host is disabled and cannot be re-enabled until after it has rebooted.
     */
    public static class HostDisabledUntilReboot extends XenAPIException {
        public final String host;

        /**
         * Create a new HostDisabledUntilReboot
         *
         * @param host
         */
        public HostDisabledUntilReboot(String host) {
            super("The specified host is disabled and cannot be re-enabled until after it has rebooted.");
            this.host = host;
        }

    }

    /**
     * A device with the name given already exists on the selected VM
     */
    public static class DeviceAlreadyExists extends XenAPIException {
        public final String device;

        /**
         * Create a new DeviceAlreadyExists
         *
         * @param device
         */
        public DeviceAlreadyExists(String device) {
            super("A device with the name given already exists on the selected VM");
            this.device = device;
        }

    }

    /**
     * The PBD could not be plugged because the SR is in use by another host and is not marked as sharable.
     */
    public static class SrNotSharable extends XenAPIException {
        public final String sr;
        public final String host;

        /**
         * Create a new SrNotSharable
         *
         * @param sr
         * @param host
         */
        public SrNotSharable(String sr, String host) {
            super("The PBD could not be plugged because the SR is in use by another host and is not marked as sharable.");
            this.sr = sr;
            this.host = host;
        }

    }

    /**
     * The default SR reference does not point to a valid SR
     */
    public static class DefaultSrNotFound extends XenAPIException {
        public final String sr;

        /**
         * Create a new DefaultSrNotFound
         *
         * @param sr
         */
        public DefaultSrNotFound(String sr) {
            super("The default SR reference does not point to a valid SR");
            this.sr = sr;
        }

    }

    /**
     * Cannot downgrade license while in pool. Please disband the pool first, then downgrade licenses on hosts separately.
     */
    public static class LicenseCannotDowngradeWhileInPool extends XenAPIException {

        /**
         * Create a new LicenseCannotDowngradeWhileInPool
         */
        public LicenseCannotDowngradeWhileInPool() {
            super("Cannot downgrade license while in pool. Please disband the pool first, then downgrade licenses on hosts separately.");
        }

    }

    /**
     * The request was rejected because there are too many pending tasks on the server.
     */
    public static class TooManyPendingTasks extends XenAPIException {

        /**
         * Create a new TooManyPendingTasks
         */
        public TooManyPendingTasks() {
            super("The request was rejected because there are too many pending tasks on the server.");
        }

    }

    /**
     * An SR with that uuid already exists.
     */
    public static class SrUuidExists extends XenAPIException {
        public final String uuid;

        /**
         * Create a new SrUuidExists
         *
         * @param uuid
         */
        public SrUuidExists(String uuid) {
            super("An SR with that uuid already exists.");
            this.uuid = uuid;
        }

    }

    /**
     * This patch has already been applied
     */
    public static class PatchAlreadyApplied extends XenAPIException {
        public final String patch;

        /**
         * Create a new PatchAlreadyApplied
         *
         * @param patch
         */
        public PatchAlreadyApplied(String patch) {
            super("This patch has already been applied");
            this.patch = patch;
        }

    }

    /**
     * You attempted an operation that was explicitly blocked (see the blocked_operations field of the given object).
     */
    public static class OperationBlocked extends XenAPIException {
        public final String ref;
        public final String code;

        /**
         * Create a new OperationBlocked
         *
         * @param ref
         * @param code
         */
        public OperationBlocked(String ref, String code) {
            super("You attempted an operation that was explicitly blocked (see the blocked_operations field of the given object).");
            this.ref = ref;
            this.code = code;
        }

    }

    /**
     * The provision call can only be invoked on templates, not regular VMs.
     */
    public static class ProvisionOnlyAllowedOnTemplate extends XenAPIException {

        /**
         * Create a new ProvisionOnlyAllowedOnTemplate
         */
        public ProvisionOnlyAllowedOnTemplate() {
            super("The provision call can only be invoked on templates, not regular VMs.");
        }

    }

    /**
     * VM failed to shutdown before the timeout expired
     */
    public static class VmShutdownTimeout extends XenAPIException {
        public final String vm;
        public final String timeout;

        /**
         * Create a new VmShutdownTimeout
         *
         * @param vm
         * @param timeout
         */
        public VmShutdownTimeout(String vm, String timeout) {
            super("VM failed to shutdown before the timeout expired");
            this.vm = vm;
            this.timeout = timeout;
        }

    }

    /**
     * The network contains active PIFs and cannot be deleted.
     */
    public static class NetworkContainsPif extends XenAPIException {
        public final String pifs;

        /**
         * Create a new NetworkContainsPif
         *
         * @param pifs
         */
        public NetworkContainsPif(String pifs) {
            super("The network contains active PIFs and cannot be deleted.");
            this.pifs = pifs;
        }

    }

    /**
     * There was an error connecting to the host. the service contacted didn't reply properly.
     */
    public static class JoiningHostServiceFailed extends XenAPIException {

        /**
         * Create a new JoiningHostServiceFailed
         */
        public JoiningHostServiceFailed() {
            super("There was an error connecting to the host. the service contacted didn't reply properly.");
        }

    }

    /**
     * This VM has locked the DVD drive tray, so the disk cannot be ejected
     */
    public static class VbdTrayLocked extends XenAPIException {
        public final String vbd;

        /**
         * Create a new VbdTrayLocked
         *
         * @param vbd
         */
        public VbdTrayLocked(String vbd) {
            super("This VM has locked the DVD drive tray, so the disk cannot be ejected");
            this.vbd = vbd;
        }

    }

    /**
     * This operation cannot be performed because the specified VDI could not be found on the storage substrate
     */
    public static class VdiMissing extends XenAPIException {
        public final String sr;
        public final String vdi;

        /**
         * Create a new VdiMissing
         *
         * @param sr
         * @param vdi
         */
        public VdiMissing(String sr, String vdi) {
            super("This operation cannot be performed because the specified VDI could not be found on the storage substrate");
            this.sr = sr;
            this.vdi = vdi;
        }

    }

    /**
     * The uuid you supplied was invalid.
     */
    public static class UuidInvalid extends XenAPIException {
        public final String type;
        public final String uuid;

        /**
         * Create a new UuidInvalid
         *
         * @param type
         * @param uuid
         */
        public UuidInvalid(String type, String uuid) {
            super("The uuid you supplied was invalid.");
            this.type = type;
            this.uuid = uuid;
        }

    }

    /**
     * This operation is not allowed under your license.  Please contact your support representative.
     */
    public static class LicenceRestriction extends XenAPIException {

        /**
         * Create a new LicenceRestriction
         */
        public LicenceRestriction() {
            super("This operation is not allowed under your license.  Please contact your support representative.");
        }

    }

    /**
     * Network has active VIFs
     */
    public static class VifInUse extends XenAPIException {
        public final String network;
        public final String VIF;

        /**
         * Create a new VifInUse
         *
         * @param network
         * @param VIF
         */
        public VifInUse(String network, String VIF) {
            super("Network has active VIFs");
            this.network = network;
            this.VIF = VIF;
        }

    }

    /**
     * This command is only allowed on the OEM edition.
     */
    public static class OnlyAllowedOnOemEdition extends XenAPIException {
        public final String command;

        /**
         * Create a new OnlyAllowedOnOemEdition
         *
         * @param command
         */
        public OnlyAllowedOnOemEdition(String command) {
            super("This command is only allowed on the OEM edition.");
            this.command = command;
        }

    }

    /**
     * The operation cannot be performed on physical device
     */
    public static class VdiIsAPhysicalDevice extends XenAPIException {
        public final String vdi;

        /**
         * Create a new VdiIsAPhysicalDevice
         *
         * @param vdi
         */
        public VdiIsAPhysicalDevice(String vdi) {
            super("The operation cannot be performed on physical device");
            this.vdi = vdi;
        }

    }

    /**
     * There was an error processing your license.  Please contact your support representative.
     */
    public static class LicenseProcessingError extends XenAPIException {

        /**
         * Create a new LicenseProcessingError
         */
        public LicenseProcessingError() {
            super("There was an error processing your license.  Please contact your support representative.");
        }

    }

    /**
     * The request was asynchronously cancelled.
     */
    public static class TaskCancelled extends XenAPIException {
        public final String task;

        /**
         * Create a new TaskCancelled
         *
         * @param task
         */
        public TaskCancelled(String task) {
            super("The request was asynchronously cancelled.");
            this.task = task;
        }

    }

    /**
     * Host cannot rejoin pool because it should have fenced (it is not in the master's partition)
     */
    public static class HaShouldBeFenced extends XenAPIException {
        public final String host;

        /**
         * Create a new HaShouldBeFenced
         *
         * @param host
         */
        public HaShouldBeFenced(String host) {
            super("Host cannot rejoin pool because it should have fenced (it is not in the master's partition)");
            this.host = host;
        }

    }

    /**
     * You attempted an operation on a VM that was judged to be unsafe by the server. This can happen if the VM would run on a CPU that has a potentially incompatible set of feature flags to those the VM requires. If you want to override this warning then use the 'force' option.
     */
    public static class VmUnsafeBoot extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmUnsafeBoot
         *
         * @param vm
         */
        public VmUnsafeBoot(String vm) {
            super("You attempted an operation on a VM that was judged to be unsafe by the server. This can happen if the VM would run on a CPU that has a potentially incompatible set of feature flags to those the VM requires. If you want to override this warning then use the 'force' option.");
            this.vm = vm;
        }

    }

    /**
     * PIF has no IP configuration (mode curently set to 'none')
     */
    public static class PifHasNoNetworkConfiguration extends XenAPIException {

        /**
         * Create a new PifHasNoNetworkConfiguration
         */
        public PifHasNoNetworkConfiguration() {
            super("PIF has no IP configuration (mode curently set to 'none')");
        }

    }

    /**
     * The request was rejected because the server is too busy.
     */
    public static class TooBusy extends XenAPIException {

        /**
         * Create a new TooBusy
         */
        public TooBusy() {
            super("The request was rejected because the server is too busy.");
        }

    }

    /**
     * You attempted to set a value that is not supported by this implementation.  The fully-qualified field name and the value that you tried to set are returned.  Also returned is a developer-only diagnostic reason.
     */
    public static class ValueNotSupported extends XenAPIException {
        public final String field;
        public final String value;
        public final String reason;

        /**
         * Create a new ValueNotSupported
         *
         * @param field
         * @param value
         * @param reason
         */
        public ValueNotSupported(String field, String value, String reason) {
            super("You attempted to set a value that is not supported by this implementation.  The fully-qualified field name and the value that you tried to set are returned.  Also returned is a developer-only diagnostic reason.");
            this.field = field;
            this.value = value;
            this.reason = reason;
        }

    }

    /**
     * You gave an invalid session reference.  It may have been invalidated by a server restart, or timed out.  You should get a new session handle, using one of the session.login_ calls.  This error does not invalidate the current connection.  The handle parameter echoes the bad value given.
     */
    public static class SessionInvalid extends XenAPIException {
        public final String handle;

        /**
         * Create a new SessionInvalid
         *
         * @param handle
         */
        public SessionInvalid(String handle) {
            super("You gave an invalid session reference.  It may have been invalidated by a server restart, or timed out.  You should get a new session handle, using one of the session.login_ calls.  This error does not invalidate the current connection.  The handle parameter echoes the bad value given.");
            this.handle = handle;
        }

    }

    /**
     * This operation cannot be performed because the referenced network is not properly shared. The network must either be entirely virtual or must be physically present via a currently_attached PIF on every host.
     */
    public static class HaConstraintViolationNetworkNotShared extends XenAPIException {
        public final String network;

        /**
         * Create a new HaConstraintViolationNetworkNotShared
         *
         * @param network
         */
        public HaConstraintViolationNetworkNotShared(String network) {
            super("This operation cannot be performed because the referenced network is not properly shared. The network must either be entirely virtual or must be physically present via a currently_attached PIF on every host.");
            this.network = network;
        }

    }

    /**
     * HA could not be enabled on the Pool because a liveset could not be formed: check storage and network heartbeat paths.
     */
    public static class HaFailedToFormLiveset extends XenAPIException {

        /**
         * Create a new HaFailedToFormLiveset
         */
        public HaFailedToFormLiveset() {
            super("HA could not be enabled on the Pool because a liveset could not be formed: check storage and network heartbeat paths.");
        }

    }

    /**
     * You cannot bond interfaces across different hosts.
     */
    public static class PifCannotBondCrossHost extends XenAPIException {

        /**
         * Create a new PifCannotBondCrossHost
         */
        public PifCannotBondCrossHost() {
            super("You cannot bond interfaces across different hosts.");
        }

    }

    /**
     * This operation cannot be performed because it would invalidate VM failover planning such that the system would be unable to guarantee to restart protected VMs after a Host failure.
     */
    public static class HaOperationWouldBreakFailoverPlan extends XenAPIException {

        /**
         * Create a new HaOperationWouldBreakFailoverPlan
         */
        public HaOperationWouldBreakFailoverPlan() {
            super("This operation cannot be performed because it would invalidate VM failover planning such that the system would be unable to guarantee to restart protected VMs after a Host failure.");
        }

    }

    /**
     * The requested update could not be found.  This can occur when you designate a new master or xe patch-clean.  Please upload the update again
     */
    public static class CannotFindPatch extends XenAPIException {

        /**
         * Create a new CannotFindPatch
         */
        public CannotFindPatch() {
            super("The requested update could not be found.  This can occur when you designate a new master or xe patch-clean.  Please upload the update again");
        }

    }

    /**
     * You attempted an operation which would have resulted in duplicate keys in the database.
     */
    public static class DbUniquenessConstraintViolation extends XenAPIException {
        public final String table;
        public final String field;
        public final String value;

        /**
         * Create a new DbUniquenessConstraintViolation
         *
         * @param table
         * @param field
         * @param value
         */
        public DbUniquenessConstraintViolation(String table, String field, String value) {
            super("You attempted an operation which would have resulted in duplicate keys in the database.");
            this.table = table;
            this.field = field;
            this.value = value;
        }

    }

    /**
     * The requested update could to be obtained from the master.
     */
    public static class CannotFetchPatch extends XenAPIException {
        public final String uuid;

        /**
         * Create a new CannotFetchPatch
         *
         * @param uuid
         */
        public CannotFetchPatch(String uuid) {
            super("The requested update could to be obtained from the master.");
            this.uuid = uuid;
        }

    }

    /**
     * You attempted to run a VM on a host which doesn't have a PIF on a Network needed by the VM. The VM has at least one VIF attached to the Network.
     */
    public static class VmRequiresNetwork extends XenAPIException {
        public final String vm;
        public final String network;

        /**
         * Create a new VmRequiresNetwork
         *
         * @param vm
         * @param network
         */
        public VmRequiresNetwork(String vm, String network) {
            super("You attempted to run a VM on a host which doesn't have a PIF on a Network needed by the VM. The VM has at least one VIF attached to the Network.");
            this.vm = vm;
            this.network = network;
        }

    }

    /**
     * Operation could not be performed because the drive is not empty
     */
    public static class VbdNotEmpty extends XenAPIException {
        public final String vbd;

        /**
         * Create a new VbdNotEmpty
         *
         * @param vbd
         */
        public VbdNotEmpty(String vbd) {
            super("Operation could not be performed because the drive is not empty");
            this.vbd = vbd;
        }

    }

    /**
     * Not enough host memory is available to perform this operation
     */
    public static class HostNotEnoughFreeMemory extends XenAPIException {

        /**
         * Create a new HostNotEnoughFreeMemory
         */
        public HostNotEnoughFreeMemory() {
            super("Not enough host memory is available to perform this operation");
        }

    }

    /**
     * An error occurred during the migration process.
     */
    public static class VmMigrateFailed extends XenAPIException {
        public final String vm;
        public final String source;
        public final String destination;
        public final String msg;

        /**
         * Create a new VmMigrateFailed
         *
         * @param vm
         * @param source
         * @param destination
         * @param msg
         */
        public VmMigrateFailed(String vm, String source, String destination, String msg) {
            super("An error occurred during the migration process.");
            this.vm = vm;
            this.source = source;
            this.destination = destination;
            this.msg = msg;
        }

    }

    /**
     * The operation could not be performed because the VBD was not connected to the VM.
     */
    public static class DeviceNotAttached extends XenAPIException {
        public final String VBD;

        /**
         * Create a new DeviceNotAttached
         *
         * @param VBD
         */
        public DeviceNotAttached(String VBD) {
            super("The operation could not be performed because the VBD was not connected to the VM.");
            this.VBD = VBD;
        }

    }

    /**
     * The specified host is disabled.
     */
    public static class HostDisabled extends XenAPIException {
        public final String host;

        /**
         * Create a new HostDisabled
         *
         * @param host
         */
        public HostDisabled(String host) {
            super("The specified host is disabled.");
            this.host = host;
        }

    }

    /**
     * The SR backend does not support the operation (check the SR's allowed operations)
     */
    public static class SrOperationNotSupported extends XenAPIException {
        public final String sr;

        /**
         * Create a new SrOperationNotSupported
         *
         * @param sr
         */
        public SrOperationNotSupported(String sr) {
            super("The SR backend does not support the operation (check the SR's allowed operations)");
            this.sr = sr;
        }

    }

    /**
     * You must use tar output to retrieve system status from an OEM host.
     */
    public static class SystemStatusMustUseTarOnOem extends XenAPIException {

        /**
         * Create a new SystemStatusMustUseTarOnOem
         */
        public SystemStatusMustUseTarOnOem() {
            super("You must use tar output to retrieve system status from an OEM host.");
        }

    }

    /**
     * The host joining the pool cannot contain any shared storage.
     */
    public static class JoiningHostCannotContainSharedSrs extends XenAPIException {

        /**
         * Create a new JoiningHostCannotContainSharedSrs
         */
        public JoiningHostCannotContainSharedSrs() {
            super("The host joining the pool cannot contain any shared storage.");
        }

    }

    /**
     * You need at least 1 VCPU to start a VM
     */
    public static class VmNoVcpus extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmNoVcpus
         *
         * @param vm
         */
        public VmNoVcpus(String vm) {
            super("You need at least 1 VCPU to start a VM");
            this.vm = vm;
        }

    }

    /**
     * The uploaded patch file is invalid.  See attached log for more details.
     */
    public static class InvalidPatchWithLog extends XenAPIException {
        public final String log;

        /**
         * Create a new InvalidPatchWithLog
         *
         * @param log
         */
        public InvalidPatchWithLog(String log) {
            super("The uploaded patch file is invalid.  See attached log for more details.");
            this.log = log;
        }

    }

    /**
     * The SR operation cannot be performed because a device underlying the SR is in use by the host.
     */
    public static class SrDeviceInUse extends XenAPIException {

        /**
         * Create a new SrDeviceInUse
         */
        public SrDeviceInUse() {
            super("The SR operation cannot be performed because a device underlying the SR is in use by the host.");
        }

    }

    /**
     * The operation could not be performed while the host is still armed; it must be disarmed first
     */
    public static class HaHostIsArmed extends XenAPIException {
        public final String host;

        /**
         * Create a new HaHostIsArmed
         *
         * @param host
         */
        public HaHostIsArmed(String host) {
            super("The operation could not be performed while the host is still armed; it must be disarmed first");
            this.host = host;
        }

    }

    /**
     * Your license has expired.  Please contact your support representative.
     */
    public static class LicenseExpired extends XenAPIException {

        /**
         * Create a new LicenseExpired
         */
        public LicenseExpired() {
            super("Your license has expired.  Please contact your support representative.");
        }

    }

    /**
     * The credentials given by the user are incorrect, so access has been denied, and you have not been issued a session handle.
     */
    public static class SessionAuthenticationFailed extends XenAPIException {

        /**
         * Create a new SessionAuthenticationFailed
         */
        public SessionAuthenticationFailed() {
            super("The credentials given by the user are incorrect, so access has been denied, and you have not been issued a session handle.");
        }

    }

    /**
     * You tried to create a VLAN on top of another VLAN - use the underlying physical PIF/bond instead
     */
    public static class PifIsVlan extends XenAPIException {
        public final String PIF;

        /**
         * Create a new PifIsVlan
         *
         * @param PIF
         */
        public PifIsVlan(String PIF) {
            super("You tried to create a VLAN on top of another VLAN - use the underlying physical PIF/bond instead");
            this.PIF = PIF;
        }

    }

    /**
     * The host joining the pool cannot already be a master of another pool.
     */
    public static class JoiningHostCannotBeMasterOfOtherHosts extends XenAPIException {

        /**
         * Create a new JoiningHostCannotBeMasterOfOtherHosts
         */
        public JoiningHostCannotBeMasterOfOtherHosts() {
            super("The host joining the pool cannot already be a master of another pool.");
        }

    }

    /**
     * This host can not be forgotten because there are some user VMs still running
     */
    public static class HostHasResidentVms extends XenAPIException {
        public final String host;

        /**
         * Create a new HostHasResidentVms
         *
         * @param host
         */
        public HostHasResidentVms(String host) {
            super("This host can not be forgotten because there are some user VMs still running");
            this.host = host;
        }

    }

    /**
     * The operation you requested cannot be performed because the specified PIF is the management interface.
     */
    public static class PifIsManagementInterface extends XenAPIException {
        public final String PIF;

        /**
         * Create a new PifIsManagementInterface
         *
         * @param PIF
         */
        public PifIsManagementInterface(String PIF) {
            super("The operation you requested cannot be performed because the specified PIF is the management interface.");
            this.PIF = PIF;
        }

    }

    /**
     * The MAC address specified is not valid.
     */
    public static class MacInvalid extends XenAPIException {
        public final String MAC;

        /**
         * Create a new MacInvalid
         *
         * @param MAC
         */
        public MacInvalid(String MAC) {
            super("The MAC address specified is not valid.");
            this.MAC = MAC;
        }

    }

    /**
     * Operation could not be performed because the drive is empty
     */
    public static class VbdIsEmpty extends XenAPIException {
        public final String vbd;

        /**
         * Create a new VbdIsEmpty
         *
         * @param vbd
         */
        public VbdIsEmpty(String vbd) {
            super("Operation could not be performed because the drive is empty");
            this.vbd = vbd;
        }

    }

    /**
     * This operation could not be performed because the state partition could not be found
     */
    public static class CannotFindStatePartition extends XenAPIException {

        /**
         * Create a new CannotFindStatePartition
         */
        public CannotFindStatePartition() {
            super("This operation could not be performed because the state partition could not be found");
        }

    }

    /**
     * This pool is not in emergency mode.
     */
    public static class NotInEmergencyMode extends XenAPIException {

        /**
         * Create a new NotInEmergencyMode
         */
        public NotInEmergencyMode() {
            super("This pool is not in emergency mode.");
        }

    }

    /**
     * The patch precheck stage failed: the server is of an incorrect version.
     */
    public static class PatchPrecheckFailedWrongServerVersion extends XenAPIException {
        public final String patch;
        public final String foundVersion;
        public final String requiredVersion;

        /**
         * Create a new PatchPrecheckFailedWrongServerVersion
         *
         * @param patch
         * @param foundVersion
         * @param requiredVersion
         */
        public PatchPrecheckFailedWrongServerVersion(String patch, String foundVersion, String requiredVersion) {
            super("The patch precheck stage failed: the server is of an incorrect version.");
            this.patch = patch;
            this.foundVersion = foundVersion;
            this.requiredVersion = requiredVersion;
        }

    }

    /**
     * You tried to create a PIF, but the network you tried to attach it to is already attached to some other PIF, and so the creation failed.
     */
    public static class NetworkAlreadyConnected extends XenAPIException {
        public final String network;
        public final String connectedPIF;

        /**
         * Create a new NetworkAlreadyConnected
         *
         * @param network
         * @param connectedPIF
         */
        public NetworkAlreadyConnected(String network, String connectedPIF) {
            super("You tried to create a PIF, but the network you tried to attach it to is already attached to some other PIF, and so the creation failed.");
            this.network = network;
            this.connectedPIF = connectedPIF;
        }

    }

    /**
     * This operation cannot be performed because the specified VDI is of an incompatible type (eg: an HA statefile cannot be attached to a guest)
     */
    public static class VdiIncompatibleType extends XenAPIException {
        public final String vdi;
        public final String type;

        /**
         * Create a new VdiIncompatibleType
         *
         * @param vdi
         * @param type
         */
        public VdiIncompatibleType(String vdi, String type) {
            super("This operation cannot be performed because the specified VDI is of an incompatible type (eg: an HA statefile cannot be attached to a guest)");
            this.vdi = vdi;
            this.type = type;
        }

    }

    /**
     * The VM could not be imported; is the file corrupt?
     */
    public static class ImportError extends XenAPIException {
        public final String msg;

        /**
         * Create a new ImportError
         *
         * @param msg
         */
        public ImportError(String msg) {
            super("The VM could not be imported; is the file corrupt?");
            this.msg = msg;
        }

    }

    /**
     * The SR could not be connected because the driver was not recognised.
     */
    public static class SrUnknownDriver extends XenAPIException {
        public final String driver;

        /**
         * Create a new SrUnknownDriver
         *
         * @param driver
         */
        public SrUnknownDriver(String driver) {
            super("The SR could not be connected because the driver was not recognised.");
            this.driver = driver;
        }

    }

    /**
     * There was a failure communicating with the plugin.
     */
    public static class XenapiPluginFailure extends XenAPIException {
        public final String status;
        public final String stdout;
        public final String stderr;

        /**
         * Create a new XenapiPluginFailure
         *
         * @param status
         * @param stdout
         * @param stderr
         */
        public XenapiPluginFailure(String status, String stdout, String stderr) {
            super("There was a failure communicating with the plugin.");
            this.status = status;
            this.stdout = stdout;
            this.stderr = stderr;
        }

    }

    /**
     * The MAC address specified still exists on this host.
     */
    public static class MacStillExists extends XenAPIException {
        public final String MAC;

        /**
         * Create a new MacStillExists
         *
         * @param MAC
         */
        public MacStillExists(String MAC) {
            super("The MAC address specified still exists on this host.");
            this.MAC = MAC;
        }

    }

    /**
     * This operation cannot be completed as the host is in use by (at least) the object of type and ref echoed below.
     */
    public static class HostInUse extends XenAPIException {
        public final String host;
        public final String type;
        public final String ref;

        /**
         * Create a new HostInUse
         *
         * @param host
         * @param type
         * @param ref
         */
        public HostInUse(String host, String type, String ref) {
            super("This operation cannot be completed as the host is in use by (at least) the object of type and ref echoed below.");
            this.host = host;
            this.type = type;
            this.ref = ref;
        }

    }

    /**
     * HA can only be enabled for 2 hosts or more. Note that 2 hosts requires a pre-configured quorum tiebreak script.
     */
    public static class HaTooFewHosts extends XenAPIException {

        /**
         * Create a new HaTooFewHosts
         */
        public HaTooFewHosts() {
            super("HA can only be enabled for 2 hosts or more. Note that 2 hosts requires a pre-configured quorum tiebreak script.");
        }

    }

    /**
     * The specified patch is applied and cannot be destroyed.
     */
    public static class PatchIsApplied extends XenAPIException {

        /**
         * Create a new PatchIsApplied
         */
        public PatchIsApplied() {
            super("The specified patch is applied and cannot be destroyed.");
        }

    }

    /**
     * The SR is still connected to a host via a PBD. It cannot be destroyed.
     */
    public static class SrHasPbd extends XenAPIException {
        public final String sr;

        /**
         * Create a new SrHasPbd
         *
         * @param sr
         */
        public SrHasPbd(String sr) {
            super("The SR is still connected to a host via a PBD. It cannot be destroyed.");
            this.sr = sr;
        }

    }

    /**
     * The host is still booting.
     */
    public static class HostStillBooting extends XenAPIException {

        /**
         * Create a new HostStillBooting
         */
        public HostStillBooting() {
            super("The host is still booting.");
        }

    }

    /**
     * The specified object no longer exists.
     */
    public static class ObjectNolongerExists extends XenAPIException {

        /**
         * Create a new ObjectNolongerExists
         */
        public ObjectNolongerExists() {
            super("The specified object no longer exists.");
        }

    }

    /**
     * The hosts in this pool are not homogeneous.
     */
    public static class HostsNotHomogeneous extends XenAPIException {
        public final String reason;

        /**
         * Create a new HostsNotHomogeneous
         *
         * @param reason
         */
        public HostsNotHomogeneous(String reason) {
            super("The hosts in this pool are not homogeneous.");
            this.reason = reason;
        }

    }

    /**
     * You tried to create a PIF, but it already exists.
     */
    public static class PifVlanExists extends XenAPIException {
        public final String PIF;

        /**
         * Create a new PifVlanExists
         *
         * @param PIF
         */
        public PifVlanExists(String PIF) {
            super("You tried to create a PIF, but it already exists.");
            this.PIF = PIF;
        }

    }

    /**
     * There is not enough space to upload the update
     */
    public static class OutOfSpace extends XenAPIException {
        public final String location;

        /**
         * Create a new OutOfSpace
         *
         * @param location
         */
        public OutOfSpace(String location) {
            super("There is not enough space to upload the update");
            this.location = location;
        }

    }

    /**
     * The uploaded patch file already exists
     */
    public static class PatchAlreadyExists extends XenAPIException {
        public final String uuid;

        /**
         * Create a new PatchAlreadyExists
         *
         * @param uuid
         */
        public PatchAlreadyExists(String uuid) {
            super("The uploaded patch file already exists");
            this.uuid = uuid;
        }

    }

    /**
     * The specified VM has too little memory to be started.
     */
    public static class VmMemorySizeTooLow extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmMemorySizeTooLow
         *
         * @param vm
         */
        public VmMemorySizeTooLow(String vm) {
            super("The specified VM has too little memory to be started.");
            this.vm = vm;
        }

    }

    /**
     * This operation cannot be performed because the host is not disabled.
     */
    public static class HostNotDisabled extends XenAPIException {

        /**
         * Create a new HostNotDisabled
         */
        public HostNotDisabled() {
            super("This operation cannot be performed because the host is not disabled.");
        }

    }

    /**
     * The value specified is of the wrong type
     */
    public static class FieldTypeError extends XenAPIException {
        public final String field;

        /**
         * Create a new FieldTypeError
         *
         * @param field
         */
        public FieldTypeError(String field) {
            super("The value specified is of the wrong type");
            this.field = field;
        }

    }

    /**
     * The management interface on a slave cannot be disabled because the slave would enter emergency mode.
     */
    public static class SlaveRequiresManagementInterface extends XenAPIException {

        /**
         * Create a new SlaveRequiresManagementInterface
         */
        public SlaveRequiresManagementInterface() {
            super("The management interface on a slave cannot be disabled because the slave would enter emergency mode.");
        }

    }

    /**
     * The operation attempted is not valid for a template VM
     */
    public static class VmIsTemplate extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmIsTemplate
         *
         * @param vm
         */
        public VmIsTemplate(String vm) {
            super("The operation attempted is not valid for a template VM");
            this.vm = vm;
        }

    }

    /**
     * This operation cannot be performed because the specified VM is protected by xHA
     */
    public static class VmIsProtected extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmIsProtected
         *
         * @param vm
         */
        public VmIsProtected(String vm) {
            super("This operation cannot be performed because the specified VM is protected by xHA");
            this.vm = vm;
        }

    }

    /**
     * VM cannot be started because it requires a VDI which cannot be attached
     */
    public static class VmRequiresVdi extends XenAPIException {
        public final String vm;
        public final String vdi;

        /**
         * Create a new VmRequiresVdi
         *
         * @param vm
         * @param vdi
         */
        public VmRequiresVdi(String vm, String vdi) {
            super("VM cannot be started because it requires a VDI which cannot be attached");
            this.vm = vm;
            this.vdi = vdi;
        }

    }

    /**
     * Read/write CDs are not supported
     */
    public static class VbdCdsMustBeReadonly extends XenAPIException {

        /**
         * Create a new VbdCdsMustBeReadonly
         */
        public VbdCdsMustBeReadonly() {
            super("Read/write CDs are not supported");
        }

    }

    /**
     * The host joining the pool cannot have any VMs with active tasks.
     */
    public static class JoiningHostCannotHaveVmsWithCurrentOperations extends XenAPIException {

        /**
         * Create a new JoiningHostCannotHaveVmsWithCurrentOperations
         */
        public JoiningHostCannotHaveVmsWithCurrentOperations() {
            super("The host joining the pool cannot have any VMs with active tasks.");
        }

    }

    /**
     * An HA statefile could not be created, perhaps because no SR with the appropriate capability was found.
     */
    public static class CannotCreateStateFile extends XenAPIException {

        /**
         * Create a new CannotCreateStateFile
         */
        public CannotCreateStateFile() {
            super("An HA statefile could not be created, perhaps because no SR with the appropriate capability was found.");
        }

    }

    /**
     * You tried to call a method with the incorrect number of parameters.  The fully-qualified method name that you used, and the number of received and expected parameters are returned.
     */
    public static class MessageParameterCountMismatch extends XenAPIException {
        public final String method;
        public final String expected;
        public final String received;

        /**
         * Create a new MessageParameterCountMismatch
         *
         * @param method
         * @param expected
         * @param received
         */
        public MessageParameterCountMismatch(String method, String expected, String received) {
            super("You tried to call a method with the incorrect number of parameters.  The fully-qualified method name that you used, and the number of received and expected parameters are returned.");
            this.method = method;
            this.expected = expected;
            this.received = received;
        }

    }

    /**
     * The restore could not be performed because this backup has been created by a different (incompatible) product verion
     */
    public static class RestoreIncompatibleVersion extends XenAPIException {

        /**
         * Create a new RestoreIncompatibleVersion
         */
        public RestoreIncompatibleVersion() {
            super("The restore could not be performed because this backup has been created by a different (incompatible) product verion");
        }

    }

    /**
     * The VM rejected the attempt to detach the device.
     */
    public static class DeviceDetachRejected extends XenAPIException {
        public final String type;
        public final String ref;
        public final String msg;

        /**
         * Create a new DeviceDetachRejected
         *
         * @param type
         * @param ref
         * @param msg
         */
        public DeviceDetachRejected(String type, String ref, String msg) {
            super("The VM rejected the attempt to detach the device.");
            this.type = type;
            this.ref = ref;
            this.msg = msg;
        }

    }

    /**
     * The host joining the pool cannot have any running or suspended VMs.
     */
    public static class JoiningHostCannotHaveRunningOrSuspendedVms extends XenAPIException {

        /**
         * Create a new JoiningHostCannotHaveRunningOrSuspendedVms
         */
        public JoiningHostCannotHaveRunningOrSuspendedVms() {
            super("The host joining the pool cannot have any running or suspended VMs.");
        }

    }

    /**
     * The patch precheck stage failed: prerequisite patches are missing.
     */
    public static class PatchPrecheckFailedPrerequisiteMissing extends XenAPIException {
        public final String patch;
        public final String prerequisitePatchUuidList;

        /**
         * Create a new PatchPrecheckFailedPrerequisiteMissing
         *
         * @param patch
         * @param prerequisitePatchUuidList
         */
        public PatchPrecheckFailedPrerequisiteMissing(String patch, String prerequisitePatchUuidList) {
            super("The patch precheck stage failed: prerequisite patches are missing.");
            this.patch = patch;
            this.prerequisitePatchUuidList = prerequisitePatchUuidList;
        }

    }

    /**
     * Cannot forward messages because the host cannot be contacted.  The host may be switched off or there may be network connectivity problems.
     */
    public static class CannotContactHost extends XenAPIException {
        public final String host;

        /**
         * Create a new CannotContactHost
         *
         * @param host
         */
        public CannotContactHost(String host) {
            super("Cannot forward messages because the host cannot be contacted.  The host may be switched off or there may be network connectivity problems.");
            this.host = host;
        }

    }

    /**
     * You attempted an operation on a VM which requires PV drivers to be installed but the drivers were not detected.
     */
    public static class VmMissingPvDrivers extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmMissingPvDrivers
         *
         * @param vm
         */
        public VmMissingPvDrivers(String vm) {
            super("You attempted an operation on a VM which requires PV drivers to be installed but the drivers were not detected.");
            this.vm = vm;
        }

    }

    /**
     * This operation cannot be performed because the specified VDI could not be found in the specified SR
     */
    public static class VdiLocationMissing extends XenAPIException {
        public final String sr;
        public final String location;

        /**
         * Create a new VdiLocationMissing
         *
         * @param sr
         * @param location
         */
        public VdiLocationMissing(String sr, String location) {
            super("This operation cannot be performed because the specified VDI could not be found in the specified SR");
            this.sr = sr;
            this.location = location;
        }

    }

    /**
     * Operation cannot proceed while a VLAN exists on this interface.
     */
    public static class PifVlanStillExists extends XenAPIException {
        public final String PIF;

        /**
         * Create a new PifVlanStillExists
         *
         * @param PIF
         */
        public PifVlanStillExists(String PIF) {
            super("Operation cannot proceed while a VLAN exists on this interface.");
            this.PIF = PIF;
        }

    }

    /**
     * The network contains active VIFs and cannot be deleted.
     */
    public static class NetworkContainsVif extends XenAPIException {
        public final String vifs;

        /**
         * Create a new NetworkContainsVif
         *
         * @param vifs
         */
        public NetworkContainsVif(String vifs) {
            super("The network contains active VIFs and cannot be deleted.");
            this.vifs = vifs;
        }

    }

    /**
     * The value given is invalid
     */
    public static class InvalidValue extends XenAPIException {
        public final String field;
        public final String value;

        /**
         * Create a new InvalidValue
         *
         * @param field
         * @param value
         */
        public InvalidValue(String field, String value) {
            super("The value given is invalid");
            this.field = field;
            this.value = value;
        }

    }

    /**
     * The requested plugin could not be found.
     */
    public static class XenapiMissingPlugin extends XenAPIException {
        public final String name;

        /**
         * Create a new XenapiMissingPlugin
         *
         * @param name
         */
        public XenapiMissingPlugin(String name) {
            super("The requested plugin could not be found.");
            this.name = name;
        }

    }

    /**
     * The restore could not be performed because the host's current management interface is not in the backup. The interfaces mentioned in the backup are:
     */
    public static class RestoreTargetMgmtIfNotInBackup extends XenAPIException {

        /**
         * Create a new RestoreTargetMgmtIfNotInBackup
         */
        public RestoreTargetMgmtIfNotInBackup() {
            super("The restore could not be performed because the host's current management interface is not in the backup. The interfaces mentioned in the backup are:");
        }

    }

    /**
     * There was an error connecting to the host while joining it in the pool.
     */
    public static class JoiningHostConnectionFailed extends XenAPIException {

        /**
         * Create a new JoiningHostConnectionFailed
         */
        public JoiningHostConnectionFailed() {
            super("There was an error connecting to the host while joining it in the pool.");
        }

    }

    /**
     * You tried to call a method that does not exist.  The method name that you used is echoed.
     */
    public static class MessageMethodUnknown extends XenAPIException {
        public final String method;

        /**
         * Create a new MessageMethodUnknown
         *
         * @param method
         */
        public MessageMethodUnknown(String method) {
            super("You tried to call a method that does not exist.  The method name that you used is echoed.");
            this.method = method;
        }

    }

    /**
     * You cannot delete the specified default template.
     */
    public static class VmCannotDeleteDefaultTemplate extends XenAPIException {
        public final String vm;

        /**
         * Create a new VmCannotDeleteDefaultTemplate
         *
         * @param vm
         */
        public VmCannotDeleteDefaultTemplate(String vm) {
            super("You cannot delete the specified default template.");
            this.vm = vm;
        }

    }

    /**
     * This operation can only be performed on CD VDIs (iso files or CDROM drives)
     */
    public static class VdiIsNotIso extends XenAPIException {
        public final String vdi;
        public final String type;

        /**
         * Create a new VdiIsNotIso
         *
         * @param vdi
         * @param type
         */
        public VdiIsNotIso(String vdi, String type) {
            super("This operation can only be performed on CD VDIs (iso files or CDROM drives)");
            this.vdi = vdi;
            this.type = type;
        }

    }

    /**
     * The server failed to handle your request, due to an internal error.  The given message may give details useful for debugging the problem.
     */
    public static class InternalError extends XenAPIException {
        public final String message;

        /**
         * Create a new InternalError
         *
         * @param message
         */
        public InternalError(String message) {
            super("The server failed to handle your request, due to an internal error.  The given message may give details useful for debugging the problem.");
            this.message = message;
        }

    }

    /**
     * This command is not allowed on the OEM edition.
     */
    public static class NotAllowedOnOemEdition extends XenAPIException {
        public final String command;

        /**
         * Create a new NotAllowedOnOemEdition
         *
         * @param command
         */
        public NotAllowedOnOemEdition(String command) {
            super("This command is not allowed on the OEM edition.");
            this.command = command;
        }

    }

    /**
     * The restore could not be performed because the restore script failed.  Is the file corrupt?
     */
    public static class RestoreScriptFailed extends XenAPIException {
        public final String log;

        /**
         * Create a new RestoreScriptFailed
         *
         * @param log
         */
        public RestoreScriptFailed(String log) {
            super("The restore could not be performed because the restore script failed.  Is the file corrupt?");
            this.log = log;
        }

    }

    /**
     * XHA cannot be enabled because this host's license does not allow it
     */
    public static class LicenseDoesNotSupportXha extends XenAPIException {

        /**
         * Create a new LicenseDoesNotSupportXha
         */
        public LicenseDoesNotSupportXha() {
            super("XHA cannot be enabled because this host's license does not allow it");
        }

    }

    /**
     * Media could not be ejected because it is not removable
     */
    public static class VbdNotRemovableMedia extends XenAPIException {
        public final String vbd;

        /**
         * Create a new VbdNotRemovableMedia
         *
         * @param vbd
         */
        public VbdNotRemovableMedia(String vbd) {
            super("Media could not be ejected because it is not removable");
            this.vbd = vbd;
        }

    }

    /**
     * The device is not currently attached
     */
    public static class DeviceAlreadyDetached extends XenAPIException {
        public final String device;

        /**
         * Create a new DeviceAlreadyDetached
         *
         * @param device
         */
        public DeviceAlreadyDetached(String device) {
            super("The device is not currently attached");
            this.device = device;
        }

    }

    /**
     * A VDI with the specified location already exists within the SR
     */
    public static class LocationNotUnique extends XenAPIException {
        public final String SR;
        public final String location;

        /**
         * Create a new LocationNotUnique
         *
         * @param SR
         * @param location
         */
        public LocationNotUnique(String SR, String location) {
            super("A VDI with the specified location already exists within the SR");
            this.SR = SR;
            this.location = location;
        }

    }

    /**
     * The function is not implemented
     */
    public static class NotImplemented extends XenAPIException {
        public final String function;

        /**
         * Create a new NotImplemented
         *
         * @param function
         */
        public NotImplemented(String function) {
            super("The function is not implemented");
            this.function = function;
        }

    }

    /**
     * Cannot plug VIF
     */
    public static class CannotPlugVif extends XenAPIException {
        public final String VIF;

        /**
         * Create a new CannotPlugVif
         *
         * @param VIF
         */
        public CannotPlugVif(String VIF) {
            super("Cannot plug VIF");
            this.VIF = VIF;
        }

    }

    /**
     * The backup could not be performed because the backup script failed.
     */
    public static class BackupScriptFailed extends XenAPIException {
        public final String log;

        /**
         * Create a new BackupScriptFailed
         *
         * @param log
         */
        public BackupScriptFailed(String log) {
            super("The backup could not be performed because the backup script failed.");
            this.log = log;
        }

    }

    /**
     * You attempted an operation that was not allowed.
     */
    public static class OperationNotAllowed extends XenAPIException {
        public final String reason;

        /**
         * Create a new OperationNotAllowed
         *
         * @param reason
         */
        public OperationNotAllowed(String reason) {
            super("You attempted an operation that was not allowed.");
            this.reason = reason;
        }

    }

    /**
     * Cannot find a plan for placement of VMs as there are no other hosts available.
     */
    public static class HaNoPlan extends XenAPIException {

        /**
         * Create a new HaNoPlan
         */
        public HaNoPlan() {
            super("Cannot find a plan for placement of VMs as there are no other hosts available.");
        }

    }

    /**
     * A timeout happened while attempting to detach a device from a VM.
     */
    public static class DeviceDetachTimeout extends XenAPIException {
        public final String type;
        public final String ref;

        /**
         * Create a new DeviceDetachTimeout
         *
         * @param type
         * @param ref
         */
        public DeviceDetachTimeout(String type, String ref) {
            super("A timeout happened while attempting to detach a device from a VM.");
            this.type = type;
            this.ref = ref;
        }

    }

    /**
     * The specified VM has a duplicate VBD device and cannot be started.
     */
    public static class VmDuplicateVbdDevice extends XenAPIException {
        public final String vm;
        public final String vbd;
        public final String device;

        /**
         * Create a new VmDuplicateVbdDevice
         *
         * @param vm
         * @param vbd
         * @param device
         */
        public VmDuplicateVbdDevice(String vm, String vbd, String device) {
            super("The specified VM has a duplicate VBD device and cannot be started.");
            this.vm = vm;
            this.vbd = vbd;
            this.device = device;
        }

    }

    /**
     * Some events have been lost from the queue and cannot be retrieved.
     */
    public static class EventsLost extends XenAPIException {

        /**
         * Create a new EventsLost
         */
        public EventsLost() {
            super("Some events have been lost from the queue and cannot be retrieved.");
        }

    }

    /**
     * There was an SR backend failure.
     */
    public static class SrBackendFailure extends XenAPIException {
        public final String status;
        public final String stdout;
        public final String stderr;

        /**
         * Create a new SrBackendFailure
         *
         * @param status
         * @param stdout
         * @param stderr
         */
        public SrBackendFailure(String status, String stdout, String stderr) {
            super("There was an SR backend failure.");
            this.status = status;
            this.stdout = stdout;
            this.stderr = stderr;
        }

    }

    /**
     * You attempted an operation on a VM which requires a more recent version of the PV drivers. Please upgrade your PV drivers.
     */
    public static class VmOldPvDrivers extends XenAPIException {
        public final String vm;
        public final String major;
        public final String minor;

        /**
         * Create a new VmOldPvDrivers
         *
         * @param vm
         * @param major
         * @param minor
         */
        public VmOldPvDrivers(String vm, String major, String minor) {
            super("You attempted an operation on a VM which requires a more recent version of the PV drivers. Please upgrade your PV drivers.");
            this.vm = vm;
            this.major = major;
            this.minor = minor;
        }

    }

    /**
     * The operation you requested cannot be performed because the specified PIF does not allow unplug.
     */
    public static class PifDoesNotAllowUnplug extends XenAPIException {
        public final String PIF;

        /**
         * Create a new PifDoesNotAllowUnplug
         *
         * @param PIF
         */
        public PifDoesNotAllowUnplug(String PIF) {
            super("The operation you requested cannot be performed because the specified PIF does not allow unplug.");
            this.PIF = PIF;
        }

    }

    /**
     * The system rejected the password change request; perhaps the new password was too short?
     */
    public static class ChangePasswordRejected extends XenAPIException {
        public final String msg;

        /**
         * Create a new ChangePasswordRejected
         *
         * @param msg
         */
        public ChangePasswordRejected(String msg) {
            super("The system rejected the password change request; perhaps the new password was too short?");
            this.msg = msg;
        }

    }

    /**
     * Another operation involving the object is currently in progress
     */
    public static class OtherOperationInProgress extends XenAPIException {
        public final String clazz;
        public final String object;

        /**
         * Create a new OtherOperationInProgress
         *
         * @param clazz
         * @param object
         */
        public OtherOperationInProgress(String clazz, String object) {
            super("Another operation involving the object is currently in progress");
            this.clazz = clazz;
            this.object = object;
        }

    }

    /**
     * You attempted an operation which involves a host which could not be contacted.
     */
    public static class HostOffline extends XenAPIException {
        public final String host;

        /**
         * Create a new HostOffline
         *
         * @param host
         */
        public HostOffline(String host) {
            super("You attempted an operation which involves a host which could not be contacted.");
            this.host = host;
        }

    }

    /**
     * The host failed to acquire an IP address on its management interface and therefore cannot contact the master.
     */
    public static class HostHasNoManagementIp extends XenAPIException {

        /**
         * Create a new HostHasNoManagementIp
         */
        public HostHasNoManagementIp() {
            super("The host failed to acquire an IP address on its management interface and therefore cannot contact the master.");
        }

    }

    /**
     * The operation could not be performed because HA is enabled on the Pool
     */
    public static class HaIsEnabled extends XenAPIException {

        /**
         * Create a new HaIsEnabled
         */
        public HaIsEnabled() {
            super("The operation could not be performed because HA is enabled on the Pool");
        }

    }

    /**
     * The host name is invalid.
     */
    public static class HostNameInvalid extends XenAPIException {
        public final String reason;

        /**
         * Create a new HostNameInvalid
         *
         * @param reason
         */
        public HostNameInvalid(String reason) {
            super("The host name is invalid.");
            this.reason = reason;
        }

    }

    /**
     * The operation could not be performed because a domain still exists for the specified VM.
     */
    public static class DomainExists extends XenAPIException {
        public final String vm;
        public final String domid;

        /**
         * Create a new DomainExists
         *
         * @param vm
         * @param domid
         */
        public DomainExists(String vm, String domid) {
            super("The operation could not be performed because a domain still exists for the specified VM.");
            this.vm = vm;
            this.domid = domid;
        }

    }

    /**
     * This host cannot join the pool because the pool has HA enabled but this host has HA disabled.
     */
    public static class HaPoolIsEnabledButHostIsDisabled extends XenAPIException {

        /**
         * Create a new HaPoolIsEnabledButHostIsDisabled
         */
        public HaPoolIsEnabledButHostIsDisabled() {
            super("This host cannot join the pool because the pool has HA enabled but this host has HA disabled.");
        }

    }

    /**
     * This message has been deprecated.
     */
    public static class MessageDeprecated extends XenAPIException {
        public final String message;

        /**
         * Create a new MessageDeprecated
         *
         * @param message
         */
        public MessageDeprecated(String message) {
            super("This message has been deprecated.");
            this.message = message;
        }

    }

    /**
     * This operation cannot be performed because the referenced SR is not properly shared. The SR must both be marked as shared and a currently_attached PBD must exist for each host.
     */
    public static class HaConstraintViolationSrNotShared extends XenAPIException {
        public final String SR;

        /**
         * Create a new HaConstraintViolationSrNotShared
         *
         * @param SR
         */
        public HaConstraintViolationSrNotShared(String SR) {
            super("This operation cannot be performed because the referenced SR is not properly shared. The SR must both be marked as shared and a currently_attached PBD must exist for each host.");
            this.SR = SR;
        }

    }

    /**
     * This operation is not supported during an upgrade
     */
    public static class NotSupportedDuringUpgrade extends XenAPIException {

        /**
         * Create a new NotSupportedDuringUpgrade
         */
        public NotSupportedDuringUpgrade() {
            super("This operation is not supported during an upgrade");
        }

    }

    /**
     * An unknown error occurred while attempting to configure an interface.
     */
    public static class PifConfigurationError extends XenAPIException {
        public final String PIF;
        public final String msg;

        /**
         * Create a new PifConfigurationError
         *
         * @param PIF
         * @param msg
         */
        public PifConfigurationError(String PIF, String msg) {
            super("An unknown error occurred while attempting to configure an interface.");
            this.PIF = PIF;
            this.msg = msg;
        }

    }

    /**
     * The specified interface cannot be used because it has no IP address
     */
    public static class InterfaceHasNoIp extends XenAPIException {
        public final String iface;

        /**
         * Create a new InterfaceHasNoIp
         *
         * @param iface
         */
        public InterfaceHasNoIp(String iface) {
            super("The specified interface cannot be used because it has no IP address");
            this.iface = iface;
        }

    }


    public static String toString(Object object) {
        if (object == null) {
            return null;
        }
        return (String) object;
    }

    public static Long toLong(Object object) {
        if (object == null) {
            return null;
        }
        return Long.valueOf((String) object);
    }

    public static Double toDouble(Object object) {
        if (object == null) {
            return null;
        }
        return (Double) object;
    }

    public static Boolean toBoolean(Object object) {
        if (object == null) {
            return null;
        }
        return (Boolean) object;
    }

    public static Date toDate(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return (Date) object;
        } catch (ClassCastException e){
            //Occasionally the date comes back as an ocaml float rather than 
            //in the xmlrpc format! Catch this and convert. 
            return (new Date((long) (1000*Double.parseDouble((String) object))));
        }
    }

    public static Types.XenAPIObjects toXenAPIObjects(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return XenAPIObjects.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return XenAPIObjects.UNRECOGNIZED;
        }
    }

    public static Types.AfterApplyGuidance toAfterApplyGuidance(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return AfterApplyGuidance.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return AfterApplyGuidance.UNRECOGNIZED;
        }
    }

    public static Types.Cls toCls(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return Cls.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Cls.UNRECOGNIZED;
        }
    }

    public static Types.ConsoleProtocol toConsoleProtocol(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return ConsoleProtocol.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return ConsoleProtocol.UNRECOGNIZED;
        }
    }

    public static Types.EventOperation toEventOperation(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return EventOperation.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return EventOperation.UNRECOGNIZED;
        }
    }

    public static Types.HostAllowedOperations toHostAllowedOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return HostAllowedOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return HostAllowedOperations.UNRECOGNIZED;
        }
    }

    public static Types.IpConfigurationMode toIpConfigurationMode(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return IpConfigurationMode.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return IpConfigurationMode.UNRECOGNIZED;
        }
    }

    public static Types.NetworkOperations toNetworkOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return NetworkOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return NetworkOperations.UNRECOGNIZED;
        }
    }

    public static Types.OnCrashBehaviour toOnCrashBehaviour(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OnCrashBehaviour.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return OnCrashBehaviour.UNRECOGNIZED;
        }
    }

    public static Types.OnNormalExit toOnNormalExit(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return OnNormalExit.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return OnNormalExit.UNRECOGNIZED;
        }
    }

    public static Types.StorageOperations toStorageOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return StorageOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return StorageOperations.UNRECOGNIZED;
        }
    }

    public static Types.TaskAllowedOperations toTaskAllowedOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return TaskAllowedOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TaskAllowedOperations.UNRECOGNIZED;
        }
    }

    public static Types.TaskStatusType toTaskStatusType(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return TaskStatusType.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return TaskStatusType.UNRECOGNIZED;
        }
    }

    public static Types.VbdMode toVbdMode(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VbdMode.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VbdMode.UNRECOGNIZED;
        }
    }

    public static Types.VbdOperations toVbdOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VbdOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VbdOperations.UNRECOGNIZED;
        }
    }

    public static Types.VbdType toVbdType(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VbdType.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VbdType.UNRECOGNIZED;
        }
    }

    public static Types.VdiOperations toVdiOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VdiOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VdiOperations.UNRECOGNIZED;
        }
    }

    public static Types.VdiType toVdiType(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VdiType.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VdiType.UNRECOGNIZED;
        }
    }

    public static Types.VifOperations toVifOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VifOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VifOperations.UNRECOGNIZED;
        }
    }

    public static Types.VmOperations toVmOperations(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VmOperations.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VmOperations.UNRECOGNIZED;
        }
    }

    public static Types.VmPowerState toVmPowerState(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return VmPowerState.valueOf(((String) object).toUpperCase());
        } catch (IllegalArgumentException ex) {
            return VmPowerState.UNRECOGNIZED;
        }
    }

    public static Set<String> toSetOfString(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<String> result = new LinkedHashSet<String>();
        for(Object item: items) {
            String typed = toString(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.AfterApplyGuidance> toSetOfAfterApplyGuidance(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.AfterApplyGuidance> result = new LinkedHashSet<Types.AfterApplyGuidance>();
        for(Object item: items) {
            Types.AfterApplyGuidance typed = toAfterApplyGuidance(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.HostAllowedOperations> toSetOfHostAllowedOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.HostAllowedOperations> result = new LinkedHashSet<Types.HostAllowedOperations>();
        for(Object item: items) {
            Types.HostAllowedOperations typed = toHostAllowedOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.NetworkOperations> toSetOfNetworkOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.NetworkOperations> result = new LinkedHashSet<Types.NetworkOperations>();
        for(Object item: items) {
            Types.NetworkOperations typed = toNetworkOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.StorageOperations> toSetOfStorageOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.StorageOperations> result = new LinkedHashSet<Types.StorageOperations>();
        for(Object item: items) {
            Types.StorageOperations typed = toStorageOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.TaskAllowedOperations> toSetOfTaskAllowedOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.TaskAllowedOperations> result = new LinkedHashSet<Types.TaskAllowedOperations>();
        for(Object item: items) {
            Types.TaskAllowedOperations typed = toTaskAllowedOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.VbdOperations> toSetOfVbdOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.VbdOperations> result = new LinkedHashSet<Types.VbdOperations>();
        for(Object item: items) {
            Types.VbdOperations typed = toVbdOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.VdiOperations> toSetOfVdiOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.VdiOperations> result = new LinkedHashSet<Types.VdiOperations>();
        for(Object item: items) {
            Types.VdiOperations typed = toVdiOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.VifOperations> toSetOfVifOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.VifOperations> result = new LinkedHashSet<Types.VifOperations>();
        for(Object item: items) {
            Types.VifOperations typed = toVifOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Types.VmOperations> toSetOfVmOperations(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Types.VmOperations> result = new LinkedHashSet<Types.VmOperations>();
        for(Object item: items) {
            Types.VmOperations typed = toVmOperations(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Bond> toSetOfBond(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Bond> result = new LinkedHashSet<Bond>();
        for(Object item: items) {
            Bond typed = toBond(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<PBD> toSetOfPBD(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<PBD> result = new LinkedHashSet<PBD>();
        for(Object item: items) {
            PBD typed = toPBD(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<PIF> toSetOfPIF(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<PIF> result = new LinkedHashSet<PIF>();
        for(Object item: items) {
            PIF typed = toPIF(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<PIFMetrics> toSetOfPIFMetrics(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<PIFMetrics> result = new LinkedHashSet<PIFMetrics>();
        for(Object item: items) {
            PIFMetrics typed = toPIFMetrics(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<SM> toSetOfSM(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<SM> result = new LinkedHashSet<SM>();
        for(Object item: items) {
            SM typed = toSM(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<SR> toSetOfSR(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<SR> result = new LinkedHashSet<SR>();
        for(Object item: items) {
            SR typed = toSR(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VBD> toSetOfVBD(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VBD> result = new LinkedHashSet<VBD>();
        for(Object item: items) {
            VBD typed = toVBD(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VBDMetrics> toSetOfVBDMetrics(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VBDMetrics> result = new LinkedHashSet<VBDMetrics>();
        for(Object item: items) {
            VBDMetrics typed = toVBDMetrics(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VDI> toSetOfVDI(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VDI> result = new LinkedHashSet<VDI>();
        for(Object item: items) {
            VDI typed = toVDI(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VIF> toSetOfVIF(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VIF> result = new LinkedHashSet<VIF>();
        for(Object item: items) {
            VIF typed = toVIF(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VIFMetrics> toSetOfVIFMetrics(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VIFMetrics> result = new LinkedHashSet<VIFMetrics>();
        for(Object item: items) {
            VIFMetrics typed = toVIFMetrics(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VLAN> toSetOfVLAN(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VLAN> result = new LinkedHashSet<VLAN>();
        for(Object item: items) {
            VLAN typed = toVLAN(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VM> toSetOfVM(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VM> result = new LinkedHashSet<VM>();
        for(Object item: items) {
            VM typed = toVM(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VMGuestMetrics> toSetOfVMGuestMetrics(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VMGuestMetrics> result = new LinkedHashSet<VMGuestMetrics>();
        for(Object item: items) {
            VMGuestMetrics typed = toVMGuestMetrics(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VMMetrics> toSetOfVMMetrics(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VMMetrics> result = new LinkedHashSet<VMMetrics>();
        for(Object item: items) {
            VMMetrics typed = toVMMetrics(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<VTPM> toSetOfVTPM(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<VTPM> result = new LinkedHashSet<VTPM>();
        for(Object item: items) {
            VTPM typed = toVTPM(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Blob> toSetOfBlob(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Blob> result = new LinkedHashSet<Blob>();
        for(Object item: items) {
            Blob typed = toBlob(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Console> toSetOfConsole(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Console> result = new LinkedHashSet<Console>();
        for(Object item: items) {
            Console typed = toConsole(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Crashdump> toSetOfCrashdump(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Crashdump> result = new LinkedHashSet<Crashdump>();
        for(Object item: items) {
            Crashdump typed = toCrashdump(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Host> toSetOfHost(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Host> result = new LinkedHashSet<Host>();
        for(Object item: items) {
            Host typed = toHost(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<HostCpu> toSetOfHostCpu(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<HostCpu> result = new LinkedHashSet<HostCpu>();
        for(Object item: items) {
            HostCpu typed = toHostCpu(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<HostCrashdump> toSetOfHostCrashdump(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<HostCrashdump> result = new LinkedHashSet<HostCrashdump>();
        for(Object item: items) {
            HostCrashdump typed = toHostCrashdump(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<HostMetrics> toSetOfHostMetrics(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<HostMetrics> result = new LinkedHashSet<HostMetrics>();
        for(Object item: items) {
            HostMetrics typed = toHostMetrics(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<HostPatch> toSetOfHostPatch(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<HostPatch> result = new LinkedHashSet<HostPatch>();
        for(Object item: items) {
            HostPatch typed = toHostPatch(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Message> toSetOfMessage(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Message> result = new LinkedHashSet<Message>();
        for(Object item: items) {
            Message typed = toMessage(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Network> toSetOfNetwork(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Network> result = new LinkedHashSet<Network>();
        for(Object item: items) {
            Network typed = toNetwork(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Pool> toSetOfPool(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Pool> result = new LinkedHashSet<Pool>();
        for(Object item: items) {
            Pool typed = toPool(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<PoolPatch> toSetOfPoolPatch(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<PoolPatch> result = new LinkedHashSet<PoolPatch>();
        for(Object item: items) {
            PoolPatch typed = toPoolPatch(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Task> toSetOfTask(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Task> result = new LinkedHashSet<Task>();
        for(Object item: items) {
            Task typed = toTask(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<DataSource.Record> toSetOfDataSourceRecord(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<DataSource.Record> result = new LinkedHashSet<DataSource.Record>();
        for(Object item: items) {
            DataSource.Record typed = toDataSourceRecord(item);
            result.add(typed);
        }
        return result;
    }

    public static Set<Event.Record> toSetOfEventRecord(Object object) {
        if (object == null) {
            return null;
        }
        Object[] items = (Object[]) object;
        Set<Event.Record> result = new LinkedHashSet<Event.Record>();
        for(Object item: items) {
            Event.Record typed = toEventRecord(item);
            result.add(typed);
        }
        return result;
    }

    public static Map<String, String> toMapOfStringString(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,String> result = new HashMap<String,String>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            String value = toString(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.HostAllowedOperations> toMapOfStringHostAllowedOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.HostAllowedOperations> result = new HashMap<String,Types.HostAllowedOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.HostAllowedOperations value = toHostAllowedOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.NetworkOperations> toMapOfStringNetworkOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.NetworkOperations> result = new HashMap<String,Types.NetworkOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.NetworkOperations value = toNetworkOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.StorageOperations> toMapOfStringStorageOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.StorageOperations> result = new HashMap<String,Types.StorageOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.StorageOperations value = toStorageOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.TaskAllowedOperations> toMapOfStringTaskAllowedOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.TaskAllowedOperations> result = new HashMap<String,Types.TaskAllowedOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.TaskAllowedOperations value = toTaskAllowedOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.VbdOperations> toMapOfStringVbdOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.VbdOperations> result = new HashMap<String,Types.VbdOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.VbdOperations value = toVbdOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.VdiOperations> toMapOfStringVdiOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.VdiOperations> result = new HashMap<String,Types.VdiOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.VdiOperations value = toVdiOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.VifOperations> toMapOfStringVifOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.VifOperations> result = new HashMap<String,Types.VifOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.VifOperations value = toVifOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Types.VmOperations> toMapOfStringVmOperations(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Types.VmOperations> result = new HashMap<String,Types.VmOperations>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Types.VmOperations value = toVmOperations(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<String, Blob> toMapOfStringBlob(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<String,Blob> result = new HashMap<String,Blob>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            String key = toString(entry.getKey());
            Blob value = toBlob(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Long, Long> toMapOfLongLong(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Long,Long> result = new HashMap<Long,Long>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Long key = toLong(entry.getKey());
            Long value = toLong(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Long, Double> toMapOfLongDouble(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Long,Double> result = new HashMap<Long,Double>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Long key = toLong(entry.getKey());
            Double value = toDouble(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Long, Set<String>> toMapOfLongSetOfString(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Long,Set<String>> result = new HashMap<Long,Set<String>>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Long key = toLong(entry.getKey());
            Set<String> value = toSetOfString(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Types.VmOperations, String> toMapOfVmOperationsString(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Types.VmOperations,String> result = new HashMap<Types.VmOperations,String>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Types.VmOperations key = toVmOperations(entry.getKey());
            String value = toString(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Bond, Bond.Record> toMapOfBondBondRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Bond,Bond.Record> result = new HashMap<Bond,Bond.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Bond key = toBond(entry.getKey());
            Bond.Record value = toBondRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<PBD, PBD.Record> toMapOfPBDPBDRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<PBD,PBD.Record> result = new HashMap<PBD,PBD.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            PBD key = toPBD(entry.getKey());
            PBD.Record value = toPBDRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<PIF, PIF.Record> toMapOfPIFPIFRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<PIF,PIF.Record> result = new HashMap<PIF,PIF.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            PIF key = toPIF(entry.getKey());
            PIF.Record value = toPIFRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<PIFMetrics, PIFMetrics.Record> toMapOfPIFMetricsPIFMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<PIFMetrics,PIFMetrics.Record> result = new HashMap<PIFMetrics,PIFMetrics.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            PIFMetrics key = toPIFMetrics(entry.getKey());
            PIFMetrics.Record value = toPIFMetricsRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<SM, SM.Record> toMapOfSMSMRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<SM,SM.Record> result = new HashMap<SM,SM.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            SM key = toSM(entry.getKey());
            SM.Record value = toSMRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<SR, SR.Record> toMapOfSRSRRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<SR,SR.Record> result = new HashMap<SR,SR.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            SR key = toSR(entry.getKey());
            SR.Record value = toSRRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VBD, VBD.Record> toMapOfVBDVBDRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VBD,VBD.Record> result = new HashMap<VBD,VBD.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VBD key = toVBD(entry.getKey());
            VBD.Record value = toVBDRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VBDMetrics, VBDMetrics.Record> toMapOfVBDMetricsVBDMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VBDMetrics,VBDMetrics.Record> result = new HashMap<VBDMetrics,VBDMetrics.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VBDMetrics key = toVBDMetrics(entry.getKey());
            VBDMetrics.Record value = toVBDMetricsRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VDI, VDI.Record> toMapOfVDIVDIRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VDI,VDI.Record> result = new HashMap<VDI,VDI.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VDI key = toVDI(entry.getKey());
            VDI.Record value = toVDIRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VIF, VIF.Record> toMapOfVIFVIFRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VIF,VIF.Record> result = new HashMap<VIF,VIF.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VIF key = toVIF(entry.getKey());
            VIF.Record value = toVIFRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VIFMetrics, VIFMetrics.Record> toMapOfVIFMetricsVIFMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VIFMetrics,VIFMetrics.Record> result = new HashMap<VIFMetrics,VIFMetrics.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VIFMetrics key = toVIFMetrics(entry.getKey());
            VIFMetrics.Record value = toVIFMetricsRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VLAN, VLAN.Record> toMapOfVLANVLANRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VLAN,VLAN.Record> result = new HashMap<VLAN,VLAN.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VLAN key = toVLAN(entry.getKey());
            VLAN.Record value = toVLANRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VM, String> toMapOfVMString(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VM,String> result = new HashMap<VM,String>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VM key = toVM(entry.getKey());
            String value = toString(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VM, Set<String>> toMapOfVMSetOfString(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VM,Set<String>> result = new HashMap<VM,Set<String>>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VM key = toVM(entry.getKey());
            Set<String> value = toSetOfString(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VM, Map<String, String>> toMapOfVMMapOfStringString(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VM,Map<String, String>> result = new HashMap<VM,Map<String, String>>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VM key = toVM(entry.getKey());
            Map<String, String> value = toMapOfStringString(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VM, VM.Record> toMapOfVMVMRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VM,VM.Record> result = new HashMap<VM,VM.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VM key = toVM(entry.getKey());
            VM.Record value = toVMRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VMGuestMetrics, VMGuestMetrics.Record> toMapOfVMGuestMetricsVMGuestMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VMGuestMetrics,VMGuestMetrics.Record> result = new HashMap<VMGuestMetrics,VMGuestMetrics.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VMGuestMetrics key = toVMGuestMetrics(entry.getKey());
            VMGuestMetrics.Record value = toVMGuestMetricsRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<VMMetrics, VMMetrics.Record> toMapOfVMMetricsVMMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<VMMetrics,VMMetrics.Record> result = new HashMap<VMMetrics,VMMetrics.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            VMMetrics key = toVMMetrics(entry.getKey());
            VMMetrics.Record value = toVMMetricsRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Blob, Blob.Record> toMapOfBlobBlobRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Blob,Blob.Record> result = new HashMap<Blob,Blob.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Blob key = toBlob(entry.getKey());
            Blob.Record value = toBlobRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Console, Console.Record> toMapOfConsoleConsoleRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Console,Console.Record> result = new HashMap<Console,Console.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Console key = toConsole(entry.getKey());
            Console.Record value = toConsoleRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Crashdump, Crashdump.Record> toMapOfCrashdumpCrashdumpRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Crashdump,Crashdump.Record> result = new HashMap<Crashdump,Crashdump.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Crashdump key = toCrashdump(entry.getKey());
            Crashdump.Record value = toCrashdumpRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Host, Host.Record> toMapOfHostHostRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Host,Host.Record> result = new HashMap<Host,Host.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Host key = toHost(entry.getKey());
            Host.Record value = toHostRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<HostCpu, HostCpu.Record> toMapOfHostCpuHostCpuRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<HostCpu,HostCpu.Record> result = new HashMap<HostCpu,HostCpu.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            HostCpu key = toHostCpu(entry.getKey());
            HostCpu.Record value = toHostCpuRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<HostCrashdump, HostCrashdump.Record> toMapOfHostCrashdumpHostCrashdumpRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<HostCrashdump,HostCrashdump.Record> result = new HashMap<HostCrashdump,HostCrashdump.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            HostCrashdump key = toHostCrashdump(entry.getKey());
            HostCrashdump.Record value = toHostCrashdumpRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<HostMetrics, HostMetrics.Record> toMapOfHostMetricsHostMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<HostMetrics,HostMetrics.Record> result = new HashMap<HostMetrics,HostMetrics.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            HostMetrics key = toHostMetrics(entry.getKey());
            HostMetrics.Record value = toHostMetricsRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<HostPatch, HostPatch.Record> toMapOfHostPatchHostPatchRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<HostPatch,HostPatch.Record> result = new HashMap<HostPatch,HostPatch.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            HostPatch key = toHostPatch(entry.getKey());
            HostPatch.Record value = toHostPatchRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Message, Message.Record> toMapOfMessageMessageRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Message,Message.Record> result = new HashMap<Message,Message.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Message key = toMessage(entry.getKey());
            Message.Record value = toMessageRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Network, Network.Record> toMapOfNetworkNetworkRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Network,Network.Record> result = new HashMap<Network,Network.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Network key = toNetwork(entry.getKey());
            Network.Record value = toNetworkRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Pool, Pool.Record> toMapOfPoolPoolRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Pool,Pool.Record> result = new HashMap<Pool,Pool.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Pool key = toPool(entry.getKey());
            Pool.Record value = toPoolRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<PoolPatch, PoolPatch.Record> toMapOfPoolPatchPoolPatchRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<PoolPatch,PoolPatch.Record> result = new HashMap<PoolPatch,PoolPatch.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            PoolPatch key = toPoolPatch(entry.getKey());
            PoolPatch.Record value = toPoolPatchRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Map<Task, Task.Record> toMapOfTaskTaskRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map map = (Map) object;
        Map<Task,Task.Record> result = new HashMap<Task,Task.Record>();
        Set<Map.Entry> entries = map.entrySet();
        for(Map.Entry entry: entries) {
            Task key = toTask(entry.getKey());
            Task.Record value = toTaskRecord(entry.getValue());
            result.put(key, value);
        }
        return result;
    }

    public static Bond toBond(Object object) {
        if (object == null) {
            return null;
        }
        return new Bond((String) object);
    }

    public static PBD toPBD(Object object) {
        if (object == null) {
            return null;
        }
        return new PBD((String) object);
    }

    public static PIF toPIF(Object object) {
        if (object == null) {
            return null;
        }
        return new PIF((String) object);
    }

    public static PIFMetrics toPIFMetrics(Object object) {
        if (object == null) {
            return null;
        }
        return new PIFMetrics((String) object);
    }

    public static SM toSM(Object object) {
        if (object == null) {
            return null;
        }
        return new SM((String) object);
    }

    public static SR toSR(Object object) {
        if (object == null) {
            return null;
        }
        return new SR((String) object);
    }

    public static VBD toVBD(Object object) {
        if (object == null) {
            return null;
        }
        return new VBD((String) object);
    }

    public static VBDMetrics toVBDMetrics(Object object) {
        if (object == null) {
            return null;
        }
        return new VBDMetrics((String) object);
    }

    public static VDI toVDI(Object object) {
        if (object == null) {
            return null;
        }
        return new VDI((String) object);
    }

    public static VIF toVIF(Object object) {
        if (object == null) {
            return null;
        }
        return new VIF((String) object);
    }

    public static VIFMetrics toVIFMetrics(Object object) {
        if (object == null) {
            return null;
        }
        return new VIFMetrics((String) object);
    }

    public static VLAN toVLAN(Object object) {
        if (object == null) {
            return null;
        }
        return new VLAN((String) object);
    }

    public static VM toVM(Object object) {
        if (object == null) {
            return null;
        }
        return new VM((String) object);
    }

    public static VMGuestMetrics toVMGuestMetrics(Object object) {
        if (object == null) {
            return null;
        }
        return new VMGuestMetrics((String) object);
    }

    public static VMMetrics toVMMetrics(Object object) {
        if (object == null) {
            return null;
        }
        return new VMMetrics((String) object);
    }

    public static VTPM toVTPM(Object object) {
        if (object == null) {
            return null;
        }
        return new VTPM((String) object);
    }

    public static Blob toBlob(Object object) {
        if (object == null) {
            return null;
        }
        return new Blob((String) object);
    }

    public static Console toConsole(Object object) {
        if (object == null) {
            return null;
        }
        return new Console((String) object);
    }

    public static Crashdump toCrashdump(Object object) {
        if (object == null) {
            return null;
        }
        return new Crashdump((String) object);
    }

    public static Host toHost(Object object) {
        if (object == null) {
            return null;
        }
        return new Host((String) object);
    }

    public static HostCpu toHostCpu(Object object) {
        if (object == null) {
            return null;
        }
        return new HostCpu((String) object);
    }

    public static HostCrashdump toHostCrashdump(Object object) {
        if (object == null) {
            return null;
        }
        return new HostCrashdump((String) object);
    }

    public static HostMetrics toHostMetrics(Object object) {
        if (object == null) {
            return null;
        }
        return new HostMetrics((String) object);
    }

    public static HostPatch toHostPatch(Object object) {
        if (object == null) {
            return null;
        }
        return new HostPatch((String) object);
    }

    public static Message toMessage(Object object) {
        if (object == null) {
            return null;
        }
        return new Message((String) object);
    }

    public static Network toNetwork(Object object) {
        if (object == null) {
            return null;
        }
        return new Network((String) object);
    }

    public static Pool toPool(Object object) {
        if (object == null) {
            return null;
        }
        return new Pool((String) object);
    }

    public static PoolPatch toPoolPatch(Object object) {
        if (object == null) {
            return null;
        }
        return new PoolPatch((String) object);
    }

    public static Session toSession(Object object) {
        if (object == null) {
            return null;
        }
        return new Session((String) object);
    }

    public static Task toTask(Object object) {
        if (object == null) {
            return null;
        }
        return new Task((String) object);
    }

    public static User toUser(Object object) {
        if (object == null) {
            return null;
        }
        return new User((String) object);
    }

    public static Bond.Record toBondRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Bond.Record record = new Bond.Record();
            record.uuid = toString(map.get("uuid"));
            record.master = toPIF(map.get("master"));
            record.slaves = toSetOfPIF(map.get("slaves"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static PBD.Record toPBDRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        PBD.Record record = new PBD.Record();
            record.uuid = toString(map.get("uuid"));
            record.host = toHost(map.get("host"));
            record.SR = toSR(map.get("SR"));
            record.deviceConfig = toMapOfStringString(map.get("device_config"));
            record.currentlyAttached = toBoolean(map.get("currently_attached"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static PIF.Record toPIFRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        PIF.Record record = new PIF.Record();
            record.uuid = toString(map.get("uuid"));
            record.device = toString(map.get("device"));
            record.network = toNetwork(map.get("network"));
            record.host = toHost(map.get("host"));
            record.MAC = toString(map.get("MAC"));
            record.MTU = toLong(map.get("MTU"));
            record.VLAN = toLong(map.get("VLAN"));
            record.metrics = toPIFMetrics(map.get("metrics"));
            record.physical = toBoolean(map.get("physical"));
            record.currentlyAttached = toBoolean(map.get("currently_attached"));
            record.ipConfigurationMode = toIpConfigurationMode(map.get("ip_configuration_mode"));
            record.IP = toString(map.get("IP"));
            record.netmask = toString(map.get("netmask"));
            record.gateway = toString(map.get("gateway"));
            record.DNS = toString(map.get("DNS"));
            record.bondSlaveOf = toBond(map.get("bond_slave_of"));
            record.bondMasterOf = toSetOfBond(map.get("bond_master_of"));
            record.VLANMasterOf = toVLAN(map.get("VLAN_master_of"));
            record.VLANSlaveOf = toSetOfVLAN(map.get("VLAN_slave_of"));
            record.management = toBoolean(map.get("management"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.disallowUnplug = toBoolean(map.get("disallow_unplug"));
        return record;
    }

    public static PIFMetrics.Record toPIFMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        PIFMetrics.Record record = new PIFMetrics.Record();
            record.uuid = toString(map.get("uuid"));
            record.ioReadKbs = toDouble(map.get("io_read_kbs"));
            record.ioWriteKbs = toDouble(map.get("io_write_kbs"));
            record.carrier = toBoolean(map.get("carrier"));
            record.vendorId = toString(map.get("vendor_id"));
            record.vendorName = toString(map.get("vendor_name"));
            record.deviceId = toString(map.get("device_id"));
            record.deviceName = toString(map.get("device_name"));
            record.speed = toLong(map.get("speed"));
            record.duplex = toBoolean(map.get("duplex"));
            record.pciBusPath = toString(map.get("pci_bus_path"));
            record.lastUpdated = toDate(map.get("last_updated"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static SM.Record toSMRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        SM.Record record = new SM.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.type = toString(map.get("type"));
            record.vendor = toString(map.get("vendor"));
            record.copyright = toString(map.get("copyright"));
            record.version = toString(map.get("version"));
            record.requiredApiVersion = toString(map.get("required_api_version"));
            record.configuration = toMapOfStringString(map.get("configuration"));
            record.capabilities = toSetOfString(map.get("capabilities"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.driverFilename = toString(map.get("driver_filename"));
        return record;
    }

    public static SR.Record toSRRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        SR.Record record = new SR.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.allowedOperations = toSetOfStorageOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringStorageOperations(map.get("current_operations"));
            record.VDIs = toSetOfVDI(map.get("VDIs"));
            record.PBDs = toSetOfPBD(map.get("PBDs"));
            record.virtualAllocation = toLong(map.get("virtual_allocation"));
            record.physicalUtilisation = toLong(map.get("physical_utilisation"));
            record.physicalSize = toLong(map.get("physical_size"));
            record.type = toString(map.get("type"));
            record.contentType = toString(map.get("content_type"));
            record.shared = toBoolean(map.get("shared"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.tags = toSetOfString(map.get("tags"));
            record.smConfig = toMapOfStringString(map.get("sm_config"));
            record.blobs = toMapOfStringBlob(map.get("blobs"));
        return record;
    }

    public static VBD.Record toVBDRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VBD.Record record = new VBD.Record();
            record.uuid = toString(map.get("uuid"));
            record.allowedOperations = toSetOfVbdOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringVbdOperations(map.get("current_operations"));
            record.VM = toVM(map.get("VM"));
            record.VDI = toVDI(map.get("VDI"));
            record.device = toString(map.get("device"));
            record.userdevice = toString(map.get("userdevice"));
            record.bootable = toBoolean(map.get("bootable"));
            record.mode = toVbdMode(map.get("mode"));
            record.type = toVbdType(map.get("type"));
            record.unpluggable = toBoolean(map.get("unpluggable"));
            record.storageLock = toBoolean(map.get("storage_lock"));
            record.empty = toBoolean(map.get("empty"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.currentlyAttached = toBoolean(map.get("currently_attached"));
            record.statusCode = toLong(map.get("status_code"));
            record.statusDetail = toString(map.get("status_detail"));
            record.runtimeProperties = toMapOfStringString(map.get("runtime_properties"));
            record.qosAlgorithmType = toString(map.get("qos_algorithm_type"));
            record.qosAlgorithmParams = toMapOfStringString(map.get("qos_algorithm_params"));
            record.qosSupportedAlgorithms = toSetOfString(map.get("qos_supported_algorithms"));
            record.metrics = toVBDMetrics(map.get("metrics"));
        return record;
    }

    public static VBDMetrics.Record toVBDMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VBDMetrics.Record record = new VBDMetrics.Record();
            record.uuid = toString(map.get("uuid"));
            record.ioReadKbs = toDouble(map.get("io_read_kbs"));
            record.ioWriteKbs = toDouble(map.get("io_write_kbs"));
            record.lastUpdated = toDate(map.get("last_updated"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static VDI.Record toVDIRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VDI.Record record = new VDI.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.allowedOperations = toSetOfVdiOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringVdiOperations(map.get("current_operations"));
            record.SR = toSR(map.get("SR"));
            record.VBDs = toSetOfVBD(map.get("VBDs"));
            record.crashDumps = toSetOfCrashdump(map.get("crash_dumps"));
            record.virtualSize = toLong(map.get("virtual_size"));
            record.physicalUtilisation = toLong(map.get("physical_utilisation"));
            record.type = toVdiType(map.get("type"));
            record.sharable = toBoolean(map.get("sharable"));
            record.readOnly = toBoolean(map.get("read_only"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.storageLock = toBoolean(map.get("storage_lock"));
            record.location = toString(map.get("location"));
            record.managed = toBoolean(map.get("managed"));
            record.missing = toBoolean(map.get("missing"));
            record.parent = toVDI(map.get("parent"));
            record.xenstoreData = toMapOfStringString(map.get("xenstore_data"));
            record.smConfig = toMapOfStringString(map.get("sm_config"));
            record.isASnapshot = toBoolean(map.get("is_a_snapshot"));
            record.snapshotOf = toVDI(map.get("snapshot_of"));
            record.snapshots = toSetOfVDI(map.get("snapshots"));
            record.snapshotTime = toDate(map.get("snapshot_time"));
            record.tags = toSetOfString(map.get("tags"));
        return record;
    }

    public static VIF.Record toVIFRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VIF.Record record = new VIF.Record();
            record.uuid = toString(map.get("uuid"));
            record.allowedOperations = toSetOfVifOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringVifOperations(map.get("current_operations"));
            record.device = toString(map.get("device"));
            record.network = toNetwork(map.get("network"));
            record.VM = toVM(map.get("VM"));
            record.MAC = toString(map.get("MAC"));
            record.MTU = toLong(map.get("MTU"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.currentlyAttached = toBoolean(map.get("currently_attached"));
            record.statusCode = toLong(map.get("status_code"));
            record.statusDetail = toString(map.get("status_detail"));
            record.runtimeProperties = toMapOfStringString(map.get("runtime_properties"));
            record.qosAlgorithmType = toString(map.get("qos_algorithm_type"));
            record.qosAlgorithmParams = toMapOfStringString(map.get("qos_algorithm_params"));
            record.qosSupportedAlgorithms = toSetOfString(map.get("qos_supported_algorithms"));
            record.metrics = toVIFMetrics(map.get("metrics"));
        return record;
    }

    public static VIFMetrics.Record toVIFMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VIFMetrics.Record record = new VIFMetrics.Record();
            record.uuid = toString(map.get("uuid"));
            record.ioReadKbs = toDouble(map.get("io_read_kbs"));
            record.ioWriteKbs = toDouble(map.get("io_write_kbs"));
            record.lastUpdated = toDate(map.get("last_updated"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static VLAN.Record toVLANRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VLAN.Record record = new VLAN.Record();
            record.uuid = toString(map.get("uuid"));
            record.taggedPIF = toPIF(map.get("tagged_PIF"));
            record.untaggedPIF = toPIF(map.get("untagged_PIF"));
            record.tag = toLong(map.get("tag"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static VM.Record toVMRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VM.Record record = new VM.Record();
            record.uuid = toString(map.get("uuid"));
            record.allowedOperations = toSetOfVmOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringVmOperations(map.get("current_operations"));
            record.powerState = toVmPowerState(map.get("power_state"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.userVersion = toLong(map.get("user_version"));
            record.isATemplate = toBoolean(map.get("is_a_template"));
            record.suspendVDI = toVDI(map.get("suspend_VDI"));
            record.residentOn = toHost(map.get("resident_on"));
            record.affinity = toHost(map.get("affinity"));
            record.memoryTarget = toLong(map.get("memory_target"));
            record.memoryStaticMax = toLong(map.get("memory_static_max"));
            record.memoryDynamicMax = toLong(map.get("memory_dynamic_max"));
            record.memoryDynamicMin = toLong(map.get("memory_dynamic_min"));
            record.memoryStaticMin = toLong(map.get("memory_static_min"));
            record.VCPUsParams = toMapOfStringString(map.get("VCPUs_params"));
            record.VCPUsMax = toLong(map.get("VCPUs_max"));
            record.VCPUsAtStartup = toLong(map.get("VCPUs_at_startup"));
            record.actionsAfterShutdown = toOnNormalExit(map.get("actions_after_shutdown"));
            record.actionsAfterReboot = toOnNormalExit(map.get("actions_after_reboot"));
            record.actionsAfterCrash = toOnCrashBehaviour(map.get("actions_after_crash"));
            record.consoles = toSetOfConsole(map.get("consoles"));
            record.VIFs = toSetOfVIF(map.get("VIFs"));
            record.VBDs = toSetOfVBD(map.get("VBDs"));
            record.crashDumps = toSetOfCrashdump(map.get("crash_dumps"));
            record.VTPMs = toSetOfVTPM(map.get("VTPMs"));
            record.PVBootloader = toString(map.get("PV_bootloader"));
            record.PVKernel = toString(map.get("PV_kernel"));
            record.PVRamdisk = toString(map.get("PV_ramdisk"));
            record.PVArgs = toString(map.get("PV_args"));
            record.PVBootloaderArgs = toString(map.get("PV_bootloader_args"));
            record.PVLegacyArgs = toString(map.get("PV_legacy_args"));
            record.HVMBootPolicy = toString(map.get("HVM_boot_policy"));
            record.HVMBootParams = toMapOfStringString(map.get("HVM_boot_params"));
            record.HVMShadowMultiplier = toDouble(map.get("HVM_shadow_multiplier"));
            record.platform = toMapOfStringString(map.get("platform"));
            record.PCIBus = toString(map.get("PCI_bus"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.domid = toLong(map.get("domid"));
            record.domarch = toString(map.get("domarch"));
            record.lastBootCPUFlags = toMapOfStringString(map.get("last_boot_CPU_flags"));
            record.isControlDomain = toBoolean(map.get("is_control_domain"));
            record.metrics = toVMMetrics(map.get("metrics"));
            record.guestMetrics = toVMGuestMetrics(map.get("guest_metrics"));
            record.lastBootedRecord = toString(map.get("last_booted_record"));
            record.recommendations = toString(map.get("recommendations"));
            record.xenstoreData = toMapOfStringString(map.get("xenstore_data"));
            record.haAlwaysRun = toBoolean(map.get("ha_always_run"));
            record.haRestartPriority = toString(map.get("ha_restart_priority"));
            record.isASnapshot = toBoolean(map.get("is_a_snapshot"));
            record.snapshotOf = toVM(map.get("snapshot_of"));
            record.snapshots = toSetOfVM(map.get("snapshots"));
            record.snapshotTime = toDate(map.get("snapshot_time"));
            record.transportableSnapshotId = toString(map.get("transportable_snapshot_id"));
            record.blobs = toMapOfStringBlob(map.get("blobs"));
            record.tags = toSetOfString(map.get("tags"));
            record.blockedOperations = toMapOfVmOperationsString(map.get("blocked_operations"));
        return record;
    }

    public static VMGuestMetrics.Record toVMGuestMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VMGuestMetrics.Record record = new VMGuestMetrics.Record();
            record.uuid = toString(map.get("uuid"));
            record.osVersion = toMapOfStringString(map.get("os_version"));
            record.PVDriversVersion = toMapOfStringString(map.get("PV_drivers_version"));
            record.PVDriversUpToDate = toBoolean(map.get("PV_drivers_up_to_date"));
            record.memory = toMapOfStringString(map.get("memory"));
            record.disks = toMapOfStringString(map.get("disks"));
            record.networks = toMapOfStringString(map.get("networks"));
            record.other = toMapOfStringString(map.get("other"));
            record.lastUpdated = toDate(map.get("last_updated"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.live = toBoolean(map.get("live"));
        return record;
    }

    public static VMMetrics.Record toVMMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VMMetrics.Record record = new VMMetrics.Record();
            record.uuid = toString(map.get("uuid"));
            record.memoryActual = toLong(map.get("memory_actual"));
            record.VCPUsNumber = toLong(map.get("VCPUs_number"));
            record.VCPUsUtilisation = toMapOfLongDouble(map.get("VCPUs_utilisation"));
            record.VCPUsCPU = toMapOfLongLong(map.get("VCPUs_CPU"));
            record.VCPUsParams = toMapOfStringString(map.get("VCPUs_params"));
            record.VCPUsFlags = toMapOfLongSetOfString(map.get("VCPUs_flags"));
            record.state = toSetOfString(map.get("state"));
            record.startTime = toDate(map.get("start_time"));
            record.installTime = toDate(map.get("install_time"));
            record.lastUpdated = toDate(map.get("last_updated"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static VTPM.Record toVTPMRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        VTPM.Record record = new VTPM.Record();
            record.uuid = toString(map.get("uuid"));
            record.VM = toVM(map.get("VM"));
            record.backend = toVM(map.get("backend"));
        return record;
    }

    public static Blob.Record toBlobRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Blob.Record record = new Blob.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.size = toLong(map.get("size"));
            record.lastUpdated = toDate(map.get("last_updated"));
            record.mimeType = toString(map.get("mime_type"));
        return record;
    }

    public static Console.Record toConsoleRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Console.Record record = new Console.Record();
            record.uuid = toString(map.get("uuid"));
            record.protocol = toConsoleProtocol(map.get("protocol"));
            record.location = toString(map.get("location"));
            record.VM = toVM(map.get("VM"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static Crashdump.Record toCrashdumpRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Crashdump.Record record = new Crashdump.Record();
            record.uuid = toString(map.get("uuid"));
            record.VM = toVM(map.get("VM"));
            record.VDI = toVDI(map.get("VDI"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static DataSource.Record toDataSourceRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        DataSource.Record record = new DataSource.Record();
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.enabled = toBoolean(map.get("enabled"));
            record.standard = toBoolean(map.get("standard"));
            record.units = toString(map.get("units"));
            record.min = toDouble(map.get("min"));
            record.max = toDouble(map.get("max"));
            record.value = toDouble(map.get("value"));
        return record;
    }

    public static Event.Record toEventRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Event.Record record = new Event.Record();
            record.id = toLong(map.get("id"));
            record.timestamp = toDate(map.get("timestamp"));
            record.clazz = toString(map.get("class"));
            record.operation = toEventOperation(map.get("operation"));
            record.ref = toString(map.get("ref"));
            record.objUuid = toString(map.get("obj_uuid"));


        Object a,b;
        a=map.get("snapshot");
        switch(toXenAPIObjects(record.clazz))
        {
                case           SESSION: b =           toSessionRecord(a); break;
                case              TASK: b =              toTaskRecord(a); break;
                case             EVENT: b =             toEventRecord(a); break;
                case              POOL: b =              toPoolRecord(a); break;
                case        POOL_PATCH: b =         toPoolPatchRecord(a); break;
                case                VM: b =                toVMRecord(a); break;
                case        VM_METRICS: b =         toVMMetricsRecord(a); break;
                case  VM_GUEST_METRICS: b =    toVMGuestMetricsRecord(a); break;
                case              HOST: b =              toHostRecord(a); break;
                case    HOST_CRASHDUMP: b =     toHostCrashdumpRecord(a); break;
                case        HOST_PATCH: b =         toHostPatchRecord(a); break;
                case      HOST_METRICS: b =       toHostMetricsRecord(a); break;
                case          HOST_CPU: b =           toHostCpuRecord(a); break;
                case           NETWORK: b =           toNetworkRecord(a); break;
                case               VIF: b =               toVIFRecord(a); break;
                case       VIF_METRICS: b =        toVIFMetricsRecord(a); break;
                case               PIF: b =               toPIFRecord(a); break;
                case       PIF_METRICS: b =        toPIFMetricsRecord(a); break;
                case              BOND: b =              toBondRecord(a); break;
                case              VLAN: b =              toVLANRecord(a); break;
                case                SM: b =                toSMRecord(a); break;
                case                SR: b =                toSRRecord(a); break;
                case               VDI: b =               toVDIRecord(a); break;
                case               VBD: b =               toVBDRecord(a); break;
                case       VBD_METRICS: b =        toVBDMetricsRecord(a); break;
                case               PBD: b =               toPBDRecord(a); break;
                case         CRASHDUMP: b =         toCrashdumpRecord(a); break;
                case              VTPM: b =              toVTPMRecord(a); break;
                case           CONSOLE: b =           toConsoleRecord(a); break;
                case              USER: b =              toUserRecord(a); break;
                case       DATA_SOURCE: b =        toDataSourceRecord(a); break;
                case              BLOB: b =              toBlobRecord(a); break;
                case           MESSAGE: b =           toMessageRecord(a); break;
                default: throw new RuntimeException("Internal error in auto-generated code whilst unmarshalling event snapshot");
        }
        record.snapshot = b;
        return record;
    }

    public static Host.Record toHostRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Host.Record record = new Host.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.allowedOperations = toSetOfHostAllowedOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringHostAllowedOperations(map.get("current_operations"));
            record.APIVersionMajor = toLong(map.get("API_version_major"));
            record.APIVersionMinor = toLong(map.get("API_version_minor"));
            record.APIVersionVendor = toString(map.get("API_version_vendor"));
            record.APIVersionVendorImplementation = toMapOfStringString(map.get("API_version_vendor_implementation"));
            record.enabled = toBoolean(map.get("enabled"));
            record.softwareVersion = toMapOfStringString(map.get("software_version"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.capabilities = toSetOfString(map.get("capabilities"));
            record.cpuConfiguration = toMapOfStringString(map.get("cpu_configuration"));
            record.schedPolicy = toString(map.get("sched_policy"));
            record.supportedBootloaders = toSetOfString(map.get("supported_bootloaders"));
            record.residentVMs = toSetOfVM(map.get("resident_VMs"));
            record.logging = toMapOfStringString(map.get("logging"));
            record.PIFs = toSetOfPIF(map.get("PIFs"));
            record.suspendImageSr = toSR(map.get("suspend_image_sr"));
            record.crashDumpSr = toSR(map.get("crash_dump_sr"));
            record.crashdumps = toSetOfHostCrashdump(map.get("crashdumps"));
            record.patches = toSetOfHostPatch(map.get("patches"));
            record.PBDs = toSetOfPBD(map.get("PBDs"));
            record.hostCPUs = toSetOfHostCpu(map.get("host_CPUs"));
            record.hostname = toString(map.get("hostname"));
            record.address = toString(map.get("address"));
            record.metrics = toHostMetrics(map.get("metrics"));
            record.licenseParams = toMapOfStringString(map.get("license_params"));
            record.haStatefiles = toSetOfString(map.get("ha_statefiles"));
            record.haNetworkPeers = toSetOfString(map.get("ha_network_peers"));
            record.blobs = toMapOfStringBlob(map.get("blobs"));
            record.tags = toSetOfString(map.get("tags"));
        return record;
    }

    public static HostCpu.Record toHostCpuRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        HostCpu.Record record = new HostCpu.Record();
            record.uuid = toString(map.get("uuid"));
            record.host = toHost(map.get("host"));
            record.number = toLong(map.get("number"));
            record.vendor = toString(map.get("vendor"));
            record.speed = toLong(map.get("speed"));
            record.modelname = toString(map.get("modelname"));
            record.family = toLong(map.get("family"));
            record.model = toLong(map.get("model"));
            record.stepping = toString(map.get("stepping"));
            record.flags = toString(map.get("flags"));
            record.features = toString(map.get("features"));
            record.utilisation = toDouble(map.get("utilisation"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static HostCrashdump.Record toHostCrashdumpRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        HostCrashdump.Record record = new HostCrashdump.Record();
            record.uuid = toString(map.get("uuid"));
            record.host = toHost(map.get("host"));
            record.timestamp = toDate(map.get("timestamp"));
            record.size = toLong(map.get("size"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static HostMetrics.Record toHostMetricsRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        HostMetrics.Record record = new HostMetrics.Record();
            record.uuid = toString(map.get("uuid"));
            record.memoryTotal = toLong(map.get("memory_total"));
            record.memoryFree = toLong(map.get("memory_free"));
            record.live = toBoolean(map.get("live"));
            record.lastUpdated = toDate(map.get("last_updated"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static HostPatch.Record toHostPatchRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        HostPatch.Record record = new HostPatch.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.version = toString(map.get("version"));
            record.host = toHost(map.get("host"));
            record.applied = toBoolean(map.get("applied"));
            record.timestampApplied = toDate(map.get("timestamp_applied"));
            record.size = toLong(map.get("size"));
            record.poolPatch = toPoolPatch(map.get("pool_patch"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static Message.Record toMessageRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Message.Record record = new Message.Record();
            record.uuid = toString(map.get("uuid"));
            record.name = toString(map.get("name"));
            record.priority = toLong(map.get("priority"));
            record.cls = toCls(map.get("cls"));
            record.objUuid = toString(map.get("obj_uuid"));
            record.timestamp = toDate(map.get("timestamp"));
            record.body = toString(map.get("body"));
        return record;
    }

    public static Network.Record toNetworkRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Network.Record record = new Network.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.allowedOperations = toSetOfNetworkOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringNetworkOperations(map.get("current_operations"));
            record.VIFs = toSetOfVIF(map.get("VIFs"));
            record.PIFs = toSetOfPIF(map.get("PIFs"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.bridge = toString(map.get("bridge"));
            record.blobs = toMapOfStringBlob(map.get("blobs"));
            record.tags = toSetOfString(map.get("tags"));
        return record;
    }

    public static Pool.Record toPoolRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Pool.Record record = new Pool.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.master = toHost(map.get("master"));
            record.defaultSR = toSR(map.get("default_SR"));
            record.suspendImageSR = toSR(map.get("suspend_image_SR"));
            record.crashDumpSR = toSR(map.get("crash_dump_SR"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.haEnabled = toBoolean(map.get("ha_enabled"));
            record.haConfiguration = toMapOfStringString(map.get("ha_configuration"));
            record.haStatefiles = toSetOfString(map.get("ha_statefiles"));
            record.haHostFailuresToTolerate = toLong(map.get("ha_host_failures_to_tolerate"));
            record.haPlanExistsFor = toLong(map.get("ha_plan_exists_for"));
            record.haAllowOvercommit = toBoolean(map.get("ha_allow_overcommit"));
            record.haOvercommitted = toBoolean(map.get("ha_overcommitted"));
            record.blobs = toMapOfStringBlob(map.get("blobs"));
            record.tags = toSetOfString(map.get("tags"));
            record.guiConfig = toMapOfStringString(map.get("gui_config"));
        return record;
    }

    public static PoolPatch.Record toPoolPatchRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        PoolPatch.Record record = new PoolPatch.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.version = toString(map.get("version"));
            record.size = toLong(map.get("size"));
            record.poolApplied = toBoolean(map.get("pool_applied"));
            record.hostPatches = toSetOfHostPatch(map.get("host_patches"));
            record.afterApplyGuidance = toSetOfAfterApplyGuidance(map.get("after_apply_guidance"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static Session.Record toSessionRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Session.Record record = new Session.Record();
            record.uuid = toString(map.get("uuid"));
            record.thisHost = toHost(map.get("this_host"));
            record.thisUser = toUser(map.get("this_user"));
            record.lastActive = toDate(map.get("last_active"));
            record.pool = toBoolean(map.get("pool"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }

    public static Task.Record toTaskRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        Task.Record record = new Task.Record();
            record.uuid = toString(map.get("uuid"));
            record.nameLabel = toString(map.get("name_label"));
            record.nameDescription = toString(map.get("name_description"));
            record.allowedOperations = toSetOfTaskAllowedOperations(map.get("allowed_operations"));
            record.currentOperations = toMapOfStringTaskAllowedOperations(map.get("current_operations"));
            record.created = toDate(map.get("created"));
            record.finished = toDate(map.get("finished"));
            record.status = toTaskStatusType(map.get("status"));
            record.residentOn = toHost(map.get("resident_on"));
            record.progress = toDouble(map.get("progress"));
            record.type = toString(map.get("type"));
            record.result = toString(map.get("result"));
            record.errorInfo = toSetOfString(map.get("error_info"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
            record.subtaskOf = toTask(map.get("subtask_of"));
            record.subtasks = toSetOfTask(map.get("subtasks"));
        return record;
    }

    public static User.Record toUserRecord(Object object) {
        if (object == null) {
            return null;
        }
        Map<String,Object> map = (Map<String,Object>) object;
        User.Record record = new User.Record();
            record.uuid = toString(map.get("uuid"));
            record.shortName = toString(map.get("short_name"));
            record.fullname = toString(map.get("fullname"));
            record.otherConfig = toMapOfStringString(map.get("other_config"));
        return record;
    }


   public static Bond toBond(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toBond(parseResult(task.getResult(connection)));
    }

   public static PBD toPBD(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toPBD(parseResult(task.getResult(connection)));
    }

   public static PIF toPIF(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toPIF(parseResult(task.getResult(connection)));
    }

   public static PIFMetrics toPIFMetrics(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toPIFMetrics(parseResult(task.getResult(connection)));
    }

   public static SM toSM(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toSM(parseResult(task.getResult(connection)));
    }

   public static SR toSR(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toSR(parseResult(task.getResult(connection)));
    }

   public static VBD toVBD(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVBD(parseResult(task.getResult(connection)));
    }

   public static VBDMetrics toVBDMetrics(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVBDMetrics(parseResult(task.getResult(connection)));
    }

   public static VDI toVDI(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVDI(parseResult(task.getResult(connection)));
    }

   public static VIF toVIF(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVIF(parseResult(task.getResult(connection)));
    }

   public static VIFMetrics toVIFMetrics(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVIFMetrics(parseResult(task.getResult(connection)));
    }

   public static VLAN toVLAN(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVLAN(parseResult(task.getResult(connection)));
    }

   public static VM toVM(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVM(parseResult(task.getResult(connection)));
    }

   public static VMGuestMetrics toVMGuestMetrics(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVMGuestMetrics(parseResult(task.getResult(connection)));
    }

   public static VMMetrics toVMMetrics(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVMMetrics(parseResult(task.getResult(connection)));
    }

   public static VTPM toVTPM(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toVTPM(parseResult(task.getResult(connection)));
    }

   public static Blob toBlob(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toBlob(parseResult(task.getResult(connection)));
    }

   public static Console toConsole(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toConsole(parseResult(task.getResult(connection)));
    }

   public static Crashdump toCrashdump(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toCrashdump(parseResult(task.getResult(connection)));
    }

   public static Host toHost(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toHost(parseResult(task.getResult(connection)));
    }

   public static HostCpu toHostCpu(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toHostCpu(parseResult(task.getResult(connection)));
    }

   public static HostCrashdump toHostCrashdump(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toHostCrashdump(parseResult(task.getResult(connection)));
    }

   public static HostMetrics toHostMetrics(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toHostMetrics(parseResult(task.getResult(connection)));
    }

   public static HostPatch toHostPatch(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toHostPatch(parseResult(task.getResult(connection)));
    }

   public static Message toMessage(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toMessage(parseResult(task.getResult(connection)));
    }

   public static Network toNetwork(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toNetwork(parseResult(task.getResult(connection)));
    }

   public static Pool toPool(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toPool(parseResult(task.getResult(connection)));
    }

   public static PoolPatch toPoolPatch(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toPoolPatch(parseResult(task.getResult(connection)));
    }

   public static Session toSession(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toSession(parseResult(task.getResult(connection)));
    }

   public static Task toTask(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toTask(parseResult(task.getResult(connection)));
    }

   public static User toUser(Task task, Connection connection) throws XenAPIException, BadServerResponse, XmlRpcException, BadAsyncResult{
               return Types.toUser(parseResult(task.getResult(connection)));
    }

}
