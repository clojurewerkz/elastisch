(ns clojurewerkz.elastisch.native.document
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv])
  (:import clojure.lang.IPersistentMap))

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
  "Fetches and returns a document by id or nil if it does not exist"
  ([index mapping-type id]
     (let [res (es/get (cnv/->get-request index
                                          mapping-type
                                          id))]
       (cnv/get-response->map (.get res))))
  ([index mapping-type id & {:as params}]
     (let [res (es/get (cnv/->get-request index
                                          mapping-type
                                          id
                                          params))]
       (cnv/get-response->map (.get res)))))

(defn delete
  "Deletes document from the index.

   Related ElasticSearch documentation guide: http://www.elasticsearch.org/guide/reference/api/delete.html"
  ([index mapping-type id]
     )
  ([index mapping-type id & {:as params}]
     ))