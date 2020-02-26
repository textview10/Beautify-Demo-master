#ifndef _THREAD_STD_H
#define _THREAD_STD_H

#include <thread>
#include <queue>
#include <mutex>
#include <atomic>
#include <condition_variable>
#include <string>
#include <android/log.h>
//#define  LOGETT(...)  __android_log_print(ANDROID_LOG_ERROR,"beautify_ext_handler",__VA_ARGS__)

#define  LOGETT(...)

class UserCallBack
{
public:
	void* (*func)(void* cookie);
	void* cookie;
};

/**
 *
 */
template<typename Ret_T>
class Callable {
public:
	virtual Ret_T call() = 0;
	virtual ~Callable(){};
};


template<typename Ret_T>
class CallableProxy{
public:
    Callable<Ret_T>* mCallable;
	Ret_T ret;

	std::mutex m_mutex;
	std::condition_variable m_cv;
	bool  processed = false;

	CallableProxy(Callable<Ret_T>* callable){
		mCallable = callable;
	}

	void process(){
        LOGETT("WorkerThread::CallableProxy::process in");
		ret = mCallable->call();
        LOGETT("WorkerThread::CallableProxy::process lock before");
		std::unique_lock<std::mutex> lk(m_mutex);
        LOGETT("WorkerThread::CallableProxy::process lock out");
		processed = true;
		m_cv.notify_all();
        LOGETT("WorkerThread::CallableProxy::process out");
	}

	Ret_T waitDone(){
        LOGETT("WorkerThread::CallableProxy::waitDone in");
		std::unique_lock<std::mutex> lk(m_mutex);
        m_cv.wait(lk, [this] { return this->processed; });
        LOGETT("WorkerThread::CallableProxy::waitDone out");
		return ret;
	}

	static void* processData(void* data){
		CallableProxy<Ret_T>* proxy = static_cast<CallableProxy<Ret_T> *>(data);
		proxy->process();
		return proxy;
	}
};

template<typename Ret_T>
class AsynCallableProxy{
public:
	Callable<Ret_T>* mCallable;
	Ret_T ret;

	AsynCallableProxy(Callable<Ret_T>* callable){
		mCallable = callable;
	}
	void process(){
		ret = mCallable->call();
        delete mCallable;
        mCallable = nullptr;
	}

	static void* processData(void* data){
		AsynCallableProxy<Ret_T>* proxy = static_cast<AsynCallableProxy<Ret_T> *>(data);
		proxy->process();
        delete proxy;
		return nullptr;
	}
};

struct ThreadMsg;

class WorkerThread 
{
public:

	WorkerThread(const char* threadName = "test");

	~WorkerThread();

	bool CreateThread();

	void ExitThread();

	bool PostMsg(const UserCallBack* userCallBack);

	template<typename Ret_T>
	void asynCall(Callable<Ret_T>* callable){
		AsynCallableProxy<Ret_T>* asynCallableProxy = new AsynCallableProxy<Ret_T>(callable);
		UserCallBack* userCallBack = new UserCallBack();
		userCallBack->cookie = asynCallableProxy;
		userCallBack->func = &AsynCallableProxy<Ret_T>::processData;
		PostMsg(userCallBack);
		return;
	}

	template<typename Ret_T>
	Ret_T call(Callable<Ret_T>* callable){
		CallableProxy<Ret_T>* callableProxy = new CallableProxy<Ret_T>(callable);
		UserCallBack* userCallBack = new UserCallBack();
		userCallBack->cookie = callableProxy;
		userCallBack->func = &CallableProxy<Ret_T>::processData;
		bool posted = PostMsg(userCallBack);
        if(posted) {
            Ret_T ret = callableProxy->waitDone();
            delete callableProxy;
            return ret;
        } else {
            delete callableProxy;
            return static_cast<Ret_T>(0);
        }
	}
private:
	void Process();
	std::thread* m_thread = nullptr;
	std::queue<ThreadMsg*> m_queue;
	std::mutex m_mutex;
	std::condition_variable m_cv;
	std::atomic<bool> m_timerExit;
	const char* THREAD_NAME;
};

#endif