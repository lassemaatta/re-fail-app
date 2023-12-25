(ns re-fail-app.views.core
  (:require [clojure.string :as str]
            [re-fail-app.components.badge :as badge]
            [re-fail-app.components.button :as button]
            [re-fail-app.components.card :as card]
            [re-fail-app.components.collapse :as collapse]
            [re-fail-app.components.error :as error]
            [re-fail-app.components.form :as form]
            [re-fail-app.components.grid :as grid]
            [re-fail-app.components.list :as list]
            [re-fail-app.components.stack :as stack]
            [re-frame.core :as rf]
            [re-frame.subs :as rf-subs]
            [reagent.core :as r]))

(def <sub (comp deref rf/subscribe))

;; Effect for registering a watch into a regular Clojure atom
(rf/reg-fx
  ::register-watch
  (fn [{:keys [id atom on-change]}]
    (add-watch
      atom
      id
      (fn [_key _ref _old-val new-val]
        (on-change new-val)))))

(defn- generate-todo
  [text]
  {::id    (random-uuid)
   ::title text
   ::state :draft})

(rf/reg-event-fx ::init
  (fn [{:keys [db]} _]
    {:db              (->> [(generate-todo "Foo")
                            (generate-todo "Bar")
                            (generate-todo "Quuz")]
                           (map (juxt ::id identity))
                           (into {})
                           (assoc db ::todos))
     ;; The `re.frame.subs/query->reaction` atom contains each active (deduplicated) subscription
     ;; As it's a plain clojure atom, we can observe it with `add-watch`.
     ::register-watch {:id        ::sub-watch
                       :atom      rf-subs/query->reaction
                       :on-change (fn [new-val]
                                    (rf/dispatch [::set-sub-count (count new-val)]))}}))

(rf/reg-event-fx ::set-sub-count
  (fn [{:keys [db]} [_ c]]
    {:db (assoc db ::sub-count c)}))

(rf/reg-sub ::sub-count
  :-> ::sub-count)

(rf/reg-sub ::todos
  :-> ::todos)

(rf/reg-sub ::ids
  :<- [::todos]
  :-> keys)

(rf/reg-sub ::by-id
  :<- [::todos]
  (fn [todos [_ id]]
    (get todos id)))

(rf/reg-sub ::ids-by-state
  (fn []
    [(rf/subscribe [::ids])
     (rf/subscribe [::bug-enabled? ::lazy-sub])])
  (fn [[ids lazy-sub?] [_ state]]
    (let [lazy-result (filter (fn [id]
                                (let [todo (<sub [::by-id id])]
                                  (= state (::state todo))))
                              ids)]
      ;; This seq contains calls to `<sub`. If it is realized here
      ;; eagerly, re-frame will realize this sub depends on `::by-id` and
      ;; re-run this sub when the TODO state changes. However, if the lazy
      ;; seq is not realized, this sub will re-run only when `::ids`
      ;; (or the bug flag) changes.
      (if lazy-sub?
        lazy-result
        (doall lazy-result)))))

(rf/reg-sub ::new-todo
  (fn [{::keys [new-todo]}]
    (or new-todo {::state :draft})))

(rf/reg-sub ::edit-data
  (fn [[_ id]]
    (if id
      (rf/subscribe [::by-id id])
      (rf/subscribe [::new-todo])))
  :-> identity)

(rf/reg-sub ::edit-title
  (fn [[_ id]]
    (rf/subscribe [::edit-data id]))
  :-> ::title)

(rf/reg-sub ::edit-description
  (fn [[_ id]]
    (rf/subscribe [::edit-data id]))
  :-> ::description)

(rf/reg-sub ::can-save?
  (fn [[_ id]]
    (rf/subscribe [::edit-title id]))
  :-> some?)

(rf/reg-event-fx ::set-title
  (fn [{:keys [db]} [_ id title]]
    {:db (if id
           (assoc-in db [::todos id ::title] title)
           (assoc-in db [::new-todo ::title] title))}))

(rf/reg-event-fx ::set-description
  (fn [{:keys [db]} [_ id title]]
    {:db (if id
           (assoc-in db [::todos id ::description] title)
           (assoc-in db [::new-todo ::description] title))}))

