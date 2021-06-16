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
#data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\Fish_Mirror_ALL'))
#data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\Fish_left'))
#data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\new_FishPhoto'))
#data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\Fish_up'))
#data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\Fish_right'))
#data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\new_FishPhoto_mirror'))
#data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\Fish_down'))
data_dir.append((pathlib.Path(R'C:\Users\sever\Desktop\통합')))

total_count = len(data_dir)  # 전체 작업 카운트 (학습 데이터 경로 수)
batch_size = 256  # 개의 이미지마다 가중치 계산, 메모리 문제 해결위해 낮춤
img_height = 128  # need processing image size.
img_width = 128  # need processing image size.

for i in range(total_count):  # 전체 작업 횟수에 대해
    train_ds = tf.keras.preprocessing.image_dataset_from_directory(  # 훈련 데이터셋
        data_dir[i],
        validation_split=0.2,  # training data 80% validation data 20%
        subset="training",
        seed=123,
        image_size=(img_height, img_width),
        batch_size=batch_size)

    val_ds = tf.keras.preprocessing.image_dataset_from_directory(  # 검증 데이터셋
        data_dir[i],
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

    """
      버퍼링된 프리페치를 사용하여 I/O가 차단되지 않고 디스크에서 데이터를 생성할 수 있도록 합니다. 
      데이터를 로드할 때 사용해야 하는 두 가지 중요한 메서드입니다.
      .cache()는 첫 번째 epoch 동안 디스크에서 이미지를 로드한 후 이미지를 메모리에 유지합니다. 
      이렇게 하면 모델을 훈련하는 동안 데이터세트가 병목 상태가 되지 않습니다. 
      데이터세트가 너무 커서 메모리에 맞지 않는 경우, 이 메서드를 사용하여 성능이 높은 온디스크 캐시를 생성할 수도 있습니다.
      .prefetch()는 훈련 중에 데이터 전처리 및 모델 실행과 겹칩니다.
      """
    """ 디스크 사용율 너무 높아서 중간에 오류가 발생하는 것 같음 비활성화
      AUTOTUNE = tf.data.experimental.AUTOTUNE
      train_ds = train_ds.cache().shuffle(1000).prefetch(buffer_size=AUTOTUNE)
      val_ds = val_ds.cache().prefetch(buffer_size=AUTOTUNE)
      """

    # 모댈 생성 시작
    if i == 0:  # 최초 학습 시에만 모델 옵션 지정
        """
          처음, 이미지를 입력받아서 전처리를 해주는 레이어가 있고, 
          그 아래로는 CNN 계층, 마지막으로 FC layer의 Dense 레이어를 추가
        """
        #https://www.pyimagesearch.com/2018/12/31/keras-conv2d-and-convolutional-layers/
        model = Sequential([
            layers.experimental.preprocessing.Rescaling(1./255, input_shape=(img_height, img_width, 3)),
            layers.Conv2D(16, 3, padding='same', activation='relu'),
            layers.MaxPooling2D(),
            layers.Conv2D(32, 3, padding='same', activation='relu'),
            layers.MaxPooling2D(),
            layers.Conv2D(64, 3, padding='same', activation='relu'),
            layers.MaxPooling2D(),
            layers.Dropout(0.3),
            layers.Flatten(),
            layers.Dense(128, activation='relu'),
            layers.Dense(len(class_names))

           #layers.Dense(len(class_names), activation='softmax'), #출력 층으로서, 전체 분류 개수만큼의 확률을 반환하고, 반환 된 데이터의 전체 합은 1
        ])
        # 오버 피팅 방지를 위해 배치 정규화 + L2 정규화 + 드롭아웃 적용
        # model.add(Dense(256, activation='relu',kernel_regularizer=keras.regularizers.l2(0.001))) # L2 정규화
        # model.add(Dropout(0.3))  # 30%의 노드를 랜덤으로 선택하여 이번 예측에 활용하지 않고 나머지 70%의 노드만 사용하여 예측값을 내기 때문에 오버 피팅을 방지
        # model.add(BatchNormalization()) # 배치 정규화
        model.compile(optimizer='adam',
                      loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
                      metrics=['accuracy'])  # 컴파일은 분류 문제이니 크로스 엔트로피, 옵티마이저는 아담
    else:  # 기존 모델 불러와서 재 학습
        model = load_model('./model_' + str(i - 1) + '.h5')

    model.summary()

    epochs = 10  # 학습 반복횟수
    history = model.fit(
        train_ds,
        validation_data=val_ds,
        epochs=epochs,
    )

    model.save('./model_' + str(i) + '.h5')

    # 학습 결과 시각화
    acc = history.history['accuracy']
    val_acc = history.history['val_accuracy']
    loss = history.history['loss']
    val_loss = history.history['val_loss']
    epochs_range = range(epochs)
    plt.figure(figsize=(8, 8))
    plt.subplot(1, 2, 1)
    plt.plot(epochs_range, acc, label='Training Accuracy')
    plt.plot(epochs_range, val_acc, label='Validation Accuracy')
    plt.legend(loc='lower right')
    plt.title('Training and Validation Accuracy')
    plt.subplot(1, 2, 2)
    plt.plot(epochs_range, loss, label='Training Loss')
    plt.plot(epochs_range, val_loss, label='Validation Loss')
    plt.legend(loc='upper right')
    plt.title('Training and Validation Loss')
    plt.savefig('./model_' + str(i) + '.png')

    #tf.keras.backend.clear_session()  # 메모리 누수를 막기 위해 세션 초기화
