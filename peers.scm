(library (peers)
  (export peers-vector
	  peers-add!)
  (import (chezscheme))

  (define peers '())

  (define (peers-vector) (list->vector peers))

  (define (peers-add! peer)
    (set! peers
	  (append peers (list peer))))
)
