(ns clojurewerkz.elastisch.rest.document
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.rest          :as rest])
  (:use     clojure.set
            [clojurewerkz.elastisch.rest.utils :only [join-names]]
            [clojurewerkz.elastisch.rest.response :only [not-found?]]))

;;
;; API
;;

(defn create
  "Adds document to the search index. Document id will be generated automatically"
  ([index mapping-type document & {:as params}]
     (rest/post (rest/mapping-type-url index mapping-type) :body document :query-params params)))

(defn put
  "Creates or updates a document in the search index, using the provided document id"
  ([index mapping-type id document]
     (rest/put (rest/record-url index mapping-type id) :body document))
  ([index mapping-type id document & {:as params}]
     (rest/put (rest/record-url index mapping-type id) :body document :query-params params)))

(defn get
  "Fetches and returns a document by id or nil if it does not exist"
  [index mapping-type id & {:as params}]
  (let [result (rest/get (rest/record-url index mapping-type id) :query-params params)]
    (if (not-found? result)
      nil
      result)))

(defn delete
  "Deletes document from the index.

   Related ElasticSearch documentation guide: http://www.elasticsearch.org/guide/reference/api/delete.html"
  ([index mapping-type id]
     (rest/delete (rest/record-url index mapping-type id)))
  ([index mapping-type id & {:as params}]
     (rest/delete (rest/record-url index mapping-type id) :query-params params)))

(defn present?
  "Returns true if a document with the given id is present in the provided index / mapping type."
  [index mapping-type id]
  (not (nil? (get index mapping-type id))))

(defn multi-get
  "Multi get returns only documents that are found (exist)"
  ([query]
     (let [results (rest/post (rest/index-mget-url)
                              :body {:docs query})]
       (filter :exists (:docs results))))
  ([index query]
     (let [results (rest/post (rest/index-mget-url index)
                              :body {:docs query})]
       (filter :exists (:docs results))))
  ([index mapping-type query]
     (let [results (rest/post (rest/index-mget-url index mapping-type)
                              :body {:docs query})]
       (filter :exists (:docs results)))))

(defn search
  "Performs a search query across one or more indexes and one or more mapping types.

   Passing index name as \"_all\" means searching across all indexes."
  [index mapping-type & {:as options}]
  (let [qk   [:search_type :scroll]
        qp   (select-keys options qk)
        body (dissoc options qk)]
    (rest/post (rest/search-url (join-names index) (join-names mapping-type))
               :body body
               :query-params qp)))

(defn search-all-types
  "Performs a search query across one or more indexes and all mapping types."
  [index & {:as options}]
  (let [qk   [:search_type :scroll]
        qp   (select-keys options qk)
        body (dissoc options qk)]
    (rest/post (rest/search-url (join-names index))
               :body body
               :query-params qp)))

(defn search-all-indexes-and-types
  "Performs a search query across all indexes and all mapping types. This may put very high load on your
   ElasticSearch cluster so use this function with care."
  [index & {:as options}]
  (let [qk   [:search_type :scroll]
        qp   (select-keys options qk)
        body (dissoc options qk)]
    (rest/post (rest/search-url)
               :body body
               :query-params qp)))

(defn replace
  "Replaces document with given id with a new one"
  [index mapping-type id document]
  (delete index mapping-type id)
  (put index mapping-type id document))


(defn count
  "Performs a count query.

   Related ElasticSearch documentation guide: http://www.elasticsearch.org/guide/reference/api/count.html"
  ([index mapping-type]
     (rest/post (rest/count-url) (join-names index) (join-names mapping-type)))
  ([index mapping-type query]
     (rest/post (rest/count-url) (join-names index) (join-names mapping-type) :body query))
  ([index mapping-type query & { :as options }]
     (rest/post (rest/count-url (join-names index) (join-names mapping-type))
                :query-params (select-keys options [:df :analyzer :default_operator])
                :body query)))

(def ^{:doc "Optional parameters that all query-based delete functions share"
       :const true}
  optional-delete-query-parameters [:df :analyzer :default_operator :consistency])

(defn delete-by-query
  "Performs a delete-by-query operation.

   Related ElasticSearch documentation guide: http://www.elasticsearch.org/guide/reference/api/delete-by-query.html"
  ([index mapping-type query]
     (rest/delete (rest/delete-by-query-url (join-names index) (join-names mapping-type)) :body query))
  ([index mapping-type query & { :as options }]
     (rest/delete (rest/delete-by-query-url (join-names index) (join-names mapping-type))
                  :query-params (select-keys options optional-delete-query-parameters)
                  :body query)))

(defn delete-by-query-across-all-types
  "Performs a delete-by-query operation across all mapping types.

   Related ElasticSearch documentation guide: http://www.elasticsearch.org/guide/reference/api/delete-by-query.html"
  ([index query]
     (rest/delete (rest/delete-by-query-url (join-names index)) :body query))
  ([index query & {:as options}]
     (rest/delete (rest/delete-by-query-url (join-names index))
                  :query-params (select-keys options optional-delete-query-parameters)
                  :body query)))

(defn delete-by-query-across-all-indexes-and-types
  "Performs a delete-by-query operation across all indexes and mapping types.
   This may put very high load on your ElasticSearch cluster so use this function with care.

   Related ElasticSearch documentation guide: http://www.elasticsearch.org/guide/reference/api/delete-by-query.html"
  ([query]
     (rest/delete (rest/delete-by-query-url) :body query))
  ([query & {:as options}]
     (rest/delete (rest/delete-by-query-url)
                  :query-params (select-keys options optional-delete-query-parameters)
                  :body query)))


(defn more-like-this
  "Performs a More Like This (MLT) query.

   Related ElasticSearch documentation guide: http://www.elasticsearch.org/guide/reference/api/more-like-this.html"
  [index mapping-type id &{:as params}]
  (rest/get (rest/more-like-this-url index mapping-type id)
            :query-params params))

;; TODO percolate
;; TODO multi-search