;;; Extra functions

(defun o1(f1 f2) 
  #'(lambda(x) (f1 (f2 x))))

(defun fact(x)
  (if (eq x 0) 1
    (* x (fact (- x 1)))))

(defun fact2(x) 
  (let ((acc 1))
    (while (not (eq x 0))
      (setq acc (* acc x))
      (setq x (- x 1)))
    acc))

; Tail recursive factorial - eats constant stack...
(defun fact3(x) (fact33 x 1))
(defun fact33(x acc)
   (if (eq x 0) acc (fact33 (- x 1) (* x acc))))

(defun fact4(x) 
   (let* ((fact #'(lambda(x acc) 
                       (if (eq x 0) acc (fact (- x 1) (* x acc))))))
       (fact x 1)))

(defun fib (n)
  (if (< n 2)
      1
    (+ (fib (- n 2)) (fib (- n 1)))))


(defvar *test-name* nil)

(defmacro deftest (name parameters &rest body)
;  "Define a test function. Within a test function we can call
;   other test functions or use 'check' to run individual test
;   cases."
  `(defun ,name ,parameters
    (let ((*test-name* (append *test-name* (list ',name))))
      ,@body)))

(defmacro check (&rest forms)
;  "Run each expression in 'forms' as a test case."
  `(combine-results
    ,@(map #'(lambda (f) `(report-result ,f ',f)) forms)))

(defmacro combine-results (&rest forms)
;  "Combine the results (as booleans) of evaluating 'forms' in order."
  (let ((result (gensym)))
    `(let ((,result t))
      ,@(map #'(lambda(f) `(unless ,f (setf ,result nil))) forms)
      ,result)))

(defun report-result (result form)
;  "Report the results of a single test case. Called by 'check'."
  (if result
  	(format t "pass ... %S: %S\n" *test-name* form)
  	(format t "FAIL ... %S: %S\n" *test-name* form))
  result)
  