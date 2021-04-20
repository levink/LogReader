#pragma once

namespace util {
	inline bool isLineEnding(const char c) {
		return c == '\n' || c == '\r';
	}
	inline size_t strlen(const char* str) {
		if (str == nullptr) {
			return 0;
		}

		size_t i = 0;
		while (str[i]) i++;
		return i;
	}
	inline char* createCopy(const char* str, const size_t size) {
		
		if (str == nullptr) {
			return nullptr;
		}

		const auto result = new char[size + 1];
		for (size_t i = 0; i < size; i++) {
			result[i] = str[i];
		}
		result[size] = 0;
		return result;
	}
}