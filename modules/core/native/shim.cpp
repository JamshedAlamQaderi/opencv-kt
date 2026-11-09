// C shim for opencv_core. See modules/core/cinterop/include/opencvkt/core.h.
#include <cstring>
#include <string>

#include <opencv2/core.hpp>
#include <opencv2/core/utility.hpp>

#include "opencvkt/core.h"
#include "opencvkt_internal.hpp"

namespace ocvkt {

namespace {
thread_local std::string g_last_error;
thread_local bool g_has_error = false;
}  // namespace

void set_error(const char* message) {
    g_last_error = message ? message : "";
    g_has_error = true;
}

void clear_error() {
    g_has_error = false;
    g_last_error.clear();
}

}  // namespace ocvkt

using ocvkt::guard;
using ocvkt::guard_new;
using ocvkt::mat;

extern "C" {

const char* ocvkt_last_error(void) { return ocvkt::g_has_error ? ocvkt::g_last_error.c_str() : nullptr; }

const char* ocvkt_version_string(void) { return CV_VERSION; }

const char* ocvkt_build_information(void) {
    static const std::string info = cv::getBuildInformation();
    return info.c_str();
}

ocvkt_mat ocvkt_mat_new(void) { return new cv::Mat(); }

ocvkt_mat ocvkt_mat_new_filled(int32_t rows, int32_t cols, int32_t type, double s0, double s1, double s2, double s3) {
    return guard_new<cv::Mat>([&] { return new cv::Mat(rows, cols, type, cv::Scalar(s0, s1, s2, s3)); });
}

ocvkt_mat ocvkt_mat_clone(ocvkt_mat m) {
    return guard_new<cv::Mat>([&] { return new cv::Mat(mat(m).clone()); });
}

void ocvkt_mat_delete(ocvkt_mat m) { delete static_cast<cv::Mat*>(m); }

int32_t ocvkt_mat_rows(ocvkt_mat m) { return mat(m).rows; }
int32_t ocvkt_mat_cols(ocvkt_mat m) { return mat(m).cols; }
int32_t ocvkt_mat_type(ocvkt_mat m) { return mat(m).type(); }
int32_t ocvkt_mat_channels(ocvkt_mat m) { return mat(m).channels(); }
int32_t ocvkt_mat_depth(ocvkt_mat m) { return mat(m).depth(); }
int32_t ocvkt_mat_empty(ocvkt_mat m) { return mat(m).empty() ? 1 : 0; }
int64_t ocvkt_mat_total(ocvkt_mat m) { return static_cast<int64_t>(mat(m).total()); }
int64_t ocvkt_mat_elem_size(ocvkt_mat m) { return static_cast<int64_t>(mat(m).elemSize()); }

ocvkt_status ocvkt_mat_get_bytes(ocvkt_mat m, uint8_t* dst, int64_t dst_len) {
    return guard([&] {
        const cv::Mat& src = mat(m);
        const size_t row_bytes = static_cast<size_t>(src.cols) * src.elemSize();
        const size_t needed = row_bytes * static_cast<size_t>(src.rows);
        CV_Assert(dst_len >= 0 && static_cast<size_t>(dst_len) >= needed);
        if (src.isContinuous()) {
            if (needed) std::memcpy(dst, src.data, needed);
        } else {
            for (int r = 0; r < src.rows; ++r) std::memcpy(dst + r * row_bytes, src.ptr(r), row_bytes);
        }
    });
}

ocvkt_status ocvkt_mat_put_bytes(ocvkt_mat m, const uint8_t* src, int64_t src_len) {
    return guard([&] {
        cv::Mat& dst = mat(m);
        const size_t row_bytes = static_cast<size_t>(dst.cols) * dst.elemSize();
        const size_t needed = row_bytes * static_cast<size_t>(dst.rows);
        CV_Assert(src_len >= 0 && static_cast<size_t>(src_len) == needed);
        if (dst.isContinuous()) {
            if (needed) std::memcpy(dst.data, src, needed);
        } else {
            for (int r = 0; r < dst.rows; ++r) std::memcpy(dst.ptr(r), src + r * row_bytes, row_bytes);
        }
    });
}

ocvkt_status ocvkt_mat_set_to(ocvkt_mat m, double s0, double s1, double s2, double s3) {
    return guard([&] { mat(m).setTo(cv::Scalar(s0, s1, s2, s3)); });
}

ocvkt_status ocvkt_mat_convert_to(ocvkt_mat src, ocvkt_mat dst, int32_t rtype, double alpha, double beta) {
    return guard([&] { mat(src).convertTo(mat(dst), rtype, alpha, beta); });
}

}  // extern "C"
