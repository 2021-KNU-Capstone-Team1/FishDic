import matplotlib.pyplot as plt
import numpy as np
import os
import PIL
import tensorflow as tf
import pathlib

from tensorflow import keras
from tensorflow.keras import layers
from tensorflow.keras.models import Sequential
from tensorflow.keras import initializers
from tensorflow.keras import losses

from PIL import Image
import os, glob, numpy as np
from tensorflow.keras.models import load_model
from tensorflow.python.keras.layers.core import Dense, Dropout
from tensorflow.python.keras.layers.normalization_v2 import BatchNormalization

data_dir = []  # 학습 데이터 경로
data_dir.append((pathlib.Path(R'C:\Users\sever\Desktop\통합')))

batch_size = 512  # 개의 이미지마다 가중치 계산, 메모리 문제 해결위해 낮춤
img_height = 128  # need processing image size.
img_width = 128  # need processing image size.

train_ds = tf.keras.preprocessing.image_dataset_from_directory(  # 훈련 데이터셋
    data_dir[0],
    validation_split=0.2,  # training data 80% validation data 20%
    subset="training",
    seed=123,
    image_size=(img_height, img_width),
    batch_size=batch_size)

val_ds = tf.keras.preprocessing.image_dataset_from_directory(  # 검증 데이터셋
    data_dir[0],
    validation_split=0.2,
    subset="validation",
    seed=123,
    image_size=(img_height, img_width),
    batch_size=batch_size)

class_names = train_ds.class_names  # 분류 이름
print(class_names)
with open("./label.txt", "w") as txt_file:  # 분류 이름 재사용 위한 텍스트 파일로 출력
    for line in class_names:
        txt_file.write(line + "\n")

# 사전 훈련된 모델 MobileNet V2에서 기본 모델을 생성합니다.
base_model = tf.keras.applications.MobileNetV2(input_shape=(img_height, img_width, 3),
                                               include_top=False,  # 맨 위층(분류 층) 제외
                                               weights='imagenet')

for image_batch, label_batch in train_ds.take(1):
    pass

image_batch.shape
feature_batch = base_model(image_batch)  # 224x224 이미지가 7x7x1280의 특징 블록으로 변환됨을 확인할 수 있음
print(feature_batch.shape)

base_model.trainable = False
# 기본 모델 아키텍처를 살펴봅니다.
base_model.summary()

"""
분류 층을 맨 위에 추가하기
특징 블록에서 예측을 하기위해 tf.keras.layers.GlobalAveragePooling2D 층을 사용하여 특징을 이미지 한개 당 1280개의 요소 벡터로 
변환하여 7x7 공간 위치에 대한 평균을 구하세요.
"""
global_average_layer = tf.keras.layers.GlobalAveragePooling2D()
feature_batch_average = global_average_layer(feature_batch)
print(feature_batch_average.shape)
"""
tf.keras.layers.Dense층을 사용하여 특징을 예측으로 변환
"""
prediction_layer = keras.layers.Dense(len(class_names))
prediction_batch = prediction_layer(feature_batch_average)
print(prediction_batch.shape)

model = tf.keras.Sequential([
    base_model,
    global_average_layer,
    prediction_layer
])

# base_learning_rate = 0.0001

# 클래스 분류 문제에서 softmax 함수를 거치면 from_logits = False
model.compile(loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              # 다중 클래스 분류 문제이므로, CategoricalCrosstentropy 사용
              optimizer='adam',
              metrics=['accuracy'])

model.summary()
len(model.trainable_variables)

initial_epochs = 10
validation_steps = 20

loss0, accuracy0 = model.evaluate(val_ds, steps=validation_steps)
print("initial loss: {:.2f}".format(loss0))
print("initial accuracy: {:.2f}".format(accuracy0))
history = model.fit(train_ds,
                    epochs=initial_epochs,
                    validation_data=val_ds)

acc = history.history['accuracy']
val_acc = history.history['val_accuracy']
loss = history.history['loss']
val_loss = history.history['val_loss']

plt.figure(figsize=(8, 8))
plt.subplot(2, 1, 1)
plt.plot(acc, label='Training Accuracy')
plt.plot(val_acc, label='Validation Accuracy')
plt.legend(loc='lower right')
plt.ylabel('Accuracy')
plt.ylim([min(plt.ylim()), 1])
plt.title('Training and Validation Accuracy')

plt.subplot(2, 1, 2)
plt.plot(loss, label='Training Loss')
plt.plot(val_loss, label='Validation Loss')
plt.legend(loc='upper right')
plt.ylabel('Cross Entropy')
plt.ylim([0, 1.0])
plt.title('Training and Validation Loss')
plt.xlabel('epoch')
plt.savefig('./mobile_model.png')

model.save('./mobile_model.h5')
