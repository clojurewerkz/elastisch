(ns clojurewerkz.elastisch.response
  (:require [clojurewerkz.support.http.statuses :as statuses]))

;;
;; API
;;

(defn ok?
  [response]
  (true? (:ok response)))

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
  [response]
  (get-in response [:hits :total]))

(defn any-hits?
  [response]
  (> (total-hits response) 0))

(def no-hits? (complement any-hits?))

(defn hits-from
  [response]
  (get-in response [:hits :hits]))
