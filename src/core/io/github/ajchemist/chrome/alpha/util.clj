(ns io.github.ajchemist.chrome.alpha.util
  (:require
   [clojure.string :as str]
   ))


(defn os-name
  []
  (let [^String osname (str/upper-case (System/getProperty "os.name"))]
    (case osname
      "MAC OS X" "macos"
      "LINUX"    "linux"
      "WINDOWS"  "windows"
      (condp #(. ^String %2 startsWith ^String %) osname
        "windows" "windows"
        (throw (ex-info "Unknown osname" {:osname osname}))))))


(defn os-keyword
  []
  (keyword (os-name)))
