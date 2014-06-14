(ns environment-controller.core)


(def perfect-temp 70)
(def tolerance 5)
(def lower-temp-limit (- perfect-temp tolerance))
(def upper-temp-limit (+ perfect-temp tolerance))


(def heater-countdown (atom 0))
(def cooler-countdown (atom 0))


(defn get-next-states [states temp cooler-count heater-count]
  (let [too-cold? (< temp lower-temp-limit)
        too-hot? (> temp upper-temp-limit)]
    {:heater too-cold?
     :cooler (and too-hot? (= cooler-count 0))
     :blower (or too-hot? too-cold? (> heater-count 0))}))

(defn get-next-heater-countdown [next-heater-state heater-count]
  (cond
   next-heater-state 5
   (> heater-count 0) (dec heater-count)
   :else heater-count))

(defn get-next-cooler-countdown [cooler-state next-cooler-state cooler-count]
  (cond
   (and cooler-state (not next-cooler-state)) 2
   (> cooler-count 0) (dec cooler-count)
   :else cooler-count))

(defn tic! [hvac]
  (let [{:keys [states set-states! get-temp]} @hvac
        temp (get-temp)
        cooler-state (:cooler states)
        {next-heater-state :heater, next-cooler-state :cooler :as next-states}
          (get-next-states states temp @cooler-countdown @heater-countdown)
        next-heater-countdown (get-next-heater-countdown next-heater-state @heater-countdown)
        next-cooler-countdown (get-next-cooler-countdown cooler-state next-cooler-state @cooler-countdown)]

    (reset! heater-countdown next-heater-countdown)
    (reset! cooler-countdown next-cooler-countdown)
    (set-states! next-states)))


