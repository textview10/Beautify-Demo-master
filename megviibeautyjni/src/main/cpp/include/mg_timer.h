//
// Created by Xie Jiantao on 2018/2/26.
//

#ifndef MEGVIICLUSTER_MG_TIMER_H
#define MEGVIICLUSTER_MG_TIMER_H

#include <chrono>
#include <android/log.h>
#include <malloc.h>
#define LOG_TIMER(TAG, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG,  __VA_ARGS__)
class Timer {
    const char* m_name;

    const char* m_tag;
    std::chrono::high_resolution_clock::time_point m_start;

public:
    Timer():m_name(nullptr), m_tag(nullptr) {
        m_start = std::chrono::high_resolution_clock::now();
    }
    ~Timer(){
        end();
    }

    void start(const char* tag, const char* name){
        if(m_name!= nullptr){
            end();
        }
        m_start = std::chrono::high_resolution_clock::now();
        m_tag = tag;
        m_name = name;
        struct mallinfo info = mallinfo();
        LOG_TIMER(m_tag, "start %s", m_name);
    }

    void end() {
        if (m_name == nullptr){
            return;
        }
        struct mallinfo info = mallinfo();
        LOG_TIMER(m_tag, "end %s, time is %.3fms", m_name, get_msecs());

        m_name = nullptr;
    }

    double get_secs() const {
        auto now = std::chrono::high_resolution_clock::now();
        return std::chrono::duration_cast<std::chrono::nanoseconds>(now -
                                                                    m_start)
                       .count() *
               1e-9;
    }

    double get_msecs() const { return get_secs() * 1e3; }

    double get_secs_reset() {
        auto ret = get_secs();
        return ret;
    }

    double get_msecs_reset() { return get_secs_reset() * 1e3; }



};


#endif //MEGVIICLUSTER_MG_TIMER_H
