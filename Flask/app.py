from flask import Flask
import flask
import cv2 as cv
import numpy as np
import pyttsx3

app = Flask(__name__)

whT = 320
confThreshold =0.5
nmsThreshold= 0.2
#### LOAD MODEL
## Coco Names
classesFile = r"C:\Users\hp\Desktop\EDI_OD\classes.names"
classNames = []
with open(classesFile, 'rt') as f:
    classNames = f.read().rstrip('\n').split('\n')
#print(classNames)
## Model Files
modelConfiguration = r"C:\Users\hp\Desktop\EDI_OD\yolov4-obj.cfg"
modelWeights = r"C:\Users\hp\Desktop\EDI_OD\yolov4-obj_3000.weights"
net = cv.dnn.readNetFromDarknet(modelConfiguration, modelWeights)
net.setPreferableBackend(cv.dnn.DNN_BACKEND_OPENCV)
net.setPreferableTarget(cv.dnn.DNN_TARGET_CPU)

def most_frequent(List):
	return max(set(List), key = List.count)

def findObjects(outputs,img):
    hT, wT, cT = img.shape
    bbox = []
    classIds = []
    confs = []
    for output in outputs:
        for det in output:
            scores = det[5:]
            classId = np.argmax(scores)
            confidence = scores[classId]
            if confidence > confThreshold:
                #w,h = int(det[2]*wT) , int(det[3]*hT)
                #x,y = int((det[0]*wT)-w/2) , int((det[1]*hT)-h/2)
                #bbox.append([x,y,w,h])
                classIds.append(classId)
                confs.append(float(confidence))
	if len(classIds) == 0:
		return "No object detected"
	else:
		returnvalue = most_frequent(classIds)
		return returnvalue

@app.route("/web",methods=["GET","POST"])
def hello2():
	return flask.render_template('submit.html')


@app.route("/api",methods=["GET","POST"])
def hello2():
	img = flask.request.files["data_file"].read()
	img = np.frombuffer(img, np.uint8)
	# convert numpy array to image
	img = cv.imdecode(img,cv.IMREAD_COLOR)
	blob = cv.dnn.blobFromImage(img, 1 / 255, (whT, whT), [0, 0, 0], 1, crop=False)
	net.setInput(blob)
	layersNames = net.getLayerNames()
	outputNames = [(layersNames[i[0] - 1]) for i in net.getUnconnectedOutLayers()]
	outputs = net.forward(outputNames)
	objdetected = findObjects(outputs,img)
	if objdetected == "No object detected":
		return objdetected
	objdetected = str(classNames[objdetected])
	return str(objdetected)

if __name__ == "__main__":
	app.run()
#
