(ns elastisch.rest-client
  (:require [clj-http.client   :as http]
            [clojure.data.json :as json]))

;; FIXME: rewrite that to macros

(def throw-exceptions false)

(defn json-post-req
  [^String uri &{ :keys [body] :as options }]
  (json/read-json
   (:body (http/post uri (merge options { :accept :json :body (json/json-str body) })))))

(defn json-put-req
  [^String uri &{ :keys [body] :as options}]
  (json/read-json
   (:body (http/put uri (merge options { :accept :json :body (json/json-str body)  :throw-exceptions throw-exceptions })))))

(defn json-get-req
  [^String uri]
  (json/read-json
   (:body (http/get uri { :accept :json :throw-exceptions throw-exceptions }))))

(defn head-req
  [^String uri]
  (http/head uri { :accept :json :throw-exceptions throw-exceptions }))

(defn delete-req
  [^String uri]
  (json/read-json
   (:body (http/delete uri { :accept :json :throw-exceptions throw-exceptions }))))