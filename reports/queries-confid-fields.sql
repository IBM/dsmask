-- IA Projects
SELECT rid, name FROM "IAVIEWS"."IAPROJECT" ORDER BY name


-- Confidential fields per-project
SELECT dc.name AS dc_name,
  irc.database AS db_name, 
  irc.schema AS schema_name, 
  irc.table AS tab_name, 
  irc.name AS col_name,
  conf.clazz_state, conf.clazz_date
FROM (
SELECT ip.rid AS proj_id, 
  irc.rid AS regcol_id,
  dc.rid AS dc_id,
  q.STATE AS clazz_state,
  MAX(q.DATE) AS clazz_date
FROM IAVIEWS.IAPROJECT ip,
 IAVIEWS.IAREGISTEREDCOLUMN irc,
 CMVIEWS.DQCLASSIFICATION q,
 CMVIEWS.DQDATACLASS dc,
 IGVIEWS.IGASSIGNEDOBJECTSOFATERM bto,
 IGVIEWS.IGBUSINESSTERM bt
WHERE ip.rid=irc.projectrid
  AND bt.ABBREVIATION='Confid'
  AND bt.RID=bto.BUSINESSTERMRID
  AND dc.RID=bto.CLASSIFIEDOBJECTRID
  AND dc.RID=q.OFDATACLASSRID
  AND irc.DATAFIELDRID=q.CLASSIFIESOBJECTRID
  AND q.STATE NOT IN ('Rejected')
  AND ip.rid=${P_PROJ_ID}
GROUP BY ip.rid, irc.rid, dc.rid, q.state
) conf 
INNER JOIN IAVIEWS.IAPROJECT ip
  ON conf.proj_id=ip.rid
INNER JOIN IAVIEWS.IAREGISTEREDCOLUMN irc
  ON conf.proj_id=irc.projectrid AND conf.regcol_id=irc.rid
INNER JOIN CMVIEWS.DQDATACLASS dc
  ON conf.dc_id=dc.rid
ORDER BY dc.name,
  irc.database, irc.schema, irc.table, irc.table, irc.name,
  conf.clazz_state, conf.clazz_date;


-- Tables with confidential fields for a particular source database
-- (used in MaskBatcher.groovy)
SELECT DISTINCT scm.name, tab.name
FROM CMVIEWS.DQCLASSIFICATION q
INNER JOIN CMVIEWS.DQDATACLASS dc
  ON q.OFDATACLASSRID=dc.rid
INNER JOIN CMVIEWS.PDRDATABASECOLUMN fld
  ON q.CLASSIFIESOBJECTRID=fld.rid
INNER JOIN CMVIEWS.PDRDATABASETABLE tab
  ON tab.rid=fld.OFDATABASETABLERID
INNER JOIN CMVIEWS.PDRDATABASESCHEMA scm
  ON scm.rid=tab.OFDATASCHEMARID
INNER JOIN CMVIEWS.PDRDATABASE db
  ON db.rid=scm.OFDATABASERID
INNER JOIN IGVIEWS.IGASSIGNEDOBJECTSOFATERM bto
  ON bto.CLASSIFIEDOBJECTRID=dc.RID
INNER JOIN IGVIEWS.IGBUSINESSTERM bt
  ON bt.RID=bto.BUSINESSTERMRID AND bt.ABBREVIATION='Confid'
WHERE COALESCE(q.State, '-') IN ('Approved', '-')
  AND LOWER(db.name)=LOWER(?)
ORDER BY scm.name, tab.name;
