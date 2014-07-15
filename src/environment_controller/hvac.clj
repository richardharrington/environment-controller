(ns environment-controller.hvac)

(defprotocol ITemp
  (get-temp [this]))

(defprotocol IDeviceStates
  (set-device-states! [this states]))
