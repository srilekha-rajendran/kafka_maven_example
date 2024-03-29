import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class KafkaExample {
    private final String topic;
    private final Properties props;

    public KafkaExample(String brokers, String username, String password) {
        this.topic = username + "-1";

      //  String jaasTemplate = "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";";
       // String jaasCfg = String.format(jaasTemplate, username, password);

        String serializer = StringSerializer.class.getName();
        String deserializer = StringDeserializer.class.getName();
        props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("group.id", username + "-consumer");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest");
        props.put("session.timeout.ms", "30000");
        props.put("key.deserializer", deserializer);
        props.put("value.deserializer", deserializer);
        props.put("key.serializer", serializer);
        props.put("value.serializer", serializer);
     //   props.put("security.protocol", "SASL_SSL");
      //  props.put("sasl.mechanism", "SCRAM-SHA-256");
      //  props.put("sasl.jaas.config", jaasCfg);
    }

    public void consume() {
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Arrays.asList(topic));
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(1000);
            for (ConsumerRecord<String, String> record : records) {
                System.out.printf("%s [%d] offset=%d, key=%s, value=\"%s\"\n",
								  record.topic(), record.partition(),
								  record.offset(), record.key(), record.value());
			}
        }
    }

    public void produce() {
        Thread one = new Thread() {
            public void run() {
            	 System.out.println("Sending........1....");
                try {
                    Producer<String, String> producer = new KafkaProducer<>(props);
                    int i = 0;
                   // while(true) 
                    for(int j =10;j<20;j++)
                    {
                        Date d = new Date();
                        try {
							producer.send(new ProducerRecord<>(topic, Integer.toString(i), d.toString())).get();
						} catch (ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                        System.out.println("Sending.......2.....");
                        Thread.sleep(10);
                        i++;
                    }
                } catch (InterruptedException v) {
                    System.out.println(v);
                }
            }
        };
        one.start();
    }

    public static void main(String[] args) {
	    
	String brokers = "localhost:9092";
	String username = "srilekha";
	String password = "srilekha";
	
	KafkaExample c = new KafkaExample(brokers, username, password);
	c.createTopic(username + "-2");
        c.produce();
        c.consume();
    }
    public void createTopic(String topic) {
    	
    	AdminClient adminClient = AdminClient.create(props);
        short rep = 1;
        NewTopic newTopic = new NewTopic(topic,1,rep);

        adminClient.createTopics(Collections.singletonList(newTopic));
   	System.out.println("Topic created............");
    }
}
