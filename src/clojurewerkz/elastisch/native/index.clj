(ns clojurewerkz.elastisch.native.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse
           org.elasticsearch.action.admin.indices.create.CreateIndexResponse
           org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse
           org.elasticsearch.action.admin.indices.stats.IndicesStats
           org.elasticsearch.action.index.IndexResponse
           org.elasticsearch.action.admin.indices.open.OpenIndexResponse
           org.elasticsearch.action.admin.indices.close.CloseIndexResponse
           org.elasticsearch.action.admin.indices.optimize.OptimizeResponse
           org.elasticsearch.action.admin.indices.flush.FlushResponse))

;;
;; API
;;

(defn create
  "Creates an index.

   Accepted options are :mappings and :settings. Both accept maps with the same structure as in the REST API.

   Examples:

    (require '[clojurewerkz.elastisch.native.index :as idx])

    (idx/create \"myapp_development\")
    (idx/create \"myapp_development\" :settings {\"number_of_shards\" 1})

    (let [mapping-types {:person {:properties {:username   {:type \"string\" :store \"yes\"}
                                               :first-name {:type \"string\" :store \"yes\"}
                                               :last-name  {:type \"string\"}
                                               :age        {:type \"integer\"}
                                               :title      {:type \"string\" :analyzer \"snowball\"}
                                               :planet     {:type \"string\"}
                                               :biography  {:type \"string\" :analyzer \"snowball\" :term_vector \"with_positions_offsets\"}}}}]
      (idx/create \"myapp_development\" :mappings mapping-types))

   Related ElasticSearch API Reference section:
   http://www.elasticsearch.org/guide/reference/api/admin-indices-create-index.html"
  [^String index-name & {:keys [settings mappings]}]
  (let [ft                       (es/admin-index-create (cnv/->create-index-request index-name settings mappings))
        ^CreateIndexResponse res (.get ft)]
    {:ok (.acknowledged res) :acknowledged (.acknowledged res)}))


(defn exists?
  "Returns true if given index (or indices) exists"
  [index-name]
  (let [ft                        (es/admin-index-exists (cnv/->index-exists-request index-name))
        ^IndicesExistsResponse res (.get ft)]
    (.exists res)))


(defn delete
  "Deletes an existing index"
  [^String index-name]
  (let [ft                       (es/admin-index-delete (cnv/->delete-index-request index-name))
        ^DeleteIndexResponse res (.get ft)]
    {:ok (.acknowledged res) :acknowledged (.acknowledged res)}))


(defn update-settings
  "Updates index settings. No argument version updates index settings globally"
  ([index-name settings]
     (let [ft (es/admin-update-index-settings (cnv/->update-settings-request index-name settings))]
       (.get ft)
       true)))


(defn open
  "Opens an index"
  [index-name]
  (let [ft                     (es/admin-open-index (cnv/->open-index-request index-name))
        ^OpenIndexResponse res (.get ft)]
    {:ok (.acknowledged res) :acknowledged (.acknowledged res)}))

(defn close
  "Closes an index"
  [index-name]
  (let [ft                     (es/admin-close-index (cnv/->close-index-request index-name))
        ^CloseIndexResponse res (.get ft)]
    {:ok (.acknowledged res) :acknowledged (.acknowledged res)}))

(defn optimize
  "Optimizes an index or multiple indices"
  [index-name & {:as options}]
  (let [ft                    (es/admin-optimize-index (cnv/->optimize-index-request index-name options))
        ^OptimizeResponse res (.get ft)]
    (cnv/broadcast-operation-response->map res)))

(defn flush
  "Flushes an index or multiple indices"
  [index-name & {:as options}]
  (let [ft                 (es/admin-flush-index (cnv/->flush-index-request index-name options))
        ^FlushResponse res (.get ft)]
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
  ([]
     (let [ft                (es/admin-index-stats (cnv/->index-stats-request))
           ^IndicesStats res (.get ft)]
       ;; TODO: convert stats into a map
       res))
  ([& {:as options}]
     (let [ft                (es/admin-index-stats (cnv/->index-stats-request options))
           ^IndicesStats res (.get ft)]
       ;; TODO: convert stats into a map
       res)))
