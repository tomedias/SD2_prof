package sd2223.trab2.servers.proxy.msgs;

import sd2223.trab2.api.Message;
import sd2223.trab2.servers.Domain;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public record PostStatusResult(String id, String content, String created_at, MastodonAccount account) {
	
	public long getId() {
		return Long.valueOf(id);
	}
	
	long getCreationTime()  {
		try{
			SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			long x = isoFormat.parse(created_at).getTime();
			return x;

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