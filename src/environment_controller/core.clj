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
(def cooler-countdown (atom 0))
(def stored-states (atom {:heater :off, :cooler :off, :fan :off}))

(def off-or-on
  {false :off
   true :on})

(defn tic [hvac]
  (let [{set-hvac-states! :set-states!, get-hvac-temp :get-temp} @hvac
        temp (get-hvac-temp)
        too-cold (too-cold? temp)
        too-hot (too-hot? temp)
        next-states {:heater (off-or-on too-cold)
                     :cooler (off-or-on (and too-hot (= @cooler-countdown 0)))
                     :fan (off-or-on (or too-hot too-cold (> @heater-countdown 0)))}]
    (cond
     (= (next-states :heater) :on) (reset! heater-countdown 6)
     (> @heater-countdown 0) (swap! heater-countdown dec))
    (cond
     (and (= (next-states :cooler) :off) (= (@stored-states :cooler) :on)) (reset! cooler-countdown 3)
     (> @cooler-countdown 0) (swap! cooler-countdown dec))
    (reset! stored-states next-states)
    (set-hvac-states! next-states)))

