# DB
<b>(서버 어류 DB 관리를 위한 디렉토리)</b><br>

---
## < 파일 설명 >
    ./images : 어류 DB의 각 어류에 대하여 이미지 갱신을 위한 어류 이미지 저장 디렉토리
    ./작업용 : 어류 DB 구축 시 사용 한 참고 데이터들

    FishDicDB.db : 어류 DB 파일
    FishDicDB.sqbpro : SQLite DB Browser 프로젝트 파일
    version : 로컬 버전 (클라이언트)과 비교하여 클라이언트 측의 DB 갱신 수행 위한 DB의 버전 관리 파일로서, 서버 측에서 DB 갱신 시 갱신 날짜를 기록한다.