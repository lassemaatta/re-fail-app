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

;; Tree of hiccup elements
(s/def ::hiccup (s/or :string string?
                      :simple (s/and vector?
                                     (s/cat :tag keyword?
                                            :opts (s/? map?)
                                            :children ::hiccup-vals))
                      :component (s/and vector?
                                        (s/cat :fn #(instance? js/Object %)
                                               :args (s/* any?)))))

;; When using map/for to produce a seq of child elements
(s/def ::hiccup-seq (s/coll-of ::hiccup :kind seq?))

;; A regex spec for 0..N hiccup elements
(s/def ::hiccup-vals (s/* ::hiccup))
