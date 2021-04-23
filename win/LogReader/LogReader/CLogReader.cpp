#include "CLogReader.h"

CLogReader::CLogReader() = default;

CLogReader::~CLogReader() = default;

bool CLogReader::setFilter(const char* filter) {
	pattern = Pattern(filter);
	return !pattern.hasErrors();
}

bool CLogReader::addSourceBlock(const char* block, const size_t blockSize) {
	bool blockAdded = false;
	try {
		builder.append(block, blockSize);
		blockAdded = true;
	}
	catch (...) {
		builder.clear();
	}
	return blockAdded;
}

