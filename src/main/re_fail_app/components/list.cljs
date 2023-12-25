(ns re-fail-app.components.list
  (:require ["react-bootstrap/ListGroup" :as ListGroup]))

(defn group
  "A list group"
  [opts & children]
  (into [:> ListGroup opts] children))

(defn group*
  "Like [[group]], but takes a sequence of children"
  [opts children]
  [:> ListGroup opts children])

(defn item
  [{:keys [on-click] :as opts} & children]
  (into [:> ListGroup/Item
         (cond-> opts
           on-click (assoc :action "action"))]
        children))
