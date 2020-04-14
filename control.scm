(import (chezscheme)
	(suv suv)
	(blockchain)
	(json json))

(display "control server starting on port 8000")

;; should probably replace this stuff with
;; srfis
(define (string-index str chr)
  (let ([len (string-length str)])
    (do ((pos 0 (+ 1 pos)))
	((or (>= pos len) (char=? chr (string-ref str pos)))
	 (and (< pos len) pos)))))

(define (string-split str chr)
  (let ([idx (string-index str chr)])
    (if idx
	(cons (substring str 0
			 (if (and (positive? idx)
				  (char=? #\return (string-ref str (+ -1 idx))))
			     (+ -1 idx)
			     idx))
	      (string-split (substring str (+ 1 idx) (string-length str))
			  chr))
	(list str))))

(define (string-join strs del)
  (if (null? strs)
      ""
      (fold-left (lambda (acc x) (string-append acc del x))
            (car strs)
            (cdr strs))))

(define (but-last l)
  (reverse (cdr (reverse l))))

(define (last l)
  (car (reverse l)))

(define (run-cmd client req)
  (let* ([split (string-split req #\space)]
	 [cmd (car split)]
	 [args (cdr split)])
    (suv-write client
	       (string-append
		(cond [(string-ci=? cmd "ECHO")
		       (substring req
				  (min 5 (string-length req))
				  (string-length req))]
		      [(string-ci=? cmd "BLOCKS")
		       (json->string (blockchain-vector))]
		      [(string-ci=? cmd "BLOCK-MINE")
		       (let* ([bdata (substring req
						(min 11 
						     (string-length req))
						(string-length req))]
			      [block (blockchain-gen-block bdata)])
			 (blockchain-add-block! block)
			 (json->string block))]
		      [(string-ci=? cmd "PEERS")
		       "TODO: return peers"]
		      [(string-ci=? cmd "PEER-ADD")
		       "TODO: add peer"]
		      [else "ERROR: unknown command"])
		"\r\n"))))

(define (read-handler client)
  (let ([buf ""])
    (lambda (req)
      (let* ([split (string-split (string-append buf
						 req)
				  #\newline)]
	    [reqs (but-last split)])
	(map (lambda (req)
	       (run-cmd client req))
	     reqs)
	(set! buf (last split))))))
      
(suv-listen "127.0.0.1"
	    8000
	    (lambda (client)
	      (suv-accept client)
	      (suv-read-start client
			      (read-handler client))))

(suv-run)
