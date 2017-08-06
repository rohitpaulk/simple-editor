(ns simple-editor.core)

(defn open-editor
  "Opens the editor with the given file"
  [file-path]
  (println (format "Here are the contents from (%s)\n" file-path))
  (println (slurp file-path)))

(defn -main
  "Entry point"
  []
  (open-editor "resources/foo.txt"))
