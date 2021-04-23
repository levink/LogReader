#pragma once
#include "Pattern.h"

class CLogReader {
	Pattern pattern;
	StringBuilder builder;
public:
	CLogReader();
    ~CLogReader();

	bool setFilter(const char* filter);
	bool addSourceBlock(const char* block, size_t blockSize);
	
	template<typename Func> 
	void parse(bool last, Func callback);
};

template <typename Func>
void CLogReader::parse(const bool last, Func callback) {

    if (builder.isEmpty()) {
        return;
    }

    auto it = builder.begin();
    while (it.hasCompleteLines()) {
        if (pattern.match(it)) {
            const String& str = StringBuilder::extractLine(it);
            callback(str);
        }
        it.skipLine();
    }

    if (last) {
        if (pattern.match(it)) {
            const String& str = StringBuilder::extractAll(it);
            callback(str);
        }
        it.skipAll();
    }

    builder.clearBefore(it);
}
