(library (blockchain)
  (export blockchain-last-block
	  blockchain-add-block!
	  blockchain-replace!
	  block->hash)
  (import (chezscheme)
	  (csha256 csha256))

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

  (define blockchain-genisis-block (make-block 0
					       ""
					       1465154705
					       "Genisis Block."))

  (define blockchain (list blockchain-genisis-block))

  (define blockchain-last-block (car (reverse blockchain)))

  (define (blockchain-next-block data)
    (let ((b blockchain-last-block))
	 (make-block (+ (block-index b) 1)
		     (block->hash b)
		     (time-second (current-time))
		     data)))

  (define (blockchain-add-block! b)
    (if (block-valid? b
		      (blockchain-last-block))
	(set! blockchain
	      (append blockchain (cons b '())))))

  (define (blockchain-valid? chain)
    (define (chain-valid? chain)
      (or (null? (cdr chain))
	  (and (block-valid? (car chain)
			     (cadr chain))
	       (chain-valid? (cdr chain)))))
    (and (equal? blockchain-genisis-block
		 (car chain))
	 (chain-valid? chain)))

  (define (blockchain-replace! new)
    (if (and (blockchain-valid? new)
	     (> (length new) (length blockchain)))
	(begin (set! blockchain new)
	       ;; TODO (blockchain-broadcast)
	       )))
)
