(library (peers)
  (export peers-vector
	  peers-add!)
  (import (chezscheme)
	  (srfi s13 strings)
	  (srfi s1 lists)
	  (suv suv))

  ;; peers should be an alist ip:port -> handle
  (define peers '())

  (define (peers-vector) (list->vector peers))

  (define (peers-add! peer)
    (set! peers
	  (append peers (list peer))))

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

  ;; for testing just echo msg
  (define (process-msg! client msg)
    (suv-write client
	       (string-append (or msg
				  "Protocol Error")
			      "\r\n")))

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
		;; (peers-add! ip:port client)
		(suv-read-start client
				(read-handler client))
		;; (suv-write client initial-query)
		))
)					
