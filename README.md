# GCP Lambda architecture example

## Modules
1. [common](./common/README.md) - common module for models, utils, etc..
1. [publisher](./publisher/README.md) - simple app to publish events to Pub/Sub
1. [stream-job](./stream-job/README.md) - Apache Beam SDK job for real time processing
1. [batch-job](./batch-job/README.md) - Apache Beam SDK job for batch processing
1. [api-service](./api-service/README.md) - Spring web app that provides API