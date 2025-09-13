package com.wtf.app.model.dto;

import com.wtf.app.model.enums.TaskSocialData;
import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.Constants;
import com.wtf.app.parent.SocialParentCommonUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.openqa.selenium.WebDriver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

//@AllArgsConstructor
//@NoArgsConstructor
@Builder
public class AutomationContext implements AutoCloseable {
    private Set<String> traversedGroupsOnlyLocalVar = ConcurrentHashMap.newKeySet();
    private Set<String> traversedGroupsAndIndividualsLocalVar = ConcurrentHashMap.newKeySet();
    private TaskType taskType;
    private TaskSocialData taskSocialDataConst;
    private final String taskId = UUID.randomUUID().toString();
    private final String outputDirectory;
    private String profileName;
    private WebDriver driver;
    private SocialParentCommonUtils instance;
    private SocialModel socialModel;


    public AutomationContext() {
        this.taskType = null;
        this.outputDirectory = "automation-logs/" + "GENERAL" + "/" + taskId;
        taskSocialDataConst = null;
        createOutputDirectory();
    }

    public AutomationContext(TaskType taskType) {
        this.taskType = taskType != null ? taskType : TaskType.WHATSAPP_FETCH_CONTACTS;
        profileName=taskType.name();
        this.outputDirectory = "automation-logs/" + taskType.name() + "/" + taskId;
        taskSocialDataConst = Constants.getTASK_SOCIAL_DATA().get(taskType);
        //this.socialTypeMap.put(taskType, SocialType.valueOf(taskType.name().substring(0, taskType.name().indexOf('_'))));
        createOutputDirectory();
    }

    public AutomationContext(TaskType taskType, SocialParentCommonUtils socialParentCommonUtils) {
        this.taskType = taskType != null ? taskType : TaskType.WHATSAPP_FETCH_CONTACTS;
        profileName = taskType.name();
        this.instance = socialParentCommonUtils;
        this.outputDirectory = "automation-logs/" + taskType.name() + "/" + taskId;
        taskSocialDataConst = Constants.getTASK_SOCIAL_DATA().get(taskType);
        //this.socialTypeMap.put(taskType, SocialType.valueOf(taskType.name().substring(0, taskType.name().indexOf('_'))));
        createOutputDirectory();
    }


    public AutomationContext(TaskType taskType, WebDriver driver) {
        this.driver = driver;
        this.taskType = taskType != null ? taskType : TaskType.WHATSAPP_FETCH_CONTACTS;
        this.profileName = taskType.name();
        this.outputDirectory = "automation-logs/" + taskType.name() + "/" + taskId;
        taskSocialDataConst = Constants.getTASK_SOCIAL_DATA().get(taskType);
        //this.socialTypeMap.put(taskType, SocialType.valueOf(taskType.name().substring(0, taskType.name().indexOf('_'))));
        createOutputDirectory();
    }

    public AutomationContext(TaskType taskType, SocialParentCommonUtils instance, WebDriver driver) {
        this.taskType = taskType != null ? taskType : TaskType.WHATSAPP_FETCH_CONTACTS;
        profileName = taskType.name();
        this.driver = driver;
        this.instance = instance;
        this.outputDirectory = "automation-logs/" + taskType.name() + "/" + taskId;
        taskSocialDataConst = Constants.getTASK_SOCIAL_DATA().get(taskType);
        //this.socialTypeMap.put(taskType, SocialType.valueOf(taskType.name().substring(0, taskType.name().indexOf('_'))));
        createOutputDirectory();
    }
    
    public AutomationContext(Set<String> traversedGroupsOnlyLocalVar, 
                           Set<String> traversedGroupsAndIndividualsLocalVar,
                           TaskType taskType, 
                           TaskSocialData taskSocialDataConst,
                           String profileName,
                           String outputDirectory,
                           WebDriver driver,
                           SocialParentCommonUtils instance,
                             SocialModel socialModel) {
        this.traversedGroupsOnlyLocalVar = traversedGroupsOnlyLocalVar != null ? traversedGroupsOnlyLocalVar : ConcurrentHashMap.newKeySet();
        this.traversedGroupsAndIndividualsLocalVar = traversedGroupsAndIndividualsLocalVar != null ? traversedGroupsAndIndividualsLocalVar : ConcurrentHashMap.newKeySet();
        this.taskType = taskType != null ? taskType : TaskType.WHATSAPP_FETCH_CONTACTS;
        this.taskSocialDataConst = taskSocialDataConst != null ? taskSocialDataConst : Constants.getTASK_SOCIAL_DATA().get(taskType);
        this.profileName = profileName != null ? profileName : taskType.name();
        this.driver = driver;
        this.instance = instance;
        this.socialModel = socialModel;// retrieve from initial setup
        this.outputDirectory = outputDirectory != null ? outputDirectory : "automation-logs/" + taskType.name() + "/" + taskId;
        createOutputDirectory();
    }


    private String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public Set<String> getTraversedGroupsOnlyLocalVar() {
        return traversedGroupsOnlyLocalVar;
    }

    public Set<String> getTraversedGroupsAndIndividualsLocalVar() {
        return traversedGroupsAndIndividualsLocalVar;
    }

    public void setTraversedGroupsOnlyLocalVar(Set<String> traversedGroupsOnlyLocalVar) {
        this.traversedGroupsOnlyLocalVar = traversedGroupsOnlyLocalVar;
    }

    public void setTraversedGroupsAndIndividualsLocalVar(Set<String> traversedGroupsAndIndividualsLocalVar) {
        this.traversedGroupsAndIndividualsLocalVar = traversedGroupsAndIndividualsLocalVar;
    }

    public Set<String> addTraversedGroupsOnly(String groupName) {
        traversedGroupsOnlyLocalVar.add(groupName);
        return traversedGroupsOnlyLocalVar;
    }

    public Set<String> addTraversedGroupsAndIndividuals(String groupName) {
        traversedGroupsAndIndividualsLocalVar.add(groupName);
        return traversedGroupsAndIndividualsLocalVar;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }

    private void createOutputDirectory() {
        try {
            Files.createDirectories(Paths.get(outputDirectory));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create output directory", e);
        }
    }

    @Override
    public void close() {
        // Cleanup resources if needed
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskSocialData getTaskSocialDataConst() {
        return taskSocialDataConst;
    }

    public void setTaskSocialDataConst(TaskSocialData taskSocialDataConst) {
        this.taskSocialDataConst = taskSocialDataConst;
    }

    public WebDriver getDriver() {
        return driver;
    }

    public void setDriver(WebDriver driver) {
        this.driver = driver;
    }

    public SocialParentCommonUtils getInstance() {
        return instance;
    }

    public void setInstance(SocialParentCommonUtils instance) {
        this.instance = instance;
    }

    public SocialModel getSocialModel() {
        return socialModel;
    }

    public void setSocialModel(SocialModel socialModel) {
        this.socialModel = socialModel;
    }
}
