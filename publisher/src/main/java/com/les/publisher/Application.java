package com.les.publisher;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.les.common.model.Messages.*;

public class Application {

    public static void main(String[] args) throws Exception {
        String projectId = System.getenv("GOOGLE_APPLICATION_PROJECT_ID");
        String topicId = System.getenv("GOOGLE_APPLICATION_TOPIC_ID");

        var cli = getNumberOfMessages(args);
        var numberOfMessages = Integer.parseInt(cli.getOptionValue("n"));
        var timeout = Integer.parseInt(cli.getOptionValue("t"));

        TopicName topic = TopicName.ofProjectTopicName(projectId, topicId);

        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topic).build();

            for (int i = 0; i < numberOfMessages; i++) {
                var now = Instant.now();
                var message = OrderMessage.newBuilder()
                        .setId(UUID.randomUUID().toString())
                        .setTimestamp(now.toEpochMilli())
                        .setType(Type.forNumber(ThreadLocalRandom.current().nextInt(3)))
                        .build();

                var pubsubMessage = PubsubMessage.newBuilder()
                        .setData(message.toByteString())
                        .putAttributes("timestamp", now.toString())
                        .build();


                var future = publisher.publish(pubsubMessage);

                var pubsubMessageId = future.get();
                System.out.println("Pub/Sub message id: " + pubsubMessageId + ", timestamp: " + now + ", type: " + message.getType());
                Thread.sleep(1000L * timeout);
            }
        } finally {
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.SECONDS);
            }
        }

    }

    private static CommandLine getNumberOfMessages(String[] args) throws ParseException {
        var cmdParser = new DefaultParser();
        var options = new Options();
        options.addOption("n", "number", true, "Number of messages to sent");
        options.addOption("t", "timeout", true, "Timeout between messages in sec");
        return cmdParser.parse(options, args);
    }
}
