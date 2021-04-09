BEGIN TRANSACTION;
CREATE TABLE IF NOT EXISTS "어류_테이블" (
	"이름(국명)"	TEXT NOT NULL UNIQUE,
	"학명"	TEXT NOT NULL UNIQUE,
	"생물분류"	TEXT NOT NULL,
	"이미지"	BLOB,
	"형태"	TEXT,
	"분포"	TEXT,
	"몸길이"	TEXT,
	"서식지"	TEXT,
	PRIMARY KEY("이름(국명)")
);
CREATE TABLE IF NOT EXISTS "금어기_테이블" (
	"이름(국명)"	TEXT NOT NULL UNIQUE,
	"금지체장"	TEXT,
	"금지체중"	TEXT,
	"금지구역"	TEXT,
	"수심"	TEXT,
	"금지시작기간"	TEXT NOT NULL,
	"금지종료기간"	TEXT NOT NULL,
	FOREIGN KEY("이름(국명)") REFERENCES "어류_테이블"("이름(국명)") ON DELETE CASCADE ON UPDATE CASCADE
);
COMMIT;
