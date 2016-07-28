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

(ns clojurewerkz.elastisch.rest.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clj-http.client             :as http]
            [clojurewerkz.elastisch.rest.utils :refer [join-names]])
  (:import clojurewerkz.elastisch.rest.Connection))

;;
;; create, delete, exists?
;;

(defn create
  "Creates an index.

  Supported Options:
  * `:mappings`: see <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/mapping.html>.
  * `:settings`: see <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/index-modules.html>.

  Not-yet-supported Options:
  * `:warmers`: see <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-warmers.html>.
  * `:aliases`: see <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-aliases.html>.
  * `:creation_date`: override the creation-date stored in the index’s metadata.
    The value should be an integer, the epoch time in milliseconds.

  Examples:

  ```clojure
  (require '[clojurewerkz.elastisch.rest.index :as idx])

  (idx/create conn \"myapp_development\")
  (idx/create conn \"myapp_development\" {:settings {\"number_of_shards\" 1}})

  (let [mapping-types {:person {:properties {:username   {:type \"string\" :store \"yes\"}
                                             :first-name {:type \"string\" :store \"yes\"}
                                             :last-name  {:type \"string\"}
                                             :age        {:type \"integer\"}
                                             :title      {:type \"string\" :analyzer \"snowball\"}
                                             :planet     {:type \"string\"}
                                             :biography  {:type \"string\" :analyzer \"snowball\" :term_vector \"with_positions_offsets\"}}}}]
    (idx/create conn \"myapp_development\" {:mappings mapping-types}))
  ```

  Related Elasticsearch API Reference section:
  <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-create-index.html>"
  ([^Connection conn ^String index-name]
   (create conn index-name nil))
  ([^Connection conn ^String index-name opts]
   (let [{:keys [settings mappings]} opts]
     (rest/post conn (rest/index-url conn
                                     index-name)
                {:body (if mappings
                         {:settings settings :mappings mappings}
                         {:settings settings})}))))

(defn exists?
  "Used to check if the index (indices) exists or not.

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-exists.html>"
  [^Connection conn ^String index-name]
  (= 200 (:status (rest/head conn (rest/index-url conn
                                                  index-name)))))

(defn type-exists?
  "Used to check if a type/types exists in an index/indices.

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-types-exists.html>"
  [^Connection conn ^String index-name ^String type-name]
  (= 200 (:status (rest/head conn (rest/mapping-type-url conn
                                                         index-name type-name)))))

(defn delete
  "Deletes an existing index.

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-delete-index.html>"
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
  "The get mapping API allows to retrieve mapping definitions for an index or index/type.

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-get-mapping.html>"
  ([^Connection conn ^String index-name]
     (rest/get conn (rest/index-mapping-url conn
                                            (join-names index-name))))
  ([^Connection conn ^String index-name ^String type-name]
     (rest/get conn
               (rest/index-mapping-url conn
                                       index-name type-name))))

(defn update-mapping
  "The PUT mapping API allows you to provide type mappings while creating a new index, add a new type to an existing index, or add new fields to an existing type.

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-put-mapping.html>"
  [^Connection conn ^String index-name-or-names ^String type-name opts]
  (let [{:keys [mapping]} opts]
    (rest/put conn
              (rest/index-mapping-url conn (join-names index-name-or-names) type-name)
              {:body mapping
               :query-params (dissoc opts :mapping)})))

;;
;; Settings
;;

(defn update-settings
  "Change specific index level settings in real time.

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-update-settings.html>"
  ([^Connection conn settings]
     (rest/put conn (rest/index-settings-url conn) {:body settings}))
  ([^Connection conn ^String index-name settings]
     (rest/put conn (rest/index-settings-url conn
                                             index-name) {:body settings})))


(defn get-settings
  "The get settings API allows to retrieve settings of an index or multiple indices

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-get-settings.html>"
  ([^Connection conn]
     (rest/get conn (rest/index-settings-url conn)))
  ([^Connection conn ^String index-name]
     (rest/get conn (rest/index-settings-url conn index-name))))

