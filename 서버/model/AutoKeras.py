import matplotlib.pyplot as plt
import numpy as np
import os
import PIL
import tensorflow as tf
import pathlib
import autokeras as ak

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
data_dir.append(pathlib.Path(R'C:\Users\sever\Desktop\원본 통합본'))

total_count = len(data_dir)  # 전체 작업 카운트 (학습 데이터 경로 수)
batch_size = 256  # 개의 이미지마다 가중치 계산
img_height = 150  # need processing image size.
img_width = 150  # need processing image size.

clf = ak.ImageClassifier(overwrite=False, max_trials=20, objective="val_accuracy", max_model_size=63897083)

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
    """
      AUTOTUNE = tf.data.experimental.AUTOTUNE
      train_ds = train_ds.cache().shuffle(1000).prefetch(buffer_size=AUTOTUNE)
      val_ds = val_ds.cache().prefetch(buffer_size=AUTOTUNE)
    """
    """
      RGB 채널 값은 [0, 255] 범위에 있습니다. 신경망에는 이상적이지 않습니다. 
      일반적으로 입력 값을 작게 만들어야 합니다. 
      여기서는 Rescaling 레이어를 사용하여 값이 [0, 1]에 있도록 표준화합니다.
    """
    normalization_layer = layers.experimental.preprocessing.Rescaling(1. / 255)
    normalized_ds = train_ds.map(lambda x, y: (normalization_layer(x), y))

    image_batch, labels_batch = next(iter(normalized_ds))
    first_image = image_batch[0]
    # Notice the pixels values are now in `[0,1]`.
    print(np.min(first_image), np.max(first_image))

    if i == 0:
        history = clf.fit(train_ds, validation_data=val_ds, validation_split=0.0, epochs= 10)

        predicted_y = clf.predict(val_ds)
        print(predicted_y)

        # Evaluate the best model with testing data.
        print(clf.evaluate(val_ds))
        model = clf.export_model()
        print(type(model))  # <class 'tensorflow.python.keras.engine.training.Model'>
        model.save('./model_' + str(i) + '.h5')
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
        plt.savefig('./model/model_' + str(i) + '.png')

    else :
        model = load_model('./model_' + str(i - 1) + '.h5')
        model.summary()
        epochs = 15  # 학습 반복횟수

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

        # tf.keras.backend.clear_session()  # 메모리 누수를 막기 위해 세션 초기화
"""
