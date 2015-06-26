(ns logs-cleaner.utils-test
  (:require [clojure.test :refer :all]
            [midje.sweet :refer :all]
            [logs-cleaner.utils :refer :all]))

(def yaml-file "/Users/phoenix/Workspace/tmp_workspace/magpie_conf/magpie.yaml")

(facts "test utils"
  (fact "test read yaml"
    (keys (read-yaml yaml-file))
    => (contains "magpie.logs.dir")))
