(ns re-fail-app.components.spec.common
  (:require [cljs.spec.alpha :as s]))

(s/def ::variant #{:primary
                   :secondary
                   :success
                   :danger
                   :warning
                   :info
                   :light
                   :dark
                   :link})

(s/def ::hiccup (s/or :string string?
                      :simple (s/and vector?
                                     (s/cat :tag keyword?
                                            :opts (s/? map?)
                                            :children (s/* ::hiccup)))
                      :component (s/and vector?
                                        (s/cat :fn #(instance? js/Object %)
                                               :args (s/* any?)))))
