/*
+------------------------------------------------------------------------------+
|                                                                              |
|              J A V A    C L A S S    S P E C I F I C A T I O N               |
|                                                                              |
|                  Copyright 2011-2014 Motorola Solutions Inc.                 |
|                              All Rights Reserved.                            |
|                                                                              |
|                       Motorola Confidential Restricted                       |
+------------------------------------------------------------------------------+
--------------------------------------------------------------------------------

PRODUCT NAME:  ITM Server  
CODED     BY:  Jeff Lu

------------------------------- REVISION HISTORY -------------------------------
dd/mmm/yy  <name>                <CR number>   <description>
04/02/08  cnij001              CCMPD00968807 Add new template
26/02/08  cnij001              CCMPD00968807 Changed setTemplateRadioFlag 
                               after review
11/03/08   chad001             CCMPD01002349 owner agency is not used now                               
13/Mar/08 Tommy Thomadsen      CCMPD01006519 Updated job states + clean up.
01/Apr/08 Tommy Thomadsen      CCMPD01013871 Cleanup database.
24/Apr/08 Kim Mortesnen        CCMPD01022415, Fix defect. 
22/09/08   chad001             CCMPD01090964 Add new column
15/12/08   cmtr001             CCMPD01139373 Client Refresh improvement. 
05/Jan/09 Hai Dong             CCMPD01145535 Take CM5000 feature.
12/Jan/09  Michael Leismann      CCMPD01146532 Added software for CM5000
                                               control head
20/02/09   chad001               CCMPD01171824 status api    
 4/Mar/09 Jens Hansen          CCMPD01173870 Added variable and inner class to
                                             handle sellable features.                                               
13/03/09   cjh102              CCMPD01181042 Added lastExecutedJob                                           
07/07/09   jnss008             CCMPD01235271 Added notes
07/Sep/09  Michael Leismann      CCMPD01258972 Added extended model type, iTM
                                               model type and codeplug version
16/Sep/09  Michael Leismann      CCMPD01259651 Supports the new radio type
                                               structure (using ItmModelType
                                               instead of DeviceType)
06/Oct/09  Jens Hansen           CCMPD01270882 If ext. model type is set to 
                                               null it is converted to ""   
10/Nov/09  Kim Mortensen         CCMPD01284501 Moving Codeplug_Version from 
                                               Device to Codeplug.
26/Nov/09  hgkj73                CCMPD01288783 Added for agency partitioning                                              
30/Nov/09  Michael Leismann      CCMPD01287031 Jobs on memory stick feature:
                                               Removed offline job status
22/Dec/09  Kim Mortensen         CCMPD01297806 Radio user is prevented from 
                                               changing a radio name of the 
                                               known radio                                                                                               
13/Jan/10 Sigurdur Jonsson       CCMPD01302541 iTM 3.0 Feature, job scheduling and
                                               expired scheduling cleanup.
13/Jan/10  Kim Mortensen         CCMPD01302833 Unit test errors fixed.
19/01/10   hgkj73                CCMPD01304268 review comment fixed
09/03/10   ckfm01                CCMPD01319807 Adding notification ID.
07/06/10   chad001               CCMPD01357654 Get the laguages in current codeplug                                                                                               
27/Oct/10  Sigurdur Jonsson      CCMPD01394082 Policy feature, network logging
27/Dec/10  cvkh36                CCMPD01453343 iTM4.0 performance issue
24/Mar/11  Michael Leismann      CCMPD01483125 iTM 5.0: Code clean-up
02/May/11  Hai Dong              CCMPD01495824 iTM 5.0 Change language setting 
18/May/11   chad001              CCMPD01508612 correct language text 
28/May/12  wch378                wch378_autoinclude    Auto Include unknown radio
23/Aug/12  wch378                wch378_CCMPD01687313 BugFix And Enhancement
03/Sep/12  a23126                CCMPD01692862 iTM6.0- Add flash pack support info in meta-data 
                                               - RadioModelInfo.xml
11/Apr/14  a21944                CCMPD01881928 iTM7.0  Folder Refresh Performance Enhance	
04/08/14   tqfn38                CCMPD01913521 Flexera license development  											   
------------------------------------------------------------------------------*/
package com.mot.dm.common.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.mot.dm.common.util.CommonUtils;
import com.mot.dm.common.util.CommonConstants.QueryTerminalType;


