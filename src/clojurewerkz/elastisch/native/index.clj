;; Copyright 2011-2015 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
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

(ns clojurewerkz.elastisch.native.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import org.elasticsearch.client.Client
           org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
           org.elasticsearch.action.admin.indices.create.CreateIndexResponse
           org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
           org.elasticsearch.action.admin.indices.stats.IndicesStatsResponse
           org.elasticsearch.action.index.IndexResponse
           org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse
           org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse
           org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse
           org.elasticsearch.action.admin.indices.open.OpenIndexResponse
           org.elasticsearch.action.admin.indices.close.CloseIndexResponse
           org.elasticsearch.action.admin.indices.optimize.OptimizeResponse
           org.elasticsearch.action.admin.indices.flush.FlushResponse
           org.elasticsearch.action.admin.indices.refresh.RefreshResponse
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse
           org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheResponse
           org.elasticsearch.action.admin.indices.status.IndicesStatusResponse
           org.elasticsearch.action.admin.indices.segments.IndicesSegmentResponse
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse
           org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse))

;;
;; API
;;

(defn create
  "Creates an index.

   Accepted options are :mappings and :settings. Both accept maps with the same structure as in the REST API.

   Examples:

    (require '[clojurewerkz.elastisch.native.index :as idx])

    (idx/create conn \"myapp_development\")
    (idx/create conn \"myapp_development\" :settings {\"number_of_shards\" 1})

    (let [mapping-types {:person {:properties {:username   {:type \"string\" :store \"yes\"}
                                               :first-name {:type \"string\" :store \"yes\"}
                                               :last-name  {:type \"string\"}
                                               :age        {:type \"integer\"}
                                               :title      {:type \"string\" :analyzer \"snowball\"}
                                               :planet     {:type \"string\"}
                                               :biography  {:type \"string\" :analyzer \"snowball\" :term_vector \"with_positions_offsets\"}}}}]
      (idx/create conn \"myapp_development\" :mappings mapping-types))

   Related ElasticSearch API Reference section:
   http://www.elasticsearch.org/guide/reference/api/admin-indices-create-index.html"
  [^Client conn ^String index-name & args]
  (let [opts                        (ar/->opts args)
        {:keys [settings mappings]} opts
        ft                       (es/admin-index-create conn (cnv/->create-index-request index-name settings mappings))
        ^CreateIndexResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))


(defn exists?
  "Returns true if given index (or indices) exists"
  [^Client conn ^String index-name]
  (let [ft                        (es/admin-index-exists conn (cnv/->index-exists-request index-name))
        ^IndicesExistsResponse res (.actionGet ft)]
    (.isExists res)))


(defn type-exists?
  "Returns true if a type/types exists in an index/indices"
  [^Client conn ^String index-name type-name]
  (let [ft                        (es/admin-types-exists conn (cnv/->types-exists-request index-name type-name))
        ^TypesExistsResponse res (.actionGet ft)]
    (.isExists res)))


(defn delete
  "Deletes an existing index"
  ([^Client conn]
     (let [ft                       (es/admin-index-delete conn (cnv/->delete-index-request))
           ^DeleteIndexResponse res (.actionGet ft)]
       {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))
  ([^Client conn ^String index-name]
     (let [ft                       (es/admin-index-delete conn (cnv/->delete-index-request index-name))
           ^DeleteIndexResponse res (.actionGet ft)]
       {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)})))

(defn get-mapping
  "The get mapping API allows to retrieve mapping definition of index or index/type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-mapping.html"
  ([^Client conn ^String index-name]
     (let [ft                       (es/admin-get-mappings conn (cnv/->get-mappings-request))
           ^GetMappingsResponse res (.actionGet ft)]
       (cnv/get-mappings-response->map res)))
  ([^Client conn ^String index-name ^String type-name]
     (let [ft                       (es/admin-get-mappings conn (cnv/->get-mappings-request index-name type-name))
           ^GetMappingsResponse res (.actionGet ft)]
       (cnv/get-mappings-response->map res))))

(defn update-mapping
  "The put mapping API allows to register or modify specific mapping definition for a specific type."
  [^Client conn ^String index-name ^String mapping-type & args]
  (let [opts                    (ar/->opts args)
        ft                      (es/admin-put-mapping conn (cnv/->put-mapping-request index-name mapping-type opts))
        ^PutMappingResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn delete-mapping
  "Allow to delete a mapping (type) along with its data."
  [^Client conn ^String index-name ^String mapping-type]
  (let [ft                       (es/admin-delete-mapping conn (cnv/->delete-mapping-request index-name mapping-type))
        ^PutMappingResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn update-settings
  "Updates index settings. No argument version updates index settings globally"
  ([^Client conn index-name settings]
     (let [ft (es/admin-update-index-settings conn (cnv/->update-settings-request index-name settings))]
       (.actionGet ft)
       true)))

(defn get-settings
  "Gets index settings."
  ([^Client conn index-name]
     (let [ft (es/admin-get-index-settings conn (cnv/->get-settings-request index-name))
           res (.actionGet ft)]
       (cnv/->get-settings-response->map res))))

(defn open
  "Opens an index"
  [^Client conn index-name]
  (let [ft (es/admin-open-index conn (cnv/->open-index-request index-name))
        ^OpenIndexResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn close
  "Closes an index or indices
  Usage:
    (idx/close conn \"my-index\")
    (idx/close conn [\"my-index\" \"dein-index\"])"
  [^Client conn index-name]
  (let [ft (es/admin-close-index conn (cnv/->close-index-request index-name))
        ^CloseIndexResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn optimize
  "Optimizes an index or multiple indices"
  [^Client conn index-name & args]
  (let [opts                  (ar/->opts args)
        ft                    (es/admin-optimize-index conn (cnv/->optimize-index-request index-name opts))
        ^OptimizeResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn flush
  "Flushes an index or multiple indices"
  [^Client conn index-name & args]
  (let [opts               (ar/->opts args)
        ft                 (es/admin-flush-index conn (cnv/->flush-index-request index-name opts))
        ^FlushResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn refresh
  "Refreshes an index or multiple indices"
  [^Client conn index-name]
  (let [ft                 (es/admin-refresh-index conn (cnv/->refresh-index-request index-name))
        ^RefreshResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn clear-cache
  "Clears caches index or multiple indices"
  [^Client conn index-name & args]
  (let [opts                           (ar/->opts args)
        ft                             (es/admin-clear-cache conn (cnv/->clear-indices-cache-request index-name opts))
        ^ClearIndicesCacheResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn stats
  "Returns statistics about indexes.

   No argument version returns all stats.
   Options may be used to define what exactly will be contained in the response:

   :docs : the number of documents, deleted documents
   :store : the size of the index
   :indexing : indexing statistics
   :types : document type level stats
   :groups : search group stats to retrieve the stats for
   :get : get operation statistics, including missing stats
   :search : search statistics, including custom grouping using the groups parameter (search operations can be associated with one or more groups)
   :merge : merge operation stats
   :flush : flush operation stats
   :refresh : refresh operation stats"
  ([^Client conn]
     (let [ft                        (es/admin-index-stats conn (cnv/->index-stats-request))
           ^IndicesStatsResponse res (.actionGet ft)]
       ;; TODO: convert stats into a map
       res))
  ([^Client conn & args]
     (let [opts                      (ar/->opts args)
           ft                        (es/admin-index-stats conn (cnv/->index-stats-request opts))
           ^IndicesStatsResponse res (.actionGet ft)]
       ;; TODO: convert stats into a map
       res)))

(defn status
  "Returns status for one or more indices.

   Options may be used to define what exactly will be contained in the response:

   :recovery (boolean, default: false): should the status include recovery information?
   :snapshot (boolean, default: false): should the status include snapshot information?"
  [^Client conn index-name & args]
  (let [opts                       (ar/->opts args)
        ft                         (es/admin-status conn (cnv/->indices-status-request index-name opts))
        ^IndicesStatusResponse res (.actionGet ft)]
    (cnv/broadcast-operation-response->map res)))

(defn segments
  "Returns segments information for one or more indices."
  [^Client conn index-name]
  (let [ft                           (es/admin-index-segments conn (cnv/->indices-segments-request index-name))
        ^IndicesSegmentResponse res (.actionGet ft)]
    (merge (cnv/broadcast-operation-response->map res)
           (cnv/indices-segments-response->map res))))

(defn update-aliases
  "Performs a batch of alias operations. Takes a collection of actions in the following form where
   plural keys (indices, aliases) can be supplied as collections or single strings

  [{:add    { :indices \"test1\" :alias \"alias1\" }}
   {:remove { :index \"test1\" :aliases \"alias1\" }}]"
  [^Client conn ops & args]
  (let [opts                        (ar/->opts args)
        ft                          (es/admin-update-aliases conn (cnv/->indices-aliases-request ops opts))
        ^IndicesAliasesResponse res (.actionGet ft)]
    {:ok (.isAcknowledged res) :acknowledged (.isAcknowledged res)}))

(defn create-template
  [^Client conn ^String template-name & args]
  (let [opts                          (ar/->opts args)
        ft                            (es/admin-put-index-template conn (cnv/->create-index-template-request template-name opts))
        ^PutIndexTemplateResponse res (.actionGet ft)]
    {:ok true :acknowledged (.isAcknowledged res)}))

(defn put-template
  [^Client conn ^String template-name & args]
  (let [opts                          (ar/->opts args)
        ft                            (es/admin-put-index-template conn (cnv/->put-index-template-request template-name opts))
        ^PutIndexTemplateResponse res (.actionGet ft)]
    {:ok true :acknowledged (.isAcknowledged res)}))

(defn delete-template
  [^Client conn ^String template-name]
  (let [ft                               (es/admin-delete-index-template conn (cnv/->delete-index-template-request template-name))
        ^DeleteIndexTemplateResponse res (.actionGet ft)]
    {:ok true :acknowledged (.isAcknowledged res)}))
