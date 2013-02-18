(ns clojurewerkz.elastisch.native
  (:refer-clojure :exclude [get])
  (:require [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import [org.elasticsearch.client Client]
           [org.elasticsearch.client.transport TransportClient]
           [org.elasticsearch.common.settings Settings]
           org.elasticsearch.action.ActionFuture
           [org.elasticsearch.action.index IndexRequest]
           [org.elasticsearch.action.get GetRequest]
           [org.elasticsearch.action.admin.indices.exists.indices IndicesExistsRequest]
           [org.elasticsearch.action.admin.indices.create CreateIndexRequest]))



;;
;; Implementation
;;

(def ^{:dynamic true}
  *client*)

;;
;; API
;;

(defn ^Client connect
  "Connects to one or more ElasticSearch cluster nodes using
   TCP/IP communication transport"
  ([]
     (TransportClient.))
  ([pairs]
     (let [tc (TransportClient.)]
       (doseq [[host port] pairs]
         (.addTransportAddress tc (cnv/->socket-transport-address host port)))
       tc))
  ([pairs settings]
     (let [tc (TransportClient. (cnv/->settings settings))]
       (doseq [[host port] pairs]
         (.addTransportAddress tc (cnv/->socket-transport-address host port)))
       tc)))

(defn ^Client connect!
  "Connects to one or more ElasticSearch cluster nodes using
   TCP/IP communication transport"
  [& args]
  (alter-var-root (var *client*) (constantly (apply connect args))))

(defn ^Client connect-to-local-nodes
  "Connects to one or more local ElasticSearch cluster nodes using
   TCP/IP communication transport"
  ([ids]
     (let [tc (TransportClient.)]
       (doseq [id ids]
         (.addTransportAddress tc (cnv/->local-transport-address id)))
       tc))
  ([ids settings]
     (let [tc (TransportClient. (cnv/->settings settings))]
       (doseq [id ids]
         (.addTransportAddress tc (cnv/->local-transport-address id)))
       tc)))


(defn ^ActionFuture index
  "Executes an index action request"
  [^IndexRequest req]
  (.index ^Client *client* req))

(defn ^ActionFuture get
  "Executes a get action request"
  [^GetRequest req]
  (.get ^Client *client* req))

(defn ^ActionFuture admin-index-exists
  "Executes an indices exist request"
  [^IndicesExistsRequest req]
  (-> ^Client *client* .admin .indices (.exists req)))

(defn ^ActionFuture admin-index-create
  "Executes a create index request"
  [^CreateIndexRequest req]
  (-> ^Client *client* .admin .indices (.create req)))
