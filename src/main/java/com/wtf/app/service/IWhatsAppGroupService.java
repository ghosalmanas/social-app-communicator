package com.wtf.app.service;

import com.wtf.app.parent.WATGCommonUtils;
import java.io.IOException;
import java.awt.AWTException;
import java.util.Set;

public interface IWhatsAppGroupService {
    void traverseGroups(WATGCommonUtils instance) throws InterruptedException, AWTException, IOException;
    String getFilePathToExportContactsData();
    Set<String> getAlreadyTraversedGroupsOnlyAndUpdateDate();
    int getExpectedSizeToModuloToInsertExportToFile();
    boolean searchAndClickOnLastVisitedGroup(int counter, boolean hasNewGroupJoinedButNotRestored, String lastVisitedGroup);
    int getMaxAllowedRepeatOfSameGroup();
    // Add other public methods from WATGFetchAllWAContactsWGroupNamesService as needed
}
