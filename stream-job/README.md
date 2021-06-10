# stream job

Example of Apache Beam SDK pipeline for real time processing

## How to run locally
```
./gradlew stream-job:clean stream-job:execute -Dexec.args="--project=gcp-bigdata-313810 --inputSubscription=projects/gcp-bigdata-313810/subscriptions/orders-sub --outputTableSpec=gcp-bigdata-313810:order_events_dataset.orders --outputDirectory=gs://gcp-lambda-arch-example-orders/orders  --runner=DirectRunner --region=us-central1"
```

## How to create DataFlow job on GCP
```
./gradlew stream-job:clean stream-job:execute -Dexec.args="--project=gcp-bigdata-313810 --inputSubscription=projects/gcp-bigdata-313810/subscriptions/orders-sub --outputTableSpec=gcp-bigdata-313810:order_events_dataset.orders --outputDirectory=gs://gcp-lambda-arch-example-orders/orders  --runner=DataFlowRunner --region=us-central1"
```