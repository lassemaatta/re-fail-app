(ns re-fail-app.components.common
  (:require [goog.functions]))

(defn debounced
  [f interval]
  (goog.functions.debounce f interval))
