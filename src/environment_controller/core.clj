(ns environment-controller.core)


(def perfect-temp 70)
(def tolerance 5)
(def lower-temp-limit (- perfect-temp tolerance))
(def upper-temp-limit (+ perfect-temp tolerance))


(def heater-countdown-store (atom 0))
(def cooler-countdown-store (atom 0))


(defn get-next-states [states temp cooler-countdown heater-countdown]
  (let [too-cold? (< temp lower-temp-limit)
        too-hot? (> temp upper-temp-limit)]
    {:heater too-cold?
     :cooler (and too-hot? (= cooler-countdown 0))
     :blower (or too-hot? too-cold? (> heater-countdown 0))}))

(defn get-next-heater-countdown [next-heater-state heater-countdown]
  (cond
   next-heater-state 5
   (> heater-countdown 0) (dec heater-countdown)
   :else heater-countdown))

(defn get-next-cooler-countdown [cooler-state next-cooler-state cooler-countdown]
  (cond
   (and cooler-state (not next-cooler-state)) 2
   (> cooler-countdown 0) (dec cooler-countdown)
   :else cooler-countdown))

(defn tic! [hvac]
  (let [{:keys [states set-states! get-temp]} @hvac
        temp (get-temp)
        cooler-state (:cooler states)
        heater-countdown @heater-countdown-store
        cooler-countdown @cooler-countdown-store
        {next-heater-state :heater, next-cooler-state :cooler :as next-states}
          (get-next-states states temp cooler-countdown heater-countdown)
        next-heater-countdown (get-next-heater-countdown next-heater-state heater-countdown)
        next-cooler-countdown (get-next-cooler-countdown cooler-state next-cooler-state cooler-countdown)]

    (reset! heater-countdown-store next-heater-countdown)
    (reset! cooler-countdown-store next-cooler-countdown)
    (set-states! next-states)))


