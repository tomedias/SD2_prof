package sd2223.trab2.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface RecordProcessor {
	void onReceive(ConsumerRecord<String, String> r);
}
