(ns io.github.ajchemist.chrome.alpha-test
  (:require
   [clojure.test :as test :refer [deftest is are testing]]
   [io.github.ajchemist.chrome.alpha :as chrome]
   )
  (:import
   java.nio.file.Files
   java.nio.file.Path
   java.nio.file.attribute.FileAttribute
   ))


(set! *warn-on-reflection* true)


(defn mktempdir
  ^Path
  ([^String prefix]
   (Files/createTempDirectory prefix (make-array FileAttribute 0))))


(deftest main
  (println (chrome/find-chrome-binary-path))


  (is
    (= (chrome/build-command-args
         {"--no-first-run"                     true
          "--no-default-browser-check"         true
          "--disable-plugin-power-saver"       true
          "--disable-popup-blocking"           true
          "--disable-translate"                true
          "--disable-default-apps"             true
          "--safebrowsing-disable-auto-update" true})

       (chrome/build-command-args
         {"no-first-run"                     true
          "no-default-browser-check"         true
          "disable-plugin-power-saver"       true
          "disable-popup-blocking"           true
          "disable-translate"                true
          "disable-default-apps"             true
          "safebrowsing-disable-auto-update" true})

       (chrome/build-command-args
         {:no-first-run                     true
          :no-default-browser-check         true
          :disable-plugin-power-saver       true
          :disable-popup-blocking           true
          :disable-translate                true
          :disable-default-apps             true
          :safebrowsing-disable-auto-update true})))
  )


(def headless-proc
  (chrome/process
    {:headless                         true
     :disable-gpu                      true
     :incognito                        true
     :start-in-incognito               true
     :no-first-run                     true
     :no-default-browser-check         true
     :disable-plugin-power-saver       true
     :disable-popup-blocking           true
     :disable-translate                true
     :disable-default-apps             true
     :disable-sync                     true
     :disable-extensions               true
     :disable-background-networking    true
     :safebrowsing-disable-auto-update true
     :user-data-dir                    (mktempdir "chrome-user-")}))


(deftest headless
  (chrome/destroy headless-proc)
  )


(set! *warn-on-reflection* false)
