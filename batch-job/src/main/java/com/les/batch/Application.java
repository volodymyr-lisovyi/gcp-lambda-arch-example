package com.les.batch;

import com.google.protobuf.InvalidProtocolBufferException;
import com.les.common.model.Messages;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.Count;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.List;

import static com.les.common.model.Messages.OrderMessage;

public class Application {

    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    /**
     * The {@link Options} class provides the custom execution options passed by the executor at the
     * command-line.
     */
    public interface Options extends PipelineOptions, StreamingOptions {

        @Description("The directory to read files from. Must end with a slash.")
        @Validation.Required
        ValueProvider<String> getInputDirectory();

        void setInputDirectory(ValueProvider<String> value);

    }

    public static void main(String[] args) {
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        options.setStreaming(true);

        PipelineResult.State state = run(options).waitUntilFinish();
        LOG.info("Pipeline state: {}", state);
    }

    public static PipelineResult run(Options options) {

        var pipeline = Pipeline.create(options);
        pipeline.getCoderRegistry().registerCoderForClass(Messages.Type.class, new Coder<Messages.Type>() {
            @Override
            public void encode(Messages.Type value, OutputStream outStream) throws IOException {
                outStream.write(value.getNumber());
            }

            @Override
            public Messages.Type decode(InputStream inStream) throws IOException {
                return Messages.Type.forNumber(inStream.read());
            }

            @Override
            public List<? extends Coder<?>> getCoderArguments() {
                return null;
            }

            @Override
            public void verifyDeterministic() throws NonDeterministicException {
            }
        });

        pipeline
                .apply(
                        "Read data from file(s)",
                        TextIO.read()
                                .from(options.getInputDirectory())
                )
                .apply(
                        "Map strings to OrderMessage",
                        MapElements.via(new SimpleFunction<String, OrderMessage>() {
                            @Override
                            public OrderMessage apply(String input) {
                                try {
                                    OrderMessage orderMessage = OrderMessage.parseFrom(Base64.getDecoder().decode(input));
                                    LOG.debug("Successfully parsed: {}", orderMessage);
                                    return orderMessage;
                                } catch (InvalidProtocolBufferException e) {
                                    LOG.error("Error while parsing OrderMessage: {}", e.getMessage());
                                    throw new RuntimeException(e);
                                }
                            }
                        })
                )
                .apply(
                        "Convert to KV<Type, Long>",
                        MapElements.via(new SimpleFunction<OrderMessage, Messages.Type>() {
                            @Override
                            public Messages.Type apply(OrderMessage input) {
                                return input.getType();
                            }
                        })
                )
                .apply(
                        "Count by type",
                        Count.perElement()
                )
                .apply("Map to String", MapElements.via(new SimpleFunction<KV<Messages.Type, Long>, String>() {
                    @Override
                    public String apply(KV<Messages.Type, Long> input) {
                        return input.getKey().toString() + ":" + input.getValue();
                    }
                }))
                // todo: write metrics to BigTable/BigQuery/etc..
                .apply(
                        "Write result",
                        TextIO
                                .write()
                                .withoutSharding()
                                .to("./orders-count-by-type.txt")
                );

        return pipeline.run();
    }
}
