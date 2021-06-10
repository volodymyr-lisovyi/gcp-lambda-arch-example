# batch job

Example of Apache Beam SDK pipeline for real time processing

## How to run locally
```
./gradlew batch-job:clean batch-job:execute -Dexec.args="--project=gcp-bigdata-313810  --inputDirectory=gs://gcp-lambda-arch-example-orders/orders/  --runner=DirectRunner --region=us-central1"
```

## How to create DataFlow job on GCP
```
./gradlew batch-job:clean batch-job:execute -Dexec.args="--project=gcp-bigdata-313810  --inputDirectory=gs://gcp-lambda-arch-example-orders/orders/  --runner=DataFlowRunner --region=us-central1"
```

## Test job to generate file with encoded messages
```
./gradlew batch-job:clean batch-job:testExecute
```