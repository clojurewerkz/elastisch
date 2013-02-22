(ns clojurewerkz.elastisch.native.document
  (:refer-clojure :exclude [get replace count sort])
  (:require [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.conversion :as cnv]
            [clojurewerkz.elastisch.query  :as q])
  (:import clojure.lang.IPersistentMap
           org.elasticsearch.action.get.GetResponse
           org.elasticsearch.action.count.CountResponse
           org.elasticsearch.action.delete.DeleteResponse))

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

;; TODO: search
;; TODO: search-all-types
;; TODO: search-all-indexes-and-types
;; TODO: multi-get
;; TODO: scroll
;; TODO: replace
