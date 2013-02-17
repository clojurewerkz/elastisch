(ns clojurewerkz.elastisch.test.helpers)

(defn ci?
  "Returns true if tests are running in the CI environment
   (on travis-ci.org)"
  []
  (System/getenv "CI"))