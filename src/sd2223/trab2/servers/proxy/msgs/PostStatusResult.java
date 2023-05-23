package sd2223.trab2.servers.proxy.msgs;

import sd2223.trab2.api.Message;
import sd2223.trab2.servers.Domain;

public record PostStatusResult(String id, String content, String created_at, MastodonAccount account) {
	
	public long getId() {
		return Long.valueOf(id);
	}
	
	long getCreationTime() {
		return 0;
	}
	
	public String getText() {
		return content;
	}
	
	public Message toMessage() {
		var m = new Message( getId(), account.username(), Domain.get(), getText());
		m.setCreationTime( getCreationTime() );
		return m;
	}
}