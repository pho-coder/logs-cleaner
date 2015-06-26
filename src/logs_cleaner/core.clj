(ns logs-cleaner.core
  (:gen-class)
  (:require [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [taoensso.timbre.appenders.3rd-party.rotor :as rotor]
            [taoensso.timbre.appenders.3rd-party.rolling :as rolling]
            [environ.core :refer [env]]

            [logs-cleaner.utils :as utils]))

(defn init []
  (timbre/merge-config!
   {:appenders {:my-app (rolling/rolling-appender {:path "logs-cleaner.log"
                                                   :pattern :daily})}}))

(defn check-env []
  (timbre/info "check env")
  (let [MAGPIE-HOME (env :magpie-home)
        MAGPIE-CONF-DIR (env :magpie-conf-dir)]
    (if (or (nil? MAGPIE-HOME) (nil? MAGPIE-CONF-DIR))
      {:ok? false :info "MAGPIE-HOME or MAGPIE-CONF-DIR is null"}
      {:ok? true :info {:magpie-home MAGPIE-HOME
                        :magpie-conf-dir MAGPIE-CONF-DIR}})))

(defn check-dirs [magpie-home magpie-conf-dir]
  (timbre/info "check files")
  (let [magpie-logs-dir (str magpie-home "/logs")]
    (if-not (.isDirectory (clojure.java.io/file magpie-logs-dir))
      {:ok? false :info (str "dir " magpie-logs-dir " not exists!")}
      (let [magpie-conf-file (str magpie-conf-dir "/magpie.yaml")]
        (if-not (.exists (clojure.java.io/file magpie-conf-file))
          {:ok? false :info (str "file " magpie-conf-file " not exists!")}
          (let [magpie-workers-logs-dir ((utils/read-yaml magpie-conf-file) "magpie.logs.dir")]
            (if-not (.isDirectory (clojure.java.io/file magpie-workers-logs-dir))
              {:ok? false :info (str "dir " magpie-workers-logs-dir " not exists!")}
              {:ok? true :info {:magpie-logs-dir magpie-logs-dir
                                :magpie-workers-logs-dir magpie-workers-logs-dir}})))))))

(defn deal-files [dir]
  (let [files (utils/list-logs (java.io.File. dir))
        logs (filter #(utils/log-name-ok? (.getName %)) files)
        logs-delete (filter #(utils/modified-before-two-weeks? (System/currentTimeMillis) %) logs)
        delete-result (map #(utils/delete-file %) logs-delete)
        total-size (reduce (fn [size re]
                             (if (re :ok?)
                               (do (timbre/info "delete" ((re :info) :file-name) "size:" ((re :info) :size) "modified time:" ((re :info) :modified-time))
                                   (+ size ((re :info) :size)))
                               (timbre/warn "delete fail" ((re :info) :file-name)))) 0 delete-result)]
    (timbre/info "total delete size:" (/ total-size 1024.0 1024.0) "MB")))

(defn -main
  "logs cleaner"
  [& args]
;;  (timbre/info "logs cleaner come on!")
  (try
    (init)
    (catch Exception e
      (prn (.toString e))))
  (let [{ok? :ok? info :info} (check-env)]
    (if-not ok?
      (do (timbre/error info)
          (System/exit 1))
      (let [magpie-home (info :magpie-home)
            magpie-conf-dir (info :magpie-conf-dir)]
        (let [{ok? :ok? info :info} (check-dirs magpie-home magpie-conf-dir)]
          (if-not ok?
            (do (timbre/error info)
                (System/exit 1))
            (let [magpie-logs-dir (info :magpie-logs-dir)
                  magpie-workers-logs-dir (info :magpie-workers-logs-dir)]
              (timbre/info magpie-logs-dir magpie-workers-logs-dir)
              (timbre/info "deal" magpie-logs-dir)
              (deal-files magpie-logs-dir)
              (timbre/info "deal" magpie-workers-logs-dir)
              (deal-files magpie-workers-logs-dir)
              (System/exit 0))))))))
