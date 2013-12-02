(ns clojurewerkz.elastisch.rest.index
  (:refer-clojure :exclude [flush])
  (:require [clojurewerkz.elastisch.rest :as rest]
            [clj-http.client             :as http]
            [clojurewerkz.elastisch.rest.utils :refer [join-names]]))

;;
;; create, delete, exists?
;;

(defn create
  "Creates an index.

   Accepted options are :mappings and :settings. Both accept maps with the same structure as in the REST API.

   Examples:

    (require '[clojurewerkz.elastisch.rest.index :as idx])

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
  (rest/post (rest/index-url index-name) :body (if mappings
                                                 {:settings settings :mappings mappings}
                                                 {:settings settings})))

(defn exists?
  "Used to check if the index (indices) exists or not.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-indices-exists.html"  [^String index-name]
   (= 200 (:status (rest/head (rest/index-url index-name)))))

(defn delete
  "Deletes an existing index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-index.html"
  ([]
     (rest/delete (rest/index-url "_all")))
  ([^String index-name]
     (rest/delete (rest/index-url index-name))))

;;
;; Mappings
;;

(defn get-mapping
  "The get mapping API allows to retrieve mapping definition of index or index/type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-mapping.html"
  ([^String index-name]
     (rest/get (rest/index-mapping-url (join-names index-name))))
  ([^String index-name ^String type-name]
     (rest/get
      (rest/index-mapping-url index-name type-name))))

(defn update-mapping
  "The put mapping API allows to register or modify specific mapping definition for a specific type.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-put-mapping.html"
  [^String index-name-or-names ^String type-name & {:keys [mapping ignore_conflicts]}]
  (rest/put (rest/index-mapping-url (join-names index-name-or-names) type-name)
            :body mapping :query-params {:ignore_conflicts ignore_conflicts}))

(defn delete-mapping
  "Allow to delete a mapping (type) along with its data.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-delete-mapping.html"
  [^String index-name ^String type-name]
  (rest/delete (rest/index-mapping-url index-name type-name)))

;;
;; Settings
;;

(defn update-settings
  "Change specific index level settings in real time.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-update-settings.html"
  ([settings]
     (rest/put (rest/index-settings-url) :body settings))
  ([^String index-name settings]
     (rest/put (rest/index-settings-url index-name) :body settings)))


(defn get-settings
  "The get settings API allows to retrieve settings of an index or multiple indices

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-get-settings.html
  "
  ([]
     (rest/get (rest/index-settings-url)))
  ([^String index-name]
     (rest/get (rest/index-settings-url index-name))))

;;
;; Open/close
;;

(defn open
  "Opens an index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html"
  [index-name]
  (rest/post (rest/index-open-url index-name)))

(defn close
  "Closes an index.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-open-close.html"
  [index-name]
  (rest/post (rest/index-close-url index-name)))

(defn snapshot
  "Takes a snapthot of an index or multiple indexes.

  API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-gateway-snapshot.html"
  [index-name]
  (rest/post (rest/index-snapshot-url index-name)))

(defn refresh
  "Refreshes an index manually.

   Refreshing an index makes all changes (added, modified and deleted documents) since the last refresh available for search. In other
   words, index changes become \"visible\" to clients. ElasticSearch periodically refreshes indexes, the period is configurable via index
   settings.

   0-arity updates *all* indexes and may be a very expensive operation. Use it carefully.
   1-arity refreshes a single index.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-refresh.html"
  ([]
     (rest/post (rest/index-refresh-url)))
  ([index-name]
     (rest/post (rest/index-refresh-url (join-names index-name)))))


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
  ([]
     (rest/post (rest/index-optimize-url)))
  ([index-name & {:as options}]
     (rest/post (rest/index-optimize-url (join-names index-name)) :body options)))


(defn flush
  "Flushes an index.

   This causes the index by flushing data to the index storage and clearing the internal transaction log.
   Typically it is sufficient to let ElasticSearch when to periodically flush indexes.

   0-arity flushes *all* indexes and may be a very expensive operation. Use it carefully.
   1-arity flushes a single index.

   Accepted options:

   :refresh : should a refresh be performed after the flush?

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-flush.html"
  ([]
     (rest/post (rest/index-flush-url)))
  ([index-name]
     (rest/post (rest/index-flush-url (join-names index-name))))
  ([index-name & {:as options}]
     (rest/post (rest/index-flush-url (join-names index-name)) :body options)))


(defn clear-cache
  "Clears index caches.

   0-arity clears caches for *all* indexes and may be a very expensive operation. Use it carefully.
   1-arity clears caches for a single index.

   Accepted options:

   :filter : should filter caches be cleared?
   :field_data : should field data caches be cleared?
   :bloom : should Bloom filter caches be cleared?

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-clearcache.html"
  ([]
     (rest/post (rest/index-clear-cache-url)))
  ([index-name]
     (rest/post (rest/index-clear-cache-url (join-names index-name))))
  ([index-name & {:as options}]
     (rest/post (rest/index-clear-cache-url (join-names index-name)) :body options)))

;;
;; Aliases
;;

(defn update-aliases
  "Performs a batch of alias operations. Takes a collection of actions in the form of

   { :add    { :index \"test1\" :alias \"alias1\" } }
   { :remove { :index \"test1\" :alias \"alias1\" } }

   and so on, the same as described in the ElasticSearch documentation guide on aliases:
   http://www.elasticsearch.org/guide/reference/api/admin-indices-aliases.html"
  [& actions]
  (rest/post (rest/index-aliases-batch-url) :body {:actions actions}))

(defn get-aliases
  "Fetches and returns aliases for an index or multiple indexes.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-aliases.html"
  [index-name]
  (rest/get (rest/index-aliases-url (join-names index-name))))

;;
;; Templates
;;

(defn create-template
  "Creates or updates a new index template.

   Accepted options:

   :template : a pattern of index name that this template will be applied to
   :settigns : the same as for index/create
   :mappings : the same as for index/create

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-templates.html"
  [^String template-name & {:keys [template settings mappings]}]
  (rest/post (rest/index-template-url template-name) :body (if mappings
                                                             {:template template
                                                              :settings settings
                                                              :mappings mappings}
                                                             {:template template
                                                              :settings settings})))

(defn get-template
  [^String template-name]
  (rest/get (rest/index-template-url template-name)))

(defn delete-template
  [^String template-name]
  (rest/delete (rest/index-template-url template-name)))


(defn status
  "Returns recovery and/or snapshot status of an index.

   Accepted options:

   :recovery : should recovery status be returned?
   :snapshot : should snapshot status be returned?

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-status.html"
  ([index-name & {:as options}]
     (rest/get (rest/index-status-url (join-names index-name)) :query-params options)))

(defn segments
  "Returns segments information for an index or multiple indexes.

   API Reference: http://www.elasticsearch.org/guide/reference/api/admin-indices-segments.html"
  ([]
     (rest/get (rest/index-segments-url)))
  ([index-name]
     (rest/get (rest/index-status-url (join-names index-name)))))


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
  ([index-name & {:as options}]
     (rest/get (rest/index-stats-url (join-names index-name)) :query-params options)))