public class Device extends DeviceCommon
{

    private static final long serialVersionUID = 8162108687854106920L;

    public static String UNKNOWN = "UNKNOWN";

    private String deviceUid;
    
    //hardware id
    private String hardwareId;


    // device path in file repository
    private String deviceFullPath;

    // devices notification ID
    private String notificationID;

    private JobInfo lastJob;

    private JobInfo lastExecutedJob;

    private Codeplug lastCodeplug;

    // agency where the device is being used
    private Agency agency;

    private Codeplug currentCodeplug;

    /*
     * The radio last connect time
     */
    private Timestamp lastConnectTime;

    /*
     * The proxy name that detected the last connection
     */
    private String lastConnectProxyName = "";
    
    private boolean loggingEnabled = false;

    /*
     * The itmModelType objects and the itmModelTypeId and extModelType strings
     */
    private ItmModelType itmModelType;

    private List<PolicyDeviceMap> policyDeviceMapList = new ArrayList<PolicyDeviceMap>();

    /**
     * A list of features enabled/disable on the device. Used when the proxy
     * updates the device after a job has finished
     */
    private List<FeatureOnDevice> featuresOnDevice = null;   

    private List<String> pendingFeatureList = null;
    
    private List<DeviceAdditionalAttributes> deviceAdditionalAttributesList = null;
    
    /*A special attribute to indicate device type(transparent for customer)*/
    private QueryTerminalType terminalType;

    /*
     * 
     * Format will be XXXX*AA_BB*CC_DD
     * 
     * 
     * */
    private String hwConfigInfo = "";

    /*
     * It shall only be invoked by OR Maping
     * If user updates device shall invoke updateHardwareConfigMap
     * 
     * */
    public void setHwConfigInfo(String hwConfigInfo)
    {
        this.hwConfigInfo = hwConfigInfo;
        
        hwConfigMap.clear();
        hwConfigSWVer = "";
        if (hwConfigInfo == null || hwConfigInfo.trim().equals(""))
        {
            return;
        }

        // Get the column order string and split it into an array
        String[] hwConfigInfoArr = hwConfigInfo.split("@");
        if (hwConfigInfoArr == null || hwConfigInfoArr.length <= 1)
        {
            return;
        }

        hwConfigSWVer = hwConfigInfoArr[0];

        for (int i = 1; i < hwConfigInfoArr.length; i++)
        {
            String[] hwConfigIdAndValue = hwConfigInfoArr[i].split("_");
            if (hwConfigIdAndValue == null)
            {
                continue;
            }

            if (hwConfigIdAndValue.length == 0 || hwConfigIdAndValue[0] == null) //key cannot be empty
            {
                continue;
            }

            
            if (hwConfigIdAndValue.length > 1 && null != hwConfigIdAndValue[1] && hwConfigIdAndValue[1].length() > 0)
            {
                hwConfigMap.put(Integer.parseInt(hwConfigIdAndValue[0]),
                        CommonUtils.HexString2ByteArray(hwConfigIdAndValue[1]));
            }
            else
            {
                hwConfigMap.put(Integer.parseInt(hwConfigIdAndValue[0]), new byte[] {});
            }
        }

    }

    public String getHwConfigInfo()
    {
        if(null == hwConfigInfo) //empty string is safe than null
        {
            hwConfigInfo = "";
        }
        
        return this.hwConfigInfo;
    }

    private HashMap<Integer, byte[]> hwConfigMap = new HashMap<Integer, byte[]>();
    private String hwConfigSWVer = "";

