(ns environment-controller.hvac)

(defn make-hvac []
  (let [hvac (atom nil)]
    (reset! hvac
            {:get-temp (fn []) ; not implemented
             :states {} ; not implemented
             :set-states! (fn [states])}) ; not implemented
    hvac))

;; TODO: turn this into an interface.