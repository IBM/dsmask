-- Generate a substitution table for an integer column.
-- Tested on Oracle 12c

(SELECT v AS v_1, v AS v_2 FROM myschema.mytable WHERE v<100)
  UNION ALL
(SELECT tab1.v_1, tab2.v_2
 FROM
 (SELECT ROWNUM AS pos_1, v AS v_1 FROM myschema.mytable WHERE v>=100) tab1,
 (SELECT ROWNUM AS pos_2, v AS v_2 FROM 
   (SELECT v FROM myschema.mytable WHERE v>=100
    ORDER BY dbms_random.value()) x) tab2
 WHERE tab1.pos_1=tab2.pos_2);


(SELECT v AS v_1, v AS v_2 FROM myschema.mytable WHERE v<100)
  UNION ALL
(SELECT tab1.v_1, tab2.v_2
 FROM
  (SELECT ROWNUM AS pos_1, v AS v_1 FROM 
   (SELECT v FROM myschema.mytable WHERE v>=100
    ORDER BY v) y) tab1,
 (SELECT ROWNUM AS pos_2, v AS v_2 FROM 
   (SELECT v FROM myschema.mytable WHERE v>=100
    ORDER BY STANDARD_HASH(TO_CHAR(v) || 'P@$$w0rd')) x) tab2
 WHERE tab1.pos_1=tab2.pos_2);

