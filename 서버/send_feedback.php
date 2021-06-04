<?php
/*** 
 * 서버로 피드백 데이터 전송 스크립트 
 * ---
 * $_FILES는 배열 요소로써 업로드한 파일에 관한 정보를 가지고 있다.
 * $_FILES['key']['name'] - 업로드한 파일명
 * $_FILES['key']['type'] - 업로드한 파일의 MIME 타입 ( Jpg, Gif 등)
 * $_FILES['key']['size'] - 업로드한 파일의 사이즈( 바이트 )
 * $_FILES['key']['error'] - 파일 업로드 작업시 발생한 에러코드 (‘0′ 은 성공을 의미하고 나머지는 실패)
 * $_FILES['key']['tmp_name'] - 파일이 저장된 서버의 임시 저장소 (ex) /tmp/phpe7qJky 등)
---
 * 1) 사용자로부터의 어류 판별에 대한 피드백을 위하여 판별 기능 시 다음 파일들을 업로드\
 * 2) 사용자가 판별에 사용한 이미지 + 해당 이미지의 모델에 대한 출력값
 * 3) 클라이언트 측으로부터 압축하여 받을 것
 * 4) 클라이언트 측과 키를 일치시킬 것
 * 5) 판별 수행 완료 시점에 사용자가 서버에 피드백 데이터가 업로드 완료되기를 기다리지 않도록 비동기식으로 수행
 * ***/
$KEY = 'Gh94K7572e503WjsiiV6dQZjQHea2126';
$MAX_UPLOAD_SIZE = 31457280; //최대 업로드 크기 (30MB)
//$ALLOWED_TYPE = 'application/zip'; //업로드 제한 타입

define ('SITE_ROOT', realpath(dirname(__FILE__))); //서버 절대 경로

$target_dir = "\\feedback\\";
$target_dir = $target_dir . basename($_FILES[$KEY]['name']); //목적지 디렉토리의 업로드 될 파일의 전체 경로

if($_FILES[$KEY]['size'] > 0 && $_FILES[$KEY]['size'] <= $MAX_UPLOAD_SIZE)
{
    if(move_uploaded_file(realpath($_FILES[$KEY]['tmp_name']), SITE_ROOT . $target_dir)) //임시 디렉토리에서 목적지 디렉토리로 이동
        echo "success";
    else
        echo "fail";
}
?>