    public void updateHardwareConfigMap(HashMap<Integer, byte[]> hardwareConfig)
    {
        String currentSWVersion = "";
        String[] entry = Software.getSoftwareEntry(this.softwareVersion);
        if (entry != null)
        {
            currentSWVersion = entry[Software.RELEASE_NUM_FIELD];
        }
        
        if (currentSWVersion.compareToIgnoreCase(getHardwareConfigSWVer()) < 0)
        {
            return; // avoid sw downgrade case
        }

        this.hwConfigSWVer = currentSWVersion;
        this.hwConfigMap.clear();
        
        if(hardwareConfig != null && hardwareConfig.size() > 0 )
        {
            this.hwConfigMap.putAll(hardwareConfig);
        }
        
        this.hwConfigInfo = getHwConfigInfoByConfigMap(hardwareConfig);
    }

    public HashMap<Integer, byte[]> getHardwareConfigMap()
    { 
        return hwConfigMap;
    }

    public String getHardwareConfigSWVer()
    {
        return hwConfigSWVer;
    }

    private String getHwConfigInfoByConfigMap(HashMap<Integer, byte[]> hwConfigMap)
    {
        StringBuilder newHwConfigInfoBuilder = new StringBuilder();
        newHwConfigInfoBuilder.append(this.hwConfigSWVer);
        
        if(null != hwConfigMap && hwConfigMap.size() > 0)
        {
            Iterator<Entry<Integer, byte[]>> hwIt = hwConfigMap.entrySet().iterator();
            while (hwIt.hasNext())
            {
                Map.Entry<Integer, byte[]> entry = (Map.Entry<Integer, byte[]>) hwIt.next();
                Integer key = entry.getKey();
                byte[] value = entry.getValue();

                if (key != null
                        && value != null)
                {
                    newHwConfigInfoBuilder.append(String.format("@%d_%s", key, CommonUtils.ByteArray2HexString(value)));
                }

            }
        }

        return newHwConfigInfoBuilder.toString();
    }
    
    public List<String> getPendingFeatureList()
    {
        return pendingFeatureList;
    }

    public void setPendingFeatureList(List<String> pendingFeatureList)
    {
        this.pendingFeatureList = pendingFeatureList;
    }

    @Override
    public long getId()
    {
        if (null != agency)
        {
            return agency.getId();
        }
        return super.getId();
    }


    public String getDeviceFullPath()
    {
        return deviceFullPath;
    }

    public void setDeviceFullPath(String fullPath)
    {
        this.deviceFullPath = fullPath;
    }
    
    public boolean isControlHead()
    {
        String itmModelTypeID = itmModelType.getItmModelTypeId();
        if (itmModelTypeID.equals(ItmModelType.Ethernet_CONTROL_HEAD_ITM_MODEL_TYPE_ID) ||
                itmModelTypeID.equals(ItmModelType.MTM800E_CONTROL_HEAD_ITM_MODEL_TYPE_ID))
        {
            return true;
        }
        
        //Can handle auto-include case
        if (itmModelTypeID.equals(ItmModelType.UnKnown_Radio_ITM_MODEL_TYPE_ID))
        {
            if (terminalType != null && terminalType.equals(QueryTerminalType.NGCH_CH))
            {
                return true;
            }
        }
        
        return false;
    }

