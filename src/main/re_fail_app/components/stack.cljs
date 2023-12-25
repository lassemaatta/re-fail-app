(ns re-fail-app.components.stack
  (:require ["react-bootstrap/Stack" :as Stack]))

(defn stack
  [opts & children]
  (into [:> Stack opts] children))
