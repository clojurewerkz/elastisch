(ns elastisch.urls)

(defn base
  []
  "http://localhost:9200/")

(defn index
  [index-name]
  (str (base) "/" index-name))

(defn index-record
  [index-name type id]
  (str (base) "/" index-name "/" type "1"))

(defn index-mapping
  [index-name type]
  (str (base) "/" index-name "/" type "/_mapping"))