;;
;; Open/close
;;

(defn open
  "Opens an index.

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/2.3/indices-open-close.html>"
  [^Connection conn index-name]
  (rest/post conn (rest/index-open-url conn
                                       index-name)))

(defn close
  "Closes an index.

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html>"
  [^Connection conn index-name]
  (rest/post conn (rest/index-close-url conn
                                        index-name)))

(defn snapshot
  "Takes a snapshot of an index or multiple indexes.

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-gateway-snapshot.html>"
  [^Connection conn index-name]
  (rest/post conn (rest/index-snapshot-url conn
                                           index-name)))

(defn refresh
  "Refreshes an index manually.

  Refreshing an index makes all changes (added, modified and deleted documents) since the last refresh available for search. In other
  words, index changes become \"visible\" to clients. Elasticsearch periodically refreshes indexes, the period is configurable via index
  settings.

  * 0-arity updates *all* indexes and may be a very expensive operation. Use it carefully.
  * 1-arity refreshes a single index.

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-refresh.html>"
  ([^Connection conn]
     (rest/post conn (rest/index-refresh-url conn)))
  ([^Connection conn index-name]
     (rest/post conn (rest/index-refresh-url conn
                                             (join-names index-name)))))


(defn optimize
  "Optimizes an index.

  Optimization makes searches over the index faster and also reclaims some disk space used by
  deleted documents. Optionally you can optimize and refresh an index in a single request.

  * 0-arity optimizes *all* indexes and may be a very expensive operation. Use it carefully.
  * 1-arity optimizes a single index.

  Accepted options:

  * `:max_num_segments`: the number of segments to optimize to.
  * `:only_expunge_deletes`: should the optimize process only expunge segments with deleted documents in it?
  * `:refresh`: when set to `true`, refreshes the index
  * `:flush`: when set to `true`, flushes the index
  * `:wait_for_merge`: should the request wait for the merge to end?


  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-optimize.html>"
  ([^Connection conn]
     (rest/post conn (rest/index-optimize-url conn)))
  ([^Connection conn index-name]
     (optimize conn index-name nil))
  ([^Connection conn index-name opts]
     (rest/post conn (rest/index-optimize-url conn
                                              (join-names index-name))
                {:body opts})))


(defn flush
  "Flushes an index.

  This causes the index by flushing data to the index storage and clearing the internal transaction log.
  Typically it is sufficient to let Elasticsearch when to periodically flush indexes.

  * 0-arity flushes *all* indexes and may be a very expensive operation. Use it carefully.
  * 1-arity flushes a single index.

  Accepted options:

  * `:refresh`: should a refresh be performed after the flush?

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-flush.html>"
  ([^Connection conn]
     (rest/post conn (rest/index-flush-url conn)))
  ([^Connection conn index-name]
     (rest/post conn (rest/index-flush-url conn
                                           (join-names index-name))))
  ([^Connection conn index-name opts]
     (rest/post conn (rest/index-flush-url conn
                                           (join-names index-name)) {:body opts})))


(defn clear-cache
  "Clears index caches.

  * 0-arity clears caches for *all* indexes and may be a very expensive operation. Use it carefully.
  * 1-arity clears caches for a single index.

  Accepted options:

  * `:filter`: should filter caches be cleared?
  * `:field_data`: should field data caches be cleared?
  * `:bloom`: should Bloom filter caches be cleared?

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-clearcache.html>"
  ([^Connection conn]
     (rest/post conn (rest/index-clear-cache-url conn)))
  ([^Connection conn index-name]
     (rest/post conn (rest/index-clear-cache-url conn
                                                 (join-names index-name))))
  ([^Connection conn index-name opts]
     (rest/post conn (rest/index-clear-cache-url conn
                                                 (join-names index-name))
                {:body opts})))

;;
;; Aliases
;;

