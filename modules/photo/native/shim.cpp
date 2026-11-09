// C shim for opencv_photo. See modules/photo/include/opencvkt/photo.h.
#include <opencv2/photo.hpp>

#include "opencvkt/photo.h"
#include "opencvkt_internal.hpp"

using ocvkt::guard;
using ocvkt::mat;

extern "C" {

ocvkt_status ocvkt_photo_inpaint(ocvkt_mat src, ocvkt_mat mask, ocvkt_mat dst, double radius, int32_t flags) {
    return guard([&] { cv::inpaint(mat(src), mat(mask), mat(dst), radius, flags); });
}

ocvkt_status ocvkt_photo_fast_nl_means_denoising(ocvkt_mat src, ocvkt_mat dst, float h, int32_t template_window,
                                                 int32_t search_window) {
    return guard([&] { cv::fastNlMeansDenoising(mat(src), mat(dst), h, template_window, search_window); });
}

}  // extern "C"
