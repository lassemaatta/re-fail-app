(ns re-fail-app.components.collapse
  (:require ["react-bootstrap/Collapse" :as Collapse]))

(defn collapse
  [opts & children]
  [:> Collapse opts
   (into [:div] children)])
