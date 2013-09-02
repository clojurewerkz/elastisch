(ns clojurewerkz.elastisch.test.helpers
  (:require [clojurewerkz.elastisch.native :as es]))

(defn ci?
  "Returns true if tests are running in the CI environment
   (on travis-ci.org)"
  []
  (System/getenv "CI"))

(defn infer-cluster-name
  "Returns current cluster name set via the ES_CLUSTER_NAME env variable"
  []
  (get (System/getenv) "ES_CLUSTER_NAME" "elasticsearch_antares"))

(defn connect-native-client
  ([]
     (connect-native-client (infer-cluster-name)))
  ([cluster-name]
     (es/connect! [["127.0.0.1" 9300]]
                  {"cluster.name" cluster-name })))

(defn maybe-connect-native-client
  []
  (when (not (es/connected?))
    (connect-native-client)))
