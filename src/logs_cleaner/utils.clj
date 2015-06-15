(ns logs-cleaner.utils)

(defn get-paths-env []
  (let [MAGPIE-HOME (System/getenv "MAGPIE_HOME")
        MAGPIE-CONF-DIR (System/getenv "MAGPIE_CONF_DIR")]
    (if MAGPIE-HOME
      ()
      {:success })))

(defn get-logs-dirs [])
