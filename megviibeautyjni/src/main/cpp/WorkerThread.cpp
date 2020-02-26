#include <WorkerThread.h>
#include <iostream>

using namespace std;


#define MSG_EXIT_THREAD			1
#define MSG_POST_MSG			2

struct ThreadMsg
{
	ThreadMsg(int i, const void* m) { id = i; msg = m; }
	int id;
	const void* msg;
};

//----------------------------------------------------------------------------
// WorkerThread
//----------------------------------------------------------------------------
WorkerThread::WorkerThread(const char* threadName) : m_thread(0), m_timerExit(false), THREAD_NAME(threadName)
{
}

//----------------------------------------------------------------------------
// ~WorkerThread
//----------------------------------------------------------------------------
WorkerThread::~WorkerThread()
{
	//ExitThread();
}

//----------------------------------------------------------------------------
// CreateThread
//----------------------------------------------------------------------------
bool WorkerThread::CreateThread()
{
    LOGETT("CreateThread in");
	if (m_thread == nullptr)
		m_thread = new thread(&WorkerThread::Process, this);
    LOGETT("CreateThread out");
	return true;
}

//----------------------------------------------------------------------------
// ExitThread
//----------------------------------------------------------------------------
void WorkerThread::ExitThread()
{
	if (nullptr == m_thread)
		return;
	// Create a new ThreadMsg
	ThreadMsg* threadMsg = new ThreadMsg(MSG_EXIT_THREAD, 0);
	// Put exit thread message into the queue
	{
        std::lock_guard<std::mutex> lk(m_mutex);
		m_queue.push(threadMsg);
        m_timerExit = true;
		m_cv.notify_all();
	}
	m_thread->join();
	delete m_thread;
    m_thread = nullptr;
	m_thread = 0;
}

//----------------------------------------------------------------------------
// PostMsg
//----------------------------------------------------------------------------
bool WorkerThread::PostMsg(const UserCallBack* userCallBack)
{
	ThreadMsg* threadMsg = new ThreadMsg(MSG_POST_MSG, userCallBack);

	// Add user data msg to queue and notify worker thread
	std::lock_guard<std::mutex> lk(m_mutex);
    if(m_timerExit){
        return false;
    }
	m_queue.push(threadMsg);
	m_cv.notify_all();
    return true;
}

//----------------------------------------------------------------------------
// Process
//----------------------------------------------------------------------------
void WorkerThread::Process()
{
	while (true)
	{
		ThreadMsg* msg = nullptr;
		{
			// Wait for a message to be added to the queue
			std::unique_lock<std::mutex> lk(m_mutex);
			while (m_queue.empty()) {
                m_cv.wait(lk);
            }
			msg = m_queue.front();
			m_queue.pop();
		}

		switch (msg->id)
		{
			case MSG_POST_MSG:
			{
				// Convert the ThreadMsg void* data back to a UserCallBack*
				const UserCallBack* userData = static_cast<const UserCallBack*>(msg->msg);

				userData->func(userData->cookie);

				// Delete dynamic data passed through message queue
				delete userData;
				delete msg;
				break;
			}

			case MSG_EXIT_THREAD:
			{
				delete msg;
				return;
			}
		}
	}
}


