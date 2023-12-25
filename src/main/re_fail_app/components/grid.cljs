(ns re-fail-app.components.grid
  (:require ["react-bootstrap/Col$default" :as Col]
            ["react-bootstrap/Container" :as Container]
            ["react-bootstrap/Row" :as Row]))

(defn container
  [opts & children]
  (into [:> Container opts] children))

(defn row
  [opts & children]
  (into [:> Row opts] children))

(defn column
  [opts & children]
  (into [:> Col opts] children))
