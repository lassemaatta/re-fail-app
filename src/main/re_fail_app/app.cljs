(ns re-fail-app.app
  (:require [re-fail-app.views.core :as views]
            [reagent.dom :as rd]
            [re-frame.core :as rf]))

(enable-console-print!)

(defn render
  []
  (rd/render [views/view]
             (.getElementById js/document "root")))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^:dev/after-load clear-cache-and-render!
  []
  (rf/clear-subscription-cache!)
  (render))

(defn ^:export init
  []
  (render))
