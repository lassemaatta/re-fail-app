(ns re-fail-app.components.badge
  (:require ["react-bootstrap/Badge" :as Badge]))

(defn badge
  [opts text]
  [:> Badge opts text])
