package com.les.stream;

import com.google.api.services.bigquery.model.TableRow;
import com.les.common.model.Messages;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.extensions.protobuf.ProtoCoder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.Filter;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.windowing.*;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;
import org.joda.time.Instant;

import java.util.Base64;

import static com.les.common.model.Messages.OrderMessage;

public class Application {

    /**
     * The {@link Options} class provides the custom execution options passed by the executor at the
     * command-line.
     */
    public interface Options extends PipelineOptions, StreamingOptions {
        @Description(
                "The Cloud Pub/Sub subscription to consume from. "
                        + "The name should be in the format of "
                        + "projects/<project-id>/subscriptions/<subscription-name>.")
        @Validation.Required
        ValueProvider<String> getInputSubscription();

        void setInputSubscription(ValueProvider<String> value);

        @Description("The directory to output files to. Must end with a slash.")
        @Validation.Required
        ValueProvider<String> getOutputDirectory();

        void setOutputDirectory(ValueProvider<String> value);

        @Description("Table spec to write the output to")
        @Validation.Required
        ValueProvider<String> getOutputTableSpec();

        void setOutputTableSpec(ValueProvider<String> value);

    }

    public static void main(String[] args) {
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        options.setStreaming(true);

        run(options);
    }

    public static PipelineResult run(Options options) {

        var pipeline = Pipeline.create(options);

        PCollection<OrderMessage> orderMessages = pipeline
                .apply(
                        "Read from Pub/Sub subscription",
                        PubsubIO.readProtos(OrderMessage.class)
                                .fromSubscription(options.getInputSubscription())
                                .withTimestampAttribute("timestamp")
                );

        orderMessages
                .apply(
                        "Map to string",
                        MapElements.via(new SimpleFunction<OrderMessage, String>() {
                            @Override
                            public String apply(OrderMessage input) {

                                return Base64.getEncoder().encodeToString(input.toByteArray());
                            }
                        })
                )
                .apply(
                        "Window",
                        Window.<String>into(FixedWindows.of(Duration.standardHours(1)))
                                .triggering(
                                        AfterWatermark.pastEndOfWindow()
                                                // During the hour, get near real-time estimates.
                                                .withEarlyFirings(
                                                        AfterProcessingTime
                                                                .pastFirstElementInPane()
                                                                .plusDelayOf(Duration.standardMinutes(1))
                                                )
                                                // Fire on any late data so the bill can be corrected.
                                                .withLateFirings(AfterPane.elementCountAtLeast(1))
                                )
                                .withAllowedLateness(Duration.standardMinutes(30))
                                .accumulatingFiredPanes()
                )
                .apply(
                        "Write File(s)",
                        TextIO.write()
                                .withWindowedWrites()
                                .withNumShards(1)
                                .to(options.getOutputDirectory() + "orders-")
                );

        orderMessages
                .apply(
                        "Filter OrderMessages with ORDER type",
                        Filter.by(orderMessage -> orderMessage.getType() == Messages.Type.ORDER)
                )
                .apply(
                        "Transform OrderMessage to TableRow",
                        MapElements.via(new SimpleFunction<OrderMessage, TableRow>() {
                            @Override
                            public TableRow apply(OrderMessage userMessage) {
                                return new TableRow()
                                        .set("id", userMessage.getId())
                                        .set("timestamp", Instant.ofEpochMilli(userMessage.getTimestamp()).toString());
                            }
                        })
                )
                .apply(
                        "Write to BigQuery",
                        BigQueryIO.writeTableRows()
                                .withoutValidation()
                                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_NEVER)
                                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
                                .withMethod(BigQueryIO.Write.Method.STREAMING_INSERTS)
                                .to(options.getOutputTableSpec())
                );

        return pipeline.run();
    }
}
