CREATE TABLE Files (
	url         TEXT        UNIQUE ON CONFLICT REPLACE NOT NULL PRIMARY KEY,
	[date]      INTEGER     NOT NULL
);

CREATE TABLE Mask (
	mask        TEXT        UNIQUE ON CONFLICT REPLACE NOT NULL PRIMARY KEY,
	[date]      INTEGER     NOT NULL
);
