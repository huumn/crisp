(library (control)
  (export control-listen)
  (import (chezscheme)
	  (only (srfi s13 strings) string-contains)
	  (only (srfi s1 lists) drop-right last)
	  (json json)
	  (suv suv)
	  (blockchain)
	  (peers))

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

(define (run-cmd! client req)
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
			      [block (blockchain-mine-block bdata)])
			 (blockchain-add-block! block)
			 (peers-broadcast-last-block)
			 (json->string block))]
		      [(string-ci=? cmd "PEERS")
		       (json->string (peers-vector))]
		      [(string-ci=? cmd "PEER-ADD")
		       (if (not (= (length args) 2))
			   "ERROR: must specify ip and port"
			   (begin
			     (peers-connect-add! (car args)
						 (string->number (cadr args)))
			     "OK"))]
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
	       (run-cmd! client req))
	     reqs)
	(set! buf (last split))))))

(define (control-listen port)
  (suv-listen "127.0.0.1"
	      port
	      (lambda (client)
		(suv-accept client)
		(suv-read-start client
				(read-handler client)))))
)
