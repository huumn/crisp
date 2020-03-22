; blockchain

(define blockchain-genisis-block (make-block 0
					     ""
					     1465154705
					     "Genisis Block."))

(define blockchain (list blockchain-genisis-block))

(define blockchain-last-block (car (reverse blockchain)))

(define (blockchain-next-block data)
  (let ((b last-block))
       (make-block (+ (block-index b))
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
             (blockchain-broadcast))))
