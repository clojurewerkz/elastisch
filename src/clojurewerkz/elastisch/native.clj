(ns clojurewerkz.elastisch.native
  (:refer-clojure :exclude [get])
  (:require [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.client.transport.TransportClient
           org.elasticsearch.common.settings.Settings
           org.elasticsearch.action.ActionFuture
           org.elasticsearch.action.index.IndexRequest
           org.elasticsearch.action.get.GetRequest
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
           org.elasticsearch.action.admin.indices.create.CreateIndexRequest
           org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
           org.elasticsearch.action.admin.indices.open.OpenIndexRequest
           org.elasticsearch.action.admin.indices.close.CloseIndexRequest
           org.elasticsearch.action.admin.indices.optimize.OptimizeRequest
           org.elasticsearch.action.admin.indices.flush.FlushRequest
           org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest
           org.elasticsearch.action.admin.indices.settings.UpdateSettingsRequest
           org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest
           org.elasticsearch.action.admin.indices.status.IndicesStatusRequest
           org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequest
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest))



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

(defn connected?
  "Returns true if current client is connected (*client* is bound)"
  []
  (bound? (var *client*)))



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

(defn ^ActionFuture admin-index-delete
  "Executes a delete index request"
  [^DeleteIndexRequest req]
  (-> ^Client *client* .admin .indices (.delete req)))

(defn ^ActionFuture admin-update-index-settings
  "Executes an update index settings request"
  [^UpdateSettingsRequest req]
  (-> ^Client *client* .admin .indices (.updateSettings req)))

(defn ^ActionFuture admin-open-index
  "Executes an open index request"
  [^OpenIndexRequest req]
  (-> ^Client *client* .admin .indices (.open req)))

(defn ^ActionFuture admin-close-index
  "Executes a close index request"
  [^CloseIndexRequest req]
  (-> ^Client *client* .admin .indices (.close req)))

(defn ^ActionFuture admin-optimize-index
  "Executes a optimize index request"
  [^OptimizeRequest req]
  (-> ^Client *client* .admin .indices (.optimize req)))

(defn ^ActionFuture admin-flush-index
  "Executes a flush index request"
  [^FlushRequest req]
  (-> ^Client *client* .admin .indices (.flush req)))

(defn ^ActionFuture admin-gateway-snapshot
  "Executes a gateway snapshot request"
  [^GatewaySnapshotRequest req]
  (-> ^Client *client* .admin .indices (.gatewaySnapshot req)))

(defn ^ActionFuture admin-clear-cache
  "Executes a cache clear request"
  [^ClearIndicesCacheRequest req]
  (-> ^Client *client* .admin .indices (.clearCache req)))

(defn ^ActionFuture admin-status
  "Executes a status request"
  [^IndicesStatusRequest req]
  (-> ^Client *client* .admin .indices (.status req)))

(defn ^ActionFuture admin-index-stats
  "Executes an indices stats request"
  [^IndicesStatsRequest req]
  (-> ^Client *client* .admin .indices (.stats req)))

(defn ^ActionFuture admin-index-segments
  "Executes an indices segments request"
  [^IndicesSegmentsRequest req]
  (-> ^Client *client* .admin .indices (.segments req)))

(defn ^ActionFuture admin-update-aliases
  "Executes an update aliases request"
  [^IndicesAliasesRequest req]
  (-> ^Client *client* .admin .indices (.aliases req)))