(defn update-aliases
  "Performs a batch of alias operations. Takes a collection of actions in the form of

  ```edn
  { :add    { :index \"test1\" :alias \"alias1\" } }
  { :remove { :index \"test1\" :alias \"alias1\" } }
  ```

  and so on, the same as described in the Elasticsearch documentation guide on aliases:
  <http://www.elasticsearch.org/guide/reference/api/admin-indices-aliases.html>"
  [^Connection conn & actions]
  (rest/post conn (rest/index-aliases-batch-url conn)
             {:body {:actions actions}}))

(defn get-aliases
  "Fetches and returns aliases for an index or multiple indexes.

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-aliases.html>"
  [^Connection conn index-name]
  (rest/get conn (rest/index-aliases-url conn
                                         (join-names index-name))))

;;
;; Templates
;;

(defn create-template
  "Creates or updates a new index template.

  Accepted options:

  * `:template`: a pattern of index name that this template will be applied to
  * `:settings`: the same as for [[create]]
  * `:mappings`: the same as for [[create]]
  * `:aliases`: template aliases configuration

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-templates.html>"
  ([^Connection conn ^String template-name]
   (create-template conn template-name nil))
  ([^Connection conn ^String template-name opts]
   (let [{:keys [template settings mappings aliases]} opts]
     (rest/post conn (rest/index-template-url conn
                                              template-name)
                {:body (merge {:template template
                              :settings settings}
                              (if mappings {:mappings mappings})
                              (if aliases {:aliases aliases}))
                             }))))

(defn get-template
  [^Connection conn ^String template-name]
  (rest/get conn (rest/index-template-url conn
                                          template-name)))

(defn delete-template
  [^Connection conn ^String template-name]
  (rest/delete conn (rest/index-template-url conn
                                             template-name)))

#_
(defn recovery
  "TODO refine description

  Provides insight into index shard recoveries.

  Accepted options:

  `:detailed`: Display a detailed view. This is primarily useful for viewing the recovery of physical index files. Default: `false`.
  `:active_only`: Display only those recoveries that are currently on-going. Default: `false`.

  API reference: <https://www.elastic.co/guide/en/elasticsearch/reference/1.7/indices-recovery.html>"
  ([^Connection conn opts]
     (rest/get conn (rest/index-recovery-url conn)
               {:query-params opts}))
  ([^Connection conn index-name opts]
     (rest/get conn (rest/index-recovery-url conn
                                             (join-names index-name))
               {:query-params opts})))

;; The above fails with "Can't have more than 1 variadic overload"

(defn recovery
  "TODO refine description AND resolve the above issue"
  [conn index-name]
  (rest/get conn (rest/index-recovery-url conn
                                          (join-names index-name))))

(defn segments
  "Returns segments information for an index or multiple indexes.

  API Reference: <http://www.elasticsearch.org/guide/reference/api/admin-indices-segments.html>"
  ([^Connection conn]
     (rest/get conn (rest/index-segments-url conn)))
  ([^Connection conn index-name]
     (rest/get conn (rest/index-segments-url conn
                                             (join-names index-name)))))


(defn stats
  "Returns statistics about an index or multiple indexes

  Accepted options define what exactly will be contained in the response:

  * `:stats`: the specific stat(s) to return (defaults to all)
  * `:types`: combined with index stats to provide document type level stats
  * `:groups`: search statistics can be associated with one or more groups
  * `:fields`: fields to be included in the statistics by default where applicable
  * `:completion_fields`: fields to be included in the completion suggest statistics
  * `:fielddata_fields`: fields to be included in the fielddata statistics

  API Reference: <https://www.elastic.co/guide/en/elasticsearch/reference/1.5/indices-stats.html>"
  ([^Connection conn index-name]
   (stats conn index-name nil))
  ([^Connection conn index-name opts]
   (rest/get conn (rest/index-stats-url conn
                                        (join-names index-name)
                                        (join-names (get opts :stats "_all")))
             {:query-params (dissoc opts :stats)})))
