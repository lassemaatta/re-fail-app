(ns re-fail-app.app
  (:require [re-fail-app.views.core :as views]
            [re-frame.core :as rf]
            [reagent.dom :as rd]))

(enable-console-print!)

(defn- render
  []
  (rd/render [views/view]
             (.getElementById js/document "root")))

(defn ^:dev/after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export init
  []
  (render)
  (rf/dispatch-sync [::views/init]))
