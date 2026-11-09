// Internal helpers shared by every module's C++ shim (never exposed to Kotlin).
#pragma once

#include <exception>
#include <string>
#include <utility>

#include <opencv2/core.hpp>

#include "opencvkt/prelude.h"

namespace ocvkt {

void set_error(const char* message);
void clear_error();

// Runs `body`, translating any C++ exception into a status code + thread-local error message.
// C++ exceptions must never cross the C ABI (Kotlin/Native and JNI cannot unwind them).
template <class F>
inline ocvkt_status guard(F&& body) noexcept {
    clear_error();
    try {
        std::forward<F>(body)();
        return 0;
    } catch (const cv::Exception& e) {
        set_error(e.what());
        return 1;
    } catch (const std::exception& e) {
        set_error(e.what());
        return 2;
    } catch (...) {
        set_error("unknown C++ exception");
        return 3;
    }
}

// Same as guard(), but for functions that return a freshly allocated object (NULL on failure).
template <class T, class F>
inline T* guard_new(F&& body) noexcept {
    T* result = nullptr;
    guard([&] { result = std::forward<F>(body)(); });
    return result;
}

inline cv::Mat& mat(ocvkt_mat handle) { return *static_cast<cv::Mat*>(handle); }

}  // namespace ocvkt