    public boolean isCloneJobEnable()
    {
        boolean isEnable = false;
        if (this.getLastJob() != null && (
                this.getGuiRadioState() == StatusConstants.DEVICE_NEW
                        || this.getGuiRadioState() == StatusConstants.DEVICE_FAILED
                        || this.getGuiRadioState() == StatusConstants.DEVICE_READY
                        || this.getGuiRadioState() == StatusConstants.DEVICE_EXPIRED))
        {
            JobInfo theLastJob = this.getLastJob();
            Set<Task> taskSet = theLastJob.getTasksList();
            Iterator<Task> iter = taskSet.iterator();
            /*
             * we don't allow to clone simple jobs like Get Codeplug, restore codeplug,
             * recover radio. For these jobs, the first task is CP Upload,CP overwrite,
             * recover.
             */
            Task task = (Task) iter.next();
            int taskValue = task.getTaskActionType().intValue();
            if (taskValue != StatusConstants.TASK_TYPE_CP_UPLOAD
                    && taskValue != StatusConstants.TASK_TYPE_CP_OVERRIDE
                    && taskValue != StatusConstants.TASK_TYPE_RECOVERY)
            {
                isEnable = true;
            }
        }
        return isEnable;
    }

    public boolean isCancelJobEnable()
    {
        boolean isEnable = true;
        if (this.getGuiRadioState() == StatusConstants.DEVICE_FAILED || this.getGuiRadioState() == StatusConstants.DEVICE_READY
        || this.getGuiRadioState() == StatusConstants.DEVICE_EXPIRED || this.getGuiRadioState() == StatusConstants.DEVICE_NEW)
        {
            isEnable = false;
        }
        return isEnable;
    }

    public boolean isProgramRadioEnable()
    {
        boolean isEnable = true;
        if (this.isJobPending() || this.isJobRunning() || this.isJobPartial())
        {
            isEnable = false;
        }
        return isEnable;
    }

    public Agency getAgency()
    {
        return agency;
    }

    public void setAgency(Agency agency)
    {
        this.agency = agency;
    }

    public List<PolicyDeviceMap> getPolicyDeviceMapList()
    {
        return policyDeviceMapList;
    }

    public void setPolicyDeviceMapList(List<PolicyDeviceMap> policyDeviceMapList)
    {
        this.policyDeviceMapList = policyDeviceMapList;
    }

    public boolean isLoggingEnabled()
    {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled)
    {
        this.loggingEnabled = loggingEnabled;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return new org.apache.commons.lang.builder.HashCodeBuilder().append(deviceId).hashCode();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof Device == false)
        {
            return false;
        }
        if (this == obj)
        {
            return true;
        }

        Device rhs = (Device) obj;

