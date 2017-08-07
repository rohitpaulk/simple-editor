(ns simple-editor.core)

(require '[lanterna.terminal :as t])

(defn render
  "Renders lines + cursor to the terminal"
  [term, {:keys [lines, cursor]}]
  (t/clear term)
  (defn put-line
    "Writes the string to a terminal and prints a newline"
    [line]
    (t/put-string term (str line "\n")))
  (dorun (map put-line lines))
  (t/move-cursor term (:x cursor) (:y cursor)))

(defn str-insert
    "Insert c in string s at index i."
      [s c i]
        (str (subs s 0 i) c (subs s i)))

(defn process-char
  "
  Inserts a character at the cursor position, moves the cursor forward
  "
  [char {:keys [lines cursor]}]
  (let [line-to-modify (nth lines (:y cursor))
        modified-line (str-insert line-to-modify char (:x cursor))
        new-lines (assoc lines (:y cursor) modified-line)
        new-cursor (assoc cursor :x (+ (:x cursor) 1))]
    {:lines new-lines :cursor new-cursor}))

(defn process-key
  "
  Processes one input character from the terminal.

  Returns the new state.
  "
  [key state]
  ; TODO: Process other stuff?
  (process-char key, state))

(defn -get-lines
  "Returns an array of lines, given a file path"
  [file-path]
  (def file-contents (slurp file-path))
  (clojure.string/split-lines file-contents))

(defn processable-keys
  "Returns a lazy sequence that terminates when a user presses escape"
  [term]

  (defn nil-if-escape
    [key]
    (if (= key :escape) nil key))

  (defn da-func []
    (nil-if-escape (t/get-key-blocking  term)))

  (defn is-char [x]
    (not= clojure.lang.Keyword (type x)))

  (filter is-char (take-while identity (repeatedly da-func))))

(defn open-editor
  "Opens the editor with the given file"
  [file-path]
  (def lines (-get-lines file-path))
  (def cursor {:x 0 :y 0})
  (def state {:lines lines :cursor cursor})
  (def term (t/get-terminal :unix))
  (t/in-terminal term
    (render term state)
    (doseq [key (processable-keys term)]
      (def state (process-key key state))
      (render term state))))

(defn -main
  "Entry point"
  []
  (open-editor "resources/foo.txt"))
