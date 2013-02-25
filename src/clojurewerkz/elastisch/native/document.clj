(ns clojurewerkz.elastisch.native.document
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojurewerkz.elastisch.query  :as q])
  (:import clojure.lang.IPersistentMap
           org.elasticsearch.action.get.GetResponse
           org.elasticsearch.action.count.CountResponse
           org.elasticsearch.action.delete.DeleteResponse
           org.elasticsearch.action.search.SearchResponse))

(defn ^IPersistentMap create
  "Adds document to the search index and waits for the response.
   If not given as an option, document id will be generated automatically."
  ([index mapping-type document]
     (let [res (es/index (cnv/->index-request index
                                              mapping-type
                                              document
                                              {:op-type "create"}))]
       (cnv/index-response->map (.get res))))
  ([index mapping-type document & {:as params}]
     (let [res (es/index (cnv/->index-request index
                                              mapping-type
                                              document
                                              (merge params {:op-type "create"})))]
       (cnv/index-response->map (.get res)))))

(defn async-create
  "Adds document to the search index and returns a future without waiting
    for the response.
   If not given as an option, document id will be generated automatically."
  ([index mapping-type document]
     (future (create index mapping-type document)))
  ([index mapping-type document & {:as params}]
     (future (create index mapping-type document params))))

(defn put
  "Creates or updates a document in the search index using the provided document id
   and waits for the response."
  ([index mapping-type id document]
     (let [res (es/index (cnv/->index-request index
                                              mapping-type
                                              document
                                              {:id id :op-type "index"}))]
       (cnv/index-response->map (.get res))))
  ([index mapping-type id document & {:as params}]
     (let [res (es/index (cnv/->index-request index
                                              mapping-type
                                              document
                                              (merge params {:id id :op-type "index"})))]
       (cnv/index-response->map (.get res)))))

(defn async-put
  "Creates or updates a document in the search index using the provided document id
   and returns a future without waiting for the response."
  ([index mapping-type id document]
     (future (put index mapping-type id document)))
  ([index mapping-type id document & {:as params}]
     (future (put index mapping-type id document params))))



(defn get
  "Fetches and returns a document by id or nil if it does not exist.
   Waits for response."
  ([index mapping-type id]
     (let [ft               (es/get (cnv/->get-request index
                                                       mapping-type
                                                       id))
           ^GetResponse res (.get ft)]
       (when (.isExists res)
         (cnv/get-response->map res))))
  ([index mapping-type id & {:as params}]
     (let [ft               (es/get (cnv/->get-request index
                                                       mapping-type
                                                       id
                                                       params))
           ^GetResponse res (.get ft)]
       (when (.isExists res)
         (cnv/get-response->map (.get ft))))))

(defn async-get
  "Fetches and returns a document by id or nil if it does not exist.
   Returns a future without waiting."
  ([index mapping-type id]
     (future (get index mapping-type id)))
  ([index mapping-type id & {:as params}]
     (future (get index mapping-type id params))))

(defn present?
  "Returns true if a document with the given id is present in the provided index
   with the given mapping type."
  [index mapping-type id]
  (not (nil? (get index mapping-type id))))

(defn multi-get
  "Multi get returns only documents that are found (exist).

   Queries can passed as a collection of maps with three keys: :_index,
   :_type and :_id:

   (doc/multi-get [{:_index index-name :_type mapping-type :_id \"1\"}
                   {:_index index-name :_type mapping-type :_id \"2\"}])

   
   2-argument version accepts an index name that eliminates the need to include
   :_index in every query map:

   (doc/multi-get index-name [{:_type mapping-type :_id \"1\"}
                              {:_type mapping-type :_id \"2\"}])

   3-argument version also accepts a mapping type that eliminates the need to include
   :_type in every query map:

   (doc/multi-get index-name mapping-type [{:_id \"1\"}
                                           {:_id \"2\"}])"
  ([queries]
     ;; example response from the REST API:
     ;; ({:_index people, :_type person, :_id 1, :_version 1, :exists true, :_source {...}})
     (let [ft                    (es/multi-get (cnv/->multi-get-request queries))
           ^MultiGetResponse res (.get ft)
           results               (cnv/multi-get-response->seq res)]
       (filter :exists results)))
  ([index queries]
     (let [qs                    (map #(assoc % :_index index) queries)
           ft                    (es/multi-get (cnv/->multi-get-request qs))
           ^MultiGetResponse res (.get ft)
           results               (cnv/multi-get-response->seq res)]
       (filter :exists results)))
  ([index mapping-type queries]
     (let [qs                    (map #(assoc % :_index index :_type mapping-type) queries)
           ft                    (es/multi-get (cnv/->multi-get-request qs))
           ^MultiGetResponse res (.get ft)
           results               (cnv/multi-get-response->seq res)]
       (filter :exists results))))

(defn delete
  "Deletes document from the index."
  ([index mapping-type id]
     (let [ft                  (es/delete (cnv/->delete-request index mapping-type id))
           ^DeleteResponse res (.get ft)]
       (cnv/delete-response->map res)))
  ([index mapping-type id & {:as options}]
     (let [ft                  (es/delete (cnv/->delete-request index mapping-type id options))
           ^DeleteResponse res (.get ft)]
       (cnv/delete-response->map res))))

(defn count
  "Performs a count query."
  ([index mapping-type]
     (count index mapping-type (q/match-all)))
  ([index mapping-type query]
     (let [ft (es/count (cnv/->count-request index mapping-type {:query query}))
           ^CountResponse res (.get ft)]
       (merge {:count (.getCount res)}
              (cnv/broadcast-operation-response->map res))))
  ([index mapping-type query & {:as options}]
     (let [ft (es/count (cnv/->count-request index mapping-type (merge options
                                                                       {:query query})))
           ^CountResponse res (.get ft)]
       (merge {:count (.getCount res)}
              (cnv/broadcast-operation-response->map res)))))

(defn search
  "Performs a search query across one or more indexes and one or more mapping types.

   Passing index name as \"_all\" means searching across all indexes."
  [index mapping-type & {:as options}]
  (let [ft                  (es/search (cnv/->search-request index mapping-type options))
        ^SearchResponse res (.get ft)]
    (cnv/search-response->seq res)))

(defn search-all-types
  "Performs a search query across one or more indexes and all mapping types."
  [index & {:as options}]
  (let [ft                  (es/search (cnv/->search-request index nil options))
        ^SearchResponse res (.get ft)]
    (cnv/search-response->seq res)))

(defn search-all-indexes-and-types
  "Performs a search query across all indexes and all mapping types. This may put very high load on your
   ElasticSearch cluster so use this function with care."
  [& {:as options}]
  (let [ft                  (es/search (cnv/->search-request [] nil options))
        ^SearchResponse res (.get ft)]
    (cnv/search-response->seq res)))

(defn scroll
  "Performs a scroll query, fetching the next page of results from a
   query given a scroll id"
  [scroll-id & {:as options}]
  (let [ft                  (es/search-scroll (cnv/->search-scroll-request scroll-id options))
        ^SearchResponse res (.get ft)]
    (cnv/search-response->seq res)))

(defn replace
  "Replaces document with given id with a new one"
  [index mapping-type id document]
  (delete index mapping-type id)
  (put index mapping-type id document))
