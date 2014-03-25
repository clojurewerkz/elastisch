;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native
  (:refer-clojure :exclude [get count])
  (:require [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.client.transport.TransportClient
           org.elasticsearch.common.settings.Settings
           [org.elasticsearch.node NodeBuilder Node]
           ;; Actions
           org.elasticsearch.action.ActionFuture
           org.elasticsearch.action.index.IndexRequest
           [org.elasticsearch.action.get GetRequest MultiGetRequest]
           org.elasticsearch.action.delete.DeleteRequest
           org.elasticsearch.action.update.UpdateRequest
           org.elasticsearch.action.deletebyquery.DeleteByQueryRequest
           org.elasticsearch.action.count.CountRequest
           [org.elasticsearch.action.search SearchRequest SearchScrollRequest]
           org.elasticsearch.action.mlt.MoreLikeThisRequest
           [org.elasticsearch.action.percolate PercolateRequest PercolateResponse]
           ;; Admin Client
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
           org.elasticsearch.action.admin.indices.create.CreateIndexRequest
           org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
           org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest
           org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
           org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest
           org.elasticsearch.action.admin.indices.open.OpenIndexRequest
           org.elasticsearch.action.admin.indices.close.CloseIndexRequest
           org.elasticsearch.action.admin.indices.optimize.OptimizeRequest
           org.elasticsearch.action.admin.indices.flush.FlushRequest
           org.elasticsearch.action.admin.indices.refresh.RefreshRequest
           org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest
           org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest
           org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest
           org.elasticsearch.action.admin.indices.status.IndicesStatusRequest
           org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequest
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest
           org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest))



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
   TCP/IP communication transport. Returns the client."
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
   TCP/IP communication transport. Alters the *client* var root."
  [& args]
  (alter-var-root (var *client*) (constantly (apply connect args))))

(defn ^Node build-local-node
  [settings]
  (let [is (cnv/->settings settings)
        nb (.. NodeBuilder nodeBuilder
               (settings is)
               (client true))]
    (.build ^NodeBuilder nb)))

(defn ^Thread start-local-node
  [^Node node]
  (.start node)
  node)

(defn ^Client connect-to-local-node
  "Connects to a local ElasticSearch cluster nodes using
   local transport. Returns the client. Supposed to be used for automated testing."
  [^Node node]
  (.client node))

(defn ^Client connect-to-local-node!
  "Connects to one or more local ElasticSearch cluster nodes using
   local transport. Returns the client. Alters the *client* var root.
   Supposed to be used for automated testing."
  [^Node node]
  (alter-var-root (var *client*) (constantly (connect-to-local-node node))))

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

(defn ^ActionFuture update
  "Executes a update action request"
  [^UpdateRequest req]
  (.update ^Client *client* req))

(defn ^ActionFuture delete
  "Executes a delete action request"
  [^DeleteRequest req]
  (.delete ^Client *client* req))

(defn ^ActionFuture delete-by-query
  "Executes a delete by query action request"
  [^DeleteByQueryRequest req]
  (.deleteByQuery ^Client *client* req))

(defn ^ActionFuture count
  "Executes a count action request"
  [^CountRequest req]
  (.count ^Client *client* req))

(defn ^ActionFuture search
  "Executes a search action request"
  [^SearchRequest req]
  (.search ^Client *client* req))

(defn ^ActionFuture search-scroll
  "Executes a search action request"
  [^SearchScrollRequest req]
  (.searchScroll ^Client *client* req))

(defn ^ActionFuture more-like-this
  "Executes a more-like-this action request"
  [^MoreLikeThisRequest req]
  (.moreLikeThis ^Client *client* req))

(defn ^ActionFuture percolate
  "Executes a more-like-this action request"
  [^PercolateRequest req]
  (.percolate ^Client *client* req))


;;
;; Admin Client
;;

(defn ^ActionFuture admin-index-exists
  "Executes an indices exist request"
  [^IndicesExistsRequest req]
  (-> ^Client *client* .admin .indices (.exists req)))

(defn ^ActionFuture admin-types-exists
  "Executes an types exist request"
  [^TypesExistsRequest req]
  (-> ^Client *client* .admin .indices (.typesExists req)))

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

(defn ^ActionFuture admin-get-mappings
  "Executes a get mapping request"
  [^GetMappingsRequest req]
  (-> ^Client *client* .admin .indices (.getMappings req)))

(defn ^ActionFuture admin-put-mapping
  "Executes a put mapping request"
  [^PutMappingRequest req]
  (-> ^Client *client* .admin .indices (.putMapping req)))

(defn ^ActionFuture admin-delete-mapping
  "Executes a delete mapping request"
  [^DeleteMappingRequest req]
  (-> ^Client *client* .admin .indices (.deleteMapping req)))

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
