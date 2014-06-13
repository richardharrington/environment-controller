(ns environment-controller.core)

(def perfect-temp 70)
(def tolerance 5)

(def lower-temp-limit (- perfect-temp tolerance))
(def upper-temp-limit (+ perfect-temp tolerance))

(defn too-cold? [temp]
  (< temp lower-temp-limit))

(defn too-hot? [temp]
  (> temp upper-temp-limit))


(def heater-countdown (atom 0))

(def off-or-on
  {false :off
   true :on})



(defn tic [hvac]
  (let [{:keys [set-states! get-temp]} @hvac
        temp (get-temp)
        too-cold (too-cold? temp)
        too-hot (too-hot? temp)
        next-states {:heater (off-or-on too-cold)
                     :cooler (off-or-on too-hot)
                     :fan (off-or-on (or too-hot too-cold (> @heater-countdown 0)))}]
    (cond
     (= (next-states :heater) :on) (reset! heater-countdown 5)
     (> @heater-countdown 0) (swap! heater-countdown dec))
    (set-states! next-states)))



;; (defn tic [{get-temp :get-temp :as hvac}]
;;   (let [temp (get-temp)]
;;     (cond
;;      (< temp 65) {:cool nil, :heat true, :fan true}
;;      (> temp 75) {:cool true, :heat nil, :fan true}
;;      :otherwise  {:cool nil, :heat nil, :fan nil})))
