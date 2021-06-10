package com.les.api.service;

import com.google.cloud.bigquery.BigQuery;


public abstract class BigQueryService {

    protected final BigQuery bigQuery;

    public BigQueryService(BigQuery bigQuery) {
        this.bigQuery = bigQuery;
    }
}
