(ns clojurewerkz.elastisch.document
  (:refer-clojure :exclude [get replace])
  (:require [clojurewerkz.elastisch.rest          :as rest])
  (:use     clojure.set
            [clojurewerkz.elastisch.utils :only [join-names]]
            [clojurewerkz.elastisch.response :only [not-found?]]))

;;
;; API
;;

(defn create
  "Adds document to the search index, with its id"
  [index type document & {:as params}]
  (rest/post (rest/index-type index type) :body document :query-params params))

(defn put
  "Adds document to the search index"
  ([index type id document]
     (rest/put (rest/record index type id) :body document))
  ([index type id document & {:as params}]
     (rest/put (rest/record index type id) :body document :query-params params)))

(defn get
  "Gets Document by Id or returns nil if document is not found."
  [index type id & {:as params}]
  (let [result (rest/get (rest/record index type id) :query-params params)]
    (if (not-found? result)
      nil
      result)))

(defn delete
  "Deletes document from the index"
  ([index type id]
     (rest/delete (rest/record index type id)))
  ([index type id & {:as params}]
     (rest/delete (rest/record index type id) :query-params params)))

(defn present?
  [index type id]
  (not (nil? (get index type id))))

(defn multi-get
  "Multi get returns only items that are present in database."
  ([query]
     (let [results (rest/post (rest/index-mget)
                              :body { :docs query })]
       (filter :exists (:docs results))))
  ([index query]
     (let [results (rest/post (rest/index-mget index)
                              :body { :docs query })]
       (filter :exists (:docs results))))
  ([index type query]
     (let [results (rest/post (rest/index-mget index type)
                              :body { :docs query })]
       (filter :exists (:docs results)))))

(defn search
  "Performs a search query"
  [index type & { :as options }]
  (let [qk   [:search_type :scroll :size]
        qp   (select-keys options qk)
        body (dissoc options qk)]
    (rest/post (rest/search (join-names index) (join-names type))
               :body body
               :query-params qp)))

(defn replace
  "Replaces document with given id with a new one"
  [idx-name idx-type id document]
  (delete idx-name idx-type id)
  (put idx-name idx-type id document))

(defn count
  "Performs a count query.

   For Elastic Search reference, see http://www.elasticsearch.org/guide/reference/api/count.html"
  ([index type]
     (rest/post (rest/count-path) (join-names index) (join-names type)))
  ([index type query]
     (rest/post (rest/count-path) (join-names index) (join-names type) :body query))
  ([idx-name idx-type query & { :as options }]
     (let [qk   [:q :df :analyzer :default_operator]
           qp   (select-keys options qk)]
       (rest/post (rest/count-path) (join-names index) (join-names type)
                  :query-params qp
                  :body query))))

;; TODO delete-by-query
;; TODO more-like-this
;; TODO percolate
;; TODO multi-search