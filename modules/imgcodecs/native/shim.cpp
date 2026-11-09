// C shim for opencv_imgcodecs. See modules/imgcodecs/include/opencvkt/imgcodecs.h.
#include <vector>

#include <opencv2/imgcodecs.hpp>

#include "opencvkt/imgcodecs.h"
#include "opencvkt_internal.hpp"

using ocvkt::guard;
using ocvkt::guard_new;
using ocvkt::mat;

static std::vector<int> to_params(const int32_t* params, int32_t len) {
    return params && len > 0 ? std::vector<int>(params, params + len) : std::vector<int>();
}

extern "C" {

ocvkt_mat ocvkt_imgcodecs_imread(const char* filename, int32_t flags) {
    return guard_new<cv::Mat>([&] { return new cv::Mat(cv::imread(filename, flags)); });
}

ocvkt_status ocvkt_imgcodecs_imwrite(const char* filename, ocvkt_mat img, const int32_t* params, int32_t params_len,
                                     int32_t* out_ok) {
    return guard([&] {
        const bool ok = cv::imwrite(filename, mat(img), to_params(params, params_len));
        if (out_ok) *out_ok = ok ? 1 : 0;
    });
}

ocvkt_mat ocvkt_imgcodecs_imdecode(const uint8_t* buf, int64_t len, int32_t flags) {
    return guard_new<cv::Mat>([&] {
        const cv::Mat raw(1, static_cast<int>(len), CV_8UC1, const_cast<uint8_t*>(buf));
        return new cv::Mat(cv::imdecode(raw, flags));
    });
}

ocvkt_status ocvkt_imgcodecs_imencode(const char* ext, ocvkt_mat img, const int32_t* params, int32_t params_len,
                                      ocvkt_mat out_buf, int32_t* out_ok) {
    return guard([&] {
        std::vector<uchar> bytes;
        const bool ok = cv::imencode(ext, mat(img), bytes, to_params(params, params_len));
        cv::Mat(bytes, true).reshape(1, 1).copyTo(mat(out_buf));
        if (out_ok) *out_ok = ok ? 1 : 0;
    });
}

}  // extern "C"
