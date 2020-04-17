(import (chezscheme)
	(srfi s13 strings)
	(srfi s1 lists)
	(json json)
	(suv suv)
	(blockchain)
	(peers))

(display "control server starting on port 8000")

;; should probably use string-contains from srfi s13
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
		       (json->string (peers-vector))]
		      [(string-ci=? cmd "PEER-ADD")
		       (if (null? args)
			   "ERROR: must specify peer"
			   (begin
			     (peers-add! (car args))
			     (car args)))]
		      [else "ERROR: unknown command"])
		"\r\n"))))

(define (read-handler client)
  (let ([buf ""])
    (lambda (req)
      (let* ([split (string-split (string-append buf
						 req)
				  #\newline)]
	    [reqs (drop-right split 1)])
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
