# ContentDiff service

## Description
Microservice that provides functionality to compare two base64 encoded content.

It exposes three REST endpoints:
- _**left endpoint**_ - used to put/update *left* content
- _**right endpoint**_ - used to put/update *right* content
- _**diff endpoint**_ - used to get difference between *left* and *right* content.

_Left_ and _right_ content that want to be compared are bound using unique content ID.

## Pre-requirements
Pre-requirements to successfully build and run ContentDiff service are:
- Linux/Windows
- Oracle JDK 8

## Build
Lightbend Activator is used for building. Alternatively, if SBT is installed on machine, it can be directly used instead.
If on Linux machine, make sure Activator script has sufficient permissions to be run:
```sh
$ chmod 777 bin/activator
```

Command to build on Linux:
```sh
$ bin/activator compile scalastyle jacoco:check jacoco:report it:test doc universal:packageBin
```
Windows:
```bat
> bin\activator.bat compile scalastyle jacoco:check jacoco:report it:test doc universal:packageBin
```

This will:
- compile source code located in _src/main_ folder
- run Scala style checker against code located in _src/main_ folder
- compile and run unit tests located in _src/test_ folder
- run code coverage analysis and generate coverage report _target/scala-2.12/jacoco/html/index.html_
- compile and run integration tests located in _src/it_ folder
- generate API docs that can be found in _target/scala-2.12/api_ folder
- generate jar, source jar and pom files:
    - _target/scala-2.12/content-diff_2.12-1.0.0.jar_
    - _target/scala-2.12/content-diff_2.12-1.0.0-sources.jar_
    - _target/scala-2.12/content-diff_2.12-1.0.0.pom_
- create installation zip file _target/universal/content-diff-1.0.0.zip_
	
## Run using Lightbend Activator
Linux:
```sh
$ bin/activator run
```
Windows:
```bat
> bin\activator.bat run
```

## Install
Installation consists of extracting installation zip file _target/universal/content-diff-1.0.0.zip_ to arbitrary location.

## Run
Goto _content-diff-1.0.0/bin_ folder and run _content-diff_ script.<br />
Linux:
```sh
$ ./content-diff
```
Windows:
```bat
> content-diff.bat
```

## Usage
- To put/update content on _left_ endpoint use HTTP PUT request<br />
_**url:**_ 	_http://<host>:8080/v1/diff/{content_id}/left_<br />
_**body (JSON):**_
```json
{
	"data": "<content>"
}
```
- To put/update content on _right_ endpoint use HTTP PUT request<br />
_**url**_: 	_http://<host>:8080/v1/diff/{content_id}/right_<br />
_**body (JSON):**_
```json
{
	"data": "<content>"
}
```
- To get difference between _'left'_ and _'right'_ content use HTTP GET request<br />
_**url:**_ _http://<host>:8080/v1/diff/{content_id}_

## Examples
- Putting content with ID 1 using _left_ endpoint<br />
_**url:**_:	_http://localhost:8080/v1/diff/1/left_<br />
_**method:**_ PUT<br />
_**body (JSON):**_
```json
{
	"data": "AAAAAA=="
}
```
- Get difference between _left_ and _right_ content with ID 1<br />
_**url:**_: _http://localhost:8080/v1/diff/1_<br />
_**method:**_ GET

## Design
Microservice consists of three major components:
- _**ContentDiffController**_: REST controller that exposes left, right and diff endpoints to accept client requests. See `org.content.diff.ContentDiffController` scaladoc for more info.
- _**ContentDiffService**_: Contains logic to process content. See `org.content.diff.ContentDiffService` scaladoc for more info.
- _**ReplyService**_: Provides mechanism to publish results of the request processing to client. See `org.content.diff.ReplyService` scaladoc for more info.
	
Components use following messages to communicate with each other:
- _**LeftRequest**_: put/update _left_ content
- _**RightRequest**_: put/update _right_ content
- _**DiffRequest**_: get difference between _left_ and _right_ content
- _**LeftRightResponse**_: response for LeftRequest and RightRequest
- _**DiffResponse**_: response for DiffRequest.

See `org.content.diff.ContentDiffMessages` scaladoc for more info.

## Improvements
Possible future improvements:
1) Refactor ContentProcessor actor system to handle multiply instances of `ContentProcessor` actor (e.g. as router or cluster solution), each one with its own internal cache responsible to handle content with specific range of IDs. In this way it would be possible to simultaneously handle several requests for content processing and at a same time maintain cache consistency.
2) Provide cache expiry functionality so that content would not be stored 'forever' in internal actor cache.

## License
Free software. Enjoy :)
