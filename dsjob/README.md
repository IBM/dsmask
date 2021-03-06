# DataStage job design and supporting data

Job design details for substitution dictionary generation.

DataStage must be configured to support the H2 JDBC driver.
The file Server/DSEngine/isjdbc.config should contain:

```bash
CLASSPATH=/opt/Masker/dsmask-jmask/lib/h2-1.4.200.jar
CLASS_NAMES=org.h2.Driver
```

Job contains two linked steps:
- source connector;
- target connector.

Source connector reads data from the source database.
It generates the actual substitution table in a query
similar to the following one:

```sql
(SELECT isn AS isn_1, isn AS isn_2 FROM AIS.DICTI WHERE isn<1)
UNION ALL
(SELECT CAST(tab1.isn_1 AS NUMBER(20)) AS isn_1, 
        CAST(tab2.isn_2 AS NUMBER(20)) AS isn_2
 FROM
 (SELECT ROWNUM AS pos_1, ISN AS isn_1 FROM AIS.DICTI WHERE isn>=1) tab1,
 (SELECT ROWNUM AS pos_2, ISN AS isn_2 FROM 
   (SELECT ISN FROM AIS.DICTI WHERE isn>=1
    ORDER BY dbms_random.value()) x) tab2
 WHERE tab1.pos_1=tab2.pos_2);
```

The first part of UNION ALL allows to skip some substitutions
(for example, if some special-meaning values should not be
replaced with anything else).

The second part joins the sorted input values by their positions.
First sort is done in the ascending order, and the second is
the random one (hence the randomness of the substitution table).

Source connector output link has two columns defined,
both non-null and having the "key" mark set for the
first column.
This later allows to automatically create a primary key
on the target table without explicit SQL.


Target connector writes to H2 database through the JDBC driver.
JDBC URL template is the following:

```
jdbc:h2:#MaskGlobals.DICT_PATH#/demo-subst;AUTO_SERVER=TRUE;FILE_LOCK=SOCKET;COMPRESS=TRUE
```

Here "demo-subst" is the H2 database name.

Target connector is configured to replace the target table on each invocation.
Other settings can be left as defaults.

The target connector should also be configured to run in a single thread,
as multithreaded inserts are not supported by H2.

This allows to build an H2 table to be used in a KeyLookup algorithm.

