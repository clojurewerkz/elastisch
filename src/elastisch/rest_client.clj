(ns elastisch.rest-client
  (:require [clj-http.client   :as http]
            [clojure.data.json :as json]))

;; FIXME: rewrite that to macros

(def throw-exceptions false)

(defn post
  [^String uri &{ :keys [body] :as options }]
  (json/read-json
   (:body (http/post uri (merge options { :accept :json :body (json/json-str body) })))))

(defn put
  [^String uri &{ :keys [body] :as options}]
  (json/read-json
   (:body (http/put uri (merge options { :accept :json :body (json/json-str body)  :throw-exceptions throw-exceptions })))))

(defn get
  [^String uri]
  (json/read-json
   (:body (http/get uri { :accept :json :throw-exceptions throw-exceptions }))))

(defn head
  [^String uri]
  (http/head uri { :accept :json :throw-exceptions throw-exceptions }))

(defn delete
  [^String uri]
  (json/read-json
   (:body (http/delete uri { :accept :json :throw-exceptions throw-exceptions }))))