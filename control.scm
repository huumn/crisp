(import (igropyr http)
	(blockchain))

(printf "control server starting on port 8080")

(define (cb header path query)
  (response 200
	    "text/html"
	    (block->hash blockchain-last-block)))

(server (request cb)
	(request cb)
	(set)
	(listen "127.0.0.1" 8080))

