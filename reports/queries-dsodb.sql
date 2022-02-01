-- List masking job batches without details
WITH data AS (SELECT 
  jrp.ParamValue AS BatchId
  ,SUBSTR(jr.InvocationId, 1, POSITION('-', jr.InvocationId)-1) AS DbName
  ,TRANSLATE(SUBSTR(jr.InvocationId, POSITION('-', jr.InvocationId)+1), '.', '-') AS TableName
  ,jr.RunStartTimestamp + CURRENT TIMEZONE AS BeginTime
  ,jr.RunEndTimestamp + CURRENT TIMEZONE EndTime
  ,jr.RunMajorStatus
  ,jr.RunMinorStatus
FROM DSODB.JobRun jr
INNER JOIN DSODB.JobExec je
  ON je.JobId=jr.JobId
INNER JOIN DSODB.JobRunParamsView jrp
  ON jr.RunId=jrp.RunId AND jrp.ParamName='BatchId'
WHERE je.ProjectName='dstage1' 
  AND je.JobName IN ('MaskJdbc', 'MaskOra', 'MaskDb2')
  AND jr.InvocationId IS NOT NULL
  AND jr.InvocationId NOT IN ('', '-')
  AND POSITION('-', jr.InvocationId) > 0
  AND jr.RunType='RUN'
), aggr AS (
SELECT
  BatchId, DbName
  ,COUNT(*) AS TableCount
  ,MIN(BeginTime) AS BeginTime
  ,MAX(EndTime) AS EndTime
  ,SUM(CASE WHEN RunMajorStatus='FIN' THEN 0 ELSE 1 END) AS PendingCount
  ,SUM(CASE WHEN RunMinorStatus IN ('RNF','RNS','FWF','CRA','STP','SYN') THEN 1 ELSE 0 END) AS ErrorCount
  ,SUM(CASE WHEN RunMinorStatus IN ('RNW','FWW') THEN 1 ELSE 0 END) AS WarningCount
FROM data
GROUP BY BatchId, DbName
)
SELECT BatchId, DbName || ' (' ||
   VARCHAR_FORMAT(BeginTime, 'YYYY-MM-DD HH24:MI') || ' - ' ||
   COALESCE(VARCHAR_FORMAT(EndTime, 'YYYY-MM-DD HH24:MI'), '?') || ')' AS BatchName
FROM aggr
ORDER BY BeginTime DESC;


-- Masking job batches grouped by the parameter
WITH data AS (SELECT 
  jrp.ParamValue AS BatchId
  ,SUBSTR(jr.InvocationId, 1, POSITION('-', jr.InvocationId)-1) AS DbName
  ,TRANSLATE(SUBSTR(jr.InvocationId, POSITION('-', jr.InvocationId)+1), '.', '-') AS TableName
  ,jr.RunStartTimestamp + CURRENT TIMEZONE AS BeginTime
  ,jr.RunEndTimestamp + CURRENT TIMEZONE EndTime
  ,jr.RunMajorStatus
  ,jr.RunMinorStatus
FROM DSODB.JobRun jr
INNER JOIN DSODB.JobExec je
  ON je.JobId=jr.JobId
INNER JOIN DSODB.JobRunParamsView jrp
  ON jr.RunId=jrp.RunId AND jrp.ParamName='BatchId'
WHERE je.ProjectName='dstage1' 
  AND je.JobName IN ('MaskJdbc', 'MaskOra', 'MaskDb2')
  AND jr.InvocationId IS NOT NULL
  AND jr.InvocationId NOT IN ('', '-')
  AND POSITION('-', jr.InvocationId) > 0
  AND jr.RunType='RUN'
), aggr AS (
SELECT
  BatchId, DbName
  ,COUNT(*) AS TableCount
  ,MIN(BeginTime) AS BeginTime
  ,MAX(EndTime) AS EndTime
  ,SUM(CASE WHEN RunMajorStatus='FIN' THEN 0 ELSE 1 END) AS PendingCount
  ,SUM(CASE WHEN RunMinorStatus IN ('RNF','RNS','FWF','CRA','STP','SYN') THEN 1 ELSE 0 END) AS ErrorCount
  ,SUM(CASE WHEN RunMinorStatus IN ('RNW','FWW') THEN 1 ELSE 0 END) AS WarningCount
FROM data
GROUP BY BatchId, DbName
)
SELECT BatchId, DbName, TableCount, BeginTime, EndTime
   ,TIMESTAMPDIFF(2, EndTime - BeginTime) AS RuntimeSeconds
   ,(CASE WHEN PendingCount=0 THEN 'Процесс завершен' ELSE 'Выполняется маскирование' END) AS ProcState
   ,(CASE WHEN ErrorCount=0 THEN
       CASE WHEN WarningCount=0 THEN 'Предупреждений нет' ELSE 'ВНИМАНИЕ! Предупреждений: ' || TO_CHAR(WarningCount) END
       ELSE 'ВНИМАНИЕ! Ошибок: ' || TO_CHAR(ErrorCount) END) AS ProcResult
FROM aggr
ORDER BY DbName, BeginTime
;


-- Masking batch execution details
WITH data AS (SELECT 
  jrp.ParamValue AS BatchId
  ,SUBSTR(jr.InvocationId, 1, POSITION('-', jr.InvocationId)-1) AS DbName
  ,TRANSLATE(SUBSTR(jr.InvocationId, POSITION('-', jr.InvocationId)+1), '.', '-') AS TableName
  ,jr.RunStartTimestamp + CURRENT TIMEZONE AS BeginTime
  ,jr.RunEndTimestamp + CURRENT TIMEZONE EndTime
  ,jr.RunMajorStatus
  ,jr.RunMinorStatus
FROM DSODB.JobRun jr
INNER JOIN DSODB.JobExec je
  ON je.JobId=jr.JobId
INNER JOIN DSODB.JobRunParamsView jrp
  ON jr.RunId=jrp.RunId AND jrp.ParamName='BatchId'
WHERE je.ProjectName='dstage1' 
  AND je.JobName IN ('MaskJdbc', 'MaskOra', 'MaskDb2')
  AND jr.InvocationId IS NOT NULL
  AND jr.InvocationId NOT IN ('', '-')
  AND POSITION('-', jr.InvocationId) > 0
  AND jr.RunType='RUN'
  AND jrp.ParamValue='f65f0353-e03a-426b-8443-957fcf9f8797'
)
SELECT TableName
  ,VARCHAR_FORMAT(BeginTime, 'DD.MM.YYYY HH24:MI') || ' - ' ||
    VARCHAR_FORMAT(EndTime, 'DD.MM.YYYY HH24:MI') AS RuntimePeriod
  ,TIMESTAMPDIFF(2, EndTime - BeginTime) AS RuntimeSeconds
  ,(CASE WHEN RunMajorStatus='FIN' THEN 'Завершено' ELSE 'Выполняется' END) AS ExecState
  ,CASE WHEN RunMinorStatus IN ('RNF','RNS','FWF','CRA','STP','SYN') THEN 'Ошибка выполнения!'
    ELSE CASE WHEN RunMinorStatus IN ('RNW','FWW') THEN 'Есть предупреждения!' ELSE 'Успех' END 
    END AS ExecResult
FROM data
ORDER BY TableName;
