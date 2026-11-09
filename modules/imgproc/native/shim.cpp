// C shim for opencv_imgproc. See modules/imgproc/include/opencvkt/imgproc.h.
#include <opencv2/imgproc.hpp>

#include "opencvkt/imgproc.h"
#include "opencvkt_internal.hpp"

using ocvkt::guard;
using ocvkt::mat;

extern "C" {

ocvkt_status ocvkt_imgproc_cvt_color(ocvkt_mat src, ocvkt_mat dst, int32_t code, int32_t dst_cn) {
    return guard([&] { cv::cvtColor(mat(src), mat(dst), code, dst_cn); });
}

ocvkt_status ocvkt_imgproc_gaussian_blur(ocvkt_mat src, ocvkt_mat dst, int32_t ksize_w, int32_t ksize_h,
                                         double sigma_x, double sigma_y, int32_t border_type) {
    return guard([&] {
        cv::GaussianBlur(mat(src), mat(dst), cv::Size(ksize_w, ksize_h), sigma_x, sigma_y, border_type);
    });
}

ocvkt_status ocvkt_imgproc_resize(ocvkt_mat src, ocvkt_mat dst, int32_t width, int32_t height, double fx, double fy,
                                  int32_t interpolation) {
    return guard([&] { cv::resize(mat(src), mat(dst), cv::Size(width, height), fx, fy, interpolation); });
}

ocvkt_status ocvkt_imgproc_threshold(ocvkt_mat src, ocvkt_mat dst, double thresh, double maxval, int32_t type,
                                     double* out_thresh) {
    return guard([&] {
        const double t = cv::threshold(mat(src), mat(dst), thresh, maxval, type);
        if (out_thresh) *out_thresh = t;
    });
}

ocvkt_status ocvkt_imgproc_canny(ocvkt_mat image, ocvkt_mat edges, double threshold1, double threshold2,
                                 int32_t aperture_size, int32_t l2_gradient) {
    return guard([&] {
        cv::Canny(mat(image), mat(edges), threshold1, threshold2, aperture_size, l2_gradient != 0);
    });
}

}  // extern "C"
