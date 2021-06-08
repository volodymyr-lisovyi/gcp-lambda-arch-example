# GCP Lambda architecture example

## Modules
1. [common](./common/README.md) - common module for models, utils, etc..
1. [publisher](./publisher/README.md) - simple app to publish events to Pub/Sub
1. [stream-job](./stream-job/README.md) - Apache Beam SDK job for real time processing
1. [batch-job](./batch-job/README.md) - Apache Beam SDK job for batch processing
1. [api-service](./api-service/README.md) - Spring web app that provides API

## How to
1. Export env vars 
```
export GOOGLE_APPLICATION_CREDENTIALS=<path-to-credentials>
export GOOGLE_APPLICATION_PROJECT_ID=<your-project-id>
export GOOGLE_APPLICATION_TOPIC_ID=<your-topic-id>
```

2. Publish message to Pub/Sub
```
./gradlew publisher:run
```
You should see something like
```
> Task :publisher:run
Pub/Sub message id: 2507399245914814
```
