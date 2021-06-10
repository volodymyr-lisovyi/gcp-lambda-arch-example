package com.les.api.service;

import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.JobId;
import com.google.cloud.bigquery.JobInfo;
import com.google.cloud.bigquery.QueryJobConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OrdersBigQueryService extends BigQueryService {

    public static Logger LOG = LoggerFactory.getLogger(OrdersBigQueryService.class);

    public OrdersBigQueryService(BigQuery bigQuery) {
        super(bigQuery);
    }

    public Mono<Long> getOrdersCount() {
        return Mono.just(
                QueryJobConfiguration.newBuilder(
                        "SELECT count(*) FROM `gcp-bigdata-313810.order_events_dataset.orders`"
                ).build()
        )
                .map(
                        queryJobConfiguration -> bigQuery.create(
                                JobInfo.newBuilder(queryJobConfiguration).setJobId(JobId.of(UUID.randomUUID().toString())).build()
                        )
                )
                .map(
                        queryJob -> {
                            try {
                                return queryJob.waitFor();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .map(
                        queryJob -> {
                            try {
                                return queryJob.getQueryResults();
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .map(
                        result -> result.iterateAll().iterator().next().get(0).getLongValue()
                );
    }
}
