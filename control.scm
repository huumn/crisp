(import (chezscheme)
	(srfi s13 strings)
	(srfi s1 lists)
	(json json)
	(suv suv)
	(blockchain)
	(peers))

(display "control server starting on port 8000")

(define (string-split str sep)
  (let ([idx (string-contains str sep)])
    (if idx
	(cons (substring str 0 idx)
	      (string-split (substring str
				       (+ idx
					  (string-length sep))
				       (string-length str))
			    sep))
	(list str))))

(define (run-cmd client req)
  (let* ([split (string-split req " ")]
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
				  "\r\n")]
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
