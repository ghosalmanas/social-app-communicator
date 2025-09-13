package com.wtf.app.service.interfaces;

import com.wtf.app.model.enums.TaskType;
import com.wtf.app.parent.WATGCommonUtils;
import org.springframework.scheduling.annotation.Async;

import java.awt.*;
import java.io.IOException;
import java.util.List;

public interface IAllWAGroupNamesRetrievalAndJoinNewGroups {

	@Async
	void traverseGroups(WATGCommonUtils instance) throws InterruptedException, AWTException, IOException;

	void fetchAllWAGroupNamesAndJoinNewGroups(WATGCommonUtils instance) throws InterruptedException, AWTException, IOException;

	boolean clearAlreadyTraversedOrBroadcastedGroupsFromDBExecution(TaskType taskType);

	boolean clearAlreadyTraversedOrBroadcastedGroupsInExecution(TaskType taskType);

	boolean clearAllAlreadyTraversedInDBFile(TaskType taskType);

	boolean removeGroupsFromAlreadyTraversed(List<String> groups);
}
