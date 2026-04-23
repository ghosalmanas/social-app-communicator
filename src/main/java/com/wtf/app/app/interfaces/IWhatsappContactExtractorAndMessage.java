package com.wtf.app.app.interfaces;

import java.io.IOException;

public interface IWhatsappContactExtractorAndMessage {

	void updateContactsOrBroadcastMessages(String FILE_NAME_FOR_ALL_GROUP_OR_CONTACT_NAMES_FROM_TXT,
			Boolean doWeReallyNeedToSendMessageToEveryGroup, String BROADCAST_MESSAGE_TO_SEND)
			throws InterruptedException, IOException;
}
