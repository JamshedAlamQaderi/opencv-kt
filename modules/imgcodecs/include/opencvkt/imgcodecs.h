/* opencv-kt C ABI — imgcodecs module. Implemented in modules/imgcodecs/native/shim.cpp. */
#ifndef OPENCVKT_IMGCODECS_H
#define OPENCVKT_IMGCODECS_H

#include "opencvkt/prelude.h"

OCVKT_BEGIN_DECLS

/** New Mat with the decoded image; empty Mat if the file can't be read. NULL only on exception. */
OCVKT_API ocvkt_mat ocvkt_imgcodecs_imread(const char* filename, int32_t flags);

/** *out_ok = 1 if the file was written. `params` is a flat [key, value, key, value, ...] array (may be NULL). */
OCVKT_API ocvkt_status ocvkt_imgcodecs_imwrite(const char* filename, ocvkt_mat img, const int32_t* params,
                                               int32_t params_len, int32_t* out_ok);

/** New Mat decoded from an in-memory buffer; empty Mat if undecodable. NULL only on exception. */
OCVKT_API ocvkt_mat ocvkt_imgcodecs_imdecode(const uint8_t* buf, int64_t len, int32_t flags);

/** Encodes `img` with the codec selected by `ext` (".png", ".jpg", ...) into `out_buf` (resized to 1 x N CV_8U). */
OCVKT_API ocvkt_status ocvkt_imgcodecs_imencode(const char* ext, ocvkt_mat img, const int32_t* params,
                                                int32_t params_len, ocvkt_mat out_buf, int32_t* out_ok);

OCVKT_END_DECLS

#endif /* OPENCVKT_IMGCODECS_H */
