package sd2223.trab2.servers.proxy.msgs;

import sd2223.trab2.api.Message;
import sd2223.trab2.servers.Domain;
import java.text.SimpleDateFormat;

public record PostStatusResult(String id, String content, String created_at, MastodonAccount account) {


	public static final String PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

	public long getId() {
		return Long.valueOf(id);
	}
	
	long getCreationTime()  {
		try{
			SimpleDateFormat isoFormat = new SimpleDateFormat(PATTERN);
			return isoFormat.parse(created_at).getTime();
		}catch (Exception e){
			return 0;
		}

	}
	
	public String getText() {
		return content.replace("<p>","").replace("</p>", "");
	}
	
	public Message toMessage() {
		var m =new Message( getId(), account.username(),  Domain.get(), getText());
		m.setCreationTime( getCreationTime() );
		return m;
	}
}