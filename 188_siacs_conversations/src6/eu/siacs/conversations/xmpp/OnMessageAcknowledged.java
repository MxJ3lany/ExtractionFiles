package eu.siacs.conversations.xmpp;

import eu.siacs.conversations.entities.Account;

public interface OnMessageAcknowledged {
	boolean onMessageAcknowledged(Account account, String id);
}
