(ns re-fail-app.app
  (:require [cljs.spec.test.alpha :as stest]
            [re-fail-app.views.core :as views]
            [re-frame.core :as rf]
            [reagent.dom :as rd]))

(enable-console-print!)

(defn- instrument
  []
  (println "Instrumented:" (stest/instrument)))

(defn- render
  []
  (rd/render [views/view]
             (.getElementById js/document "root")))

(defn ^:dev/after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (instrument)
  (render))

(defn ^:export init
  []
  (instrument)
  (render))
