(ns elastish.rest-client
  (:require [clj-http.client   :as http]
            [clojure.data.json :as json]))


(defn post-req
  [^String uri &{ :keys [body] :as options }]

  (json/read-json
   (:body (http/post uri (merge options { :accept :json, :body (json/json-str body) })))))

(defn get-req
  [^String uri]

  (json/read-json
   (:body (http/get uri { :accept :json }))))

(defn delete-req
  [^String uri]

  (json/read-json
   (:body (http/delete uri { :accept :json }))))