(ns re-fail-app.components.button
  (:require ["react-bootstrap/Button" :as Button]
            [cljs.spec.alpha :as s]))

(s/def ::title string?)
(s/def ::disabled? boolean?)
(s/def ::outline? boolean?)
(s/def ::variant #{:primary
                   :secondary
                   :success
                   :danger
                   :warning
                   :info
                   :light
                   :dark
                   :link})

(s/def ::variant-args (s/keys :req-un [::title]
                              :opt-un [::disabled?
                                       ::outline?]))

(s/def ::button-args (s/merge ::variant-args
                              (s/keys :req-un [::variant])))

(s/fdef button
  :args (s/cat :button-args ::button-args))

(defn button
  [{:keys [title variant disabled? outline? on-click]}]
  [:> Button
   {:variant  (if outline?
                (str "outline-" (name variant))
                (name variant))
    :disabled (true? disabled?)
    :on-click on-click}
   title])

(s/fdef primary   :args (s/cat :args ::variant-args))
(s/fdef secondary :args (s/cat :args ::variant-args))
(s/fdef success   :args (s/cat :args ::variant-args))
(s/fdef danger    :args (s/cat :args ::variant-args))
(s/fdef warning   :args (s/cat :args ::variant-args))
(s/fdef info      :args (s/cat :args ::variant-args))

(defn primary   [args] [button (assoc args :variant :primary)])
(defn secondary [args] [button (assoc args :variant :secondary)])
(defn success   [args] [button (assoc args :variant :success)])
(defn danger    [args] [button (assoc args :variant :danger)])
(defn warning   [args] [button (assoc args :variant :warning)])
(defn info      [args] [button (assoc args :variant :info)])