(rf/reg-event-fx ::set-state
  (fn [{:keys [db]} [_ id state]]
    {:db (assoc-in db [::todos id ::state] state)}))

(rf/reg-event-fx ::save
  (fn [{:keys [db]} _]
    (let [todo (-> (::new-todo db)
                   (assoc ::id (random-uuid)))]
      {:db (-> db
               (update ::todos conj [(::id todo) todo])
               (dissoc ::new-todo))})))

(rf/reg-event-fx ::delete
  (fn [{:keys [db]} [_ id]]
    {:db (update db ::todos dissoc id)}))

(rf/reg-event-fx ::begin-editing
  (fn [{:keys [db]} [_ id]]
    {:db (assoc db ::editing id)}))

(rf/reg-event-fx ::finish-editing
  (fn [{:keys [db]} _]
    {:db (dissoc db ::editing)}))

(rf/reg-sub ::edit-id
  :-> ::editing)

(rf/reg-event-fx ::toggle-bug
  (fn [{:keys [db]} [_ bug-id]]
    {:db (update db ::bugs (fnil (fn [bugs]
                                   (if (contains? bugs bug-id)
                                     (disj bugs bug-id)
                                     (conj bugs bug-id)))
                                 #{}) bug-id)}))

(rf/reg-sub ::bugs
  (fn [{::keys [bugs]}]
    (set bugs)))

(rf/reg-sub ::bug-enabled?
  :<- [::bugs]
  (fn [bugs [_ bug-id]]
    (contains? bugs bug-id)))

(defn- edit-title
  [id]
  (let [title (<sub [::edit-title id])]
    [form/group {:id    "title"
                 :class "mb-3"}
     [form/label {:text "Title"}]
     [form/text {:placeholder "Title for TODO"
                 :value       title
                 :on-change   (fn [value]
                                (rf/dispatch [::set-title id value]))}]]))

(defn- edit-description
  [id]
  (let [description (<sub [::edit-description id])]
    [form/group {:id    "description"
                 :class "mb-3"}
     [form/label {:text "Description"}]
     [form/text-area
      {:placeholder "Description for TODO"
       :value       description
       :on-change   (fn [value]
                      (rf/dispatch [::set-description id value]))}]]))

(defn todo-editor
  [id]
  [card/card {:header (if id
                        "Edit TODO"
                        "Create new TODO")}
   [form/form {}
    [edit-title id]
    [edit-description id]
    (if id
      [button/primary {:title    "Done"
                       :on-click (fn [_]
                                    (rf/dispatch [::finish-editing]))}]
      [button/primary {:title     "Create"
                       :disabled? (not (<sub [::can-save? id]))
                       :on-click  (fn [_]
                                    (rf/dispatch [::save]))}])]])

(def state->variant
  {:draft   nil
   :blocked :warning
   :done    :success})

(defn todo-item
  [id _ignored]
  (r/with-let [*open  (r/atom false)
               toggle #(swap! *open not)]
    (let [{::keys [title state description]} (<sub [::by-id id])
          variant                            (state->variant state)]
      [list/item
       (cond-> {:on-click toggle}
         variant (assoc :variant variant))
       [:div
        [:div.fw-bold title]
        [collapse/collapse {:in (deref *open)}
         (->> (-> (or description "There's no description.")
                  (str/split #"\n"))
              (map (fn [text]
                     [:p text]))
              (into [:<>]))]]])))

(defn todo-list
  [*id-sub]
  [list/group* {}
   (doall
     (for [id (deref *id-sub)]
       ;; We produce a seq of elements where each element should contain
       ;; a unique :key. If the key is not provided, the elements are
       ;; identified by their position. This is problematic if the elements
       ;; contain state (e.g. reagent ratoms) as these will be bound to
       ;; a particular position! -> Adding or removing elements will move
       ;; the existing elements around but not the ratoms.
       (let [include-key? (not (<sub [::bug-enabled? ::no-key]))]
         ;; Supply a dummy property to refresh component when bug is toggled
         (cond-> [todo-item id include-key?]
           include-key? (with-meta {:key id})))))])

(defn random-query-button
  []
  [button/secondary
   {:title    "Query a random todo"
    :on-click (fn [_]
                ;; Each button press will register a new subscription,
                ;; which will never be cleaned up
                (<sub [::by-id (random-uuid)]))}])

(defn existing-query-button
  []
  [button/secondary
   {:title    "Query all TODO ids"
    :on-click (fn [_]
                ;; Here the query is a) a constant and b) used elsewhere in the program
                ;; so no uncontrolled cache leaking occurs
                (println "Found:" (<sub [::ids])))}])

(defn todo-list-card
  []
  [card/card {:header "Read-only view of TODOs"}
   [:div {:class "mb-3"}
    [card/text "All TODOs"]
    [todo-list (rf/subscribe [::ids])]]
   [:div {:class "mb-3"}
    [card/text "TODOs in draft state"]
    [todo-list (rf/subscribe [::ids-by-state :draft])]]
   [:div {:class "mb-3"}
    [card/text "TODOs in done state"]
    [todo-list (rf/subscribe [::ids-by-state :done])]]
   [:div
    (when (<sub [::bug-enabled? ::unique-sub-js])
      [random-query-button])
    (when (<sub [::bug-enabled? ::existing-sub-js])
      [existing-query-button])]])

(defn edit-state
  [id state]
  [button/group {:class "ms-auto"}
   (for [choice [:draft :blocked :done]]
     ^{:key choice}
     [button/primary
      {:title     (name choice)
       :disabled? (= choice state)
       :on-click  #(rf/dispatch [::set-state id choice])}])])

(defn todo-item-edit
  [id]
  (let [{::keys [title state]} (<sub [::by-id id])
        variant                (state->variant state)]
    ^{:key id}
    [list/item (cond-> {}
                 variant (assoc :variant variant))
     [stack/stack {:direction :horizontal
                   :gap       3}
      [:div title]
      [edit-state id state]
      [button/primary {:title    "Edit"
                       :on-click #(rf/dispatch [::begin-editing id])}]
      [button/danger {:title    "Remove"
                      :on-click #(rf/dispatch [::delete id])}]]]))

(defn todo-edit-list
  []
  [list/group* {}
   (for [id (<sub [::ids])]
     ^{:key (str id)} [todo-item-edit id])])

(defn todo-edit-list-card
  []
  [card/card {:header "List of TODOs to edit"}
   [todo-edit-list]])

(defn- bug
  [bug-id label description]
  [list/item {}
   [stack/stack {:direction :horizontal
                 :gap       3}
    [button/toggle-button
     {:id        (str "toggle-bug-" bug-id)
      :title     label
      :variant   :danger
      :outline?  true
      :checked?  (<sub [::bug-enabled? bug-id])
      :on-change #(rf/dispatch [::toggle-bug bug-id])}]
    [:span description]]])

(def bugs [{:id          ::no-key
            :label       "No :key meta"
            :description "Don't assign `id` as `:key` when listing TODOs"}
           {:id          ::lazy-sub
            :label       "Lazy sub"
            :description "Don't realize lazy sequences in subscriptions."}
           {:id          ::existing-sub-js
            :label       "Cache leak (safe)"
            :description "Add button for subscribing to an existing query in a callback"}
           {:id          ::unique-sub-js
            :label       "Cache leak"
            :description "Add button for subscribing to a unique query in a callback"}])

(defn bug-o-matic
  []
  [card/card {:header "Introduce bugs"}
   [list/group* {}
    (for [{:keys [id label description]} bugs]
      ^{:key id} [bug id label description])]])

(defn stats
  []
  [card/card {:header "Info"}
   [list/group {}
    [list/item {}
     [stack/stack {:direction :horizontal
                   :gap       3}
      [:span "Re-frame subscription cache size:"]
      [badge/badge {:variant :primary}
       (<sub [::sub-count])]]]]])

(defn view
  []
  [error/boundary
   [grid/container {:class "mt-2"}
    [grid/row {:class "g-3"}
     [grid/column {:class "col-6"}
      [todo-edit-list-card]]
     [grid/column {:class "col-6"}
      [todo-editor (<sub [::edit-id])]]
     [grid/column {:class "col-8"}
      [bug-o-matic]]
     [grid/column {:class "col-4"}
      [stats]]
     [grid/column {}
      [todo-list-card]]]]])
