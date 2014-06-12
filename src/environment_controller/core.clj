(ns environment-controller.core)

(defn tic [hvac]
  (let [{:keys [set-states get-temp]} @hvac
        temp (get-temp)]
    (cond
     (> temp 75) (set-states {:heater :off, :cooler :on, :fan :on})
     (< temp 65) (set-states {:heater :on, :cooler :off, :fan :on})
     :otherwise (set-states {:heater :off, :cooler :off, :fan :off}))))


;; (defn tic [{get-temp :get-temp :as hvac}]
;;   (let [temp (get-temp)]
;;     (cond
;;      (< temp 65) {:cool nil, :heat true, :fan true}
;;      (> temp 75) {:cool true, :heat nil, :fan true}
;;      :otherwise  {:cool nil, :heat nil, :fan nil})))
