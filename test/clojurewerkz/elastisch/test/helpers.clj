(ns clojurewerkz.elastisch.test.helpers
  (:require [clojurewerkz.elastisch.native :as es])
  (:import  [org.elasticsearch.node NodeBuilder]
            [org.elasticsearch.common.settings ImmutableSettings]))

(defn infer-cluster-name
  "Returns current cluster name set via the ES_CLUSTER_NAME env variable"
  []
  (get (System/getenv) "ES_CLUSTER_NAME" "elasticsearch_antares"))

(def ^{:dynamic true}
  *local-node*)

(defn start-local-node []
  "Start an embedded data node for the tests"
  (let [tcp-port     9301
        our-settings (.. ImmutableSettings
                         settingsBuilder
                         (put "gateway.type" "none")
                         (put "transport.tcp.port" tcp-port)
                         (put "multicast.enabled" false)
                         build)
        node (.. NodeBuilder
                 nodeBuilder
                 (settings our-settings)
                 (clusterName (infer-cluster-name))
                 build)]
    (. node start)
    (alter-var-root (var *local-node*) (constantly node))
    node))

(defn connect-native-client
  ([]
     (connect-native-client (infer-cluster-name)))
  ([cluster-name]
     (es/connect! [["127.0.0.1" 9301]]
                  {"cluster.name" cluster-name })))

(defn maybe-connect-native-client
  []
  (when (not (bound? (var *local-node*)))
    (start-local-node)
    ;; TODO - use client straight off the node?
    ;; (. node client)
    )
  (when (not (es/connected?))
    (connect-native-client))
  (println "ES_CLUSTER_NAME env variable is not set. Please set it to your local ES cluster name."))
