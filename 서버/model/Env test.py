from tensorflow.python.client import device_lib
import tensorflow as tf
import sys


def get_available_devices():  # 사용 가능 한 장치 목록 출력
    local_device_protos = device_lib.list_local_devices()
    return [x.name for x in local_device_protos]


print(get_available_devices())
print(sys.version)  # 파이썬 인터프리터 버전
print(tf.__version__)  # 텐서플로우 버전

"""
https://stackoverflow.com/questions/41402409/tensorflow-doesnt-seem-to-see-my-gpu/44990513#44990513
파이썬 버전 : 3.8.10 (default, May 19 2021, 13:12:57) [MSC v.1916 64 bit (AMD64)] (3.8로 설치 시 지정)
텐서플로우 버전 : 2.5.0
--
pip install tensorflow-gpu
CUDA 11.2
cuDNN 8.1.0.77
"""