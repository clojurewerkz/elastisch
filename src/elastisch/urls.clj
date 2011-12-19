(ns elastisch.urls
  (:require [elastisch.utils       :as utils]))

(def base
  "http://localhost:9200")

(defn index
  [index-name]
  (format "%s/%s" base index-name))

(defn record
  [index-name type id params]
  (if (empty? (keys params))
    (format "%s/%s/%s/%s" base index-name type id)
    (format "%s/%s/%s/%s?%s" base index-name type id (utils/join-hash params))))


(defn- _index-mapping
  "Returns index mapping"
  ([^String index-name]
     (format "%s/%s/_mapping" base index-name))
  ([^String index-name ^String type-name]
     (format "%s/%s/%s/_mapping" base index-name type-name)))

(defn index-mapping
  "Returns index mapping"
  ([^String index-name & [ ^String type-name ^Boolean ignore-conflicts ]]
     (let [url (if (nil? type-name) (_index-mapping index-name) (_index-mapping index-name type-name))]
       (if (nil? ignore-conflicts)
         url
         (format "%s?ignore_conflicts=%s" url (.toString ignore-conflicts))))))

(defn index-settings
  ([]
     (format "%s/_settings" base))
  ([^String index-name]
    (format "%s/%s/_settings" base index-name)))

(defn index-open
  [^String index-name]
  (format "%s/%s/_open" base index-name))

(defn index-close
  [^String index-name]
  (format "%s/%s/_close" base index-name))
