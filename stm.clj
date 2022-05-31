; 1. Single Threaded
; 2. Atoms only
; 3. Mutexes
; 4. STM - normal case
; 5. STM - side effects
; 6. STM - Atoms 
; 7. STM - Live Lock
; 8. STM - Dead Lock
; 9. STM - Failed commit crash
; 10. STM - weak Atomicity
; 11. STM - Performance
; 12. STM - Power

; come up with some kind of case where you want to do something in parallel. Where each thing takes roughly the same amount of time.
; These things compete for some central source of atoms or something or whatever.


; the question becomes... what exactly am I going to process what requires synchronization via STM or another mechanism?

; Also, I need to re-implement promises and futures to use atoms so that they are STM compatible.

; Compiling code is probably a good test. 
; Can simulate with random sleeps to some extent.

; ok so compiling code is like
; * list of targets (lib or dll or exe)
; * each target has a list of files
; * you have a list of files to compile for each target
; * linking has dependencies between other link targets, but compiling can all happen in parallel.
; * You have a list of files to link for each target
; * As you go collect errors/warnings/logs
; * should be like a DAG structure.

(def targets [ SuperCoolProgram ])

(def compile-files-done (atom []))
(def compile-files-todo (atom []))

(def link-files-done (atom []))
(def link-files-todo (atom []))

(def errors [])
(def warnings [])

(def compiler 'clang)
(def linker 'clang)

(defn compile-file-internal [file]
    ;(System/exec compiler "-c" file "-o" (System/tmpnam))
    ;(System/sleep (rand 0.001 0.0))
    true)

(defn compile-result-success [result] true)
    
(defn compile-result-message [result] "")

(defn compile-file-st [filename] 
    ;(println "Compiling" filename)
    (let [compile-result (compile-file-internal filename)]
        (if (compile-result-success compile-result)
            (do (swap! compile-files-done conj filename)
                (compile-result-message compile-result))
            (do (swap! errors cons (compile-result-message compile-result))
                (swap! warnings cons (compile-result-message compile-result))
                (swap! compile-files-done cons filename))
        )
    )
)

(defn compile-file-stm [filename] 
    ;(println "Compiling" filename)
(dosync
    (let [compile-result (compile-file-internal filename)]
        (if (compile-result-success compile-result)
            (do (swap! compile-files-done conj filename)
                (compile-result-message compile-result))
            (do (swap! errors cons (compile-result-message compile-result))
                (swap! warnings cons (compile-result-message compile-result))
                (swap! compile-files-done cons filename))
        )
    )
)
)

(defn compile-file-atoms [filename] 
    ;(println "Compiling" filename)
    (let [compile-result (compile-file-internal filename)]
        (if (compile-result-success compile-result)
            (do (swap! compile-files-done conj filename)
                (compile-result-message compile-result))
            (do (swap! errors cons (compile-result-message compile-result))
                (swap! warnings cons (compile-result-message compile-result))
                (swap! compile-files-done cons filename))
        )
    )
)

(def files (doall (range 1000)))

(println "Single Threaded")
(println (time (doall (map compile-file-st files))))

(println "Atoms Only")
(println (time (doall (map deref (doall (pmap compile-file-atoms files))))))

(println "STM")
(println (time (doall (map deref (doall (pmap compile-file-stm files))))))



