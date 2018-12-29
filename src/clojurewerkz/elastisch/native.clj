;; Copyright 2011-2016 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns clojurewerkz.elastisch.native
  (:refer-clojure :exclude [get count update])
  (:require [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.client.transport.TransportClient
           (org.elasticsearch.node NodeBuilder Node)
           ;; Actions
           org.elasticsearch.action.ActionFuture
           org.elasticsearch.action.index.IndexRequest
           (org.elasticsearch.action.get GetRequest MultiGetRequest)
           org.elasticsearch.action.delete.DeleteRequest
           org.elasticsearch.action.update.UpdateRequest
           org.elasticsearch.action.count.CountRequest
           (org.elasticsearch.action.search SearchRequest SearchScrollRequest
            MultiSearchRequest)
           (org.elasticsearch.action.percolate PercolateRequest)
           (org.elasticsearch.action.suggest SuggestRequest)
           ;; Admin Client
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest
           org.elasticsearch.action.admin.indices.create.CreateIndexRequest
           org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest
           org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest
           org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest
           org.elasticsearch.action.admin.indices.open.OpenIndexRequest
           org.elasticsearch.action.admin.indices.close.CloseIndexRequest
           org.elasticsearch.action.admin.indices.forcemerge.ForceMergeRequest

           org.elasticsearch.action.admin.indices.flush.FlushRequest
           org.elasticsearch.action.admin.indices.refresh.RefreshRequest
           org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest
           org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest
           org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest
           org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequest
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest
           org.elasticsearch.action.admin.indices.alias.get.GetAliasesRequest
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest
           org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest
           org.elasticsearch.action.admin.cluster.repositories.put.PutRepositoryRequest
           org.elasticsearch.action.admin.cluster.snapshots.create.CreateSnapshotRequest
           org.elasticsearch.action.admin.cluster.snapshots.delete.DeleteSnapshotRequest))

;;
;; Core
;;

(defn ^ActionFuture index
  "Executes an index action request"
  [^Client conn ^IndexRequest req]
  (.index ^Client conn req))

(defn ^ActionFuture get
  "Executes a get action request"
  [^Client conn ^GetRequest req]
  (.get ^Client conn req))

(defn ^ActionFuture multi-get
  "Executes a multi-get action request"
  [^Client conn ^MultiGetRequest req]
  (.multiGet ^Client conn req))

(defn ^ActionFuture update
  "Executes an update action request"
  [^Client conn ^UpdateRequest req]
  (.update ^Client conn req))

(defn ^ActionFuture delete
  "Executes a delete action request"
  [^Client conn ^DeleteRequest req]
  (.delete ^Client conn req))

(defn ^ActionFuture count
  "Executes a count action request"
  [^Client conn ^CountRequest req]
  (.count ^Client conn req))

(defn ^ActionFuture search
  "Executes a search action request"
  [^Client conn ^SearchRequest req]
  (.search ^Client conn req))

(defn ^ActionFuture multi-search
  [^Client conn ^MultiSearchRequest req]
  (.multiSearch conn req))

(defn ^ActionFuture search-scroll
  "Executes a search action request"
  [^Client conn ^SearchScrollRequest req]
  (.searchScroll ^Client conn req))

(defn ^ActionFuture percolate
  "Executes a more-like-this action request"
  [^Client conn ^PercolateRequest req]
  (.percolate ^Client conn req))

(defn ^ActionFuture suggest
  "Executes a suggestion action request"
  [^Client conn, ^SuggestRequest req]
  (.suggest ^Client conn req))


;;
;; Admin Client
;;

(defn ^ActionFuture admin-index-exists
  "Executes an indices exist request"
  [^Client conn ^IndicesExistsRequest req]
  (-> ^Client conn .admin .indices (.exists req)))

(defn ^ActionFuture admin-types-exists
  "Executes a types exist request"
  [^Client conn ^TypesExistsRequest req]
  (-> ^Client conn .admin .indices (.typesExists req)))

(defn ^ActionFuture admin-index-create
  "Executes a create index request"
  [^Client conn ^CreateIndexRequest req]
  (-> ^Client conn .admin .indices (.create req)))

(defn ^ActionFuture admin-index-delete
  "Executes a delete index request"
  [^Client conn ^DeleteIndexRequest req]
  (-> ^Client conn .admin .indices (.delete req)))

(defn ^ActionFuture admin-update-index-settings
  "Executes an update index settings request"
  [^Client conn ^UpdateSettingsRequest req]
  (-> ^Client conn .admin .indices (.updateSettings req)))

(defn ^ActionFuture admin-get-index-settings
  "Executes an update index settings request"
  [^Client conn ^GetSettingsRequest req]
  (-> ^Client conn .admin .indices (.getSettings req)))

