(ns re-fail-app.components.form
  (:require ["react-bootstrap/Form" :as Form]
            [clojure.set :as set]
            [re-fail-app.components.common :as common]
            [reagent.core :as r]))

(defn label
  [{:keys [text] :as opts}]
  [:> Form/Label
   (dissoc opts :text)
   text])

(defn control
  [{:keys [value]}]
  (let [value          (or value "")
        debounced      (common/debounced (fn [callback value]
                                           (callback value)) 300)
        ;; What value we last received from the caller
        *last-value    (r/atom value)
        ;; TODO: separate *reported-value?
        ;; What value to display
        *current-value (r/atom value)]
    (fn [{:keys [value on-change] :as opts}]
      (let [value (or value "")]
        ;; Check if value changed due to some other actor
        (when (and (not= (deref *last-value) value)
                   (not= (deref *current-value) value))
          (reset! *current-value value)
          (when (= "" value)
            ;; Mock update to drop any pending calls
            (debounced (constantly nil) value)))
        (reset! *last-value value)
        [:> Form/Control
         (-> opts
             (assoc :value (deref *current-value))
             (set/rename-keys {:disabled? :disabled})
             (assoc :on-change (fn [e]
                                 (let [value (-> e .-target .-value)]
                                   (reset! *current-value value)
                                   (debounced on-change value)))))]))))

(def ^:private input-component
  "Reagent input wrapped within the Form/Control.
  See https://github.com/reagent-project/reagent/blob/master/doc/examples/material-ui.md#material-ui"
  (r/reactify-component (fn [props] [:input props])))

(defn text
  [opts]
  [control (assoc opts :as input-component)])

(def ^:private textarea-component
  (r/reactify-component (fn [props] [:textarea props])))

(defn text-area
  [opts]
  [control (assoc opts :as textarea-component)])

(defn group
  [opts & children]
  (into [:> Form/Group
         (set/rename-keys opts {:id :controlId})]
        children))

(defn form
  [opts & children]
  (into [:> Form opts]
        children))
