(import (chezscheme)
	(srfi s13 strings)
	(suv suv)
	(control)
	(peers))

(define control-port 6751)
(define peers-port 9335)

(define (parse-flags! args)
  (define (set-flag! flag arg)
    (cond [(and (string=? "--control" flag)
		(string->number arg))
	   (set! control-port (string->number arg))]
	  [(and (string=? "--peers" flag)
		(string->number arg))
	   (set! peers-port (string->number arg))]
	  [else #f]))
  (define (display-err expl what)
    (display (format "~a at ~s\n"
		     expl
		     what))
    (exit))    
  (cond [(= (length args) 1)
	 (display-err "Wrong number of args"
		      what)]
        [(>= (length args) 2)
	 (if (set-flag! (car args) (cadr args))
	     (parse-flags! (cddr args))
	     (display-err "Invalid flag"
			  (car args)))]))
	      
(parse-flags! (cdr (command-line)))

(display (format "Control listening on port ~a\n"
		 control-port))
(control-listen control-port)

(display (format "Peers listening on port ~a\n"
		 peers-port))
(peers-listen peers-port)

(flush-output-port)

(suv-run)


 
