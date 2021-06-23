
(defun output_list ()
    "Create colour assignment list"

    (setq colours_assigned_list (list ))

    (loop for country_list in country_map
        do(setq country (car country_list))
        do(setq country_index (gethash country country_index_map))
        do(setq country_colour (aref optimal_colour_tuple country_index))        
        do(setq country_colour (nth country_colour colours_available_list))
        do(setq colour_assignment (list (list country country_colour)))
        do(setq colours_assigned_list (append colours_assigned_list colour_assignment))
    )

    (return-from output_list colours_assigned_list)
)

;----------------------------------------------------

(defun evaluate (colour_tuple)

    (setq valid_colouring 1)

    (loop for vertex_list in country_map
    
        do(setq vertex (car vertex_list))
        do(setq neighbor_list (cdr vertex_list)) 
        do(setq vertex_index (gethash vertex country_index_map))
        do(setq vertex_colour (aref colour_tuple vertex_index))

        do(loop for neighbour in neighbor_list
            do(setq neighbour_index (gethash neighbour country_index_map))
            do(setq neighbour_colour (aref colour_tuple neighbour_index))

            do(when (= vertex_colour neighbour_colour)
                (progn
                    (setq valid_colouring 0)
                    (return)
                )
            )
        )

        do(when (= valid_colouring 0)
            (progn
                (return)
            )            
        )
    )

    (return-from evaluate valid_colouring)
)

;----------------------------------------------------

(defun copy_array (dest src)
    (setq length_array (length dest))
    (dotimes (index length_array)
        (setf (aref dest index) (aref src index))
    )
)

;----------------------------------------------------

(defun enumerate (colour_tuple pos)
    "Enumerate all possible states"
    
    (if (>= pos num_countries)
        (progn            
            (setq valid (evaluate colour_tuple))

            (when (= valid 1)
                (setq colours_req 0)
                (dotimes (index num_countries)
                    (setq curr_colour (aref colour_tuple index))
                    (setq colours_req (max colours_req curr_colour))
                )
                (setq colours_req (+ 1 colours_req))

                (if (= coloring_possible 0)
                    (progn
                        (setq coloring_possible 1)
                        (setq min_colours_req colours_req)
                        (copy_array optimal_colour_tuple colour_tuple)
                    )
                    (progn                                               
                        (when (< colours_req min_colours_req)
                            (progn
                                (setq min_colours_req colours_req)
                                (copy_array optimal_colour_tuple colour_tuple)
                            )
                        )
                    )            
                )   
            )
        )
        (progn
            (dotimes (colour num_colours_available)
                (setf (aref colour_tuple pos) colour)
                (enumerate colour_tuple (+ 1 pos))
            )
        )        
    )    
)

;----------------------------------------------------

(defun string_to_list (line)
    "Convert string to list"

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

; Read file and parse input

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

; Map counties to integers

(setq country_index_map (make-hash-table)) 
(setq index 0)
(loop for country_list in country_map
    do(setq country (car country_list))
    do(setf (gethash country country_index_map) index)
    do(setq index (+ 1 index))
)

(setf colour_tuple (make-array num_countries))
(setf optimal_colour_tuple (make-array num_countries))
(setq min_colours_req num_countries)
(setq coloring_possible 0)

;----------------------------------------------------

;  Perform coloring

(enumerate colour_tuple 0)

;----------------------------------------------------

; Report Coloring

(when (or (= coloring_possible 0) (> min_colours_req num_colours_available))
    (progn
        (print NIL)
        (exit)
    )
)

(setq colours_assigned_list (output_list ))
(format t "~%(")
(setq colours_assigned_list (output_list ))
(loop for colour_assignment in colours_assigned_list
    do(format t "~% ~S" colour_assignment)
)
(format t "~%)")

;----------------------------------------------------