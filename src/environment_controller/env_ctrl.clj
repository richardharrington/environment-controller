(ns environment-controller.env-ctrl)


(def perfect-temp 70)
(def tolerance 5)
(def lower-temp-limit (- perfect-temp tolerance))
(def upper-temp-limit (+ perfect-temp tolerance))


(def heater-countdown-store (atom 0))
(def cooler-countdown-store (atom 0))


(defn get-next-states [temp cooler-countdown heater-countdown]
  (let [too-cold? (< temp lower-temp-limit)
        too-hot? (> temp upper-temp-limit)]
    {:heater-on? too-cold?
     :cooler-on? (and too-hot? (= cooler-countdown 0))
     :blower-on? (or too-hot? too-cold? (> heater-countdown 0))}))

(defn get-next-heater-countdown [next-heater-on? heater-countdown]
  (cond
   next-heater-on? 5
   (> heater-countdown 0) (dec heater-countdown)
   :else heater-countdown))

(defn get-next-cooler-countdown [cooler-on? next-cooler-on? cooler-countdown]
  (cond
   (and cooler-on? (not next-cooler-on?)) 2
   (> cooler-countdown 0) (dec cooler-countdown)
   :else cooler-countdown))

(defn tic! [hvac]
  (let [{:keys [states set-states! get-temp]} @hvac
        cooler-on? (:cooler-on? states)
        heater-countdown @heater-countdown-store
        cooler-countdown @cooler-countdown-store
        temp (get-temp)
        {next-heater-on? :heater-on?, next-cooler-on? :cooler-on? :as next-states}
          (get-next-states temp cooler-countdown heater-countdown)
        next-heater-countdown (get-next-heater-countdown next-heater-on? heater-countdown)
        next-cooler-countdown (get-next-cooler-countdown cooler-on? next-cooler-on? cooler-countdown)]

    (reset! heater-countdown-store next-heater-countdown)
    (reset! cooler-countdown-store next-cooler-countdown)
    (set-states! next-states)))


