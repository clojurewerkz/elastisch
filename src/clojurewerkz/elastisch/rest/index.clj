;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.rest.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clj-http.client             :as http]
            [clojurewerkz.elastisch.rest.utils :refer [join-names]]
            [clojurewerkz.elastisch.arguments :as ar])
  (:import clojurewerkz.elastisch.rest.Connection))

;;
;; create, delete, exists?
;;

(defn create
  "Creates an index.

   Accepted options are :mappings and :settings. Both accept maps with the same structure as in the REST API.

   Examples:

    (require '[clojurewerkz.elastisch.rest.index :as idx])

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
  [^Connection conn ^String index-name & args]
  (let [opts                        (ar/->opts args)
        {:keys [settings mappings]} opts]
    (rest/post conn (rest/index-url conn
                                    index-name)
               {:body (if mappings
                        {:settings settings :mappings mappings}
                        {:settings settings})})))

(defn exists?
  "Used to check if the index (indices) exists or not.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-indices-exists.html"
  [^Connection conn ^String index-name]
  (= 200 (:status (rest/head conn (rest/index-url conn
                                                  index-name)))))

(defn type-exists?
  "Used to check if a type/types exists in an index/indices.

   API Reference: http://www.elasticsearch.org/guide/en/elasticsearch/reference/current/indices-types-exists.html"
  [^Connection conn ^String index-name ^String type-name]
  (= 200 (:status (rest/head conn (rest/mapping-type-url conn
                                                         index-name type-name)))))

(defn delete
  "Deletes an existing index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-index.html"
  ([^Connection conn]
     (rest/delete conn (rest/index-url conn
                                       "_all")))
  ([^Connection conn ^String index-name]
     (rest/delete conn (rest/index-url conn
                                       index-name))))

;;
;; Mappings
;;

(defn get-mapping
  "The get mapping API allows to retrieve mapping definition of index or index/type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-mapping.html"
  ([^Connection conn ^String index-name]
     (rest/get conn (rest/index-mapping-url conn
                                            (join-names index-name))))
  ([^Connection conn ^String index-name ^String type-name]
     (rest/get conn
               (rest/index-mapping-url conn
                                       index-name type-name))))

(defn update-mapping
  "The put mapping API allows to register or modify specific mapping definition for a specific type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-put-mapping.html"
  [^Connection conn ^String index-name-or-names ^String type-name & args]
  (let [opts                               (ar/->opts args)
        {:keys [mapping ignore_conflicts]} opts]
    (rest/put conn (rest/index-mapping-url conn
                                           (join-names index-name-or-names) type-name)
              {:body mapping :query-params {:ignore_conflicts ignore_conflicts}})))

(defn delete-mapping
  "Allow to delete a mapping (type) along with its data.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-mapping.html"
  [^Connection conn ^String index-name ^String type-name]
  (rest/delete conn (rest/index-mapping-url conn
                                            index-name type-name)))

;;
;; Settings
;;

(defn update-settings
  "Change specific index level settings in real time.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-update-settings.html"
  ([^Connection conn settings]
     (rest/put conn (rest/index-settings-url conn) {:body settings}))
  ([^Connection conn ^String index-name settings]
     (rest/put conn (rest/index-settings-url conn
                                             index-name) {:body settings})))


(defn get-settings
  "The get settings API allows to retrieve settings of an index or multiple indices

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-settings.html
  "
  ([^Connection conn]
     (rest/get conn (rest/index-settings-url conn)))
  ([^Connection conn ^String index-name]
     (rest/get conn (rest/index-settings-url conn index-name))))

;;
;; Open/close
;;

(defn open
  "Opens an index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html"
  [^Connection conn index-name]
  (rest/post conn (rest/index-open-url conn
                                       index-name)))

(defn close
  "Closes an index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html"
  [^Connection conn index-name]
  (rest/post conn (rest/index-close-url conn
                                        index-name)))

(defn snapshot
  "Takes a snapthot of an index or multiple indexes.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-gateway-snapshot.html"
  [^Connection conn index-name]
  (rest/post conn (rest/index-snapshot-url conn
                                           index-name)))

(defn refresh
  "Refreshes an index manually.

   Refreshing an index makes all changes (added, modified and deleted documents) since the last refresh available for search. In other
   words, index changes become \"visible\" to clients. ElasticSearch periodically refreshes indexes, the period is configurable via index
   settings.

   0-arity updates *all* indexes and may be a very expensive operation. Use it carefully.
   1-arity refreshes a single index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-refresh.html"
  ([^Connection conn]
     (rest/post conn (rest/index-refresh-url conn)))
  ([^Connection conn index-name]
     (rest/post conn (rest/index-refresh-url conn
                                             (join-names index-name)))))


(defn optimize
  "Optimizes an index.

   Optimization makes searches over the index faster and also reclaims some disk space used by
   deleted documents. Optionally you can optimize and refresh an index in a single request.

   0-arity optimizes *all* indexes and may be a very expensive operation. Use it carefully.
   1-arity optimizes a single index.

   Accepted options:

   :max_num_segments : the number of segments to optimize to.
   :only_expunge_deletes : should the optimize process only expunge segments with deleted documents in it?
   :refresh : when set to true, refreshes the index
   :flush : when set to true, flushes the index
   :wait_for_merge : should the request wait for the merge to end?


   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-optimize.html"
  ([^Connection conn]
     (rest/post conn (rest/index-optimize-url conn)))
  ([^Connection conn index-name & args]
     (rest/post conn (rest/index-optimize-url conn
                                              (join-names index-name))
                {:body (ar/->opts args)})))


(defn flush
  "Flushes an index.

   This causes the index by flushing data to the index storage and clearing the internal transaction log.
   Typically it is sufficient to let ElasticSearch when to periodically flush indexes.

   0-arity flushes *all* indexes and may be a very expensive operation. Use it carefully.
   1-arity flushes a single index.

   Accepted options:

   :refresh : should a refresh be performed after the flush?

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-flush.html"
  ([^Connection conn]
     (rest/post conn (rest/index-flush-url conn)))
  ([^Connection conn index-name]
     (rest/post conn (rest/index-flush-url conn
                                           (join-names index-name))))
  ([^Connection conn index-name & args]
     (rest/post conn (rest/index-flush-url conn
                                           (join-names index-name)) {:body (ar/->opts args)})))


(defn clear-cache
  "Clears index caches.

   0-arity clears caches for *all* indexes and may be a very expensive operation. Use it carefully.
   1-arity clears caches for a single index.

   Accepted options:

   :filter : should filter caches be cleared?
   :field_data : should field data caches be cleared?
   :bloom : should Bloom filter caches be cleared?

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-clearcache.html"
  ([^Connection conn]
     (rest/post conn (rest/index-clear-cache-url conn)))
  ([^Connection conn index-name]
     (rest/post conn (rest/index-clear-cache-url conn
                                                 (join-names index-name))))
  ([^Connection conn index-name & args]
     (rest/post conn (rest/index-clear-cache-url conn
                                                 (join-names index-name))
                {:body (ar/->opts args)})))

;;
;; Aliases
;;

(defn update-aliases
  "Performs a batch of alias operations. Takes a collection of actions in the form of

   { :add    { :index \"test1\" :alias \"alias1\" } }
   { :remove { :index \"test1\" :alias \"alias1\" } }

   and so on, the same as described in the ElasticSearch documentation guide on aliases:
   http://www.elasticsearch.org/guide/reference/api/admin-indices-aliases.html"
  [^Connection conn & actions]
  (rest/post conn (rest/index-aliases-batch-url conn)
             {:body {:actions actions}}))

(defn get-aliases
  "Fetches and returns aliases for an index or multiple indexes.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-aliases.html"
  [^Connection conn index-name]
  (rest/get conn (rest/index-aliases-url conn
                                         (join-names index-name))))

;;
;; Templates
;;

(defn create-template
  "Creates or updates a new index template.

   Accepted options:

   :template : a pattern of index name that this template will be applied to
   :settings : the same as for index/create
   :mappings : the same as for index/create
   :aliases  : template aliases configuration

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-templates.html"
  [^Connection conn ^String template-name & args]
  (let [opts                                 (ar/->opts args)
        {:keys [template settings mappings aliases]} opts]
    (rest/post conn (rest/index-template-url conn
                                             template-name)
               {:body (merge {:template template
                             :settings settings}
                             (if mappings {:mappings mappings})
                             (if aliases {:aliases aliases}))
                            })))

(defn get-template
  [^Connection conn ^String template-name]
  (rest/get conn (rest/index-template-url conn
                                          template-name)))

(defn delete-template
  [^Connection conn ^String template-name]
  (rest/delete conn (rest/index-template-url conn
                                             template-name)))


(defn status
  "Returns recovery and/or snapshot status of an index.

   Accepted options:

   :recovery : should recovery status be returned?
   :snapshot : should snapshot status be returned?

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-status.html"
  ([^Connection conn index-name & args]
     (rest/get conn (rest/index-status-url conn
                                           (join-names index-name))
               {:query-params (ar/->opts args)})))

(defn segments
  "Returns segments information for an index or multiple indexes.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-segments.html"
  ([^Connection conn]
     (rest/get conn (rest/index-segments-url conn)))
  ([^Connection conn index-name]
     (rest/get conn (rest/index-status-url conn
                                           (join-names index-name)))))


(defn stats
  "Returns statistics about an index or multiple indexes

   Accepted options define what exactly will be contained in the response:

   :docs : the number of documents, deleted documents
   :store : the size of the index
   :indexing : indexing statistics
   :types : document type level stats
   :get : get operation statistics, including missing stats
   :search : search statistics, including custom grouping using the groups parameter (search operations can be associated with one or more groups)
   :merge : merge operation stats
   :flush : flush operation stats
   :refresh : refresh operation stats
   :clear : clear all the flags first

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-stats.html"
  ([^Connection conn index-name & args]
     (rest/get conn (rest/index-stats-url conn
                                          (join-names index-name))
               {:query-params (ar/->opts args)})))
