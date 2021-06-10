package com.les.batch;

import com.google.protobuf.InvalidProtocolBufferException;
import com.les.common.model.Messages;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.les.common.model.Messages.OrderMessage;

public class TestApplication {

    private static final Logger LOG = LoggerFactory.getLogger(TestApplication.class);


    public static void main(String[] args) {

        PipelineResult.State state = run().waitUntilFinish();
        LOG.info("Pipeline state: {}", state);
    }

    public static PipelineResult run() {

        var pipeline = Pipeline.create();

        var list = List.of(
                OrderMessage.newBuilder().setId(UUID.randomUUID().toString()).setType(Messages.Type.ORDER).setTimestamp(Instant.now().toEpochMilli()).build(),
                OrderMessage.newBuilder().setId(UUID.randomUUID().toString()).setType(Messages.Type.SELL).setTimestamp(Instant.now().toEpochMilli()).build(),
                OrderMessage.newBuilder().setId(UUID.randomUUID().toString()).setType(Messages.Type.REFUND).setTimestamp(Instant.now().toEpochMilli()).build()
        );
        pipeline
                .apply(
                        Create.of(list)
                )
                .apply(
                        "Map strings to OrderMessage",
                        MapElements.via(new SimpleFunction<OrderMessage, String>() {
                            @Override
                            public String apply(OrderMessage input) {
                                return Base64.getEncoder().encodeToString(input.toByteArray());
                            }
                        })
                )
                .apply(
                        "Write to file(s)",
                        TextIO.write()
                                .withoutSharding()
                                .to("orders")
                );

        return pipeline.run();
    }
}
