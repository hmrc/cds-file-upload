# cds-file-upload

## About
This microservice is part of the Customs Declaration Service (CDS) Secure File Upload Service (SFUS). It is designed to work in tandem with the [cds-file-upload-frontend](https://github.com/hmrc/cds-file-upload-frontend) service.

It provides two functions for the frontend:
- It receives the Upscan scan result for each uploaded file (sent via the [Notification Service](https://github.com/hmrc/customs-notification)), stores it in Mongo, and returns it to the frontend by file reference. This lets the frontend check that every file has a success notification before it shows the File Receipt page.
- It returns the verified email address for the signed-in user's EORI, which it gets from [customs-data-store](https://github.com/hmrc/customs-data-store).

Stored notifications are deleted automatically after one hour (see [Config settings](#config-settings)).

| Key | Value |
|---|---|
| Digital service | CDS Exports |
| Local port | `6795` |
| Base path | `/cds-file-upload` (public) and `/internal` (MDTP only) |
| Front-end | [cds-file-upload-frontend](https://github.com/hmrc/cds-file-upload-frontend) (port `6793`) |
| Acceptance tests | [sfus-acceptance-ui-journey-tests](https://github.com/hmrc/sfus-acceptance-ui-journey-tests) |
| Performance tests | [cds-file-upload-performance-tests](https://github.com/hmrc/cds-file-upload-performance-tests) |
| Stubs used in Local and Staging | [customs-declarations-stub](https://github.com/hmrc/customs-declarations-stub) |

## How to Run This Service

### Prerequisites
Use [Service Manager](#service-manager-profiles) to start all the services that SFUS depends on:

```bash
sm2 --start CDS_FUF_ALL
```

You also need a local MongoDB running on port `27017`.

### Running the Service Locally
To run this service locally, stop the instance started by Service Manager and run it with sbt:

```bash
sm2 --stop CDS_FILE_UPLOAD
sbt run
```

The service starts on default port `6795`.

To test this service through the frontend, refer to the [cds-file-upload-frontend](https://github.com/hmrc/cds-file-upload-frontend#running-the-service-locally) repository's README. When the frontend runs with its test-only routes, its fake S3 endpoint sends a notification to `POST /internal/notification` on this service for every uploaded file.

## How to Test This Service

### Local

#### Unit Tests
```bash
sbt test
```

#### Integration Tests
The integration tests need a local MongoDB on port `27017`. Downstream services are stubbed with WireMock on port `11111`, which you can change with the `WIREMOCK_SERVICE_LOCATOR_PORT` environment variable.

```bash
sbt it/test
```

#### Pre-Push Check
There is a script called `precheck.sh` that formats the code, runs all unit and integration tests, and examines their coverage (minimum 78% statement coverage). It also checks that all files are properly formatted.
It is good practice to run it just before pushing to GitHub.

```bash
./precheck.sh
```

#### Acceptance Tests (smoke and regression)
This service has no UI. It is tested end to end through [cds-file-upload-frontend](https://github.com/hmrc/cds-file-upload-frontend) by [sfus-acceptance-ui-journey-tests](https://github.com/hmrc/sfus-acceptance-ui-journey-tests). The smoke and regression packs cover the file upload journey (MRN entry, contact details, number of files, upload and File Receipt page), which depends on this service's notification endpoints, and the secure messaging journeys.

For how to run each test pack, the Service Manager profile it uses, and the full list of scenarios, see that repository's README.

#### Performance tests
The performance tests for this service are in [cds-file-upload-performance-tests](https://github.com/hmrc/cds-file-upload-performance-tests). For how to run them and which environments they run against, see that repository's README.

### Manual Testing in QA and Staging

After the successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/cds-file-upload-pipeline/), manual testing is performed in the QA and/or Staging environments, see [Manual Service Verification in QA and Staging](https://github.com/hmrc/sfus-acceptance-ui-journey-tests).

## Service Catalogue
- [cds-file-upload in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/cds-file-upload)

## Jenkins Pipeline
- [cds-file-upload build](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSExports/job/cds-file-upload/)

- The acceptance test Jenkins jobs (smoke and regression) are listed in the [sfus-acceptance-ui-journey-tests README](https://github.com/hmrc/sfus-acceptance-ui-journey-tests).
- The performance test Jenkins jobs are listed in the [cds-file-upload-performance-tests README](https://github.com/hmrc/cds-file-upload-performance-tests).

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile | Use | Services started |
|---|---|---|
| `CDS_FUF_ALL` | Running and developing this service and [cds-file-upload-frontend](https://github.com/hmrc/cds-file-upload-frontend) locally | `CDS_FILE_UPLOAD`, `CDS_FILE_UPLOAD_FRONTEND`, `AUTH`, `AUTH_LOGIN_STUB`, `AUTH_LOGIN_API`, `USER_DETAILS`, `CUSTOMS_DECLARATIONS_STUB`, `IDENTITY_VERIFICATION`, `TRACKING_CONSENT_FRONTEND`, `BAS_GATEWAY`, `BAS_GATEWAY_FRONTEND` |
| `CDS_SFUS_ATS` | Running the [acceptance tests](https://github.com/hmrc/sfus-acceptance-ui-journey-tests) | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json) |
| `CDS_EXPORTS_ALL` | All CDS Exports services (declarations, movements, file upload and internal) | See [service-manager-config](https://github.com/hmrc/service-manager-config/blob/main/profiles.json) |

| Service | Port | Repository |
|---|---|---|
| `CDS_FILE_UPLOAD` | 6795 | This service |
| `CDS_FILE_UPLOAD_FRONTEND` | 6793 | [cds-file-upload-frontend](https://github.com/hmrc/cds-file-upload-frontend) |
| `CUSTOMS_DECLARATIONS_STUB` | 6790 | [customs-declarations-stub](https://github.com/hmrc/customs-declarations-stub) (stubs customs-data-store locally) |

## Endpoints

All paths are relative to the service root (`http://localhost:6795` when running locally). The `/cds-file-upload` endpoints need an [authenticated user with the HMRC-CUS-ORG enrolment](https://github.com/hmrc/sfus-acceptance-ui-journey-tests).

| Method | Path | Purpose | Sample request | Response |
|---|---|---|---|---|
| GET | `/cds-file-upload/notification/:reference` | Get the Upscan notification for an uploaded file | `/cds-file-upload/notification/e4d94295-52b1-4837-bdc0-7ab8d7b0f1af` | `200` with the notification JSON<br>Not found: `404` |
| GET | `/cds-file-upload/eori-email` | Get the verified email address for the authenticated user's EORI | – | `200` with email and deliverability status<br>Not found: `404`<br>Downstream failure: `500` |
| POST | `/internal/notification` | Receive an Upscan notification from customs-notification. *Internal to MDTP and needs no authentication* | XML body – see example below | `202`<br>Failed to save: `500`<br>Malformed XML: `400`<br>Content type is not XML: `415` |

### Example payloads

#### File Upload Notification

`GET /cds-file-upload/notification/e4d94295-52b1-4837-bdc0-7ab8d7b0f1af`

Example response:

```json
{
  "fileReference": "e4d94295-52b1-4837-bdc0-7ab8d7b0f1af",
  "outcome": "SUCCESS",
  "filename": "sample.pdf",
  "createdAt": "2026-10-15T10:30:00Z"
}
```

`outcome` is `SUCCESS` or `FAILURE`.

#### EORI Email

`GET /cds-file-upload/eori-email`

Example response:

```json
{
  "address": "some@email.com",
  "deliverable": true
}
```

`deliverable` is `false` when customs-data-store has marked the email address as undeliverable.

#### Notification Callback

`POST /internal/notification`

```text
Content-Type: application/xml
```

```xml
<Root>
  <FileReference>e4d94295-52b1-4837-bdc0-7ab8d7b0f1af</FileReference>
  <BatchId>5e634e09-77f6-4ff1-b92a-8a9676c715c4</BatchId>
  <FileName>sample.pdf</FileName>
  <Outcome>SUCCESS</Outcome>
  <Details>[detail block]</Details>
</Root>
```

`FileReference` and `Outcome` are required. `FileName` is optional. If the XML does not contain the required elements, the raw payload is still stored (without details) and a warning is logged.

A successfully stored notification returns:

```text
202 Accepted
```

To send it locally:

```bash
curl --location --request POST 'http://localhost:6795/internal/notification' --header 'Content-Type: application/xml' --data-raw '<Root><FileReference>e4d94295-52b1-4837-bdc0-7ab8d7b0f1af</FileReference><FileName>sample.pdf</FileName><Outcome>SUCCESS</Outcome></Root>'
```

## Developer Notes

### Feature Flags
This service has no feature flags.

### Config settings
The following setting determines how long notifications are stored before they are deleted automatically:

```
notifications {
  ttl-seconds = 3600
}
```

- `ttl-seconds`: this is the Time To Live, in seconds, of the `createdAt` index on the `notifications` collection. It cannot be changed after the initial deployment without manually dropping the index.

### Scalafmt
The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

## License
This code is open source software licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
