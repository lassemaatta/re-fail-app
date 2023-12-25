(ns re-fail-app.components.error
  (:require [reagent.core :as r]))

(def ^:dynamic *error-printer* nil)

(defn- display-error
  [err]
  (if-let [data (ex-data err)]
    [:<>
     [:p (ex-message err)]
     (when *error-printer*
       [:pre
        [:code (*error-printer* err)]])
     [:pre
      [:code (pr-str data)]]]
    [:pre [:code (pr-str err)]]))

(defn boundary
  [& _children]
  (let [*error (r/atom nil)]
    (r/create-class
      {:display-name                 "ErrorBoundary"
       :component-did-catch          (fn [_this _e _info])
       :get-derived-state-from-error (fn [err]
                                       (reset! *error err))
       :reagent-render               (fn [& children]
                                       (let [err (deref *error)]
                                         (if err
                                           (display-error err)
                                           (into [:<>] children))))})))
