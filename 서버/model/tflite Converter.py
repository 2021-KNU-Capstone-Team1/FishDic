from tensorflow import keras
import tensorflow as tf

# h5 to pb
model = keras.models.load_model('./model.h5', compile=False) # 원본 모델
export_path = './output_pb' # pb 파일 출력 경로
model.save(export_path, save_format="tf")

# pb to tflite
saved_model_dir = export_path
converter = tf.lite.TFLiteConverter.from_saved_model(saved_model_dir)
converter.target_spec.supported_ops = [tf.lite.OpsSet.TFLITE_BUILTINS,
                                       tf.lite.OpsSet.SELECT_TF_OPS]
tflite_model = converter.convert()
open('./model.tflite', 'wb').write(tflite_model) # tflite로 변환 된 모델
