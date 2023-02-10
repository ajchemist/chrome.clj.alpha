(ns ajchemist.chrome.alpha
  (:require
   [clojure.string :as str]
   [clojure.java.io :as jio]
   [me.raynes.conch.low-level :as conch]
   [ajchemist.chrome.alpha.util :as util]
   ))


(def WINNT_CHROME_BINARY_PATH_CANDIDATES
  [(str (jio/file (System/getenv "LOCALAPPDATA") "Google/Chrome SxS/Application/chrome.exe"))
   (str (jio/file (System/getenv "PROGRAMFILES") "Google/Chrome/Application/chrome.exe"))
   (str (jio/file (System/getenv "PROGRAMFILES(x86)") "Google/Chrome/Application/chrome.exe"))])


(def MACOS_CHROME_BINARY_PATH_CANDIDATES
  ["/Applications/Google Chrome Canary.app/Contents/MacOS/Google Chrome Canary"
   "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome"])


(defn- executable?
  [path]
  (let [file (jio/as-file path)]
    (and (. file exists) (. file canExecute))))


(defn find-command
  [command]
  (some
    (fn [path]
      (let [file (jio/file path command)]
        (when (executable? file)
          (str file))))
    (str/split (System/getenv "PATH") #":")))


(defn find-chrome-binary-path
  ([]
   (find-chrome-binary-path
     (case (util/os-keyword)
       :macos  MACOS_CHROME_BINARY_PATH_CANDIDATES
       :window WINNT_CHROME_BINARY_PATH_CANDIDATES
       nil)))
  ([candidates]
   (find-chrome-binary-path candidates ["google-chrome"]))
  ([candidates fallbacks]
   (or
     (some
       (fn [path]
         (when (executable? path)
           path))
       candidates)
     (apply (some-fn find-command) fallbacks))))


(defn- coerce-cli-opt-name
  [opt]
  (let [name (name opt)]
    (if (str/starts-with? name "--")
      name
      (str "--" name))))


(defn build-command-args
  [{:keys [binary-path]
    :or   {binary-path (find-chrome-binary-path)}
    :as   options}]
  {:pre [(executable? binary-path)]}
  (reduce-kv
    (fn [args opt value]
      (cond
        (nil? value)  args
        (true? value) (conj args (coerce-cli-opt-name opt))
        :else         (conj args (str (coerce-cli-opt-name opt) "=" value))))
    [binary-path]
    (dissoc options :binary-path)))


(defn process
  "return conch/proc object"
  [{:keys [print-output?]
    :as   options}]
  (let [cmd-args (build-command-args (dissoc options :print-output?))
        proc     (apply conch/proc cmd-args)]
    (if print-output?
      (future
        (jio/copy (jio/reader (:err proc)) *out*)
        (jio/copy (jio/reader (:out proc)) *out*))
      (future
        (conch/stream-to-out proc :err)
        (conch/stream-to-out proc :out)))
    (with-meta proc options)))


(defn destroy
  [proc]
  (conch/destroy proc))


(defn get-process-options
  [proc]
  (meta proc))
