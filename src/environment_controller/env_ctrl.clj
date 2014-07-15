(ns environment-controller.env-ctrl
  (:require [environment-controller.hvac :as hvac]))


(def perfect-temp 70)
(def tolerance 5)
(def heater-cooldown-period 5)
(def cooler-recharge-period 3)

(def lower-temp-limit (- perfect-temp tolerance))
(def upper-temp-limit (+ perfect-temp tolerance))
(def initial-devices-state {:heater-on? false, :cooler-on? false, :blower-on? false})


(def heater-countdown-store (atom 0))
(def cooler-countdown-store (atom 0))
(def last-states-store (atom initial-devices-state))


(defn get-next-states [temp cooler-countdown heater-countdown]
  (let [too-cold? (< temp lower-temp-limit)
        too-hot? (> temp upper-temp-limit)]
    {:heater-on? too-cold?
     :cooler-on? (and too-hot? (= cooler-countdown 0))
     :blower-on? (or too-hot? too-cold? (> heater-countdown 0))}))

(defn get-next-heater-countdown [next-heater-on? heater-countdown]
  (cond
   next-heater-on? (dec heater-cooldown-period)
   (> heater-countdown 0) (dec heater-countdown)
   :else heater-countdown))

(defn get-next-cooler-countdown [cooler-on? next-cooler-on? cooler-countdown]
  (cond
   (and cooler-on? (not next-cooler-on?)) (dec cooler-recharge-period)
   (> cooler-countdown 0) (dec cooler-countdown)
   :else cooler-countdown))

(defn tic! [hvac]
  (let [states @last-states-store
        cooler-on? (:cooler-on? states)
        heater-countdown @heater-countdown-store
        cooler-countdown @cooler-countdown-store
        temp (hvac/get-temp hvac)
        {next-heater-on? :heater-on?, next-cooler-on? :cooler-on? :as next-states}
          (get-next-states temp cooler-countdown heater-countdown)
        next-heater-countdown (get-next-heater-countdown next-heater-on? heater-countdown)
        next-cooler-countdown (get-next-cooler-countdown cooler-on? next-cooler-on? cooler-countdown)]

    (reset! heater-countdown-store next-heater-countdown)
    (reset! cooler-countdown-store next-cooler-countdown)
    (reset! last-states-store next-states)
    (hvac/set-device-states! hvac next-states)))


