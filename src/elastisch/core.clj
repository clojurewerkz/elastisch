(ns elastisch.core
  (:refer-clojure :exclude [get])
  (:require [elastisch.utils         :as utils]
            [elastisch.rest-client   :as rest]
            [elastisch.urls          :as urls])
  (:use     [clojure.set]))

(defn put
  [index type id document & {:keys [version op_type routing parent timestamp ttl prelocate timeout refresh replication consistency] :as all}]
  (rest/json-put-req
   (urls/record index type id all)
   :body document))

(defn create
  "Creates document, autogenerating ID"
  [index type document & {:keys [version op_type routing parent timestamp ttl prelocate timeout refresh replication consistency] :as all}]
  (rest/json-post-req
   (urls/index-type index type all)
   :body document))

(defn get
  "Gets Document by Id or returns nil if document is not found."
  [index type id & {:keys [realtime fields routing preference refresh] :as all}]
  (let [result (rest/json-get-req
                (urls/record index type id all))]
    (if (utils/not-found? result)
      nil
      result)))

(defn delete
  [index type id & {:keys [version routing parent replication consistency refresh] :as all}]
  (rest/delete-req
   (urls/record index type id all)))

(defn present?
  [index type id]
  (not (nil? (get index type id))))

(defn multi-get
  "Multi get returns only items that are present in database."
  ([query]
     (let [results (rest/json-post-req
                     (urls/index-mget)
                     :body { :docs query })]
       (filter #(:exists %) (:docs results))))
  ([index query]
     (let [results (rest/json-post-req
                    (urls/index-mget index)
                    :body { :docs query })]
       (filter #(:exists %) (:docs results))))
  ([index type query]
     (let [results (rest/json-post-req
                    (urls/index-mget index type)
                    :body { :docs query })]
       (filter #(:exists %) (:docs results)))))

;; (defn uri-search
;;   [index type & { :keys [q df analyzer default-operator explain fields sort track-scores timeout from size search-type lowercase-expanded-terms analyze-wildcard]}])

;;
(defn search
  [index-name-or-names type-name-or-names & { :keys [query sort facets filter highlight size from fields min-score version explain script-fields index-boost
                                                     ;; Query string attributes
                                                     search-type scroll size] :as options }]
  (let [query-string-attributes (select-keys options [:search-type :scroll :size])
        body-attributes         (difference options query-string-attributes)]
  (rest/json-post-req
   (urls/search (utils/join-names index-name-or-names) (utils/join-names type-name-or-names) query-string-attributes)
   :body body-attributes)))

;; defn search
;; defn count
;; defn delete-by-query
;; defn more-like-this

