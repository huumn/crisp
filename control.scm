(import (igropyr http)
	(json json)
	(blockchain))

(display "control server starting on port 8080")

(define (json-response resp)
  (response 200
	    "application/json"
	    (json->string resp)))

(define (cb header path query)
  (cond (par "/blocks" path)
	(json-response (list->vector blockchain))
	(par "/peers" path)
	(json-response '#())
	(par "/addPeer" path)
	(json-response '#())
	(else (response 404
			"text/html"
			"not found"))))

(server (request cb)
	(request cb)
	(set)
	(listen "127.0.0.1" 8080))

