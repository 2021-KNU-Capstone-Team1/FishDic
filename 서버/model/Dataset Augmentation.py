from imgaug import augmenters as iaa
import numpy as np
import cv2
import os
import matplotlib.pyplot as plt


# 이미지 읽어오는 함수
def load_images_from_folder(folder):
    images = []
    for filename in os.listdir(folder):  # 리스트에서 이미지 파일만 읽어옴
        print(os.path.join(folder, filename))
        file =os.path.join(folder, filename)
        img = cv2.imread(file, cv2.IMREAD_UNCHANGED)
        if img is None :
            print("Can't read Images")
        else :
            images.append(img)
    return images


# 이미지 저장 함수
def write_images(name, number, images):
    for i in range(0, len(images)):
        name_slice = name[43:]
        cv2.imwrite(R'C:\Users\sever\Desktop\img_argumentation\%s\mo_%s_%d.jpg' % (name_slice, number, i), images[i])  # 이미지 저장할 경로 설정
    print("image saving complete")


# 여러 폴더에 한번에 저장하기 
def imagewriterfunction(folder, images):
    for i in range(0, len(images)):
        write_images(folder, str(i), images[i])
    print("all images saved to folder")


# 이미지 증강 코드
def augmentations1(images):
    seq1 = iaa.Sequential([
        iaa.AverageBlur(k=(2, 7)),
        iaa.MedianBlur(k=(3, 11))
    ])

    seq2 = iaa.ChannelShuffle(p=1.0)
    seq3 = iaa.Dropout((0.05, 0.1), per_channel=0.5)
    seq4 = iaa.Sequential([
        iaa.Add((-15, 15)),
        iaa.Multiply((0.3, 1.5))
    ])
    print("image augmentation beginning")
    images_aug1 = seq1.augment_images(images)
    print("sequence 1 completed......")
    images_aug2 = seq2.augment_images(images)
    print("sequence 2 completed......")
    images_aug3 = seq3.augment_images(images)
    print("sequence 3 completed......")
    images_aug4 = seq4.augment_images(images)
    print("sequence 4 completed......")
    print("proceed to next augmentations")
    list = [images_aug1, images_aug2, images_aug3, images_aug4]
    return list
photos = R'C:\Users\sever\Desktop\total_original_fish'  # 이미지 읽어올 경로
folders = []
folders = os.listdir(photos)  # 이미지 경로 내 파일 및 디렉토리 리스트

print(folders)

print("img_scanning start")

photo = []

for i in range(len(folders)) :
    photo = load_images_from_folder(os.path.join(photos, folders[i]))
    photo_augmented1234 = augmentations1(photo)  # 이미지 증강 0,1,2,3 이 리스트 형태로 있다
    for j in range(len(photo_augmented1234)):
        imagewriterfunction(os.path.join(photos, folders[i]), photo_augmented1234)
# write_images('저장할 폴더 이름', '각 이미지에 붙일 이름', photos_augmented1234[0])
""""  
photos1 = load_images_from_folder(os.path.join(photos, folders[0]))
photos2 = load_images_from_folder(os.path.join(photos, folders[1]))
photos3 = load_images_from_folder(os.path.join(photos, folders[2]))

photo_augmented1234 = augmentations1(photos1)  # 이미지 증강 0,1,2,3 이 리스트 형태로 있다

write_images('저장할 폴더 이름', '각 이미지에 붙일 이름', photos_augmented1234[0])
"""
