package com.wtf.app.app.interfaces;

import java.awt.*;
import java.io.IOException;

public interface IWAContactExtractorAndMessageSenderJoiner {

	void updateContactsOrBroadcastMessages(String FILE_NAME_FOR_ALL_GROUP_OR_CONTACT_NAMES_FROM_TXT,
			Boolean doWeReallyNeedToSendMessageToEveryGroup, String BROADCAST_MESSAGE_TO_SEND)
			throws InterruptedException, IOException;

	void fetchAllWAGroupNames_JoinNewGroups() throws InterruptedException, AWTException, IOException;

	void moveGroupsToArchive() throws InterruptedException, AWTException, IOException;

}
