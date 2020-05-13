(library (blockchain)
  (export blockchain-last-block
	  blockchain-gen-block
	  blockchain-add-block!
	  blockchain-replace!
	  blockchain-vector
	  block-ahead?
	  block-valid-descendant?)
  (import (chezscheme)
	  (csha256 csha256))

  (define (make-block index prev-hash timestamp data difficulty nonce)
    (list (cons "index" index)
	  (cons "prev-hash" prev-hash)
	  (cons "timestamp" timestamp)
	  (cons "data" data)
	  (cons "difficulty" difficulty)
	  (cons "nonce" nonce)))

  (define (block-index b) (cdr (assoc "index" b)))
  (define (block-prev-hash b) (cdr (assoc "prev-hash" b)))
  (define (block-timestamp b) (cdr (assoc "timestamp" b)))
  (define (block-data b) (cdr (assoc "data" b)))
  (define (block-difficulty b) (cdr (assoc "difficulty" b)))
  (define (block-nonce b) (cdr (assoc "nonce" b)))

  (define (block-valid-descendant? new prev)
    (and (= (+ 1 (block-index prev))
	    (block-index new))
	 (equal? (block->hash prev)
		 (block-prev-hash new))))

  (define (block-valid-timestamp? new prev)
    (define FUTURE-DRIFT-SECS 60)
    (define PAST-DRIFT-SECS 60)
    (and (< (- (block-timestamp prev) PAST-DRIFT-SECS)
	    (block-timestamp new))
	 (< (- (block-timestamp new) FUTURE-DRIFT-SECS)
	    (time-second (current-time)))))

  (define (block-valid? new prev)
    (and (block-valid-descendant? new prev)
	 (block-valid-timestamp? new prev)))

  (define (block-valid-difficulty? block)
    (define (difficult-enough? hash difficulty)
      (string-prefix? (make-string difficulty #\0)
		      (number->string (string->number hash 16) 2)))
    (difficult-enough? (block->hash block)
		       (block-difficulty block)))

  (define (block-ahead? block last-block)
    (> (block-index block)
       (block-index last-block)))

  (define (block->hash b)
      (sha256 (string-append (number->string (block-index b))
			     (block-prev-hash b)
			     (number->string (block-timestamp b))
			     (block-data b)
			     (number->string (block-difficulty b))
			     (number->string (block-nonce b)))))

  (define (chain-valid? chain genisis-block)
    (and (equal? genisis-block
		 (chain-genisis-block chain))
	 (every block-valid?
		chain
		(cdr chain))))

  (define chain-genisis-block last)

  (define chain-last-block car)

  (define chain-add-block cons)

  (define chain-recent-ref list-ref)

  (define (chain-difficulty chain block-ival diff-ival)
    (define (adjust-difficulty last-block chain)
      (let* ([diff-ival-secs (* block-ival diff-ival)]
	     [last-adj-block (chain-recent-ref blockchain
					       diff-ival)]
	     [last-diff (block-difficulty last-adj-block)]
	     [last-ival-secs (- (block-timestamp last-block)
				(block-timestamp last-adj-block))])
	(cond [(< last-ival-secs (/ diff-ival-secs 2))
	       (+ last-diff 1)]
	      [(> last-ival-secs (* diff-ival-secs 2))
	       (- last-diff 1)]
	      [else last-diff])))
    (let* ([last-block (chain-last-block chain)]
	   [last-index (block-index last-block)])
      (if (and (equal? 0 (mod last-index diff-ival))
	       (> last-index 0))
	  (adjust-difficulty last-block
			     chain)
	  (block-difficulty last-block))))

  (define (chain-total-difficulty chain)
    (fold-left (lambda (a b)
		 (+ a (expt 2 (block-difficulty b))))
	       0 chain))

  (define blockchain-genisis-block (make-block 0
					       ""
					       1465154705
					       "Genisis Block."
					       0
					       0))

  (define blockchain (list blockchain-genisis-block))

  (define (blockchain-vector) (list->vector blockchain))

  (define (blockchain-last-block) (chain-last-block blockchain))
	    
  (define (blockchain-mine-block data)
    (define BLOCK-INTERVAL-SECS 10)
    (define DIFFICULTY-INTERVAL-BLOCKS 10)
    (let* ([prev (blockchain-last-block)]
	   [prevhash (block->hash prev)]
	   [idx (+ (block-index prev) 1)]
	   [time (time-second (current-time))]
	   [difficulty (chain-difficulty blockchain
					 BLOCK-INTERVAL-SECS
					 DIFFICULTY-INTERVAL-BLOCKS))])
      (do ([nonce 0 (+ nonce 1)]
	   [block (make-block idx
			      prevhash
			      time
			      data
			      difficulty
			      nonce)])
	  ((block-valid-difficulty? block) block)))
	      

  (define (blockchain-add-block! b)
    (if (block-valid-descendant? b
				(blockchain-last-block))
	(set! blockchain
	      (chain-add-block block blockchain))))

  (define (blockchain-replace! chain)
    (if (and (chain-valid? chain blockchain-genisis-block)
	     (> (chain-total-difficulty chain)
		(chain-total-difficulty blockchain)))
	(set! blockchain chain)))

  )