(defn ^ActionFuture admin-get-mappings
  "Executes a get mapping request"
  [^Client conn ^GetMappingsRequest req]
  (-> ^Client conn .admin .indices (.getMappings req)))

(defn ^ActionFuture admin-put-mapping
  "Executes a put mapping request"
  [^Client conn ^PutMappingRequest req]
  (-> ^Client conn .admin .indices (.putMapping req)))

(defn ^ActionFuture admin-open-index
  "Executes an open index request"
  [^Client conn ^OpenIndexRequest req]
  (-> ^Client conn .admin .indices (.open req)))

(defn ^ActionFuture admin-close-index
  "Executes a close index request"
  [^Client conn ^CloseIndexRequest req]
  (-> ^Client conn .admin .indices (.close req)))

;;DEPRECATED in 2.1- replaced with forceMerge
(defn ^ActionFuture admin-merge-index
  "Executes a optimize index request"
  [^Client conn ^ForceMergeRequest req]
  (-> ^Client conn .admin .indices (.forceMerge req)))

(defn ^ActionFuture admin-flush-index
  "Executes a flush index request"
  [^Client conn ^FlushRequest req]
  (-> ^Client conn .admin .indices (.flush req)))

(defn ^ActionFuture admin-refresh-index
  "Executes a refresh index request"
  [^Client conn ^RefreshRequest req]
  (-> ^Client conn .admin .indices (.refresh req)))

(defn ^ActionFuture admin-put-repository
  "Executes a put repository request"
  [^Client conn ^PutRepositoryRequest req]
  (-> ^Client conn .admin .cluster (.putRepository req)))

(defn ^ActionFuture admin-create-snapshot
  "Executes a create snapshot request"
  [^Client conn ^CreateSnapshotRequest req]
  (-> ^Client conn .admin .cluster (.createSnapshot req)))

(defn ^ActionFuture admin-delete-snapshot
  "Executes a delete snapshot request"
  [^Client conn ^DeleteSnapshotRequest req]
  (-> ^Client conn .admin .cluster (.deleteSnapshot req)))

(defn ^ActionFuture admin-clear-cache
  "Executes a cache clear request"
  [^Client conn ^ClearIndicesCacheRequest req]
  (-> ^Client conn .admin .indices (.clearCache req)))

(defn ^ActionFuture admin-index-stats
  "Executes an indices stats request"
  [^Client conn ^IndicesStatsRequest req]
  (-> ^Client conn .admin .indices (.stats req)))

(defn ^ActionFuture admin-index-segments
  "Executes an indices segments request"
  [^Client conn ^IndicesSegmentsRequest req]
  (-> ^Client conn .admin .indices (.segments req)))

(defn ^ActionFuture admin-get-aliases
  "Executes a get aliases request"
  [^Client conn ^GetAliasesRequest req]
  (-> ^Client conn .admin .indices (.getAliases req)))

(defn ^ActionFuture admin-update-aliases
  "Executes an update aliases request"
  [^Client conn ^IndicesAliasesRequest req]
  (-> ^Client conn .admin .indices (.aliases req)))

(defn ^ActionFuture admin-put-index-template
  "Executes a put index template request"
  [^Client conn ^PutIndexTemplateRequest req]
  (-> ^Client conn .admin .indices (.putTemplate req)))

(defn ^ActionFuture admin-delete-index-template
  "Executes a delete index template request"
  [^Client conn ^DeleteIndexTemplateRequest req]
  (-> ^Client conn .admin .indices (.deleteTemplate req)))


;;
;; API
;;

(defn ^Client connect
  "Connects to one or more Elasticsearch cluster nodes using
  TCP/IP communication transport. Returns the client."
  ([]
     (.build (TransportClient/builder)))
  ([pairs]
     (let [tc (.build (TransportClient/builder))]
       (doseq [[host port] pairs]
         (.addTransportAddress tc (cnv/->socket-transport-address host port)))
       tc))
  ([pairs settings]
     (let [tcb (doto (TransportClient/builder)
                 (.settings (cnv/->settings settings)))
           tc (.build tcb)]
       (doseq [[host port] pairs]
         (.addTransportAddress tc (cnv/->socket-transport-address host port)))
       tc)))

(defn ^Node build-local-node
  ([settings] (build-local-node settings nil))
  ([settings {:keys [client], :or {client true}}]
   (let [is (cnv/->settings settings)
         nb (.. NodeBuilder nodeBuilder
                (settings is)
                (client client))]
     (.build ^NodeBuilder nb))))

(defn ^Node start-local-node
  [^Node node]
  (.start node)
  node)

(defn ^Node stop-local-node
  [^Node node]
  (.stop node)
  node)

(defn ^Client connect-to-local-node
  "Connects to a local Elasticsearch cluster nodes using
  local transport. Returns the client. Supposed to be used for automated testing."
  [^Node node]
  (.client node))
