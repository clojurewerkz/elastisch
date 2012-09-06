(ns clojurewerkz.elastisch.rest.response
  (:require [clojurewerkz.support.http.statuses :as statuses]))

;;
;; API
;;

(defn ok?
  [response]
  (true? (get response :ok)))

(defn conflict?
  [response]
  (let [s (:status response)]
    (and s (statuses/conflict? s))))

(defn not-found?
  [response]
  (let [s (:status response)]
    (or (false? (:exists response))
        (and s (statuses/missing? s)))))

(defn acknowledged?
  [response]
  (:acknowledged response))

(defn timed-out?
  [response]
  (:timed_out response))


(defn total-hits
  "Returns number of search hits from a response"
  [response]
  (get-in response [:hits :total]))

(defn count-from
  "Returns total number of search hits from a response"
  [response]
  (get response :count))

(defn any-hits?
  "Returns true if a response has any search hits, false otherwise"
  [response]
  (> (total-hits response) 0))

(def no-hits? (complement any-hits?))

(defn hits-from
  "Returns search hits from a response as a collection. To retrieve hits overview, get the :hits
   key from the response"
  [response]
  (get-in response [:hits :hits]))

(defn facets-from
  "Returns facets information (overview and actual facets) from a response as a map. Keys in the map depend on
   the facets query performed"
  [response]
  (get response :facets {}))

(defn ids-from
  "Returns search hit ids from a response"
  [response]
  (if (any-hits? response)
    (set (map :_id (hits-from response)))
    #{}))
