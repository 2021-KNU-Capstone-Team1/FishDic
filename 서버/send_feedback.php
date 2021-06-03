<?php
/*** 
 * 서버로 피드백 데이터 전송 스크립트 
 * ---
 * $_FILES는 배열 요소로써 업로드한 파일에 관한 정보를 가지고 있다.
 * form_name : 파일 폼 이름
 * $_FILES[’form_name’][’name’] - 업로드한 파일명
 * $_FILES[’form_name’][’type’] - 업로드한 파일의 MIME 타입 ( Jpg, Gif 등)
 * $_FILES[’form_name’][’size’] - 업로드한 파일의 사이즈( 바이트 )
 * $_FILES[’form_name’][’error’] - 파일 업로드 작업시 발생한 에러코드 (‘0′ 은 성공을 의미하고 나머지는 실패)
 * $_FILES[’form_name’][’tmp_name’] - 파일이 저장된 서버의 임시 저장소 (ex) /tmp/phpe7qJky 등)
---
 * 1) 사용자로부터의 어류 판별에 대한 피드백을 위하여 판별 기능 시 다음 파일들을 업로드\
 * 2) 사용자가 판별에 사용한 이미지 + 해당 이미지의 모델에 대한 출력값
 * 3) 클라이언트 측으로부터 압축하여 받을 것
 * 4) 클라이언트 측에서 'feedback_data'로 키 설정할 것
 * 5) 판별 수행 완료 시점에 사용자가 서버에 피드백 데이터가 업로드 완료되기를 기다리지 않도록 비동기식으로 수행
 * ***/

define ('SITE_ROOT', realpath(dirname(__FILE__))); //서버 절대 경로
$target_dir = "\\feedback\\";
$target_dir = $target_dir . basename($_FILES['feedback_data']['name']); //목적지 디렉토리의 업로드 될 파일의 전체 경로

if($_FILES['feedback_data']['size'] > 0 && $_FILES['feedback_data']['size'] <= 31457280) //30MB로 크기 제한
{
    if(move_uploaded_file(realpath($_FILES['feedback_data']['tmp_name']), SITE_ROOT . $target_dir)) //임시 디렉토리에서 목적지 디렉토리로 이동
        echo "success";
    else
        echo "fail";
}
?>