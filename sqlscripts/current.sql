SELECT userID, count(userID) AS cnt FROM trajectory.gowalla GROUP BY userID ORDER BY cnt;

SELECT a.*, b.* FROM gowalla as a INNER JOIN towalla as b ON (a.userid=2 and b.userid=1174 and a.locid=b.locid);