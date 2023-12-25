(ns re-fail-app.views.core
  (:require [clojure.string :as str]
            [re-fail-app.components.button :as button]
            [re-fail-app.components.card :as card]
            [re-fail-app.components.collapse :as collapse]
            [re-fail-app.components.error :as error]
            [re-fail-app.components.form :as form]
            [re-fail-app.components.grid :as grid]
            [re-fail-app.components.list :as list]
            [re-fail-app.components.stack :as stack]
            [re-frame.core :as rf]
            [reagent.core :as r]))

(def <sub (comp deref rf/subscribe))

(defn- generate-todo
  [text]
  {::id    (random-uuid)
   ::title text
   ::state :draft})

(rf/reg-event-fx ::init
  (fn [{:keys [db]} _]
    {:db (->> [(generate-todo "Foo")
               (generate-todo "Bar")
               (generate-todo "Quuz")]
              (map (juxt ::id identity))
              (into {})
              (assoc db ::todos))}))

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
  :<- [::ids]
  (fn [ids [_ state]]
    (->> ids
         (filter (fn [id]
                   (let [todo (<sub [::by-id id])]
                     (= state (::state todo)))))
         doall)))

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
      [button/primary {:title     "Done"
                       :on-click  (fn [_]
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
  [id]
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
   (for [id (deref *id-sub)]
     (cond-> [todo-item id]
       true (with-meta {:key id})))])

(defn todo-list-card
  []
  [card/card {:header "Read-only view of TODOs"}
   [:div {:class "mb-3"}
    [card/text "All TODOs"]
    [todo-list (rf/subscribe [::ids])]]
   [:div {:class "mb-3"}
    [card/text "TODOs in draft state"]
    [todo-list (rf/subscribe [::ids-by-state :draft])]]
   [:div
    [card/text "TODOs in done state"]
    [todo-list (rf/subscribe [::ids-by-state :done])]]])

(defn edit-state
  [id state]
  [button/group {:class "ms-auto"}
   (for [choice [:draft :blocked :done]]
     ^{:key choice}
     [button/primary
      {:title     (name choice)
       :disabled? (= choice state)
       :on-click #(rf/dispatch [::set-state id choice])}])])

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


(defn view
  []
  [error/boundary
   [grid/container {:class "mt-2"}
    [grid/row {:class "mb-3"}
     [grid/column {}
      [todo-edit-list-card]]
     [grid/column {}
      [todo-editor (<sub [::edit-id])]]]
    [grid/row {}
     [grid/column {}
      [todo-list-card]]]]])
