(ns sanitize-filename.core
  (:require [clojure.string :as s]))

;; Resources on valid file names
;; * https://msdn.microsoft.com/en-us/library/aa365247(v=vs.85).aspx#naming_conventions

(def CHARACTER_FILTER #"[\x00-\x1F\x80-\x9f\/\\:\*\?\"<>\|]")
(def INVALID_TRAILING_CHARS #"[\. ]+$")
(def UNICODE_WHITESPACE #"\p{Space}")
(def WINDOWS_RESERVED_NAMES #"^(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(\..*)?$")
(def RESERVED_NAMES #"^\.+$")
(def FALLBACK_FILENAME "file")

(defn- filter-windows-reserved-names [filename]
  (if (re-matches WINDOWS_RESERVED_NAMES filename)
    FALLBACK_FILENAME
    filename
    )
  )

(defn- filter-blank [filename]
  (if (s/blank? filename)
    FALLBACK_FILENAME
    filename
    )
  )

(defn- filter-reserved-names [filename]
  (if (re-matches RESERVED_NAMES filename)
    FALLBACK_FILENAME
    filename
    )
  )

(defn- filter-invalid-trailing-chars [filename]
  (s/replace filename
             INVALID_TRAILING_CHARS
             #(s/join "" (take (count %1) (repeat "$")))))

(defn- -filter [filename]
  (-> filename
      filter-windows-reserved-names
      filter-reserved-names
      filter-blank
      filter-invalid-trailing-chars
      )
  )

(defn- -sanitize [filename]
  (-> filename
      ;(s/replace CHARACTER_FILTER ""))
      ; NOTE: different with zaru
      ; replace with $, to indicate that there was a special character
      (s/replace CHARACTER_FILTER (s/re-quote-replacement "$")))
  )

(defn- truncate [filename]
  (if (> (.length filename) 254)
    (.substring filename 0 254)
    filename)
  )

;; exported function
(defn sanitize [filename]
  (-> filename
      -sanitize
      -filter
      truncate)
  )

;(defn -main []
;  should print "$a$b$  我是c.zip"
;  (println (sanitize "/a/b/  我是c.zip")))
