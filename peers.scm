(library (peers)
  (export peers-vector
	  peers-add!)
  (import (chezscheme))

  ;; peers should be an alist ip:port -> handle
  (define peers '())

  (define (peers-vector) (list->vector peers))

  (define (peers-add! peer)
    (set! peers
	  (append peers (list peer))))

  ;; messages are of form <len msg as string>\r\n<message>\r\n
  (define (read-handler client)
    (let ([nexpect -1]
  	  [buf ""])
      (lambda (req)
	(if (eq? nexpect -1)
	    ;; look for first \r\n in buf, store nexpected
	    ;; else attempt to read in nexpect chars from buf
	    )

  ;; (suv-listen "127.0.0.1"
  ;; 	      9000
  ;; 	      (lambda (client)
  ;; 		(suv-accept client)
  ;; 		(peers-add! ;;ip:port
  ;; 		 client)
  ;; 		(suv-read-start client
  ;; 				(read-handler client))
  ;; 		(suv-write client
  ;; 			   ;; write framed query length message
  ;; 			   ...)))
)
