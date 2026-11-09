/* opencv-kt C ABI — photo module. Implemented in modules/photo/native/shim.cpp. */
#ifndef OPENCVKT_PHOTO_H
#define OPENCVKT_PHOTO_H

#include "opencvkt/prelude.h"

OCVKT_BEGIN_DECLS

OCVKT_API ocvkt_status ocvkt_photo_inpaint(ocvkt_mat src, ocvkt_mat mask, ocvkt_mat dst, double radius, int32_t flags);

OCVKT_API ocvkt_status ocvkt_photo_fast_nl_means_denoising(ocvkt_mat src, ocvkt_mat dst, float h,
                                                           int32_t template_window, int32_t search_window);

OCVKT_END_DECLS

#endif /* OPENCVKT_PHOTO_H */