        return new EqualsBuilder().append(deviceId, rhs.deviceId)
                .append(hardwareId, rhs.hardwareId).append(logicalId, rhs.logicalId)
                .append(deviceName, rhs.deviceName).append(serialNumber, rhs.serialNumber)
                .append(deviceFullPath, rhs.deviceFullPath).append(itmModelType, rhs.itmModelType)
                .append(templateName, rhs.templateName)
                .append(templateCodeplugVersion, rhs.templateCodeplugVersion)
                .append(templateModified, rhs.templateModified)
                .append(templateRadioFlag, rhs.templateRadioFlag)
                .append(featuresOnDevice, rhs.featuresOnDevice)
                .append(policyDeviceMapList, rhs.policyDeviceMapList)
                .append(loggingEnabled, rhs.loggingEnabled).isEquals();
    }

    @Override
    public String toString()
    {
        ToStringBuilder sb = new ToStringBuilder(this, ToStringStyle.DEFAULT_STYLE).append(
                "deviceId", this.deviceId).append("deviceName", this.deviceName);
        /*
         * The information will only valid if both of them are not null, it
         * should never happen that one of them is null and the other is not
         * null.
         */
        if (lastConnectProxyName != null && lastConnectTime != null)
            sb.append("lastConnectProxyName", this.lastConnectProxyName).append("lastConnectTime",
                    this.lastConnectTime.toString());
        return sb.toString();
    }

    /**
     * @return the currentCodeplug
     */
    public Codeplug getCurrentCodeplug()
    {
        return currentCodeplug;
    }

    /**
     * @param currentCodeplug
     *            the currentCodeplug to set
     */
    public void setCurrentCodeplug(Codeplug currentCodeplug)
    {
        this.currentCodeplug = currentCodeplug;
    }

    public String getDeviceUid()
    {
        return deviceUid;
    }

    public void setDeviceUid(String deviceUid)
    {
        this.deviceUid = deviceUid;
    }

    /**
     * Value shall be used to display on the GUI, only.
     * 
     * @return State to display on the GUI.
     */
    public int getGuiRadioState()
    {
        return guiRadioState;
    }

    public void setGuiRadioState(int guiRadioState)
    {
        this.guiRadioState = guiRadioState;
    }

    /**
     * @return True if this is a new radio. This determined based on whether the
     *         radio has codeplugs. Templates will always have codeplugs and for
     *         that reason they are always new.
     */
    public boolean isNew()
    {
        return (currentCodeplug == null);
    }

    public JobInfo getLastJob()
    {
        return lastJob;
    }

    public void setLastJob(JobInfo lastJob)
    {
        this.lastJob = lastJob;
    }

    public Codeplug getLastCodeplug()
    {
        return lastCodeplug;
    }

    public void setLastCodeplug(Codeplug lastCodeplug)
    {
        this.lastCodeplug = lastCodeplug;
    }

    public int getStatusOfLastJob()
    {
        if (lastJob == null)
            return 0;
        return lastJob.getJobStatus();
    }

    public boolean isJobPending()
    {
        if (lastJob == null)
            return false;
        return (lastJob.getJobStatus() == StatusConstants.JOB_STATUS_PENDING || lastJob
                .getJobStatus() == StatusConstants.JOB_STATUS_WAITING || lastJob
                    .getJobStatus() == StatusConstants.JOB_STATUS_PENDING_LICENSE);
    }

    public boolean isJobRunning()
    {
        if (lastJob == null)
            return false;
        return lastJob.getJobStatus() == StatusConstants.JOB_STATUS_RUNNING;
    }

    public boolean isJobPerformed()
    {
        if (lastJob == null)
            return false;
        return lastJob.getJobStatus() == StatusConstants.JOB_STATUS_PERFORMED;
    }

    public boolean isJobFailed()
    {
        if (lastJob == null)
            return false;
        return lastJob.getJobStatus() == StatusConstants.JOB_STATUS_FAILED;
    }

    public boolean isJobCanceled()
    {
        if (lastJob == null)
            return false;
        return lastJob.getJobStatus() == StatusConstants.JOB_STATUS_CANCELED;
    }

    public boolean isJobTerminated()
    {
        if (lastJob == null)
            return false;
        return lastJob.getJobStatus() == StatusConstants.JOB_STATUS_TERMINATED;
    }

    public boolean isJobPartial()
    {
        if (lastJob == null)
            return false;
        return lastJob.getJobStatus() == StatusConstants.JOB_STATUS_PARTIAL;
    }

    public boolean isOwnPolicy()
    {
        if (getPolicyDeviceMapList() == null || getPolicyDeviceMapList().size() == 0)
        {
          return false;
        }
        else
        {
          return true;
        }
    }
    

    public List<FeatureOnDevice> getFeaturesOnDevice()
    {
        return featuresOnDevice;
    }

    public void setFeaturesOnDevice(List<FeatureOnDevice> featuresOnDevice)
    {
        this.featuresOnDevice = featuresOnDevice;
    }

    /**
     * returns a new FeatureOnDevice object, which can then be populated with
     * values by the invoking party.
     * 
     * @return FeatureOnDevice object
     */
    public FeatureOnDevice getFeatureOnDeviceObject()
    {
        return new FeatureOnDevice();
    }

    /*
     * Get the last connect time
     * 
     * @return Timestamp
     */
    public Timestamp getLastConnectTime()
    {
        return lastConnectTime;
    }

    /*
     * Set the last connect time
     * 
     * @param lastConnectTime Timestamp
     */
    public void setLastConnectTime(Timestamp lastConnectTime)
    {
        this.lastConnectTime = lastConnectTime;
    }

    /*
     * Get the proxy name that detected last connection
     * 
     * @return String
     */
    public String getLastConnectProxyName()
    {
        return lastConnectProxyName;
    }

    /*
     * Set the proxy name that detected last connection
     * 
     * @param lastConnectProxyName String
     */
    public void setLastConnectProxyName(String lastConnectProxyName)
    {
        this.lastConnectProxyName = lastConnectProxyName;
    }

    /*
     * Get the itm model type
     * 
     * @return itm model type
     */
    public ItmModelType getItmModelType()
    {
        return itmModelType;
    }

    /*
     * Set the itm model type
     * 
     * @param itmModelType
     */
    public void setItmModelType(ItmModelType itmModelType)
    {
        this.itmModelType = itmModelType;
        itmModelTypeId = itmModelType.getItmModelTypeId();

    }

    /*
     * Get the extended model types as a List
     * 
     * @return List of extended model type strings
     */
    public List<String> getExtModelTypes()
    {
        List<String> extModelTypes = new ArrayList<String>();

        // If the extended model type is known then use that one ...
        if (extModelType.length() != 0)
        {
            extModelTypes.add(extModelType);
        }
        // ... otherwise get all the extended model types from the itmModelType
        else
        {
            extModelTypes = itmModelType.getExtModelTypes();
        }

        return extModelTypes;
    }
    
    
    public String getHardwareId()
    {
        return hardwareId;
    }

    public void setHardwareId(String hardwareId)
    {
        this.hardwareId = hardwareId;
    }

    /*
     * If the extModelType is known (=not empty) then get the model type name
     * from the model type map otherwise return an empty string
     * 
     * @return extended model type description
     */
    public String getExtModelTypeDescription()
    {
        String extModelTypeDescription = "";
        if (extModelType.length() != 0)
        {
            extModelTypeDescription = itmModelType.getModelTypeNames().get(extModelType);
        }

        return extModelTypeDescription;
    }

    /**
     * Returns the last job executed on the device.
     * 
     * @return A JobInfo object
     */
    public JobInfo getLastExecutedJob()
    {
        return lastExecutedJob;
    }

    /**
     * Set the last job executed on the device
     * 
     * @param lastExecutedJob
     */
    public void setLastExecutedJob(JobInfo lastExecutedJob)
    {
        this.lastExecutedJob = lastExecutedJob;
    }

    /**
     * Inner class used to specify a feature on the device
     * 
     * @author cjh102
     */
    public class FeatureOnDevice implements java.io.Serializable
    {
        private static final long serialVersionUID = -4492648257076864859L;

        private String featureID;

        private String festureState;

        public String getFeatureID()
        {
            return featureID;
        }

        public void setFeatureID(String featureID)
        {
            this.featureID = featureID;
        }

        public String getFestureState()
        {
            return festureState;
        }

        public void setFestureState(String festureState)
        {
            this.festureState = festureState;
        }
    }

    /**
     * Get the device Notification ID
     * 
     * @return
     */
    public String getNotificationID()
    {
        return notificationID;
    }

    /**
     * Set the Notification ID.
     * 
     * @param notificationID
     */
    public void setNotificationID(String notificationID)
    {
        this.notificationID = notificationID;
    }

	public List<DeviceAdditionalAttributes> getDeviceAdditionalAttributesList() {
		return deviceAdditionalAttributesList;
	}

	public void setDeviceAdditionalAttributesList(
			List<DeviceAdditionalAttributes> deviceAdditionalAttributesList) {
		this.deviceAdditionalAttributesList = deviceAdditionalAttributesList;
	}

    /**
     * A special attribute to indicate device type(transparent for customer)
     * @return the terminalType
     */
    public QueryTerminalType getTerminalType()
    {
        return terminalType;
    }

    /**
     * A special attribute to indicate device type(transparent for customer)
     * @param terminalType the terminalType to set
     */
    public void setTerminalType(QueryTerminalType terminalType)
    {
        this.terminalType = terminalType;
    }
}
