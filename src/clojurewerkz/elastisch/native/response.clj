(ns clojurewerkz.elastisch.native.response)

;;
;; API
;;

(defn ok?
  [response]
  ;; TODO: can this be detected more precisely
  ;;       from native client responses? MK.
  true)

(defn not-found?
  [m]
  (false? (:exists m)))

(defn acknowledged?
  [m]
  (:acknowledged m))

(defn valid?
  "Returns true if a validation query response indicates valid query, false otherwise"
  [m]
  (:valid m))

(defn timed-out?
  [m]
  (:timed_out m))

(defn total-hits
  "Returns number of search hits from a response"
  [m]
  (get-in m [:hits :total]))

(defn count-from
  "Returns total number of search hits from a response"
  [m]
  (get m :count))

(defn any-hits?
  "Returns true if a response has any search hits, false otherwise"
  [m]
  (> (total-hits m) 0))

(def no-hits? (complement any-hits?))

(defn hits-from
  "Returns search hits from a response as a collection. To retrieve hits overview, get the :hits
   key from the response"
  [m]
  (get-in m [:hits :hits]))

(defn facets-from
  "Returns facets information (overview and actual facets) from a response as a map. Keys in the map depend on
   the facets query performed"
  [m]
  (get m :facets {}))

(defn ids-from
  "Returns search hit ids from a response"
  [m]
  (if (any-hits? m)
    (set (map :_id (hits-from m)))
    #{}))

(defn matches-from
  "Returns matches from a percolation response as a collection."
  [m]
  (get m :matches []))
