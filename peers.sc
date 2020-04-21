(library (peers)
  (export peers-vector
	  peers-connect-add!)
  (import (chezscheme)
	  (only (srfi s13 strings) string-contains)
	  (only (srfi s1 lists) drop-right last)
	  (suv suv)
	  (json json)
	  (blockchain))

  ;; list of pointers to "socket", ie uv handle
  (define peers '())

  (define (peers-vector)
    (list->vector (map suv-getpeername
		       peers)))

  (define (peers-add! peer)
    (set! peers
  	  (append peers (list peer))))

  (define (peers-connect-add! ip port)
    (suv-connect ip
  		 port
  		 (lambda (server)
  		   (peers-add! server)
  		   (suv-read-start server
  				   (read-handler server))
  		   (suv-write server (last-block-msg)))))

  (define (make-msg type data)
    (define (frame-msg msg)
      (format "~d\r\n~a\r\n"
  	      (string-length msg)
  	      msg))
    (frame-msg (json->string (list  (cons "type" type)
  				    (cons "data" data)))))

  (define (msg-type m) (cdr (assoc "type" m)))
  (define (msg-data m) (cdr (assoc "data" m)))

  (define (last-block-msg) (make-msg "last-block" #f))
  (define (last-block-resp-msg)
    (make-msg "last-block-resp"
  	      (json->string (blockchain-last-block))))
  (define (blocks-msg) (make-msg "blocks" #f))
  (define (blocks-resp-msg)
    (make-msg "blocks-resp"
  	      (json->string (blockchain-vector))))
    
  ;; messages are of form <len msg as string>\r\n<message>\r\n
  ;; returns a list of parsed messages, followed by #f if an
  ;; error occurs while parsing, or ending in any partial msg
  (define (parse-msgs msgstr)
    (let ([idx (string-contains msgstr "\r\n")])
      (if idx
  	  (let ([mlen (string->number (substring msgstr
  						 0
  						 idx))])
  	    (if mlen
  		(let ([rmsg (substring msgstr
  				       (+ idx 2)
  				       (string-length msgstr))])
  		  (if (>= (string-length rmsg)
  			  (+ mlen 2))
  		      (if (string=? (substring rmsg mlen (+ mlen 2))
  				    "\r\n")
  			  (cons (substring rmsg
  					   0
  					   mlen)
  				(parse-msgs (substring rmsg
  						       (+ mlen 2)
  						       (string-length rmsg))))
  			  (list #f))
  		      (list msgstr)))
  		(list #f)))
  	  (list msgstr))))

  (define (process-msg! client msg)
    (let* ([pmsg (string->json msg)]
  	   [type (msg-type pmsg)])
      (cond [(string=? type "last-block")
  	     (suv-write client
  			(last-block-resp-msg))]
  	    [(string=? type "last-block-resp")
  	     (display (msg-data pmsg))]
  	    [(string=? type "blocks")
  	     (suv-write client
  			(blocks-resp-msg))]
  	    [(string=? type "blocks-resp")
  	     (display (msg-data pmsg))]
  	    [else (make-msg "error" "unknown msg type")])))

    ;; (suv-write client
    ;; 	       (string-append (or msg
    ;; 				  "Protocol Error")
    ;; 			      "\r\n")))


  (define (read-handler client)
    (let ([buf ""])
      (lambda (req)
  	(let ([msgs (parse-msgs (string-append buf
  					       req))])
  	  (map (lambda (msg)
  		 (process-msg! client msg))
  	       (drop-right msgs 1))
  	  (if (last msgs)
  	      (set! buf (last msgs))
  	      (begin
  		(set! buf "")
  		(process-msg! client
  			     (last msgs))))))))

  (suv-listen "127.0.0.1"
  	      9000
  	      (lambda (client)
  		(suv-accept client)
  		(peers-add! client)
  		(suv-read-start client
  				(read-handler client))
  		(suv-write client (last-block-msg))))
)		
