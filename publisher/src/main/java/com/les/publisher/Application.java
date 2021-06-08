package com.les.publisher;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static com.les.common.model.Messages.*;

public class Application {

    public static void main(String[] args) throws Exception {
        String projectId = System.getenv("GOOGLE_APPLICATION_PROJECT_ID");
        String topicId = System.getenv("GOOGLE_APPLICATION_TOPIC_ID");

        TopicName topic = TopicName.ofProjectTopicName(projectId, topicId);

        Publisher publisher = null;
        try {
            publisher = Publisher.newBuilder(topic).build();

            var message = OrderMessage.newBuilder()
                    .setId(UUID.randomUUID().toString())
                    .setTimestamp(Instant.now().toEpochMilli())
                    .setType(Type.forNumber(ThreadLocalRandom.current().nextInt(3)))
                    .build();

            var pubsubMessage = PubsubMessage.newBuilder()
                    .setData(message.toByteString())
                    .build();

            var future = publisher.publish(pubsubMessage);

            var pubsubMessageId = future.get();
            System.out.println("Pub/Sub message id: " + pubsubMessageId);
        } finally {
            if (publisher != null) {
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.SECONDS);
            }
        }

    }
}
