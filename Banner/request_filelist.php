<?php
    /* 배너 이미지 목록 반환 스크립트 */
    $path = realpath('./');
    foreach (new RecursiveIteratorIterator(new RecursiveDirectoryIterator($path)) as $filename)
        if (strpos($filename,'.jpeg') || strpos($filename,'.jpg') //이미지들만 출력
        || strpos($filename,'.bmp') || strpos($filename,'.gif'))
            $list = $list.basename($filename)."\r\n";
    echo $list;
?>