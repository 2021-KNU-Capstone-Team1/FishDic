import matplotlib.pyplot as plt
import numpy as np
import os
import PIL
from numpy.lib.shape_base import split
import tensorflow as tf
import pathlib

from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras.models import Sequential

from PIL import Image
import os, glob, numpy as np
from tensorflow.keras.models import load_model

import json

model = keras.models.load_model('./model.h5')
model.summary()
TARGET_IMAGE_PATH = "./target_images/" # 판별 할 어류 이미지들 위치
IMAGE_WIDTH  = 128
IMAGE_HEIGHT = 128
PIXELS = IMAGE_HEIGHT * IMAGE_WIDTH  * 3

# 판별 할 어류 이미지들 불러와서 변환 및 크기 조정 후 x에 추가
target_images = [] # 판별할 이미지들
target_image_names = [] #판별할 이미지 이름들
files = glob.glob(TARGET_IMAGE_PATH + "\\*.*")
for i, f in enumerate(files):
    img = Image.open(f)
    img = img.convert("RGB")
    img = img.resize((IMAGE_WIDTH , IMAGE_HEIGHT))
    data = np.asarray(img)
    target_image_names.append(f)
    target_images.append(data)

target_images = np.array(target_images)

class_names = [] # 분류 이름
with open("./label.txt", "r") as txt_file:
    class_names = txt_file.read()
class_names = class_names.splitlines(); # 줄바꿈 문자로 분리

prediction = model.predict(target_images)
print(prediction)

result = {}
percentage = -1;
for i in range(len(target_images)): # 입력 받은 전체 이미지에 대하여
    score = tf.nn.softmax(prediction[i]) # 해당 어류일 확률
    np.set_printoptions(formatter={'float': lambda x: "{0:0.3f}".format(x)})

    print("Target Image : " + target_image_names[i])
    for j in range(len(class_names)):
        percentage = round((float)(100 * score[j]), 3)
        if(percentage > 10):
            result[class_names[j]] = percentage
            print(
            "해당 이미지는 {} 와 일치할 확률이 {:.2f} 퍼센트입니다."
            .format(class_names[j], percentage)
            )
    print("----------------------------------------\n")
