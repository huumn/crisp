(library (blockchain)
  (export blockchain-last-block
	  blockchain-gen-block
	  blockchain-add-block!
	  blockchain-replace!
	  blockchain-vector)
  (import (chezscheme)
	  (csha256 csha256))

  (define (make-block index prev-hash timestamp data)
    (list (cons "index" index)
	  (cons "prev-hash" prev-hash)
	  (cons "timestamp" timestamp)
	  (cons "data" data)))

  (define (block-index b) (cdr (assoc "index" b)))
  (define (block-prev-hash b) (cdr (assoc "prev-hash" b)))
  (define (block-timestamp b) (cdr (assoc "timestamp" b)))
  (define (block-data b) (cdr (assoc "data" b)))

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

  (define (blockchain-vector) (list->vector blockchain))

  (define (blockchain-last-block) (car (reverse blockchain)))

  (define (blockchain-gen-block data)
    (let ((b (blockchain-last-block)))
	 (make-block (+ (block-index b) 1)
		     (block->hash b)
		     (time-second (current-time))
		     data)))

  (define (blockchain-add-block! b)
    (if (block-valid? b
		      (blockchain-last-block))
	(set! blockchain
	      (append blockchain (list b)))))


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
	       ))))
