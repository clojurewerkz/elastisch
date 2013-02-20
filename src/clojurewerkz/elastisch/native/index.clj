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
           org.elasticsearch.action.admin.indices.flush.FlushResponse
           org.elasticsearch.action.admin.indices.refresh.RefreshResponse
           org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotResponse
           org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse
           org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse
           org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse))

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

(defn refresh
  "Refreshes an index or multiple indices"
  [index-name]
  (let [ft                 (es/admin-refresh-index (cnv/->refresh-index-request index-name))
        ^RefreshResponse res (.get ft)]
    (cnv/broadcast-operation-response->map res)))

(defn snapshot
  "Performs a snapshot through the gateway for one or multiple indices"
  [index-name]
  (let [ft                           (es/admin-gateway-snapshot (cnv/->gateway-snapshot-request index-name))
        ^GatewaySnapshotResponse res (.get ft)]
    (cnv/broadcast-operation-response->map res)))

(defn clear-cache
  "Clears caches index or multiple indices"
  [index-name & {:as options}]
  (let [ft                             (es/admin-clear-cache (cnv/->clear-indices-cache-request index-name options))
        ^ClearIndicesCacheResponse res (.get ft)]
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

(defn status
  "Returns status for one or more indices.

   Options may be used to define what exactly will be contained in the response:

   :recovery (boolean, default: false): should the status include recovery information?
   :snapshot (boolean, default: false): should the status include snapshot information?"
  [index-name & {:as options}]
  (let [ft                         (es/admin-status (cnv/->indices-status-request index-name options))
        ^IndicesStatusResponse res (.get ft)]
    (cnv/broadcast-operation-response->map res)))

(defn segments
  "Returns segments information for one or more indices."
  [index-name]
  (let [ft                           (es/admin-index-segments (cnv/->indices-segments-request index-name))
        ^IndicesSegmentsResponse res (.get ft)]
    (merge (cnv/broadcast-operation-response->map res)
           (cnv/indices-segments-response->map res))))

(defn update-aliases
  "Performs a batch of alias operations. Takes a collection of actions in the form of

   { :add    { :index \"test1\" :alias \"alias1\" } }
   { :remove { :index \"test1\" :alias \"alias1\" } }"
  [ops & {:as options}]
  (let [ft                          (es/admin-update-aliases (cnv/->indices-aliases-request ops options))
        ^IndicesAliasesResponse res (.get ft)]
    {:ok (.acknowledged res) :acknowledged (.acknowledged res)}))

(defn create-template
  [^String template-name & {:as options}]
  (let [ft                            (es/admin-put-index-template (cnv/->create-index-template-request template-name options))
        ^PutIndexTemplateResponse res (.get ft)]
    {:ok true :acknowledged (.acknowledged res)}))

(defn put-template
  [^String template-name & {:as options}]
  (let [ft                            (es/admin-put-index-template (cnv/->put-index-template-request template-name options))
        ^PutIndexTemplateResponse res (.get ft)]
    {:ok true :acknowledged (.acknowledged res)}))

(defn delete-template
  [^String template-name]
  (let [ft                               (es/admin-delete-index-template (cnv/->delete-index-template-request template-name))
        ^DeleteIndexTemplateResponse res (.get ft)]
    {:ok true :acknowledged (.acknowledged res)}))
  