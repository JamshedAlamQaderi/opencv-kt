/* opencv-kt C ABI — imgproc module. Implemented in modules/imgproc/native/shim.cpp. */
#ifndef OPENCVKT_IMGPROC_H
#define OPENCVKT_IMGPROC_H

#include "opencvkt/prelude.h"

OCVKT_BEGIN_DECLS

OCVKT_API ocvkt_status ocvkt_imgproc_cvt_color(ocvkt_mat src, ocvkt_mat dst, int32_t code, int32_t dst_cn);

OCVKT_API ocvkt_status ocvkt_imgproc_gaussian_blur(ocvkt_mat src, ocvkt_mat dst, int32_t ksize_w, int32_t ksize_h,
                                                   double sigma_x, double sigma_y, int32_t border_type);

OCVKT_API ocvkt_status ocvkt_imgproc_resize(ocvkt_mat src, ocvkt_mat dst, int32_t width, int32_t height,
                                            double fx, double fy, int32_t interpolation);

/** Writes the computed threshold value (relevant for THRESH_OTSU / THRESH_TRIANGLE) to *out_thresh. */
OCVKT_API ocvkt_status ocvkt_imgproc_threshold(ocvkt_mat src, ocvkt_mat dst, double thresh, double maxval,
                                               int32_t type, double* out_thresh);

OCVKT_API ocvkt_status ocvkt_imgproc_canny(ocvkt_mat image, ocvkt_mat edges, double threshold1, double threshold2,
                                           int32_t aperture_size, int32_t l2_gradient);

OCVKT_END_DECLS

#endif /* OPENCVKT_IMGPROC_H */
