## Pre-requisites

The project uses [Leiningen 2](https://leiningen.org) and requires ElasticSearch `1.4.x` or more recent to be running
locally.

`script.disable_dynamic` must be `false` for scripting tests to pass. To set this, add the following to your `elasticsearch.yml` file:

    script:
        disable_dynamic: false

Make
sure you have those two installed and then run tests against all supported Clojure versions using

    lein all test


#### Testing with docker

* pull [Elasticsearch](https://hub.docker.com/_/elasticsearch/) image 

```
$> docker pull elasticsearch:2.0.2
```
nb! Tag must match with Elasticsearch version in the `project.clj`

* build and run instance with custom config file

The custom file in the `resources/config/elasticsearch.yml` has all the required settings for full-scale test activated, so you dont have
manually tweak them.

```
docker run -d -p 9200:9200 -p 9300:9300 --name="elastisch-test"  \
	-v "$PWD/resources/config":/usr/share/elasticsearch/config   \
	elasticsearch:2.0.2 \ 
	-Des.node.name="es-test" -Des.network.bind_host=0.0.0.0
```

* set environment variables

```
$> export ES_URL="http://192.168.99.100:9200" ;;for rest-client
$> export ES_CLUSTER_NAME="elasticsearch"
$> export ES_CLUSTER_HOST="192.168.99.100"
```

* run tests


```
lein with-profile dev,1.8 test :only clojurewerkz.elastisch.native-api.count-test
lein with-profile dev,1.8 test :native ;;only native client
lein with-profile dev,1.8 test 	      ;;all the tests with clj1.8
```

## Pull Requests

Then create a branch and make your changes on it. Once you are done with your changes and all
tests pass, write a [good, detailed commit message](http://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html) and submit a pull request on GitHub.

Don't forget to add your changes to `ChangeLog.md` and credit yourself!

