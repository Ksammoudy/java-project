import sys
import json
import os
from deepface import DeepFace

if len(sys.argv) < 2:
    print("ERROR_NO_IMAGE")
    sys.exit()

image_path = sys.argv[1]

print("IMAGE_PATH=" + image_path)
print("IMAGE_EXISTS=" + str(os.path.exists(image_path)))

try:
    result = DeepFace.represent(
        img_path=image_path,
        model_name="VGG-Face",
        detector_backend="opencv",
        enforce_detection=False
    )

    if result is None or len(result) == 0:
        print("ERROR_EMPTY_RESULT")
        sys.exit()

    embedding = result[0]["embedding"]
    print(json.dumps(embedding))

except Exception as e:
    msg = str(e).encode("ascii", "ignore").decode()
    print("REAL_ERROR=" + msg)
    print("ERROR_FACE")
