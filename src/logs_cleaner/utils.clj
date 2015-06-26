(ns logs-cleaner.utils
  (:import [java.io File FileReader]
           [org.yaml.snakeyaml Yaml]))

(defn read-yaml [filename]
  (let [^Yaml yaml (Yaml.)
        parameters (.load yaml (FileReader. filename))]
    (apply conj {} parameters)))

(defn modified-before-two-weeks? [now file]
  (let [modified-time (.lastModified file)
        two-weeks (* 2 7 24 60 60 1000)]
    (if (> (- now modified-time) two-weeks)
      true
      false)))

(defn log-name-ok? [logname]
  (if (or (.endsWith logname ".log") (.contains logname ".log."))
    true
    false))

(defn list-logs [^File dir]
  (filter #(.isFile %) (.listFiles dir)))

(defn delete-file [file]
  (let [file-name (.getAbsolutePath file)
        size (.length file)
        modified-time (.toString (java.sql.Timestamp. (.lastModified file)))]
    (if (.delete file)
      {:ok? true :info {:file-name file-name
                        :size size
                        :modified-time modified-time}}
      {:ok? false :info {:file-name file-name}})))
