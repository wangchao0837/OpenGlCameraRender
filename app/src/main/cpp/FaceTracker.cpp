//
// Created by 7invensun on 2019-12-30.
//

#include "FaceTracker.h"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "native-lib", __VA_ARGS__)

FaceTracker::FaceTracker(const char *model, const char *seeta) {
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(
            makePtr<CascadeClassifier>(model));
    DetectionBasedTracker::Parameters detectorParams;
    //追踪器
    tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, detectorParams);

    faceAlignment = makePtr<seeta::FaceAlignment>(seeta);
}

void FaceTracker::detector(Mat src, vector<Rect2f> &rects) {

    vector<Rect> faces;

    tracker->process(src);

    tracker->getObjects(faces);


    if (faces.size()) {
        Rect face = faces[0];
        rects.push_back(Rect2f(face.x, face.y, face.width, face.height));


        seeta::FacialLandmark points[5];
        seeta::ImageData imageData(src.cols, src.rows);

        imageData.data = src.data;
        seeta::FaceInfo faceInfo;
        seeta::Rect bbox;

        bbox.x = face.x;
        bbox.y = face.y;
        bbox.width = face.width;
        bbox.height = face.height;

        faceInfo.bbox = bbox;
        faceAlignment->PointDetectLandmarks(imageData, faceInfo, points);

        for (int i = 0; i < 5; ++i) {
            rects.push_back(Rect2f(points[i].x, points[i].y, 0, 0));
        }

    }


}

void FaceTracker::startTracking() {
    tracker->run();
}

void FaceTracker::stopTracking() {
    tracker->stop();
}
