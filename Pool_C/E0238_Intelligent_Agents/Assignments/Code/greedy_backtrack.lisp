(defun print_input (country_map colours_available_list)
    "Print input summary"

    (format t "~% List of countries: ~%")
    (loop for country_list in country_map
        do(format t " ~S : ~S ~%" (car country_list) (cdr country_list)) 
    )

    (format t "~% List of available colours: ~% ~S ~%" colours_available_list)
)

;----------------------------------------------------

(defun find_colour (neighbour_list)
    "Find colour for a vertex"

    (dotimes (index num_countries)
        (setf (aref colour_used_array index) 0)
    )

    (loop for neighbour in neighbour_list            
        do(setq neighbour_colour (gethash neighbour country_colour_map))
        do(when (> neighbour_colour -1)
            (setf (aref colour_used_array neighbour_colour) 1)
        )
    )
    
    (setq index 0)
    (loop 
        (when (= (aref colour_used_array index) 0) 
            (return)
        )
        (setq index (+ index 1))
    )
    (return-from find_colour index) 
)

;----------------------------------------------------

(defun output_list ()
    "Create colour assignment list"

    (setq colours_assigned_list (list ))

    (loop for country_list in country_map
        do(setq country (car country_list))
        do(setq country_colour (gethash country country_colour_map))
        do(setq country_colour (nth country_colour colours_available_list))
        do(setq colour_assignment (list (list country country_colour)))
        do(setq colours_assigned_list (append colours_assigned_list colour_assignment))
    )

    (return-from output_list colours_assigned_list)
)

;----------------------------------------------------

(defun copy_map (dest src)
    "Copy dest map to src map"

    ; (format t "~% Copying map... ~%")
    (loop for country_list in country_map
        do(setq country_key (car country_list))
        do(setq country_colour (gethash country_key src))
        do(setf (gethash country_key dest) country_colour) 
    )     
)

;----------------------------------------------------

(defun print_map (hashmap hashmap_name)
    "Print map"

    ; (format t "~% Printing hash map ~S ~%" hashmap_name)
    (loop for country_list in country_map
        do(setq country_key (car country_list))
        do(format t "~%~S -> ~S" country_key (gethash country_key hashmap))
    )
    (format t "~%" )
)

;----------------------------------------------------

(defun string_to_list (line)
    (setq line_len (length line))
    (setq temp_list (list ))

    (dotimes (i line_len)
        (setq curr_char (char line i))
        (when (and (and (not (eq curr_char #\Space )) (not (eq curr_char #\( ))) (not (eq curr_char #\) )))
            (setq temp_list (append temp_list (list (intern (string curr_char)))))
        )
    )

    (return-from string_to_list temp_list)
)

;----------------------------------------------------

(require "asdf")
(asdf:load-system :uiop)

(setq filename (car *args*))
(setq lines (uiop:read-file-lines filename))

(setq country_map (list ))
(loop for line in lines
    do(setq temp_list (string_to_list line))
    do(when (not (eq temp_list NIL))
        (setq country_map (append country_map (list temp_list)))
    )
)

(setq colours_available_list (car (last country_map)))
(setq country_map (remove colours_available_list country_map))

(setq num_countries (length country_map)) 
(setq num_colours_available (length colours_available_list)) 

;----------------------------------------------------

; Assign -1 (no colour) to all the countries
(setq country_colour_map (make-hash-table)) 
(loop for country_list in country_map
    do(setq country (car country_list))
    do(setf (gethash country country_colour_map) -1)
)

; An auxillary array to calculate minimum unused colour
(setf colour_used_array (make-array num_countries))

; Minimum colours required to colour the country_map
(setq min_colours_req 0)

;----------------------------------------------------

; Colour the country_map greedily
(loop for country_list in country_map
    do(setq country (car country_list))
    do(setq neighbour_list (cdr country_list)) 

    do(setq min_unused_colour (find_colour neighbour_list))     
    do(setf (gethash country country_colour_map) min_unused_colour)

    do(setq min_colours_req (max min_colours_req min_unused_colour)) 
)

;----------------------------------------------------

; Set threshold to T
(setq threshold min_colours_req)
(loop  

    ; fail = FALSE
    (setq threshold_reduction_fail 0)
    ; (format t "~% Current Threshold : ~S ~%" threshold)

    ; For all vertices v; 
    (loop for vertex_list in country_map

        do(setq vertex (car vertex_list))
        do(setq vertex_colour (gethash vertex country_colour_map))

        ; If color[v] = T
        do(when (= vertex_colour threshold)
            (progn

                ; For all vertices v; 
                ;     pseudocolor[v]=color[v]
                (setq pseudo_country_colour_map (make-hash-table)) 
                (copy_map  pseudo_country_colour_map country_colour_map)

                (setq neighbor_list (cdr vertex_list)) 
                
                ; For all colors c; 1 ≤ c ≤ T
                (dotimes (curr_colour threshold)

                    (setq vertx_colour_fail 0)

                    ; pseudocolor[v] = c
                    (setf (gethash vertex pseudo_country_colour_map) curr_colour)          

                    ; For all neighbors n of v,                    
                    (loop for neighbor in neighbor_list

                        do(setq neighbour_colour_fail 0)

                        ; If color[n]=c
                        ;     Reassign pseudocolor[n] to avoid conflicts;     
                        do(setq neighbor_colour (gethash neighbor country_colour_map))
                        do(when (= neighbor_colour curr_colour)                                      
            
                            (loop for country_list in country_map
                                do(setq country (car country_list))
                                do(when (eq neighbor country)
                                    (progn

                                        (dotimes (index num_countries)
                                            (setf (aref colour_used_array index) 0)
                                        )                                    

                                        (setq adj_country_list (cdr country_list))   

                                        (loop for adj_country in adj_country_list                                               
                                            do(setq adj_country_colour (gethash adj_country pseudo_country_colour_map))
                                            do(setf (aref colour_used_array adj_country_colour) 1)
                                        )
                                        
                                        (setq index 0)
                                        (loop 
                                            (when (= (aref colour_used_array index) 0) 
                                                (return)
                                            )
                                            (setq index (+ index 1))
                                        )
                                        (setq min_unused_colour index) 

                                        (if (>= min_unused_colour threshold)
                                            (progn
                                                (setq neighbour_colour_fail 1)
                                                (return)
                                            )
                                            (setf (gethash neighbor pseudo_country_colour_map) min_unused_colour)
                                        )                                                                                
                                    )                                    
                                ) 
                            )                                 
                        )

                        ; Neighbour colouring failed
                        do(when (= neighbour_colour_fail 1)
                            (progn
                                (setq vertx_colour_fail 1)                            
                                (return)
                            )    
                        )
                    )

                    ; Vertex colouring successful
                    (if (= vertx_colour_fail 0)
                        (return)
                    )  
                )

                (if (= vertx_colour_fail 0)
                    (progn
                        (copy_map country_colour_map pseudo_country_colour_map)
                    )
                    (progn
                        (setq threshold_reduction_fail 1)
                        (return)
                    )
                )
            )
        )    
    )

    (if (= threshold_reduction_fail 0)
        (progn
            (setq threshold (- threshold 1))
        )
        (progn
            (return)
        )
    )
)

;----------------------------------------------------

(setq min_colours_req (+ 1 threshold))

(when (> min_colours_req num_colours_available)
    (progn
        (print NIL)
        (exit)
    )
)

(setq colours_assigned_list (output_list ))
(format t "~%(")
(loop for colour_assignment in colours_assigned_list
    do(format t "~% ~S" colour_assignment)
)
(format t "~%)")

;----------------------------------------------------