(ns re-fail-app.preloads
  (:require [cljs.pprint :as pprint]
            [cljs.repl :as repl]
            [cljs.spec.alpha :as s]
            [cljs.spec.test.alpha :as stest]
            [expound.alpha :as expound]
            [re-fail-app.components.error :as error]
            ;; Require specs for instrumentation
            [re-fail-app.components.spec.core]))

(defn- instrument
  []
  (let [vars (->> (stest/instrument)
                  sort
                  (into []))]
    (println "Instrumenting:")
    (pprint/pprint vars)))

(defn ^:dev/before-load preloads-before-load
  []
  (stest/unstrument))

(defn ^:dev/after-load preloads-after-load
  []
  (instrument))

(instrument)

;; Use expound for displaying spec errors
(set! s/*explain-out* expound/printer)

(set! error/*error-printer* repl/error->str)
