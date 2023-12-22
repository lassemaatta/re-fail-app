(ns re-fail-app.components.grid
  (:require ["react-bootstrap/Col$default" :as Col]
            ["react-bootstrap/Container" :as Container]
            ["react-bootstrap/Row" :as Row]))

(defn container
  [args & children]
  (into [:> Container args] children))

(defn row
  [args & children]
  (into [:> Row args] children))

(defn column
  [args & children]
  (into [:> Col args] children))
