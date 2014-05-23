;; Copyright (c) 2011-2014 Michael S. Klishin, Alex Petrov, and the ClojureWerkz Team
;;
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file epl-v10.html at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns clojurewerkz.elastisch.native-api.snapshot-test
  (:require [clojurewerkz.elastisch.native.admin :as admin]
            [clojurewerkz.elastisch.native :as es]
            [clojurewerkz.elastisch.native.response :refer [acknowledged? accepted?]]
            [clojurewerkz.elastisch.fixtures :as fx]
            [clojure.test :refer :all]
            [clojurewerkz.elastisch.test.helpers  :as th])
  (:import java.io.File))

(use-fixtures :each fx/reset-indexes fx/prepopulate-people-index fx/prepopulate-tweets-index)

(defn ^String tmp-dir
  []
  (System/getProperty "java.io.tmpdir"))

(let [conn (th/connect-native-client)]
  (deftest ^{:snapshots true :native true} test-snapshotting
    (let [repo "native-backup1"
          p    (tmp-dir)
          s    "native-snapshot1"
          _    (.mkdir (File. p))
          r1   (admin/register-snapshot-repository conn repo
                                                   {:type "fs"
                                                    :settings {:location p
                                                               :compress true}})
          _   (try
                (admin/delete-snapshot conn repo s)
                (catch Exception _))
          r2 (try
               (admin/take-snapshot conn repo s {:wait-for-completion? true})
               (catch org.elasticsearch.snapshots.InvalidSnapshotNameException _))
          r3 (admin/delete-snapshot conn repo s)]
      (is (acknowledged? r1))
      (is (accepted? r2))
      (is (acknowledged? r3)))))
