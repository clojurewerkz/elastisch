(ns clojurewerkz.elastisch.native
  (:refer-clojure :exclude [get count])
  (:require [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.client.transport.TransportClient
           org.elasticsearch.common.settings.Settings
           ;; Actions
           org.elasticsearch.action.ActionFuture
           org.elasticsearch.action.index.IndexRequest
           [org.elasticsearch.action.get GetRequest MultiGetRequest]
           org.elasticsearch.action.delete.DeleteRequest
           org.elasticsearch.action.count.CountRequest
           org.elasticsearch.action.search.SearchRequest
           ;; Admin Client
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
           org.elasticsearch.action.admin.indices.create.CreateIndexRequest
           org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
           org.elasticsearch.action.admin.indices.open.OpenIndexRequest
           org.elasticsearch.action.admin.indices.close.CloseIndexRequest
           org.elasticsearch.action.admin.indices.optimize.OptimizeRequest
           org.elasticsearch.action.admin.indices.flush.FlushRequest
           org.elasticsearch.action.admin.indices.refresh.RefreshRequest
           org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest
           org.elasticsearch.action.admin.indices.settings.UpdateSettingsRequest
           org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest
           org.elasticsearch.action.admin.indices.status.IndicesStatusRequest
           org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequest
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest))



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



;;
;; Core
;;

(defn ^ActionFuture index
  "Executes an index action request"
  [^IndexRequest req]
  (.index ^Client *client* req))

(defn ^ActionFuture get
  "Executes a get action request"
  [^GetRequest req]
  (.get ^Client *client* req))

(defn ^ActionFuture multi-get
  "Executes a multi-get action request"
  [^MultiGetRequest req]
  (.multiGet ^Client *client* req))

(defn ^ActionFuture delete
  "Executes a delete action request"
  [^DeleteRequest req]
  (.delete ^Client *client* req))

(defn ^ActionFuture count
  "Executes a count action request"
  [^CountRequest req]
  (.count ^Client *client* req))

(defn ^ActionFuture search
  "Executes a search action request"
  [^SearchRequest req]
  (.search ^Client *client* req))


;;
;; Admin Client
;;

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

(defn ^ActionFuture admin-refresh-index
  "Executes a refresh index request"
  [^RefreshRequest req]
  (-> ^Client *client* .admin .indices (.refresh req)))

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

(defn ^ActionFuture admin-put-index-template
  "Executes a put index template request"
  [^PutIndexTemplateRequest req]
  (-> ^Client *client* .admin .indices (.putTemplate req)))

(defn ^ActionFuture admin-delete-index-template
  "Executes a delete index template request"
  [^DeleteIndexTemplateRequest req]
  (-> ^Client *client* .admin .indices (.deleteTemplate req)))
