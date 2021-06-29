# 서버
<b>(서버에서 관리되는 요소들을 위한 디렉토리)</b><br>

---
## < Directory Structure Information >
    ./banner : 서버 배너 이미지 디렉토리
    ./DB : 서버 어류 DB 디렉토리
    ./feedback : 서버로 전송 될 피드백 데이터 저장 디렉토리
    ./model : 서버 Keras 이미지 딥러닝 모델 디렉토리
    ./OpenCV : 이미지 딥러닝을 위한 OpenCV 디렉토리
    ./SQLiteDBImageUpdater : 어류 DB의 이미지 갱신을 위한 애플리케이션 디렉토리
    
    .htaccess : 디렉토리 단위 웹 서버 설정 파일
    send_feedback.php : 서버로 피드백 데이터 전송을 위한 Server-Side PHP 스크립트
    install_to_assets.cmd : DB, banner, model을 앱 디렉토리의 assets에 설치 위한 스크립트
    version_update.cmd : 버전 관리 파일 갱신 위한 스크립트
---
## < Server Information >
| Hardware | Information |
|:---|:---|
| CPU | Intel(R) Core(TM) i9-10900K |
| GPU | NVIDIA GeForce RTX 3060 |
| RAM | 16GB |

| Software | Version |
|:---|:---|
| Windows | 10 |
| Python Interpreter | 3.8.10 |
| tensorflow-gpu | 2.5.0 |
| CUDA | 11.2 |
| CuDNN | 8.1.0.77 |
| Bitnami WAMP Stack | 7.4.16 |
| OpenCV | 3.4.8 |