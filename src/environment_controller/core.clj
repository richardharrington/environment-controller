(ns environment-controller.core)

(defn tic [{get-temp :get-temp :as hvac}]
  (let [temp (get-temp)]
    (cond
     (< temp 65) {:cool nil, :heat true, :fan true}
     (> temp 75) {:cool true, :heat nil, :fan true}
     :otherwise  {:cool nil, :heat nil, :fan nil})))
