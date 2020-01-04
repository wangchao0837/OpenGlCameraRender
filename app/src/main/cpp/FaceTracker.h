//
// Created by 7invensun on 2019-12-30.
//

#ifndef CAMERAGLRENDER_FACETRACKER_H
#define CAMERAGLRENDER_FACETRACKER_H


#include <opencv2/opencv.hpp>
#include <opencv2/objdetect.hpp>
#include <vector>
#include "alignment/include/face_alignment.h"
#include <android/log.h>

using namespace std;
using namespace cv;

class CascadeDetectorAdapter : public DetectionBasedTracker::IDetector {
public:
    CascadeDetectorAdapter(Ptr<CascadeClassifier> detector) :
            IDetector(),
            Detector(detector) {
        CV_Assert(detector);
    }

    void detect(const cv::Mat &Image, std::vector<cv::Rect> &objects) {
        Detector->detectMultiScale(Image, objects, scaleFactor, minNeighbours, 0, minObjSize,
                                   maxObjSize);
    }

    virtual ~CascadeDetectorAdapter() {
    }

private:
    CascadeDetectorAdapter();

    Ptr<CascadeClassifier> Detector;
};

class FaceTracker {
public:
    FaceTracker(const char *model, const char *seeta);

    void detector(Mat src, vector<Rect2f> &rects);

    void startTracking();

    void stopTracking();

private:
    Ptr<DetectionBasedTracker> tracker;
    Ptr<seeta::FaceAlignment> faceAlignment;
};

#endif //CAMERAGLRENDER_FACETRACKER_H
