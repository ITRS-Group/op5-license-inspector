(ns com.itrsgroup.op5-license-inspector
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.string :as str]
            [trptcolin.versioneer.core :refer [get-version]]
            [camel-snake-kebab.core :refer [->snake_case_string]])
  (:import (java.util Base64)
           (java.time Instant ZonedDateTime ZoneOffset))
  (:gen-class))

(defn license-file?
  [path]
  (let [f (slurp path)]
    (and (str/starts-with? f "-----BEGIN OP5 LICENSE BLOCK-----\n")
         (str/ends-with? f "\n-----END OP5 LICENSE BLOCK-----\n"))))

(defn decoded-license
  [path]
  (as-> (slurp path) <>
        (str/replace <> #"-{5}.+-{5}" "")
        (str/replace <> #"\n" "")
        (String. (.decode (Base64/getDecoder) <>))))

(defn object-re [s]
  (re-pattern (str "\\[\"" s "\"\\] = \"(.+)\"")))

(defn object-value [k s]
  (let [v (-> (->snake_case_string k)
              object-re
              (re-find s)
              second)]
    (try
      (if (integer? (read-string v))
        (read-string v)
        v)
      (catch Exception _ v))))

(defn validity-map [s]
  (as-> (re-find #"license_time\", time, (\d+), (\d+)" s) <>
        (rest <>)
        (map read-string <>)
        (map #(Instant/ofEpochSecond %) <>)
        (map #(ZonedDateTime/ofInstant % (ZoneOffset/ofHours 3)) <>)
        (map str <>)
        (map #(str/split % #"T") <>)
        (map first <>)
        (hash-map :valid-from (first <>) :valid-until (second <>))))

(defn custom-values-map [s]
  (as-> (re-find #"(?is)\[\"custom\"\] = \{(.*)\}," s) <>
        (second <>)
        (str/trim <>)
        (str/split-lines <>)
        (map str/trim <>)
        (map #(re-seq #"\"([\w\s]+)\"" %) <>)
        (for [i <>] (map second i))
        (zipmap (map first <>) (map second <>))
        (dissoc <> "company_name")
        (hash-map :custom <>)))

(def objects [:company-name
              :hosts
              :services
              :peers
              :pollers
              :customer-account
              :customer-id
              :recipient
              :branding])

(defn objects-map [s]
  (->> objects
       (map #(hash-map % (object-value % s)))
       (apply merge)))

(defn logger?  [s] (not (re-find #"Your logger is limited to 1 host" s)))
(defn trapper? [s] (not (re-find #"Your license does not include the Trapper module" s)))
(defn nagvis?  [s] (not (re-find #"Your license does not include the NagVis module" s)))
(defn bsm?     [s] (not (re-find #"monitor.bsm:\"\) and true then\n\t\t\treturn false" s)))
(defn reports? [s] (not (re-find #"The reports module isn't included in your license" s)))

(defn modules-map [s]
  {:modules {:logger (logger? s)
             :trapper (trapper? s)
             :nagvis (nagvis? s)
             :bsm (bsm? s)
             :reports (reports? s)}})

(defn license-map
  [path]
  (let [s (decoded-license path)]
    (apply merge (objects-map s)
                 (validity-map s)
                 (custom-values-map s)
                 (modules-map s))))

(defn license-info
  [path]
  (remove nil?
          (let [m (license-map path)]
           [(str "Recipient:    " (:recipient m))
            (str "Company:      " (:company-name m))
            (str "Customer ID:  " (:customer-id m))
            ""
            (str "Valid from:   " (:valid-from m))
            (str "Valid to:     " (:valid-until m))
            ""
            (str "License type: " (:branding m))
            ""
            (str "Hosts:        " (:hosts m))
            (str "Services:     " (:services m))
            (str "Peers:        " (:peers m))
            (str "Pollers:      " (:pollers m))
            ""
            (str "Logger:       " (:logger  (:modules m)))
            (str "Trapper:      " (:trapper (:modules m)))
            (str "NagVis:       " (:nagvis  (:modules m)))
            (str "BSM:          " (:bsm     (:modules m)))
            (str "Reports:      " (:reports (:modules m)))
            (when (seq (:custom m)) (str "\nCustom data:  " (:custom m)))])))

(def version-number
  "The version number as defined in project.clj."
  ;; Note that this is evaluated at build time by native-image.
  (get-version "com.itrsgroup" "op5-license-inspector"))
;;
;; Beginning of command line parsing.

(defn cli-options []
  [["-h"
    "--help"
    "Print this help message."
    :default false]
   ["-v"
    "--version"
    "Print the current version number."
    :default false]])

(defn usage
  "Print a brief description and a short list of available options."
  [options-summary]
  (str/join
   \newline
   ["op5-license-inspector: Get some useful data from OP5 lic files."
    ""
    (str "Version: " version-number)
    ""
    "Usage: op5-license-inspector FILE"
    ""
    "Options:"
    options-summary
    ""
    "Examples:"
    ""
    "Command: $ op5-license-inspector op5license.lic"
    "Result:  Display useful data from the license file op5license.lic."]))

(def exit-messages
  "Exit messages used by `exit`."
  {:64 "ERROR: No license file provided."
   :65 "ERROR: More than one license file provided."
   :66 "ERROR: Invalid license file."})

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]}
        (parse-opts args (cli-options))]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :exit-code 0}
      ;;
      (:version options) ; version => exit OK with version number
      {:exit-message version-number :exit-code 0}
      ;;
      errors ; errors => exit with description of errors
      {:exit-message (str/join \newline errors) :exit-code 1}
      ;;
      (zero? (count arguments))
      {:exit-message (:64 exit-messages) :exit-code 64}
      ;;
      (< 1 (count arguments))
      {:exit-message (:65 exit-messages) :exit-code 65}
      ;;
      (not (license-file? (first arguments)))
      {:exit-message (:66 exit-messages) :exit-code 66}
      ;;
      (= 1 (count arguments))
      {:license-file (first arguments)}
      ;;
      :else
      {:exit-message (usage summary) :exit-code 1})))

;; End of command line parsing.

(defn exit
  "Print a `message` and exit the program with the given `status` code.
  See also [[exit-messages]]."
  [status message]
  (println message)
  (System/exit status))

(defn -main [& args]
  (let [{:keys [license-file exit-message exit-code]}
        (validate-args args)]
    (when exit-message
      (exit exit-code exit-message))
    (when license-file
      (doseq [l (license-info license-file)]
        (println l))))
  (System/exit 0))
