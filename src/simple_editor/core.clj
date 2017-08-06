(ns simple-editor.core)

(defn render
  "Renders the buffer + cursor"
  [buffer, _]
  (def lines (:lines buffer))
  (dorun (map println lines)))

(defn process-input
  "Processes one input character from the terminal"
  [char]
  (println "Processing input"))

(defn open-editor
  "Opens the editor with the given file"
  [file-path]
  (println (format "Here are the contents from (%s)\n" file-path))
  (def file-contents (slurp file-path))
  (def lines (clojure.string/split-lines file-contents))
  (def buffer {:lines lines})
  (while true (do 
                (render buffer nil) 
                (process-input))

(defn -main
  "Entry point"
  []
  (open-editor "resources/foo.txt"))
