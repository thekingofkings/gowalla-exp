CREATE DATABASE IF NOT EXISTS trajectory;

CREATE USER 'kdd14'@'localhost' IDENTIFIED BY 'kdd14';

GRANT FILE ON *.* TO 'kdd14'@'localhost';
GRANT ALL ON trajectory.* TO 'kdd14'@'localhost';

USE trajectory;
CREATE TABLE IF NOT EXISTS gowalla (
	userID INTEGER,
	time VARCHAR(22),
	latitude DOUBLE,
	longitude DOUBLE,
	locID	INTEGER
);


LOAD DATA INFILE 'C:\\Users\\hxw186\\dataset\\Gowalla_totalCheckins.txt' INTO TABLE gowalla;


CREATE TABLE IF NOT EXISTS pair (
	user1 INTEGER,
	user2 INTEGER
);

LOAD DATA INFILE 'C:\\Users\\hxw186\\dataset\\Gowalla_edges.txt' INTO TABLE pair;