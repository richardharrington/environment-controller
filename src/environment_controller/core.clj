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

(defn tic [hvac]
  (let [{:keys [states set-states! get-temp]} @hvac
        temp (get-temp)
        next-states (get-next-states states temp @cooler-countdown @heater-countdown)]
    (cond
     (next-states :heater) (reset! heater-countdown 5)
     (> @heater-countdown 0) (swap! heater-countdown dec))
    (cond
     (and (states :cooler) (not (next-states :cooler))) (reset! cooler-countdown 2)
     (> @cooler-countdown 0) (swap! cooler-countdown dec))
    (set-states! next-states)))


