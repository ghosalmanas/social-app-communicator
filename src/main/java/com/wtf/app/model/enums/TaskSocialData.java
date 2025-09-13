package com.wtf.app.model.enums;

import java.util.Set;

public class TaskSocialData {
    private TaskType taskType;
    private SocialType socialType;
    private boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA;
    private String visitedGroupFileName;
    private Set<String> visitedGroupNames;
    private String joinGroupFileName;
    private Set<String> joinGroupNames;


    public TaskSocialData(TaskType taskType, SocialType socialType, boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA, String visitedGroupFileName, Set<String> visitedGroupNames, String joinGroupFileName, Set<String> joinGroupNames) {
        this.taskType = taskType;
        this.socialType = socialType;
        this.isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA = isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA;
        this.visitedGroupFileName = visitedGroupFileName;
        this.visitedGroupNames = visitedGroupNames;
        this.joinGroupFileName = joinGroupFileName;
        this.joinGroupNames = joinGroupNames;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskContextEnum(TaskType taskType) {
        this.taskType = taskType;
    }

    public SocialType getSocialType() {
        return socialType;
    }

    public void setSocialType(SocialType socialType) {
        this.socialType = socialType;
    }

    public boolean isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days() {
        return isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA;
    }

    public void setAlreadyUpdatedThePersonalGroupAndContactsForTheLast3Days(boolean alreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA) {
        isAlreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA = alreadyUpdatedThePersonalGroupAndContactsForTheLast3DaysWA;
    }

    public String getVisitedGroupFileName() {
        return visitedGroupFileName;
    }

    public void setVisitedGroupFileName(String visitedGroupFileName) {
        this.visitedGroupFileName = visitedGroupFileName;
    }

    public String getJoinGroupFileName() {
        return joinGroupFileName;
    }

    public void setJoinGroupFileName(String joinGroupFileName) {
        this.joinGroupFileName = joinGroupFileName;
    }

    public Set<String> getVisitedGroupNames() {
        return visitedGroupNames;
    }

    public void setVisitedGroupNames(Set<String> visitedGroupNames) {
        this.visitedGroupNames = visitedGroupNames;
    }

    public Set<String> getJoinGroupNames() {
        return joinGroupNames;
    }

    public void setJoinGroupNames(Set<String> joinGroupNames) {
        this.joinGroupNames = joinGroupNames;
    }

}
