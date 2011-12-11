(ns elastish.index
  (:require [clojure.data.json    :as json]
            [elastish.urls        :as urls]
            [elastish.rest-client :as rest]))

(defn create
  [index-name & { :keys [settings mappings]  }]
  (let [request-body { :settings settings :mappings mappings } ]
    (rest/post-req
     (urls/index index-name)
     :body request-body)
    )
  )

(defn delete
  [index-name]
  (rest/delete-req (urls/index index-name)))


