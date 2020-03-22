(import (csha256 csha256))

; block

(define-record-type block
  (fields index
	  prev-hash
	  timestamp
	  data))

(define (block-valid? new prev)
  (and (= (+ 1
	     (block-index prev))
	  (block-index new))
       (equal? (block->hash prev)
	       (block-prev-hash new))))

(define (block->hash b)
    (sha256 (string-append (number->string (block-index b))
			   (block-prev-hash b)
			   (number->string (block-timestamp b))
			   (block-data b))))

