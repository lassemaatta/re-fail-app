(ns re-fail-app.components.button
  (:require ["react-bootstrap/Button" :as Button]
            ["react-bootstrap/ButtonGroup" :as ButtonGroup]))

(defn button
  [{:keys [class title variant type disabled? outline? on-click]}]
  [:> Button
   {:variant  (if outline?
                (str "outline-" (name variant))
                (name variant))
    :type     (when type (name type))
    :class    class
    :disabled (true? disabled?)
    :on-click (fn [e]
                (.preventDefault e)
                (on-click e)
                nil)}
   title])

(defn primary   [opts] [button (assoc opts :variant :primary)])
(defn secondary [opts] [button (assoc opts :variant :secondary)])
(defn success   [opts] [button (assoc opts :variant :success)])
(defn danger    [opts] [button (assoc opts :variant :danger)])
(defn warning   [opts] [button (assoc opts :variant :warning)])
(defn info      [opts] [button (assoc opts :variant :info)])

(defn group
  [opts & children]
  (into [:> ButtonGroup opts] children))
