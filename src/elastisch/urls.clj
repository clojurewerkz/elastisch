(ns elastisch.urls)

(def base
  "http://localhost:9200")

(defn index
  [index-name]
  (format "%s/%s" base index-name))

(defn index-record
  [index-name type id]
  (format "%s/%s/%s/%s" base index-name type id))

(defn index-mapping
  "Returns index mapping"
  ([^String index-name]
     (println index-name)
     (format "%s/%s/_mapping" base index-name))
  ([^String index-name, ^String type-name]
     (format "%s/%s/%s/_mapping" base index-name type-name)))

(defn index-settings
  [^String index-name]
  (format "%s/%s/_settings" base index-name))

;; defn index-open
;; defn index-close

